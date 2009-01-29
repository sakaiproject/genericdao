/******************************************************************************
 * CompleteGenericDao.java - created by aaronz@vt.edu on Aug 28, 2006
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

import org.sakaiproject.genericdao.api.finders.AllFinder;
import org.sakaiproject.genericdao.api.finders.ByExampleFinder;
import org.sakaiproject.genericdao.api.modifiers.BatchModifier;


/**
 * This is an extensible DAO interface which provides methods that are not
 * dependent on any underlying framework which should meet most needs
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated Use {@link GeneralGenericDao} instead
 */
public interface CompleteGenericDao 
	extends BasicGenericDao, AllFinder, ByExampleFinder, BatchModifier {

}
