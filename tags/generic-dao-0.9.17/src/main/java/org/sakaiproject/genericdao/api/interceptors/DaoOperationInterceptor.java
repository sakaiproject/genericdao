/**
 * $Id$
 * $URL$
 * DaoOperationInterceptor.java - genericdao - May 3, 2008 3:39:39 PM - azeckoski
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


/**
 * Base class for read and write interceptors
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface DaoOperationInterceptor {

   /**
    * @return the persistent class types which this interceptor deals with
    */
   public Class<?> interceptType();

}
