/******************************************************************************
 * HibernateGeneralGenericDao.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2006, 2007, 2008
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * A Hibernate (http://hibernate.org/) based implementation of GeneralGenericDao
 * which can be extended to add more specialized DAO methods.
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class HibernateGeneralGenericDao extends HibernateBasicGenericDao implements GeneralGenericDao {

//   public <T> void deleteSet(Set<T> entities) {
//      checkEntitySet(entities);
//      // TODO - reattach non-persistent objects
//      getHibernateTemplate().deleteAll(entities);
//   }

   @SuppressWarnings("unchecked")
   public <T> List<T> findAll(Class<T> entityClass, int firstResult, int maxResults) {
      DetachedCriteria criteria = DetachedCriteria.forClass(checkClass(entityClass));
      List<T> items = getHibernateTemplate().findByCriteria(criteria, firstResult, maxResults);
      return items;
   }


   // OVERRIDES

   /**
    * MUST override this method
    */
   protected <T> int baseCountAll(Class<T> type) {
      return count(START_QUERY + " " + checkClass(type).getName());
   }

   /**
    * MUST override this method
    */
   protected <T> int baseSaveSet(Class<?> type, Set<T> entities) {
      getHibernateTemplate().saveOrUpdateAll(entities);
      return entities.size();
   }

   /**
    * MUST override this method
    */
   protected <T> int baseDeleteSet(Class<T> type, Serializable[] ids) {
      Set<Object> entities = new HashSet<Object>();
      for (int i = 0; i < ids.length; i++) {
         Object object = baseFindById(type, ids[i]);
         if (object != null) {
            entities.add(object);
         }
      }
      getHibernateTemplate().deleteAll(entities);
      return entities.size();
/** This will not flush the item from the session so it is hopeless -AZ
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < ids.length; i++) {
         Object id = ids[i];
         if (id != null) {
            if (i > 0) { sb.append(','); }
            sb.append('?');
         }
      }
      String hql = "delete from "+type.getName()+" entity where entity.id in (" + sb + ")";
      int deletes = getHibernateTemplate().bulkUpdate(hql, ids);
      return deletes;
***/
   }

   // COMMON CODE

   public <T> List<T> findAll(Class<T> type) {
      return findAll(type, 0, 0);
   }

   public <T> int countAll(Class<T> type) {
      checkClass(type);
      int count = 0;

      // check the cache first
      boolean usedCache = false;
      String searchCacheName = getSearchCacheName(type);
      String cacheKey = "countAll::" + type.getName();
      if (getCacheProvider().exists(searchCacheName, cacheKey)) {
         Integer iCount = (Integer) getCacheProvider().get(searchCacheName, cacheKey);
         if (iCount != null) {
            count = iCount.intValue();
            usedCache = true;
         }
      }

      if (! usedCache) {
         count = baseCountAll(type);

         // cache the id results for the search
         getCacheProvider().put(searchCacheName, cacheKey, Integer.valueOf(count));
      }
      return count;
   }

   public <T> void deleteSet(Class<T> type, Serializable[] ids) {
      checkClass(type);
      if (ids.length > 0) {
         String operation = "deleteSet";
         beforeWrite(operation, type, ids, null);
   
         int changes = baseDeleteSet(type, ids);
   
         afterWrite(operation, type, ids, null, changes);
   
         // clear all removed items from the cache
         String cacheName = getCacheName(type);
         for (int i = 0; i < ids.length; i++) {
            if (ids[i] != null) {
               String key = ids[i].toString();
               getCacheProvider().remove(cacheName, key);
            }
         }
         // clear the search caches
         getCacheProvider().clear(getSearchCacheName(type));
      }
   }

   public <T> void saveSet(Set<T> entities) {
      if (entities == null || entities.isEmpty()) {
         System.out.println("WARN: Empty list of entities for saveSet, nothing to do...");
      } else {
         Class<?> type = checkEntitySet(entities);
   
         String operation = "saveSet";
         beforeWrite(operation, type, null, entities.toArray());
   
         int changes = baseSaveSet(type, entities);
   
         afterWrite(operation, type, null, entities.toArray(), changes);
   
         // clear all saved items from the cache
         String cacheName = getCacheName(type);
         for (T t : entities) {
            Object id = baseGetIdValue(t);
            if (id != null) {
               String key = id.toString();
               getCacheProvider().remove(cacheName, key);
            }
         }
         // clear the search caches
         getCacheProvider().clear(getSearchCacheName(type));
      }
   }

   public <T> void deleteSet(Set<T> entities) {
      if (entities.size() > 0) {
         Class<?> type = checkEntitySet(entities);
         List<Object> ids = new ArrayList<Object>();
         for (T t : entities) {
            Object id = baseGetIdValue(t);
            if (id != null) {
               ids.add(id);
            }
         }
         deleteSet(type, ids.toArray(new Serializable[ids.size()]));
      }
   }

   @SuppressWarnings("unchecked")
   public void saveMixedSet(Set[] entitySets) {
      for (int i=0; i<entitySets.length; i++) {
         checkEntitySet(entitySets[i]);
      }
      for (int i=0; i<entitySets.length; i++) {
         saveSet(entitySets[i]);
      }
   }

   @SuppressWarnings("unchecked")
   public void deleteMixedSet(Set[] entitySets) {
      for (int i=0; i<entitySets.length; i++) {
         checkEntitySet(entitySets[i]);
      }
      for (int i=0; i<entitySets.length; i++) {
         deleteSet(entitySets[i]);
      }
   }

   /**
    * Validates the class type and the list of entities before performing
    * a batch operation (throws IllegalArgumentException)
    * 
    * @param entities a Set of persistent entities, should all be of the same type
    */
   protected Class<?> checkEntitySet(Set<?> entities) {
      Class<?> entityClass = null;
      Iterator<?> it = entities.iterator();
      while(it.hasNext()) {
         Object entity = it.next();
         if (entityClass == null) {
            entityClass = (Class<?>) findClass(entity);
         }
         if (! checkClass(entityClass).isInstance(entity)) {
            throw new IllegalArgumentException("Entity set item " +
                  entity.toString() + " is not of type: " + entityClass +
                  ", the type is: " + entity.getClass() +
            " (All items must be of consistent persistent type)");
         }
      }
      return entityClass;
   }

}
