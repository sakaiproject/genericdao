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

import java.util.List;

import org.sakaiproject.genericdao.api.BasicGenericDao;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.test.BasicDataInterceptor.Intercept;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.BasicGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public abstract class AbstractTestBasicGenericDao extends AbstractTestBaseDao {

   protected BasicGenericDao genericDao;

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (BasicGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.BasicGenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUpInTransaction: BasicGenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }


   // TESTS

   public void testCountBySearch() {
      long count = genericDao.countBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertEquals(4, count);

      count = genericDao.countBySearch(GenericTestObject.class, 
            new Search( new Restriction("hiddenItem", Boolean.FALSE, Restriction.NOT_EQUALS) ) );
      assertEquals(2, count);

      count = genericDao.countBySearch(GenericTestObject.class, 
            new Search( "title", "invalid" ) );
      assertEquals(0, count);

      // test foreign keys
      count = genericDao.countBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", gto4.getId() ) );
      assertEquals(1, count);

      count = genericDao.countBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", new Long(999999) ) );
      assertEquals(0, count);

      count = genericDao.countBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", "", Restriction.NOT_NULL ) );
      assertEquals(2, count);
   }

   public void testCountBySearchInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      long count = genericDao.countBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertEquals(4, count);

      // no intercepts
      assertEquals(0, dataInterceptor.getIntercepts().size());

      count = genericDao.countBySearch(GenericTestObject.class, 
            new Search( new Restriction("hiddenItem", Boolean.FALSE, Restriction.NOT_EQUALS) ) );
      assertEquals(2, count);

      // no intercepts
      assertEquals(0, dataInterceptor.getIntercepts().size());
   }

   public void testFindBySearch() {
      List<GenericTestObject> l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));

      // now do a couple tests on the array handling ability of the system
      String[] titles = new String[] {gto1.getTitle(), gto3.getTitle(), gto5.getTitle()};
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", titles) );
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto5));

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", titles, Restriction.NOT_EQUALS) );
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto6));

      String[] onetitle = new String[] {gto3.getTitle()};
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(gto3));

      // test the various searches and filters
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE) ) );
      assertNotNull(l);
      assertEquals(5, l.size());

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE), new Order("title"), 2, 2) );
      assertEquals(2, l.size());
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction[] { 
                  new Restriction("hiddenItem", Boolean.FALSE, Restriction.EQUALS),
                  new Restriction("title", titles)
            }, new Order("title")) );
      assertEquals(2, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto3));

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction[] { 
                  new Restriction("hiddenItem", Boolean.FALSE, Restriction.EQUALS),
                  new Restriction("title", titles)
            }, new Order("title"), 0, 0, false) );
      assertEquals(5, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto5));
      assertTrue(l.contains(gto6));

      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction[] { 
                  new Restriction("hiddenItem", Boolean.FALSE, Restriction.EQUALS),
                  new Restriction("title", titles)
            }, new Order("title"), 1, 2, false) );
      assertEquals(2, l.size());
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));

      // test that empty search is ok
      l = genericDao.findBySearch(GenericTestObject.class, new Search() );
      assertEquals(6, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto5));
      assertTrue(l.contains(gto6));

      // test search with only order is ok
      Search orderOnly = new Search();
      orderOnly.addOrder( new Order("title") );
      l = genericDao.findBySearch(GenericTestObject.class, orderOnly );
      assertEquals(6, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto5));
      assertTrue(l.contains(gto6));

      // null search causes exception
      try {
         l = genericDao.findBySearch(GenericTestObject.class, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }


      // now test the ability to deal with foreign keys
      List<GenericTestParentObject> pl = null;

      pl = genericDao.findBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", "", Restriction.NOT_NULL ) );
      assertEquals(2, pl.size());
      assertTrue(pl.contains(gtpo1));
      assertTrue(pl.contains(gtpo2));

      pl = genericDao.findBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", gto4.getId() ) );
      assertEquals(1, pl.size());
      assertTrue(pl.contains(gtpo1));

      pl = genericDao.findBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", gto5.getId() ) );
      assertEquals(1, pl.size());
      assertTrue(pl.contains(gtpo2));

      pl = genericDao.findBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", new Long(10000000) ) );
      assertEquals(0, pl.size());
   }

   public void testFindBySearchInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      List<GenericTestObject> l = genericDao.findBySearch(GenericTestObject.class, 
            new Search("hiddenItem", Boolean.FALSE) );
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertNotNull(intercept.search);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("afterRead", intercept.intercept);
      assertEquals(4, intercept.ids.length);
      assertNotNull(intercept.search);
      assertNotNull(intercept.entities);
      assertEquals(4, intercept.entities.length);
      assertEquals(gto1, intercept.entities[0]);
      assertEquals(gto2, intercept.entities[1]);
      assertEquals(gto3, intercept.entities[2]);
      assertEquals(gto6, intercept.entities[3]);

      // test the various searches and filters
      dataInterceptor.reset();
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE), new Order("title"), 2, 2) );
      assertEquals(2, l.size());
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));

      assertEquals(2, dataInterceptor.getIntercepts().size());
      intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertNotNull(intercept.search);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("afterRead", intercept.intercept);
      assertEquals(2, intercept.ids.length);
      assertNotNull(intercept.search);
      assertNotNull(intercept.entities);
      assertEquals(2, intercept.entities.length);
      assertEquals(gto3, intercept.entities[0]);
      assertEquals(gto4, intercept.entities[1]);

      // now test the ability to deal with no results
      dataInterceptor.reset();
      l = genericDao.findBySearch(GenericTestObject.class, 
            new Search( "id", new Long(9999999)) );
      assertEquals(0, l.size());

      assertEquals(2, dataInterceptor.getIntercepts().size());
      intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertNotNull(intercept.search);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("findBySearch", intercept.operation);
      assertEquals("afterRead", intercept.intercept);
      assertEquals(0, intercept.ids.length);
      assertNotNull(intercept.search);
      assertNotNull(intercept.entities);
      assertEquals(0, intercept.entities.length);
   }

   public void testFindOneBySearch() {
      GenericTestObject gto = null;
      GenericTestParentObject gtpo = null;

      String[] onetitle = new String[] {gto3.getTitle()};
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());

      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE), new Order("title"), 2, 2) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());

      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", "XXXXXXXXXXXXXX") );
      assertNull(gto);

      // test foreign keys
      gtpo = genericDao.findOneBySearch(GenericTestParentObject.class, 
            new Search( "gto.id", gto5.getId() ) );
      assertNotNull(gtpo);
      assertEquals(gtpo2.getUid(), gtpo.getUid());
   }

   public void testFindOneBySearchInterceptors() {
      GenericTestObject gto = null;

      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      String[] onetitle = new String[] {gto3.getTitle()};
      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", onetitle) );
      assertNotNull(gto);
      assertEquals(gto3.getId(), gto.getId());

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findOneBySearch", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertNotNull(intercept.search);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("findOneBySearch", intercept.operation);
      assertEquals("afterRead", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertNotNull(intercept.search);
      assertNotNull(intercept.entities);
      assertEquals(1, intercept.entities.length);

      dataInterceptor.reset();

      gto = genericDao.findOneBySearch(GenericTestObject.class, 
            new Search("title", "XXXXXXXXXXXXXX") );
      assertNull(gto);

      assertEquals(1, dataInterceptor.getIntercepts().size());
      intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findOneBySearch", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertNotNull(intercept.search);
      assertEquals(null, intercept.entities);
   }


   // DEPRECATED TESTS BELOW

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#countByProperties(java.lang.Class, java.lang.String[], java.lang.Object[])}.
    */
   public void testCountByPropertiesClassStringArrayObjectArray() {
      int count = genericDao.countByProperties(GenericTestObject.class, 
            new String[] {"hiddenItem"}, new Object[] {Boolean.FALSE});
      assertEquals(4, count);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#countByProperties(java.lang.Class, java.lang.String[], java.lang.Object[], int[])}.
    */
   public void testCountByPropertiesClassStringArrayObjectArrayIntArray() {
      int count = genericDao.countByProperties(GenericTestObject.class, 
            new String[] {"hiddenItem"}, new Object[] {Boolean.TRUE}, 
            new int[] {ByPropsFinder.NOT_EQUALS});
      assertEquals(4, count);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#findByProperties(java.lang.Class, java.lang.String[], java.lang.Object[])}.
    */
   @SuppressWarnings({ "unchecked" })
   public void testFindByPropertiesClassStringArrayObjectArray() {
      List l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"hiddenItem"}, new Object[] {Boolean.FALSE});
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));

      // now do a couple tests on the array handling ability of the system
      String[] titles = new String[] {gto1.getTitle(), gto3.getTitle(), gto5.getTitle()};
      l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"title"}, new Object[] {titles});
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto5));

      String[] onetitle = new String[] {gto3.getTitle()};
      l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"title"}, new Object[] {onetitle});
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(gto3));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#findByProperties(java.lang.Class, java.lang.String[], java.lang.Object[], int[])}.
    */
   @SuppressWarnings({ "unchecked" })
   public void testFindByPropertiesClassStringArrayObjectArrayIntArray() {
      List l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"hiddenItem"}, new Object[] {Boolean.FALSE}, 
            new int[] {ByPropsFinder.NOT_EQUALS});
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto5));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#findByProperties(java.lang.Class, java.lang.String[], java.lang.Object[], int[], java.lang.String[])}.
    */
   @SuppressWarnings({ "unchecked" })
   public void testFindByPropertiesClassStringArrayObjectArrayIntArrayStringArray() {
      List l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"hiddenItem"}, new Object[] {Boolean.FALSE}, 
            new int[] {ByPropsFinder.EQUALS}, new String[] {"title"});

      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto6));
      assertEquals(l.get(0), gto1);
      assertEquals(l.get(1), gto2);
      assertEquals(l.get(2), gto3);
      assertEquals(l.get(3), gto6);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#findByProperties(java.lang.Class, java.lang.String[], java.lang.Object[], int[], int, int)}.
    */
   @SuppressWarnings({ "unchecked" })
   public void testFindByPropertiesClassStringArrayObjectArrayIntArrayIntInt() {
      List l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"title"}, new Object[] {"aaronz test%"}, 
            new int[] {ByPropsFinder.LIKE}, 0, 4);
      assertNotNull(l);
      assertEquals(4, l.size());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateBasicGenericDao#findByProperties(java.lang.Class, java.lang.String[], java.lang.Object[], int[], java.lang.String[], int, int)}.
    */
   @SuppressWarnings({ "unchecked" })
   public void testFindByPropertiesClassStringArrayObjectArrayIntArrayStringArrayIntInt() {
      List l = genericDao.findByProperties(GenericTestObject.class, 
            new String[] {"title"}, new Object[] {"aaronz test%"}, 
            new int[] {ByPropsFinder.LIKE}, 
            new String[] {"title"+ByPropsFinder.DESC}, 1, 3);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(! l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));
      assertTrue(! l.contains(gto5));
      assertEquals(l.get(0), gto4);
      assertEquals(l.get(1), gto3);
      assertEquals(l.get(2), gto2);
   }

}
