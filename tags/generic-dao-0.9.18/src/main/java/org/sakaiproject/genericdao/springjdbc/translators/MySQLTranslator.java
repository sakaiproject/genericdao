/**
 * $Id$
 * $URL$
 * MySQLTranslator.java - genericdao - Apr 26, 2008 1:51:14 PM - azeckoski
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
 * Translator for MySQL
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class MySQLTranslator extends BasicTranslator {

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#handlesDB()
    */
   public String handlesDB() {
      return DBTYPE_MYSQL;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#makeAutoIdQuery(java.lang.String, java.lang.String)
    */
   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "SELECT last_insert_id()";
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#makeLimitQuery(java.lang.String, java.lang.String, long, long)
    */
   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      return sql + " LIMIT " + (start > 0 ? start+", " : "") + limit; // LIMIT [START,] LIMIT
   }

}
