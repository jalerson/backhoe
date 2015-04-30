package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskPriority extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private Integer level;
	@OneToMany(mappedBy = "priority")
	private List<Task> tasks;
	
	public TaskPriority(){}
	
	public TaskPriority(String name, Integer level){
		this(name, level, null);
	}
	
	public TaskPriority(String name, Integer level, List<Task> tasks) {
		super();
		this.name = name;
		this.level = level;
		this.tasks = tasks;
	}
	
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
}
