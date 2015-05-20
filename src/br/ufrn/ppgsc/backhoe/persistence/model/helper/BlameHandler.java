package br.ufrn.ppgsc.backhoe.persistence.model.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;

import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;

public class BlameHandler implements ISVNAnnotateHandler {
	
	private List<Blame> blames;
	private ChangedPath changedPath;
	
	public BlameHandler(){
		this.blames = new ArrayList<Blame>();
	}

	@Override
	public void handleEOF() {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleLine(Date date, long revision, String author, String line) throws SVNException {
		handleLine(date, revision, author, line, null, -1, null, null, -1);
	}

	@Override
	public void handleLine(Date date, long revision, String author, String line, Date mergedDate, long mergedRevision, String mergedAuthor, String mergedPath, int lineNumber) throws SVNException {
		if(revision != -1 && !(isComment(line))) {
			Blame blame = new Blame();
			blame.setRevision(revision);
			blame.setAuthor(author);
			blame.setDate(date);
			blame.setLine(line);
			blame.setChangedPath(changedPath);
			blame.setLineNumber(lineNumber);
			blames.add(blame);
		}
	}

	@Override
	public boolean handleRevision(Date date, long revision, String author, File contents) throws SVNException {
		return false;
	}
	
	public List<Blame> getBlameList() {
		return blames;
	}

	public void setBlameList(List<Blame> blameList) {
		this.blames = blameList;
	}

	public ChangedPath getChangedPath() {
		return changedPath;
	}

	public void setChangedPath(ChangedPath changedPath) {
		this.changedPath = changedPath;
	}
	
	private boolean isComment(String line) {
		if(line.trim().length() == 0) return true; 

		boolean result;
		Pattern pattern = Pattern.compile("(?<!.+)^//.+$");
		Matcher matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)//(?!.+)");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)\\*(?!.+)");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^/\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\s+\\*.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("(?<!.+)^\\*+/.+$");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		pattern = Pattern.compile("^C:");
		matcher = pattern.matcher(line.trim());

		result = matcher.find();
		if(result) return true;

		return false;
	}
}
