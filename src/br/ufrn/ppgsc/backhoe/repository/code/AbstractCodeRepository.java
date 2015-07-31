package br.ufrn.ppgsc.backhoe.repository.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.DiffChild;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;

public abstract class AbstractCodeRepository extends AbstractRepository implements CodeRepository{
	
	protected AbstractDeveloperDAO developerDao;
	protected AbstractCommitDAO commitDao;
	
	public AbstractCodeRepository(String username, String password, String url){
		super(username, password, url);
		try {
			this.developerDao = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
			this.commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
		} catch (DAONotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean connect() throws MissingParameterException {
		if(url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if(username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if(password == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		return specificConnect();
	}
	
	protected abstract boolean specificConnect();
	
	/**
	 * 
	 * Util methods
	 * 
	 * Find commit from log and task log
	 * 
	 */
	
	public List<Commit> findCommitsFromLogs(List<TaskLog> logs, boolean collectChangedPaths, List<String> ignoredPaths){
		Map<String, Commit> commitsMAP = new HashMap<String, Commit>();
		if(!logs.isEmpty()){
			int count = 1;
			for(TaskLog log: logs){
				System.out.print("Processing log "+count+++"/"+logs.size()+"...");
				List<Commit> commits = findCommitsFromLog(log, collectChangedPaths, ignoredPaths);
				for(Commit commit: commits){
					if(!commitsMAP.containsKey(commit.getRevision()))
						commitsMAP.put(commit.getRevision(), commit);
				}
				System.out.println("Done!");
			}
		}
		return new ArrayList<Commit>(commitsMAP.values());
	}
	
	public List<Commit> findCommitsFromLog(TaskLog log, boolean collectChangedPaths, List<String> ignoredPaths){
		Set<String> revisions = new HashSet<String>();
		
		List<Commit> commits = new ArrayList<Commit>();
		
		if(log.getRevisions() != null && !log.getRevisions().isEmpty()){
			String[] revisionsIDs = log.getRevisions().replaceAll(" ", "").split(",");
			
			revisions.addAll(new ArrayList<String>(Arrays.asList(revisionsIDs)));
			
			Pattern pattern = Pattern.compile("(Revisão|revisão|Revisao|revisao|#)"
					+ "(:| |) "
					+ "(\\d+)");
			
			if(log.getDescription() != null && !log.getDescription().isEmpty()){
				Matcher matcher = pattern.matcher(log.getDescription());
				if (matcher.find()){
					revisions.add(matcher.group(3));
				}
			}

			for(String revision: revisions){
				
				Commit commit = commitDao.findByRevision(revision); // find in local bd
				if(commit == null)
					commit = findCommitByRevision(revision, collectChangedPaths, ignoredPaths); // find in code repository
				
				if(commit != null){
					commit.setTask(log.getTask());
					commitDao.update(commit);
					commits.add(commit);
				}
			}
		}		
		return commits;
	}
	
	protected List<ChangedPath> findJavaChangedPaths(Matcher matcher, Commit commit) {
		List<ChangedPath> changedPaths = new LinkedList<ChangedPath>();
		while (matcher.find()) {
			String c1 = matcher.group(1);
			String trunk = "/" + matcher.group(3);
			String path = matcher.group(4);
			if (path.contains(".java")) {
				ChangedPath changedPath = new ChangedPath(trunk+path, c1.charAt(0), commit, null);
				changedPaths.add(changedPath);
			} else {
				continue;
			}
		}
		return changedPaths;
	}
	
	protected List<ChangedPath> getChangedPathsFromLogTarefas(List<TaskLog> logs, Commit.RepositoryType repositoryType) {
		
		String re1 = "([AUD])"; // Single Character 1
		String re2 = "(\\s+)"; // White Space 1
		String re3 = "(trunk)"; // Word 1
		String re4 = "((?:\\/[\\w\\.\\-]+)+)"; // Unix Path 1

		String re5 = "(\\s)";
		String re6 = "(\\d+)";
		String re7 = "(:)";
		String re8 = "(branches)";
		String re9 = "(trunk2)";

		// ([AUD])(\s+)(trunk)((?:\/[\w\.\-]+)+)
		Pattern p1 = Pattern.compile(re1 + re2 + re3 + re4,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		// (\\s)(\\d+)(\\s)
		Pattern p2 = Pattern.compile(re5 + re6 + re5, Pattern.CASE_INSENSITIVE
				| Pattern.DOTALL);
		// (:)(\\d+)(\\s)
		Pattern p3 = Pattern.compile(re7 + re6 + re5);
		// ([AUD])(\s+)(branches)((?:\/[\w\.\-]+)+)
		Pattern p4 = Pattern.compile(re1 + re2 + re8 + re4,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		// ([AUD])(\s+)(trunk2)((?:\/[\w\.\-]+)+)
		Pattern p5 = Pattern.compile(re1 + re2 + re9 + re4,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		// (\\s)(\\d+)
		Pattern p6 = Pattern.compile(re5 + re6);
		// (:)(\\d+)
		Pattern p7 = Pattern.compile(re7 + re6);
		
		Pattern authorPattern = Pattern.compile("(por) ([a-zA-Z0-9]+)");
		
		List<ChangedPath> changes = new ArrayList<ChangedPath>();
		
		long revision = -1;
		for (TaskLog log : logs) {
			String[] logParts = log.getDescription().split("[Rr]evis[ãa]o");
			// getting the revisions

			for (int i = 1; i < logParts.length; i++) {
				Matcher revMatcher = authorPattern.matcher(logParts[i]);
				/*if(!revMatcher.find()) {
					continue;
				} else {
					if(!svnLogins.contains(revMatcher.group(2))) {
						continue;
					}
				}*/
				if(revMatcher.find()) {
					Developer developer = new Developer();
					developer.setCodeRepositoryUsername(revMatcher.group(2));
					log.setAuthor(developer);
//					log.setUsuario(revMatcher.group(2));
				}
				
				revMatcher = p2.matcher(logParts[i]);

				// (begin) get the revision
				if (revMatcher.find()) {
					revision = Long.valueOf(revMatcher.group(2));
				} else {
					revMatcher = p3.matcher(logParts[i]);
					if (revMatcher.find()) {
						revision = Long.valueOf(revMatcher.group(2));
					} else {
						revMatcher = p6.matcher(logParts[i]);
						if (revMatcher.find()) {
							revision = Long.valueOf(revMatcher.group(2));
						} else {
							revMatcher = p7.matcher(logParts[i]);
							if(revMatcher.find()) {
								revision = Long.valueOf(revMatcher.group(2));
							}
						}
					}
				}
				// (end)

				// (begin) get the changed paths/files
				
				Commit commit = new Commit(repositoryType);
				commit.setRevision(new Long(revision).toString());
				commit.setTask(log.getTask());
				
				List<Matcher> assetMatchers = new LinkedList<Matcher>();
				assetMatchers.add(p1.matcher(logParts[i]));
				assetMatchers.add(p4.matcher(logParts[i]));
				assetMatchers.add(p5.matcher(logParts[i]));
				
				for(Matcher assetMatcher: assetMatchers){
					List<ChangedPath> changedPaths = findJavaChangedPaths(assetMatcher, commit);
					if(!changedPaths.isEmpty()){
						changes.addAll(changedPaths);
						break;
					}
				}
				// (end)
			}
		}
		return changes;
	}
	
	protected void buildDiffChilds(Diff diff, BufferedReader brOut){
		DiffChild childReference = null;
		String theLineBefore = null;
		try {
			System.out.println("===================== DIFF ===================");
			while (brOut.ready()) {
				String line = brOut.readLine();			
				System.out.println(line);
				if (!verifyWhiteSpace(line)) {
					if (isDiffChild(line)) {
						DiffChild child = new DiffChild();
						diff.getChildren().add(child);
						child.setHeader(line);
						childReference = child;
					} else if (isAddition(line)) {
						if (childReference != null) {
							childReference.getAdditions().add(line);
							if (theLineBefore != null)
								childReference.setLineJustBefore(theLineBefore);
							theLineBefore = null;
						}
					} else if (isRemoval(line)) {
						if (childReference != null) {
							childReference.getRemovals().add(line);
							if (theLineBefore != null)
								childReference.setLineJustBefore(theLineBefore);
							theLineBefore = null;
						}
					} else {
						theLineBefore = line;
					}
				}
			}
			System.out.println("================== DIFF END ===================");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
	
	private boolean isDiffChild(String line) {
		Pattern pattern = Pattern
				.compile("@@\\s-\\d+,\\d+\\s\\+\\d+,\\d+\\s@@");
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	private boolean isAddition(String line) {
		Pattern pattern = Pattern.compile("^(\\+\\s.+)");
		Matcher matcher = pattern.matcher(line);

		boolean result = matcher.find();

		if (!result) {
			pattern = Pattern.compile("^(\\+)");
			matcher = pattern.matcher(line);
			result = matcher.find();
		}

		return result;
	}

	private boolean isRemoval(String line) {
		Pattern pattern = Pattern.compile("-\\s.+");
		Matcher matcher = pattern.matcher(line);

		return matcher.find();
	}
	
	private boolean verifyWhiteSpace(String line) {
		return line.replaceAll("\\+", "").replaceAll("-", "").trim().length() == 0;
	}
}
