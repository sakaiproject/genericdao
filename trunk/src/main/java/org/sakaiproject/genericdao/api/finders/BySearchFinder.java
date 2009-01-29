/**
 * $Id$
 * $URL$
 * BySearchFinder.java - genericdao - Apr 30, 2008 9:46:32 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.finders;

import java.util.List;

import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * This finder includes methods to find persisted objects 
 * based on search restrictions, settings, limits, and order<br/>
 * Uses the {@link Search} object to control the search
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface BySearchFinder {

   /**
    * Find all objects of the type associated with this DAO based on
    * matching the supplied search and return them in a List
    * <p>
    * Example usage:<br/>
    * (Sample object of MyClass with "title" property)
    * <p>
    * String title = "this title";<br/>
    * List<MyItem> l = findBySearch(MyItem.class, new Search("title", title);<br/>
    * <br/>
    * The list (l) will contain all objects which have a title which matches "this title"
    * 
    * @param <T>
    * @param entityClass class type of the persistent object
    * @param search the {@link Search} object which defines {@link Restriction} on data 
    * and {@link Order} of returned results 
    * @return a List of 0 or more persisted objects
    * @see #findOneBySearch(Class, Search)
    * @see #countBySearch(Class, Search)
    */
   public <T> List<T> findBySearch(Class<T> entityClass, Search search);

   /**
    * Get a count of items of a type associated with this DAO based on
    * matching the supplied search<br/>
    * <b>NOTE:</b> Ignores the start and limit set in the search,
    * also ordering has no effect
    * 
    * @param <T>
    * @param entityClass class type of the persistent object
    * @param search the {@link Search} object which defines {@link Restriction} on data 
    * and {@link Order} of returned results 
    * @return the number of persisted objects found
    * @see #findBySearch(Class, Search)
    */
   public <T> long countBySearch(Class<T> entityClass, Search search);

   /**
    * Convenience method for getting a single item<br/>
    * Find a single object of the type associated with this DAO based on
    * matching the supplied search and return it<br/>
    * This search must result in a single item or no items,
    * if there is more than one item then the first item is returned,
    * it is a good idea to sort the search to ensure consistency in case more than one item is found
    * <p>
    * Example usage:<br/>
    * (Sample object of MyClass with "title" property)
    * <p>
    * String title = "this title";<br/>
    * MyItem item = findBySearch(MyItem.class, new Search("title", title);<br/>
    * <br/>
    * item will be null if nothing matches "this title",
    * item will be a persistent object if one or more items has the title "this title"
    * 
    * @param <T>
    * @param entityClass class type of the persistent object
    * @param search the {@link Search} object which defines {@link Restriction} on data 
    * and {@link Order} of returned results 
    * @return a persistent object OR null if none are found
    * @see #findBySearch(Class, Search)
    */
   public <T> T findOneBySearch(Class<T> entityClass, Search search);

}
