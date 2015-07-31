package br.ufrn.ppgsc.backhoe.persistence.model.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffChild {
	
	private String lineJustBefore;
	private String header;
	private List<String> additions;
	private List<String> removals;
	private String lineJustAfter;
	private List<Blame> blames;
	
	public DiffChild() {
		this.additions = new ArrayList<String>();
		this.removals = new ArrayList<String>();
		this.blames = new ArrayList<Blame>();
	}
	
	public DiffChild(String lineJustBefore, String lineJustAfter, 
			String headLine, List<String> additions, List<String> removals) {
		this.lineJustBefore = lineJustBefore;
		this.lineJustAfter = lineJustAfter;
		this.header = headLine;
		this.additions = additions;
		this.removals = removals;
		this.blames = new ArrayList<Blame>();
	}
	
	public String getLineJustBefore() {
		return lineJustBefore;
	}
	
	public void setLineJustBefore(String lineJustBefore) {
		this.lineJustBefore = lineJustBefore;
	}
	
	public String getHeader() {
		return header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public List<String> getAdditions() {
		return additions;
	}
	
	public void setAdditions(List<String> additions) {
		this.additions = additions;
	}
	
	public List<String> getRemovals() {
		return removals;
	}
	
	public void setRemovals(List<String> removals) {
		this.removals = removals;
	}
	
	public String getLineJustAfter() {
		return lineJustAfter;
	}
	
	public void setLineJustAfter(String lineJustAfter) {
		this.lineJustAfter = lineJustAfter;
	}
		
	public long getLineOfAddition() {
		String input = getHeader();
		Pattern pattern = Pattern.compile("\\+\\d+");
		
		Matcher matcher = pattern.matcher(input);
		
		matcher.find();
		String result = matcher.group();
		result = result.replaceFirst("\\+","");
		Long longResult = Long.parseLong(result);
		return longResult+3;
	}
	
	public long getLineOfRemove() {
		String input = getHeader();
		Pattern pattern = Pattern.compile("-\\d+");
		
		Matcher matcher = pattern.matcher(input);
		
		matcher.find();
		String result = matcher.group();
		result = result.replaceFirst("-","");
		Long longResult = Long.parseLong(result);
		return longResult+3;
	}

	public List<Blame> getBlames() {
		return blames;
	}

	public void setBlames(List<Blame> blames) {
		this.blames = blames;
	}

	@Override
	public String toString() {
		return "DiffChild [lineJustBefore=" + lineJustBefore + ", header="
				+ header + ", additions=" + additions + ", removals="
				+ removals + ", lineJustAfter=" + lineJustAfter + ", blames="
				+ blames + "]";
	}
}
