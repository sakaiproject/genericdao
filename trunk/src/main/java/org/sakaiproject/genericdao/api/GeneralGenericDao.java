/******************************************************************************
 * GeneralGenericDao.java - created by aaronz@vt.edu on Mar 20, 2008
 * 
 * Copyright (c) 2006, 2007, 2008
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

import org.sakaiproject.genericdao.api.finders.AllFinder;
import org.sakaiproject.genericdao.api.modifiers.BatchModifier;

/**
 * This is an extensible DAO interface which provides methods that are not
 * dependent on any underlying framework which should meet most needs
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface GeneralGenericDao 
	extends BasicGenericDao, AllFinder, BatchModifier {

}
