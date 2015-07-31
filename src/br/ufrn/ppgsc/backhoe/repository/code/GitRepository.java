package br.ufrn.ppgsc.backhoe.repository.code;

import java.io.BufferedReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Blame;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.repository.code.gitAPI.GITLogChange;
import br.ufrn.ppgsc.backhoe.repository.code.gitAPI.GITLogEntry;
import br.ufrn.ppgsc.backhoe.repository.code.gitAPI.GitAPIException;
import br.ufrn.ppgsc.backhoe.repository.code.gitAPI.GitHandleImpl;


public class GitRepository extends AbstractCodeRepository{
	
	private GitHandleImpl gitRepositoryHandle;
	
	public GitRepository(){
		this(null, null, null);
	}

	public GitRepository(String username, String password, String url) {
		super(username, password, url);
	}
	
	@Override
	protected boolean specificConnect() {
		
		System.out.println("> Connecting to GIT REPOSITORY: "+url);
		
		gitRepositoryHandle = new GitHandleImpl(url, username, password);
		
		if(!gitRepositoryHandle.wasClonedRepository()){
			try {
				System.out.print(">>> BACKHOE is cloning GIT REPOSITORY in local storage ... ");
				gitRepositoryHandle.cloneRepository();
				System.out.println("Done!");
				return true;
			} catch (GitAPIException e) {
				System.out.println("Failed!");
			}
		}else{
			try {
				System.out.print(">>> BACKHOE is updating GIT REPOSITORY in local storage ... ");
				gitRepositoryHandle.pull();
				System.out.println("Done!");
				return true;
			} catch (GitAPIException e) {
				System.out.println("Failed!");
			}
		}
		return false;
	}

	@Override
	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate,
			Date endDate, List<String> developers, boolean collectChangedPaths,
			List<String> ignoredPaths) {
		
		System.out.print("\n>> BACKHOE is loking for commits in the informed date interval! "+startDate.toString()+" - "+endDate.toString()+" ... ");
		
		List<GITLogEntry> logs = gitRepositoryHandle.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, collectChangedPaths, ignoredPaths);
		
		List<Commit> commits = new ArrayList<Commit>();
		
		for(GITLogEntry log: logs){
			Commit commit = commitDao.findByRevision(log.getRevision());
			if(commit == null){
				commit = this.GITLogEntryToCommit(log);
				commitDao.save(commit);
			}
			commits.add(commit);
		}
		System.out.println("Done!\n>>> "+commits.size()+" commits were founded!");
		return commits;
	}

	@Override
	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate,
			boolean collectChangedPaths, List<String> ignoredPaths) {
		return findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths);
	}

	@Override
	public Commit findCommitByRevision(String revision,
			boolean collectChangedPaths, List<String> ignoredPaths) {
		try {
			GITLogEntry log = this.gitRepositoryHandle.getCommitInformations(revision);
			return GITLogEntryToCommit(log);
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Commit GITLogEntryToCommit(GITLogEntry log){
		Commit commit = null;
		if(log != null){
			Developer developer = developerDao.findByCodeRepositoryUsernameOrEmail(log.getAuthorEmail());
			if(developer == null){
				developer = new Developer(null, log.getAuthorEmail(), null);
				developerDao.save(developer);
			}
			commit = new Commit(log.getRevision(), log.getComment(), log.getCreatedAt(), log.getBranch(), 
					developer, null, null, Commit.RepositoryType.GIT);
			
			List<GITLogChange> logChanges = log.getChangedPaths();
			List<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
			
			for(GITLogChange logChange: logChanges){
				ChangedPath changedPath = new ChangedPath(logChange.getPath(), logChange.getChangeType(), commit, logChange.getContent());
				changedPaths.add(changedPath);
			}
			commit.setChangedPaths(changedPaths);
		}
		return commit;
	}

	@Override
	public String getFileContent(String path, String revision) {
		try {
			return this.gitRepositoryHandle.getChangeContent(revision, path);
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> getFileRevisions(String path, String startRevision,
			String endRevision) {
		try {
			List<String> fileRevisions = this.gitRepositoryHandle.getFileRevisions(path, startRevision, endRevision);
			return fileRevisions;
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

	@Override
	public List<ChangedPath> getChangedPathsFromLogTarefas(List<TaskLog> logs) {
		return this.getChangedPathsFromLogTarefas(logs, Commit.RepositoryType.GIT);
	}
	
	@Override
	public List<Diff> buildDiffs(List<ChangedPath> changedPaths) {
		ArrayList<Diff> diffs = new ArrayList<Diff>();

		for (ChangedPath changedPath : changedPaths) {
			List<String> fileRevisions;
			try {
				fileRevisions = this.gitRepositoryHandle.getFileRevisions(changedPath.getPath(), null, changedPath.getCommit().getRevision());
						
				String fixingRevision = changedPath.getCommit().getRevision();		
				String startRevision = fileRevisions.get(0);				
				String previousRevision = (fileRevisions.size() > 1)? fileRevisions.get(fileRevisions.size()-2):fileRevisions.get(0);
				
				System.out.println("=================================================================");
				System.out.println("Executing Diff on path: "+changedPath.getPath()+"\n"
								 + "Fixing Revision: "+fixingRevision+"\n"
								 + "Previous Revision: "+previousRevision);
				
				BufferedReader brOut = this.gitRepositoryHandle.diff(previousRevision, changedPath.getPath(), 
											  fixingRevision, changedPath.getPath());
	
				System.out.println("=================================================================");
				
				Diff diff = new Diff(changedPath.getCommit().getTask(), fixingRevision,
						previousRevision, startRevision, changedPath);
				
				diffs.add(diff);
				
				if(brOut != null)
					buildDiffChilds(diff, brOut);
			
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return diffs;
	}

	@Override
	public List<Blame> buildBlames(List<Diff> diffs) {
		// TODO Auto-generated method stub
		return null;
	}	
}
