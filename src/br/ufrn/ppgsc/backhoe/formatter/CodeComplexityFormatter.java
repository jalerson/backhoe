package br.ufrn.ppgsc.backhoe.formatter;

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

public class CodeComplexityFormatter extends AbstractFormatter {

	public CodeComplexityFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super(startDate, endDate, developers, localRepository, minerName, systemName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void format() {
		String contentFile = generateContentFileCSV(startDate, endDate, developers);
		String fileName = systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate()+"_CodeComplexityContribution.csv";
		String filePathAndName = getDirPath()+ "/" + fileName;
		createCSV(contentFile, filePathAndName);
	}
	
	public String generateContentFileCSV(Date startDateInterval, Date endDateInterval, List<String> developers){
		String sql = "select d.codeRepositoryUsername, mt.slug, sum(m.value) as sumValue " +
					 "from Developer d, Commit c, ChangedPath cp, Metric m, MetricType mt "+
					 "where m.objectType = 'ChangedPath' " +
					 " and m.minerSlug = 'codeComplexityMiner' " +
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
			
			// ADDITION COMPLEXITY, CHANGE COMPLEXITY
			
			Hashtable<String, List<Integer>>codeComplexityMAP = new Hashtable<String, List<Integer>>();
			ArrayList<Integer> totalComplexityContributions = new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0}));
			ArrayList<Float> avgComplexityContributions = new ArrayList<Float>(Arrays.asList(new Float[]{0f,0f}));
			
			DecimalFormat decimalFormat = new DecimalFormat("0.0000");
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator(',');
			decimalFormat.setDecimalFormatSymbols(symbols);
			
			for (String developer : developers) {
				codeComplexityMAP.put(developer, new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0})));
			}
			
			Map<String, Integer> metricToArrayPosition = new HashMap<String, Integer>();
			metricToArrayPosition.put("complexity:added", 0);
			metricToArrayPosition.put("complexity:changed", 1);
			
			while (rs.next()){
				ArrayList<Integer> complexityContribution = (ArrayList<Integer>) codeComplexityMAP.get(rs.getString("codeRepositoryUsername"));
				complexityContribution.set(metricToArrayPosition.get(rs.getString("slug")), rs.getInt("sumValue"));
				codeComplexityMAP.put(rs.getString("codeRepositoryUsername"), complexityContribution);
				
				totalComplexityContributions.set(metricToArrayPosition.get(rs.getString("slug")), totalComplexityContributions.get(metricToArrayPosition.get(rs.getString("slug"))) + 
																	  rs.getInt("sumValue"));
			}
			
			for(int i = 0; i < totalComplexityContributions.size(); i++) {
				avgComplexityContributions.set(i, (float) totalComplexityContributions.get(i) / developers.size());
			}
			
			String str = "DEVELOPER;ADDITION COMPLEXITY;CHANGE COMPLEXITY\n";
			for (int i = 0; i < developers.size(); i++) {
				List<Integer> complexityInformation = codeComplexityMAP.get(developers.get(i));
				str += "\""+developers.get(i)+"\";"; // developer login
				for(int j = 0; j < complexityInformation.size(); j++) {
					str += "\""+decimalFormat.format(complexityInformation.get(j))+"\";"; // developer complexity contribution
				}
				str += "\n";
			}
			return str;
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return null;
	}

}
