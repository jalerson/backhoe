package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Developer extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private String email;
	private String codeRepositoryUsername;
	private String taskRepositoryUsername;
	@OneToMany(mappedBy = "createdBy")
	private List<Task> tasks;
	@OneToMany(mappedBy = "author")
	private List<TaskLog> logs;
	@OneToMany(mappedBy = "author")
	private List<Commit> commits;
	
	public Developer(){
		this(null, null, null);
	}
	
	public Developer(String name, String email, String codeRepositoryUsername){
		this(name, email, codeRepositoryUsername, null);
	}
	
	public Developer(String name, String email, String codeRepositoryUsername, String taskRepositoryUsername){
		this(name, email, codeRepositoryUsername, taskRepositoryUsername,
			 new LinkedList<Task>(),
			 new LinkedList<TaskLog>(),
			 new LinkedList<Commit>());
	}
	
	public Developer(String name, String email, String codeRepositoryUsername,
			String taskRepositoryUsername, List<Task> tasks,
			List<TaskLog> logs, List<Commit> commits) {
		super();
		this.name = name;
		this.email = email;
		this.codeRepositoryUsername = codeRepositoryUsername;
		this.taskRepositoryUsername = taskRepositoryUsername;
		this.tasks = tasks;
		this.logs = logs;
		this.commits = commits;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Developer) {
			Developer developer = (Developer) obj;
			if(developer.getId() != null && this.id != null && developer.getId() == this.id) {
				return true;
			} else {
				if(developer.getCodeRepositoryUsername() != null && this.codeRepositoryUsername != null && developer.getCodeRepositoryUsername() == this.codeRepositoryUsername) {
					return true;
				} else {
					if(developer.getTaskRepositoryUsername() != null && this.taskRepositoryUsername != null && developer.getTaskRepositoryUsername() == this.taskRepositoryUsername) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public String getCodeRepositoryUsername() {
		return codeRepositoryUsername;
	}
	public void setCodeRepositoryUsername(String codeRepositoryUsername) {
		this.codeRepositoryUsername = codeRepositoryUsername;
	}
	public String getTaskRepositoryUsername() {
		return taskRepositoryUsername;
	}
	public void setTaskRepositoryUsername(String taskRepositoryUsername) {
		this.taskRepositoryUsername = taskRepositoryUsername;
	}
	public List<Commit> getCommits() {
		return commits;
	}
	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}
	public List<TaskLog> getLogs() {
		return logs;
	}
	public void setLogs(List<TaskLog> logs) {
		this.logs = logs;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		return "Developer [id=" + id + ", codeRepositoryUsername="
				+ codeRepositoryUsername + "]";
	}	
}
