/**
 * $Id$
 * $URL$
 * GeneralGenericDaoTest.java - genericdao - Apr 26, 2008 7:10:30 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.hibernate;

import org.sakaiproject.genericdao.test.AbstractTestGeneralGenericDao;


/**
 * Testing general Generic Dao
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class GeneralGenericDaoTest extends AbstractTestGeneralGenericDao {

   protected String[] getConfigLocations() {
      // point to the spring-hibernate.xml file, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse)
      return new String[] {"spring-common.xml","spring-hibernate.xml"};
   }

}
