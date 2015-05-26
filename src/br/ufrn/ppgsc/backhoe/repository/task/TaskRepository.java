package br.ufrn.ppgsc.backhoe.repository.task;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public interface TaskRepository extends Repository{
	
	List<Task> findBugFixTasks(Date startDate, Date endDate, long[] systems);
	List<Task> findBuggedTasks(Date dataInicio, Date dataFim, long[] sistemas);
	List<Task> findFiledTestsTasks(Date dataInicio, Date dataFim, long[] sistemas);
	
	List<TaskLog> findFiledTestsTaskLogs(List<Task> tasks, List<String> developers);
	List<TaskLog> findBugFixTaskLogs(List<Task> tasks, List<String> developers);
	List<TaskLog> findBuggedTaskLogs(List<Task> tasks, List<String> developers);
	
	List<Task> filterBugFixTasks(List<Task> tasks);
	
	List<Task> findTasks(Date startDate, Date endDate,
			 long[] taskTypeIDs,
			 long[] taskStatusIDs,
			 long[] systems);
	List<TaskLog> findTaskLogsFromTasks(List<Task> tasks,
			   long[] logTypeIDs,
			   List<String> developers);
	Connection getConnection();
	
}