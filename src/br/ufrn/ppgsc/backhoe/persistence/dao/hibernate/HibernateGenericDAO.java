package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import br.ufrn.ppgsc.backhoe.persistence.dao.abs.AbstractDAO;

public abstract class HibernateGenericDAO<T, ID extends Serializable> implements AbstractDAO<T, ID>{
	
	private Session session;
	
	@SuppressWarnings("deprecation")
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
	
	@SuppressWarnings("unchecked")
	public T get(ID id) {
        return (T) getSession().load(getTypeClass(), id);
    }
	 
	@SuppressWarnings("unchecked")
	public T findByID(ID id){
		getSession().clear();
		getSession().flush();
		Query query = getSession().createQuery("from "+getTypeClass().getName()+" where id = :id");
		query.setParameter("id", id);
		return (T) query.uniqueResult();
	}
 
    public void save(T obj) {
    	try{
	        getSession().clear();
			getSession().flush();
			getSession().beginTransaction();
	        getSession().persist(obj);
	        getSession().getTransaction().commit();
    	}catch (Exception e) {
    		e.printStackTrace();
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
    
    public void saveOrUpdate(T obj){
    	try{
    		getSession().clear();
    		getSession().flush();
	    	getSession().beginTransaction();
	        getSession().saveOrUpdate(obj);
	        getSession().getTransaction().commit();
    	}catch (Exception e) {
    		e.printStackTrace();
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
 
    public void update(T obj) {
    	try{
    		getSession().clear();
    		getSession().flush();
    		getSession().beginTransaction();
        	getSession().merge(obj);
        	getSession().getTransaction().commit();
    	}catch (Exception e) {
    		e.printStackTrace();
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
 
    public void delete(T obj) {
    	try{
    		getSession().clear();
    		getSession().flush();
	    	getSession().beginTransaction();
	    	getSession().delete(obj);
	    	getSession().getTransaction().commit();
    	}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
    }
 
    @SuppressWarnings("unchecked")
	public List<T> all() {
        return getSession().createQuery(("FROM " + getTypeClass().getName())).list();
    }
	
	private Class<?> getTypeClass() {
        Class<?> clazz = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return clazz;
    }

	@Override
	public boolean saveOrUpdateAll(List<T> objects) {
		boolean savedAll = true;
		try{
	    	getSession().beginTransaction();
	    	for(T object: objects)
	    		getSession().saveOrUpdate(object);
	    	getSession().flush();
	    	getSession().getTransaction().commit();
		}catch (Exception e) {
			savedAll = false;
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
		return savedAll;
	}
	
	@Override
	public void save(List<T> objects) {
		try{
			getSession().beginTransaction();
			for (T object : objects)
				getSession().persist(object);
			getSession().flush();
			getSession().getTransaction().commit();
		}catch (Exception e) {
			getSession().getTransaction().rollback();
		}finally{
			getSession().close();
		}
	}
}
