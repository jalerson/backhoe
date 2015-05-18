package br.ufrn.ppgsc.backhoe.repository.taskCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.TaskNotFoundException;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.hibernate.HibernateTaskDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.repository.code.SVNRepository;
import br.ufrn.ppgsc.backhoe.repository.task.IProjectRepository;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public class TaskCodeManager {
	
	private TaskRepository taskRepository;
	private CodeRepository codeRepository;
	private Connection conection;
	private AbstractDeveloperDAO developerDAO;
	private AbstractTaskDAO taskDAO;
	
	public TaskCodeManager(){
		this.taskRepository = new IProjectRepository();
		this.codeRepository = new SVNRepository();
		this.developerDAO = new HibernateDeveloperDAO();
		this.taskDAO = new HibernateTaskDAO();
	}
	
	public List<Commit> findCommitsByTask(Task task){
		return null;
	}
	
	public Task findTaskByCommit(Commit commit){
		String comment = commit.getComment();
		if(comment != null){
			try{
				// pegando e validando o taksID no comentario do commit
				String taskID = comment.split(" ")[1];
				Long.parseLong(taskID); 
				
				String sql = "SELECT usuario.login, tarefa.* from iproject.tarefa " + 
							 "LEFT JOIN comum.usuario ON tarefa.id_responsavel = usuario.id_usuario " +
							 "WHERE tarefa.id_tarefa = [taskID]";
				sql = sql.replace("[taskID]", taskID);
				
				Statement stm = this.conection.createStatement();
				ResultSet rs = stm.executeQuery(sql);
				
				if(rs.next()){
					
					String login = rs.getString("login");
					Developer developer = null;
					if(login != null && !login.isEmpty()){
						developer = developerDAO.findByCodeRepositoryUsername(login);
						if(developer == null){
							developer = new Developer();
							developer.setCodeRepositoryUsername(login);
							developerDAO.save(developer);
						}
					}
					
					Long tarefaID = rs.getLong("id_tarefa");
					Task task = taskDAO.findByID(tarefaID);
					if(task == null){
						task = new Task();
						task.setId(tarefaID);
						task.setTitle(rs.getString("titulo"));
						//TaskPriority taskPriority = new TaskPriority(null, rs.getInt("prioridade"));
						//task.setPriority(taskPriority);
						task.setCreatedBy(developer);
						taskDAO.save(task);
					}
					return task;
				}else{
					throw new TaskNotFoundException("Task not found by informed id #"+taskID);
				}
			} catch (ArrayIndexOutOfBoundsException e){
				System.out.println("Impossible to recover TaskID from commit comment");
			} catch (NumberFormatException e){
				System.out.println("Impossible to recover TaskID from commit comment");
			} catch (TaskNotFoundException e){
				System.out.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public CodeRepository getCodeRepository() {
		return codeRepository;
	}
}
