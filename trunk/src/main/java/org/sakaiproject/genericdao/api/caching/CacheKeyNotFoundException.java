/**
 * $Id$
 * $URL$
 * CacheObjectNotFoundException.java - genericdao - May 14, 2008 12:16:52 PM - azeckoski
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
 * thrown if an object cannot be found in a cache
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class CacheKeyNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public String cacheName;
	public String cacheKey;

	public CacheKeyNotFoundException(String message, String cacheName, String cacheKey) {
		super(message);
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
	}

	public CacheKeyNotFoundException(String message, String cacheName,
			String cacheKey, Throwable cause) {
		super(message, cause);
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
	}

}
