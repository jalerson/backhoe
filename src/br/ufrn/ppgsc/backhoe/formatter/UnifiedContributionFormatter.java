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

public class UnifiedContributionFormatter extends AbstractFormatter {

	public UnifiedContributionFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super(startDate, endDate, developers, localRepository, minerName, systemName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public File format() {
		System.out.println("\nFormating file with UnifiedContributionMetrics...");
		String contentFile = generateContentFileCSV(startDate, endDate, developers);
		String fileName = systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate()+"_UnifiedContributionMetrics.csv";
		String filePathAndName = getDirPath()+ "/" + fileName;
		System.out.println("UnifiedContributionMetrics formated!\n");
		return createCSV(contentFile, filePathAndName);
	}
	
	public void codeContributionAndCodeCompexityFormatter(Date startDateInterval, Date endDateInterval, 
														  List<String> developers, Hashtable<String, List<Integer>>contributions,
														  ArrayList<Integer> totalContributions,
														  Map<String, Integer> metricToArrayPosition){
		
		String sql = "select d.codeRepositoryUsername, mt.slug, sum(m.value) as sumValue " +
					 "from Developer d, Commit c, ChangedPath cp, Metric m, MetricType mt "+
					 "where m.objectType = 'ChangedPath' " +
					 " and m.minerSlug in ('codeContributionMiner', 'codeComplexityMiner') " +
					 " and m.objectId = cp.id " +
					 " and m.type_id = mt.id " +
					 " and c.author_id = d.id " +
					 " and cp.commit_id = c.revision "+
					 " and c.revision in (select distinct revision from Commit " + 
								   		 "where author_id in (select id from Developer "+
													   		 "where codeRepositoryUsername in "+ sqlListFormatter(developers) +") " +
										 "and createdAt between ':startDateInterval' and ':endDateInterval') " +
					"group by d.codeRepositoryUsername, mt.name";
		sql = sql.replace(":startDateInterval", startDateInterval.toString());
		sql = sql.replace(":endDateInterval", endDateInterval.toString());
		
		try {
			ResultSet rs = localRepository.getConection().createStatement().executeQuery(sql);
			
			while (rs.next()){
				ArrayList<Integer> developerContribution = (ArrayList<Integer>) contributions.get(rs.getString("codeRepositoryUsername"));
				developerContribution.set(metricToArrayPosition.get(rs.getString("slug")), rs.getInt("sumValue"));
				contributions.put(rs.getString("codeRepositoryUsername"), developerContribution);
				
				totalContributions.set(metricToArrayPosition.get(rs.getString("slug")), totalContributions.get(metricToArrayPosition.get(rs.getString("slug"))) + 
																	  rs.getInt("sumValue"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void bugFixBuggyCommitAndFailedTestsFormatter(Date startDateInterval, Date endDateInterval, 
			  List<String> developers, Hashtable<String, List<Integer>>contributions,
			  ArrayList<Integer> totalContributions,
			  Map<String, Integer> metricToArrayPosition){
		
		String sql = "SELECT d.codeRepositoryUsername, mt.slug, m.value from Metric m, Developer d, MetricType mt " +
					 "where m.objectId = d.id " +
					 "  and m.type_id = mt.id " +
					 "  and m.minerSlug in ('bugFixContributionMiner', 'buggyCommitMiner', 'failedTestsMiner') " + 
					 "  and m.startDateInterval = ':startDateInterval' " +
					 "  and m.endDateInterval = ':endDateInterval' " +
					 "  and d.id in (select id from Developer where codeRepositoryUsername in :developerLogins);";
		
		sql = sql.replace(":startDateInterval", startDateInterval.toString());
		sql = sql.replace(":endDateInterval", endDateInterval.toString());
		sql = sql.replace(":developerLogins", sqlListFormatter(developers));
		
		try {
			ResultSet rs = localRepository.getConection().createStatement().executeQuery(sql);
			
			while (rs.next()){
				ArrayList<Integer> developerContribution = (ArrayList<Integer>) contributions.get(rs.getString("codeRepositoryUsername"));
				developerContribution.set(metricToArrayPosition.get(rs.getString("slug")), rs.getInt("value"));
				contributions.put(rs.getString("codeRepositoryUsername"), developerContribution);
				
				totalContributions.set(metricToArrayPosition.get(rs.getString("slug")), 
																totalContributions.get(metricToArrayPosition.get(rs.getString("slug"))) + 
																	  rs.getInt("value"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String generateContentFileCSV(Date startDateInterval, Date endDateInterval, List<String> developers){
					
		// ADDED LOC, CHANGED LOC, REMOVED LOC,	ADDED METHODS, CHANGED METHODS, 
		// ADDITION COMPLEXITY, CHANGE COMPLEXITY, BUGFIXES, BUGGY COMMITS, FAILED TESTS
		
		Hashtable<String, List<Integer>>contributions = new Hashtable<String, List<Integer>>();
		ArrayList<Integer> totalContributions = new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0,0,0,0,0,0,0,0,0}));
		
		DecimalFormat decimalFormat = new DecimalFormat("0.0000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		decimalFormat.setDecimalFormatSymbols(symbols);
		
		for (String developer : developers) {
			contributions.put(developer, new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0,0,0,0,0,0,0,0,0})));
		}
		
		Map<String, Integer> metricToArrayPosition = new HashMap<String, Integer>();
		metricToArrayPosition.put("loc:added", 0);
		metricToArrayPosition.put("loc:changed", 1);
		metricToArrayPosition.put("loc:deleted", 2);
		metricToArrayPosition.put("methods:added", 3);
		metricToArrayPosition.put("methods:changed", 4);
		metricToArrayPosition.put("complexity:added", 5);
		metricToArrayPosition.put("complexity:changed", 6);
		metricToArrayPosition.put("bugs:fixed", 7);
		metricToArrayPosition.put("commit:buggy", 8);
		metricToArrayPosition.put("tests:failed", 9);
		
		codeContributionAndCodeCompexityFormatter(startDateInterval, endDateInterval, developers, contributions, totalContributions, metricToArrayPosition);
		bugFixBuggyCommitAndFailedTestsFormatter(startDateInterval, endDateInterval, developers, contributions, totalContributions, metricToArrayPosition);
		
		String str = "DEVELOPER;ADDED LOC;CHANGED LOC;REMOVED LOC;ADDED METHODS;"
				+ "CHANGED METHODS;ADDITION COMPLEXITY;CHANGE COMPLEXITY;BUGFIXES;BUGGY COMMITS;FAILED TESTS\n";
		for (int i = 0; i < developers.size(); i++) {
			List<Integer> contribution = contributions.get(developers.get(i));
			str += "\""+developers.get(i)+"\";"; // developer login
			for(int j = 0; j < contribution.size(); j++) {
				str += "\""+decimalFormat.format(contribution.get(j))+"\";"; // developer contribution
			}
			str += "\n";
		}
		return str;
	}
}
