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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.genericdao.api.GenericDao;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.test.CrazyTestObject;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CrazyGenericDaoTest extends AbstractTransactionalSpringContextTests {

    @Override
    protected String[] getConfigLocations() {
        // point to the spring-*.xml file, must be on the classpath
        // (add component/src/webapp/WEB-INF to the build path in Eclipse)
        return new String[] {"spring-common.xml","spring-jdbc.xml"};
    }

    protected GeneralGenericDao genericDao; 

    public CrazyTestObject gto1;
    public CrazyTestObject gto2;
    public CrazyTestObject gto3;
    public CrazyTestObject gto4;
    public CrazyTestObject gto5;
    public CrazyTestObject gto6;

    // the values to return for fake data
    public final static String TEST_TITLE = "aaronz test object";

    // run this before each test starts
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        gto1 = new CrazyTestObject(TEST_TITLE, Boolean.FALSE);
        gto2 = new CrazyTestObject(TEST_TITLE + "2", Boolean.FALSE);
        gto3 = new CrazyTestObject(TEST_TITLE + "3", Boolean.FALSE);
        gto4 = new CrazyTestObject(TEST_TITLE + "4", Boolean.TRUE);
        gto5 = new CrazyTestObject(TEST_TITLE + "5", Boolean.TRUE);
        gto6 = new CrazyTestObject("number six", Boolean.FALSE);
    }

    public void preloadGTOs(GenericDao dao) {
        // preload data if desired
        dao.create(gto1);
        dao.create(gto2);
        dao.create(gto3);
        dao.create(gto4);
        dao.create(gto5);
        dao.create(gto6);
    }

    // run this before each test starts and as part of the transaction
    @Override
    protected void onSetUpInTransaction() {
        // get the GenericDaoFinder from the spring context (you should inject this)
        genericDao = (GeneralGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.CrazyGenericDao");
        if (genericDao == null) {
            throw new RuntimeException("onSetUpInTransaction: GenericDao could not be retrieved from spring context");
        }

        // preload data if desired
        preloadGTOs(genericDao);
    }

    public void testGetPersistentClasses() {
        List<Class<?>> l = genericDao.getPersistentClasses();
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains(CrazyTestObject.class));
    }

    public void testGetIdProperty() {
        String s = null;

        s = genericDao.getIdProperty(CrazyTestObject.class);
        assertNotNull(s);
        assertEquals(s, "id");

        s = genericDao.getIdProperty(String.class);
        assertNull(s);
    }

    public void testFindById() {
        String gtoId = gto1.getId();
        assertNotNull(gtoId);
        CrazyTestObject gto = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gtoId);
        assertNotNull(gto);
        assertEquals(gto, gto1);

        gto = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, "99999999");
        assertNull(gto);
    }

    public void testCreate() {
        // test to see if creates work
        CrazyTestObject gto = new CrazyTestObject(TEST_TITLE, Boolean.FALSE);
        genericDao.create(gto);
        String gtoId = gto.getId();
        CrazyTestObject t1 = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gtoId);
        assertNotNull(t1);
        assertEquals(t1, gto);

        // try to create an existing object
        try {
            genericDao.create(gto2);
            fail("should have thrown Exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        } // other exceptions should cause a test failure
    }

    public void testUpdate() {
        gto1.setTitle("New title");
        genericDao.update(gto1);
        CrazyTestObject t2 = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gto1.getId());
        assertNotNull(t2);
        assertEquals("New title", t2.getTitle());
        assertEquals(t2, gto1);

        // try to update an unsaved object
        try {
            CrazyTestObject gto = new CrazyTestObject(TEST_TITLE, Boolean.FALSE);
            genericDao.update(gto);
            fail("should have thrown Exception");
        } catch (Exception e) {
            assertNotNull(e);
        } // other exceptions should cause a test failure
    }

    public void testSave() {
        // test to see if creates work
        CrazyTestObject gto = new CrazyTestObject(TEST_TITLE, Boolean.FALSE);
        genericDao.save(gto);
        String gtoId = gto.getId();
        CrazyTestObject t1 = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gtoId);
        assertNotNull(t1);
        assertEquals(t1, gto);

        // test to see if updates work
        gto1.setTitle("New title");
        genericDao.save(gto1);
        CrazyTestObject t2 = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gto1.getId());
        assertNotNull(t2);
        assertEquals(t2, gto1);
        assertEquals("New title", t2.getTitle());

        // have to test for the exception at the end or spring will throw an UnexpectedRollbackException
        try {
            genericDao.save(null);
            fail("null value saved");
        } catch (Exception e) {
            assertNotNull(e);
        } // other exceptions should cause a test failure
    }

    public void testDeleteClassSerializable() {
        String gtoId = gto1.getId();
        boolean b = genericDao.delete(CrazyTestObject.class, gtoId);
        assertEquals(b, true);

        CrazyTestObject gto = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gtoId);
        assertNull(gto);

        b = genericDao.delete(CrazyTestObject.class, "-100");
        assertEquals(b, false);
    }

    public void testDelete() {
        String gtoId = gto1.getId();
        genericDao.delete(gto1);
        assertNotNull(gtoId);

        CrazyTestObject gto = (CrazyTestObject) genericDao.findById(CrazyTestObject.class, gtoId);
        assertNull(gto);

        // have to test for the exception at the end or spring will throw an UnexpectedRollbackException
        try {
            genericDao.delete(null);
            fail("null value deleted");
        } catch (Exception e) {
            assertNotNull(e);
        } // other exceptions should cause a test failure
    }

    public void testCountBySearch() {
        long count = genericDao.countBySearch(CrazyTestObject.class, 
              new Search("hiddenItem", Boolean.FALSE) );
        assertEquals(4, count);

        count = genericDao.countBySearch(CrazyTestObject.class, 
              new Search( new Restriction("hiddenItem", Boolean.FALSE, Restriction.NOT_EQUALS) ) );
        assertEquals(2, count);

        count = genericDao.countBySearch(CrazyTestObject.class, 
              new Search( "title", "invalid" ) );
        assertEquals(0, count);
     }

     public void testFindBySearch() {
        List<CrazyTestObject> l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search("hiddenItem", Boolean.FALSE) );
        assertNotNull(l);
        assertEquals(4, l.size());
        assertTrue(l.contains(gto1));
        assertTrue(l.contains(gto2));
        assertTrue(l.contains(gto3));
        assertTrue(l.contains(gto6));

        // now do a couple tests on the array handling ability of the system
        String[] titles = new String[] {gto1.getTitle(), gto3.getTitle(), gto5.getTitle()};
        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search("title", titles) );
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains(gto1));
        assertTrue(l.contains(gto3));
        assertTrue(l.contains(gto5));

        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search("title", titles, Restriction.NOT_EQUALS) );
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains(gto2));
        assertTrue(l.contains(gto4));
        assertTrue(l.contains(gto6));

        String[] onetitle = new String[] {gto3.getTitle()};
        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search("title", onetitle) );
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains(gto3));

        // test the various searches and filters
        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE) ) );
        assertNotNull(l);
        assertEquals(5, l.size());

        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE), new Order("title"), 2, 2) );
        assertEquals(2, l.size());
        assertTrue(l.contains(gto3));
        assertTrue(l.contains(gto4));

        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search( new Restriction[] { 
                    new Restriction("hiddenItem", Boolean.FALSE, Restriction.EQUALS),
                    new Restriction("title", titles)
              }, new Order("title")) );
        assertEquals(2, l.size());
        assertTrue(l.contains(gto1));
        assertTrue(l.contains(gto3));

        l = genericDao.findBySearch(CrazyTestObject.class, 
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

        l = genericDao.findBySearch(CrazyTestObject.class, 
              new Search( new Restriction[] { 
                    new Restriction("hiddenItem", Boolean.FALSE, Restriction.EQUALS),
                    new Restriction("title", titles)
              }, new Order("title"), 1, 2, false) );
        assertEquals(2, l.size());
        assertTrue(l.contains(gto2));
        assertTrue(l.contains(gto3));

        // test that empty search is ok
        l = genericDao.findBySearch(CrazyTestObject.class, new Search() );
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
        l = genericDao.findBySearch(CrazyTestObject.class, orderOnly );
        assertEquals(6, l.size());
        assertTrue(l.contains(gto1));
        assertTrue(l.contains(gto2));
        assertTrue(l.contains(gto3));
        assertTrue(l.contains(gto4));
        assertTrue(l.contains(gto5));
        assertTrue(l.contains(gto6));

        // null search causes exception
        try {
           l = genericDao.findBySearch(CrazyTestObject.class, null);
           fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
           assertNotNull(e.getMessage());
        }

     }

     public void testFindOneBySearch() {
        CrazyTestObject gto = null;

        String[] onetitle = new String[] {gto3.getTitle()};
        gto = genericDao.findOneBySearch(CrazyTestObject.class, 
              new Search("title", onetitle) );
        assertNotNull(gto);
        assertEquals(gto3.getId(), gto.getId());

        gto = genericDao.findOneBySearch(CrazyTestObject.class, 
              new Search( new Restriction("title", TEST_TITLE+"%", Restriction.LIKE), new Order("title"), 2, 2) );
        assertNotNull(gto);
        assertEquals(gto3.getId(), gto.getId());

        gto = genericDao.findOneBySearch(CrazyTestObject.class, 
              new Search("title", "XXXXXXXXXXXXXX") );
        assertNull(gto);
     }

     public void testCountAll() {
         int count = 0;

         count = genericDao.countAll(CrazyTestObject.class);
         assertEquals(6, count);
      }

      public void testFindAllClass() {
         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(6, l.size());
         assertTrue(l.contains(gto1));
         assertTrue(l.contains(gto2));
         assertTrue(l.contains(gto3));
         assertTrue(l.contains(gto4));
         assertTrue(l.contains(gto5));
         assertTrue(l.contains(gto6));
      }

      public void testFindAllClassIntInt() {
         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class, 0, 2);
         assertNotNull(l);
         assertEquals(2, l.size());
      }

      public void testDeleteSetIds() {
         genericDao.deleteSet(CrazyTestObject.class, new Serializable[] {gto2.getId(), gto3.getId()});

         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(4, l.size());
         assertTrue(! l.contains(gto2));
         assertTrue(! l.contains(gto3));

         // delete nothing is ok
         genericDao.deleteSet(CrazyTestObject.class, new Serializable[] {});
      }

      /**
       * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#deleteMixedSet(java.util.Set[])}.
       */
      @SuppressWarnings("unchecked")
      public void testDeleteMixedSet() {
         Set deleteSet = new HashSet();
         deleteSet.add(gto2);
         deleteSet.add(gto3);
         Set[] setArray = new Set[] { deleteSet };
         genericDao.deleteMixedSet(setArray);

         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(4, l.size());
         assertTrue(! l.contains(gto2));
         assertTrue(! l.contains(gto3));


         // Now check that invalid objects cause failure
         deleteSet = new HashSet();
         deleteSet.add(gto1);
         Set deleteFailSet = new HashSet();
         deleteFailSet.add("string"); // non-matching object type
         deleteFailSet.add("string2"); // non-matching object type
         setArray = new Set[] { deleteSet, deleteFailSet };
         try {
            genericDao.deleteMixedSet(setArray);
            fail("Should have thrown an exception before getting here");
         } catch (IllegalArgumentException e) {
            assertNotNull(e);
         } catch (Exception e) {
            fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
         }

         l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(4, l.size());
         assertTrue(l.contains(gto1));
         assertTrue(! l.contains(gto2));
         assertTrue(! l.contains(gto3));
      }

      /**
       * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#deleteSet(java.util.Set)}.
       */
      public void testDeleteSet() {
         Set<CrazyTestObject> deleteSet = new HashSet<CrazyTestObject>();
         deleteSet.add(gto1);
         deleteSet.add(gto2);
         genericDao.deleteSet(deleteSet);

         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(4, l.size());
         assertTrue(! l.contains(gto1));
         assertTrue(! l.contains(gto2));

         // delete nothing is ok
         genericDao.deleteSet( new HashSet<CrazyTestObject>() );

         // Now try to cause various Exceptions
//       test no longer needed
//       Set<CrazyTestObject> deleteFailSet = new HashSet<CrazyTestObject>();
//       deleteFailSet.add(gto4);
//       deleteFailSet.add("string"); // non-matching object type
//       try {
//       genericDao.deleteSet(deleteFailSet);
//       fail("Should have thrown an exception before getting here");
//       } catch (IllegalArgumentException e) {
//       assertNotNull(e);
//       } catch (Exception e) {
//       fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//       }

//       l = genericDao.findAll(CrazyTestObject.class);
//       assertNotNull(l);
//       assertEquals(3, l.size());
//       assertTrue(l.contains(gto4));

         Set<String> deleteFailSet = new HashSet<String>();
         deleteFailSet.add("string"); // non-persistent object type
         try {
            genericDao.deleteSet(deleteFailSet);
            fail("Should have thrown an exception before getting here");
         } catch (IllegalArgumentException e) {
            assertNotNull(e);
         } catch (Exception e) {
            fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
         }

         l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(4, l.size());
         assertTrue(l.contains(gto4));

         // had to remove this test because it depends on order -AZ
//       CrazyTestObject gto = new CrazyTestObject("title", Boolean.TRUE);
//       deleteFailSet = new HashSet();
//       // I don't like that order is important for this test to work... -AZ
//       deleteFailSet.add(gto); // object is not in the DB
//       deleteFailSet.add(gto4);
//       try {
//       genericDao.deleteSet(deleteFailSet);
//       fail("Should have thrown an exception before getting here");
//       } catch (InvalidDataAccessApiUsageException e) {
//       assertNotNull(e);
//       } catch (Exception e) {
//       fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//       }

//       l = genericDao.findAll(CrazyTestObject.class);
//       assertNotNull(l);
//       assertEquals(3, l.size());
//       assertTrue(l.contains(gto4));
      }

      /**
       * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveMixedSet(java.util.Set[])}.
       */
      @SuppressWarnings("unchecked")
      public void testSaveMixedSet() {
         CrazyTestObject gtoA = new CrazyTestObject("titleA", Boolean.TRUE);
         CrazyTestObject gtoB = new CrazyTestObject("titleB", Boolean.FALSE);
         Set saveSet = new HashSet();
         saveSet.add(gtoA);
         saveSet.add(gtoB);
         Set[] setArray = new Set[] { saveSet };
         genericDao.saveMixedSet(setArray);

         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(8, l.size());

         CrazyTestObject gtoC = new CrazyTestObject("titleC", Boolean.TRUE);
         saveSet = new HashSet<CrazyTestObject>();
         gto3.setTitle("XXXXX");
         saveSet.add(gto3);
         saveSet.add(gtoC);
         setArray = new Set[] { saveSet };
         genericDao.saveMixedSet(setArray);

         CrazyTestObject one = genericDao.findById(CrazyTestObject.class, gto3.getId());
         assertEquals("XXXXX", one.getTitle());

         l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(9, l.size());


         // Now check that invalid objects cause failure
         Set saveFailSet = new HashSet();
         saveFailSet.add("string"); // non-matching object type
         saveFailSet.add("string2"); // non-matching object type
         CrazyTestObject gtoD = new CrazyTestObject("titleD", Boolean.TRUE);
         saveSet = new HashSet();
         saveSet.add(gtoD);
         setArray = new Set[] { saveSet, saveFailSet };
         try {
            genericDao.saveMixedSet(setArray);
            fail("Should have thrown an exception before getting here");
         } catch (IllegalArgumentException e) {
            assertNotNull(e);
         } catch (Exception e) {
            fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
         }

         l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(9, l.size());
         assertTrue(! l.contains(gtoD));
      }

      /**
       * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveSet(java.util.Set)}.
       */
      public void testSaveSet() {
         CrazyTestObject gtoA = new CrazyTestObject("titleA", Boolean.TRUE);
         CrazyTestObject gtoB = new CrazyTestObject("titleB", Boolean.FALSE);
         // batch insert
         Set<CrazyTestObject> saveSet = new HashSet<CrazyTestObject>();
         saveSet.add(gtoA);
         saveSet.add(gtoB);
         genericDao.saveSet(saveSet);

         List<CrazyTestObject> l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(8, l.size());

         // batch updates
         saveSet = new HashSet<CrazyTestObject>();
         gto1.setTitle("XXXXX");
         gto2.setTitle("XXXXX");
         saveSet.add(gto1);
         saveSet.add(gto2);
         genericDao.saveSet(saveSet);

         CrazyTestObject one = genericDao.findById(CrazyTestObject.class, gto1.getId());
         assertEquals("XXXXX", one.getTitle());
         CrazyTestObject two = genericDao.findById(CrazyTestObject.class, gto2.getId());
         assertEquals("XXXXX", two.getTitle());

         // Now try to cause various Exceptions

         // save empty set is OK
         genericDao.saveSet( new HashSet<CrazyTestObject>() );

         //    test no longer needed
//       CrazyTestObject gtoC = new CrazyTestObject("titleC", Boolean.TRUE);
//       saveSet = new HashSet<CrazyTestObject>();
//       saveSet.add(gtoC);
//       saveSet.add("string"); // mixed types
//       try {
//       genericDao.saveSet(saveSet);
//       fail("Should have thrown an exception before getting here");
//       } catch (IllegalArgumentException e) {
//       assertNotNull(e);
//       } catch (Exception e) {
//       fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//       }

//       l = genericDao.findAll(CrazyTestObject.class);
//       assertNotNull(l);
//       assertEquals(7, l.size());
//       assertTrue(! l.contains(gtoC));

         Set<String> failSaveSet = new HashSet<String>();
         failSaveSet.add("string"); // not a persistent type
         try {
            genericDao.saveSet(failSaveSet);
            fail("Should have thrown an exception before getting here");
         } catch (IllegalArgumentException e) {
            assertNotNull(e);
         } catch (Exception e) {
            fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
         }

         l = genericDao.findAll(CrazyTestObject.class);
         assertNotNull(l);
         assertEquals(8, l.size());
      }

}
