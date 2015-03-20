package br.ufrn.ppgsc.backhoe.miner;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public abstract class Miner {
	public abstract boolean setup() throws MissingParameterException;
	public abstract void execute();
}
