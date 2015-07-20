package br.ufrn.ppgsc.backhoe.persistence.model;

public class Main {
	
	public enum RepositoryType{
		
		GIT(1), SVN(2);
		
		private final int type;
		
		RepositoryType(int type) { this.type = type; }
	    public int getValue() { return type; }
	}
	
	public static void main(String[] args) {
		
		System.out.println(RepositoryType.GIT);
		
	}

}
