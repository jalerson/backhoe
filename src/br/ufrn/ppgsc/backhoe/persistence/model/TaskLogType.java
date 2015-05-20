package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskLogType implements Model {
	
	@Id
	private Long id;
	private String name;
	@OneToMany(mappedBy = "type")
	private List<TaskLog> logs;
	
	public TaskLogType(){
		this(null, null);
	}
	
	public TaskLogType(Long id, String name) {
		this(id, name, new LinkedList<TaskLog>());
	}
	
	public TaskLogType(Long id, String name, List<TaskLog> logs) {
		super();
		this.id = id;
		this.name = name;
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
	public List<TaskLog> getLogs() {
		return logs;
	}
	public void setLogs(List<TaskLog> logs) {
		this.logs = logs;
	}
}
