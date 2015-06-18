package br.ufrn.ppgsc.backhoe.repository.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractProjectDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskLogDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskLogTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskPriorityDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskStatusDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Project;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLogType;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskPriority;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskStatus;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskType;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;
import br.ufrn.ppgsc.backhoe.util.TaskRepositoryUtil;

public class IProjectRepository extends AbstractRepository implements TaskRepository{
	
	private Connection connection;
	private AbstractTaskDAO taskDAO;
	private AbstractTaskLogDAO taskLogDAO;
	private AbstractDeveloperDAO developerDAO;
	private AbstractTaskStatusDAO taskStatusDAO;
	private AbstractTaskPriorityDAO taskPriorityDAO;
	private AbstractTaskTypeDAO taskTypeDAO;
	private AbstractProjectDAO projectDAO;
	private AbstractTaskLogTypeDAO taskLogTypeDAO;
	
	public IProjectRepository(String username, String password, String url){
		super(username, password, url);
		try {
			this.taskDAO = (AbstractTaskDAO) DAOFactory.createDAO(DAOType.TASK);
			this.taskLogDAO = (AbstractTaskLogDAO) DAOFactory.createDAO(DAOType.TASK_LOG);
			this.developerDAO = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
			this.taskStatusDAO = (AbstractTaskStatusDAO) DAOFactory.createDAO(DAOType.TASK_STATUS);
			this.taskPriorityDAO = (AbstractTaskPriorityDAO) DAOFactory.createDAO(DAOType.TASK_PRIORITY);
			this.taskTypeDAO = (AbstractTaskTypeDAO) DAOFactory.createDAO(DAOType.TASK_TYPE);
			this.projectDAO = (AbstractProjectDAO) DAOFactory.createDAO(DAOType.PROJECT);
			this.taskLogTypeDAO = (AbstractTaskLogTypeDAO) DAOFactory.createDAO(DAOType.TASK_LOG_TYPE);
		} catch (DAONotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public IProjectRepository(){
		this(null, null, null);
	}

	@Override
	public boolean connect() throws MissingParameterException {
		
		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if(username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if(password == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		
		try {
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(url, username, password);
			//
			TaskRepositoryUtil.populateSampleTablesFromTaskRepository(this);
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public List<Task> findBugFixTasks(Date startDate, Date endDate, long[] systems){
		
		/* id_tipo_tarefa in 
		 *  103 - ERRO DE EXECUCAO
		 *  102 - ERRO DE NEGÓCIO/VALIDAÇÃO
		 *	106 - ERRO DE AMBIENTE
		 *	4   - ERRO
		 *	44  - ERRO DE REQUISITO NAO-FUNCIONAL
		 *	46  - ERRO DE PADRAO DE NAVEGACAO
		 *	47  - ERRO DE PADROES ARQUITETURAIS
		 *	43  - ERRO DE PADRONIZACAO DE VISUALIZACAO
		 *	49  - ERRO DE COMPORTAMENTO ESPERADO
		 *	51  - ERRO DE COMPORTAMENTO/ESTRUTURAL
		 *  52  - NOTIFICAÇÃO DE ERRO
		 */
		long[] taskTypeIDs = {103, 102, 106, 4, 44, 46, 47, 43, 49, 51, 52};
		return findTasks(startDate, endDate, taskTypeIDs, null, systems);
	}
	
	public List<Task> filterBugFixTasks(List<Task> tasks){
		List<Task> filteredTasks = new ArrayList<Task>();
		
		List<Long> bugFixTaskTypeIDs = new ArrayList<Long>(Arrays.asList(new Long[]{103L, 102L, 106L, 4L, 44L, 46L, 
				 					 47L, 43L, 49L, 51L, 52L}));
		for(Task task: tasks)
			if(bugFixTaskTypeIDs.contains(task.getType().getId()))
				filteredTasks.add(task);

		return filteredTasks;
	}
	
	public List<Task> filterFailedTestsTasks(List<Task> tasks){
		List<Task> filteredTasks = new ArrayList<Task>();
		
		List<Long> faildedTaskStatusIDs = new ArrayList<Long>(Arrays.asList(new Long[]{16L}));
		
		for(Task task: tasks)
			if(faildedTaskStatusIDs.contains(task.getStatus().getId()))
				filteredTasks.add(task);

		return filteredTasks;
	}
	
	public List<Task> filterBuggedTasks(List<Task> tasks){
		List<Task> filteredTasks = new ArrayList<Task>();
		
		List<Long> buggyTaskStatusIDs = new ArrayList<Long>(Arrays.asList(new Long[]{43L, 17L, 20L}));
		List<Long> buggyTaskTypeIDs = new ArrayList<Long>(Arrays.asList(new Long[]{103L, 106L, 4L, 44L, 46L, 
																				   47L, 43L, 28L, 49L, 51L}));
		for(Task task: tasks)
			if(buggyTaskStatusIDs.contains(task.getStatus().getId()) &&
			   buggyTaskTypeIDs.contains(task.getType().getId()))
				filteredTasks.add(task);

		return filteredTasks;
	}
	
	public List<Task> findFiledTestsTasks(Date startDate, Date endDate, long[] systems) {
		
		/* id_tipo_tarefa in 
		 * 16 - ERRO DE TESTE
		 */
		
		long[] taskStatusIDs = {16};
		return findTasks(startDate, endDate, null, taskStatusIDs, systems);
	}
	
	public List<Task> findBuggedTasks(Date startDate, Date endDate, long[] systems){

		/* id_tipo_tarefa in 
		 *  103 - ERRO DE EXECUCAO
		 *	106 - ERRO DE AMBIENTE
		 *	4   - ERRO
		 *	44  - ERRO DE REQUISITO NAO-FUNCIONAL
		 *	46  - ERRO DE PADRAO DE NAVEGACAO
		 *	47  - ERRO DE PADROES ARQUITETURAIS
		 *	43  - ERRO DE PADRONIZACAO DE VISUALIZACAO
		 *	28  - ERRO DE PADRONIZACAO DE CODIGO
		 *	49  - ERRO DE COMPORTAMENTO ESPERADO
		 *	51  - ERRO DE COMPORTAMENTO/ESTRUTURAL
		 *
		 * id_status in
		 * 	43 - CONCLUIDA
		 * 	17 - FINALIZADA
		 * 	20 - EM PRODUCAO
		 */
		long[] taskTypeIDs = {103, 106, 4, 44, 46, 47, 43, 28, 49, 51},
			   taskStatusIDs = {43, 17, 20};
		return findTasks(startDate, endDate, taskTypeIDs, taskStatusIDs, systems);
	}
	
	public List<TaskLog> findBugFixTaskLogs(List<Task> tasks, List<String> developers) {
		
		/* id_tipo_log in 
		 *  1  - DESENVOLVIMENTO
		 *  13 - SOLICITAÇÃO DE UPDATE EM PRODUÇÃO
		 */ 
		
		long[] logTypeIDs = {1, 13};
		return findTaskLogsFromTasks(tasks, logTypeIDs, developers);
	}
	
	public List<TaskLog> findBugFixTaskLogs(Date startDate, Date endDate, long[] systems, List<String> developers) {
		
		/* id_tipo_log in 
		 *  1  - DESENVOLVIMENTO
		 *  13 - SOLICITAÇÃO DE UPDATE EM PRODUÇÃO
		 */ 
		
		long[] logTypeIDs = {1, 13};
		long[]  bugFixTaskTypeIDs = {103L, 102L, 106L, 4L, 44L, 46L, 
				 47L, 43L, 49L, 51L, 52L};
		return findTaskLogs(logTypeIDs, developers, startDate, endDate, bugFixTaskTypeIDs, null, systems);
	}
	
	public List<TaskLog> findFiledTestsTaskLogs(List<Task> tasks, List<String> developers){
		
		/* id_tipo_log in 
		 *  1 - DESENVOLVIMENTO
		 */ 
		
		long[] logTypeIDs = {1};
		return findTaskLogsFromTasks(tasks, logTypeIDs, developers);
	}
	
	public List<TaskLog> findBuggedTaskLogs(List<Task> tasks, List<String> developers){
		
		/* id_tipo_log in 
		 *  13 - SOLICITAÇÃO DE UPDATE EM PRODUÇÃO
		 */ 
		
		long[] logTypeIDs = {13};
		return findTaskLogsFromTasks(tasks, logTypeIDs, developers);
	}
	
	public List<Task> findTasks(Date startDate, Date endDate,
								 long[] taskTypeIDs,
								 long[] taskStatusIDs,
								 long[] systems){
		
		List<Task> tasks = new ArrayList<Task>();
		
		String sql = "SELECT tarefa.*, usuario.login "+
					 " FROM iproject.tarefa "+
					 " INNER JOIN comum.usuario ON tarefa.id_responsavel = usuario.id_usuario "+
					 " WHERE tarefa.data_cadastro between '[startDate]' and '[endDate]' ";
		
		sql = sql.replace("[startDate]", startDate.toString());
		sql = sql.replace("[endDate]", endDate.toString());
		
		if(systems != null && systems.length != 0){
			sql += " AND tarefa.id_subsistema in (select id_sub_sistema from "
					+ "iproject.subsistema where id_sistema in "+ preparePlaceholders(systems)+")";
		}
		if(taskTypeIDs != null && taskTypeIDs.length != 0){
			sql +=  "AND tarefa.id_tipo_tarefa in "+ preparePlaceholders(taskTypeIDs);
		}
		if(taskStatusIDs != null && taskStatusIDs.length != 0){
			sql += " AND tarefa.id_status in "+ preparePlaceholders(taskStatusIDs);
		}
		
		sql += ";";

		try {
			Statement stm = this.connection.createStatement();
			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) {
				
				String login = rs.getString("login");
				Developer developer = developerDAO.findByCodeRepositoryUsername(login);
				if(developer == null){
					developer = new Developer();
					developer.setCodeRepositoryUsername(login);
					developerDAO.save(developer);
				}
				
				Long taskID = rs.getLong("id_tarefa");
				Task task = taskDAO.findByID(taskID);
				if(task == null){
					TaskType taskType = taskTypeDAO.findByID(rs.getLong("id_tipo_tarefa"));
					TaskPriority taskPriority = taskPriorityDAO.findByID(rs.getLong("id_prioridade"));
					TaskStatus taskStatus = taskStatusDAO.findByID(rs.getLong("id_status"));
					Project project = projectDAO.findByID(rs.getLong("id_projeto"));
					String title = rs.getString("titulo");
					Date createdAt = rs.getDate("data_cadastro");
					task = new Task(taskID, title, createdAt, null, taskType, taskPriority, taskStatus,project, developer);
					taskDAO.save(task);
				}
				tasks.add(task);
			}
			return tasks;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<TaskLog> findTaskLogs(long[] logTypeIDs,
									  List<String> developers,
									  Date startDate, Date endDate,
									  long[] taskTypeIDs,
									  long[] taskStatusIDs,
									  long[] systems){
		
		ArrayList<TaskLog> logs = new ArrayList<TaskLog>();
		String sql = "SELECT usuario.login, log_tarefa.*, tarefa.* from iproject.log_tarefa"+
						" INNER JOIN iproject.tarefa on tarefa.id_tarefa = log_tarefa.id_tarefa"+
						" INNER JOIN comum.usuario ON tarefa.id_responsavel = usuario.id_usuario"+
						" where log_tarefa.data between '"+startDate+"' and '"+endDate+"'";
		
		sql = sql.replace("[startDate]", startDate.toString());
		sql = sql.replace("[endDate]", endDate.toString());
		
		if(logTypeIDs != null && logTypeIDs.length != 0){
			sql += "  AND id_tipo_log IN " + preparePlaceholders(logTypeIDs) ;
		}
		
		if(systems != null && systems.length != 0){
			sql += " AND tarefa.id_subsistema in (select id_sub_sistema from "
					+ "iproject.subsistema where id_sistema in "+ preparePlaceholders(systems)+")";
		}
		if(taskTypeIDs != null && taskTypeIDs.length != 0){
			sql +=  "AND tarefa.id_tipo_tarefa in "+ preparePlaceholders(taskTypeIDs);
		}
		if(taskStatusIDs != null && taskStatusIDs.length != 0){
			sql += " AND tarefa.id_status in "+ preparePlaceholders(taskStatusIDs);
		}
		
		try {
			Statement stm = this.connection.createStatement();
			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()){
				String login = rs.getString("login");
				Developer developer = developerDAO.findByCodeRepositoryUsername(login);
				if(developer == null){
					developer = new Developer();
					developer.setCodeRepositoryUsername(login);
					developerDAO.save(developer);
				}
				
				Long taskID = rs.getLong("id_tarefa");
				Task task = taskDAO.findByID(taskID);
				if(task == null){
					TaskType taskType = taskTypeDAO.findByID(rs.getLong("id_tipo_tarefa"));
					TaskPriority taskPriority = taskPriorityDAO.findByID(rs.getLong("id_prioridade"));
					TaskStatus taskStatus = taskStatusDAO.findByID(rs.getLong("id_status"));
					Project project = projectDAO.findByID(rs.getLong("id_projeto"));
					String title = rs.getString("titulo");
					Date createdAt = rs.getDate("data_cadastro");
					task = new Task(taskID, title, createdAt, null, taskType, taskPriority, taskStatus,project, developer);
					taskDAO.save(task);
				}
				
				Long logID = rs.getLong("id");
				TaskLog log = taskLogDAO.findByID(logID);
				if(log == null){
					log = new TaskLog();
					TaskLogType taskLogType = taskLogTypeDAO.findByID(rs.getLong("id_tipo_log"));
					log.setId(logID);
					log.setCreatedAt(rs.getDate("data"));
					log.setDescription(rs.getString("log"));
					log.setTask(task);
					Developer author = getAuthor(rs.getString("log"), developers);
					if(author != null) {
						log.setAuthor(author);
					} else {
						log.setAuthor(developer);
					}
					log.setType(taskLogType);
					taskLogDAO.save(log);
				}
				logs.add(log);
			}
			return logs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<TaskLog> findTaskLogsFromTasks(List<Task> tasks,
											   long[] logTypeIDs,
											   List<String> developers){
		
		List<TaskLog> logs = new ArrayList<TaskLog>();

		String sql = "SELECT log_tarefa.*, usuario.login "
				   + "FROM iproject.log_tarefa "
			 	   + "INNER JOIN comum.usuario ON log_tarefa.id_usuario = usuario.id_usuario "
				   + "WHERE id_tarefa IN [tasksIDs] ";
		
		long[] tasksIDs = new long[tasks.size()];
		int index = 0;
		for (Task model: tasks)
			tasksIDs[index++] = model.getId();
		
		sql = sql.replace("[tasksIDs]", preparePlaceholders(tasksIDs));
		
		if(logTypeIDs != null && logTypeIDs.length != 0){
			sql += "  AND id_tipo_log IN " + preparePlaceholders(logTypeIDs) ;
		}
		
		Hashtable<Long, Task> taskIdToTaskMAP = new Hashtable<Long, Task>();
		
		for (Task task : tasks)
			taskIdToTaskMAP.put(task.getId(), task);

		try {
			Statement stm = this.connection.createStatement();
			
			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) {
				
				String login = rs.getString("login");
				Developer developer = developerDAO.findByCodeRepositoryUsername(login);
				if(developer == null){
					developer = new Developer();
					developer.setCodeRepositoryUsername(login);
					developerDAO.save(developer);
				}
				
				Long logID = rs.getLong("id");
				TaskLog log = taskLogDAO.findByID(logID);
				if(log == null){
					log = new TaskLog();
					TaskLogType taskLogType = taskLogTypeDAO.findByID(rs.getLong("id_tipo_log"));
					log.setId(logID);
					log.setCreatedAt(rs.getDate("data"));
					log.setDescription(rs.getString("log"));
					log.setTask(taskIdToTaskMAP.get(rs.getLong("id_tarefa")));
					log.setRevisions(rs.getString("revision"));
					Developer author = getAuthor(rs.getString("log"), developers);
					if(author != null) {
						log.setAuthor(author);
					} else {
						log.setAuthor(developer);
					}
					log.setType(taskLogType);
					taskLogDAO.save(log);
				}
				
				logs.add(log);
			}
			return logs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Developer getAuthor(String log, List<String> developers) {
		if(log != null)
			for (String login : developers){
				if(log.contains(login)){
					Developer developer = developerDAO.findByCodeRepositoryUsername(login);
					if(developer == null){
						developer = new Developer();
						developer.setCodeRepositoryUsername(login);
						developerDAO.save(developer);
					}
					return developer;
				}
			}
		return null;
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}
}