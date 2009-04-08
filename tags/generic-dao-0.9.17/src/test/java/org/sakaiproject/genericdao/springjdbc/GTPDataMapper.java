/**
 * $Id$
 * $URL$
 * GTPDataMapper.java - genericdao - May 9, 2008 10:14:37 AM - azeckoski
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.EntityColumnMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.test.GenericTestObject;
import org.sakaiproject.genericdao.test.GenericTestParentObject;

/**
 * Mapper for the Generic Test Parent
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class GTPDataMapper implements DataMapper, EntityColumnMapper {

   public String getIdPropertyName() {
      return "uid";
   }

   public Class<?> getPersistentType() {
      return GenericTestParentObject.class;
   }

   public NamesRecord getPropertyToColumnNamesMapping() {
      NamesRecord nr = new NamesRecord();
      nr.setNameMapping("uid", "GTP_ID");
      nr.setNameMapping("title", "GTP_TITLE");
      nr.setNameMapping("gto", "GTP_GTO");
      return nr;
   }

   public String getTableName() {
      return "SUPER_GENERIC_PARENT";
   }

   public String generateDDL(String databaseTypeConstant) {
      StringBuilder sql = new StringBuilder();
      if (DBTYPE_HSQLDB.equals(databaseTypeConstant)) {
         sql.append("CREATE TABLE {TABLENAME} ( " +
               "{ID}               BIGINT NOT NULL IDENTITY PRIMARY KEY," +
               "GTP_TITLE          VARCHAR(255) NOT NULL," +
               "GTP_GTO            BIGINT," +
               "FOREIGN KEY (GTP_GTO) REFERENCES {TABLENAME:GenericTestObject}({ID:GenericTestObject})" +
         ");\n");
      } else if (DBTYPE_MYSQL.equals(databaseTypeConstant)) {
         sql.append("CREATE TABLE {TABLENAME} ( " +
               "{ID}               bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY," +
               "GTP_TITLE          varchar(255) NOT NULL," +
               "GTP_GTO            bigint(20)," +
               "FOREIGN KEY (GTP_GTO) REFERENCES {TABLENAME:GenericTestObject}({ID:GenericTestObject})" +
         ");\n");
      } else {
         throw new UnsupportedOperationException("No support for the database type: " + databaseTypeConstant);
      }
      return sql.toString();      
   }

   public Object mapColumnsToObject(Map<String, Object> columnsData) {
      GenericTestParentObject gtpo = new GenericTestParentObject();
      for (Entry<String, Object> entry : columnsData.entrySet()) {
         String key = entry.getKey();
         Object value = entry.getValue();
         if ("GTP_ID".equals(key)) {
            gtpo.setUid((Long)value);
         } else if ("GTP_TITLE".equals(key)) {
            gtpo.setTitle((String)value);
         } else if ("GTP_GTO".equals(key)) {
            GenericTestObject gto = new GenericTestObject();
            gto.setId((Long)value);
            gtpo.setGto(gto);
         }
      }
      return null;
   }

   public Map<String, Object> mapObjectToColumns(Object persistentObject) {
      GenericTestParentObject gtpo = (GenericTestParentObject) persistentObject;
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("GTP_ID", gtpo.getUid());
      map.put("GTP_TITLE", gtpo.getTitle());
      map.put("GTP_GTO", gtpo.getGto() == null ? null : gtpo.getGto().getId());
      return map;
   }

}
