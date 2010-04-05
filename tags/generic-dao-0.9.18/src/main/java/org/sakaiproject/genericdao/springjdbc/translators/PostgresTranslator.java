/**
 * $Id$
 * $URL$
 * PostgresTranslator.java - genericdao - Apr 26, 2008 2:54:12 PM - azeckoski
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
 * Postgres DB translator
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class PostgresTranslator extends BasicTranslator {

   public String handlesDB() {
      return DBTYPE_POSTGRES;
   }

   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "SELECT currval('"+tableName+"_"+idColumnName+"_seq')";
   }

   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      return sql + " limit " + limit + (start > 0 ? " offset "+start+", " : ""); // " limit ? offset ?";
   }

}
