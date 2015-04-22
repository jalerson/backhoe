package br.ufrn.ppgsc.backhoe.miner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.util.CodeRepositoryUtil;
import br.ufrn.ppgsc.backhoe.vo.wrapper.ClassWrapper;
import br.ufrn.ppgsc.backhoe.vo.wrapper.MethodWrapper;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class CodeContributionMiner extends AbstractMiner {
	
	private MetricType addedLOCMetricType;
	private MetricType changedLOCMetricType;
	private MetricType deletedLOCMetricType;
	private MetricType addedMethodsMetricType;
	private MetricType changedMethodsMetricType;
	
	public CodeContributionMiner(CodeRepository repository, Date startDate, Date endDate, List<String> developers, List<String> ignoredPaths) {
		super(repository, startDate, endDate, developers, ignoredPaths);
	}
	
	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {
	
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
		
		return repository.connect();
	}

	@Override
	public void execute() {
		List<Commit> commits = null;
		if(developers == null) {
			commits = repository.findCommitsByTimeRange(startDate, endDate, true, ignoredPaths);
		} else {
			commits = repository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true, ignoredPaths);
		}

		commitDao.save(commits);
		
		System.out.println("\nCalculating code contribution metrics...");
		
		int processedCommits = 0;
		
		for (Commit commit : commits) {
			List<ChangedPath> changedPaths = commit.getChangedPaths();
			for (ChangedPath changedPath : changedPaths) {
				if(changedPath.getChangeType().equals('D'))
					continue;
				if(!(changedPath.getPath().toLowerCase().contains(".java")))
					continue;
				
				String currentContent = repository.getFileContent(changedPath.getPath(), changedPath.getCommit().getRevision());
				changedPath.setContent(currentContent);
				changedPathDao.update(changedPath);
				List<Long> fileRevisions = repository.getFileRevisions(changedPath.getPath(), 1L, changedPath.getCommit().getRevision());
				
				if(fileRevisions.size() > 1) {
					Long previousRevision = CodeRepositoryUtil.getPreviousRevision(changedPath.getCommit().getRevision(), fileRevisions);
					String previousContent = repository.getFileContent(changedPath.getPath(), previousRevision);
					calculateLOCMetrics(changedPath, previousContent, currentContent);
					calculateMethodMetrics(changedPath, previousContent, currentContent);
				} else {
					ClassWrapper newClass = new ClassWrapper(currentContent);
					List<String> lines = CodeRepositoryUtil.getContentByLines(currentContent);
					
					Integer added = 0;
					for (String line : lines) {
						if(CodeRepositoryUtil.isComment(line)) {
							continue;
						}
						added++;
					}
					
					Metric addedLocMetric = new Metric();
					addedLocMetric.setObjectId(changedPath.getId());
					addedLocMetric.setObjectType("ChangedPath");
					addedLocMetric.setValue(added.floatValue());
					addedLocMetric.setType(addedLOCMetricType);
					metricDao.save(addedLocMetric);
					
					Metric addedMethodsMetric = new Metric();
					addedMethodsMetric.setObjectId(changedPath.getId());
					addedMethodsMetric.setObjectType("ChangedPath");
					addedMethodsMetric.setValue(new Float(newClass.getMethods().size()));
					addedMethodsMetric.setType(addedMethodsMetricType);
					metricDao.save(addedMethodsMetric);
				}
			}
			System.out.println("[" + ++processedCommits + "] of [" + commits.size() + "] processed commits.");
		}
		System.out.println("Code Contribution Miner execut end!\n");
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
		metricDao.save(addedMethodsMetric);
		
		for(MethodWrapper currentMethod : currentMethods) {
			MethodWrapper correspondingPreviousMethod = null;
			for(MethodWrapper previousMethod : previousMethods) {
				if(previousMethod.equals(currentMethod)) {
					correspondingPreviousMethod = previousMethod;
				}
			}
			if(correspondingPreviousMethod == null) {
				continue;
			}
			
			List<String> previousMethodBody = CodeRepositoryUtil.getContentByLines(correspondingPreviousMethod.toString());
			List<String> currentMethodBody = CodeRepositoryUtil.getContentByLines(currentMethod.toString());
			Patch patch = DiffUtils.diff(previousMethodBody, currentMethodBody);
			List<Delta> deltas = patch.getDeltas();
			if(deltas.size() > 0) {
				changedMethods++;
			}
		}
		
		Metric changedMethodsMetric = new Metric();
		changedMethodsMetric.setObjectId(changedPath.getId());
		changedMethodsMetric.setObjectType("ChangedPath");
		changedMethodsMetric.setValue(changedMethods.floatValue());
		changedMethodsMetric.setType(changedMethodsMetricType);
		metricDao.save(changedMethodsMetric);
	}

	private void calculateLOCMetrics(ChangedPath changedPath, String previousContent, String currentContent) {
		Integer added = 0;
		Integer deleted = 0;
		Integer changed = 0;
		Patch patch = DiffUtils.diff(CodeRepositoryUtil.getContentByLines(previousContent), CodeRepositoryUtil.getContentByLines(currentContent));
		List<Delta> deltas = patch.getDeltas();
		
		for (Delta delta : deltas) {
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
		metricDao.save(addedLocMetric);
		
		Metric changedLocMetric = new Metric();
		changedLocMetric.setObjectId(changedPath.getId());
		changedLocMetric.setObjectType("ChangedPath");
		changedLocMetric.setValue(changed.floatValue());
		changedLocMetric.setType(changedLOCMetricType);
		metricDao.save(changedLocMetric);
		
		Metric deletedLocMetric = new Metric();
		deletedLocMetric.setObjectId(changedPath.getId());
		deletedLocMetric.setObjectType("ChangedPath");
		deletedLocMetric.setValue(deleted.floatValue());
		deletedLocMetric.setType(deletedLOCMetricType);
		metricDao.save(deletedLocMetric);
	}
}