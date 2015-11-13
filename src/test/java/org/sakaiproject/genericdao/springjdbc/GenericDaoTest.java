/**
 * $Id$
 * $URL$
 * GenericDaoTest.java - genericdao - Apr 25, 2008 5:28:03 PM - azeckoski
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

import org.junit.Ignore;
import org.sakaiproject.genericdao.test.AbstractTestGenericDao;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Ignore
@ContextConfiguration(locations={"/spring-common.xml","/spring-jdbc.xml"})
public class GenericDaoTest extends AbstractTestGenericDao {

	// TODO test the jdbc specific stuff

}
