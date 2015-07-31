package br.ufrn.ppgsc.backhoe.persistence.model.helper;

import java.util.LinkedList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;

public class Diff {
	
//	private TaskLog log;
	private Task task;
	private String fixingRevision;
	private String previousRevision;
	private String startRevision;
	private ChangedPath changedPath;
	private List<DiffChild> children;
	
	public Diff(){
		this.children = new LinkedList<DiffChild>();
	}
	
	public Diff(Task task, String fixingRevision, String previousRevision,
			String startRevision, ChangedPath changedPath) {
		this(task, fixingRevision, previousRevision, startRevision, changedPath, new LinkedList<DiffChild>());
	}
	
	public Diff(Task task, String fixingRevision, String previousRevision,
			String startRevision, ChangedPath changedPath,
			List<DiffChild> children) {
		super();
		this.task = task;
		this.fixingRevision = fixingRevision;
		this.previousRevision = previousRevision;
		this.startRevision = startRevision;
		this.changedPath = changedPath;
		this.children = children;
	}

	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public String getFixingRevision() {
		return fixingRevision;
	}
	public void setFixingRevision(String fixingRevision) {
		this.fixingRevision = fixingRevision;
	}
	public String getPreviousRevision() {
		return previousRevision;
	}
	public void setPreviousRevision(String previousRevision) {
		this.previousRevision = previousRevision;
	}
	public String getStartRevision() {
		return startRevision;
	}
	public void setStartRevision(String startRevision) {
		this.startRevision = startRevision;
	}
	public ChangedPath getChangedPath() {
		return changedPath;
	}
	public void setChangedPath(ChangedPath changedPath) {
		this.changedPath = changedPath;
	}
	public List<DiffChild> getChildren() {
		return children;
	}
	public void setChildren(List<DiffChild> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Diff [fixingRevision=" + fixingRevision + ", previousRevision="
				+ previousRevision + ", startRevision=" + startRevision
				+ ", changedPath=" + changedPath + ", children=" + children
				+ "]";
	}
}
