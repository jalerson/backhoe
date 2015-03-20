package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public abstract class Repository {
	public abstract boolean connect() throws MissingParameterException;
	public abstract String getUsername();
	public abstract void setUsername(String username);
	public abstract String getPassword();
	public abstract void setPassword(String password);
	public abstract String getURL();
	public abstract void setURL(String url);
}
