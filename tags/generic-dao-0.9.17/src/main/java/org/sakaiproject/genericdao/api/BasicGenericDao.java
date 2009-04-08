/******************************************************************************
 * BasicGenericDao.java - created by aaronz@vt.edu on Aug 31, 2006
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

package org.sakaiproject.genericdao.api;

import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.api.finders.BySearchFinder;

/**
 * This is a Basic DAO interface which can be used to handle basic DAO
 * CRUD (create, read, update, delete) operations for any persistent object.
 * This will meet very simple DAO needs and only includes operations which
 * can be expected to run efficiently.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public interface BasicGenericDao 
	extends GenericDao, ByPropsFinder, BySearchFinder {

}
