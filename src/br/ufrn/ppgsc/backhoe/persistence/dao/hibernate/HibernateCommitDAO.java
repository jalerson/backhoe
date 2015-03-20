package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;

public class HibernateCommitDAO extends HibernateGenericDAO<Commit> implements AbstractCommitDAO {

	@Override
	public Commit findByRevision(Long revision) {
		Query query = session.createQuery("from Commit c where c.revision = :revision");
		query.setParameter("revision", revision);
		return (Commit) query.uniqueResult();
	}

	@Override
	public void save(List<Commit> commits) {
		Transaction tx = session.beginTransaction();
		tx.begin();
		for (Commit commit : commits) {
			super.save(commit);
		}
		tx.commit();
	}

}
