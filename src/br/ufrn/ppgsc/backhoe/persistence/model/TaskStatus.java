package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class TaskStatus extends Model {
	
	@Id
	private Long id;
	private String name;
	@OneToMany(mappedBy = "status")
	private List<Task> tasks;
	
	public TaskStatus(){
		this(null, null);
	}
	
	public TaskStatus(Long id, String name) {
		this(id, name, new LinkedList<Task>());
	}
	
	public TaskStatus(Long id, String name, List<Task> tasks) {
		super();
		this.id = id;
		this.name = name;
		this.tasks = tasks;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
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

	@Override
	public String toString() {
		return "TaskStatus [id=" + id + ", name=" + name + "]";
	}
}
