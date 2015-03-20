package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity
public class Task extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String title;
	private Date createdAt;
	@Type(type="text")
	private String description;
	@ManyToOne
	private TaskType type;
	@ManyToOne
	private TaskPriority priority;
	@ManyToOne
	private TaskStatus status;
	@ManyToOne
	private Project project;
	@ManyToOne
	private Developer createdBy;
	
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
}
