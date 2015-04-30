package br.ufrn.ppgsc.backhoe;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.miner.BugFixContributionMiner;
import br.ufrn.ppgsc.backhoe.miner.CodeComplexityMiner;
import br.ufrn.ppgsc.backhoe.miner.CodeContributionMiner;
import br.ufrn.ppgsc.backhoe.miner.Miner;
import br.ufrn.ppgsc.backhoe.repository.RepositoryFactory;
import br.ufrn.ppgsc.backhoe.repository.RepositoryType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.util.PropertiesUtil;

public class RunCodeContributionMiner {

	public static void main(String[] args) {
		
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
		
		Properties svnProperties = PropertiesUtil.getProperties("config/svn.properties");
		Properties iprojectProperties = PropertiesUtil.getProperties("config/postgres.properties"); 
		
		CodeRepository codeRepository = (CodeRepository) RepositoryFactory.createRepository(RepositoryType.SVN);
		codeRepository.setUsername(svnProperties.getProperty("username"));
		codeRepository.setPassword(svnProperties.getProperty("password"));
		codeRepository.setURL(svnProperties.getProperty("url"));
		
		TaskRepository iprojectRepository = (TaskRepository) RepositoryFactory.createRepository(RepositoryType.IPROJECT);
		iprojectRepository.setUsername(iprojectProperties.getProperty("username"));
		iprojectRepository.setPassword(iprojectProperties.getProperty("password"));
		iprojectRepository.setURL(iprojectProperties.getProperty("url"));
		
		Date startDate = Date.valueOf("2015-02-09");
		Date endDate = Date.valueOf("2015-02-10");
		ArrayList<String> developers = new ArrayList<String>(Arrays.asList(new String[]{"a", "b", "c"}));
		ArrayList<String> ignoredPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/LPS", "/ExemploIntegracaoSIAFI", "/branches" }));
		
		List<Miner> miners = new LinkedList<Miner>();
		
		miners.add(new CodeContributionMiner(codeRepository, iprojectRepository, startDate, endDate, developers, ignoredPaths));
		miners.add(new CodeComplexityMiner(codeRepository, iprojectRepository, startDate, endDate, developers, ignoredPaths));
		miners.add(new BugFixContributionMiner(codeRepository, iprojectRepository, startDate, endDate, developers, ignoredPaths));
		
		try {
			
			for(Miner miner: miners){
				miner.setup();
				miner.execute();
			}
			
		} catch (MissingParameterException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}
}
