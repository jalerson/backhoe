package br.ufrn.ppgsc.backhoe.repository.code;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public abstract class CodeRepository extends Repository {
	
	public abstract List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths);
	public abstract List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths);
	public abstract List<ChangedPath> findChangedPathsByRevision(Long revision);
	public abstract List<ChangedPath> findChangedPathsByRevisionRage(Long startRevision, Long endRevision);
	public abstract String getFileContent(String path, Long revision);
	public abstract List<Long> getFileRevisions(String path, Long startRevision, Long endRevision);
	
}
