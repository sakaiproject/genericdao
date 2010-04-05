/**
 * $Id$
 * $URL$
 * BasicMapCacheProvider.java - genericdao - May 14, 2008 12:23:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.genericdao.api.caching.CacheKeyNotFoundException;
import org.sakaiproject.genericdao.api.caching.CacheProvider;

/**
 * This is a very simple in memory map cache,
 * it is meant for use in testing and as an example only
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class BasicMapCacheProvider implements CacheProvider {

   private Map<String, Map<String, Object>> cache = new ConcurrentHashMap<String, Map<String,Object>>();

   protected List<String> actionRecord = new Vector<String>();
   public List<String> getActionRecord() {
      return actionRecord;
   }
   public String getLastAction() {
      String action = null;
      if (actionRecord.size() > 0) {
         action = actionRecord.get(actionRecord.size()-1);
      }
      return action;
   }
   public int size(String cacheName) {
      checkCacheName(cacheName);
      return cache.get(cacheName).size();      
   }
   public void reset() {
      for (Map<String, Object> m : cache.values()) {
         m.clear();
      }
      actionRecord.clear();
   }

   public void clear(String cacheName) {
      checkCacheName(cacheName);
      cache.get(cacheName).clear();
      actionRecord.add("clear:" + cacheName);
   }

   public void createCache(String cacheName) {
      if (cacheName == null) {
         throw new IllegalArgumentException("cacheName cannot be null");
      }
      if (! cache.containsKey(cacheName)) {
         Map<String, Object> iCache = new HashMap<String, Object>();
         cache.put(cacheName, iCache);
         actionRecord.add("create:" + cacheName + ":new");
      } else {
         cache.get(cacheName).clear();
         actionRecord.add("create:" + cacheName + ":clear");
      }
   }

   public boolean exists(String cacheName, String key) {
      checkCacheName(cacheName);
      boolean exists = cache.get(cacheName).containsKey(key);
      actionRecord.add("exists: " + cacheName + ":" + key + ":exists=" + exists);
      return exists;
   }

   public Object get(String cacheName, String key) {
      checkCacheName(cacheName);
      if (! cache.get(cacheName).containsKey(key)) {
         throw new CacheKeyNotFoundException("Key ("+key+") does not exist in cache ("+cacheName+")", cacheName, key);
      }
      actionRecord.add("get: " + cacheName + ":" + key);
      return cache.get(cacheName).get(key);
   }

   public void put(String cacheName, String key, Object value) {
      checkCacheName(cacheName);
      if (key == null) {
         throw new IllegalArgumentException("key cannot be null");
      }
      actionRecord.add("put: " + cacheName + ":" + key + ":value=" + value);
      cache.get(cacheName).put(key, value);
   }

   public boolean remove(String cacheName, String key) {
      checkCacheName(cacheName);
      boolean exists = false;
      if (cache.get(cacheName).containsKey(key)) {
         exists = true;
         cache.get(cacheName).remove(key);
      }
      actionRecord.add("remove: " + cacheName + ":" + key + ":removed=" + exists);
      return exists;
   }

   private void checkCacheName(String cacheName) {
      if (cacheName == null) {
         throw new IllegalArgumentException("cacheName cannot be null");
      }
      if (! cache.containsKey(cacheName)) {
         throw new IllegalArgumentException("Invalid cache name: " + cacheName);
      }
   }
}
