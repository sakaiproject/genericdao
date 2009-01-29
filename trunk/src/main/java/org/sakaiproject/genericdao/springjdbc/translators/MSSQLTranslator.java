/**
 * $Id$
 * $URL$
 * MSSQLTranslator.java - genericdao - Apr 26, 2008 3:17:46 PM - azeckoski
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
 * Microsoft SQL Server (MSSQL) translator
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class MSSQLTranslator extends BasicTranslator {

   public String handlesDB() {
      return DBTYPE_MSSQL;
   }

   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "SELECT SCOPE_IDENTITY()";
   }

   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      // NOTE: no support for start/offset
      return new StringBuilder( sql.length()+8 )
         .append(sql)
         .insert( getAfterSelectInsertPoint(sql), " top " + limit )
         .toString();
   }

   private int getAfterSelectInsertPoint(String sql) {
      int selectIndex = sql.toLowerCase().indexOf( "select" );
      final int selectDistinctIndex = sql.toLowerCase().indexOf( "select distinct" );
      return selectIndex + ( selectDistinctIndex == selectIndex ? 15 : 6 );
   }

}
