package br.ufrn.ppgsc.backhoe.miner;

import java.util.Date;
import java.util.List;

import br.ufrn.ppgsc.backhoe.exceptions.DAONotFoundException;
import br.ufrn.ppgsc.backhoe.exceptions.MissingParameterException;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOFactory;
import br.ufrn.ppgsc.backhoe.persistence.dao.DAOType;
import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;
import br.ufrn.ppgsc.backhoe.repository.code.CodeRepository;
import br.ufrn.ppgsc.backhoe.vo.wrapper.AbstractFileRevisionWrapper;

public class CodeContributionMiner extends Miner {
	private Date startDate;
	private Date endDate;
	private List<String> developers;
	private CodeRepository repository;
	
	public CodeContributionMiner(CodeRepository repository, Date startDate, Date endDate, List<String> developers) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.developers = developers;
		this.repository = repository;
	}
	
	public CodeContributionMiner(CodeRepository repository, Date date, List<String> developers) {
		this.startDate = date;
		this.endDate = date;
		this.developers = developers;
		this.repository = repository;
	}
	
	@Override
	public boolean setup() throws MissingParameterException {
		if(repository == null) {
			throw new MissingParameterException("Missing mandatory parameter: CodeRepository repository");
		}
		if(startDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date startDate");
		}
		if(endDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date endDate");
		}
		
		return repository.connect();
	}

	@Override
	public void execute() {
		List<Commit> commits = null;
		if(developers == null) {
			commits = repository.findCommitsByTimeRange(startDate, endDate, true);
		} else {
			commits = repository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true);
		}
		try {
			AbstractCommitDAO commitDao = (AbstractCommitDAO) DAOFactory.createDAO(DAOType.COMMIT);
			commitDao.save(commits);
		} catch (DAONotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Commit commit : commits) {
			List<ChangedPath> changedPaths = commit.getChangedPaths();
			for (ChangedPath changedPath : changedPaths) {
				if(changedPath.getChangeType().equals('D')) {
					continue;
				}
				if(!(changedPath.getPath().toLowerCase().contains(".java"))) {
					continue;
				}
				
				List<AbstractFileRevisionWrapper> fileRevisions = (List<AbstractFileRevisionWrapper>) repository.findFileRevisions(changedPath.getPath(), 1L, changedPath.getCommit().getRevision());
				
				if(fileRevisions.size() > 1) {
					calculateMetricsBetweenRevisions(changedPath, fileRevisions);
				} else {
					calculateMetricsInThisRevision(changedPath, fileRevisions);
				}
				
			}
		}
	}

	private void calculateMetricsInThisRevision(ChangedPath changedPath, List<AbstractFileRevisionWrapper> fileRevisions) {
		// TODO Auto-generated method stub
		
	}

	private void calculateMetricsBetweenRevisions(ChangedPath changedPath, List<AbstractFileRevisionWrapper> fileRevisions) {
		// TODO Auto-generated method stub
		
	}

}
