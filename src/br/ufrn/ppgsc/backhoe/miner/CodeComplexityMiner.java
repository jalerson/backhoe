package br.ufrn.ppgsc.backhoe.miner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;
import br.ufrn.ppgsc.backhoe.util.CodeRepositoryUtil;
import br.ufrn.ppgsc.backhoe.vo.ConfigurationMining;
import br.ufrn.ppgsc.backhoe.vo.wrapper.ClassWrapper;
import br.ufrn.ppgsc.backhoe.vo.wrapper.MethodWrapper;

public class CodeComplexityMiner extends AbstractMiner {
	
	private final String minerSlug = "codeComplexityMiner";

	private MetricType addedComplexityPerChangedPathMetricType;
	private MetricType changedComplexityPerChangedPathMetricType;
	
	private List<Commit> specificCommitsToMining = null;
	
	public CodeComplexityMiner(Integer system, CodeRepository codeRepository,
			TaskRepository taskRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths) {
		super(system, codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		
		System.out.println("\n=========================================================");
		System.out.println("CODE COMPLEXITY MINER: "+ConfigurationMining.getSystemName(system).toUpperCase()+ " TEAM");
		System.out.println("---------------------------------------------------------");

		this.addedComplexityPerChangedPathMetricType = metricTypeDao
				.findBySlug("complexity:added");
		if (this.addedComplexityPerChangedPathMetricType == null) {
			this.addedComplexityPerChangedPathMetricType = new MetricType(
					"Added Complexity", "complexity:added");
			metricTypeDao.save(this.addedComplexityPerChangedPathMetricType);
		}
		
		this.changedComplexityPerChangedPathMetricType = metricTypeDao
				.findBySlug("complexity:changed");
		if (this.changedComplexityPerChangedPathMetricType == null) {
			this.changedComplexityPerChangedPathMetricType = new MetricType(
					"Changed Complexity", "complexity:changed");
			metricTypeDao.save(this.changedComplexityPerChangedPathMetricType);
		}
		return codeRepository.connect();
	}

	@Override
	public void execute() {
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("BACKHOE - CALCULATE CODE COMPLEXITY METRICS");
		System.out.println("---------------------------------------------------------");

		List<Commit> commits = null;
	
		if(specificCommitsToMining != null){
			commits = specificCommitsToMining;
		}else{
			if(developers == null) {
				commits = codeRepository.findCommitsByTimeRange(startDate, endDate, true, ignoredPaths);
			} else {
				commits = codeRepository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true, ignoredPaths);
			}
		}
		calculateCodeComplexity(commits);
	}

	private void calculateCodeComplexity(List<Commit> commits) {
		
		int processedCommits = 0;
		
		if(!commits.isEmpty())
			System.out.println("\n>> BACKHOE IS CALCULATING METRICS TO FOUNDED COMMITS ... ");
		else
			System.out.println("\n>> NO COMMIT WAS FOUNDED!");

		for (Commit commit : commits) {
			
			System.out.println("\n---------------------------------------------------------");
			System.out.println("REVISION #"+((commit.getRevision().length() <= 7)? commit.getRevision(): commit.getRevision().subSequence(0, 7))+
					" - [" + ++processedCommits + "]/[" + commits.size() + "]");
			
			List<ChangedPath> changedPaths = changedPathDao.getChangedPathByCommitRevision(commit.getRevision());
			for (ChangedPath changedPath : changedPaths) {
				if (changedPath.getChangeType().equals('D')) {
					continue;
				}
				if (!(changedPath.getPath().toLowerCase().contains(".java"))) {
					continue;
				}
				
				// Verifica se ja foram executados metricas de complexidade para o changedpath informado
				if(metricDao.existsMetric(changedPath.getId(), this.minerSlug))
					continue;
				
				System.out.println(">>> Path: "+changedPath.getPath());
				
				String currentContent = changedPath.getContent();
				
				if(changedPath.getChangeType().equals('A')){ 
					
					this.calculateComplexityMetricInThisRevision(changedPath,
							currentContent);
				}else{
					
					System.out.print(">>> It's looking for path revisions ... ");
					
					List<String> fileRevisions = codeRepository.getFileRevisions(
							changedPath.getPath(), null, changedPath.getCommit()
									.getRevision());
					
					System.out.println("Done! "+fileRevisions.size()+" revisions were founded!");
					
					String previousRevision = CodeRepositoryUtil.
							getPreviousRevision(changedPath.getCommit().getRevision(), fileRevisions);
					String previousContent = codeRepository.getFileContent(changedPath.getPath(), previousRevision);
					
					if(currentContent == null || previousContent == null)
						continue;
					
					this.calculateComplexityMetricBetweenRevisions(changedPath,
							previousContent, currentContent);

				}
			}
			System.out.println("---------------------------------------------------------");
		}
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE CODE COMPLEXITY METRICS WERE CALCULATED!");
		System.out.println("=========================================================\n");
	}

	private void calculateComplexityMetricInThisRevision(
			ChangedPath changedPath, String currentContent) {
		
		try {

			ClassWrapper classWrapper = new ClassWrapper(currentContent);
			ArrayList<MethodWrapper> methods = (ArrayList<MethodWrapper>) classWrapper
					.getMethods();
			
			float complexityAddedPerchangedPath = 0f;

			for (MethodWrapper method : methods) {
				complexityAddedPerchangedPath += new Integer(method.getCyclomaticComplexity()).floatValue();
			}
			
			Metric addedComplexityMetricPerMethod = new Metric();
			addedComplexityMetricPerMethod.setObjectId(changedPath.getId());
			addedComplexityMetricPerMethod.setObjectType("ChangedPath");
			addedComplexityMetricPerMethod.setValue(complexityAddedPerchangedPath);
			addedComplexityMetricPerMethod.setType(addedComplexityPerChangedPathMetricType);
			addedComplexityMetricPerMethod.setMinerSlug(this.minerSlug);
			metricDao.save(addedComplexityMetricPerMethod);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void calculateComplexityMetricBetweenRevisions(
			ChangedPath changedPath, String previousContent,
			String currentContent) {

		ClassWrapper previousClass = new ClassWrapper(previousContent);
		ClassWrapper currentClass = new ClassWrapper(currentContent);

		ArrayList<MethodWrapper> previousMethods = (ArrayList<MethodWrapper>) previousClass
				.getMethods();

		ArrayList<MethodWrapper> currentMethods = (ArrayList<MethodWrapper>) currentClass
				.getMethods();
		
		float complexityAddedPerchangedPath = 0f,
		 	  complexityChangedPerChangedPath = 0f;

		for (MethodWrapper currentMethod : currentMethods) {

			// Search for the method before the change
			MethodWrapper previousMethod = getPreviousMethod(previousMethods,
					currentMethod);

//			float cyclomaticCompexity = 0f;
			float addedComplexity = 0f,
				  changedComplexity = 0f;

			// If it does not, it means that it was created
			if (previousMethod == null) {
				addedComplexity = currentMethod.getCyclomaticComplexity();
			} else {
				// verifies that the complexity of the method changed before and
				// after change
				if (previousMethod.getCyclomaticComplexity() != currentMethod
						.getCyclomaticComplexity()) {

					// This avoids the negative cyclomatic complexity of a
					// refactoring
					// whether the current complexity is less complex than the
					// previous one,
					// the value of the cyclomatic complexity remains at zero
					if (currentMethod.getCyclomaticComplexity() >= previousMethod
							.getCyclomaticComplexity())
						changedComplexity = currentMethod
								.getCyclomaticComplexity()
								- previousMethod.getCyclomaticComplexity();
				} else {
					// If not changed go to the next method
					continue;
				}
			}
			complexityAddedPerchangedPath += addedComplexity;
			complexityChangedPerChangedPath += changedComplexity;
		}
		
		Metric addedComplexityMetricPerMethod = new Metric();
		addedComplexityMetricPerMethod.setObjectId(changedPath.getId());
		addedComplexityMetricPerMethod.setObjectType("ChangedPath");
		addedComplexityMetricPerMethod.setValue(complexityAddedPerchangedPath);
		addedComplexityMetricPerMethod.setType(addedComplexityPerChangedPathMetricType);
		addedComplexityMetricPerMethod.setMinerSlug(this.minerSlug);
		metricDao.save(addedComplexityMetricPerMethod);

		Metric changedComplexityMetricPerMethod = new Metric();
		changedComplexityMetricPerMethod.setObjectId(changedPath.getId());
		changedComplexityMetricPerMethod.setObjectType("ChangedPath");
		changedComplexityMetricPerMethod.setValue(complexityChangedPerChangedPath);
		changedComplexityMetricPerMethod.setType(changedComplexityPerChangedPathMetricType);
		changedComplexityMetricPerMethod.setMinerSlug(this.minerSlug);
		metricDao.save(changedComplexityMetricPerMethod);
	}

	// Helper method that search the method before the change
	private MethodWrapper getPreviousMethod(
			List<MethodWrapper> previousMethods, MethodWrapper currentMethod) {
		for (MethodWrapper previousMethod : previousMethods)
			if (currentMethod.getName().equals(previousMethod.getName()))
				return previousMethod;
		return null;
	}

	public List<Commit> getSpecificCommitsToMining() {
		return specificCommitsToMining;
	}

	public void setSpecificCommitsToMining(List<Commit> specificCommitsToMining) {
		this.specificCommitsToMining = specificCommitsToMining;
	}
}
