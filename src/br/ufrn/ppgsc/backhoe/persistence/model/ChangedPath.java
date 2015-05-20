package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

@Entity
public class ChangedPath implements Model {
	@Id
	@GeneratedValue
	private Long id;
	private String path;
	private Character changeType;
	@ManyToOne
	@JoinColumn(name="commit_id")
	private Commit commit;
	@OneToMany(mappedBy = "changedPath")
	private List<ChangedLine> changedLines;
	@Type(type="text")
	private String content;
	
	public ChangedPath(){
		this.changedLines = new LinkedList<ChangedLine>();
	}
	
	public ChangedPath(String path, Character changeType, Commit commit,
			String content) {
		super();
		this.path = path;
		this.changeType = changeType;
		this.commit = commit;
		this.content = content;
		this.changedLines = new LinkedList<ChangedLine>();
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Character getChangeType() {
		return changeType;
	}
	public void setChangeType(Character changeType) {
		this.changeType = changeType;
	}
	public Commit getCommit() {
		return commit;
	}
	public void setCommit(Commit commit) {
		this.commit = commit;
	}
	public List<ChangedLine> getChangedLines() {
		return changedLines;
	}
	public void setChangedLines(List<ChangedLine> changedLines) {
		this.changedLines = changedLines;
	}
	@Override
	public String toString() {
		return "ChangedPath [id=" + id + ", changeType="
				+ changeType + ", commitID=" + commit.getRevision() + "]";
	}
}
