/******************************************************************************
 * ByPropsFinder.java created by aaronz@vt.edu
 * 
 * Copyright (c) 2006 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) primary
 * 
 *****************************************************************************/

package org.sakaiproject.genericdao.api.finders;

import java.util.List;

import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * This finder includes methods to find persisted objects 
 * based on their properties<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated Use {@link BySearchFinder} instead
 */
public interface ByPropsFinder {

	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int EQUALS = Restriction.EQUALS;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int GREATER = Restriction.GREATER;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int LESS = Restriction.LESS;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int LIKE = Restriction.LIKE;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int NULL = Restriction.NULL;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int NOT_NULL = Restriction.NOT_NULL;
	/**
	 * @deprecated Use the constants in {@link Restriction}
	 */
	public static int NOT_EQUALS = Restriction.NOT_EQUALS;

	/**
	 * Append to the name of the object property, sort in ascending order (default)
	 * @deprecated Use the {@link Order} object
	 */
	public static String ASC = " asc";
	/**
	 * Append to the name of the object property, sort in descending order
	 * @deprecated Use the {@link Order} object
	 */
	public static String DESC = " desc";

	/**
	 * Find all objects of the type associated with this DAO based on
	 * matching the supplied properties and return them in a List
	 * <p>
	 * Example usage:<br/>
	 * (Sample object of MyClass with "title" property)
	 * <p>
	 * String title = "this title";<br/>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title});<br/>
	 * <br/>
	 * The list will contain all objects which have a title which matches "this title"
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @return a List of 0 or more persistent objects
	 * @deprecated Use the {@link BySearchFinder#findBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values);

	/**
	 * Get a count of items of a the type associated with this DAO based on
	 * matching the supplied properties
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @return the number of items found
	 * @deprecated Use the {@link BySearchFinder#countBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public int countByProperties(Class entityClass, String[] objectProperties, Object[] values);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * supplied comparisons and the properties and return them in a List
	 * <p>
	 * Comparisons may be used, see the constants for {@link org.sakaiproject.genericdao.api.finders.ByPropsFinder}<br/>
	 * Note: For the LIKE comparison you will need to specify "%" as a wildcard for string comparisons
	 * <p>
	 * Example usage:<br/>
	 * (Sample object of MyClass with "title" property)
	 * <p>
	 * String title = "%title";<br/>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE});<br/>
	 * <br/>
	 * The list will contain all objects which have a title which ends with "title"
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @param comparisons the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 * @return a List of 0 or more persistent objects
    * @deprecated Use the {@link BySearchFinder#findBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, Object[] values, int[] comparisons);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * supplied comparisons and the properties and return the count
	 * <b>Note:</b>comparisons are ignored if you provide an array for the value, 
	 * however, you still need to pass a comparison
	 * <p>
	 * Comparisons may be used, see the constants for {@link org.sakaiproject.genericdao.api.finders.ByPropsFinder}<br/>
	 * Note: For the LIKE comparison you will need to specify "%" as a wildcard for string comparisons
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @param comparisons the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 * @return the number of items found
    * @deprecated Use the {@link BySearchFinder#countBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public int countByProperties(Class entityClass, String[] objectProperties, Object[] values, int[] comparisons);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * supplied comparisons and the properties and return them in a List
	 * <p>
	 * This adds the ability to sort the returned results be providing the names
	 * of the properties you want sorted and the sort order in a string
	 * (default sort order is ascending)
	 * <p>
	 * String title = "%title";<br/>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE}, new String[] {"title"+ByPropsFinder.DESC});<br/>
	 * <br/>
	 * The list will contain all objects which have a title which ends with "title" sorted by title in descending order
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @param comparisons the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 * @param sortProperties the names of the properties of the object followed by a sort order (default is ascending), 
	 * use the defined constants: e.g. ASC, DESC
	 * @return a List of 0 or more persistent objects
    * @deprecated Use the {@link BySearchFinder#findBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, 
			Object[] values, int[] comparisons, String[] sortProperties);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * supplied comparisons and the properties and return them in a List
	 * <p>
	 * Comparisons may be used, see the constants for {@link org.sakaiproject.genericdao.api.finders.ByPropsFinder}<br/>
	 * Note: For the LIKE comparison you will need to specify "%" as a wildcard for string comparisons
	 * <p>
	 * Example usage:<br/>
	 * (Sample object of MyClass with "title" property)
	 * <p>
	 * String title = "%title";<br/>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE}, 0, 2);<br/>
	 * <br/>
	 * The list will contain the first 2 objects (if that many exist) which have a title which ends with "title"
	 * <p>
	 * Other examples:
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE}, 5, 5);<br/>
	 * (<i>returns 5 objects starting with the 6th object that has a title ending with "title"</i></br>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE}, 10, 0);<br/>
	 * (<i>returns all objects starting with the 11th object that has a title ending with "title"</i></br>
	 * 
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @param comparisons the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 * @param firstResult the index of the first result object to be retrieved (numbered from 0)
	 * @param maxResults the maximum number of result objects to retrieve (or <=0 for no limit)
	 * @return a List of 0 or more persistent objects
    * @deprecated Use the {@link BySearchFinder#findBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, 
			Object[] values, int[] comparisons, int firstResult, int maxResults);

	/**
	 * Find all objects of the type associated with this DAO based on
	 * supplied comparisons and the properties and return them in a List
	 * <p>
	 * Comparisons may be used, see the constants for {@link org.sakaiproject.genericdao.api.finders.ByPropsFinder}<br/>
	 * Note: For the LIKE comparison you will need to specify "%" as a wildcard for string comparisons
	 * <p>
	 * Example usage:<br/>
	 * (Sample object of MyClass with "title" property)
	 * <p>
	 * String title = "%title";<br/>
	 * List l = findByProperties(MyClass.class, new String[] {"title"}, new Object[] {title}, new int[] {ByPropsFinder.LIKE}, new String[] {"title"+ByPropsFinder.DESC}, 0, 2);<br/>
	 * <br/>
	 * The list will contain the first 2 objects (if that many exist) which have a title which ends with "title" sorted by title in descending order 
	 * <p>
	 * @param entityClass class type of the persistent object
	 * @param objectProperties the names of the properties of the object 
	 * @param values the values of the properties (can be an array of items)
	 * @param comparisons the comparison to make between the property and the value,
	 * use the defined constants: e.g. EQUALS, LIKE, etc...
	 * @param sortProperties the names of the properties of the object followed by a sort order (default is ascending), 
	 * use the defined constants: e.g. ASC, DESC
	 * @param firstResult the index of the first result object to be retrieved (numbered from 0)
	 * @param maxResults the maximum number of result objects to retrieve (or <=0 for no limit)
	 * @return a List of 0 or more persistent objects
    * @deprecated Use the {@link BySearchFinder#findBySearch(Class, Search)}
	 */
	@SuppressWarnings("unchecked")
   public List findByProperties(Class entityClass, String[] objectProperties, 
			Object[] values, int[] comparisons, String[] sortProperties, 
			int firstResult, int maxResults);

}
