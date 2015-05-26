package br.ufrn.ppgsc.backhoe.persistence.model.helper;

import java.util.Date;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;

public class Blame {

//	private TaskLog log;
	private Task task;
	private long initialRevision;
	private long lastRevision;
	private String author;
	private Date date;
	private String line;
	private long revision;
	private ChangedPath changedPath;
	private long lineNumber;
	
	public Blame(){}
	
	public Blame(Task task, long initialRevision, long lastRevision,
			String author, Date date, String line, long revision,
			ChangedPath changedPath, long lineNumber) {
		super();
		this.task = task;
		this.initialRevision = initialRevision;
		this.lastRevision = lastRevision;
		this.author = author;
		this.date = date;
		this.line = line;
		this.revision = revision;
		this.changedPath = changedPath;
		this.lineNumber = lineNumber;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public long getInitialRevision() {
		return initialRevision;
	}
	public void setInitialRevision(long initialRevision) {
		this.initialRevision = initialRevision;
	}
	public long getLastRevision() {
		return lastRevision;
	}
	public void setLastRevision(long lastRevision) {
		this.lastRevision = lastRevision;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public long getRevision() {
		return revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public ChangedPath getChangedPath() {
		return changedPath;
	}
	public void setChangedPath(ChangedPath changedPath) {
		this.changedPath = changedPath;
	}
	public long getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}
}
