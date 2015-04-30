package br.ufrn.ppgsc.backhoe.miner;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public class BugFixContributionMiner extends AbstractMiner {

	public BugFixContributionMiner(CodeRepository codeRepository,
			TaskRepository taskRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths) {
		super(codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		return taskRepository.connect();
	}

	@Override
	public void execute() {
		Integer system = 2;//config.getSystem();
		
		System.out.print("Buscando logs no iProject... \n");
		List<TaskLog> logs = taskRepository.findLogs(startDate, endDate, new long[]{ system });
		System.out.println(logs.size()+" logs encontrados!");
		System.out.println("Ok!");
	}
}
