/******************************************************************************
 * HibernateBasicGenericDao.java - created by aaronz@vt.edu on Aug 31, 2006
 * 
 * Copyright (c) 2006 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.genericdao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.genericdao.api.BasicGenericDao;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * A Hibernate (http://hibernate.org/) based implementation of BasicGenericDao which can be extended to add more
 * specialized DAO methods.
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class HibernateBasicGenericDao extends HibernateGenericDao implements BasicGenericDao {

   /**
    * Build the Criteria object here to reduce code duplication
    * @param entityClass
    * @param search a Search object (possibly only partially complete)
    * @return a DetachedCriteria object
    */
   private DetachedCriteria buildCriteria(Class<?> entityClass, Search search) {
      // Checks to see if the required params are set and throws exception if not
      if (search == null) {
         throw new IllegalArgumentException("search cannot be null");
      }

      // Build the criteria object
      DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);

      // Only add in restrictions if there are some to add
      if (search.getRestrictions() != null && search.getRestrictions().length > 0) {
         Junction junction = Expression.conjunction(); // AND
         if (! search.conjunction) {
            // set to use disjunction
            junction = Expression.disjunction(); // OR
         }
         criteria.add(junction);
   
         // put in the restrictions
         for (int i = 0; i < search.getRestrictions().length; i++) {
            String property = search.getRestrictions()[i].property;
            Object value = search.getRestrictions()[i].value;
            if (property == null || value == null) {
               throw new IllegalArgumentException("restrictions property and value cannot be null or empty");            
            }
            if (value.getClass().isArray()) {
               // special handling for "in" type comparisons
               Object[] objectArray = (Object[]) value;
               if (objectArray.length == 1) {
                  value = objectArray[0];
               } else if (objectArray.length > 1) {
                  if (Restriction.NOT_EQUALS == search.getRestrictions()[i].comparison) {
                     junction.add( Restrictions.not( Restrictions.in(property, objectArray) ) );
                  } else {
                     junction.add(Restrictions.in(property, objectArray));
                  }
               } else {
                  // do nothing for now, this is slightly invalid but not worth dying over
               }
            }
   
            if (! value.getClass().isArray()) {
               switch (search.getRestrictions()[i].comparison) {
               case Restriction.EQUALS:
                  junction.add(Restrictions.eq(property, value));
                  break;
               case Restriction.GREATER:
                  junction.add(Restrictions.gt(property, value));
                  break;
               case Restriction.LESS:
                  junction.add(Restrictions.lt(property, value));
                  break;
               case Restriction.LIKE:
                  junction.add(Restrictions.like(property, value));
                  break;
               case Restriction.NULL:
                  junction.add(Restrictions.isNull( property ));
                  break;
               case Restriction.NOT_NULL:
                  junction.add(Restrictions.isNotNull( property ));
                  break;
               case Restriction.NOT_EQUALS:
                  junction.add(Restrictions.ne(property, value));
                  break;
               }
            }
         }
      }

      // handle the sorting (sort param can be null for no sort)
      if (search.getOrders() != null) {
         for (int i = 0; i < search.getOrders().length; i++) {
            if (search.getOrders()[i].ascending) {
               criteria.addOrder(
                     org.hibernate.criterion.Order.asc( search.getOrders()[i].property ));
            } else {
               criteria.addOrder(
                     org.hibernate.criterion.Order.desc( search.getOrders()[i].property ));
            }
         }
      }

      return criteria;
   }

   
   // OVERRIDES
   
   /**
    * MUST override this
    */
   @SuppressWarnings("unchecked")
   protected <T> long baseCountBySearch(Class<T> type, Search search) {
      DetachedCriteria criteria = buildCriteria(type, search);
      criteria.setProjection(Projections.rowCount());
      List<Number> l = (List<Number>) getHibernateTemplate().findByCriteria(criteria);
      return l.get(0).longValue();
   }

   /**
    * MUST override this
    */
   @SuppressWarnings("unchecked")
   protected <T> List<T> baseFindBySearch(Class<T> type, Search search) {
      DetachedCriteria criteria = buildCriteria(type, search);
      List<T> items = (List<T>) getHibernateTemplate().findByCriteria(criteria, 
            Long.valueOf(search.getStart()).intValue(), 
            Long.valueOf(search.getLimit()).intValue());
      // TODO need to figure out how to force persistent objects to be transitive
      return items;
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
