/**
 * $Id$
 * $URL$
 * OracleTranslator.java - genericdao - Apr 26, 2008 1:58:22 PM - azeckoski
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

import org.sakaiproject.genericdao.api.mappers.DataMapper;

/**
 * DB translator for Oracle
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class OracleTranslator extends BasicTranslator {

   public final static String ORACLE_SEQ_PREFIX = "seq_ID_";

   public static String getOracleSeqName(String tableName) {
      return chopString(ORACLE_SEQ_PREFIX + tableName, 30);
   }

   public String handlesDB() {
      return DBTYPE_ORACLE;
   }

   public String makeAutoIdQuery(String tableName, String idColumnName) {
      return "SELECT "+DataMapper.DDL_ID_SEQNAME+".CURRVAL from dual";
   }

   public String makeLimitQuery(String sql, long start, long limit, String tableName) {
      StringBuilder pagingSelect = new StringBuilder();
      if (start > 0) {
         pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
      }
      else {
         pagingSelect.append("select * from ( ");
      }
      pagingSelect.append(sql);
      if (start > 0) {
         pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
      }
      else {
         pagingSelect.append(" ) where rownum <= ?");
      }
      return pagingSelect.toString();
   }

}
