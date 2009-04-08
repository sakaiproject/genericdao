/******************************************************************************
 * BasicModifier.java - created by aaronz@vt.edu
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

package org.sakaiproject.genericdao.api.modifiers;

import java.io.Serializable;

/**
 * This Modifier provides methods to modify single persistent entities,
 * this modifier should be used in even the simplest DAO
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface BasicModifier {

	/**
	 * Create the object and assign it an id, if the object already has an id
	 * then it will be created using that id, if there is no id then one will be
	 * assigned to it
	 * @param object the persistent type object to save (hibernate version must have associated hbm file)
    * @throws IllegalArgumentException if the object already exists or the id is already used
	 */
	public void create(Object object);

	/**
	 * Updates the object (object must already exist in persistent storage)
	 * @param object the persistent type object to save (hibernate version must have associated hbm file)
    * @throws IllegalArgumentException if the object does not exist or the id is null
	 */
	public void update(Object object);

	/**
	 * Saves the object and transforms it into a persistent object if
	 * it is not already one and assigns it an id, if is already 
	 * persistent then the object is updated
	 * @param object the persistent type object to save (must have associated hbm file)
	 */
	public void save(Object object);

	/**
	 * Deletes the object and transforms it from persistent to transient
	 * @param object the java object to delete (must have associated hbm file)
	 * @see #delete(Class, Serializable)
    * @throws RuntimeException if the object cannot be deleted
	 */
	public void delete(Object object);

	/**
	 * Deletes the object represented by this id
	 * @param entityClass class type of the persistent object
	 * @param id the identifier of the object to delete
    * @return true if object deleted, false otherwise
	 */
	public <T> boolean delete(Class<T> entityClass, Serializable id);

}
