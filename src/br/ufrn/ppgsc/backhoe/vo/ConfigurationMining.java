package br.ufrn.ppgsc.backhoe.vo;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

import br.ufrn.ppgsc.backhoe.formatter.BugFixContributionFormatter;
import br.ufrn.ppgsc.backhoe.formatter.CodeComplexityFormatter;
import br.ufrn.ppgsc.backhoe.formatter.CodeContributionFormatter;
import br.ufrn.ppgsc.backhoe.formatter.Formatter;
import br.ufrn.ppgsc.backhoe.miner.BugFixContributionMiner;
import br.ufrn.ppgsc.backhoe.miner.CodeComplexityMiner;
import br.ufrn.ppgsc.backhoe.miner.CodeContributionMiner;
import br.ufrn.ppgsc.backhoe.miner.Miner;
import br.ufrn.ppgsc.backhoe.miner.MinerType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public class ConfigurationMining {
	private MinerType minerType;
	private Integer system;
	private Date startDate;
	private Date endDate;
	private ArrayList<String> ignoredPaths;
	private CodeRepository codeRepository;
	private TaskRepository taskRepository;
	private LocalRepository localRepository;
	
	public ConfigurationMining(MinerType minerType, Integer system,
			Date startDate, Date endDate, ArrayList<String> ignoredPaths,
			CodeRepository codeRepository, TaskRepository taskRepository,
			LocalRepository localRepository) {
		super();
		this.minerType = minerType;
		this.system = system;
		this.startDate = startDate;
		this.endDate = endDate;
		this.ignoredPaths = ignoredPaths;
		this.codeRepository = codeRepository;
		this.taskRepository = taskRepository;
		this.localRepository = localRepository;
	}

	public ArrayList<String> getTeam() {
		switch(system) {
			
			default: System.err.println("Set the variable system with one of the listed values (2, 3, 4, 29)");
					 return null;
		}
	}

	public Miner getMiner() {
		switch(minerType) {
//			case BUGGY_COMMIT_SINFO_MINER: return new BuggyCommitSINFOMiner();
			case CODE_CONTRIBUTION_MINER: return new CodeContributionMiner(codeRepository, taskRepository, startDate, endDate, getTeam(), ignoredPaths);
//			case FAILED_TESTS_SINFO_MINER: return new FailedTestsSINFOMiner();
			case CODECOMPLEXITY_MINER: return new CodeComplexityMiner(codeRepository, taskRepository, startDate, endDate, getTeam(), ignoredPaths);
			case BUGFIX_CONTRIBUTION_MINER: return new BugFixContributionMiner(codeRepository, taskRepository, startDate, endDate, getTeam(), ignoredPaths);
			default: return null;
		}
	}

	public Formatter getFormatter() {
		switch(minerType) {
//			case BUGGY_COMMIT_SINFO_MINER: return new BuggyCommitDissertationSINFOFormatter();
			case CODE_CONTRIBUTION_MINER: return new CodeContributionFormatter(startDate, endDate, getTeam(), localRepository, getMinerName(), getSystemName());
//			case FAILED_TESTS_SINFO_MINER: return new FailedTestsDissertationSINFOFormatter();
			case CODECOMPLEXITY_MINER: return new CodeComplexityFormatter(startDate, endDate, getTeam(), localRepository, getMinerName(), getSystemName());
			case BUGFIX_CONTRIBUTION_MINER: return new BugFixContributionFormatter(startDate, endDate, getTeam(), localRepository, getMinerName(), getSystemName());
			default: return null;
		}
	}

	public String getSystemName() {
		switch(system) {
		case 2: return "sigaa";
		case 3: return "sipac";
		case 4: return "sigrh";
		case 29: return "sucupira";
		default: return "unknown";
		}
	}
	
	public String getMinerName() {
		switch(minerType) {
		case BUGGY_COMMIT_MINER: return "buggycommit";
		case CODE_CONTRIBUTION_MINER: return "codecontribution";
		case FAILED_TESTS_MINER: return "failedtests";
		case CODECOMPLEXITY_MINER: return "codecomplexity";
		case BUGFIX_CONTRIBUTION_MINER: return "bugfix";
		default: return "unknown";
		}
	}
	
	public Integer getSystem() {
		return system;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public MinerType getMinerType() {
		return minerType;
	}

	public void setMinerType(MinerType minerType) {
		this.minerType = minerType;
	}

	public ArrayList<String> getIgnoredPaths() {
		return ignoredPaths;
	}

	public void setIgnoredPaths(ArrayList<String> ignoredPaths) {
		this.ignoredPaths = ignoredPaths;
	}

	public CodeRepository getCodeRepository() {
		return codeRepository;
	}

	public void setCodeRepository(CodeRepository codeRepository) {
		this.codeRepository = codeRepository;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public void setSystem(Integer system) {
		this.system = system;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
