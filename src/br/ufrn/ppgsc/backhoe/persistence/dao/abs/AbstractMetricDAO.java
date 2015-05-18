package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import java.sql.Date;

import br.ufrn.ppgsc.backhoe.persistence.model.Metric;

public interface AbstractMetricDAO extends AbstractDAO<Metric, Long> {
	
	boolean existsMetricToType(Long objectID, Long metricTypeID);
	boolean existsMetric(Long objectID, String minerSlug);
	boolean existsMetric(Long objectID, String minerSlug, Date startDateInterval, Date endDateInterval);

}
