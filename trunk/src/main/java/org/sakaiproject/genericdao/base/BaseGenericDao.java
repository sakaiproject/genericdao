/**
 * $Id$
 * $URL$
 * JdbcGenericDao.java - genericdao - Apr 18, 2008 10:07:08 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.genericdao.api.GenericDao;
import org.sakaiproject.genericdao.api.caching.CacheProvider;
import org.sakaiproject.genericdao.api.interceptors.DaoOperationInterceptor;
import org.sakaiproject.genericdao.api.interceptors.ReadInterceptor;
import org.sakaiproject.genericdao.api.interceptors.WriteInterceptor;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.base.caching.NonCachingCacheProvider;

/**
 * This is the simple base implementation which includes shared methods and should be extended,
 * this handles the caching checks and the interceptors on the basic methods
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public abstract class BaseGenericDao implements GenericDao {

   /**
    * MUST be overridden
    */
   protected <T> T baseFindById(Class<T> type, Serializable id) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST be overridden
    */
   protected Serializable baseCreate(Class<?> type, Object object) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST be overridden
    */
   protected void baseUpdate(Class<?> type, Object id, Object object) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST be overridden
    */
   protected <T> boolean baseDelete(Class<T> type, Serializable id) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * Get the id value from a persistent object,
    * this expects that the object is persistent so it should already be checked<br/>
    * Override this if desired
    * @param entity any persistent object
    * @return the id value or null if it is unset
    */
   protected Serializable baseGetIdValue(Object entity) {
      Class<?> type = findClass(entity);
      String idProp = getIdProperty(type);
      Object idValue = null;
      if (idProp == null) {
         throw new IllegalArgumentException("Could not find id property for this class type: " + type);
      } else {
         idValue = ReflectUtils.getInstance().getFieldValue(entity, idProp);
      }
      Serializable serialId = null;
      if (idValue != null) {
         if (! (idValue instanceof Serializable)) {
            serialId = idValue.toString();
         }
      }
      return serialId;
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
      Class<?> type = entity.getClass();
      return type;
   }

   // COMMON CODE

   private List<Class<?>> classes;
   protected void setClasses(List<Class<?>> classes) {
      this.classes = classes;
   }

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
