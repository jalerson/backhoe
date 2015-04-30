package br.ufrn.ppgsc.backhoe.repository.code;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public interface CodeRepository extends Repository {
	
	List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths, List<String> ignoredPaths);
	List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths);
//	List<ChangedPath> findChangedPathsByRevision(Long revision, List<String> ignoredPaths);
//	List<ChangedPath> findChangedPathsByRevisionRage(Long startRevision, Long endRevision, List<String> ignoredPaths);
	String getFileContent(String path, Long revision);
	List<Long> getFileRevisions(String path, Long startRevision, Long endRevision);
	
}
