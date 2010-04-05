/**
 * $Id$
 * $URL$
 * AbstractBaseDaoTest.java - genericdao - Apr 25, 2008 6:07:16 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.test;

import org.sakaiproject.genericdao.api.GenericDao;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestBaseDao extends AbstractTransactionalSpringContextTests {

   public BasicDataInterceptor dataInterceptor;
   public void commonStartup(GenericDao genericDao) {
      // load up the interceptor
      dataInterceptor = (BasicDataInterceptor) applicationContext.getBean("org.sakaiproject.genericdao.interceptors.TestInterceptor");
      if (dataInterceptor == null) {
         throw new RuntimeException("onSetUpInTransaction: dataInterceptor could not be retrieved from spring context");
      }

      // preload data if desired
      preloadGTOs(genericDao);
   }

   public BasicMapCacheProvider cacheProvider;
   public void commonCaching() {
      // load up the cache provider
      cacheProvider = (BasicMapCacheProvider) applicationContext.getBean("org.sakaiproject.genericdao.caching.CacheProvider");
      if (cacheProvider == null) {
         throw new RuntimeException("onSetUpInTransaction: cacheProvider could not be retrieved from spring context");
      }
   }

   public GenericTestObject gto1;
   public GenericTestObject gto2;
   public GenericTestObject gto3;
   public GenericTestObject gto4;
   public GenericTestObject gto5;
   public GenericTestObject gto6;

   /**
    * Contains gto4
    */
   public GenericTestParentObject gtpo1;
   /**
    * Contains gto5
    */
   public GenericTestParentObject gtpo2;

   // the values to return for fake data
   public final static String TEST_TITLE = "aaronz test object";

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      gto1 = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      gto2 = new GenericTestObject(TEST_TITLE + "2", Boolean.FALSE);
      gto3 = new GenericTestObject(TEST_TITLE + "3", Boolean.FALSE);
      gto4 = new GenericTestObject(TEST_TITLE + "4", Boolean.TRUE);
      gto5 = new GenericTestObject(TEST_TITLE + "5", Boolean.TRUE);
      gto6 = new GenericTestObject("number six", Boolean.FALSE);

      gtpo1 = new GenericTestParentObject("parent object 1", gto4);
      gtpo2 = new GenericTestParentObject("parent object 2", gto5);
   }

   public void preloadGTOs(GenericDao dao) {
      // preload data if desired
      dao.create(gto1);
      dao.create(gto2);
      dao.create(gto3);
      dao.create(gto4);
      dao.create(gto5);
      dao.create(gto6);

      dao.create(gtpo1);
      dao.create(gtpo2);
   }

}
