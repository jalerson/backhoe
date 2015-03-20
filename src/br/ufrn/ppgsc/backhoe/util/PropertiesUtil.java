package br.ufrn.ppgsc.backhoe.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
	
	public static Properties getProperties(String filename) {
		Properties properties = new Properties();
		FileInputStream file;
		try {
			file = new FileInputStream(filename);
			properties.load(file);
			return properties;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
