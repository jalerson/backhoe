package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDeveloperDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Developer;

public class HibernateDeveloperDAO extends HibernateGenericDAO<Developer, Long> implements AbstractDeveloperDAO {

	@Override
	public Developer findByCodeRepositoryUsername(String username) {
		getSession().clear();
		getSession().flush();
		Query query = getSession().createQuery("from Developer u where u.codeRepositoryUsername = :username");
		query.setParameter("username", username);
		return (Developer) query.uniqueResult();
	}
	
	@Override
	public Developer findByEmail(String email) {
		getSession().clear();
		getSession().flush();
		Query query = getSession().createQuery("from Developer u where u.email = :email");
		query.setParameter("email", email);
		return (Developer) query.uniqueResult();
	}

	@Override
	public Developer findByCodeRepositoryUsernameOrEmail(String usernameOrEmail) {
		getSession().clear();
		getSession().flush();
		Query query = getSession().createQuery("from Developer u where u.email = :email or u.codeRepositoryUsername = :username ");
		query.setParameter("email", usernameOrEmail);
		query.setParameter("username", usernameOrEmail);
		return (Developer) query.uniqueResult();
	}
}