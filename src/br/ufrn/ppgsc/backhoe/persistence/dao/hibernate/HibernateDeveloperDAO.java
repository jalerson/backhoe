package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;

public class HibernateDeveloperDAO extends HibernateGenericDAO<Developer> implements AbstractDeveloperDAO {

	@Override
	public Developer findByCodeRepositoryUsername(String username) {
		Query query = getSession().createQuery("from Developer u where u.codeRepositoryUsername = :username");
		query.setParameter("username", username);
		return (Developer) query.uniqueResult();
	}
	
}
