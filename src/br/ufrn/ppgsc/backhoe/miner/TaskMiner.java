package br.ufrn.ppgsc.backhoe.miner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		System.out.println("Calculating Metrics for Task Miner...");
		System.out.print("Researching Tasks in iproject... \n");
		long[] systems = {system};
		
		List<Task> tasks = taskRepository.findTasks(startDate, endDate, null, null, systems);
		System.out.println(tasks.size()+" <<<<<");
		List<TaskLog> logs = taskRepository.findTaskLogsFromTasks(tasks, null, developers);
		List<Commit> commits = codeRepository.findCommitsFromLogs(logs, true, ignoredPaths);
		
		Map<MinerType, Miner> minersMAP = new HashMap<MinerType, Miner>();
		
		CodeContributionMiner codeContributionMiner = new CodeContributionMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		CodeComplexityMiner codeComplexityMiner = new CodeComplexityMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		
		codeContributionMiner.setSpecificCommitsToMining(commits);
		codeComplexityMiner.setSpecificCommitsToMining(commits);
		
		minersMAP.put(MinerType.CODE_CONTRIBUTION_MINER, codeContributionMiner);
		minersMAP.put(MinerType.CODECOMPLEXITY_MINER, codeComplexityMiner);
		
		for (Map.Entry<MinerType, Miner> pair : minersMAP.entrySet()) {
		    Miner miner = pair.getValue();
		    miner.setup();
		    miner.execute();
		}
		
//		for(Commit c: commits){
//			System.out.print("revision: "+c.getRevision());
//			System.out.println(" --> task: "+c.getTask().getId());
//		}
//		
//		System.out.println("tasks: "+tasks.size());
//		System.out.println(taskRepository.findBugFixTaskLogs(taskRepository.filterBugFixTasks(tasks), developers).size());
		
//		System.out.println(logs.size());
//		System.out.println(tasks);
	}

}
