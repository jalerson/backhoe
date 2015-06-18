package br.ufrn.ppgsc.backhoe;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import br.ufrn.ppgsc.backhoe.enums.MinerType;
import br.ufrn.ppgsc.backhoe.formatter.Formatter;
import br.ufrn.ppgsc.backhoe.miner.Miner;
import br.ufrn.ppgsc.backhoe.repository.RepositoryFactory;
import br.ufrn.ppgsc.backhoe.repository.RepositoryType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.util.PropertiesUtil;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;

public class RunCodeContributionMiner {

	public static File generateCSVWithMetrics(int teamNumber, Date startDate, Date endDate) {
		
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
		
		Properties svnProperties = PropertiesUtil.getProperties("config/svn.properties");
		Properties iprojectProperties = PropertiesUtil.getProperties("config/postgres.properties");
		Properties localRepositoryProperties = PropertiesUtil.getProperties("config/localRepository.properties");
		
		CodeRepository codeRepository = (CodeRepository) RepositoryFactory.createRepository(RepositoryType.SVN);
		codeRepository.setUsername(svnProperties.getProperty("username"));
		codeRepository.setPassword(svnProperties.getProperty("password"));
		codeRepository.setURL(svnProperties.getProperty("url"));
		
		TaskRepository tasktRepository = (TaskRepository) RepositoryFactory.createRepository(RepositoryType.IPROJECT);
		tasktRepository.setUsername(iprojectProperties.getProperty("username"));
		tasktRepository.setPassword(iprojectProperties.getProperty("password"));
		tasktRepository.setURL(iprojectProperties.getProperty("url"));
		
		LocalRepository localRepository = (LocalRepository) RepositoryFactory.createRepository(RepositoryType.LOCAL);
		localRepository.setUsername(localRepositoryProperties.getProperty("username"));
		localRepository.setPassword(localRepositoryProperties.getProperty("password"));
		localRepository.setURL(localRepositoryProperties.getProperty("url"));
		
//		Date startDate = Date.valueOf("2015-02-09");
//		Date endDate = Date.valueOf("2015-02-10");

		ArrayList<String> ignoredPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/LPS", "/ExemploIntegracaoSIAFI", "/branches" }));
	
//		ArrayList<ConfigurationMining> configurations = new ArrayList<ConfigurationMining>();
		
//		configurations.add(new ConfigurationMining(MinerType.CODE_CONTRIBUTION_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		configurations.add(new ConfigurationMining(MinerType.CODECOMPLEXITY_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		configurations.add(new ConfigurationMining(MinerType.BUGFIX_CONTRIBUTION_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		configurations.add(new ConfigurationMining(MinerType.BUGGY_COMMIT_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		configurations.add(new ConfigurationMining(MinerType.FAILED_TESTS_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		configurations.add(new ConfigurationMining(MinerType.TASK_MINER, Team.SIGAA, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
		
//		configurations.add(new ConfigurationMining(MinerType.UNIFIED_CONTRIBUTION_MINER, teamNumber, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository));
//		
//		for (ConfigurationMining config : configurations) {
//	
//			Miner miner = config.getMiner();
//			Formatter formatter = config.getFormatter();
//			miner.setup();
//			miner.execute();
//			formatter.setup();
//			formatter.format();
//		}
		
		ConfigurationMining unifiedContribution = new ConfigurationMining(MinerType.UNIFIED_CONTRIBUTION_MINER, teamNumber, startDate, endDate, ignoredPaths, codeRepository, tasktRepository, localRepository);
		
		Miner unifiedContributionMiner = unifiedContribution.getMiner();
		unifiedContributionMiner.setup();
		unifiedContributionMiner.execute();
		
		Formatter unifieFormatter = unifiedContribution.getFormatter();
		unifieFormatter.setup();
		return unifieFormatter.format();
		
//		System.exit(0);
	}
}
