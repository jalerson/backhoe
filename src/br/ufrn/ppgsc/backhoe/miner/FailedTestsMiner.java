package br.ufrn.ppgsc.backhoe.miner;

import java.sql.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Blame;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;

public class FailedTestsMiner extends AbstractMiner {
	
	private final String minerSlug = "failedTestsMiner";
	
	private AbstractDeveloperDAO developerDao;
	private MetricType failedTestsPerDeveloperByPeriod;

	public FailedTestsMiner(Integer system, CodeRepository codeRepository,
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
		
		System.out.println("\n=========================================================");
		System.out.println("FILED TEST MINER: "+ConfigurationMining.getSystemName(system).toUpperCase()+ " TEAM");
		System.out.println("---------------------------------------------------------");
		
		this.failedTestsPerDeveloperByPeriod = metricTypeDao
				.findBySlug("tests:failed");
		if (this.failedTestsPerDeveloperByPeriod == null) {
			this.failedTestsPerDeveloperByPeriod = new MetricType(
					"Failed Tests", "tests:failed");
			metricTypeDao.save(this.failedTestsPerDeveloperByPeriod);
		}
		
		System.out.print("\n>> BACKHOE is connecting to CODE and TASK REPOSITORIES ... ");
		boolean connected = taskRepository.connect() && codeRepository.connect();
		if(connected)
			System.out.println("Done!");
		else
			System.out.println("Failed!");
		
		return taskRepository.connect() && codeRepository.connect();
	}

	@Override
	public void execute() {
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("BACKHOE - CALCULATE FILED TEST METRICS");
		System.out.println("---------------------------------------------------------\n");
		
		System.out.println(">> Date Interval for mining: "+startDate.toString()+" - "+endDate.toString());
		
		System.out.print("\n>> BACKHOE is looking for TASKS in Iproject ... ");
		
		List<Task> tasks = taskRepository.findFiledTestsTasks(startDate, endDate, new long[]{ system });
		
		if(tasks.isEmpty()) {
			System.out.println("Done!\n\n>> No task founded. Backhoe is finishing FILED TEST MINER!");
		}else{
			System.out.println("Done!");
			System.out.print(">> Looking logs from the founded tasks ... ");
			List<TaskLog> logs = taskRepository.findFiledTestsTaskLogs(tasks, developers);
			System.out.println("Done!");
			System.out.print(">> Lokking changed files from iProject logs ... ");
			List<ChangedPath> changedPaths = codeRepository.getChangedPathsFromLogTarefas(logs);
			System.out.println("Done!");
			System.out.println("Building Diffs ... ");
			List<Diff> diffs = codeRepository.buildDiffs(changedPaths);
			System.out.println("Done!");
			System.out.print("Building Blames ...");
			List<Blame> blames = codeRepository.buildBlames(diffs);
			System.out.println("Done!");
			
			Hashtable<String, Integer> buggyCommitPerDeveloperMAP = new Hashtable<String, Integer>();
			
			for (String login : developers) {
				buggyCommitPerDeveloperMAP.put(login, 0);
			}
			
			for (Blame blame : blames) {
				String developerLogin = blame.getAuthor();
				if(buggyCommitPerDeveloperMAP.containsKey(developerLogin)) 
					buggyCommitPerDeveloperMAP.put(developerLogin, buggyCommitPerDeveloperMAP.get(developerLogin) + 1);
			}
			
			 Set<String> keys = buggyCommitPerDeveloperMAP.keySet();
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
	        		
	            	Metric failedTestsPerDeveloper = new Metric();
	            	failedTestsPerDeveloper.setObjectId(developer.getId());
					failedTestsPerDeveloper.setObjectType("Developer");
					failedTestsPerDeveloper.setValue(Float.parseFloat(buggyCommitPerDeveloperMAP.get(developerLogin).toString()));
					failedTestsPerDeveloper.setType(failedTestsPerDeveloperByPeriod);
					failedTestsPerDeveloper.setStartDateInterval(new java.sql.Date(startDate.getTime()));
					failedTestsPerDeveloper.setEndDateInterval(new java.sql.Date(endDate.getTime()));
					failedTestsPerDeveloper.setMinerSlug(this.minerSlug);
					this.metricDao.save(failedTestsPerDeveloper);
	            }
	        }
		}
		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE FILED TEST METRICS WERE CALCULATED!");
		System.out.println("=========================================================\n");
	}
}
