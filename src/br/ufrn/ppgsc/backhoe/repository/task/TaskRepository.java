package br.ufrn.ppgsc.backhoe.repository.task;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public interface TaskRepository extends Repository{
	
	List<TaskLog> findLogs(Date startDate, Date endDate, long[] systems);

}
