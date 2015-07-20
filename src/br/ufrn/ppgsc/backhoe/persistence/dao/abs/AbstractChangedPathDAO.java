package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;

public interface AbstractChangedPathDAO extends AbstractDAO<ChangedPath, Long> {
	
	List<ChangedPath> getChangedPathByCommitRevision(String revision);

}
