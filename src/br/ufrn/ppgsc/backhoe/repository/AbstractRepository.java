package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public abstract class AbstractRepository implements Repository {
	
	protected String username;
	protected String password;
	protected String url;
	
	public AbstractRepository(){}

	public AbstractRepository(String username, String password, String url) {
		this.username = username;
		this.password = password;
		this.url = url;
	}
	
	public abstract boolean connect() throws MissingParameterException;
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}
	
	protected String preparePlaceholders(long[] values) {
		String sql = "(";
		for (int i = 0; i < values.length; i++){
			sql += values[i];
			if (i != (values.length - 1))
				sql += ", ";
		}
		sql += ")";
		return sql;
	}
}
