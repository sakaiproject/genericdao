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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.genericdao.test.AbstractTestGenericDao;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class GenericDaoTest extends AbstractTestGenericDao {

   @Override
	protected String[] getConfigLocations() {
		// point to the spring-hibernate.xml file, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse)
		return new String[] {"spring-common.xml","spring-hibernate.xml"};
	}

	/**
	 * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateGenericDao#setPersistentClasses(java.util.List)}.
	 */
	public void testSetPersistentClasses() {
	   HibernateGenericDao genericDao = new HibernateGenericDao();

	   List<String> l = new ArrayList<String>();
		l.add("org.sakaiproject.genericdao.test.GenericTestObject");
		genericDao.setPersistentClasses(l);

		// test null list
		l = null;
		try {
			genericDao.setPersistentClasses(l);
			Assert.fail("Should have thrown a NullPointerException");
		} catch (NullPointerException e) {
			Assert.assertNotNull(e.getStackTrace());
		}
		
		// test empty list
		l = new ArrayList<String>();
		try {
			genericDao.setPersistentClasses(l);
			Assert.fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e.getStackTrace());
		}
	}

}
