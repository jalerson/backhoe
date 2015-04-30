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
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskPriority;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;

public class IProjectRepository extends AbstractRepository implements TaskRepository{
	
	private Connection conection;
	private AbstractTaskDAO taskDAO;
//	private AbstractTaskLogDAO taskLogDAO;
	private AbstractDeveloperDAO developerDAO;
	
	public IProjectRepository(String username, String password, String url){
		super(username, password, url);
		try {
			this.taskDAO = (AbstractTaskDAO) DAOFactory.createDAO(DAOType.TASK);
//			this.taskLogDAO = (AbstractTaskLogDAO) DAOFactory.createDAO(DAOType.TASK_LOG);
			this.developerDAO = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
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
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<TaskLog> findLogs(Date startDate, Date endDate, long[] systems) {
		ArrayList<TaskLog> logs = new ArrayList<TaskLog>();
		String sql = "select usuario.login, log_tarefa.*, tarefa.* from iproject.log_tarefa"+
						" inner join iproject.tarefa on tarefa.id_tarefa = log_tarefa.id_tarefa"+
						" INNER JOIN comum.usuario ON tarefa.id_responsavel = usuario.id_usuario"+
						" where log_tarefa.id_tipo_log in (1, 13)"+
						" and tarefa.id_tipo_tarefa in (103, 102, 106, 4, 44, 46, 47, 43, 49, 51, 52)"+
						" AND tarefa.id_subsistema in (select id_sub_sistema from iproject.subsistema where id_sistema in [sistemas])"+
						" AND log_tarefa.data between '"+startDate+"' and '"+endDate+"'";
		sql = sql.replace("[sistemas]", preparePlaceholders(systems));
		
		try {
			Statement stm = this.conection.createStatement();
			ResultSet rs = stm.executeQuery(sql);
			
			while (rs.next()){
				Task task = new Task();
//				task.setPriority(String.valueOf(rs.getInt("prioridade")));
				TaskPriority taskPriority = new TaskPriority(null, rs.getInt("prioridade"));
				task.setPriority(taskPriority);
				Developer developer = developerDAO.findByCodeRepositoryUsername(rs.getString("login"));
				if(developer == null) {
					System.out.println("Entrei aqui: Developer: "+developer);
					developer = new Developer();
					developer.setCodeRepositoryUsername(rs.getString("login"));
				}
				task.setCreatedBy(developer);
				task.setId(rs.getLong("id_tarefa"));
				task.setTitle(rs.getString("titulo"));
				developer.getTasks().add(task);
				
				TaskLog log = new TaskLog();
				log.setCreatedAt(rs.getDate("data"));
				log.setDescription(rs.getString("log"));
				log.setAuthor(developer);
				developer.getLogs().add(log);
				
				task.getTaskLogs().add(log);
				log.setTask(task);
				
//				taskLogDAO.save(log);
				taskDAO.save(task);
				
				System.out.println(log);
//				new Scanner(System.in).nextLine();

//				taskLogDAO.save(log);
				logs.add(log);
			}
//			tx.commit();
			return logs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
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
	
//	private String getAuthor(String log, List<String> developers) {
//		for (String login : developers)
//			if(log.contains(login))
//				return login;
//		return null;
//	}
}
