package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.repository.code.SVNRepository;

public class RepositoryFactory {
	
	public static Repository createRepository(RepositoryType type) {
		switch(type) {
			case SVN: 
				return new SVNRepository();
			default:
				return null;
		}
	}
	
}
