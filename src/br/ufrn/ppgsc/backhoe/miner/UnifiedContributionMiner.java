package br.ufrn.ppgsc.backhoe.miner;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;

public class UnifiedContributionMiner extends AbstractMiner{
	
//	private final String minerSlug = "unifiedContributionMiner";

	public UnifiedContributionMiner(Integer system,
			CodeRepository codeRepository, TaskRepository taskRepository,
			Date startDate, Date endDate, List<String> developers,
			List<String> ignoredPaths) {
		super(system, codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		System.out.println("\n=========================================================");
		System.out.println("UNIFIED CONTRIBUTION MINER: "+ConfigurationMining.getSystemName(system).toUpperCase()+ " TEAM");
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
		System.out.println("BACKHOE - CALCULATE UNIFIED CONTRIBUTION MINER METRICS");
		System.out.println("---------------------------------------------------------");
			
		List<Commit> commits;

		List<Miner> miners = new LinkedList<Miner>();
		
		if(developers == null) {
			commits = codeRepository.findCommitsByTimeRange(startDate, endDate, true, ignoredPaths);
		} else {
			commits = codeRepository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true, ignoredPaths);
		}
			
		CodeContributionMiner codeContributionMiner = new CodeContributionMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		CodeComplexityMiner codeComplexityMiner = new CodeComplexityMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		BugFixContributionMiner bugFixContributionMiner = new BugFixContributionMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		BuggyCommitMiner buggyCommitMiner = new BuggyCommitMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);
		FailedTestsMiner failedTestMiner = new FailedTestsMiner(system, codeRepository, taskRepository, startDate, endDate, developers, ignoredPaths);         
		
		codeContributionMiner.setSpecificCommitsToMining(commits);
		codeComplexityMiner.setSpecificCommitsToMining(commits);
		
		miners.add(codeContributionMiner);
		miners.add(codeComplexityMiner);
		miners.add(bugFixContributionMiner);
		miners.add(buggyCommitMiner);
		miners.add(failedTestMiner);
			
		for(Miner miner: miners){
		    miner.setup();
		    miner.execute();
		}
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE UNIFIED CONTRIBUTION MINERS METRICS WERE CALCULATED!");
		System.out.println("=========================================================\n");
	}
}
