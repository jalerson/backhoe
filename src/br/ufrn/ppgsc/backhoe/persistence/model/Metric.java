package br.ufrn.ppgsc.backhoe.persistence.model;

import java.sql.Date;

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
	private Date startDateInterval;
	private Date endDateInterval;
	// Campo para salvar o slug do minerador que gerou a metrica
	private String minerSlug;
	
	public Metric(){
		
	}
	
	public Metric(Float value, String objectType, Long objectId,
			MetricType type, String minerSlug){
		this(value, objectType, objectId, type, minerSlug, null, null);
	}
	
	public Metric(Float value, String objectType, Long objectId,
			MetricType type, String minerSlug,
			Date startDateInterval, Date endDateInterval) {
		super();
		this.value = value;
		this.objectType = objectType;
		this.objectId = objectId;
		this.type = type;
		this.startDateInterval = startDateInterval;
		this.endDateInterval = endDateInterval;
		this.minerSlug = minerSlug;
	}

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
	public Date getStartDateInterval() {
		return startDateInterval;
	}
	public void setStartDateInterval(Date startDateInterval) {
		this.startDateInterval = startDateInterval;
	}
	public Date getEndDateInterval() {
		return endDateInterval;
	}
	public void setEndDateInterval(Date endDateInterval) {
		this.endDateInterval = endDateInterval;
	}
	public String getMinerSlug() {
		return minerSlug;
	}
	public void setMinerSlug(String minerSlug) {
		this.minerSlug = minerSlug;
	}

	@Override
	public String toString() {
		return "Metric [id=" + id + ", value=" + value + ", objectType="
				+ objectType + ", objectId=" + objectId + ", type=" + type
				+ ", startDateInterval=" + startDateInterval
				+ ", endDateInterval=" + endDateInterval + "]";
	}
}
