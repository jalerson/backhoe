package br.ufrn.ppgsc.backhoe.formatter;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;

public class CodeContributionFormatter extends AbstractFormatter {

	public CodeContributionFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super(startDate, endDate, developers, localRepository, minerName, systemName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public File format() {
		String contentFile = generateContentFileCSV(startDate, endDate, developers);
		String fileName = systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate()+"_CodeContribution.csv";
		String filePathAndName = getDirPath()+ "/" + fileName;
		return createCSV(contentFile, filePathAndName);
	}
	
	public String generateContentFileCSV(Date startDateInterval, Date endDateInterval, List<String> developers){
		String sql = "select d.codeRepositoryUsername, d.email, mt.slug, sum(m.value) as sumValue " +
					 "from Developer d, Commit c, ChangedPath cp, Metric m, MetricType mt "+
					 "where m.objectType = 'ChangedPath' " +
					 " and m.minerSlug = 'codeContributionMiner' " +
					 " and m.objectId = cp.id " +
					 " and m.type_id = mt.id " +
					 " and c.author_id = d.id " +
					 " and cp.commit_id = c.revision "+
					 " and c.revision in (select distinct revision from Commit " + 
								   		 "where author_id in (select id from Developer "+
													   		 "where codeRepositoryUsername in "+ sqlListFormatter(developers) +") " +
										    "or author_id in (select id from Developer "+
									   		 				 "where email in "+ sqlListFormatter(developers) +") " +
										 "and createdAt between ':startDateInterval' and ':endDateInterval') " +
					"group by d.codeRepositoryUsername, d.email, mt.slug";
		sql = sql.replace(":startDateInterval", startDateInterval.toString());
		sql = sql.replace(":endDateInterval", endDateInterval.toString());
		try {
			ResultSet rs = localRepository.getConection().createStatement().executeQuery(sql);
			
			// ADDED LOC, CHANGED LOC, REMOVED LOC,	ADDED METHODS, CHANGED METHODS
			
			Hashtable<String, List<Integer>>contributions = new Hashtable<String, List<Integer>>();
			ArrayList<Integer> totalContributions = new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0,0,0,0}));
			ArrayList<Float> avgContributions = new ArrayList<Float>(Arrays.asList(new Float[]{0f,0f,0f,0f,0f}));
			
			DecimalFormat decimalFormat = new DecimalFormat("0.0000");
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator(',');
			decimalFormat.setDecimalFormatSymbols(symbols);
			
			for (String developer : developers) {
				contributions.put(developer, new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0,0,0,0})));
			}
			
			Map<String, Integer> metricToArrayPosition = new HashMap<String, Integer>();
			metricToArrayPosition.put("loc:added", 0);
			metricToArrayPosition.put("loc:changed", 1);
			metricToArrayPosition.put("loc:deleted", 2);
			metricToArrayPosition.put("methods:added", 3);
			metricToArrayPosition.put("methods:changed", 4);
			
			while (rs.next()){
				
				String rowName = null;
				if(rs.getString("codeRepositoryUsername")!= null && contributions.containsKey(rs.getString("codeRepositoryUsername"))){
					rowName = "codeRepositoryUsername";
				}else if(rs.getString("email")!= null && contributions.containsKey(rs.getString("email"))){
					rowName = "email";
				}
				
				ArrayList<Integer> developerContribution = (ArrayList<Integer>) contributions.get(rs.getString(rowName));
				developerContribution.set(metricToArrayPosition.get(rs.getString("slug")), rs.getInt("sumValue"));
				contributions.put(rs.getString(rowName), developerContribution);
				
				totalContributions.set(metricToArrayPosition.get(rs.getString("slug")), totalContributions.get(metricToArrayPosition.get(rs.getString("slug"))) + 
																	  rs.getInt("sumValue"));
			}
			
			for(int i = 0; i < totalContributions.size(); i++) {
				avgContributions.set(i, (float) totalContributions.get(i) / developers.size());
			}
			
			String str = "DEVELOPER;ADDED LOC;CHANGED LOC;REMOVED LOC;ADDED METHODS;CHANGED METHODS\n";
			for (int i = 0; i < developers.size(); i++) {
				List<Integer> contribution = contributions.get(developers.get(i));
				str += "\""+developers.get(i)+"\";"; // developer login
				for(int j = 0; j < contribution.size(); j++) {
					str += "\""+decimalFormat.format(contribution.get(j))+"\";"; // developer contribution
				}
				str += "\n";
			}
			return str;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
