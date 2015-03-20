package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import java.util.List;

public interface AbstractDAO<T> {
	public T get(Long id);
	public void save(T obj);
	public void update(T obj);
	public void delete(T obj);
	public List<T> all();
}
