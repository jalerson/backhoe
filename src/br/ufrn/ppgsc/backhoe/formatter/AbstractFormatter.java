package br.ufrn.ppgsc.backhoe.formatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;

public abstract class AbstractFormatter implements Formatter {
	
	protected Date startDate;
	protected Date endDate;
	protected List<String> developers;
	protected LocalRepository localRepository;
	protected String minerName;
	protected String systemName;

	public AbstractFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.developers = developers;
		this.localRepository = localRepository;
		this.minerName = minerName;
		this.systemName = systemName;
	}

	public boolean setup(){
		if(localRepository == null)
			throw new MissingParameterException("Missing mandatory parameter: LocalRepository localRepository");
		return localRepository.connect();
	}
	
	protected static String sqlListFormatter(List<String> list){
		String str = "(";
		for(String element: list)
			str += "'"+element+"', ";
		return str.substring(0, str.length()-2)+")";
	}
	
	protected File createCSV(String content, String fileName){
		File csvFile = new File(fileName);
		try {
			csvFile.createNewFile();
			FileOutputStream output = new FileOutputStream(csvFile);
			output.write(content.getBytes());
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return csvFile;
	}
	
	public String getDirPath(){
		String dirPath = getNameDirPath();
		File theDir = new File(dirPath);

		if (!theDir.exists()) {
		    try{
		        theDir.mkdirs();
		    } 
		    catch(SecurityException se){}
		}
		return dirPath;
	}
	
	private String getNameDirPath() {
		return "results/"+systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate();
	}
	
	public String getHumanizedStartDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy");
		return dateFormat.format(startDate);
	}

	public String getHumanizedEndDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy");
		return dateFormat.format(endDate);
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<String> getDevelopers() {
		return developers;
	}

	public void setDevelopers(List<String> developers) {
		this.developers = developers;
	}

	public LocalRepository getLocalRepository() {
		return localRepository;
	}

	public void setLocalRepository(LocalRepository localRepository) {
		this.localRepository = localRepository;
	}
}
