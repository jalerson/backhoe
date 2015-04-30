package br.ufrn.ppgsc.backhoe.persistence.dao.abs;

import java.io.Serializable;
import java.util.List;

public interface AbstractDAO<T, ID extends Serializable> {
	T get(ID id);
	void save(T obj);
	void update(T obj);
	void delete(T obj);
	void save(List<T> commits);
	boolean saveOrUpdateAll(List<T> objects);
	T findByID(ID id);
	List<T> all();
}
