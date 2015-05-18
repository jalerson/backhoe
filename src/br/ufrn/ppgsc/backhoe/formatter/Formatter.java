package br.ufrn.ppgsc.backhoe.formatter;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public interface Formatter {
	
	boolean setup() throws MissingParameterException; 
	void format();

}
