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

import org.sakaiproject.genericdao.api.modifiers.BasicModifier;


/**
 * This is a generic DAO interface which includes the most fundamental DAO
 * operations (findById, etc) for any persistent object.
 * It also includes the methods needed for managing the DAO.
 * <p>
 * <b>NOTE:</b> This interface should be extended with at least one of the 
 * {@link org.sakaiproject.genericdao.api.finders}
 * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface GenericDao extends BasicModifier {

	/**
	 * Get a copy of the list of persistent classes that this DAO will handle
	 * @return a list of Class objects representing the types of persistent objects
	 */
	public List<Class<?>> getPersistentClasses();

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
	public String getIdProperty(Class<?> entityClass);

   /**
    * Find an object based on an id and return the persistent version of it
    * @param entityClass class type of the persistent object
    * @param id the unique id of the object
    * @return the persistent object or null if it cannot be found
    */
   public <T> T findById(Class<T> entityClass, Serializable id);

}
