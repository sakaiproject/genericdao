/**
 * $Id$
 * $URL$
 * FieldMapper.java - genericdao - Apr 18, 2008 2:40:38 PM - azeckoski
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

import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;

/**
 * Allows for mapping persistent object fields to a database fields and back
 * when using the Spring JDBC generic DAO<br/>
 * Extend it with {@link EntityColumnMapper} and {@link StatementMapper}
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface DataMapper {

   public static String DEFAULT_ID_PROPERTY = "id";
   public static String DEFAULT_ID_COLUMN = "ID";

   public static String DBTYPE_DB2 = DatabaseTranslator.DBTYPE_DB2;
   public static String DBTYPE_DERBY = DatabaseTranslator.DBTYPE_DERBY;
   public static String DBTYPE_HSQLDB = DatabaseTranslator.DBTYPE_HSQLDB;
   public static String DBTYPE_MSSQL = DatabaseTranslator.DBTYPE_MSSQL;
   public static String DBTYPE_MYSQL = DatabaseTranslator.DBTYPE_MYSQL;
   public static String DBTYPE_ORACLE = DatabaseTranslator.DBTYPE_ORACLE;
   public static String DBTYPE_POSTGRES = DatabaseTranslator.DBTYPE_POSTGRES;

   /**
    * This string will be replaced by the value from {@link #getTableName()}
    * for the current persistent class type
    */
   public static String DDL_TABLENAME = "{TABLENAME}";
   /**
    * This string plus a persistent class name (Class.getName()) and closed with "}" will be replaced by
    * the name of the table for the persistent class type supplied,
    * Example: {TABLENAME:org.domain.MyClass} would be replaced by the value 
    * returned by table name for the persistent type MyClass
    */
   public static String DDL_TABLENAME_TYPE_PREFIX = "{TABLENAME:";
   /**
    * This string will be replaced by the column name of the unique identifier
    * for the current persistent class type
    */
   public static String DDL_ID_COLUMN = "{ID}";
   /**
    * This string plus a persistent class name (Class.getName()) and closed with "}" will be replaced by
    * the unique identifier for the persistent class type supplied,
    * Example: {ID:org.domain.MyClass} would be replaced by the 
    * column name of the unique identifier for the persistent type MyClass
    */
   public static String DDL_ID_TYPE_PREFIX = "{ID:";
   /**
    * (Oracle ONLY) a sequence name will be generated and inserted
    * based on the tablename for use in generating IDs,
    * if you want to specify your own sequence name then you will lose
    * the ability to have the ID inserted into newly created objects
    */
   public static String DDL_ID_SEQNAME = "{IDSEQNAME}";
   /**
    * This string plus a property name and closed with "}" will be replaced by
    * the column name which maps to that property for the current persistent class type,
    * Example: {COLUMNNAME:title} would be replaced by the column name which maps to the title property
    */
   public static String DDL_COLUMN_PREFIX = "{COLUMNNAME:";

   /**
    * @return the class type of the persistent objects handled by this data mapper
    */
   public Class<?> getPersistentType();

   /**
    * @return the name of the table where these persistent objects are stored
    * OR return null to have generic dao generate the name from the classname
    */
   public String getTableName();

   /**
    * Specify the value which is the unique id for this persistent object,
    * normally the values on this property should be assigned by the database (e.g. autoincrement)
    * 
    * @return the name of the identifier property from the persistent object
    * OR return null to use the default "id"
    */
   public String getIdPropertyName();

   /**
    * Defines the mapping between persistent entity properties and database column names,
    * NOTE: auto generate will surely fail if you did not use the standard UPPERCASE and underscore
    * to create your column names in your DDL (e.g. "myThing" -> "MY_THING")
    * 
    * @return a {@link NamesRecord} 
    * OR return null to have generic dao generate the mapping using reflection
    */
   public NamesRecord getPropertyToColumnNamesMapping();
   
   /**
    * Called when the JDBC generic dao beans are first starting up,
    * if the first line fails then the rest of the file is ignored,
    * this means you should normally include the create table statement first<br/>
    * The first non-comment ('--') line will be run, and if successful, 
    * all other non-comment lines will be run. SQL statements may be on 
    * multiple lines but must have ';' terminators.<br/>
    * The following keys will be replaced automatically:<br/>
    * {TABLENAME} - the value returned by {@link #getTableName()}<br/>
    * {ID} - the column name of the unique identifier<br/>
    * {TABLENAME:org.domain.MyClass} - the value returned by {@link #getTableName()} for the persistent type MyClass<br/>
    * {ID:org.domain.MyClass} - the column name of the unique identifier for the persistent type MyClass<br/>
    * {IDSEQNAME} - (Oracle) a sequence name will be generated and inserted
    * based on the tablename for use in generating IDs,
    * if you want to specify your own sequence name then you will lose
    * the ability to have the ID inserted into newly created objects<br/>
    * 
    * @param databaseTypeConstant one of the database type constants from, e.g. {@link #DBTYPE_MYSQL},
    * this indicates which database your DDL should work for,
    * you can throw an exception here to indicate you do not support the given database type
    * @return the database definition language string which will create the table for this persistent object to be stored in
    */
   public String generateDDL(String databaseTypeConstant);

}
