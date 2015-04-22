package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.util.List;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;

public class HibernateCommitDAO extends HibernateGenericDAO<Commit> implements AbstractCommitDAO {

	@Override
	public Commit findByRevision(Long revision) {
		Query query = getSession().createQuery("from Commit c where c.revision = :revision");
		query.setParameter("revision", revision);
		return (Commit) query.uniqueResult();
	}

	@Override
	public void save(List<Commit> commits) {
		try{
			getSession().beginTransaction();
			for (Commit commit : commits)
				getSession().persist(commit);
			getSession().flush();
			getSession().getTransaction().commit();
		}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
	}
}
