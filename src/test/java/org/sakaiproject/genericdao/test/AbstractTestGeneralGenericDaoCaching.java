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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GeneralGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestGeneralGenericDaoCaching extends AbstractTestBaseDao {

   protected GeneralGenericDao genericDao;

   // run this before each test starts and as part of the transaction
   @Before
   public void onSetUp() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GeneralGenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.GeneralGenericDao.caching");
      if (genericDao == null) {
         throw new RuntimeException("onSetUp: CompleteGenericDao could not be retrieved from spring context");
      }

      commonCaching();
      commonStartup(genericDao);
   }

   public void testCountAll() {
      int count = 0;

      cacheProvider.reset();

      count = genericDao.countAll(GenericTestObject.class);
      assertEquals(6, count);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size("search:" + GenericTestObject.class.getName()));

      count = genericDao.countAll(GenericTestObject.class);
      assertEquals(6, count);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      count = genericDao.countAll(GenericTestObject.class);
      assertEquals(6, count);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
   }

   public void testFindAllClass() {
      cacheProvider.reset();

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
      assertNotNull(l);
      assertEquals(6, l.size());
      assertTrue(l.contains(gto1));
      assertTrue(l.contains(gto2));
      assertTrue(l.contains(gto3));
      assertTrue(l.contains(gto4));
      assertTrue(l.contains(gto5));
      assertTrue(l.contains(gto6));

      // no caching
      assertTrue( cacheProvider.getLastAction() == null );
   }

   public void testFindAllClassIntInt() {
      cacheProvider.reset();

      List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class, 0, 2);
      assertNotNull(l);
      assertEquals(2, l.size());

      // no caching
      assertTrue( cacheProvider.getLastAction() == null );
   }

   public void testDeleteSetIds() {
      cacheProvider.reset();

      genericDao.findById(GenericTestObject.class, gto2.getId());
      genericDao.findById(GenericTestObject.class, gto3.getId());
      assertEquals(2, cacheProvider.size(GenericTestObject.class.getName()));
      assertTrue( cacheProvider.exists(GenericTestObject.class.getName(), gto2.getId().toString()) );
      assertTrue( cacheProvider.exists(GenericTestObject.class.getName(), gto3.getId().toString()) );
      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {gto2.getId(), gto3.getId()});

      assertTrue( cacheProvider.getLastAction().startsWith("clear:") );
      assertFalse( cacheProvider.exists(GenericTestObject.class.getName(), gto2.getId().toString()) );
      assertFalse( cacheProvider.exists(GenericTestObject.class.getName(), gto3.getId().toString()) );
      cacheProvider.reset();

      // delete nothing is ok
      genericDao.deleteSet(GenericTestObject.class, new Serializable[] {});

      // no action in cache when nothing to delete
      assertTrue( cacheProvider.getLastAction() == null );      
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveSet(java.util.Set)}.
    */
   public void testSaveSet() {
      cacheProvider.reset();

      GenericTestObject gtoA = new GenericTestObject("titleA", Boolean.TRUE);
      GenericTestObject gtoB = new GenericTestObject("titleB", Boolean.FALSE);
      // batch insert
      Set<GenericTestObject> saveSet = new HashSet<GenericTestObject>();
      saveSet.add(gtoA);
      saveSet.add(gtoB);
      genericDao.saveSet(saveSet);

      assertTrue( cacheProvider.getLastAction().startsWith("clear:") );
      cacheProvider.reset();

      genericDao.findById(GenericTestObject.class, gto1.getId());
      genericDao.findById(GenericTestObject.class, gto2.getId());
      assertEquals(2, cacheProvider.size(GenericTestObject.class.getName()));
      assertTrue( cacheProvider.exists(GenericTestObject.class.getName(), gto1.getId().toString()) );
      assertTrue( cacheProvider.exists(GenericTestObject.class.getName(), gto2.getId().toString()) );

      // batch updates
      saveSet = new HashSet<GenericTestObject>();
      gto1.setTitle("XXXXX");
      gto2.setTitle("XXXXX");
      saveSet.add(gto1);
      saveSet.add(gto2);
      genericDao.saveSet(saveSet);

      assertTrue( cacheProvider.getLastAction().startsWith("clear:") );
      assertFalse( cacheProvider.exists(GenericTestObject.class.getName(), gto2.getId().toString()) );
      assertFalse( cacheProvider.exists(GenericTestObject.class.getName(), gto3.getId().toString()) );
      cacheProvider.reset();

      genericDao.saveSet( new HashSet<GenericTestObject>() );
      // no action in cache when nothing to delete
      assertTrue( cacheProvider.getLastAction() == null );
   }

}
