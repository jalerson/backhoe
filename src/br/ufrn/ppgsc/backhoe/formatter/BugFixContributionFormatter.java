package br.ufrn.ppgsc.backhoe.formatter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;

public class BugFixContributionFormatter extends AbstractFormatter {

	public BugFixContributionFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super(startDate, endDate, developers, localRepository, minerName, systemName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void format() {
		String contentFile = generateContentFileCSV(startDate, endDate, developers);
		String fileName = systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate()+"_BugFixContribution.csv";
		String filePathAndName = getDirPath()+ "/" + fileName;
		createCSV(contentFile, filePathAndName);
	}
	
	public String generateContentFileCSV(Date startDateInterval, Date endDateInterval, List<String> developers){
		String sql = "SELECT d.codeRepositoryUsername, m.value from Metric m, Developer d " +
					 "where m.objectId = d.id and " +
					 "m.startDateInterval = ':startDateInterval' and "+
					 "m.endDateInterval = ':endDateInterval' and " +
					 "d.id in (select id from Developer where codeRepositoryUsername in :developerLogins);";
		sql = sql.replace(":startDateInterval", startDateInterval.toString());
		sql = sql.replace(":endDateInterval", endDateInterval.toString());
		sql = sql.replace(":developerLogins", sqlListFormatter(developers));
		
		Map<String, Integer> bugFixMAP = new HashMap<String, Integer>();
		
		for (String developer : developers)
			bugFixMAP.put(developer, 0);
		
		try {
			ResultSet rs = localRepository.getConection().createStatement().executeQuery(sql);
			int bugsFixedTotal = 0;
		  
			while (rs.next()){
				bugFixMAP.put(rs.getString("codeRepositoryUsername"), rs.getInt("value"));
				bugsFixedTotal += rs.getInt("value");
			}
			
			String str = "DEVELOPER;BUGFIXES;BUGFIXES(%)\n";
			for (int i = 0; i < developers.size(); i++) {
				String codeRepositoryUsername = developers.get(i);
				Integer value = bugFixMAP.get(codeRepositoryUsername);
				Float percent = (float) value / (float) bugsFixedTotal;
				str += "\""+developers.get(i)+"\";"; // developerUsername
				str += "\""+value+"\";"; 
				str += "\""+percent+"\";";
				str += "\n";
			}
			return str;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
