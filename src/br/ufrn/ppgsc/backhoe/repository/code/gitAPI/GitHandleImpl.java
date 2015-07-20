package br.ufrn.ppgsc.backhoe.repository.code.gitAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitHandleImpl implements GitHandle {
	
	private String url,
	        user,
	        password;
	
	private String directoryPath;
	
	public GitHandleImpl(String url, String user, String password) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
	}
	
	public boolean wasClonedRepository(){
		String gitDirectoryPath = getDirectoryPath(url);
		return new File(gitDirectoryPath).isDirectory();
	}
	
	private String getDirectoryPath(String url){
		try{
			String aux = new StringBuilder(url).reverse().toString();
			aux = aux.substring(aux.indexOf(".")+1, aux.indexOf("/"));
			return "repositories/"+new StringBuilder(aux).reverse().toString();
		}catch(Exception e){}
		return null;
	}
	
	public void pull() throws GitAPIException{
		String[] command = new String[] { "git", "pull"};
		
			Process p;
			try {
				p = Runtime.getRuntime().exec(command, null, new File(getDirectoryPath(url)));
				verifyGitCommandProcessOutputError(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void cloneRepository() throws GitAPIException{

			
		String[] command = new String[] { "git", "clone", url};
		
			
			Process p;
			try {
				p = Runtime.getRuntime().exec(command, null, new File("repositories/"));
				verifyGitCommandProcessOutputError(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(wasClonedRepository()){
			this.directoryPath = getDirectoryPath(url);
		}
	}
	
	private void verifyGitCommandProcessOutputError(Process p) throws GitAPIException{
		List<String> errorOutput = new ArrayList<String>(),
			     output = new ArrayList<String>();
	
		try {
			StreamGobbler errorOutputGobbler = new StreamGobbler(p.getErrorStream(), errorOutput);
			errorOutputGobbler.start();
			p.waitFor();
			
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), output);
			outputGobbler.start();
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		if(!errorOutput.isEmpty() && isGitErrorOutputLine(errorOutput.get(0)))
			throw new GitAPIException(errorOutput.get(0));
		
		if(!output.isEmpty() && isGitErrorOutputLine(output.get(0)))
			throw new GitAPIException(output.get(0));
		
	}
	
	private boolean isGitErrorOutputLine(String line){
		if(line != null && line.startsWith("error: ") || line.startsWith("fatal: "))
			return true;
		return false;
	}
	
	/*
	 * 
	 * Methods to git command line execution
	 * 
	 */
	
	private InputStream executeGitCommand(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, new File(getDirectoryPath(url)));
		return p.getInputStream();
	}
	
	private InputStream executeGitCommand(String[] command) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, new File(getDirectoryPath(url)));
		return p.getInputStream();
	}
	
//	private InputStream executeGitCommand(String[] command, String directory) throws IOException {
//		Process p = Runtime.getRuntime().exec(command, null, new File(directory));
//		return p.getInputStream();
//	}
	
	/*
	 *
	 * Methos to recovery information of commit and its changed paths from git repository
	 *
	 */
	
	public static void main(String[] args) {
		
		GitHandleImpl git = new GitHandleImpl("https://github.com/vicenteneto/HibernateSample.git", null, null);
		
		Date startDate = Date.valueOf("2013-01-01");
		Date endDate = Date.valueOf("2015-12-17");
		List<String> developers = new ArrayList<String>(Arrays.asList(new String[]{ "joaohelis.bernardo@gmail.com", "smithascari@gmail.com","vicente.neto@dce.ufpb.br"}));
		List<GITLogEntry> logs = git.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true, null);
		for(GITLogEntry log: logs){
			System.out.println(log);
			for(GITLogChange change: log.getChangedPaths())
				System.out.println(change);
			System.out.println();
		}
	}
	
	@Override
	public List<GITLogEntry> findCommitsByTimeRangeAndDevelopers(Date startDate,
			Date endDate, List<String> developers, boolean collectChangedPaths,
			List<String> ignoredPaths) {
		// TODO Auto-generated method stub
		
		List<String> supportedChangeTypes = new ArrayList<String>(Arrays.asList(new String[]{ ".java"}));
		
		List<GITLogEntry> logEntries = new ArrayList<GITLogEntry>();
		
		String startDateFormatted = startDate.toString() + " 00:00",
			   endDateFormatted = endDate.toString() + " 23:59";
		
		BufferedReader br;
		
		try {	
			String[] command = new String[] { "git", "log", "--pretty=format:%H %ae %ad %B", 
					"--date=short", 
					"--no-merges", 
					"--name-status",
					"--after=\""+startDateFormatted+"\"",
					"--before=\""+endDateFormatted+"\""};
		
			br = new BufferedReader(new InputStreamReader(executeGitCommand(command)));
			
			GITLogEntry currentLogEntry = null;
			List<GITLogChange> logChanges = null;
			
			String line = null;
			while ((line = br.readLine()) != null){
				
				if(line.isEmpty()){
					continue;
					
				}else if(isCommitLine(line)){ // Is a commit information line
					
					currentLogEntry = commitInformationLineHandle(line);
					logChanges = new ArrayList<GITLogChange>();
					
					if(logEntries == null)
						logEntries = new ArrayList<GITLogEntry>();
					else{
						if(developers == null || (developers != null && belongsToSpecificDeveloper(currentLogEntry, developers))){
							currentLogEntry.setChangedPaths(logChanges);
							logEntries.add(currentLogEntry);
						}	
					}
					
				}else{ // Is a change commit information line
					
					if((developers == null && collectChangedPaths)|| (collectChangedPaths && developers != null && belongsToSpecificDeveloper(currentLogEntry, developers))){
					
						GITLogChange logChange = null;
						
						try {
							logChange = commitChangeInformationLineHandle(currentLogEntry, line, supportedChangeTypes);
						} catch (GitAPIException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						if(logChange != null && !isIgonredPath(logChange.getPath(), ignoredPaths)){
							logChanges.add(logChange);
						}
					}
				}
				
				if(!br.ready() && logEntries.isEmpty()){
					if(developers == null || (developers != null && belongsToSpecificDeveloper(currentLogEntry, developers))){
						currentLogEntry.setChangedPaths(logChanges);
						logEntries.add(currentLogEntry);
					}	
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logEntries;
	}
	
	private boolean isCommitLine(String line){
		
		String revisionRegexValidation = "[_A-Za-z0-9-]+";
		String emailRegexValidation = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		String dateRegexValidation = "\\d{4}-\\d{2}-\\d{2}";
		
		String[] lineParts = line.split(" +");
		
		if(lineParts.length >= 4){
			if(lineParts[0].matches(revisionRegexValidation) &&
			   lineParts[1].matches(emailRegexValidation) &&
			   lineParts[2].matches(dateRegexValidation))
				return true;
		}
		return false;
	}
	
	private boolean isIgonredPath(String path, List<String> ignoredPaths){
		if(ignoredPaths != null && !ignoredPaths.isEmpty() && path != null){
			for(String ignoredPath: ignoredPaths)
				if(path.contains(ignoredPath))
					return true;
		}
		return false;
	}

	@Override
	public List<GITLogEntry> findCommitsByTimeRange(Date startDate, Date endDate,
			boolean collectChangedPaths, List<String> ignoredPaths) {
		return findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths);
	}
	
	/*
	 * 
	 * Auxiliar methods
	 * 
	 */
	
	public boolean belongsToSpecificDeveloper(GITLogEntry log, List<String> developersEmails){
		if((log != null && log.getAuthorEmail() != null && !log.getAuthorEmail().isEmpty())&&
		   (developersEmails != null && !developersEmails.isEmpty())&&
		   (developersEmails.contains(log.getAuthorEmail()))){
			return true;
		}
		return false;
	}
	
	private boolean supportedChangeLogVerify(String path, List<String> supportedTypes){
		if(!path.startsWith("."))
			if(path != null && supportedTypes != null)
				for(String supportedType: supportedTypes)
					if(path.endsWith(supportedType))
						return true;
		return false;
	}
	
	private GITLogChange commitChangeInformationLineHandle(GITLogEntry log, String line, List<String> supportedChangeTypes) throws GitAPIException{
		
		if(line != null && !line.isEmpty() && line.split("\\s+").length == 2){
			String[] changeInformationParts = line.split("\\s+");
			String changeType = changeInformationParts[0];
			String path = changeInformationParts[1];
			
			if(!supportedChangeLogVerify(path, supportedChangeTypes))
				throw new GitAPIException("The informed changePath type is not supported!");	
			
			String content = null;
			try{
				content = getChangeContent(log.getRevision(), path);
			}catch(GitAPIException e){
				e.printStackTrace();
			}
			return new GITLogChange(path, changeType.charAt(0), content);
		} 
		return null;
	}
	
	@Override
	public String getChangeContent(String revision, String changePath) throws GitAPIException {
		
		String command = "git show "+revision+":"+changePath;
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(executeGitCommand(command)));
			
			String changeContent = br.readLine();
			
			if(changeContent != null && (changeContent.startsWith("fatal: Path")||changeContent.startsWith("fatal: Invalid object name")))
				throw new GitAPIException("Informed revision or path is not valid!");
			
			String line = null;
			while ((line = br.readLine()) != null){					
				changeContent += line + "\n";
			}
			return changeContent;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private GITLogEntry commitInformationLineHandle(String line){
		if(line != null && !line.isEmpty()){
			String[] commitInformationParts = line.split(" ");
			
			String revision = commitInformationParts[0];
			String authorEmail = commitInformationParts[1];
			Date createdAt = Date.valueOf(commitInformationParts[2]);
			String branch = null;
			int indexCommitComment = line.indexOf(commitInformationParts[2])+commitInformationParts[2].length()+1;
			String comment = line.substring(indexCommitComment);
			
			return new GITLogEntry(revision, comment, createdAt, branch, authorEmail);
		}
		return null;
	}

	@Override
	public List<String> getFileRevisions(String path, String startRevision,
			String endRevision) throws GitAPIException {
		List<String> fileRevisions = new ArrayList<String>();
		
		String command = "git log --pretty=format:%H "+path;
		
		BufferedReader br;
		
		try {
			br = new BufferedReader(new InputStreamReader(executeGitCommand(command)));
			String line = br.readLine();
	
			if(line.startsWith("fatal:"))
				throw new GitAPIException(line);
			
			do{
				fileRevisions.add(0, line);
			}while ((line = br.readLine()) != null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int startRevisionIndex = -1;
		int endRevisionIndex = -1;
		
		if(startRevision != null && endRevision != null){
			
			startRevisionIndex = fileRevisions.indexOf(startRevision);
			endRevisionIndex = fileRevisions.indexOf(endRevision);
			
			if(endRevisionIndex != -1)
				endRevisionIndex++;
			
		}else if(startRevision != null){
			
			startRevisionIndex = fileRevisions.indexOf(startRevision);
			endRevisionIndex = fileRevisions.size();
			
		}else if(endRevision != null){
			
			startRevisionIndex = 0;
			endRevisionIndex = fileRevisions.indexOf(endRevision);
			
			if(endRevisionIndex != -1)
				endRevisionIndex++;
		}
		
		if(startRevision != null || endRevision != null){
			try{
				fileRevisions = fileRevisions.subList(startRevisionIndex, endRevisionIndex);
			}catch(Exception e){}
		}
		return fileRevisions;
	}

	@Override
	public GITLogEntry getCommitInformations(String revision)
			throws GitAPIException {
		
		List<String> supportedChangeTypes = new ArrayList<String>(Arrays.asList(new String[]{ ".java"}));
		
		if(revision == null || revision.isEmpty())
			throw new GitAPIException("The informed revision is not valid!");
		
		String[] command = new String[] { "git", "show", "--pretty=format:%H %ae %ad %B", 
				"--date=short", 
				"--name-status",
				revision};
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(executeGitCommand(command)));
			String line = br.readLine();
			
			if(line == null || line.startsWith("fatal: "))
				throw new GitAPIException("The informed revision is not valid!");
			
			GITLogEntry log = commitInformationLineHandle(line);
			List<GITLogChange> changedPaths = new ArrayList<GITLogChange>();
			
			while ((line = br.readLine()) != null){
				
				if(line.isEmpty()) continue;
				GITLogChange logChange = null;
				
				try {
					logChange = commitChangeInformationLineHandle(log, line, supportedChangeTypes);
				} catch (GitAPIException e) {}
				
				if(logChange != null)
					changedPaths.add(logChange);		
			}
			log.setChangedPaths(changedPaths);
			return log;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(GitAPIException e){
			throw(e);
		}
		return null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
}