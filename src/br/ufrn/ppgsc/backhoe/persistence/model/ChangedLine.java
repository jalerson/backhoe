package br.ufrn.ppgsc.backhoe.persistence.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ChangedLine extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String originalLine;
	private String changedLine;
	private Character changeType;
	@ManyToOne
	private ChangedPath changedPath;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOriginalLine() {
		return originalLine;
	}
	public void setOriginalLine(String originalLine) {
		this.originalLine = originalLine;
	}
	public String getChangedLine() {
		return changedLine;
	}
	public void setChangedLine(String changedLine) {
		this.changedLine = changedLine;
	}
	public Character getChangeType() {
		return changeType;
	}
	public void setChangeType(Character changeType) {
		this.changeType = changeType;
	}
	public ChangedPath getChangedPath() {
		return changedPath;
	}
	public void setChangedPath(ChangedPath changedPath) {
		this.changedPath = changedPath;
	}
}
