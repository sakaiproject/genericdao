/**
 * $Id$
 * $URL$
 * DB2Translator.java - genericdao - Apr 26, 2008 2:29:28 PM - azeckoski
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
 * IBM DB2 database translator
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class DB2Translator extends BasicTranslator {

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#handlesDB()
    */
   public String handlesDB() {
      return DBTYPE_DB2;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#makeAutoIdQuery(java.lang.String)
    */
   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "values identity_val_local()";
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.genericdao.api.translators.DatabaseTranslator#makeLimitQuery(java.lang.String, long, long)
    */
   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      int startOfSelect = sql.toLowerCase().indexOf("select");

      StringBuilder pagingSelect = new StringBuilder( sql.length()+100 )
               .append( sql.substring(0, startOfSelect) ) //add the comment
               .append("select * from ( select ") //nest the main query in an outer select
               .append( getRowNumber(sql) ); //add the rownnumber bit into the outer query select list

      if ( hasDistinct(sql) ) {
         pagingSelect.append(" row_.* from ( ") //add another (inner) nested select
            .append( sql.substring(startOfSelect) ) //add the main query
            .append(" ) as row_"); //close off the inner nested select
      } else {
         pagingSelect.append( sql.substring( startOfSelect + 6 ) ); //add the main query
      }

      pagingSelect.append(" ) as temp_ where rownumber_ ");

      //add the restriction to the outer select
      if (start > 0) {
         pagingSelect.append("between ?+1 and ?");
      } else {
         pagingSelect.append("<= ?");
      }
      return pagingSelect.toString();
   }

   /**
    * true if this query is for a distinct result
    */
   private static boolean hasDistinct(String sql) {
      return sql.toLowerCase().indexOf("select distinct")>=0;
   }

   /**
    * Render the <tt>rownumber() over ( .... ) as rownumber_,</tt>
    * bit, that goes in the select list
    */
   private String getRowNumber(String sql) {
      StringBuilder rownumber = new StringBuilder(50).append("rownumber() over(");

      int orderByIndex = sql.toLowerCase().indexOf("order by");

      if ( orderByIndex>0 && !hasDistinct(sql) ) {
         rownumber.append( sql.substring(orderByIndex) );
      }

      rownumber.append(") as rownumber_,");

      return rownumber.toString();
   }

}
