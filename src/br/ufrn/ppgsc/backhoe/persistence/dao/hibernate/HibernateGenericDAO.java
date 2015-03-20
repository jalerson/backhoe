package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateGenericDAO<T> {
	protected Session session;
	
	public HibernateGenericDAO() {
		Configuration config = new Configuration();
		config.configure("hibernate.cfg.xml");
		SessionFactory factory = config.buildSessionFactory();
		session = factory.openSession();
	}
	
	public T get(Long id) {
        return (T) session.load(getTypeClass(), id);
    }
 
    public void save(T obj) {
        session.persist(obj);
    }
 
    public void update(T obj) {
    	session.merge(obj);
    }
 
    public void delete(T obj) {
    	session.delete(obj);
    }
 
    public List<T> all() {
        return session.createQuery(("FROM " + getTypeClass().getName())).list();
    }
	
	private Class<?> getTypeClass() {
        Class<?> clazz = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        return clazz;
    }
}
