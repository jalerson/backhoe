package br.ufrn.ppgsc.backhoe.persistence.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Metric extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private Float value;
	private String objectType;
	private Long objectId;
	@ManyToOne
	private MetricType type;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public Long getObjectId() {
		return objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public MetricType getType() {
		return type;
	}
	public void setType(MetricType type) {
		this.type = type;
	}
}
