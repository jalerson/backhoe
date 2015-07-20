package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractCommitDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Commit;

public class HibernateCommitDAO extends HibernateGenericDAO<Commit, Long> implements AbstractCommitDAO {

	@Override
	public Commit findByRevision(String revision) {
		Query query = getSession().createQuery("from Commit c where c.revision = :revision");
		query.setParameter("revision", revision);
		return (Commit) query.uniqueResult();
	}
}
