/**
 * $Id$
 * $URL$
 * BaseBasicGenericDao.java - genericdao - May 14, 2008 6:37:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.genericdao.api.BasicGenericDao;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * The simple base implementation for {@link BasicGenericDao},
 * this handles the caching checks and the interceptors on the basic methods
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@SuppressWarnings("deprecation")
public abstract class BaseBasicGenericDao extends BaseGenericDao implements BasicGenericDao {

   /**
    * MUST override this
    */
   protected <T> long baseCountBySearch(Class<T> type, Search search) {
      throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST override this
    */
   protected <T> List<T> baseFindBySearch(Class<T> type, Search search) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST override this
    */
   protected <T> T baseFindOneBySearch(Class<T> type, Search search) {
      T item = null;
      search.setLimit(1); // only return 1 item
      List<T> items = baseFindBySearch(type, search);
      if (items.size() > 0) {
         item = items.get(0);
      }
      return item;
   }


   // COMMON CODE
   
   public <T> long countBySearch(Class<T> type, Search search) {
      checkClass(type);
      if (search == null) {
          throw new IllegalArgumentException("search cannot be null");
      }
      long count = 0;

      // check the cache first
      boolean usedCache = false;
      String searchCacheName = getSearchCacheName(type);
      String cacheKey = "countBySearch::" + type.getName() + ":" + search.toString();
      if (getCacheProvider().exists(searchCacheName, cacheKey)) {
         Long lCount = (Long) getCacheProvider().get(searchCacheName, cacheKey);
         if (lCount != null) {
            count = lCount.longValue();
            usedCache = true;
         }
      }

      if (! usedCache) {
         count = baseCountBySearch(type, search);

         // cache the id results for the search
         getCacheProvider().put(searchCacheName, cacheKey, Long.valueOf(count));
      }
      return count;
   }

   @SuppressWarnings("unchecked")
   public <T> List<T> findBySearch(Class<T> type, Search search) {
      checkClass(type);
      if (search == null) {
          throw new IllegalArgumentException("search cannot be null");
      }
      List<T> results = new ArrayList<T>();

      // check the cache first
      boolean usedCache = false;
      String cacheName = getCacheName(type);
      String searchCacheName = getSearchCacheName(type);
      String cacheKey = "findBySearch::" + type.getName() + ":" + search.toString();
      if (getCacheProvider().exists(searchCacheName, cacheKey)) {
         String[] resultIds = (String[]) getCacheProvider().get(searchCacheName, cacheKey);
         if (resultIds != null) {
            for (int i = 0; i < resultIds.length; i++) {
               if (! getCacheProvider().exists(cacheName, resultIds[i])) {
                  usedCache = false;
                  break;
               }
               T entity = (T) getCacheProvider().get(cacheName, resultIds[i]);
               results.add(entity);
            }
            usedCache = true;
         }
      }

      if (! usedCache) {
         String operation = "findBySearch";
         beforeRead(operation, type, null, search);

         results = baseFindBySearch(type, search);

         // run through the returned items for the interceptor and for caching
         List<String> keys = new ArrayList<String>();
         for (T entity : results) {
            Object id = baseGetIdValue(entity);
            // cache each returned item
            String key = id.toString();
            keys.add(key);
            getCacheProvider().put(cacheName, key, entity);
         }
         // cache the id results for the search
         String[] ids = keys.toArray(new String[keys.size()]);
         getCacheProvider().put(searchCacheName, cacheKey, ids);
         // call the after interceptor
         afterRead(operation, type, ids, search, results.toArray(new Object[results.size()]));
      }
      return results;
   }

   @SuppressWarnings("unchecked")
   public <T> T findOneBySearch(Class<T> type, Search search) {
      checkClass(type);
      if (search == null) {
          throw new IllegalArgumentException("search cannot be null");
      }
      T entity = null;

      // check the cache first
      boolean usedCache = false;
      String cacheName = getCacheName(type);
      String searchCacheName = getSearchCacheName(type);
      String cacheKey = "findOneBySearch::" + type.getName() + ":" + search.toString();
      if (getCacheProvider().exists(searchCacheName, cacheKey)) {
         usedCache = true;
         String id = (String) getCacheProvider().get(searchCacheName, cacheKey);
         if (id != null) {
            if (getCacheProvider().exists(cacheName, id)) {
               entity = (T) getCacheProvider().get(cacheName, id);
            }
         }
      }

      if (! usedCache) {
         String operation = "findOneBySearch";
         beforeRead(operation, type, null, search);

         search.setLimit(1); // only return 1 item

         entity = baseFindOneBySearch(type, search);

         String key = null;
         if (entity != null) {
            Serializable id = baseGetIdValue(entity);
            afterRead(operation, type, new Serializable[] {id}, search, new Object[] {entity});

            if (id != null) {
               // cache the entity
               key = id.toString();
               getCacheProvider().put(cacheName, key, entity);
            }
         }
         // cache the search result
         getCacheProvider().put(searchCacheName, cacheKey, key);
      }
      return entity;
   }


   // DEPRECATED

   /**
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public int countByProperties(Class entityClass, String[] objectProperties, Object[] values) {
      int[] comparisons = new int[objectProperties.length];
      for (int i = 0; i < comparisons.length; i++) {
         comparisons[i] = Restriction.EQUALS;
      }
      return countByProperties(entityClass, objectProperties, values, comparisons);
   }

   /**
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public int countByProperties(Class entityClass, String[] objectProperties, Object[] values,
         int[] comparisons) {
      if (objectProperties.length != values.length || values.length != comparisons.length) {
         throw new IllegalArgumentException("All input arrays must be the same size");
      }
      Search search = new Search();
      for (int i = 0; i < values.length; i++) {
         search.addRestriction( new Restriction(objectProperties[i], values[i], comparisons[i]) );
      }
      return (int) countBySearch(entityClass, search);
   }

   /** 
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values) {
      int[] comparisons = new int[objectProperties.length];
      for (int i = 0; i < comparisons.length; i++)
         comparisons[i] = Restriction.EQUALS;

      return findByProperties(checkClass(entityClass), objectProperties, values, comparisons, 0, 0);
   }

   /** 
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
         int[] comparisons) {
      return findByProperties(entityClass, objectProperties, values, comparisons, null, 0, 0);
   }

   /** 
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
         int[] comparisons, String[] sortProperties) {
      return findByProperties(entityClass, objectProperties, values, comparisons, sortProperties, 0, 0);
   }

   /** 
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
         int[] comparisons, int firstResult, int maxResults) {
      return findByProperties(entityClass, objectProperties, values, comparisons, null, firstResult,
            maxResults);
   }

   /**
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
         int[] comparisons, String[] sortProperties, int firstResult, int maxResults) {
      if (objectProperties.length != values.length || values.length != comparisons.length) {
         throw new IllegalArgumentException("All input arrays must be the same size");
      }
      Search search = new Search();
      for (int i = 0; i < values.length; i++) {
         search.addRestriction( new Restriction(objectProperties[i], values[i], comparisons[i]) );
      }
      if (sortProperties != null) {
         for (int i = 0; i < sortProperties.length; i++) {
            int location = sortProperties[i].indexOf(" ");
            String property = sortProperties[i];
            if (location > 0) {
               property = sortProperties[i].substring(0, location);
            }
            Order order = null;
            if (sortProperties[i].endsWith(ByPropsFinder.DESC)) {
               order = new Order(property, false);
            } else {
               order = new Order(property, true);
            }
            search.addOrder( order );
         }
      }
      search.setStart(firstResult);
      search.setLimit(maxResults);
      return findBySearch(entityClass, search);
   }

}
