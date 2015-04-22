package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDAO;

public abstract class HibernateGenericDAO<T> implements AbstractDAO<T>{
	
	private Session session;
	
	public HibernateGenericDAO() {
		Configuration config = new Configuration();
		config.configure("hibernate.cfg.xml");
		SessionFactory factory = config.buildSessionFactory();
		session = factory.openSession();
	}
	
	 protected Session getSession(){  
        if(this.session == null || !this.session.isOpen()) 
            this.session = HibernateUtil.getSession();
        return this.session;  
    }
	
	public T get(Long id) {
        return (T) getSession().load(getTypeClass(), id);
    }
 
    public void save(T obj) {
    	try{
	    	getSession().beginTransaction();
	        getSession().persist(obj);
	        getSession().flush();
	        getSession().getTransaction().commit();
    	}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
 
    public void update(T obj) {
    	try{
    		getSession().beginTransaction();
        	getSession().merge(obj);
        	getSession().flush();
        	getSession().getTransaction().commit();
    	}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
		
    }
 
    public void delete(T obj) {
    	try{
	    	getSession().beginTransaction();
	    	getSession().delete(obj);
	    	getSession().flush();
	    	getSession().getTransaction().commit();
    	}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
 
    public List<T> all() {
        return getSession().createQuery(("FROM " + getTypeClass().getName())).list();
    }
	
	private Class<?> getTypeClass() {
        Class<?> clazz = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return clazz;
    }
}
