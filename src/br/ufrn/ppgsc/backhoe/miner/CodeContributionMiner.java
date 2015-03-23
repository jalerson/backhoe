package br.ufrn.ppgsc.backhoe.miner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractChangedPathDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.util.CodeRepositoryUtil;
import br.ufrn.ppgsc.backhoe.vo.wrapper.ClassWrapper;
import br.ufrn.ppgsc.backhoe.vo.wrapper.MethodWrapper;

public class CodeContributionMiner extends Miner {
	private Date startDate;
	private Date endDate;
	private List<String> developers;
	private CodeRepository repository;
	private AbstractCommitDAO commitDao;
	private AbstractChangedPathDAO changedPathDao;
	private AbstractMetricDAO metricDao;
	private AbstractMetricTypeDAO metricTypeDao;
	private MetricType addedLOCMetricType;
	private MetricType changedLOCMetricType;
	private MetricType deletedLOCMetricType;
	private MetricType addedMethodsMetricType;
	private MetricType changedMethodsMetricType;
	private List<String> ignoredPaths;
	
	public CodeContributionMiner(CodeRepository repository, Date startDate, Date endDate, List<String> developers, List<String> ignoredPaths) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.developers = developers;
		this.repository = repository;
		this.ignoredPaths = ignoredPaths;
	}
	
	@Override
	public boolean setup() throws MissingParameterException {
		if(repository == null) {
			throw new MissingParameterException("Missing mandatory parameter: CodeRepository repository");
		}
		if(startDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date startDate");
		}
		if(endDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date endDate");
		}
		
		try {
			this.commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
			this.changedPathDao = (AbstractChangedPathDAO) DAOFactory.createDAO(DAOType.CHANGED_PATH);
			this.metricDao = (AbstractMetricDAO) DAOFactory.createDAO(DAOType.METRIC);
			this.metricTypeDao = (AbstractMetricTypeDAO) DAOFactory.createDAO(DAOType.METRIC_TYPE);
		} catch (DAONotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.addedLOCMetricType = metricTypeDao.findBySlug("loc:added");
		if(this.addedLOCMetricType == null) {
			this.addedLOCMetricType = new MetricType();
			this.addedLOCMetricType.setName("Added LOC");
			this.addedLOCMetricType.setSlug("loc:added");
			metricTypeDao.save(this.addedLOCMetricType);
		}
		this.changedLOCMetricType = metricTypeDao.findBySlug("loc:changed");
		if(this.changedLOCMetricType == null) {
			this.changedLOCMetricType = new MetricType();
			this.changedLOCMetricType.setName("Changed LOC");
			this.changedLOCMetricType.setSlug("loc:changed");
			metricTypeDao.save(this.changedLOCMetricType);
		}
		this.deletedLOCMetricType = metricTypeDao.findBySlug("loc:deleted");
		if(this.deletedLOCMetricType == null) {
			this.deletedLOCMetricType = new MetricType();
			this.deletedLOCMetricType.setName("Deleted LOC");
			this.deletedLOCMetricType.setSlug("loc:deleted");
			metricTypeDao.save(this.deletedLOCMetricType);
		}
		this.addedMethodsMetricType = metricTypeDao.findBySlug("methods:added");
		if(this.addedMethodsMetricType == null) {
			this.addedMethodsMetricType = new MetricType();
			this.addedMethodsMetricType.setName("Added Methods");
			this.addedMethodsMetricType.setSlug("methods:added");
			metricTypeDao.save(this.addedMethodsMetricType);
		}
		this.changedMethodsMetricType = metricTypeDao.findBySlug("methods:changed");
		if(this.changedMethodsMetricType == null) {
			this.changedMethodsMetricType = new MetricType();
			this.changedMethodsMetricType.setName("Changed Methods");
			this.changedMethodsMetricType.setSlug("methods:changed");
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
		
		for (Commit commit : commits) {
			List<ChangedPath> changedPaths = commit.getChangedPaths();
			for (ChangedPath changedPath : changedPaths) {
				if(changedPath.getChangeType().equals('D')) {
					continue;
				}
				if(!(changedPath.getPath().toLowerCase().contains(".java"))) {
					continue;
				}
				
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
