package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Commit extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String comment;
	private Date createdAt;
	private Long revision;
	private String branch;
	@ManyToOne
	private Developer author;
	@ManyToOne
	private TaskLog log;
	@OneToMany(mappedBy = "commit")
	private List<ChangedPath> changedPaths;
	
	public List<ChangedPath> getChangedPaths() {
		return changedPaths;
	}
	public void setChangedPaths(List<ChangedPath> changedPaths) {
		this.changedPaths = changedPaths;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Long getRevision() {
		return revision;
	}
	public void setRevision(Long revision) {
		this.revision = revision;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public Developer getAuthor() {
		return author;
	}
	public void setAuthor(Developer author) {
		this.author = author;
	}
	public TaskLog getLog() {
		return log;
	}
	public void setLog(TaskLog log) {
		this.log = log;
	}
}
