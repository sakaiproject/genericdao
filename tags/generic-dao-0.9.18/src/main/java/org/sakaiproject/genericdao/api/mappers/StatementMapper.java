/**
 * $Id$
 * $URL$
 * StatementMapper.java - genericdao - Apr 26, 2008 10:09:15 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.mappers;

/**
 * Allows defining of SQL to use when executing standard queries,
 * ? will be replaced by the values to be updated or inserted in standard SQL fashion
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface StatementMapper extends DataMapper {

   /**
    * Will be replaced by the table name
    */
   public static String TABLE_NAME = "{tableName}";
   /**
    * Will be replaced by a list of column names,
    * example: ID, TITLE
    */
   public static String COLUMNS = "{columns}";
   /**
    * Will be replaced by a list of "?" corresponding to the list of columns,
    * example: ?,?
    */
   public static String VALUES = "{values}";
   /**
    * Will be replaced by the where portion of the statement,
    * examples: where id = ?
    */
   public static String WHERE = "{where}";
   public static String UPDATE = "{update}";
   /**
    * Will be replaced by the select,
    * examples: *, count(*), distinct(id)
    */
   public static String SELECT = "{select}";

   public String BASIC_INSERT = "INSERT INTO "+TABLE_NAME+" ("+COLUMNS+") VALUES ("+VALUES+") ";
   public String BASIC_SELECT = "SELECT "+SELECT+" FROM "+TABLE_NAME+" "+WHERE;
   public String BASIC_UPDATE = "UPDATE "+TABLE_NAME+" SET "+UPDATE+" "+WHERE;
   public String BASIC_DELETE = "DELETE FROM "+TABLE_NAME+" "+WHERE;

   /**
    * @return the template to use when generating insert statements,
    * use the {@link #TABLE_NAME}, {@link #COLUMNS} and {@link #VALUES} constants to indicate replacements
    * OR return null to use the default templates
    */
   public String getInsertTemplate();

   /**
    * @return the template to use when generating selects,
    * use the {@link #SELECT}, {@link #TABLE_NAME}, and {@link #WHERE} constants to indicate replacements
    * OR return null to use the default templates
    */
   public String getSelectTemplate();

   /**
    * @return the template to use when generating updates,
    * use the {@link #TABLE_NAME}, {@link #UPDATE} and {@link #WHERE} constants to indicate replacements
    * OR return null to use the default templates
    */
   public String getUpdateTemplate();

   /**
    * @return the template to use when generating deletes,
    * use the {@link #TABLE_NAME} and {@link #WHERE} constants to indicate replacements
    * OR return null to use the default templates
    */
   public String getDeleteTemplate();

}
