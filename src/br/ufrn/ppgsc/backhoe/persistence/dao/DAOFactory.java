package br.ufrn.ppgsc.backhoe.persistence.dao;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateChangedLineDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateChangedPathDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateMetricDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateMetricTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateProjectDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskLogDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskPriorityDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskStatusDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskTypeDAO;

public class DAOFactory {
	
	public static AbstractDAO createDAO(DAOType type) throws DAONotFoundException {
		switch(type) {
			case CHANGED_LINE: return new HibernateChangedLineDAO();
			case CHANGED_PATH: return new HibernateChangedPathDAO();
			case COMMIT: return new HibernateCommitDAO();
			case DEVELOPER: return new HibernateDeveloperDAO();
			case METRIC: return new HibernateMetricDAO();
			case METRIC_TYPE: return new HibernateMetricTypeDAO();
			case PROJECT: return new HibernateProjectDAO();
			case TASK: return new HibernateTaskDAO();
			case TASK_LOG: return new HibernateTaskLogDAO();
			case TASK_PRIORITY: return new HibernateTaskPriorityDAO();
			case TASK_STATUS: return new HibernateTaskStatusDAO();
			case TASK_TYPE: return new HibernateTaskTypeDAO();
			default: throw new DAONotFoundException("DAO not found!");
		}
	}
	
}
