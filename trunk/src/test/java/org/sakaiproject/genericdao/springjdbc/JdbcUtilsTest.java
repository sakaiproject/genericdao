/**
 * $Id$
 * $URL$
 * JdbcUtils.java - genericdao - May 9, 2008 11:43:06 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.test.GenericTestObject;
import org.sakaiproject.genericdao.test.GenericTestParentObject;

import junit.framework.TestCase;


/**
 * Testing the utility methods used in JdbcGenericDao
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class JdbcUtilsTest extends TestCase {

   JdbcGenericDao dao;

   @Override
   protected void setUp() throws Exception {
      super.setUp();

      dao = new JdbcGenericDao();
      List<DataMapper> mappers = new ArrayList<DataMapper>();
      mappers.add( new GTODataMapper() );
      mappers.add( new SimpleDataMapper(GenericTestParentObject.class) );
      dao.setDataMappers(mappers);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#setDatabaseType(java.lang.String)}.
    */
   public void testSetDatabaseType() {
      dao.setDatabaseType("mysql");
      assertEquals("MYSQL", dao.getDatabaseType());

      dao.setDatabaseType("Oracle");
      assertEquals("ORACLE", dao.getDatabaseType());

      dao.setDatabaseType("MSSQL");
      assertEquals("MSSQL", dao.getDatabaseType());

      try {
         dao.setDatabaseType(null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#getTableNameFromClass(java.lang.Class)}.
    */
   public void testGetTableNameFromClass() {
      String tableName = null;

      tableName = dao.getTableNameFromClass(GenericTestObject.class);
      assertEquals("MY_GENERIC_TEST_OBJECT", tableName);

      tableName = dao.getTableNameFromClass(GenericTestParentObject.class);
      assertEquals("GENERIC_TEST_PARENT_OBJECT", tableName);

      try {
         tableName = dao.getTableNameFromClass(String.class);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#makeEntityFromMap(java.lang.Class, java.util.Map)}.
    */
   public void testMakeEntityFromMap() {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("GTO_ID", Long.valueOf(10));
      data.put("GTO_TITLE", "TEST");
      data.put("GTO_HIDDEN", false);

      GenericTestObject gto = dao.makeEntityFromMap(GenericTestObject.class, data);
      assertNotNull(gto);
      assertEquals(Long.valueOf(10), gto.getId());
      assertEquals("TEST", gto.getTitle());
      assertEquals(Boolean.FALSE, gto.getHiddenItem());

      data = new HashMap<String, Object>();
      data.put("UID", Long.valueOf(50));
      data.put("TITLE", "TESTING");
      data.put("GTO", Long.valueOf(100));

      GenericTestParentObject gtpo = dao.makeEntityFromMap(GenericTestParentObject.class, data);
      assertNotNull(gtpo);
      assertEquals(Long.valueOf(50), gtpo.getUid());
      assertEquals("TESTING", gtpo.getTitle());
      assertNotNull(gtpo.getGto());
      assertEquals(Long.valueOf(100), gtpo.getGto().getId());

   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#makeMapFromEntity(java.lang.Object)}.
    */
   public void testMakeMapFromEntity() {
      GenericTestObject gto = new GenericTestObject("TEST", false);
      gto.setId( Long.valueOf(10) );

      Map<String, Object> data = dao.makeMapFromEntity(gto);
      assertNotNull(data);
      assertEquals(Long.valueOf(10), data.get("GTO_ID"));
      assertEquals("TEST", data.get("GTO_TITLE"));
      assertEquals(false, data.get("GTO_HIDDEN"));

      GenericTestParentObject gtpo = new GenericTestParentObject("TESTING", gto);
      gtpo.setUid( Long.valueOf(100) );

      data = dao.makeMapFromEntity(gtpo);
      assertNotNull(data);
      assertEquals(Long.valueOf(100), data.get("UID"));
      assertEquals("TESTING", data.get("TITLE"));
      assertNotNull(data.get("GTO"));
      assertEquals(Long.valueOf(10), data.get("GTO") );
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#getIdValue(java.lang.Object)}.
    */
   public void testGetIdValue() {
      GenericTestObject gto = new GenericTestObject();
      gto.setId( Long.valueOf(100) );
      Object value = dao.getIdValue(gto);
      assertNotNull(value);
      assertEquals(Long.valueOf(100), value);

      GenericTestParentObject gtpo = new GenericTestParentObject();
      gtpo.setUid( Long.valueOf(555) );
      value = dao.getIdValue(gtpo);
      assertNotNull(value);
      assertEquals(Long.valueOf(555), value);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#getIdColumn(java.lang.Class)}.
    */
   public void testGetIdColumn() {
      String idc = null;

      idc = dao.getIdColumn(GenericTestObject.class);
      assertNotNull(idc);
      assertEquals("GTO_ID", idc);

      idc = dao.getIdColumn(GenericTestParentObject.class);
      assertNotNull(idc);
      assertEquals("UID", idc);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.springjdbc.JdbcGenericDao#handleTypeReplacements(java.lang.Class, java.lang.String)}.
    */
   public void testHandleTypeReplacements() {
      String ddl = null;

      ddl = "create {TABLENAME} ({ID} varchar, other varchar)";
      ddl = dao.handleTypeReplacements(GenericTestObject.class, ddl);
      assertNotNull(ddl);
      assertEquals("create MY_GENERIC_TEST_OBJECT (GTO_ID varchar, other varchar)", ddl);

      ddl = "create {TABLENAME} ({ID} varchar, other varchar)";
      ddl = dao.handleTypeReplacements(GenericTestParentObject.class, ddl);
      assertNotNull(ddl);
      assertEquals("create GENERIC_TEST_PARENT_OBJECT (UID varchar, other varchar)", ddl);

      ddl = "create {TABLENAME} ({ID} varchar, other varchar, " +
      "FOREIGN KEY (other) REFERENCES {TABLENAME:"+GenericTestObject.class.getName()+"}({ID:"+GenericTestObject.class.getName()+"}) )";
      ddl = dao.handleTypeReplacements(GenericTestParentObject.class, ddl);
      assertNotNull(ddl);
      assertEquals("create GENERIC_TEST_PARENT_OBJECT (UID varchar, other varchar, " +
            "FOREIGN KEY (other) REFERENCES MY_GENERIC_TEST_OBJECT(GTO_ID) )", ddl);

      ddl = "create {TABLENAME} ({ID} varchar, other varchar, " +
      "FOREIGN KEY (other) REFERENCES {TABLENAME:GenericTestObject}({ID:GenericTestObject}) )";
      ddl = dao.handleTypeReplacements(GenericTestParentObject.class, ddl);
      assertNotNull(ddl);
      assertEquals("create GENERIC_TEST_PARENT_OBJECT (UID varchar, other varchar, " +
            "FOREIGN KEY (other) REFERENCES MY_GENERIC_TEST_OBJECT(GTO_ID) )", ddl);

      ddl = "create {TABLENAME} ({ID} varchar, {COLUMNNAME:title} varchar)";
      ddl = dao.handleTypeReplacements(GenericTestObject.class, ddl);
      assertNotNull(ddl);
      assertEquals("create MY_GENERIC_TEST_OBJECT (GTO_ID varchar, GTO_TITLE varchar)", ddl);

   }

}
