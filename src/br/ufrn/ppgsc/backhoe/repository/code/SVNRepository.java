package br.ufrn.ppgsc.backhoe.repository.code;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.exceptions.NonUniqueCommitByRevisionException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;
import br.ufrn.ppgsc.backhoe.persistence.model.TaskLog;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Blame;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.BlameHandler;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.Diff;
import br.ufrn.ppgsc.backhoe.persistence.model.helper.DiffChild;
import br.ufrn.ppgsc.backhoe.repository.AbstractRepository;

public class SVNRepository extends AbstractRepository implements CodeRepository {
	
	private org.tmatesoft.svn.core.io.SVNRepository repository;
	private SVNClientManager svnClientManager;
	private AbstractDeveloperDAO developerDao;
	private AbstractCommitDAO commitDao;
	
	public SVNRepository(String username, String password, String url){
		super(username, password, url);
		try {
			this.developerDao = (AbstractDeveloperDAO) DAOFactory.createDAO(DAOType.DEVELOPER);
			this.commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
		} catch (DAONotFoundException e) {
			e.printStackTrace();
		}
	}

	public SVNRepository() {
		this(null, null, null);
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
		
		DAVRepositoryFactory.setup();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
			repository.setAuthenticationManager(authManager);
			
			ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			svnClientManager = SVNClientManager.newInstance(options, repository.getAuthenticationManager());
			return true;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths) {
		return this.findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths);
	}
	
	@Override
	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths, List<String> ignoredPaths) {
		
		List<Commit> commits = new ArrayList<Commit>();

		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());

		try {
			System.out.print("Finding commits in the date interval: "+startDate.toString()+" - "+endDate.toString()+" ... ");
			
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			
			System.out.println("Finished!\n"+svnLogEntries.size()+" commits found!");
			
			for (SVNLogEntry svnLogEntry : svnLogEntries){
				if(developers == null || developers != null && developers.contains(svnLogEntry.getAuthor())){
					boolean betweenSearchRange = svnLogEntry.getDate().after(startDate) && svnLogEntry.getDate().before(endDate);
					if(betweenSearchRange){
						Commit commit = commitDao.findByRevision(svnLogEntry.getRevision());
						
						if(commit == null){
							commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
							commitDao.save(commit);
						}else if(commit != null && commit.getChangedPaths() != null && commit.getChangedPaths().isEmpty() && collectChangedPaths){
							commit.setChangedPaths(findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths));
							commitDao.update(commit);
						}
						commits.add(commit);
					}
				}
			}
			if(developers != null)
				System.out.println(commits.size()+" of "+svnLogEntries.size()+" belong to informed developers");
			return commits;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ChangedPath> findChangedPathsBySVNLogEntry(SVNLogEntry svnLogEntry, Commit associatedCommit, List<String> ignoredPaths){
		Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
		ArrayList<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
		for (SVNLogEntryPath svnLogEntryPath : svnLogEntryPaths) {
			ChangedPath changedPath = createChangedPath(svnLogEntryPath, associatedCommit, ignoredPaths);
			changedPaths.add(changedPath);
		}
		return changedPaths;
	}
	
	private ChangedPath createChangedPath(SVNLogEntryPath svnLogEntryPath, Commit associatedCommit, List<String> ignoredPaths){
		ChangedPath changedPath = null;
		if(!ignoredPaths.contains(svnLogEntryPath.getPath())){	
			changedPath = new ChangedPath();
			changedPath.setChangeType(svnLogEntryPath.getType());
			changedPath.setCommit(associatedCommit);
			changedPath.setPath(svnLogEntryPath.getPath());
			if(!changedPath.getChangeType().equals('D')){
				changedPath.setContent(getFileContent(changedPath.getPath(), changedPath.getCommit().getRevision()));
			}
		}
		return changedPath;
	}
	
	private Commit createCommit(SVNLogEntry svnLogEntry, boolean collectChangedPaths, List<String> ignoredPaths){
		Commit commit = new Commit();
		Developer developer = developerDao.findByCodeRepositoryUsername(svnLogEntry.getAuthor());
		if(developer == null) {
			developer = new Developer();
			developer.setCodeRepositoryUsername(svnLogEntry.getAuthor());
			developerDao.save(developer);
		}
		commit.setAuthor(developer);
		commit.setComment(svnLogEntry.getMessage());
		commit.setCreatedAt(svnLogEntry.getDate());
		commit.setRevision(svnLogEntry.getRevision());
		if(collectChangedPaths){
			List<ChangedPath> changedPaths = findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths);
			commit.setChangedPaths(changedPaths);
		}
		return commit;
	}
	
	public List<Commit> findCommitsFromLogs(List<TaskLog> logs, boolean collectChangedPaths, List<String> ignoredPaths){
		Map<Long, Commit> commitsMAP = new HashMap<Long, Commit>();
		for(TaskLog log: logs){
			List<Commit> commits = this.findCommitsFromLog(log, collectChangedPaths, ignoredPaths);
			for(Commit commit: commits){
				if(!commitsMAP.containsKey(commit.getRevision()))
					commitsMAP.put(commit.getRevision(), commit);
			}
		}
		return new ArrayList<Commit>(commitsMAP.values());
	}
	
	public List<Commit> findCommitsFromLog(TaskLog log, boolean collectChangedPaths, List<String> ignoredPaths){
		Set<Long> revisions = new HashSet<Long>();
		if(log.getRevisions() != null && !log.getRevisions().isEmpty()){
			String[] revisionsIDs = log.getRevisions().replaceAll(" ", "").split(",");
			for(int i = 0; i < revisionsIDs.length; i++){
				try{
					revisions.add(Long.parseLong(revisionsIDs[i]));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
		}
		
		Pattern pattern = Pattern.compile("(Revisão|revisão|Revisao|revisao|#)"
				+ "(:| |) "
				+ "(\\d+)");
		
		if(log.getDescription() != null && !log.getDescription().isEmpty()){
			Matcher matcher = pattern.matcher(log.getDescription());
			if (matcher.find()){
				revisions.add(Long.parseLong(matcher.group(3)));
			}
		}

		List<Commit> commits = new ArrayList<Commit>();
		for(Long revision: revisions){
			Commit commit = findCommitByRevision(revision, collectChangedPaths, ignoredPaths);
			if(commit != null){
				commit.setTask(log.getTask());
				commitDao.update(commit);
				commits.add(commit);
			}
		}
		return commits;
	}
	
	public Commit findCommitByRevision(Long revision,  boolean collectChangedPaths, List<String> ignoredPaths){
		Commit commit = null;
		try {
			SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
			svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(revision), SVNRevision.create(revision)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);
			
			if(svnLogEntries != null && !svnLogEntries.isEmpty()){
				if(svnLogEntries.size() > 1)
					throw new NonUniqueCommitByRevisionException("More of one commit was found by one revision");
				SVNLogEntry svnLogEntry = svnLogEntries.iterator().next();
				commit = commitDao.findByRevision(svnLogEntry.getRevision());
				if(commit == null) {
					commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
				}else if(commit != null && commit.getChangedPaths() != null && commit.getChangedPaths().isEmpty() && collectChangedPaths){
					commit.setChangedPaths(findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths));
					commitDao.update(commit);
				}
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		commitDao.save(commit);
		return commit;
	}

	@Override
	public String getFileContent(String path, Long revision) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			repository.getFile(path, revision, new SVNProperties(), output);
			return new String(output.toByteArray());
		} catch (SVNException e) {
//			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<Long> getFileRevisions(String path, Long startRevision, Long endRevision) {
		ArrayList<SVNFileRevision> svnFileRevisions = new ArrayList<SVNFileRevision>();
		ArrayList<Long> revisions = new ArrayList<Long>();
		try {
			System.out.println("Archive: "+path);
			System.out.print("Searching revisions (Rev: "+endRevision+")... ");
			repository.getFileRevisions(path, svnFileRevisions, startRevision, endRevision);
			for (SVNFileRevision svnFileRevision : svnFileRevisions) {
				revisions.add(svnFileRevision.getRevision());
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		System.out.println(revisions.size()+" founded revisions!");
		return revisions;
	}
	
	public List<ChangedPath> getChangedPathsFromLogTarefas(List<TaskLog> logs) {
		
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
				
				Commit commit = new Commit();
				commit.setRevision(revision);
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
	
	private List<ChangedPath> findJavaChangedPaths(Matcher matcher, Commit commit) {
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
	
	public List<Diff> buildDiffs(List<ChangedPath> changedPaths) {
		ArrayList<Diff> diffs = new ArrayList<Diff>();

		for (ChangedPath changedPath : changedPaths) {
			SVNRevision fixingRevision = SVNRevision.create(changedPath.getCommit().getRevision());
			List<SVNFileRevision> fileRevisions = new ArrayList<SVNFileRevision>();

			try {
				repository.getFileRevisions(changedPath.getPath(), fileRevisions, 1, changedPath.getCommit().getRevision());
				SVNRevision previousRevision = getPreviousVersion(fileRevisions);
				SVNRevision startRevision = getStartVersion(fileRevisions);
				
				ByteArrayOutputStream diffOut = new ByteArrayOutputStream();
				SVNURL urlPath = SVNURL.parseURIEncoded(this.url + changedPath.getPath());
				System.out.println("=================================================================");
				System.out.println("Executing Diff on path: "+changedPath.getPath()+"\n"
								 + "Fixing Revision: "+fixingRevision+"\n"
								 + "Previous Revision: "+previousRevision);
				SVNDiffClient diffClient = svnClientManager.getDiffClient();
				System.out.println("=================================================================");
				diffClient.doDiff(urlPath, fixingRevision, previousRevision, fixingRevision, SVNDepth.INFINITY, true, diffOut);
				
				Diff diff = new Diff(changedPath.getCommit().getTask(), fixingRevision.getNumber(), 
						previousRevision.getNumber(), startRevision.getNumber(), changedPath);
				
				diffs.add(diff);
				buildDiffChilds(diff, diffOut);
				diffOut.close();
			} catch (SVNException e) {
				if(e.getMessage().contains("is not a file in revision")) {
					System.err.println(changedPath.getPath()+" Not Found. Continuing...");
				} else {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return diffs;
	}

	private SVNRevision getPreviousVersion(List<SVNFileRevision> revisions) {
		SVNRevision revision;
		if (revisions.size() > 1) {
			revision = SVNRevision.create(((SVNFileRevision) revisions
					.get(revisions.size() - 2)).getRevision());
		} else {
			revision = SVNRevision.create(((SVNFileRevision) revisions.get(0))
					.getRevision());
		}
		return revision;
	}

	private SVNRevision getStartVersion(List<SVNFileRevision> revisions) {
		SVNRevision revision = SVNRevision.create(((SVNFileRevision) revisions
				.get(0)).getRevision());
		return revision;
	}

	private void buildDiffChilds(Diff diff, ByteArrayOutputStream diffOut) {
		ByteArrayInputStream diffInputStream = new ByteArrayInputStream(
				diffOut.toByteArray());
		BufferedReader br = new BufferedReader(new InputStreamReader(
				diffInputStream));
		DiffChild childReference = null;
		String theLineBefore = null;
		try {
			while (br.ready()) {
				String line = br.readLine();
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

	public List<Blame> buildBlames(List<Diff> diffs) {
		
		SVNLogClient logClient = svnClientManager.getLogClient();

		for (Diff diff : diffs) {
			SVNRevision svnFixingRevision = SVNRevision.create(diff
					.getFixingRevision());
			SVNRevision svnStartRevision = SVNRevision.create(diff
					.getStartRevision());
			SVNRevision svnPreviousRevision = SVNRevision.create(diff
					.getPreviousRevision());

			ChangedPath changedPath = diff.getChangedPath();

			try {
				BlameHandler blameHandler = new BlameHandler();
				blameHandler.setChangedPath(changedPath);

				SVNURL url = SVNURL.parseURIEncoded(this.url
						+ changedPath.getPath());
				logClient.doAnnotate(url, svnFixingRevision, svnStartRevision,
						svnPreviousRevision, blameHandler);

				List<Blame> blames = (List<Blame>) blameHandler.getBlameList();
				return analyzeBlames(blames, diff);
			} catch (SVNException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<Blame>();
	}
	
	private List<Blame> analyzeBlames(List<Blame> blames, Diff diff) {
		
		List<Blame> blamesAnalized = new LinkedList<Blame>();
		
		for (DiffChild diffChild : diff.getChildren()) {
			boolean onlyAdditions = diffChild.getRemovals().isEmpty();

			if (onlyAdditions) {
				for (Blame blame : blames) {
					if (blame.getLine().equals(diffChild.getLineJustBefore())) {
						blame.setTask(diff.getTask());
						diffChild.getBlames().add(blame);
						blamesAnalized.add(blame);
					}
				}
			} else {
				for (Blame blame : blames) {
					for (String removal : diffChild.getRemovals()) {
						if (blame.getLine().equals(
								removal.replaceFirst("-", ""))) {
							blame.setTask(diff.getTask());
							diffChild.getBlames().add(blame);
							blamesAnalized.add(blame);
						}
					}
				}
			}
		}
		return blamesAnalized;
	}

	private boolean verifyWhiteSpace(String line) {
		return line.replaceAll("\\+", "").replaceAll("-", "").trim().length() == 0;
	}
}