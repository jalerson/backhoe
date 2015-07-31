package br.ufrn.ppgsc.backhoe.repository.code;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.NonUniqueCommitByRevisionException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Blame;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.BlameHandler;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.DiffChild;

public class SVNRepository extends AbstractCodeRepository implements CodeRepository {
	
	private org.tmatesoft.svn.core.io.SVNRepository repository;
	private SVNClientManager svnClientManager;
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
	
	@Override
	protected boolean specificConnect() {
		DAVRepositoryFactory.setup();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
			repository.setAuthenticationManager(authManager);
			
			ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			svnClientManager = SVNClientManager.newInstance(options, repository.getAuthenticationManager());
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
			System.out.print(">> BACKHOE is loking for commits in the informed date interval! "+startDate.toString()+" - "+endDate.toString()+" ... ");
			
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			
			System.out.println("Done!\n>>> "+svnLogEntries.size()+" commits were founded!");
			
			for (SVNLogEntry svnLogEntry : svnLogEntries){
				if(developers == null || developers != null && developers.contains(svnLogEntry.getAuthor())){
					boolean betweenSearchRange = svnLogEntry.getDate().after(startDate) && svnLogEntry.getDate().before(endDate);
					if(betweenSearchRange){
						Commit commit = commitDao.findByRevision(new Long(svnLogEntry.getRevision()).toString());
						
						if(commit == null){
							commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
							commitDao.save(commit);
						}else if(commit != null && commit.getChangedPaths() != null && commit.getChangedPaths().isEmpty() && collectChangedPaths){
							commit.setChangedPaths(findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths));
							commitDao.update(commit);
						}
						commits.add(commit);
					}
				}
			}
			if(developers != null)
				System.out.println(">>> "+commits.size()+" of "+svnLogEntries.size()+" belongs to informed developers");
			return commits;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<ChangedPath> findChangedPathsBySVNLogEntry(SVNLogEntry svnLogEntry, Commit associatedCommit, List<String> ignoredPaths){
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
		Commit commit = new Commit(Commit.RepositoryType.SVN);
		Developer developer = developerDao.findByCodeRepositoryUsername(svnLogEntry.getAuthor());
		if(developer == null) {
			developer = new Developer();
			developer.setCodeRepositoryUsername(svnLogEntry.getAuthor());
			developerDao.save(developer);
		}
		commit.setAuthor(developer);
		commit.setComment(svnLogEntry.getMessage());
		commit.setCreatedAt(svnLogEntry.getDate());
		commit.setRevision(new Long(svnLogEntry.getRevision()).toString());
		if(collectChangedPaths){
			List<ChangedPath> changedPaths = findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths);
			commit.setChangedPaths(changedPaths);
		}
		return commit;
	}
	
	public Commit findCommitByRevision(String revision,  boolean collectChangedPaths, List<String> ignoredPaths){
		Commit commit = commitDao.findByRevision(revision);
		Long svnRevision = new Long(revision);
		if(commit == null){
			try {
				SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
				svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());
				SvnLog log = svnOperationFactory.createLog();
				log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
				log.addRange(SvnRevisionRange.create(SVNRevision.create(svnRevision), SVNRevision.create(svnRevision)));
				log.setDiscoverChangedPaths(true);
				log.setDepth(SVNDepth.INFINITY);
				ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
				log.run(svnLogEntries);
				
				if(svnLogEntries != null && !svnLogEntries.isEmpty()){
					if(svnLogEntries.size() > 1)
						throw new NonUniqueCommitByRevisionException("More of one commit was found by one revision");
					
					SVNLogEntry svnLogEntry = svnLogEntries.iterator().next();
						
					commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
					commitDao.save(commit);
				}
			} catch (SVNException e) {
				e.printStackTrace();
			}
		}
		return commit;
	}

	@Override
	public String getFileContent(String path, String revision) {
		Long svnRevision = new Long(revision);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			repository.getFile(path, svnRevision, new SVNProperties(), output);
			return new String(output.toByteArray());
		} catch (SVNException e) {
		}
		return null;
	}
	
	@Override
	public List<String> getFileRevisions(String path, String startRevision, String endRevision) {
		ArrayList<SVNFileRevision> svnFileRevisions = new ArrayList<SVNFileRevision>();
		ArrayList<String> revisions = new ArrayList<String>();
		
		if(startRevision == null)
			startRevision = "1";
		try {
			repository.getFileRevisions(path, svnFileRevisions, new Long(startRevision), new Long(endRevision));
			for (SVNFileRevision svnFileRevision : svnFileRevisions) {
				revisions.add(new Long(svnFileRevision.getRevision()).toString());
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return revisions;
	}
	
	public List<ChangedPath> getChangedPathsFromLogTarefas(List<TaskLog> logs){
		return this.getChangedPathsFromLogTarefas(logs, Commit.RepositoryType.SVN);
	}
	
		
	public List<Diff> buildDiffs(List<ChangedPath> changedPaths) {
		ArrayList<Diff> diffs = new ArrayList<Diff>();

		for (ChangedPath changedPath : changedPaths) {
			SVNRevision fixingRevision = SVNRevision.create(new Long(changedPath.getCommit().getRevision()));
			List<SVNFileRevision> fileRevisions = new ArrayList<SVNFileRevision>();

			try {
				repository.getFileRevisions(changedPath.getPath(), fileRevisions, 1, new Long(changedPath.getCommit().getRevision()));
				SVNRevision previousRevision = getPreviousVersion(fileRevisions);
				SVNRevision startRevision = getStartVersion(fileRevisions);
				
				ByteArrayOutputStream diffOut = new ByteArrayOutputStream();
				SVNURL urlPath = SVNURL.parseURIEncoded(this.url + changedPath.getPath());
				System.out.println("=================================================================");
				System.out.println("Executing Diff on path: "+changedPath.getPath()+"\n"
								 + "Fixing Revision: "+fixingRevision+"\n"
								 + "Previous Revision: "+previousRevision);
				SVNDiffClient diffClient = svnClientManager.getDiffClient();
				System.out.println("=================================================================");
				diffClient.doDiff(urlPath, fixingRevision, previousRevision, fixingRevision, SVNDepth.INFINITY, true, diffOut);
				
				Diff diff = new Diff(changedPath.getCommit().getTask(), Long.toString(fixingRevision.getNumber()), 
						 Long.toString(previousRevision.getNumber()),  Long.toString(startRevision.getNumber()), changedPath);
				
				diffs.add(diff);
				buildDiffChilds(diff, diffOut);
				diffOut.close();
			} catch (SVNException e) {
				if(e.getMessage().contains("is not a file in revision")) {
					System.err.println(changedPath.getPath()+" Not Found. Continuing...");
				} else {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return diffs;
	}

	private SVNRevision getPreviousVersion(List<SVNFileRevision> revisions) {
		SVNRevision revision;
		if (revisions.size() > 1) {
			revision = SVNRevision.create(((SVNFileRevision) revisions
					.get(revisions.size() - 2)).getRevision());
		} else {
			revision = SVNRevision.create(((SVNFileRevision) revisions.get(0))
					.getRevision());
		}
		return revision;
	}

	private SVNRevision getStartVersion(List<SVNFileRevision> revisions) {
		SVNRevision revision = SVNRevision.create(((SVNFileRevision) revisions
				.get(0)).getRevision());
		return revision;
	}

	private void buildDiffChilds(Diff diff, ByteArrayOutputStream diffOut) {
		ByteArrayInputStream diffInputStream = new ByteArrayInputStream(
				diffOut.toByteArray());
		BufferedReader br = new BufferedReader(new InputStreamReader(
				diffInputStream));
		buildDiffChilds(diff, br);
	}

	public List<Blame> buildBlames(List<Diff> diffs) {
		
		SVNLogClient logClient = svnClientManager.getLogClient();
		
		List<Blame> analizedBlames = new ArrayList<Blame>();

		int count = 1;
		for (Diff diff : diffs) {
			
			System.out.print("Analizing diff "+(count++)+"/"+diffs.size()+" to build blames...");
			SVNRevision svnFixingRevision = SVNRevision.create(new Long(diff
					.getFixingRevision()));
			SVNRevision svnStartRevision = SVNRevision.create(new Long(diff
					.getStartRevision()));
			SVNRevision svnPreviousRevision = SVNRevision.create(new Long(diff
					.getPreviousRevision()));

			ChangedPath changedPath = diff.getChangedPath();

			try {
				BlameHandler blameHandler = new BlameHandler();
				blameHandler.setChangedPath(changedPath);

				SVNURL url = SVNURL.parseURIEncoded(this.url
						+ changedPath.getPath());
				logClient.doAnnotate(url, svnFixingRevision, svnStartRevision,
						svnPreviousRevision, blameHandler);

				List<Blame> blames = (List<Blame>) blameHandler.getBlameList();
				analizedBlames.addAll(analyzeBlames(blames, diff));
			} catch (SVNException e) {
				e.printStackTrace();
			}
			
			System.out.println("Done!");
		}
		return analizedBlames;
	}
	
	private List<Blame> analyzeBlames(List<Blame> blames, Diff diff) {
		
		List<Blame> blamesAnalized = new LinkedList<Blame>();
		
		for (DiffChild diffChild : diff.getChildren()) {
			boolean onlyAdditions = diffChild.getRemovals().isEmpty();

			if (onlyAdditions) {
				for (Blame blame : blames) {
					if (blame.getLine().equals(diffChild.getLineJustBefore())) {
						blame.setTask(diff.getTask());
						diffChild.getBlames().add(blame);
						blamesAnalized.add(blame);
					}
				}
			} else {
				for (Blame blame : blames) {
					for (String removal : diffChild.getRemovals()) {
						if (blame.getLine().equals(
								removal.replaceFirst("-", ""))) {
							blame.setTask(diff.getTask());
							diffChild.getBlames().add(blame);
							blamesAnalized.add(blame);
						}
					}
				}
			}
		}
		return blamesAnalized;
	}
}