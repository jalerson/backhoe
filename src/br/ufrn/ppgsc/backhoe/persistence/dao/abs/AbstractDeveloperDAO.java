package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import br.ufrn.ppgsc.backhoe.persistence.model.Developer;

public interface AbstractDeveloperDAO extends AbstractDAO<Developer, Long> {
	
	public Developer findByCodeRepositoryUsername(String username);
	
}
