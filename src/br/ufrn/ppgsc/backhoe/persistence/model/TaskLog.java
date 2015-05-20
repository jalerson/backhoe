package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

@Entity
public class TaskLog implements Model {
	
	@Id
	private Long id;
	@Type(type="text")
	private String description;
	private Date createdAt;
	@ManyToOne
	private Developer author;
	@ManyToOne
	private Task task;
	@ManyToOne
	private TaskLogType type;
	@OneToMany(mappedBy = "log")
	private List<Commit> commits;
	
	public TaskLog(){
		this.commits = new LinkedList<Commit>();
	}
	
	public TaskLog(Long id, String description, Date createdAt,
			Developer author, Task task, TaskLogType type) {
		this(id, description, createdAt, author, task, type, new LinkedList<Commit>());
	}

	public TaskLog(Long id, String description, Date createdAt,
			Developer author, Task task, TaskLogType type, List<Commit> commits) {
		super();
		this.id = id;
		this.description = description;
		this.createdAt = createdAt;
		this.author = author;
		this.task = task;
		this.type = type;
		this.commits = commits;
	}

	public List<Commit> getCommits() {
		return commits;
	}
	public void setCommits(List<Commit> commits) {
		this.commits = commits;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Developer getAuthor() {
		return author;
	}
	public void setAuthor(Developer author) {
		this.author = author;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public TaskLogType getType() {
		return type;
	}
	public void setType(TaskLogType type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "TaskLog [id=" + id + ", createdAt=" + createdAt + ", author="
				+ author + ", task=" + task + "]";
	}
}