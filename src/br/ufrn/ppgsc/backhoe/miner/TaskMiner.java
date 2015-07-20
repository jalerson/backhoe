package br.ufrn.ppgsc.backhoe.miner;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;

public class TaskMiner extends AbstractMiner {

	public TaskMiner(Integer system, CodeRepository codeRepository,
			TaskRepository taskRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths) {
		super(system, codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		// TODO Auto-generated method stub
		
		System.out.println("\n=========================================================");
		System.out.println("TASK MINER: "+ConfigurationMining.getSystemName(system).toUpperCase()+ " TEAM");
		System.out.println("---------------------------------------------------------");
	
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
		// TODO Auto-generated method stub
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("BACKHOE - CALCULATE TASK METRICS");
		System.out.println("---------------------------------------------------------\n");
		
		System.out.println(">> Date Interval for mining: "+startDate.toString()+" - "+endDate.toString());
		
		System.out.print("\n>> BACKHOE is looking for TASKS in Iproject ... ");
		
		long[] systems = {system};
		List<Task> tasks = taskRepository.findTasks(startDate, endDate, null, null, systems);
		
		if(tasks.isEmpty()){
			System.out.println("Done!\n\n>> No task founded. Backhoe is finishing TASK MINER!");
		}else{
			System.out.println(tasks.size()+ " Done!");
			System.out.print(">> Looking logs from the founded tasks ... ");
			List<TaskLog> logs = taskRepository.findTaskLogsFromTasks(tasks, null, developers);
			System.out.println(logs.size()+" Done!");
			System.out.print(">> Looking commits from logs ... ");
			List<Commit> commits = codeRepository.findCommitsFromLogs(logs, true, ignoredPaths);
			System.out.println(commits.size()+" Done!");
	
			List<Miner> miners = new LinkedList<Miner>();
			
			CodeContributionMiner codeContributionMiner = new CodeContributionMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
			CodeComplexityMiner codeComplexityMiner = new CodeComplexityMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
			
			codeContributionMiner.setSpecificCommitsToMining(commits);
			codeComplexityMiner.setSpecificCommitsToMining(commits);
			
			miners.add(codeContributionMiner);
			miners.add(codeComplexityMiner);
			
			for(Miner miner: miners){
			    miner.setup();
			    miner.execute();
			}
		}
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE TASK METRICS WERE CALCULATED!");
		System.out.println("=========================================================\n");
	}

}
