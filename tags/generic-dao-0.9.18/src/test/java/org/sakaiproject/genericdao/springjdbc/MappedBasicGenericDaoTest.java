/**
 * $Id$
 * $URL$
 * GenericDaoTest.java - genericdao - May 18, 2008 4:34:33 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc;

import org.sakaiproject.genericdao.api.BasicGenericDao;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.BasicGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MappedBasicGenericDaoTest extends BasicGenericDaoTest {

   @Override
   protected void onSetUpInTransaction() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (BasicGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.MappedBasicGenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUpInTransaction: GenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }

   // TESTS

}
