package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricTypeDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;

public class HibernateMetricTypeDAO extends HibernateGenericDAO<MetricType> implements AbstractMetricTypeDAO {

	@Override
	public MetricType findBySlug(String slug) {
		Query query = session.createQuery("from MetricType m where m.slug = :slug");
		query.setParameter("slug", slug);
		return (MetricType) query.uniqueResult();
	}

}
