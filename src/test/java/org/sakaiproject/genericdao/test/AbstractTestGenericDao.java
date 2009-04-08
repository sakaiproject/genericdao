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

import org.sakaiproject.genericdao.api.GenericDao;
import org.sakaiproject.genericdao.test.BasicDataInterceptor.Intercept;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class AbstractTestGenericDao extends AbstractTestBaseDao {

   protected GenericDao genericDao; 

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // get the GenericDaoFinder from the spring context (you should inject this)
      genericDao = (GenericDao) applicationContext.getBean("org.sakaiproject.genericdao.dao.GenericDao");
      if (genericDao == null) {
         throw new RuntimeException("onSetUpInTransaction: GenericDao could not be retrieved from spring context");
      }

      commonStartup(genericDao);
   }

   public void testGetPersistentClasses() {
      List<Class<?>> l = genericDao.getPersistentClasses();
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(GenericTestObject.class));
      assertTrue(l.contains(GenericTestParentObject.class));
   }

   public void testGetIdProperty() {
      String s = null;

      s = genericDao.getIdProperty(GenericTestObject.class);
      assertNotNull(s);
      assertEquals(s, "id");

      s = genericDao.getIdProperty(GenericTestParentObject.class);
      assertNotNull(s);
      assertEquals(s, "uid");

      s = genericDao.getIdProperty(String.class);
      assertNull(s);
   }

   public void testFindById() {
      Long gtoId = gto1.getId();
      assertNotNull(gtoId);
      GenericTestObject gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(gto);
      assertEquals(gto, gto1);

      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, Long.valueOf(9999999));
      assertNull(gto);

      Long gtpoId = gtpo1.getUid();
      assertNotNull(gtpoId);
      GenericTestParentObject gtpo = (GenericTestParentObject) genericDao.findById(GenericTestParentObject.class, gtpoId);
      assertNotNull(gtpo);
      assertEquals(gtpo, gtpo1);
   }

   public void testFindByIdInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      Long gtoId = gto1.getId();
      assertNotNull(gtoId);
      GenericTestObject gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(gto);
      assertEquals(gto, gto1);

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("findById", intercept.operation);
      assertEquals("beforeRead", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);

      assertEquals("findById", dataInterceptor.getLastIntercept().operation);
      assertEquals("afterRead", dataInterceptor.getLastIntercept().intercept);
      assertEquals(1, dataInterceptor.getLastIntercept().ids.length);
      assertEquals(gtoId, dataInterceptor.getLastIntercept().ids[0]);

      dataInterceptor.reset();
      Long invalidId = Long.valueOf(9999999);
      gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, invalidId);
      assertNull(gto);
      assertEquals(2, dataInterceptor.getIntercepts().size());
      assertEquals("afterRead", dataInterceptor.getLastIntercept().intercept);
      assertEquals(1, dataInterceptor.getLastIntercept().ids.length);
      assertEquals(invalidId, dataInterceptor.getLastIntercept().ids[0]);

      dataInterceptor.reset();
      Long gtpoId = gtpo1.getUid();
      assertNotNull(gtpoId);
      GenericTestParentObject gtpo = (GenericTestParentObject) genericDao.findById(GenericTestParentObject.class, gtpoId);
      assertNotNull(gtpo);
      assertEquals(gtpo, gtpo1);
      // this is not an intercepted type
      assertEquals(0, dataInterceptor.getIntercepts().size());
   }

   public void testCreate() {
      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.create(gto);
      Long gtoId = gto.getId();
      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
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

   public void testCreateInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.create(gto);
      Long gtoId = gto.getId();

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("create", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto, intercept.entities[0]);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("create", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto, intercept.entities[0]);
      
      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(t1);
      assertEquals(t1, gto);
   }

   public void testUpdate() {
      gto1.setTitle("New title");
      genericDao.update(gto1);
      GenericTestObject t2 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gto1.getId());
      assertNotNull(t2);
      assertEquals("New title", t2.getTitle());
      assertEquals(t2, gto1);

      // try to update an unsaved object
      try {
         GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
         genericDao.update(gto);
         fail("should have thrown Exception");
      } catch (Exception e) {
         assertNotNull(e);
      } // other exceptions should cause a test failure
   }

   public void testUpdateInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      gto1.setTitle("New title");
      genericDao.update(gto1);
      Long gtoId = gto1.getId();

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("update", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto1, intercept.entities[0]);
      assertEquals("New title", ((GenericTestObject)intercept.entities[0]).getTitle());

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("update", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto1, intercept.entities[0]);
      
      GenericTestObject t2 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gto1.getId());
      assertNotNull(t2);
      assertEquals("New title", t2.getTitle());
      assertEquals(t2, gto1);
   }

   public void testSave() {
      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.save(gto);
      Long gtoId = gto.getId();
      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(t1);
      assertEquals(t1, gto);

      // test to see if updates work
      gto1.setTitle("New title");
      genericDao.save(gto1);
      GenericTestObject t2 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gto1.getId());
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

   public void testSaveInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      // test to see if creates work
      GenericTestObject gto = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
      genericDao.save(gto);
      Long gtoId = gto.getId();

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("create", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(null, intercept.ids);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto, intercept.entities[0]);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("create", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto, intercept.entities[0]);

      GenericTestObject t1 = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNotNull(t1);
      assertEquals(t1, gto);

      // test to see if updates work
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      gto1.setTitle("New title");
      genericDao.save(gto1);
      gtoId = gto1.getId();

      assertEquals(2, dataInterceptor.getIntercepts().size());
      intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("update", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto1, intercept.entities[0]);
      assertEquals("New title", ((GenericTestObject)intercept.entities[0]).getTitle());

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("update", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(1, intercept.entities.length);
      assertEquals(gto1, intercept.entities[0]);      
   }

   public void testDeleteClassSerializable() {
      Long gtoId = gto1.getId();
      boolean b = genericDao.delete(GenericTestObject.class, gtoId);
      assertEquals(b, true);

      GenericTestObject gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNull(gto);

      b = genericDao.delete(GenericTestObject.class, Long.valueOf(-100));
      assertEquals(b, false);
   }

   public void testDelete() {
      Long gtoId = gto1.getId();
      genericDao.delete(gto1);
      assertNotNull(gtoId);

      GenericTestObject gto = (GenericTestObject) genericDao.findById(GenericTestObject.class, gtoId);
      assertNull(gto);

      // have to test for the exception at the end or spring will throw an UnexpectedRollbackException
      try {
         genericDao.delete(null);
         fail("null value deleted");
      } catch (Exception e) {
         assertNotNull(e);
      } // other exceptions should cause a test failure
   }

   public void testDeleteInterceptors() {
      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      Long gtoId = gto1.getId();
      boolean b = genericDao.delete(GenericTestObject.class, gtoId);
      assertEquals(b, true);

      assertEquals(2, dataInterceptor.getIntercepts().size());
      Intercept intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("delete", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("delete", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(null, intercept.entities);

      dataInterceptor.reset();
      assertEquals(0, dataInterceptor.getIntercepts().size());

      gtoId = gto2.getId();
      genericDao.delete(gto2);
      assertNotNull(gtoId);

      assertEquals(2, dataInterceptor.getIntercepts().size());
      intercept = dataInterceptor.getIntercepts().get(0);
      assertEquals("delete", intercept.operation);
      assertEquals("beforeWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(null, intercept.entities);

      intercept = dataInterceptor.getIntercepts().get(1);
      assertEquals("delete", intercept.operation);
      assertEquals("afterWrite", intercept.intercept);
      assertEquals(1, intercept.ids.length);
      assertEquals(gtoId, intercept.ids[0]);
      assertEquals(null, intercept.entities);
   }

}
