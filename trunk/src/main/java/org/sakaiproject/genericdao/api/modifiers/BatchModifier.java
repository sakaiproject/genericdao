/******************************************************************************
 * BatchModifier.java - created by aaronz@vt.edu
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
import java.util.Set;

/**
 * This Modifier provides methods to modify groups of persistent objects
 * within a single transaction (i.e. all operations have to succeed or
 * the batch operation will fail)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface BatchModifier {

	/**
	 * Saves (creates or updates) a set of objects (persistent or transient) of
	 * the same class type in a single transaction<br/>
	 * <b>Warning:</b> Make sure all objects are of the same type and that
	 * the type is set as a persistent class for this DAO
	 * 
	 * @param entities a List of 0 or more objects to save
	 * (recommend use of HashSet)
	 */
	public <T> void saveSet(Set<T> entities);

	/**
	 * This allows us to save a set of entities (objects) that are not 
	 * all of the same class type within a single transaction<br/>
	 * <b>Warning:</b> Make sure all objects are of the types that
	 * are set as persistent classes for this DAO and that each
	 * Set contains objects of the same type
	 * 
	 * @param entitySets an array of Sets of persistent objects (entities)
	 * (each Set should have one type of entity, recommend using HashSet)
	 */
	@SuppressWarnings("unchecked")
   public void saveMixedSet(Set[] entitySets);

   /**
    * Deletes (removes) a set of persistent objects in a single transaction
    * by their unique ids
    * 
    * @param entityClass class type of the persistent object
    * @param ids the identifiers of the objects to delete
    */
   public <T> void deleteSet(Class<T> entityClass, Serializable[] ids);

	/**
	 * Deletes (removes) a set of objects (persistent or transient) of
	 * the same class type in a single transaction<br/>
	 * <b>Warning:</b> Make sure all objects are of the same type and that
	 * the type is set as a persistent class for this DAO
	 * 
	 * @param entities a Set of 0 or more objects to delete
	 * (recommend use of HashSet)
	 */
	public <T> void deleteSet(Set<T> entities);

	/**
	 * This allows us to delete a set of entities (objects) that are not 
	 * all of the same class type within a single transaction<br/>
	 * <b>Warning:</b> Make sure all objects are of the types that
	 * are set as persistent classes for this DAO and that each
	 * Set contains objects of the same type
	 * 
	 * @param entitySets an array of Sets of persistent objects (entities)
	 * (each Set should have one type of entity, recommend using HashSet)
	 */
	@SuppressWarnings("unchecked")
   public void deleteMixedSet(Set[] entitySets);

}
