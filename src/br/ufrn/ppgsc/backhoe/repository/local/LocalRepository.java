package br.ufrn.ppgsc.backhoe.repository.local;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;
import br.ufrn.ppgsc.backhoe.repository.Repository;

public class LocalRepository extends AbstractRepository implements Repository {
	
	private Connection conection;
	
	public LocalRepository(){
		
	}
	
	public LocalRepository(String username, String password, String url) {
		super(username, password, url);
	}

	@Override
	public boolean connect() throws MissingParameterException {

		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if(username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if(password == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.conection = DriverManager.getConnection(url, username, password);
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Connection getConection() {
		return conection;
	}
}
