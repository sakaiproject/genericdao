/******************************************************************************
 * ExampleFinder.java - created by aaronz@vt.edu
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
 * This finder includes methods to find persistent objects 
 * based on and example of the object
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated The example finder will be removed in the next version
 */
public interface ByExampleFinder {

	/**
	 * Find all objects of the type associated with this DAO based on
	 * the provided example object and return them in a List. Any non-null 
	 * properties will be matched exactly and any null values will
	 * be ignored.
	 * <p>
	 * <b>Note:</b> even though this method accepts any object, you should only
	 * give it the associated persistent object type.
	 * <p>
	 * <b>Note:</b> If you use primitive types in your persistent objects then
	 * those values cannot be null and must be set to something or the example
	 * matches will not work the way you expect.
	 * <p>
	 * 
	 * @param exampleObject - an example object which should have null for any
	 * properties which should not be matched and
	 * @return a List of 0 or more persistent objects
	 * @deprecated will be removed in the next version
	 */
	@SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * the provided example object and return them in a List. Any non-null 
	 * properties will be matched exactly and any null values will
	 * be ignored.
	 * <p>
	 * Order is not guaranteed.
	 * 
	 * @param exampleObject - an example object which should have null for any
	 * properties which should not be matched and
	 * @param firstResult - the index of the first result object to be retrieved (numbered from 0)
	 * @param maxResults - the maximum number of result objects to retrieve (or <=0 for no limit)
	 * @return a List of 0 or more persistent objects
	 * @see #findByExample(Object)
	 * @deprecated will be removed in the next version
	 */
	@SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject, int firstResult, int maxResults);

}
