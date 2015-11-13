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

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.genericdao.test.AbstractTestGenericDao;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@ContextConfiguration(locations={"/spring-common.xml","/spring-hibernate.xml"})
public class GenericDaoTest extends AbstractTestGenericDao {

	/**
	 * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateGenericDao#setPersistentClasses(java.util.List)}.
	 */
	@Test
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
