package br.ufrn.ppgsc.backhoe;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import br.ufrn.ppgsc.backhoe.enums.MinerType;
import br.ufrn.ppgsc.backhoe.enums.Team;
import br.ufrn.ppgsc.backhoe.formatter.Formatter;
import br.ufrn.ppgsc.backhoe.miner.Miner;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateUtil;
import br.ufrn.ppgsc.backhoe.repository.RepositoryFactory;
import br.ufrn.ppgsc.backhoe.repository.RepositoryType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.util.PropertiesUtil;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;

public class RunCodeContributionMinerPerWeek {
	
	public static void main(String[] args) {
		
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
		
		Date startDate = Date.valueOf("2015-05-28");
		//2015-05-28
		Date endDate = Date.valueOf("2015-07-01");

		ArrayList<String> ignoredPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/LPS", "/ExemploIntegracaoSIAFI", "/branches" }));
	
		ArrayList<ConfigurationMining> configurations = new ArrayList<ConfigurationMining>();
		
		List<Date> dateIntervals = genenateDateIntervalPerWeek(startDate, endDate);
		
		for(int j = 0; j + 1 < dateIntervals.size(); j ++){
		
			int[] teams = {Team.SIGAA, Team.SIPAC};
			
			for(int i = 0; i < teams.length; i++){
//				configurations.add(new ConfigurationMining(MinerType.CODE_CONTRIBUTION_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
//				configurations.add(new ConfigurationMining(MinerType.CODECOMPLEXITY_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
//				configurations.add(new ConfigurationMining(MinerType.BUGFIX_CONTRIBUTION_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
//				configurations.add(new ConfigurationMining(MinerType.BUGGY_COMMIT_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
//				configurations.add(new ConfigurationMining(MinerType.FAILED_TESTS_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
//				configurations.add(new ConfigurationMining(MinerType.TASK_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));		
				configurations.add(new ConfigurationMining(MinerType.UNIFIED_CONTRIBUTION_MINER, teams[i], dateIntervals.get(j), dateIntervals.get(j+1), ignoredPaths, codeRepository, tasktRepository, localRepository));
			}
		}
		
		for (ConfigurationMining config : configurations) {
	
			Miner miner = config.getMiner();
			Formatter formatter = config.getFormatter();
			miner.setup();
			miner.execute();
			formatter.setup();
			formatter.format();
		}
		
		HibernateUtil.close();
		System.exit(0);
		
	}
	
	private static List<Date> genenateDateIntervalPerWeek(Date startDate, Date endDate){
		
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		List<Date> datesBetween = new ArrayList<Date>();

		while (start.compareTo(end) <= 0) {
		   datesBetween.add(new Date(start.getTimeInMillis()));
		   start.add(Calendar.DAY_OF_MONTH, 7);
		}
		
		return datesBetween;
	}
}