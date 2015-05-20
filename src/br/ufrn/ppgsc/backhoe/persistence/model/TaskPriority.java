package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskPriority implements Model {
	
	@Id
	private Long id;
	private String name;
//	private Integer level;
	@OneToMany(mappedBy = "priority")
	private List<Task> tasks;
	
	public TaskPriority(){
		this(null, null);
	}
	
	public TaskPriority(Long id, String name) {
		this(id, name, new LinkedList<Task>());
	}
	
	public TaskPriority(Long id, String name, List<Task> tasks) {
		super();
		this.id = id;
		this.name = name;
		this.tasks = tasks;
	}
	
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		return "TaskPriority [id=" + id + ", name=" + name + "]";
	}
}
