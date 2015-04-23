package br.ufrn.ppgsc.backhoe.persistence.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@SuppressWarnings("deprecation")
public class HibernateUtil {  
   
	private static final SessionFactory sessionFactory;  
      
    static {  
        sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();          
    }  
      
    public static Session getSession(){  
        return sessionFactory.openSession();  
    }
}