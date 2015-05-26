package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.util.List;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractChangedPathDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.ChangedPath;

public class HibernateChangedPathDAO extends HibernateGenericDAO<ChangedPath, Long> implements AbstractChangedPathDAO {
	
	public List<ChangedPath> getChangedPathByCommitRevision(Long revision){
		String sql = "from ChangedPath " +
					 "where commit_id = :revision";
		getSession().clear();
		getSession().flush();
		Query query = getSession().createQuery(sql);
		query.setParameter("revision", revision);
		@SuppressWarnings("unchecked")
		List<ChangedPath> changedPaths =  query.list();
		
		return changedPaths;
	}

}
