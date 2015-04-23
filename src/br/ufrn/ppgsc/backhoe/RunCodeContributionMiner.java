package br.ufrn.ppgsc.backhoe;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.miner.CodeComplexityMiner;
import br.ufrn.ppgsc.backhoe.miner.CodeContributionMiner;
import br.ufrn.ppgsc.backhoe.miner.Miner;
import br.ufrn.ppgsc.backhoe.repository.RepositoryFactory;
import br.ufrn.ppgsc.backhoe.repository.RepositoryType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.util.PropertiesUtil;

public class RunCodeContributionMiner {

	public static void main(String[] args) {
		Properties svnProperties = PropertiesUtil.getProperties("config/svn.properties");
		
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF); 
		
		CodeRepository repository = (CodeRepository) RepositoryFactory.createRepository(RepositoryType.SVN);
		repository.setUsername(svnProperties.getProperty("username"));
		repository.setPassword(svnProperties.getProperty("password"));
		repository.setURL(svnProperties.getProperty("url"));
		
		Date startDate = Date.valueOf("2015-02-09");
		Date endDate = Date.valueOf("2015-02-10");
		ArrayList<String> developers = new ArrayList(Arrays.asList(new String[]{"fulano", "sicrano", "beltrano"}));
		ArrayList<String> ignoredPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/LPS", "/ExemploIntegracaoSIAFI", "/branches" }));
		
		List<Miner> miners = new LinkedList<Miner>();
		
		miners.add(new CodeContributionMiner(repository, startDate, endDate, developers, ignoredPaths));
		miners.add(new CodeComplexityMiner(repository, startDate, endDate, developers, ignoredPaths));
		
		try {
			
			for(Miner miner: miners){
				miner.setup();
				miner.execute();
			}
			
		} catch (MissingParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}
}
