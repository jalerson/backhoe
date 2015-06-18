package br.ufrn.ppgsc.backhoe.formatter;

import java.io.File;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;

public interface Formatter {
	
	boolean setup() throws MissingParameterException; 
	File format();

}
