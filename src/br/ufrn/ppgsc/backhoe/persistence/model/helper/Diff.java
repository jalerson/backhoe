package br.ufrn.ppgsc.backhoe.persistence.model.helper;

import java.util.LinkedList;
import java.util.List;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Task;

public class Diff {
	
//	private TaskLog log;
	private Task task;
	private long fixingRevision;
	private long previousRevision;
	private long startRevision;
	private ChangedPath changedPath;
	private List<DiffChild> children;
	
	public Diff(){
		this.children = new LinkedList<DiffChild>();
	}
	
	public Diff(Task task, long fixingRevision, long previousRevision,
			long startRevision, ChangedPath changedPath) {
		this(task, fixingRevision, previousRevision, startRevision, changedPath, new LinkedList<DiffChild>());
	}
	
	public Diff(Task task, long fixingRevision, long previousRevision,
			long startRevision, ChangedPath changedPath,
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
	public long getFixingRevision() {
		return fixingRevision;
	}
	public void setFixingRevision(long fixingRevision) {
		this.fixingRevision = fixingRevision;
	}
	public long getPreviousRevision() {
		return previousRevision;
	}
	public void setPreviousRevision(long previousRevision) {
		this.previousRevision = previousRevision;
	}
	public long getStartRevision() {
		return startRevision;
	}
	public void setStartRevision(long startRevision) {
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
}
