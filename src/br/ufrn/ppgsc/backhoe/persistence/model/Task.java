package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

@Entity
public class Task implements Model {
	
	@Id
	private Long id;
	private String title;
	private Date createdAt;
	@Type(type="text")
	private String description;
	@ManyToOne
	@JoinColumn(name = "taskType_id")
	private TaskType type;
	@ManyToOne
	@JoinColumn(name = "taskPriority_id")
	private TaskPriority priority;
	@ManyToOne
	@JoinColumn(name = "taskStatus_id")
	private TaskStatus status;
	@ManyToOne
	private Project project;
	@ManyToOne
	private Developer createdBy;
	@OneToMany(mappedBy = "task")
	private List<TaskLog> taskLogs;
	
	public Task(){
		this(null, null);
	}
	
	public Task(Long id, String title) {
		super();
		this.id = id;
		this.title = title;
		this.taskLogs = new LinkedList<TaskLog>();
	}
	
	public Task(Long id, String title, Date createdAt, String description,
			TaskType type, TaskPriority priority, TaskStatus status,
			Project project, Developer createdBy){
		this(id, title, createdAt, description, type, priority, 
			 status, project, createdBy, new LinkedList<TaskLog>());
	}

	public Task(Long id, String title, Date createdAt, String description,
			TaskType type, TaskPriority priority, TaskStatus status,
			Project project, Developer createdBy, List<TaskLog> taskLogs) {
		super();
		this.id = id;
		this.title = title;
		this.createdAt = createdAt;
		this.description = description;
		this.type = type;
		this.priority = priority;
		this.status = status;
		this.project = project;
		this.createdBy = createdBy;
		this.taskLogs = taskLogs;
	}
	
	public Developer getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Developer createdBy) {
		this.createdBy = createdBy;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public TaskStatus getStatus() {
		return status;
	}
	public void setStatus(TaskStatus status) {
		this.status = status;
	}
	public TaskType getType() {
		return type;
	}
	public void setType(TaskType type) {
		this.type = type;
	}
	public TaskPriority getPriority() {
		return priority;
	}
	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<TaskLog> getTaskLogs() {
		return taskLogs;
	}
	public void setTaskLogs(List<TaskLog> taskLogs) {
		this.taskLogs = taskLogs;
	}
	@Override
	public String toString() {
		return "Task [id=" + id + ", title=" + title + ", createdBy="
				+ createdBy + "]";
	}
}
