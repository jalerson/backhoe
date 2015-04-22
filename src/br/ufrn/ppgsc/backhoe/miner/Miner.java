package br.ufrn.ppgsc.backhoe.miner;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public interface Miner {
	
	boolean setup() throws MissingParameterException; 
	public void execute();
	
}
