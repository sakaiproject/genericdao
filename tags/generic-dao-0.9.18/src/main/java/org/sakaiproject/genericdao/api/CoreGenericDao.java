/******************************************************************************
 * GenericDao.java - created by aaronz@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2006, 2007
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.genericdao.api;

import java.io.Serializable;
import java.util.List;


/**
 * This is a generic DAO interface which includes the most fundamental DAO
 * operations (findById, etc) for any persistent object.
 * It also includes the methods needed for managing the DAO.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface CoreGenericDao {

	/**
	 * Get a copy of the list of persistent classes that this DAO will handle
	 * @return a list of Class objects representing the types of persistent objects
	 */
	public List<String> getPersistentClasses();

	/**
	 * This allows us to wrap our dao so that it can still load transactional data
	 * (lazily for example) outside the normal transaction
	 * 
	 * @param toinvoke a Runnable wrapper from the tool layer
	 */
	public void invokeTransactionalAccess(Runnable toinvoke);

	/**
	 * Get the id (unique identifier, often the primary key) of the persistent class
	 * @param entityClass class type of the persistent object
	 * @return the property of the persistent object or null if it cannot be found
	 */
	public <T> String getIdProperty(Class<T> entityClass);

	/**
	 * Find an object based on an id and return the persistent version of it
	 * @param entityClass class type of the persistent object
	 * @param id the unique id of the object
	 * @return the persistent object or null if it cannot be found
	 */
	public <T> T findById(Class<T> entityClass, Serializable id);

	/**
	 * Saves the object and transforms it into a persistent object if
	 * it is not already one and assigns it an id, if is already 
	 * persistent then the object is updated
	 * @param object the persistent type object to save (must have associated hbm file)
	 */
	public void save(Object object);

	/**
	 * Deletes the object represented by this id
	 * @param entityClass class type of the persistent object
	 * @param id the identifier of the object to delete
	 * @return true if deleted, false otherwise
	 */
	public <T> boolean delete(Class<T> entityClass, Serializable id);

}
