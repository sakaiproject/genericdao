/**
 * $Id$
 * $URL$
 * BaseGeneralGenericDao.java - genericdao - May 14, 2008 9:31:51 PM - azeckoski
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * This is the simple base implementation which includes shared methods and should be extended,
 * this handles the caching checks and the interceptors on the basic methods,
 * NOTE that findAll is not cached since it would put the entire set into the cache
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public abstract class BaseGeneralGenericDao extends BaseBasicGenericDao implements GeneralGenericDao {

   /**
    * MUST override this method
    */
   protected <T> int baseCountAll(Class<T> type) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST override this method
    */
   protected <T> int baseSaveSet(Class<?> type, Set<T> entities) {
       throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * MUST override this method
    */
   protected <T> int baseDeleteSet(Class<T> type, Serializable[] ids) {
       throw new UnsupportedOperationException("Not Implemented");
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
