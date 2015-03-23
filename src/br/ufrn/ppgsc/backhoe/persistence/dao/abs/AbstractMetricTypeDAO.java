package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import br.ufrn.ppgsc.backhoe.persistence.model.MetricType;

public interface AbstractMetricTypeDAO extends AbstractDAO<MetricType> {

	public MetricType findBySlug(String slug);

}
