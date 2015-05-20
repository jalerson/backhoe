package br.ufrn.ppgsc.backhoe.repository.code;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Blame;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public interface CodeRepository extends Repository {
	
	List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths, List<String> ignoredPaths);
	List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths);
	String getFileContent(String path, Long revision);
	List<Long> getFileRevisions(String path, Long startRevision, Long endRevision);
	List<ChangedPath> getChangedPathsFromLogTarefas(List<TaskLog> logs);
	List<Diff> buildDiffs(List<ChangedPath> changedPaths);
	List<Blame> buildBlames(List<Diff> diffs);
	
}
