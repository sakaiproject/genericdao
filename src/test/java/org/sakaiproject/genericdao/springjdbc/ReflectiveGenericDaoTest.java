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

import org.junit.Before;
import org.junit.Ignore;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Testing if the {@link SimpleDataMapper} and the reflection is working
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@Ignore
public class ReflectiveGenericDaoTest extends GenericDaoTest {

   @Before
   @Override
   public void onSetUp() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.ReflectiveGenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUp: GenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }

}
