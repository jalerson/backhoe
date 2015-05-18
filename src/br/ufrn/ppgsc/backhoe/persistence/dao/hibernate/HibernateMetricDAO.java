package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.sql.Date;

import org.hibernate.Query;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractMetricDAO;
import br.ufrn.ppgsc.backhoe.persistence.model.Metric;

public class HibernateMetricDAO extends HibernateGenericDAO<Metric, Long> implements AbstractMetricDAO {
	
	public boolean existsMetricToType(Long objectID, Long metricTypeID){
		String sql = "from Metric m, MetricType mt " +
				     "where m.type.id = mt.id and " +
				     "m.objectId = :objectId and " +
				     "mt.id = :metricTypeID";
		Query query = getSession().createQuery(sql);
		query.setMaxResults(1);
		query.setParameter("objectId", objectID);
		query.setParameter("metricTypeID", metricTypeID);
		
		return query.uniqueResult() == null? false: true;
	}
	
	public boolean existsMetric(Long objectID, String minerSlug){
		String sql = "from Metric m " +
				     "where m.objectId = :objectId and " +
				     "m.minerSlug = :minerSlug";
		Query query = getSession().createQuery(sql);
		query.setMaxResults(1);
		query.setParameter("objectId", objectID);
		query.setParameter("minerSlug", minerSlug);
		
		return query.uniqueResult() == null? false: true;
	}
	
	public boolean existsMetric(Long objectID, String minerSlug, Date startDateInterval, Date endDateInterval){
		String sql = "from Metric m " +
				     "where m.objectId = :objectId and " +
				     "m.minerSlug = :minerSlug and " +
				     "m.startDateInterval = :startDateInterval and " +
				     "m.endDateInterval = :endDateInterval";
		Query query = getSession().createQuery(sql);
		query.setMaxResults(1);
		query.setParameter("objectId", objectID);
		query.setParameter("minerSlug", minerSlug);
		query.setParameter("startDateInterval", startDateInterval);
		query.setParameter("endDateInterval", endDateInterval);
		
		return query.uniqueResult() == null? false: true;
	}
}
