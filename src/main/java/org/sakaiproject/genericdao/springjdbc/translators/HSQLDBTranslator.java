/**
 * $Id$
 * $URL$
 * HSQLDBTranslator.java - genericdao - Apr 26, 2008 1:56:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc.translators;

/**
 * DB translator for HSQLDB
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class HSQLDBTranslator extends BasicTranslator {

   public String handlesDB() {
      return DBTYPE_HSQLDB;
   }

   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "CALL IDENTITY()";
   }

   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      return new StringBuilder( sql.length() + 10 )
         .append( sql )
         .insert( sql.toLowerCase().indexOf( "select" ) + 6, start > 0 ? " limit "+start+" "+limit : " top "+limit )
         .toString();
   }

}
