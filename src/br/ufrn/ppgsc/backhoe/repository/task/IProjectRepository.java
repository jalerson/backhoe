package br.ufrn.ppgsc.backhoe.repository.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractProjectDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskLogDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskLogTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskPriorityDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskStatusDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Project;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLogType;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskPriority;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskStatus;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskType;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;

public class IProjectRepository extends AbstractRepository implements TaskRepository{
	
	private Connection conection;
	private AbstractTaskDAO taskDAO;
	private AbstractTaskLogDAO taskLogDAO;
	private AbstractDeveloperDAO developerDAO;
	private AbstractTaskStatusDAO taskStatusDAO;
	private AbstractTaskPriorityDAO taskPriorityDAO;
	private AbstractTaskTypeDAO taskTypeDAO;
	private AbstractProjectDAO projectDAO;
	private AbstractTaskLogTypeDAO taskLogTypeDAO;
	private AbstractCommitDAO commitDAO;
	
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
			this.commitDAO = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
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
			this.conection = DriverManager.getConnection(url, username, password);
			//
			populateSampleTablesFromIproject();
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public List<TaskLog> findLogs(Date startDate, Date endDate, long[] systems, List<String> developers) {
		
		ArrayList<TaskLog> logs = new ArrayList<TaskLog>();
		String sql = "SELECT usuario.login, log_tarefa.*, tarefa.* from iproject.log_tarefa"+
						" INNER JOIN iproject.tarefa on tarefa.id_tarefa = log_tarefa.id_tarefa"+
						" INNER JOIN comum.usuario ON tarefa.id_responsavel = usuario.id_usuario"+
						" where log_tarefa.id_tipo_log in (1, 13)"+
						" AND tarefa.id_tipo_tarefa in (103, 102, 106, 4, 44, 46, 47, 43, 49, 51, 52)"+
						" AND tarefa.id_subsistema in (select id_sub_sistema from iproject.subsistema where id_sistema in [sistemas])"+
						" AND log_tarefa.data between '"+startDate+"' and '"+endDate+"'";
		sql = sql.replace("[sistemas]", preparePlaceholders(systems));
		
		try {
			Statement stm = this.conection.createStatement();
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
	
	public void associateTaskToCommitFromIProject(List<Commit> commits, List<String> developers){
		System.out.println("Looking for tasks in iProject... ");
		for(Commit commit : commits) {
			
			if (commitDAO.findByRevision(commit.getRevision()) != null && commit.getLog() != null)
				continue;
			
			System.out.print("Looking for task of revision "+commit.getRevision()+"... ");
			
			try {
				
				String sql = "SELECT usuario.login, log_tarefa.*, tarefa.* "
						+"FROM iproject.log_tarefa "
						+"INNER JOIN iproject.tarefa ON tarefa.id_tarefa = log_tarefa.id_tarefa "
						+"INNER JOIN iproject.prioridade ON tarefa.id_prioridade = prioridade.id_prioridade "
						+"INNER JOIN comum.usuario ON usuario.id_usuario = tarefa.id_responsavel "
						+"INNER JOIN iproject.status_tarefa ON status_tarefa.id = tarefa.id_status "
						+"INNER JOIN iproject.tipo_tarefa ON tipo_tarefa.id_tipo_tarefa = tarefa.id_tipo_tarefa "
						+"WHERE (log_tarefa.revision = '"+commit.getRevision()+"' OR EXISTS(SELECT regexp_matches(log_tarefa.log, '(Revisão|revisão|Revisao|revisao)[:| ]+"+commit.getRevision()+"'))) ";
//						+"LIMIT 1";
	
				Statement stm = this.conection.createStatement();
				ResultSet rs = stm.executeQuery(sql);
				
				while(rs.next()) {
					System.out.println("Entrei aqui!");
					
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
					commit.setLog(log);
					commitDAO.update(commit);
					System.out.println(commit);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done!");
		}
		System.out.println("Looking for tasks in iProject... Done!");
	}
	
	private String preparePlaceholders(long[] values) {
		String sql = "(";
		for (int i = 0; i < values.length; i++){
			sql += values[i];
			if (i != (values.length - 1))
				sql += ", ";
		}
		sql += ")";
		return sql;
	}

	public Connection getConection() {
		return conection;
	}
	
	public void populateSampleTablesFromIproject(){
		populateTaskStatusFromIproject();
		populateTaskPriorityFromIproject();
		populateTaskTypeFromIproject();
		populateProjectFromIproject();
		populateTaskLogTypeFromIproject();
	}
	
	private void populateTaskStatusFromIproject(){
		String taskStatusSQL = "SELECT id, denominacao FROM iproject.status_tarefa";
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(taskStatusSQL);
			while (rs.next()){
				Long taskStatusID = rs.getLong("id");
				if (taskStatusDAO.findByID(taskStatusID) == null){
					TaskStatus taskStatus = new TaskStatus(taskStatusID, rs.getString("denominacao")); 
					taskStatusDAO.save(taskStatus);
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void populateTaskPriorityFromIproject(){
		String taskPrioritySQL = "SELECT id_prioridade, denominacao FROM iproject.prioridade";
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(taskPrioritySQL);
			while (rs.next()){
				Long taskPriorityID = rs.getLong("id_prioridade"); 
				if (taskPriorityDAO.findByID(taskPriorityID) == null){
					TaskPriority taskStatus = new TaskPriority(taskPriorityID, rs.getString("denominacao")); 
					taskPriorityDAO.save(taskStatus);
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void populateTaskTypeFromIproject(){
		String taskTypeSQL = "SELECT id_tipo_tarefa, denominacao FROM iproject.tipo_tarefa";
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(taskTypeSQL);
			while (rs.next()){
				Long taskTypeID = rs.getLong("id_tipo_tarefa"); 
				if (taskTypeDAO.findByID(taskTypeID) == null){
					TaskType taskStatus = new TaskType(taskTypeID, rs.getString("denominacao")); 
					taskTypeDAO.save(taskStatus);
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void populateProjectFromIproject(){
		String projectSQL = "SELECT id_projeto, nome FROM iproject.projeto";
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(projectSQL);
			while (rs.next()){
				Long projectID = rs.getLong("id_projeto"); 
				if (projectDAO.findByID(projectID) == null){
					Project taskStatus = new Project(projectID, rs.getString("nome")); 
					projectDAO.save(taskStatus);
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void populateTaskLogTypeFromIproject(){
		String projectSQL = "SELECT id_tipo_log, denominacao FROM iproject.tipo_log";
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(projectSQL);
			while (rs.next()){
				Long taskLogTypeID = rs.getLong("id_tipo_log"); 
				if (taskLogTypeDAO.findByID(taskLogTypeID) == null){
					TaskLogType taskLogType = new TaskLogType(taskLogTypeID, rs.getString("denominacao")); 
					taskLogTypeDAO.save(taskLogType);
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Developer getAuthor(String log, List<String> developers) {
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
}
