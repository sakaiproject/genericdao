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

package org.sakaiproject.genericdao.hibernate;

import org.junit.Ignore;
import org.sakaiproject.genericdao.test.AbstractTestBasicGenericDao;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.BasicGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Ignore
@ContextConfiguration(locations={"/spring-common.xml","/spring-hibernate.xml"})
public class BasicGenericDaoTest extends AbstractTestBasicGenericDao {

   // TESTS

}
