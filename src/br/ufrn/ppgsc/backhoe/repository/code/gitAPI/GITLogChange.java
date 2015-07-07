package br.ufrn.ppgsc.backhoe.repository.code.gitAPI;


public class GITLogChange {

	private String path;
	private Character changeType;
	private String content;
	
	public GITLogChange(String path, Character changeType, String content) {
		super();
		this.path = path;
		this.changeType = changeType;
		this.content = content;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "GITLogChange [path=" + path + ", changeType=" + changeType + "]";
	}
}
