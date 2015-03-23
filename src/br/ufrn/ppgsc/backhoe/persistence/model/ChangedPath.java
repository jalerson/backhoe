package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

@Entity
public class ChangedPath extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String path;
	private Character changeType;
	@ManyToOne
	private Commit commit;
	@OneToMany(mappedBy = "changedPath")
	private List<ChangedLine> changedLines;
	@Type(type="text")
	private String content;
	
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
}
