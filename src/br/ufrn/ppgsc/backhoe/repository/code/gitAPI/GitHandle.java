package br.ufrn.ppgsc.backhoe.repository.code.gitAPI;

import java.sql.Date;
import java.util.List;

public interface GitHandle {
	
	List<GITLogEntry> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, 
			boolean collectChangedPaths, List<String> ignoredPaths);
	
	List<GITLogEntry> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths);
	
	String getChangeContent(String revision, String changePath) throws GitAPIException;
	
	List<String> getFileRevisions(String path, String startRevision, String endRevision) throws GitAPIException;
	
	GITLogEntry getCommitInformations(String revision) throws GitAPIException;
	
	void cloneRepository()throws GitAPIException;
	
	boolean wasClonedRepository();
	
}
