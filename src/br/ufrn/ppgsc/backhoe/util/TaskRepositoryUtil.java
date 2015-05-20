package br.ufrn.ppgsc.backhoe.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.TaskRepositoryException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractProjectDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskLogTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskPriorityDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskStatusDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractTaskTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Project;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLogType;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskPriority;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskStatus;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskType;
import br.ufrn.ppgsc.backhoe.repository.task.TaskRepository;

public class TaskRepositoryUtil {
	
	private static AbstractTaskStatusDAO taskStatusDAO;
	private static AbstractTaskPriorityDAO taskPriorityDAO;
	private static AbstractTaskTypeDAO taskTypeDAO;
	private static AbstractProjectDAO projectDAO;
	private static AbstractTaskLogTypeDAO taskLogTypeDAO;
	private static boolean configuredDAO = false;
	
	private static boolean configureDAO(){
		if(!configuredDAO){
			try {
				taskStatusDAO = (AbstractTaskStatusDAO) DAOFactory.createDAO(DAOType.TASK_STATUS);
				taskPriorityDAO = (AbstractTaskPriorityDAO) DAOFactory.createDAO(DAOType.TASK_PRIORITY);
				taskTypeDAO = (AbstractTaskTypeDAO) DAOFactory.createDAO(DAOType.TASK_TYPE);
				projectDAO = (AbstractProjectDAO) DAOFactory.createDAO(DAOType.PROJECT);
				taskLogTypeDAO = (AbstractTaskLogTypeDAO) DAOFactory.createDAO(DAOType.TASK_LOG_TYPE);
				configuredDAO = true;
				return configuredDAO;
			} catch (DAONotFoundException e) {
				e.printStackTrace();
			}
		}
		return configuredDAO;
	}
	
	public static void populateSampleTablesFromTaskRepository(TaskRepository taskRepository){
		Connection connection = taskRepository.getConnection();
		if(!configureDAO() || connection == null)
			throw new TaskRepositoryException("DAO not configured or TaskRepository not connected");
		populateTaskStatusFromIproject(connection);
		populateTaskPriorityFromIproject(connection);
		populateTaskTypeFromIproject(connection);
		populateProjectFromIproject(connection);
		populateTaskLogTypeFromIproject(connection);
	}
	
	private static void populateTaskStatusFromIproject(Connection connection){
		String taskStatusSQL = "SELECT id, denominacao FROM iproject.status_tarefa";
		try {
			Statement stm = connection.createStatement();
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
	
	private static void populateTaskPriorityFromIproject(Connection connection){
		String taskPrioritySQL = "SELECT id_prioridade, denominacao FROM iproject.prioridade";
		try {
			Statement stm = connection.createStatement();
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
	
	private static void populateTaskTypeFromIproject(Connection connection){
		String taskTypeSQL = "SELECT id_tipo_tarefa, denominacao FROM iproject.tipo_tarefa";
		try {
			Statement stm = connection.createStatement();
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
	
	private static void populateProjectFromIproject(Connection connection){
		String projectSQL = "SELECT id_projeto, nome FROM iproject.projeto";
		try {
			Statement stm = connection.createStatement();
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
	
	private static void populateTaskLogTypeFromIproject(Connection connection){
		String projectSQL = "SELECT id_tipo_log, denominacao FROM iproject.tipo_log";
		try {
			Statement stm = connection.createStatement();
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

}
