package br.ufrn.ppgsc.backhoe.repository.code.gitAPI;

import java.util.Date;
import java.util.List;

public class GITLogEntry {
	
	private String revision;
	private String comment;
	private Date createdAt;
	private String branch;
	private String authorEmail;
	private List<GITLogChange> changedPaths;
	
	public GITLogEntry(String revision, String comment, Date createdAt,
			String branch, String authorEmail) {
		super();
		this.revision = revision;
		this.comment = comment;
		this.createdAt = createdAt;
		this.branch = branch;
		this.authorEmail = authorEmail;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
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

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public List<GITLogChange> getChangedPaths() {
		return changedPaths;
	}

	public void setChangedPaths(List<GITLogChange> changedPaths) {
		this.changedPaths = changedPaths;
	}

	@Override
	public String toString() {
		return "GITLogEntry [revision=" + revision + ", comment=" + comment
				+ ", createdAt=" + createdAt + ", branch=" + branch
				+ ", authorEmail=" + authorEmail + "]";
	}
}
