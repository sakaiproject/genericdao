/******************************************************************************
 * HibernateGenericDaoImpl.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2006 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - Project Lead
 * 
 *****************************************************************************/

package org.sakaiproject.genericdao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.azeckoski.reflectutils.ClassLoaderUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.metadata.ClassMetadata;
import org.sakaiproject.genericdao.api.GenericDao;
import org.sakaiproject.genericdao.api.caching.CacheProvider;
import org.sakaiproject.genericdao.api.interceptors.DaoOperationInterceptor;
import org.sakaiproject.genericdao.api.interceptors.ReadInterceptor;
import org.sakaiproject.genericdao.api.interceptors.WriteInterceptor;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.base.caching.NonCachingCacheProvider;
import org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * A Hibernate (http://hibernate.org/) based implementation of GenericDao
 * which can be extended to add more specialized DAO methods.
 * <p>
 * Note: This implementation is so simple it is unlikely to be useful
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HibernateGenericDao extends HibernateDaoSupport implements GenericDao {

   protected final static String COUNTALL_QUERY = "select count(*) ";
   protected final static String START_QUERY = "from";

   /**
    * Set the list of persistent classes that this DAO will handle<br/>
    * This no longer requires this to be set before the class initializes
    * Do this using Spring like so:
    * <xmp>
      <property name="persistentClasses">
         <list>
            <value>org.sakaiproject.*yourappname*.model.*YourObject*</value>
         </list>
      </property>
    * </xmp>
    * @param classes a list of Strings representing the
    * the fully qualified classpath and classname of persistent objects
    */
   public void setPersistentClasses(List<String> classes) {
      if (classes.size() == 0) {
         throw new IllegalArgumentException("persistent class list must have at least one item");
      }
      List<Class<?>> classList = new ArrayList<Class<?>>();
      for (Iterator<String> i = classes.iterator(); i.hasNext();) {
         String className = i.next();
         Class<?> c = ClassLoaderUtils.getClassFromString(className);
         if (c == null) {
            throw new IllegalArgumentException(
                  "Invalid class type in list of persistent classes: " + className);
         }
         classList.add(c);
      }
      this.classes = classList;
   }


   /**
    * Take a list of persistent hibernate objects and return non-persistent clones
    * @param items a list of persistent objects
    * @return a list of non-persistent cloned objects
    */
   protected <T> List<T> cloneList(List<T> items) {
      if (items.isEmpty()) { return items; }
      List<T> clonedItems = new ArrayList<T>();
      for (T item : items) {
         // create a clone of each hibernate object and pass back the clone only,
         // cloning only the bottom level of data
         clonedItems.add( ReflectUtils.getInstance().clone(item, 0, null) );
      }
      return clonedItems;
   }

   /*
    * Add some convenience methods here
    */

   /**
    * This is a convenience method that DAO writers can use to do counts using any HQL,
    * it is efficient in that it does not return all objects and only does the count
    * 
    * @param hqlQuery any Hibernate HQL query of the format "from Blah..." or "select thing from Blah..."
    * @return the number of items that would have been returned
    */
   protected int count(String hqlQuery) {
      String newHqlQuery = buildCountHQL(hqlQuery);
      return ((Number) getHibernateTemplate().iterate(newHqlQuery).next()).intValue();
   }

   /**
    * This is a convenience method that DAO writers can use to do counts using any HQL,
    * it is efficient in that it does not return all objects and only does the count
    * 
    * @param hqlQuery any Hibernate HQL query of the format "from Blah..." or "select thing from Blah..."
    * @param params an array of values which are represented as "?" in the hql
    * @return the number of items that would have been returned
    */
   protected int count(String hqlQuery, Object[] params) {
      String newHqlQuery = buildCountHQL(hqlQuery);
      return ((Number) getHibernateTemplate().iterate(newHqlQuery, params).next()).intValue();
   }

   /**
    * Builds count HQL from an HQL query
    * 
    * @param hqlQuery an hql query
    * @return the HQL to do a count
    */
   private String buildCountHQL(String hqlQuery) {
      if (hqlQuery == null) {
         throw new IllegalArgumentException("hqlQuery cannot be null");
      }
      hqlQuery = hqlQuery.trim();

      // clean HQL up in case it looks like this: select blah from Blah
      int fromLoc = hqlQuery.indexOf(START_QUERY);
      if (fromLoc == -1) {
         throw new IllegalArgumentException("HQL appears to be invalid: "
               + "query does not start with the string: " + START_QUERY);
      }

      // get rid of the sort by since this will cause the count query to fail
      int sortLoc = hqlQuery.indexOf("sort by");
      if (sortLoc == -1) { sortLoc = hqlQuery.length(); }


      String newHqlQuery = hqlQuery.substring(fromLoc, sortLoc);
      if (fromLoc == 0) {
         newHqlQuery = COUNTALL_QUERY + newHqlQuery;
      } else {
         // make sure we keep the "blah" part and just put count() around it: select count(blah) from Blah
         String oldPrefix = hqlQuery.substring(0, fromLoc);
         String[] parts = oldPrefix.split(" ");
         if (parts.length > 1) {
            String prefix = "select count(" + parts[1] + ") ";
            newHqlQuery = prefix + newHqlQuery;
         }
      }
      return newHqlQuery;
   }


   /**
    * Generates the HQL snippet needed to represent this property/comparison/value triple,
    * will turn the value into a string, use {@link #makeComparisonHQL(Map, String, int, Object)}
    * if you want to deliver the object directly
    * 
    * @param property the name of the entity property
    * @param comparisonConstant the comparison constant (e.g. EQUALS)
    * @param value the value to compare the property to
    * @return a string representing the HQL snippet (e.g. propA = 'apple')
    */
   protected String makeComparisonHQL(String property, int comparisonConstant, Object value) {
      String sval = null;
      if (comparisonConstant != Restriction.NOT_NULL && comparisonConstant != Restriction.NULL) {
         if (value.getClass().isAssignableFrom(Boolean.class) 
               || value.getClass().isAssignableFrom(Number.class)) {
            // special handling for boolean and numbers
            sval = value.toString();
         } else {
            sval = "'" + value.toString() + "'";
         }
      }
      return buildComparisonHQL(property, comparisonConstant, sval);
   }

   /**
    * @param params a set of params to add this value to
    * @param property the name of the entity property
    * @param comparisonConstant the comparison constant (e.g. EQUALS)
    * @param value the value to compare the property to
    * @return a string representing the HQL snippet (e.g. propA = :propA)
    */
   protected String makeComparisonHQL(Map<String, Object> params, String property, int comparisonConstant, Object value) {
      if (comparisonConstant != Restriction.NOT_NULL && comparisonConstant != Restriction.NULL) {
         params.put(property, value);
      }
      return buildComparisonHQL(property, comparisonConstant, ":" + property);
   }

   /**
    * @param property
    * @param comparisonConstant
    * @param sval
    * @return
    */
   private String buildComparisonHQL(String property, int comparisonConstant, String sval) {
      switch (comparisonConstant) {
      case Restriction.EQUALS:      return property + " = " + sval;
      case Restriction.GREATER:     return property + " > " + sval;
      case Restriction.LESS:        return property + " < " + sval;
      case Restriction.LIKE:        return property + " like " + sval;
      case Restriction.NOT_EQUALS:  return property + " <> " + sval;
      case Restriction.NOT_NULL:    return property + " is not null";
      case Restriction.NULL:        return property + " is null";
      default: throw new IllegalArgumentException("Invalid comparison constant: " + comparisonConstant);
      }
   }


   /**
    * Provides an easy way to execute an HQL query with ? params
    * 
    * @param hql a hibernate query language query
    * @param params an array of values (you should have "?" in your HQL whereever the values will be inserted)
    * @param start the entry number to start on (based on current sort rules), first entry is 0
    * @param limit the maximum number of entries to return, 0 returns as many entries as possible
    * @return a list of whatever you requested in the HQL
    */
   protected List<?> executeHqlQuery(String hql, Object[] params, int start, int limit) {
      Query query = getSessionFactory().getCurrentSession().createQuery(hql);
      query.setFirstResult(start);
      if (limit > 0) {
         query.setMaxResults(limit);
      }
      for (int i = 0; i < params.length; i++) {
         query.setParameter(i, params[i]);
      }
      return query.list();
   }

   /**
    * Provides an easy way to execute an HQL query with named parameters
    * 
    * @param hql a hibernate query language query
    * @param params the map of named parameters
    * @param start the entry number to start on (based on current sort rules), first entry is 0
    * @param limit the maximum number of entries to return, 0 returns as many entries as possible
    * @return a list of whatever you requested in the HQL
    */
   protected List<?> executeHqlQuery(String hql, Map<String, Object> params, int start, int limit) {
      Query query = getSessionFactory().getCurrentSession().createQuery(hql);
      query.setFirstResult(start);
      if (limit > 0) {
         query.setMaxResults(limit);
      }
      setParameters(query, params);
      return query.list();
   }

   /**
    * This is supported natively in Hibernate 3.2.x and up<br/>
    * sets the parameters correctly for hibernate 3.1
    * 
    * @param query
    * @param params
    */
   protected void setParameters(Query query, Map<String, Object> params) {
      for (Entry<String, Object> entry : params.entrySet()) {
         String name = entry.getKey();
         Object param = entry.getValue();
         if (param.getClass().isArray()) {
            query.setParameterList(name, (Object[]) param);
         } else {
            query.setParameter(name, param);
         }
      }
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.GenericDao#getIdProperty(java.lang.Class)
    */
   public String getIdProperty(Class<?> entityClass) {
      ClassMetadata classmeta = getSessionFactory().getClassMetadata(entityClass);
      if (classmeta == null) return null;
      return classmeta.getIdentifierPropertyName();
   }

   // OVERRIDES

   /**
    * Use hibernate metadata to get the id value
    */
   protected Serializable baseGetIdValue(Object object) {
      Class<?> type = findClass(object);
      Serializable idValue = null;
      ClassMetadata classmeta = getSessionFactory().getClassMetadata(type);
      if (classmeta != null) {
         if (classmeta.hasIdentifierProperty()) {
            idValue = classmeta.getIdentifier(object);
         }
      } else {
         throw new IllegalArgumentException("Could not get classmetadata for this object, it may not be persistent: " + object);
      }
      return idValue;
   }

   /**
    * MUST be overridden
    */
   @SuppressWarnings("unchecked")
   protected <T> T baseFindById(Class<T> type, Serializable id) {
      T entity = null;
      try {
         entity = (T) getHibernateTemplate().get(type, id);
      } catch (HibernateObjectRetrievalFailureException e) {
         // TODO - maybe log an error here or take this out later
         // if they fix the get to not throw a nasty error
         entity = null;
      }
      return entity;
   }

   /**
    * MUST be overridden
    */
   protected Serializable baseCreate(Class<?> type, Object object) {
      // this ugly but hibernate will save an object which is already persistent so we have to do this check up front
      Serializable id;
      try {
         id = getSessionFactory().getCurrentSession().getIdentifier(object);
      } catch (HibernateException e) {
         id = null;
      }
      if (id != null) {
         throw new IllegalArgumentException("This object is already persistent with id: " + id 
               + " - you must use update to save this object and not create");
      }
      id = getHibernateTemplate().save(object);
      return id;
   }

   /**
    * MUST be overridden
    */
   protected void baseUpdate(Class<?> type, Object id, Object object) {
      getHibernateTemplate().update(object);
   }

   /**
    * MUST be overridden
    */
   protected <T> boolean baseDelete(Class<T> type, Serializable id) {
      boolean deleted = false;
      Object object = baseFindById(type, id);
      if (object != null) {
         getHibernateTemplate().delete(object);
         deleted = true;
      }
      return deleted;
/** This will not flush the item from the session so it is hopeless -AZ
      String query = "delete from " + type.getName() + " where " 
            + getIdProperty(type) + "= ?";
      int i = getHibernateTemplate().bulkUpdate(query, id);
      boolean deleted = false;
      if (i > 0) {
         getSessionFactory().evict(type, id); //evict this item from the cache
         deleted = true;
      }
      return deleted;
**/
   }

   /**
    * Find the class type of a persistent object,
    * needed in the case that we are working with something that wraps it's objects<br/>
    * Override this if desired
    * @param entity a persistent entity
    * @return the persistent class type OR null if it cannot be found
    */
   protected Class<?> findClass(Object entity) {
      if (entity == null) {
         throw new IllegalArgumentException("Cannot find class type of null entity object");
      }
      Class<?> type = Hibernate.getClass(entity);
      return type;
   }

   // COMMON CODE

   private List<Class<?>> classes;
   /**
    * This does a nice bit of exception handling for us and verifies that
    * this class is valid to perform a DAO operation with
    * @param type class type of the persistent object to check
    * @return A valid entityClass type resolved to be the same as
    * the ones usable by this DAO
    */
   protected Class<?> checkClass(Class<?> type) {
      if (type == null) {
         throw new NullPointerException("type cannot be null");
      }

      if (classes == null) {
         throw new NullPointerException("persistent classes must be set");
      }

      for (Iterator<Class<?>> i = classes.iterator(); i.hasNext();) {
         Class<?> concrete = (Class<?>) i.next();
         if (concrete.isAssignableFrom(type)) {
            return concrete;
         }
      }
      throw new IllegalArgumentException("Could not resolve this class " + 
            type + " as part of the set of persistent objects: " +
            classes.toString());
   }

   private CacheProvider cacheProvider;
   /**
    * @return the current cache provider
    */
   protected CacheProvider getCacheProvider() {
      if (cacheProvider == null) {
         cacheProvider = new NonCachingCacheProvider();
      }
      return cacheProvider;
   }
   /**
    * Set the cache provider to an implementation of {@link CacheProvider},
    * this will be set to {@link NonCachingCacheProvider} if this is not set explicitly
    * @param cacheProvider
    */
   public void setCacheProvider(CacheProvider cacheProvider) {
      this.cacheProvider = cacheProvider;
   }
   /**
    * @return the cache name used for storing this type of persistent object
    */
   protected String getCacheName(Class<?> type) {
      if (type == null) {
         throw new IllegalArgumentException("type cannot be null");
      }
      return type.getName();
   }
   /**
    * @return the cachename used for storing search results for this type of object
    */
   protected String getSearchCacheName(Class<?> type) {
      if (type == null) {
         throw new IllegalArgumentException("type cannot be null");
      }
      return "search:" + type.getName();
   }
   /**
    * Creates all the caches for the current set of persistent types,
    * this should be called in the init for the generic dao being used after persistent classes are loaded
    */
   protected void initCaches() {
      for (Class<?> type : classes) {
         getCacheProvider().createCache(getCacheName(type));
         getCacheProvider().createCache(getSearchCacheName(type));
      }
   }

   // INTERCEPTOR methods

   private Map<Class<?>, ReadInterceptor> readInterceptors = new ConcurrentHashMap<Class<?>, ReadInterceptor>();
   private Map<Class<?>, WriteInterceptor> writeInterceptors = new ConcurrentHashMap<Class<?>, WriteInterceptor>();
   /**
    * Adds the provided interceptor to the current set of interceptors
    * @param interceptor
    */
   public void addInterceptor(DaoOperationInterceptor interceptor) {
      if (interceptor != null) {
         Class<?> type = interceptor.interceptType();
         if (type != null) {
            if (ReadInterceptor.class.isAssignableFrom(interceptor.getClass())) {
               readInterceptors.put(type, (ReadInterceptor) interceptor);
            }
            if (WriteInterceptor.class.isAssignableFrom(interceptor.getClass())) {
               writeInterceptors.put(type, (WriteInterceptor) interceptor);
            }
         }
      }
   }
   /**
    * Removes the provided interceptor from the current set of interceptors
    * @param interceptor
    */
   public void removeInterceptor(DaoOperationInterceptor interceptor) {
      if (interceptor != null) {
         Class<?> type = interceptor.interceptType();
         if (type != null) {
            if (ReadInterceptor.class.isAssignableFrom(interceptor.getClass())) {
               readInterceptors.remove(type);
            }
            if (WriteInterceptor.class.isAssignableFrom(interceptor.getClass())) {
               writeInterceptors.remove(type);
            }
         }
      }
   }
   /**
    * Makes the given interceptor the only active interceptor,
    * removes all others and adds only this one
    * @param interceptor
    */
   public void setInterceptor(DaoOperationInterceptor interceptor) {
      if (interceptor != null) {
         readInterceptors.clear();
         writeInterceptors.clear();
         addInterceptor(interceptor);
      }
   }

   protected void beforeRead(String operation, Class<?> type, Serializable[] ids, Search search) {
      if (type != null) {
         ReadInterceptor interceptor = readInterceptors.get(type);
         if (interceptor != null) {
            interceptor.beforeRead(operation, ids, search);
         }
      }
   }

   protected void afterRead(String operation, Class<?> type, Serializable[] ids, Search search, Object[] entities) {
      if (type != null) {
         ReadInterceptor interceptor = readInterceptors.get(type);
         if (interceptor != null) {
            interceptor.afterRead(operation, ids, search, entities);
         }
      }
   }

   protected void beforeWrite(String operation, Class<?> type, Serializable[] ids, Object[] entities) {
      if (type != null) {
         WriteInterceptor interceptor = writeInterceptors.get(type);
         if (interceptor != null) {
            interceptor.beforeWrite(operation, ids, entities);
         }
      }
   }

   protected void afterWrite(String operation, Class<?> type, Serializable[] ids, Object[] entities, int changes) {
      if (type != null) {
         WriteInterceptor interceptor = writeInterceptors.get(type);
         if (interceptor != null) {
            interceptor.afterWrite(operation, ids, entities, changes);
         }
      }
   }

   // ********* PUBLIC methods ****************


   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.GenericDao#getPersistentClasses()
    */
   public List<Class<?>> getPersistentClasses() {
      return new ArrayList<Class<?>>(classes);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.GenericDao#invokeTransactionalAccess(java.lang.Runnable)
    */
   public void invokeTransactionalAccess(Runnable toinvoke) {
      toinvoke.run();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.GenericDao#findById(java.lang.Class, java.io.Serializable)
    */
   @SuppressWarnings("unchecked")
   public <T> T findById(Class<T> type, Serializable id) {
      checkClass(type);
      if (id == null) {
         throw new IllegalArgumentException("id must be set to find persistent object");
      }

      T entity = null;
      // check cache first
      String key = id.toString();
      String cacheName = getCacheName(type);
      if (getCacheProvider().exists(cacheName, key)) {
         entity = (T) getCacheProvider().get(cacheName, key);
      } else {
         // not in cache so go to the DB
   
         // before interceptor
         String operation = "findById";
         beforeRead(operation, type, new Serializable[] {id}, null);
   
         entity = baseFindById(type, id);
   
         // now put the item in the cache
         getCacheProvider().put(cacheName, key, entity);
   
         // after interceptor
         afterRead(operation, type, new Serializable[] {id}, null, new Object[] {entity});
      }
      return entity;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#create(java.lang.Object)
    */
   public void create(Object object) {
      Class<?> type = findClass(object);
      checkClass(type);

      String operation = "create";
      beforeWrite(operation, type, null, new Object[] {object});

      Serializable idValue = baseCreate(type, object);

      // clear the search caches
      getCacheProvider().clear(getSearchCacheName(type));

      afterWrite(operation, type, new Serializable[] {idValue}, new Object[] {object}, 1);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#update(java.lang.Object)
    */
   public void update(Object object) {
      Class<?> type = findClass(object);
      checkClass(type);

      Serializable idValue = baseGetIdValue(object);
      if (idValue == null) {
         throw new IllegalArgumentException("Could not get an id value from the supplied object, cannot update without an id: " + object);
      }

      String operation = "update";
      beforeWrite(operation, type, new Serializable[] {idValue}, new Object[] {object});

      baseUpdate(type, idValue, object);

      // clear the search caches
      getCacheProvider().clear(getSearchCacheName(type));
      // clear the cache entry since this was updated
      String key = idValue.toString();
      String cacheName = getCacheName(type);
      getCacheProvider().remove(cacheName, key);

      afterWrite(operation, type, new Serializable[] {idValue}, new Object[] {object}, 1);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#save(java.lang.Object)
    */
   public void save(Object object) {
      Serializable id = baseGetIdValue(object);
      if (id == null) {
         create(object);
      } else {
         update(object);
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#delete(java.lang.Object)
    */
   public void delete(Object object) {
      Class<?> type = findClass(object);
      Serializable id = baseGetIdValue(object);
      delete(type, id);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#delete(java.lang.Class, java.io.Serializable)
    */
   public <T> boolean delete(Class<T> type, Serializable id) {
      checkClass(type);

      String operation = "delete";
      beforeWrite(operation, type, new Serializable[] {id}, null);

      boolean removed = baseDelete(type, id);

      if (removed) {
         // clear the search caches
         getCacheProvider().clear(getSearchCacheName(type));
         // clear this from the cache
         String key = id.toString();
         String cacheName = getCacheName(type);
         getCacheProvider().remove(cacheName, key);
         
         afterWrite(operation, type, new Serializable[] {id}, null, 1);
      }
      return removed;
   }

}
