package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.Commit;

public interface AbstractCommitDAO extends AbstractDAO<Commit, Long> {
	
	public Commit findByRevision(Long revision);
	
}
