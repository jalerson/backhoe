package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskLogType extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	@OneToMany(mappedBy = "type")
	private List<TaskLog> logs;
	
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
