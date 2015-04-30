package br.ufrn.ppgsc.backhoe.persistence.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class MetricType extends Model {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	@Column(unique = true)
	private String slug;
	@OneToMany(mappedBy = "type")
	private List<Metric> metrics;
	
	public MetricType(){
		this(null, null);
	}
	
	public MetricType(String name, String slug) {
		super();
		this.name = name;
		this.slug = slug;
	}

	public Long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public List<Metric> getMetrics() {
		return metrics;
	}
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	@Override
	public String toString() {
		return "MetricType [id=" + id + ", slug=" + slug + "]";
	}
}
