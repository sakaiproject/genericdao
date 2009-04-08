/**
 * $Id$
 * $URL$
 * EntityColumnMapper.java - genericdao - Apr 26, 2008 9:58:49 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.mappers;

import java.util.Map;

/**
 * Extension for the DataMapper which allows custom translation of 
 * persistent entity data into data maps and back
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface EntityColumnMapper extends DataMapper {

   /**
    * Called when the object is being input into the database,
    * this translates an object type to be persisted into a map of column names and values
    * 
    * @param persistentObject a persistent object
    * @return a map of database column names to the values to place in those columns
    * OR return null to have generic dao attempt to handle this automatically
    */
   public Map<String, Object> mapObjectToColumns(Object persistentObject);

   /**
    * Called when the object is being pulled out of the database,
    * this translates a map with column names and the data contained into a persistent object type
    * @param columnsData a map of database column names to the values in those columns
    * @return a persistent object with the data from the columnsdata placed in it
    * OR return null to have generic dao attempt to handle this automatically
    */
   public Object mapColumnsToObject(Map<String, Object> columnsData);

}
