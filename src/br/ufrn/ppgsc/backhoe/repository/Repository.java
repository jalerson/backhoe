package br.ufrn.ppgsc.backhoe.repository;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public interface Repository {
	boolean connect() throws MissingParameterException;
	String getUsername();
	void setUsername(String username);
	String getPassword();
	void setPassword(String password);
	String getURL();
	void setURL(String url);
}
