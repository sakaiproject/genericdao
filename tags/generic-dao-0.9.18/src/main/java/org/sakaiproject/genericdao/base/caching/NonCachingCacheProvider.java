/**
 * $Id$
 * $URL$
 * NonCachingCacheProvider.java - genericdao - May 14, 2008 5:27:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.base.caching;

import org.sakaiproject.genericdao.api.caching.CacheKeyNotFoundException;
import org.sakaiproject.genericdao.api.caching.CacheProvider;


/**
 * This is a stand in cache provider that does no caching,
 * used if no cache provider was specified
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class NonCachingCacheProvider implements CacheProvider {

   public void clear(String cacheName) { }

   public void createCache(String cacheName) { }

   public boolean exists(String cacheName, String key) {
      return false;
   }

   public Object get(String cacheName, String key) {
      throw new CacheKeyNotFoundException("Default no caching provider is being used, nothing is stored", cacheName, key);
   }

   public void put(String cacheName, String key, Object value) { }

   public boolean remove(String cacheName, String key) {
      return false;
   }
   
}
