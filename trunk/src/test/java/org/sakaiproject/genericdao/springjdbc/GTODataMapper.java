/**
 * $Id$
 * $URL$
 * GTODataMapper.java - genericdao - Apr 25, 2008 3:05:21 PM - azeckoski
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

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.EntityColumnMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.test.GenericTestObject;

/**
 * This maps the GTO object to database info
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class GTODataMapper implements DataMapper, EntityColumnMapper {

   public String getIdPropertyName() {
      return "id";
   }

   public Class<?> getPersistentType() {
      return GenericTestObject.class;
   }

   public String getTableName() {
      return "MY_GENERIC_TEST_OBJECT";
   }

   public NamesRecord getPropertyToColumnNamesMapping() {
      NamesRecord nr = new NamesRecord();
      nr.setNameMapping("id", "GTO_ID");
      nr.setNameMapping("title", "GTO_TITLE");
      nr.setNameMapping("hiddenItem", "GTO_HIDDEN");
      return nr;
   }

   public Object mapColumnsToObject(Map<String, Object> columnsData) {
      GenericTestObject gto = new GenericTestObject();
      for (String key : columnsData.keySet()) {
         Object value = columnsData.get(key);
         if ("GTO_ID".equals(key)) {
            gto.setId((Long)value);
         } else if ("GTO_TITLE".equals(key)) {
            gto.setTitle((String)value);
         } else if ("GTO_HIDDEN".equals(key)) {
            gto.setHiddenItem((Boolean)value);
         }
      }
      return gto;
   }

   public Map<String, Object> mapObjectToColumns(Object persistentObject) {
      GenericTestObject gto = (GenericTestObject) persistentObject;
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("GTO_ID", gto.getId());
      map.put("GTO_TITLE", gto.getTitle());
      map.put("GTO_HIDDEN", gto.getHiddenItem());
      return map;
   }

   public String generateDDL(String databaseTypeConstant) {
      StringBuilder sql = new StringBuilder();
      if (DBTYPE_HSQLDB.equals(databaseTypeConstant)) {
         sql.append("CREATE TABLE "+DDL_TABLENAME+" ( " +
               DDL_ID_COLUMN+"     BIGINT NOT NULL IDENTITY PRIMARY KEY," +
               "GTO_TITLE          VARCHAR(255) NOT NULL," +
               "GTO_HIDDEN         BOOLEAN" +
         ");\n");
      } else if (DBTYPE_MYSQL.equals(databaseTypeConstant)) {
         sql.append("CREATE TABLE "+DDL_TABLENAME+" ( " +
               DDL_ID_COLUMN+"     bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY," +
               "GTO_TITLE          VARCHAR(255) NOT NULL," +
               "GTO_HIDDEN         BOOLEAN" +
         ");\n");
      } else if (DBTYPE_ORACLE.equals(databaseTypeConstant)) {
         sql.append("CREATE TABLE "+DDL_TABLENAME+" ( " +
               DDL_ID_COLUMN+"     number(20) NOT NULL PRIMARY KEY," +
               "GTO_TITLE          VARCHAR(255) NOT NULL," +
               "GTO_HIDDEN         NUMBER(1)" +
         ");\n");
         sql.append("create sequence "+DDL_ID_SEQNAME+";\n");
         sql.append("create trigger bef_ins_TESTGTO " +
               "before insert on "+DDL_TABLENAME+" " +
               "for each row " +
               "begin " +
               "select "+DDL_ID_SEQNAME+".nextval " +
               "into :new."+DDL_ID_COLUMN+" from dual; " +
         "end;\n");
      } else {
         throw new UnsupportedOperationException("No support for the database type: " + databaseTypeConstant);
      }
      return sql.toString();
   }

}
