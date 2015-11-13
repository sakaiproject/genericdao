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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.sakaiproject.genericdao.api.GenericDao;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao} caching
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestGenericDaoCaching extends AbstractTestBaseDao {

   protected GenericDao genericDao; 

   // run this before each test starts and as part of the transaction
   @Before
   public void onSetUp() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.GenericDao.caching");
      if (genericDao == null) {
         throw new RuntimeException("onSetUp: GenericDao.caching could not be retrieved from spring context");
      }

      commonCaching();
      commonStartup(genericDao);
   }

   public void testFindById() {
      cacheProvider.reset();

      Long gtoId = gto1.getId();
      assertNotNull(gtoId);
      GenericTestObject gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(gto);
      assertEquals(gto, gto1);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, Long.valueOf(999999));
      assertNull(gto);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(2, cacheProvider.size(GenericTestObject.class.getName()));
      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, Long.valueOf(999999));
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, Long.valueOf(999999));
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );

      Long gtpoId = gtpo1.getUid();
      assertNotNull(gtpoId);
      GenericTestParentObject gtpo = (GenericTestParentObject) genericDao.findById(GenericTestParentObject.class, gtpoId);
      assertNotNull(gtpo);
      assertEquals(gtpo, gtpo1);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestParentObject.class.getName()));
      gtpo = (GenericTestParentObject) genericDao.findById(GenericTestParentObject.class, gtpoId);
      assertNotNull(gtpo);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
      gtpo = (GenericTestParentObject) genericDao.findById(GenericTestParentObject.class, gtpoId);
      assertNotNull(gtpo);
      assertTrue( cacheProvider.getLastAction().startsWith("get:") );
   }

   public void testCreate() {
      cacheProvider.reset();

      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.create(gto);

      assertTrue( cacheProvider.getLastAction().startsWith("clear:") );
      assertFalse( cacheProvider.exists(GenericTestParentObject.class.getName(), gto.getId().toString()) );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));

      Long gtoId = gto.getId();
      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(t1);
      assertEquals(t1, gto);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
   }

   public void testUpdate() {
      cacheProvider.reset();

      genericDao.findById(GenericTestObject.class, gto1.getId());
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
      assertTrue( cacheProvider.exists(GenericTestObject.class.getName(), gto1.getId().toString()) );

      gto1.setTitle("New title");
      genericDao.update(gto1);

      assertTrue( cacheProvider.getLastAction().startsWith("remove:") );
      assertFalse( cacheProvider.exists(GenericTestParentObject.class.getName(), gto1.getId().toString()) );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));

      GenericTestObject t2 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gto1.getId());
      assertNotNull(t2);
      assertEquals("New title", t2.getTitle());
      assertEquals(t2, gto1);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));
   }

   public void testSave() {
      cacheProvider.reset();

      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.save(gto);

      assertTrue( cacheProvider.getLastAction().startsWith("clear:") );
      assertFalse( cacheProvider.exists(GenericTestParentObject.class.getName(), gto.getId().toString()) );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));

      Long gtoId = gto.getId();
      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(t1);
      assertEquals(t1, gto);

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));

      // test to see if updates work
      gto1.setTitle("New title");
      genericDao.save(gto1);

      assertTrue( cacheProvider.getLastAction().startsWith("remove:") );
      assertFalse( cacheProvider.exists(GenericTestParentObject.class.getName(), gto1.getId().toString()) );
      assertEquals(1, cacheProvider.size(GenericTestObject.class.getName()));

      GenericTestObject t2 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gto1.getId());
      assertNotNull(t2);
      assertEquals(t2, gto1);
      assertEquals("New title", t2.getTitle());

      assertTrue( cacheProvider.getLastAction().startsWith("put:") );
      assertEquals(2, cacheProvider.size(GenericTestObject.class.getName()));
   }

   public void testDeleteClassSerializable() {
      cacheProvider.reset();

      Long gtoId = gto1.getId();
      boolean b = genericDao.delete(GenericTestObject.class, gtoId);
      assertEquals(b, true);

      assertTrue( cacheProvider.getLastAction().startsWith("remove:") );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(2, cacheProvider.getActionRecord().size());

      b = genericDao.delete(GenericTestObject.class, Long.valueOf(-100));
      assertEquals(b, false);

      // no cache action
      assertEquals(2, cacheProvider.getActionRecord().size());
   }

   public void testDelete() {
      cacheProvider.reset();

      Long gtoId = gto1.getId();
      genericDao.delete(gto1);
      assertNotNull(gtoId);

      assertTrue( cacheProvider.getLastAction().startsWith("remove:") );
      assertEquals(0, cacheProvider.size(GenericTestObject.class.getName()));
      assertEquals(2, cacheProvider.getActionRecord().size());
   }

}
