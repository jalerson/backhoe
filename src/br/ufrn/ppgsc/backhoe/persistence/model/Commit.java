package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


@Entity
public class Commit implements Model {
	
	@Id
	private Long revision;
	private String comment;
	private Date createdAt;
	
	private String branch;
	@ManyToOne
	private Developer author;
	@ManyToOne
	private TaskLog log;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "commit", cascade=CascadeType.ALL)
	private List<ChangedPath> changedPaths;
	
	public Commit(){
		this.changedPaths = new LinkedList<ChangedPath>();
	}
	
	public Commit(Long revision, String comment, Date createdAt, String branch,
			Developer author, TaskLog log) {
		this(revision, comment, createdAt, branch, author, log, new LinkedList<ChangedPath>());
	}

	public Commit(Long revision, String comment, Date createdAt, String branch,
			Developer author, TaskLog log, List<ChangedPath> changedPaths) {
		super();
		this.revision = revision;
		this.comment = comment;
		this.createdAt = createdAt;
		this.branch = branch;
		this.author = author;
		this.log = log;
		this.changedPaths = changedPaths;
	}
	
	public List<ChangedPath> getChangedPaths() {
		return changedPaths;
	}
	public void setChangedPaths(List<ChangedPath> changedPaths) {
		this.changedPaths = changedPaths;
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
	@Override
	public String toString() {
		return "Commit [revision=" + revision + ", author=" + author + ", log="
				+ log + "]";
	}

	@Override
	public Long getId() {
		return this.revision;
	}

	@Override
	public void setId(Long id) {
		this.revision = id;
	}
}
