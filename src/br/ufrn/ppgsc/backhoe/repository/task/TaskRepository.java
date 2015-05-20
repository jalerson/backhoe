package br.ufrn.ppgsc.backhoe.repository.task;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public interface TaskRepository extends Repository{
	
	List<TaskLog> findLogs(Date startDate, Date endDate, long[] systems, List<String> developers);
	Task findTaskByCommit(Commit commit);
	List<TaskLog> findBuggedTaskLogs(List<Task> tasks, List<String> developers);
	List<Task> findBuggedTasks(Date dataInicio, Date dataFim, long[] sistemas);
	List<Task> findFiledTestsTasks(Date dataInicio, Date dataFim, long[] sistemas);
	List<TaskLog> findFiledTestsTaskLogs(List<Task> tasks, List<String> developers);
	Connection getConnection();
	
}