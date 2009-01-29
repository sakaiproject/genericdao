/**
 * $Id$
 * $URL$
 * WriteInterceptor.java - genericdao - May 3, 2008 2:32:51 PM - azeckoski
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

/**
 * Allows for actions to be performed before and/or after each write operation
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface WriteInterceptor extends DaoOperationInterceptor {

   /**
    * Called before each write operation
    * @param operation the name of the operation (e.g. findById)
    * @param ids the ids of the objects to write (normally ids to be deleted) (may be null)
    * @param entities the persistent objects to write (may be null)
    */
   public void beforeWrite(String operation, Serializable[] ids, Object[] entities);

   /**
    * Called after each write operation
    * @param operation the name of the operation (e.g. findById)
    * @param ids the ids of the objects to write (normally ids to be deleted) (may be null)
    * @param entities the persistent objects to write (may be null)
    * @param changes the number indicating how many changes were written
    */
   public void afterWrite(String operation, Serializable[] ids, Object[] entities, int changes);

}
