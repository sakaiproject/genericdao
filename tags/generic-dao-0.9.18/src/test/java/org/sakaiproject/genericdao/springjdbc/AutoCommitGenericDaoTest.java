/**
 * $Id$
 * $URL$
 * ReflectiveGenericDaoTest.java - genericdao - Apr 26, 2008 7:25:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc;

import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Testing if the auto-commit is working along with reflection and not transaction manager
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class AutoCommitGenericDaoTest extends GenericDaoTest {

   @Override
   protected void onSetUpInTransaction() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.AutoCommitGenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUpInTransaction: GenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }

}
