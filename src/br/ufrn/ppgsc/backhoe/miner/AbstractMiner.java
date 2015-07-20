package br.ufrn.ppgsc.backhoe.miner;

import java.sql.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractChangedPathDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricTypeDAO;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public abstract class AbstractMiner implements Miner{
	
	protected Date startDate;
	protected Date endDate;
	protected Integer system;
	protected List<String> developers;
	protected CodeRepository codeRepository;
	protected TaskRepository taskRepository;
	protected AbstractCommitDAO commitDao;
	protected AbstractChangedPathDAO changedPathDao;
	protected AbstractMetricDAO metricDao;
	protected AbstractMetricTypeDAO metricTypeDao;
	protected List<String> ignoredPaths;
	
	public AbstractMiner(Integer system, CodeRepository codeRepository, TaskRepository taskRepository, Date startDate, Date endDate, List<String> developers, List<String> ignoredPaths) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.developers = developers;
		this.codeRepository = codeRepository;
		this.taskRepository = taskRepository;
		this.ignoredPaths = ignoredPaths;
		this.system = system;
	}

	public boolean setup() throws MissingParameterException {	
		
		if(codeRepository == null) {
			throw new MissingParameterException("Missing mandatory parameter: CodeRepository codeRepository");
		}
		if(taskRepository == null) {
			throw new MissingParameterException("Missing mandatory parameter: TaskRepository taskRepository");
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
		
		return this.setupMinerSpecific();
	}
	
	public abstract boolean setupMinerSpecific() throws MissingParameterException; 
	
	public abstract void execute();
}
