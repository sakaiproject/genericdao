/**
 * $Id$
 * $URL$
 * ReadInterceptor.java - genericdao - May 3, 2008 2:37:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.interceptors;

import java.io.Serializable;

import org.sakaiproject.genericdao.api.search.Search;

/**
 * Allows for actions to be performed before and/or after each read operation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface ReadInterceptor extends DaoOperationInterceptor {

   /**
    * This is called before each read operation (except findAll)
    * @param operation the name of the operation (e.g. findById)
    * @param ids the ids of the objects to read (may be null)
    * @param search the search object used to limit the objects (may be null)
    */
   public void beforeRead(String operation, Serializable[] ids, Search search);

   /**
    * This is called after each read operation (except findAll)
    * @param operation the name of the operation (e.g. findById)
    * @param ids the ids of the objects to read (may be null)
    * @param search the search object used to limit the objects (may be null)
    * @param entities the entities returned from the read operation (may be empty array)
    */
   public void afterRead(String operation, Serializable[] ids, Search search, Object[] entities);

}
