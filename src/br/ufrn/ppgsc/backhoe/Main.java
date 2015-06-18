package br.ufrn.ppgsc.backhoe;

import java.io.File;
import java.sql.Date;

import br.ufrn.ppgsc.backhoe.enums.Team;

public class Main {

	public static void main(String[] args) {
		
		File csvResult = RunCodeContributionMiner.generateCSVWithMetrics(Team.SIGAA, Date.valueOf("2015-02-09"), Date.valueOf("2015-02-10"));
		
		System.exit(0);
		
	}
}
