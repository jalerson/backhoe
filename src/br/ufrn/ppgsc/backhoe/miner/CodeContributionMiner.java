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
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class CodeContributionMiner extends AbstractMiner {
	
	private final String minerSlug = "codeContributionMiner";

	private MetricType addedLOCMetricType;
	private MetricType changedLOCMetricType;
	private MetricType deletedLOCMetricType;
	private MetricType addedMethodsMetricType;
	private MetricType changedMethodsMetricType;
	
	private List<Commit> specificCommitsToMining;
	
	public CodeContributionMiner(Integer system, CodeRepository codeRepository,
			TaskRepository taskRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths) {
		super(system, codeRepository, taskRepository, startDate, endDate, developers,
				ignoredPaths);
	}
	
	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
		
		System.out.println("=========================================================");
		System.out.println("CODE CONTRIBUTION MINING: "+ConfigurationMining.getSystemName(system).toUpperCase()+ " TEAM");
		System.out.println("---------------------------------------------------------");
	
		this.addedLOCMetricType = metricTypeDao.findBySlug("loc:added");
		if(this.addedLOCMetricType == null) {
			this.addedLOCMetricType = new MetricType("Added LOC", "loc:added");
			metricTypeDao.save(this.addedLOCMetricType);
		}
		
		this.changedLOCMetricType = metricTypeDao.findBySlug("loc:changed");
		if(this.changedLOCMetricType == null) {
			this.changedLOCMetricType = new MetricType("Changed LOC", "loc:changed");
			metricTypeDao.save(this.changedLOCMetricType);
		}
		
		this.deletedLOCMetricType = metricTypeDao.findBySlug("loc:deleted");
		if(this.deletedLOCMetricType == null) {
			this.deletedLOCMetricType = new MetricType("Deleted LOC", "loc:deleted");
			metricTypeDao.save(this.deletedLOCMetricType);
		}
		
		this.addedMethodsMetricType = metricTypeDao.findBySlug("methods:added");
		if(this.addedMethodsMetricType == null) {
			this.addedMethodsMetricType = new MetricType("Added Methods", "methods:added");
			metricTypeDao.save(this.addedMethodsMetricType);
		}
		
		this.changedMethodsMetricType = metricTypeDao.findBySlug("methods:changed");
		if(this.changedMethodsMetricType == null) {
			this.changedMethodsMetricType = new MetricType("Changed Methods", "methods:changed");
			metricTypeDao.save(this.changedMethodsMetricType);
		}
		
		return codeRepository.connect();
	}

	@Override
	public void execute() {
		
		List<Commit> commits = null;
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("BACKHOE - CALCULATE CODE CONTRIBUTION METRICS");
		System.out.println("---------------------------------------------------------");
		
		if(specificCommitsToMining != null){
			commits = specificCommitsToMining;
		}else{
			if(developers == null) {
				commits = codeRepository.findCommitsByTimeRange(startDate, endDate, true, ignoredPaths);
			} else {
				commits = codeRepository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true, ignoredPaths);
			}
		}
		
		calculateCodeContributionMetricsToCommits(commits);
		
		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE CODE CONTRIBUTION METRICS WERE CALCULATED!");
		System.out.println("=========================================================");
	}
	
	private void calculateCodeContributionMetricsToCommits(List<Commit> commits){

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
				
				if(changedPath.getChangeType().equals('D')) continue;
				
				if(!(changedPath.getPath().toLowerCase().contains(".java"))) continue;
				
				// Verifica se ja foram executados metricas de contribuicao para o changedpath informado
				if(metricDao.existsMetric(changedPath.getId(), this.minerSlug))
					continue;
				
				System.out.println(">>> Path: "+changedPath.getPath());
				
				String currentContent = changedPath.getContent();
				
				if(changedPath.getChangeType().equals('A')){ 
					
					ClassWrapper newClass = new ClassWrapper(currentContent);
					List<String> lines = CodeRepositoryUtil.getContentByLines(currentContent);
					
					Integer added = 0;
					for (String line : lines) {
						if(CodeRepositoryUtil.isComment(line))
							continue;
						added++;
					}
					
					Integer addedMethods = newClass.getMethods().size();
					
					Metric addedLocMetric = new Metric();
					addedLocMetric.setObjectId(changedPath.getId());
					addedLocMetric.setObjectType("ChangedPath");
					addedLocMetric.setValue(added.floatValue());
					addedLocMetric.setType(addedLOCMetricType);
					addedLocMetric.setMinerSlug(this.minerSlug);
					metricDao.save(addedLocMetric);

					Metric addedMethodsMetric = new Metric();
					addedMethodsMetric.setObjectId(changedPath.getId());
					addedMethodsMetric.setObjectType("ChangedPath");
					addedMethodsMetric.setValue(addedMethods.floatValue());
					addedMethodsMetric.setType(addedMethodsMetricType);
					addedMethodsMetric.setMinerSlug(this.minerSlug);
					metricDao.save(addedMethodsMetric);
				}
				
				else{
					
					System.out.print(">>> It's looking for path revisions ... ");
					
					List<String> fileRevisions = codeRepository.getFileRevisions(changedPath.getPath(), null, changedPath.getCommit().getRevision());
					
					System.out.println("Done! "+fileRevisions.size()+" revisions were founded!");
					
					String previousRevision = CodeRepositoryUtil.getPreviousRevision(changedPath.getCommit().getRevision(), fileRevisions);
					String previousContent = null;
					
					previousContent = codeRepository.getFileContent(changedPath.getPath(), previousRevision);
					
					if(currentContent == null || previousContent == null)
						continue;
					
					calculateLOCMetrics(changedPath, previousContent, currentContent);
					calculateMethodMetrics(changedPath, previousContent, currentContent);
				}	
			}
			System.out.println("---------------------------------------------------------");
		}
	}
	
	private void calculateMethodMetrics(ChangedPath changedPath, String previousContent, String currentContent) {
		ClassWrapper previousClass = new ClassWrapper(previousContent);
		ClassWrapper currentClass = new ClassWrapper(currentContent);
		
		Integer addedMethods = 0;
		Integer changedMethods = 0;
		
		ArrayList<MethodWrapper> previousMethods = (ArrayList<MethodWrapper>) previousClass.getMethods();
		ArrayList<MethodWrapper> currentMethods = (ArrayList<MethodWrapper>) currentClass.getMethods();
		
		for (MethodWrapper currentMethod : currentMethods) {
			if(!previousMethods.contains(currentMethod)) {
				addedMethods++;
			}
		}
		
		Metric addedMethodsMetric = new Metric();
		addedMethodsMetric.setObjectId(changedPath.getId());
		addedMethodsMetric.setObjectType("ChangedPath");
		addedMethodsMetric.setValue(addedMethods.floatValue());
		addedMethodsMetric.setType(addedMethodsMetricType);
		addedMethodsMetric.setMinerSlug(this.minerSlug);
		metricDao.save(addedMethodsMetric);

		for(MethodWrapper currentMethod : currentMethods) {
			
			MethodWrapper correspondingPreviousMethod = null;
			for(MethodWrapper previousMethod : previousMethods) {
				if(previousMethod.equals(currentMethod)) {
					correspondingPreviousMethod = previousMethod;
					break;
				}
			}
			if(correspondingPreviousMethod == null) {
				continue;
			}
			
			List<String> previousMethodBody = CodeRepositoryUtil.getContentByLines(correspondingPreviousMethod.toString());
			List<String> currentMethodBody = CodeRepositoryUtil.getContentByLines(currentMethod.toString());
			Patch<String> patch = DiffUtils.diff(previousMethodBody, currentMethodBody);
			List<Delta<String>> deltas = patch.getDeltas();
			if(deltas.size() > 0) {	
				changedMethods++;
			}
		}
		
		Metric changedMethodsMetric = new Metric();
		changedMethodsMetric.setObjectId(changedPath.getId());
		changedMethodsMetric.setObjectType("ChangedPath");
		changedMethodsMetric.setValue(changedMethods.floatValue());
		changedMethodsMetric.setType(changedMethodsMetricType);
		changedMethodsMetric.setMinerSlug(this.minerSlug);
		metricDao.save(changedMethodsMetric);
	}

	private void calculateLOCMetrics(ChangedPath changedPath, String previousContent, String currentContent) {
		
		Integer added = 0;
		Integer deleted = 0;
		Integer changed = 0;
		
		Patch<String> patch = DiffUtils.diff(CodeRepositoryUtil.getContentByLines(previousContent), CodeRepositoryUtil.getContentByLines(currentContent));
			
		List<Delta<String>> deltas = patch.getDeltas();
		
		for (Delta<String> delta : deltas) {
			switch(delta.getType()) {
			case DELETE:
				List<String> deletedLines = (List<String>) delta.getOriginal().getLines();
				for (String line : deletedLines) {
					if(CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					deleted++;
				}
				break;
			case CHANGE:
				List<String> changedLines = (List<String>) delta.getRevised().getLines();
				for (String line : changedLines) {
					if(CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					changed++;
				}
				break;
			case INSERT:
				List<String> addedLines = (List<String>) delta.getRevised().getLines();
				for (String line : addedLines) {
					if(CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					added++;
				}
				break;
			}
		}

		Metric addedLocMetric = new Metric();
		addedLocMetric.setObjectId(changedPath.getId());
		addedLocMetric.setObjectType("ChangedPath");
		addedLocMetric.setValue(added.floatValue());
		addedLocMetric.setType(addedLOCMetricType);
		addedLocMetric.setMinerSlug(this.minerSlug);
		metricDao.save(addedLocMetric);

		Metric changedLocMetric = new Metric();
		changedLocMetric.setObjectId(changedPath.getId());
		changedLocMetric.setObjectType("ChangedPath");
		changedLocMetric.setValue(changed.floatValue());
		changedLocMetric.setType(changedLOCMetricType);
		changedLocMetric.setMinerSlug(this.minerSlug);
		metricDao.save(changedLocMetric);

		Metric deletedLocMetric = new Metric();
		deletedLocMetric.setObjectId(changedPath.getId());
		deletedLocMetric.setObjectType("ChangedPath");
		deletedLocMetric.setValue(deleted.floatValue());
		deletedLocMetric.setType(deletedLOCMetricType);
		deletedLocMetric.setMinerSlug(this.minerSlug);
		metricDao.save(deletedLocMetric);
	}

	public List<Commit> getSpecificCommitsToMining() {
		return specificCommitsToMining;
	}

	public void setSpecificCommitsToMining(List<Commit> specificCommitsToMining) {
		this.specificCommitsToMining = specificCommitsToMining;
	}
}