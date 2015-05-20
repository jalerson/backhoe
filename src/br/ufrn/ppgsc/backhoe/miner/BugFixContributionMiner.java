package br.ufrn.ppgsc.backhoe.miner;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public class BugFixContributionMiner extends AbstractMiner {
	
	private final String minerSlug = "bugFixContributionMiner";
	
	private AbstractDeveloperDAO developerDao;
	private MetricType bugFixContributionPerDeveloperByPeriod;

	public BugFixContributionMiner(Integer system, CodeRepository codeRepository,
			TaskRepository taskRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths) {
		super(system, codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
		try {
			this.developerDao = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
		} catch (DAONotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		
		this.bugFixContributionPerDeveloperByPeriod = metricTypeDao
				.findBySlug("bugs:fixed");
		if (this.bugFixContributionPerDeveloperByPeriod == null) {
			this.bugFixContributionPerDeveloperByPeriod = new MetricType(
					"Bugs Fixed", "bugs:fixed");
			metricTypeDao.save(this.bugFixContributionPerDeveloperByPeriod);
		}
		
		return taskRepository.connect();
	}

	@Override
	public void execute() {
		
		System.out.println("Calculating Bug Fix Contribution Metrics...");
		System.out.print("Researching logs in iproject... \n");
		List<TaskLog> logs = taskRepository.findLogs(startDate, endDate, new long[]{ system }, developers);
		
		Hashtable<String, Integer> bugFixContributions = new Hashtable<String, Integer>();
		
		for (String login : developers) {
			bugFixContributions.put(login, 0);
		}
		
		for (TaskLog log : logs) {
			String developerLogin = log.getAuthor().getCodeRepositoryUsername();
			if(bugFixContributions.containsKey(developerLogin)) 
				bugFixContributions.put(developerLogin, bugFixContributions.get(developerLogin) + 1);
		}
		
		 Set<String> keys = bugFixContributions.keySet();
		 for (String developerLogin : keys){  
            if(developerLogin != null){
        		Developer developer = developerDao.findByCodeRepositoryUsername(developerLogin);
        		if(developer == null) {
        			developer = new Developer();
        			developer.setCodeRepositoryUsername(developerLogin);
        			developerDao.save(developer);
        		}
        		
        		if(metricDao.existsMetric(developer.getId(), this.minerSlug, 
        				new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime())))
					continue;
        		
            	Metric bugFixContributionPerDeveloper = new Metric();
            	bugFixContributionPerDeveloper.setObjectId(developer.getId());
				bugFixContributionPerDeveloper.setObjectType("Developer");
				bugFixContributionPerDeveloper.setValue(Float.parseFloat(bugFixContributions.get(developerLogin).toString()));
				bugFixContributionPerDeveloper.setType(bugFixContributionPerDeveloperByPeriod);
				bugFixContributionPerDeveloper.setStartDateInterval(new java.sql.Date(startDate.getTime()));
				bugFixContributionPerDeveloper.setEndDateInterval(new java.sql.Date(endDate.getTime()));
				bugFixContributionPerDeveloper.setMinerSlug(this.minerSlug);
				this.metricDao.save(bugFixContributionPerDeveloper);
            }
        }  
		
		System.out.println(logs.size()+" founded logs!");
		System.out.println("Bug Fix Contribution Miner execut end!");
	}
}
