/******************************************************************************
 * InitializingCoreGenericDAO.java - created by aaronz@vt.edu on Aug 21, 2006
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

/**
 * Added for Antranig Basman
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface InitializingCoreGenericDAO extends CoreGenericDao {
	public Object instantiate();
}
