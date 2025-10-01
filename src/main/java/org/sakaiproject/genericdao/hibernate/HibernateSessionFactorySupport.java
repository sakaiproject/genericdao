package org.sakaiproject.genericdao.hibernate;

import java.util.function.Function;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

/**
 * Minimal support class that manages a {@link SessionFactory} and provides helper methods
 * for executing work within the current Hibernate {@link Session} while translating
 * {@link HibernateException}s into Spring's {@link DataAccessException} hierarchy.
 */
public abstract class HibernateSessionFactorySupport implements InitializingBean {

   private SessionFactory sessionFactory;

   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   protected SessionFactory getSessionFactory() {
      if (sessionFactory == null) {
         throw new IllegalStateException("SessionFactory has not been set");
      }
      return sessionFactory;
   }

   protected Session currentSession() throws DataAccessException {
      try {
         return getSessionFactory().getCurrentSession();
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
   }

   protected <T> T execute(Function<Session, T> callback) throws DataAccessException {
      Session session = currentSession();
      try {
         return callback.apply(session);
      } catch (HibernateException e) {
         throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
   }

   @Override
   public void afterPropertiesSet() {
      if (this.sessionFactory == null) {
         throw new IllegalArgumentException("Property 'sessionFactory' is required");
      }
   }
}
