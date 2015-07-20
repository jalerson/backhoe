package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.repository.code.GitRepository;
import br.ufrn.ppgsc.backhoe.repository.code.SVNRepository;
import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;
import br.ufrn.ppgsc.backhoe.repository.task.IProjectRepository;

public class RepositoryFactory {
	
	public static Repository createRepository(RepositoryType type) {
		switch(type) {
			case SVN: 
				return new SVNRepository();
			case GIT:
				return new GitRepository();
			case IPROJECT:
				return new IProjectRepository();
			case LOCAL:
				return new LocalRepository();
			default:
				return null;
		}
	}
	
}
