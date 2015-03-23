package br.ufrn.ppgsc.backhoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.miner.CodeContributionMiner;
import br.ufrn.ppgsc.backhoe.repository.RepositoryFactory;
import br.ufrn.ppgsc.backhoe.repository.RepositoryType;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.util.PropertiesUtil;

public class RunCodeContributionMiner {

	public static void main(String[] args) {
		Properties svnProperties = PropertiesUtil.getProperties("svn.properties");
		
		CodeRepository repository = (CodeRepository) RepositoryFactory.createRepository(RepositoryType.SVN);
		repository.setUsername(svnProperties.getProperty("username"));
		repository.setPassword(svnProperties.getProperty("password"));
		repository.setURL(svnProperties.getProperty("url"));
		
		Date startDate = new Date(Date.parse("2014-11-03"));
		Date endDate = new Date(Date.parse("2014-11-03"));
		ArrayList<String> developers = new ArrayList(Arrays.asList(new String[]{ "a", "b", "c" }));
		ArrayList<String> ignoredPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/LPS", "/ExemploIntegracaoSIAFI", "/branches" }));
		
		CodeContributionMiner miner = new CodeContributionMiner(repository, startDate, endDate, developers, ignoredPaths);
		try {
			miner.setup();
			miner.execute();
		} catch (MissingParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
