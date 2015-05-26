package br.ufrn.ppgsc.backhoe.formatter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufrn.ppgsc.backhoe.repository.local.LocalRepository;

public class TaskMetricsFormatter extends AbstractFormatter {

	public TaskMetricsFormatter(Date startDate, Date endDate,
			List<String> developers, LocalRepository localRepository,
			String minerName, String systemName) {
		super(startDate, endDate, developers, localRepository, minerName, systemName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void format() {
		// TODO Auto-generated method stub
		String contentFile = generateContentFileCSV(startDate, endDate, developers);
		String fileName = systemName+"_"+getHumanizedStartDate()+"_"+getHumanizedEndDate()+"_TaskMetrics.csv";
		String filePathAndName = getDirPath()+ "/" + fileName;
		createCSV(contentFile, filePathAndName);

	}
	
	public String generateContentFileCSV(Date startDateInterval, Date endDateInterval, List<String> developers){
		

		String sql = "select d.codeRepositoryUsername as login, t.id as task_id, c.revision, m.objectId cp_id, cp.path, mt.slug, sum(m.value) as metric_value " +
					 "from Developer d, Commit c, ChangedPath cp, Metric m, MetricType mt, Task t " +
					 "where m.objectType = 'ChangedPath' " +
					 "and m.objectId = cp.id " +
					 "and m.type_id = mt.id " +
					 "and c.author_id = d.id " +
					 "and cp.commit_id = c.revision " +
					 "and c.task_id = t.id " +
					 "and c.revision in (select distinct revision from Commit c " +
					 "		 			 where c.author_id in (select id from Developer " +
					 "							 			  where codeRepositoryUsername in :developerLogins)) "+
					 "and t.createdAt between ':startDateInterval' and ':endDateInterval' "+
					 "group by d.codeRepositoryUsername, t.id, mt.slug, c.revision";
		
		sql = sql.replace(":startDateInterval", startDateInterval.toString());
		sql = sql.replace(":endDateInterval", endDateInterval.toString());
		sql = sql.replace(":developerLogins", sqlListFormatter(developers));
		
		Map<String, // Developer
			Map<Long, // Tasks
			Map<Long, // Commits
			Map<Long, // ChangedPaths
			List<Integer>>>>> tasksByDeveloperMAP = new HashMap<String, 
														Map<Long, 
														Map<Long, 
														Map<Long, 
														List<Integer>>>>>();
		
		for (String developer : developers){
			tasksByDeveloperMAP.put(developer, new HashMap<Long, 
												   Map<Long, 
												   Map<Long, 
												   List<Integer>>>>());
		}
		
		// ADDED LOC, CHANGED LOC, REMOVED LOC,	ADDED METHODS, CHANGED METHODS, ADDED COMPLEXITY, CHANGED COMPLEXITY
		
		Map<String, Integer> metricToArrayPosition = new HashMap<String, Integer>();
		metricToArrayPosition.put("loc:added", 0);
		metricToArrayPosition.put("loc:changed", 1);
		metricToArrayPosition.put("loc:deleted", 2);
		metricToArrayPosition.put("methods:added", 3);
		metricToArrayPosition.put("methods:changed", 4);
		metricToArrayPosition.put("complexity:added", 5);
		metricToArrayPosition.put("complexity:changed", 6);
		
		try {
			ResultSet rs = localRepository.getConection().createStatement().executeQuery(sql);
		  
//			while (rs.next()){
//				if(tasksByDeveloperMAP.containsKey(rs.getString("login"))){
//										
//					Map<Long, Map<Long, Map<Long, List<Integer>>>> tasksByDeveloper = tasksByDeveloperMAP.get(rs.getString("login"));
//					
//					if(!tasksByDeveloper.containsKey(rs.getLong("task_id"))){
//						tasksByDeveloper.put(rs.getLong("task_id"), new HashMap<Long,
//																 	 Map<Long, 
//																 	 List<Integer>>>());
//					}
//					
//					Map<Long, Map<Long, List<Integer>>> commitsByTask = tasksByDeveloper.get(rs.getLong("task_id"));
//					
//					if(!commitsByTask.containsKey(rs.getLong("revision"))){
//						commitsByTask.put(rs.getLong("revision"), new HashMap<Long, 
//																	List<Integer>>());
//					}
//					
//					Map<Long, List<Integer>> changedPathsByCommit = commitsByTask.get(rs.getLong("revision"));
//					
//					if(!changedPathsByCommit.containsKey(rs.getLong("cp_id"))){
//						changedPathsByCommit.put(rs.getLong("cp_id"), new ArrayList<Integer>(Arrays.asList(new Integer[]{0,0,0,0,0,0,0})));
//					}
//					
//					List<Integer> metricsByChangedPath = changedPathsByCommit.get(rs.getLong("cp_id"));
//					
//					metricsByChangedPath.set(metricToArrayPosition.get(rs.getString("slug")), rs.getInt("metric_value"));
//				}
//			}
			
//			String str = "DEVELOPER;TASK ID; COMMIT REVISION; PATH; METRIC; VALUE\n";
			String str = "DEVELOPER;TASK ID; COMMIT REVISION; CHANGED PATH ID; METRIC; VALUE\n";
			
			while (rs.next()){
				str += "\""+rs.getString("login")+"\";"; // developerUsername
				str += "\""+rs.getLong("task_id")+"\";"; 
				str += "\""+rs.getLong("revision")+"\";"; 
				str += "\""+rs.getString("cp_id")+"\";"; 
				str += "\""+rs.getString("slug")+"\";"; 
				str += "\""+rs.getInt("metric_value")+"\";"; 
				str += "\n";
			}
			return str;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
