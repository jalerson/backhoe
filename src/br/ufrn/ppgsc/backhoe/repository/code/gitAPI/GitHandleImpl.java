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
	
	private String gitDirectoryPath = "repositories/cinephilebox/";
	
	/*
	 * 
	 * Methods to git command line execution
	 * 
	 */
	
	private InputStream executeGitCommand(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, new File(gitDirectoryPath));
		return p.getInputStream();
	}
	
	private InputStream executeGitCommand(String[] command) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, new File(gitDirectoryPath));
		return p.getInputStream();
	}
	
	/*
	 *
	 * Methos to recovery information of commit and its changed paths from git repository
	 *
	 */
	
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
					
				}else if(line.split(" +").length > 2){ // Is a commit information line
					
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
					
					if(developers == null || (developers != null && belongsToSpecificDeveloper(currentLogEntry, developers))){
					
						GITLogChange logChange = null;
						
						try {
							logChange = commitChangeInformationLineHandle(currentLogEntry, line, supportedChangeTypes);
						} catch (GitAPIException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						if(logChange != null)
							logChanges.add(logChange);
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
		if(path != null && supportedTypes != null)
			for(String supportedType: supportedTypes)
				if(path.endsWith(supportedType))
					return true;
		return false;
	}
	
	private GITLogChange commitChangeInformationLineHandle(GITLogEntry log, String line, List<String> supportedChangeTypes) throws GitAPIException{
		
		if(line != null && !line.isEmpty()){
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
				changeContent += line;				
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
				fileRevisions.add(line);
			}while ((line = br.readLine()) != null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(startRevision != null && endRevision != null){
			
			int startRevisionIndex = fileRevisions.indexOf(startRevision);
			int endRevisionIndex = fileRevisions.indexOf(endRevision);
			
			if(endRevisionIndex != -1)
				endRevisionIndex++;
			
			try{
				fileRevisions = fileRevisions.subList(startRevisionIndex, endRevisionIndex);
			}catch(Exception e){}
		}
		
		return fileRevisions;
	}

	@Override
	public GITLogEntry getCommitInformations(String revision)
			throws GitAPIException {
		
		if(revision == null || revision.isEmpty())
			throw new GitAPIException("The informed revision is not valid!");
		
		String[] command = new String[] { "git", "show", "--pretty=format:%H %ae %ad %B", 
				"--date=short", 
				revision};
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(executeGitCommand(command)));
			String commitInfoLine = br.readLine();
			
			if(commitInfoLine == null || commitInfoLine.startsWith("fatal: "))
				throw new GitAPIException("The informed revision is not valid!");
			
			return commitInformationLineHandle(commitInfoLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(GitAPIException e){
			throw(e);
		}
		return null;
	}
}
