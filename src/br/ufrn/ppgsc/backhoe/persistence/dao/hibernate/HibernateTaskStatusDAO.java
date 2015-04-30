package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskStatusDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskStatus;

public class HibernateTaskStatusDAO extends HibernateGenericDAO<TaskStatus, Long> implements AbstractTaskStatusDAO {

}
