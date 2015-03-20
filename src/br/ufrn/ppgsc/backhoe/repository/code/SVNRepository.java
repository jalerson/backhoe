package br.ufrn.ppgsc.backhoe.repository.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
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
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.vo.wrapper.AbstractFileRevisionWrapper;

public class SVNRepository extends CodeRepository {
	private org.tmatesoft.svn.core.io.SVNRepository repository;
	private String username;
	private String password;
	private String url;
	private AbstractDeveloperDAO developerDao;
	private AbstractCommitDAO commitDao;

	public SVNRepository() {
		try {
			this.developerDao = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
			this.commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
		} catch (DAONotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean connect() throws MissingParameterException {
		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if(username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		
		DAVRepositoryFactory.setup();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
			repository.setAuthenticationManager(authManager);
			return true;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths) {
		List<Commit> commits = findCommitsByTimeRange(startDate, endDate, collectChangedPaths);
		for (Commit commit : commits) {
			if(!developers.contains(commit.getAuthor())) {
				commits.remove(commit);
			}
		}
		return commits;
	}
	
	@Override
	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths) {
		ArrayList<Commit> commits = new ArrayList<Commit>();

		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());

		try {
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			System.out.println("Finished! "+svnLogEntries.size()+" commits found!");
			
			for (SVNLogEntry svnLogEntry : svnLogEntries) {
				if(svnLogEntry.getDate().after(startDate) && svnLogEntry.getDate().before(endDate)) {
					Commit commit = new Commit();
					Developer developer = developerDao.findByCodeRepositoryUsername(svnLogEntry.getAuthor());
					if(developer == null) {
						developer = new Developer();
						developer.setCodeRepositoryUsername(svnLogEntry.getAuthor());
					}
					commit.setAuthor(developer);
					// commit.setBranch(???);
					commit.setComment(svnLogEntry.getMessage());
					commit.setCreatedAt(svnLogEntry.getDate());
					// commit.setLog(???);
					commit.setRevision(svnLogEntry.getRevision());
					if(collectChangedPaths) {
						commit.setChangedPaths(findChangedPathsByRevision(svnLogEntry.getRevision()));
					}
					
				}
			}
			return commits;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	@Override
	public List<ChangedPath> findChangedPathsByRevision(Long revision) {
		return findChangedPathsByRevisionRage(revision, revision);
	}
	
	@Override
	public List<ChangedPath> findChangedPathsByRevisionRage(Long startRevision, Long endRevision) {
		Collection<SVNLogEntry> svnLogEntries;
		ArrayList<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
		try {
			svnLogEntries = repository.log( new String[] { "" }, null, startRevision, endRevision, true, true);
			
			for (SVNLogEntry svnLogEntry : svnLogEntries) {
				Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
				for (SVNLogEntryPath svnLogEntryPath : svnLogEntryPaths) {
					ChangedPath changedPath = new ChangedPath();
					changedPath.setChangeType(svnLogEntryPath.getType());
					Commit commit = commitDao.findByRevision(svnLogEntry.getRevision());
					if(commit == null) {
						commit = new Commit();
						Developer developer = developerDao.findByCodeRepositoryUsername(svnLogEntry.getAuthor());
						if(developer == null) {
							developer = new Developer();
							developer.setCodeRepositoryUsername(svnLogEntry.getAuthor());
						}
						commit.setAuthor(developer);
						// commit.setBranch(???);
						commit.setComment(svnLogEntry.getMessage());
						commit.setCreatedAt(svnLogEntry.getDate());
						// commit.setLog(???);
						commit.setRevision(svnLogEntry.getRevision());
					}
					changedPath.setCommit(commit);
					changedPath.setPath(svnLogEntryPath.getPath());
					changedPaths.add(changedPath);
				}
			}
			
			return changedPaths;
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<AbstractFileRevisionWrapper> findFileRevisions(String path, Long startRevision, Long endRevision) {
		ArrayList<SVNFileRevision> revisions = new ArrayList<SVNFileRevision>();
		try {
			repository.getFileRevisions(path, revisions, startRevision, endRevision);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

}
