package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.repository.code.SVNRepository;
import br.ufrn.ppgsc.backhoe.repository.task.IProjectRepository;

public class RepositoryFactory {
	
	public static Repository createRepository(RepositoryType type) {
		switch(type) {
			case SVN: 
				return new SVNRepository();
			case IPROJECT:
				return new IProjectRepository();
			default:
				return null;
		}
	}
	
}
