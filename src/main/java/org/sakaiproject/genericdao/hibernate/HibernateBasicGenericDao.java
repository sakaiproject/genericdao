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
import java.util.Collection;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.query.Query;
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
public class HibernateBasicGenericDao extends HibernateGenericDao implements BasicGenericDao {
   private <T> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<T> root, Search search) {
      if (search == null) {
         throw new IllegalArgumentException("search cannot be null");
      }

      Restriction[] restrictions = search.getRestrictions();
      if (restrictions == null || restrictions.length == 0) {
         return null;
      }

      List<Predicate> predicates = new ArrayList<Predicate>();
      for (Restriction restriction : restrictions) {
         if (restriction == null) {
            continue;
         }
         String property = restriction.property;
         Object value = restriction.value;
         if (property == null || value == null) {
            throw new IllegalArgumentException("restrictions property and value cannot be null or empty");
         }

         Object[] arrayValues = null;
         if (value.getClass().isArray()) {
            arrayValues = (Object[]) value;
         } else if (value instanceof Collection) {
            arrayValues = ((Collection<?>) value).toArray();
         }

         if (arrayValues != null) {
            if (arrayValues.length == 0) {
               continue;
            }
            if (arrayValues.length == 1) {
               value = arrayValues[0];
            } else {
               In<Object> inClause = criteriaBuilder.in(root.get(property));
               for (Object element : arrayValues) {
                  inClause.value(element);
               }
               if (restriction.comparison == Restriction.NOT_EQUALS) {
                  predicates.add(criteriaBuilder.not(inClause));
               } else {
                  predicates.add(inClause);
               }
               continue;
            }
         }

         switch (restriction.comparison) {
         case Restriction.EQUALS:
            predicates.add(criteriaBuilder.equal(root.get(property), value));
            break;
         case Restriction.GREATER:
            predicates.add(buildComparablePredicate(criteriaBuilder, root.get(property), true, value));
            break;
         case Restriction.LESS:
            predicates.add(buildComparablePredicate(criteriaBuilder, root.get(property), false, value));
            break;
         case Restriction.LIKE:
            predicates.add(criteriaBuilder.like(asString(root.get(property)), value.toString()));
            break;
         case Restriction.NULL:
            predicates.add(criteriaBuilder.isNull(root.get(property)));
            break;
         case Restriction.NOT_NULL:
            predicates.add(criteriaBuilder.isNotNull(root.get(property)));
            break;
         case Restriction.NOT_EQUALS:
            predicates.add(criteriaBuilder.notEqual(root.get(property), value));
            break;
         default:
            throw new IllegalArgumentException("Unknown comparison: " + restriction.comparison);
         }
      }

      if (predicates.isEmpty()) {
         return null;
      }

      Predicate[] predicateArray = predicates.toArray(new Predicate[predicates.size()]);
      if (search.isConjunction()) {
         return criteriaBuilder.and(predicateArray);
      } else {
         return criteriaBuilder.or(predicateArray);
      }
   }

   private Expression<String> asString(Path<?> path) {
      return path.as(String.class);
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private Predicate buildComparablePredicate(CriteriaBuilder criteriaBuilder, Path<?> path, boolean greaterThan, Object value) {
      if (!(value instanceof Comparable)) {
         throw new IllegalArgumentException("comparison value must be Comparable: " + value);
      }
      Expression<? extends Comparable> expression = (Expression<? extends Comparable>) path;
      Comparable comparableValue = (Comparable) value;
      if (greaterThan) {
         return criteriaBuilder.greaterThan(expression, comparableValue);
      } else {
         return criteriaBuilder.lessThan(expression, comparableValue);
      }
   }

   private <T> void applySorting(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<T> criteriaQuery, Search search) {
      Order[] orders = search.getOrders();
      if (orders == null || orders.length == 0) {
         return;
      }

      List<jakarta.persistence.criteria.Order> jpaOrders = new ArrayList<jakarta.persistence.criteria.Order>();
      for (Order order : orders) {
         if (order == null || order.property == null) {
            continue;
         }
         Path<Object> path = root.get(order.property);
         if (order.ascending) {
            jpaOrders.add(criteriaBuilder.asc(path));
         } else {
            jpaOrders.add(criteriaBuilder.desc(path));
         }
      }

      if (!jpaOrders.isEmpty()) {
         criteriaQuery.orderBy(jpaOrders);
      }
   }

   private int toInt(long value) {
      if (value > Integer.MAX_VALUE) {
         return Integer.MAX_VALUE;
      }
      if (value < 0) {
         return 0;
      }
      return (int) value;
   }

   
   // OVERRIDES
   
   /**
    * MUST override this
    */
   protected <T> long baseCountBySearch(Class<T> type, Search search) {
      return execute(session -> {
         CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
         CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
         Root<T> root = criteriaQuery.from(type);
         criteriaQuery.select(criteriaBuilder.count(root));

         Predicate predicate = buildPredicate(criteriaBuilder, root, search);
         if (predicate != null) {
            criteriaQuery.where(predicate);
         }

         return session.createQuery(criteriaQuery).getSingleResult();
      }).longValue();
   }

   /**
    * MUST override this
    */
   protected <T> List<T> baseFindBySearch(Class<T> type, Search search) {
      return execute(session -> {
         CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
         CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
         Root<T> root = criteriaQuery.from(type);
         criteriaQuery.select(root);

         Predicate predicate = buildPredicate(criteriaBuilder, root, search);
         if (predicate != null) {
            criteriaQuery.where(predicate);
         }

         applySorting(criteriaBuilder, root, criteriaQuery, search);

         Query<T> query = session.createQuery(criteriaQuery);
         int start = toInt(search.getStart());
         if (start > 0) {
            query.setFirstResult(start);
         }
         int limit = toInt(search.getLimit());
         if (limit > 0) {
            query.setMaxResults(limit);
         }
         return query.list();
      });
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
