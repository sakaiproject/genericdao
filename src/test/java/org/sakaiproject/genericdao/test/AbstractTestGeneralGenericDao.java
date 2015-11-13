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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.genericdao.test.BasicDataInterceptor.Intercept;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GeneralGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestGeneralGenericDao extends AbstractTestBaseDao {

   protected GeneralGenericDao genericDao;

   // run this before each test starts and as part of the transaction
   @Before
   protected void onSetUp() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GeneralGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.GeneralGenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUp: CompleteGenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }

   public void testCountAll() {
      int count = 0;

      count = genericDao.countAll(GenericTestObject.class);
      assertEquals(6, count);

      count = genericDao.countAll(GenericTestParentObject.class);
      assertEquals(2, count);
   }

   public void testCountAllInterceptors() {
      int count = 0;

      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      count = genericDao.countAll(GenericTestObject.class);
      assertEquals(6, count);

      // no intercepts for counts
      assertEquals(0, dataInterceptor.getIntercepts().size());
   }

   public void testFindAllClass() {
      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(6, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto5));
      assertTrue(l.contains(gto6));

      List<GenericTestParentObject> lp = genericDao.findAll(GenericTestParentObject.class);
      assertNotNull(lp);
      assertEquals(2, lp.size());
      assertTrue(lp.contains(gtpo1));
      assertTrue(lp.contains(gtpo2));
   }

   public void testFindAllClassInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(6, l.size());

      // no intercepts for find all
      assertEquals(0, dataInterceptor.getIntercepts().size());
   }

   public void testFindAllClassIntInt() {
      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class, 0, 2);
      assertNotNull(l);
      assertEquals(2, l.size());

      List<GenericTestParentObject> lp = genericDao.findAll(GenericTestParentObject.class, 0, 1);
      assertNotNull(lp);
      assertEquals(1, lp.size());
   }

   public void testDeleteSetIds() {
      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {gto2.getId(), gto3.getId()});

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(! l.contains(gto2));
      assertTrue(! l.contains(gto3));

      genericDao.deleteSet(GenericTestParentObject.class, new Serializable[] {gtpo1.getUid(), gtpo2.getUid()});

      List<GenericTestParentObject> lp = genericDao.findAll(GenericTestParentObject.class);
      assertNotNull(lp);
      assertEquals(0, lp.size());

      // delete nothing is ok
      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {});
   }

   public void testDeleteSetIdsInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {gto2.getId(), gto3.getId()});

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("deleteSet", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(2, intercept.ids.length);
      assertEquals(0, intercept.changes);
      assertNull(intercept.search);
      assertNull(intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("deleteSet", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(2, intercept.ids.length);
      assertEquals(2, intercept.changes);
      assertNull(intercept.search);
      assertNull(intercept.entities);

      dataInterceptor.reset();

      // delete nothing is ok
      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {});

      // nothing to intercept
      assertEquals(0, dataInterceptor.getIntercepts().size());
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

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(! l.contains(gto2));
      assertTrue(! l.contains(gto3));

      // now delete a mixed set
      Set s1 = new HashSet();
      s1.add(gto6);
      Set s2 = new HashSet();
      s2.add(gtpo2);
      Set[] mSet = new Set[] { s1, s2 };
      genericDao.deleteMixedSet(mSet);

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(! l.contains(gto2));
      assertTrue(! l.contains(gto3));
      assertTrue(! l.contains(gto6));

      List<GenericTestParentObject> lp = genericDao.findAll(GenericTestParentObject.class);
      assertNotNull(lp);
      assertEquals(1, lp.size());
      assertTrue(! lp.contains(gtpo2));

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

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(! l.contains(gto2));
      assertTrue(! l.contains(gto3));
      assertTrue(! l.contains(gto6));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#deleteSet(java.util.Set)}.
    */
   public void testDeleteSet() {
      Set<GenericTestObject> deleteSet = new HashSet<GenericTestObject>();
      deleteSet.add(gto1);
      deleteSet.add(gto2);
      genericDao.deleteSet(deleteSet);

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(! l.contains(gto1));
      assertTrue(! l.contains(gto2));

      // delete nothing is ok
      genericDao.deleteSet( new HashSet<GenericTestObject>() );

      // Now try to cause various Exceptions
//    test no longer needed
//    Set<GenericTestObject> deleteFailSet = new HashSet<GenericTestObject>();
//    deleteFailSet.add(gto4);
//    deleteFailSet.add("string"); // non-matching object type
//    try {
//    genericDao.deleteSet(deleteFailSet);
//    fail("Should have thrown an exception before getting here");
//    } catch (IllegalArgumentException e) {
//    assertNotNull(e);
//    } catch (Exception e) {
//    fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//    }

//    l = genericDao.findAll(GenericTestObject.class);
//    assertNotNull(l);
//    assertEquals(3, l.size());
//    assertTrue(l.contains(gto4));

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

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(4, l.size());
      assertTrue(l.contains(gto4));

      // had to remove this test because it depends on order -AZ
//    GenericTestObject gto = new GenericTestObject("title", Boolean.TRUE);
//    deleteFailSet = new HashSet();
//    // I don't like that order is important for this test to work... -AZ
//    deleteFailSet.add(gto); // object is not in the DB
//    deleteFailSet.add(gto4);
//    try {
//    genericDao.deleteSet(deleteFailSet);
//    fail("Should have thrown an exception before getting here");
//    } catch (InvalidDataAccessApiUsageException e) {
//    assertNotNull(e);
//    } catch (Exception e) {
//    fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//    }

//    l = genericDao.findAll(GenericTestObject.class);
//    assertNotNull(l);
//    assertEquals(3, l.size());
//    assertTrue(l.contains(gto4));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveMixedSet(java.util.Set[])}.
    */
   @SuppressWarnings("unchecked")
   public void testSaveMixedSet() {
      GenericTestObject gtoA = new GenericTestObject("titleA", Boolean.TRUE);
      GenericTestObject gtoB = new GenericTestObject("titleB", Boolean.FALSE);
      Set saveSet = new HashSet();
      saveSet.add(gtoA);
      saveSet.add(gtoB);
      Set[] setArray = new Set[] { saveSet };
      genericDao.saveMixedSet(setArray);

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(8, l.size());

      GenericTestObject gtoC = new GenericTestObject("titleC", Boolean.TRUE);
      saveSet = new HashSet<GenericTestObject>();
      gto3.setTitle("XXXXX");
      saveSet.add(gto3);
      saveSet.add(gtoC);
      setArray = new Set[] { saveSet };
      genericDao.saveMixedSet(setArray);

      GenericTestObject one = genericDao.findById(GenericTestObject.class, gto3.getId());
      assertEquals("XXXXX", one.getTitle());

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(9, l.size());

      // save a mixed set
      GenericTestObject gtoM1 = new GenericTestObject("titleM1", Boolean.FALSE);
      GenericTestParentObject gtpoA = new GenericTestParentObject("parentA", gtoM1);
      Set s1 = new HashSet();
      s1.add(gtoM1);
      Set s2 = new HashSet();
      s2.add(gtpoA);
      Set[] mSet = new Set[] { s1, s2 };
      genericDao.saveMixedSet(mSet);

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(10, l.size());

      List<GenericTestParentObject> lp = genericDao.findAll(GenericTestParentObject.class);
      assertNotNull(lp);
      assertEquals(3, lp.size());

      // Now check that invalid objects cause failure
      Set saveFailSet = new HashSet();
      saveFailSet.add("string"); // non-matching object type
      saveFailSet.add("string2"); // non-matching object type
      GenericTestObject gtoD = new GenericTestObject("titleD", Boolean.TRUE);
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

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(10, l.size());
      assertTrue(! l.contains(gtoD));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveSet(java.util.Set)}.
    */
   public void testSaveSet() {
      GenericTestObject gtoA = new GenericTestObject("titleA", Boolean.TRUE);
      GenericTestObject gtoB = new GenericTestObject("titleB", Boolean.FALSE);
      // batch insert
      Set<GenericTestObject> saveSet = new HashSet<GenericTestObject>();
      saveSet.add(gtoA);
      saveSet.add(gtoB);
      genericDao.saveSet(saveSet);

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(8, l.size());

      // batch updates
      saveSet = new HashSet<GenericTestObject>();
      gto1.setTitle("XXXXX");
      gto2.setTitle("XXXXX");
      saveSet.add(gto1);
      saveSet.add(gto2);
      genericDao.saveSet(saveSet);

      GenericTestObject one = genericDao.findById(GenericTestObject.class, gto1.getId());
      assertEquals("XXXXX", one.getTitle());
      GenericTestObject two = genericDao.findById(GenericTestObject.class, gto2.getId());
      assertEquals("XXXXX", two.getTitle());

      // Now try to cause various Exceptions

      // save empty set is OK
      genericDao.saveSet( new HashSet<GenericTestObject>() );

      //    test no longer needed
//    GenericTestObject gtoC = new GenericTestObject("titleC", Boolean.TRUE);
//    saveSet = new HashSet<GenericTestObject>();
//    saveSet.add(gtoC);
//    saveSet.add("string"); // mixed types
//    try {
//    genericDao.saveSet(saveSet);
//    fail("Should have thrown an exception before getting here");
//    } catch (IllegalArgumentException e) {
//    assertNotNull(e);
//    } catch (Exception e) {
//    fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
//    }

//    l = genericDao.findAll(GenericTestObject.class);
//    assertNotNull(l);
//    assertEquals(7, l.size());
//    assertTrue(! l.contains(gtoC));

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

      l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(8, l.size());
   }

}
