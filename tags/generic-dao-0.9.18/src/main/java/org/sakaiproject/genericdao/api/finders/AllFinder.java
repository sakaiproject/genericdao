/******************************************************************************
 * AllFinder.java - created by aaronz@vt.edu
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

package org.sakaiproject.genericdao.api.finders;

import java.util.List;

/**
 * This Finder provides methods to find all persistent objects of a
 * certain type
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface AllFinder {

	/**
	 * Find all objects of the type entityClass and return them in a List.
	 * 
	 * @param entityClass class type of the persistent object
	 * @return a List of 0 or more persistent objects
	 */
	public <T> List<T> findAll(Class<T> entityClass);

	/**
	 * Find all objects of the type entityClass and return them in a List,
	 * apply limits to the number of items returned. Order of returned items is not
	 * guaranteed.
	 * 
	 * @param entityClass class type of the persistent object
	 * @param firstResult the index of the first result object to be retrieved (numbered from 0)
	 * @param maxResults the maximum number of result objects to retrieve (or <=0 for no limit)
	 * @return a List of 0 or more persistent objects
	 */
	public <T> List<T> findAll(Class<T> entityClass, int firstResult, int maxResults);

	/**
	 * Get a count of all items of the type entityClass.
	 * 
	 * @param entityClass class type of the persistent object
	 * @return the number of items found
	 */
	public <T> int countAll(Class<T> entityClass);

}
