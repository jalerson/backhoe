package br.ufrn.ppgsc.backhoe.miner;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

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
		return taskRepository.connect() && codeRepository.connect();
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		System.out.println("\nCalculating Metrics for Task Miner...");
		long[] systems = {system};
		System.out.print("Researching Tasks in iproject... ");
		List<Task> tasks = taskRepository.findTasks(startDate, endDate, null, null, systems);
		if(tasks.isEmpty()){
			System.out.println("\nNo tasks founded, finishing mineration!");
		}else{
			System.out.println("Done!");
			System.out.print("Researching TasksLog from Tasks... ");
			List<TaskLog> logs = taskRepository.findTaskLogsFromTasks(tasks, null, developers);
			System.out.println("Done!");
			System.out.print("Fiding commits from TasksLogs... ");
			List<Commit> commits = codeRepository.findCommitsFromLogs(logs, true, ignoredPaths);
			System.out.println("Done!");
	
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
		
		System.out.println("Task Miner execut end!\n");

	}

}
