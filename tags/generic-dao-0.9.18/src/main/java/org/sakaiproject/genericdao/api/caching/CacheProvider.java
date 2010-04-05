/**
 * $Id$
 * $URL$
 * CacheProvider.java - genericdao - May 14, 2008 11:41:50 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.caching;


/**
 * Allows the developer to specify their own cache mechanism which will be used for all
 * generic DAO method calls, write methods will automatically cause the cache to expire
 * any related items<br/>
 * Note that your cache must be able to store null values 
 * (these are used to cache searches which come up with no results and are critical since these
 * are often the most expensive searches and can cause full table scans)
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface CacheProvider {

   /**
    * Create a cache by the given name (or if the cache already exists then reset it),
    * the cache will be used to cache one type of persistent objects,
    * the cache only needs to exist in your implementation as it will be accessed using the given name
    * @param cacheName a string which uniquely identifies this cache
    * @throws IllegalArgumentException if the cache name is invalid
    * @throws IllegalStateException if the cache cannot be created
    */
   public void createCache(String cacheName);

   /**
    * Puts an object in the specified cache
    * @param cacheName a string which uniquely identifies this cache
    * @param key the key for a persistent object (this will be the persistent id)
    * @param value a persistent object (this can be a null to cache a miss)
    * @throws IllegalArgumentException if the cache name is invalid or cacheName or key is null
    */
   public void put(String cacheName, String key, Object value);

   /**
    * Gets an object from the cache if it can be found (maybe be a null),
    * use the exists check to see if the object is in the cache before retrieving
    * @param cacheName a string which uniquely identifies this cache
    * @param key the key for a persistent object (this will be the persistent id)
    * @return the cached persistent object (may be null)
    * @throws IllegalArgumentException if the cache name is invalid or any arguments are null
    * @throws CacheKeyNotFoundException if this key does not exist in the cache
    */
   public Object get(String cacheName, String key);

   /**
    * Removes an object from the cache if it exists or does nothing
    * @param cacheName a string which uniquely identifies this cache
    * @param key the key for a persistent object (this will be the persistent id)
    * @return true if the object was removed or false if it could not be found in the cache
    * @throws IllegalArgumentException if the cache name is invalid or any arguments are null
    */
   public boolean remove(String cacheName, String key);

   /**
    * Check if a key exists in the cache and return true if it does
    * @param cacheName a string which uniquely identifies this cache
    * @param key the key for a persistent object (this will be the persistent id)
    * @return true if the object was removed or false if it could not be found in the cache
    * @throws IllegalArgumentException if the cache name is invalid or any arguments are null
    */
   public boolean exists(String cacheName, String key);

   /**
    * Clear out all cached items from this cache
    * @param cacheName a string which uniquely identifies this cache
    * @throws IllegalArgumentException if the cache name is invalid
    */
   public void clear(String cacheName);

}
