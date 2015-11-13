/**
 * $Id$
 * $URL$
 * AbstractGenericDaoTest.java - genericdao - Apr 25, 2008 6:07:16 PM - azeckoski
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.sakaiproject.genericdao.api.BasicGenericDao;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.BasicGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestBasicGenericDaoCaching extends AbstractTestBaseDao {

   protected BasicGenericDao genericDao;

   // run this before each test starts and as part of the transaction
   @Before
   public void onSetUp() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (BasicGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.BasicGenericDao.caching");
      if (genericDao == null) {
         throw new RuntimeException("onSetUp: BasicGenericDao could not be retrieved from spring context");
      }

      commonCaching();
      commonStartup(genericDao);
   }


   // TESTS

   public void testCountBySearch() {
      cacheProvider.reset();

      long count = genericDao.countBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertEquals(4, count);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      genericDao.countBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      genericDao.countBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      count = genericDao.countBySearch(GenericTestObject.class, 
            new Search( "title", "invalid" ) );
      assertEquals(0, count);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(2, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      genericDao.countBySearch(GenericTestObject.class, 
            new Search( "title", "invalid" ) );
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      genericDao.countBySearch(GenericTestObject.class, 
            new Search( "title", "invalid" ) );
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
   }

   public void testFindBySearch() {
      cacheProvider.reset();

      List<GenericTestObject> l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(4, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertEquals(4, l.size());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      cacheProvider.reset();

      String[] onetitle = new String[] {gto3.getTitle()};
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(gto3));

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertEquals(1, l.size());
      assertTrue(l.contains(gto3));
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertEquals(1, l.size());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      cacheProvider.reset();

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( "title", "XXXXXXXXXXXXXXXXX" ) );
      assertNotNull(l);
      assertEquals(0, l.size());

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( "title", "XXXXXXXXXXXXXXXXX" ) );
      assertEquals(0, l.size());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( "title", "XXXXXXXXXXXXXXXXX" ) );
      assertEquals(0, l.size());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
   }

   public void testFindOneBySearch() {
      GenericTestObject gto = null;

      cacheProvider.reset();

      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", gto3.getTitle()) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", gto3.getTitle()) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", gto3.getTitle()) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", "XXXXXXXXXXXXXX") );
      assertNull(gto);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(2, cacheProvider.size("search:" + GenericTestObject.class.getName()));
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", "XXXXXXXXXXXXXX") );
      assertNull(gto);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", "XXXXXXXXXXXXXX") );
      assertNull(gto);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
   }

}
