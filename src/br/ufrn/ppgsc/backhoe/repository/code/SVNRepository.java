package br.ufrn.ppgsc.backhoe.repository.code;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.exceptions.NonUniqueCommitByRevisionException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;

public class SVNRepository extends AbstractRepository implements CodeRepository {
	
	private org.tmatesoft.svn.core.io.SVNRepository repository;
	private AbstractDeveloperDAO developerDao;
	private AbstractCommitDAO commitDao;
	
	public SVNRepository(String username, String password, String url){
		super(username, password, url);
		try {
			this.developerDao = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
			this.commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
		} catch (DAONotFoundException e) {
			e.printStackTrace();
		}
	}

	public SVNRepository() {
		this(null, null, null);
	}

	public boolean connect() throws MissingParameterException {
		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if(username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if(password == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		
		DAVRepositoryFactory.setup();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
			repository.setAuthenticationManager(authManager);
			return true;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths) {
		return this.findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths);
	}
	
	@Override
	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths, List<String> ignoredPaths) {
		
		List<Commit> commits = new ArrayList<Commit>();

		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());

		try {
			System.out.print("Finding commits in the date interval: "+startDate.toString()+" - "+endDate.toString()+" ... ");
			
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			
			System.out.println("Finished!\n"+svnLogEntries.size()+" commits found!");
			
			for (SVNLogEntry svnLogEntry : svnLogEntries){
				if(developers == null || developers != null && developers.contains(svnLogEntry.getAuthor())){
					boolean betweenSearchRange = svnLogEntry.getDate().after(startDate) && svnLogEntry.getDate().before(endDate);
					if(betweenSearchRange){
						Commit commit = commitDao.findByRevision(svnLogEntry.getRevision());
						
						if(commit == null){
							commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
							commitDao.save(commit);
						}else if(commit != null && commit.getChangedPaths() == null && commit.getChangedPaths().isEmpty() && collectChangedPaths){
							commit.setChangedPaths(findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths));
							commitDao.update(commit);
						}
						commits.add(commit);
					}
				}
			}
			if(developers != null)
				System.out.println(commits.size()+" of "+svnLogEntries.size()+" belong to informed developers");
			return commits;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ChangedPath> findChangedPathsBySVNLogEntry(SVNLogEntry svnLogEntry, Commit associatedCommit, List<String> ignoredPaths){
		Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
		ArrayList<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
		for (SVNLogEntryPath svnLogEntryPath : svnLogEntryPaths) {
			ChangedPath changedPath = createChangedPath(svnLogEntryPath, associatedCommit, ignoredPaths);
			changedPaths.add(changedPath);
		}
		return changedPaths;
	}
	
	private ChangedPath createChangedPath(SVNLogEntryPath svnLogEntryPath, Commit associatedCommit, List<String> ignoredPaths){
		ChangedPath changedPath = null;
		if(!ignoredPaths.contains(svnLogEntryPath.getPath())){	
			changedPath = new ChangedPath();
			changedPath.setChangeType(svnLogEntryPath.getType());
			changedPath.setCommit(associatedCommit);
			changedPath.setPath(svnLogEntryPath.getPath());
			if(!changedPath.getChangeType().equals('D')){
				changedPath.setContent(getFileContent(changedPath.getPath(), changedPath.getCommit().getRevision()));
			}
		}
		return changedPath;
	}
	
	private Commit createCommit(SVNLogEntry svnLogEntry, boolean collectChangedPaths, List<String> ignoredPaths){
		Commit commit = new Commit();
		Developer developer = developerDao.findByCodeRepositoryUsername(svnLogEntry.getAuthor());
		if(developer == null) {
			developer = new Developer();
			developer.setCodeRepositoryUsername(svnLogEntry.getAuthor());
			developerDao.save(developer);
		}
		commit.setAuthor(developer);
		commit.setComment(svnLogEntry.getMessage());
		commit.setCreatedAt(svnLogEntry.getDate());
		commit.setRevision(svnLogEntry.getRevision());
		if(collectChangedPaths){
			List<ChangedPath> changedPaths = findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths);
			commit.setChangedPaths(changedPaths);
		}
		return commit;
	}
	
	public Commit findCommitByRevision(Long revision,  boolean collectChangedPaths, List<String> ignoredPaths){
		Commit commit = null;
		try {
			SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
			svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(revision), SVNRevision.create(revision)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			
			if(svnLogEntries != null && !svnLogEntries.isEmpty()){
				if(svnLogEntries.size() > 1)
					throw new NonUniqueCommitByRevisionException("More of one commit was found by one revision");
				SVNLogEntry svnLogEntry = svnLogEntries.iterator().next();
				commit = commitDao.findByRevision(svnLogEntry.getRevision());
				if(commit == null) {
					commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
				}
				List<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
				Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
				for (SVNLogEntryPath svnLogEntryPath : svnLogEntryPaths) {
					ChangedPath changedPath = createChangedPath(svnLogEntryPath, commit, ignoredPaths);
					changedPaths.add(changedPath);
				}
				commit.setChangedPaths(changedPaths);
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return commit;
	}

	@Override
	public String getFileContent(String path, Long revision) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			repository.getFile(path, revision, new SVNProperties(), output);
			return new String(output.toByteArray());
		} catch (SVNException e) {
//			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<Long> getFileRevisions(String path, Long startRevision, Long endRevision) {
		ArrayList<SVNFileRevision> svnFileRevisions = new ArrayList<SVNFileRevision>();
		ArrayList<Long> revisions = new ArrayList<Long>();
		try {
			System.out.println("Archive: "+path);
			System.out.print("Searching revisions (Rev: "+endRevision+")... ");
			repository.getFileRevisions(path, svnFileRevisions, startRevision, endRevision);
			for (SVNFileRevision svnFileRevision : svnFileRevisions) {
				revisions.add(svnFileRevision.getRevision());
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		System.out.println(revisions.size()+" founded revisions!");
		return revisions;
	}

//	@Override
//	public List<Long> getFileRevisions(String path, Long startRevision, Long endRevision) {
//		ArrayList<SVNFileRevision> svnFileRevisions = new ArrayList<SVNFileRevision>();
//		ArrayList<Long> revisions = new ArrayList<Long>();
//		
////		System.out.println("Archive: "+path);
////		boolean revisionsExecuted = false;
//		// Esse while é usado para executar a busca de revisões novamente caso ela falhe
////		while(!revisionsExecuted) {
//			try {
////				System.out.print("Searching revisions (Rev: "+endRevision+")... ");
//				// Busca a lista de revisões da classe
//				repository.getFileRevisions(path, svnFileRevisions, startRevision, endRevision);
//				for (SVNFileRevision svnFileRevision : svnFileRevisions) {
//					revisions.add(svnFileRevision.getRevision());
//				}
////				System.out.println(revisions.size()+" founded revisions!");
////				revisionsExecuted = true;
//			} catch (SVNException svnException) {
//				svnException.printStackTrace();
//			}
////		}
//		return revisions;
//	}
	
		
}
