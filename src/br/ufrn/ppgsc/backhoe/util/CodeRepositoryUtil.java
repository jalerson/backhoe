package br.ufrn.ppgsc.backhoe.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeRepositoryUtil {
	public static Long getPreviousRevision(Long currentRevision, List<Long> fileRevisions) {
		int index = fileRevisions.indexOf(currentRevision);
		return fileRevisions.get(index - 1);
	}
	
	public static List<String> getContentByLines(String in) {
		return Arrays.asList(in.split("[\r\n]+"));
	}
	
	public static boolean isComment(String line) {
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
		//pattern = Pattern.compile("(?<!.+)}(?!.+)");
		//matcher = pattern.matcher(line.trim());

		//result = matcher.find();
		//if(result) return true;

		return false;
	}
}
