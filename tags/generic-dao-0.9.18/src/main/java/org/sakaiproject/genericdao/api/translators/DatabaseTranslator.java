/**
 * $Id$
 * $URL$
 * DatabaseTranslator.java - genericdao - Apr 26, 2008 9:45:33 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.translators;

/**
 * Defines a database translator method which translates DDL and SQL between databases
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface DatabaseTranslator {

    public static String DBTYPE_ORACLE = "ORACLE";
    public static String DBTYPE_MYSQL = "MYSQL";
    public static String DBTYPE_HSQLDB = "HSQLDB";
    public static String DBTYPE_DB2 = "DB2";
    public static String DBTYPE_MSSQL = "MSSQL";
    public static String DBTYPE_POSTGRES = "POSTGRES";
    public static String DBTYPE_DERBY = "DERBY";

    /**
     * Defines the valid database types
     */
    public static String[] DBTYPES = {
        DBTYPE_HSQLDB,
        DBTYPE_MYSQL,
        DBTYPE_ORACLE,
        DBTYPE_DERBY,
        DBTYPE_POSTGRES,
        DBTYPE_DB2,
        DBTYPE_MSSQL
    };

    /**
     * @return the database constant for the DB handled by this translator
     */
    public String handlesDB();

    /**
     * @param tableName the name of the table which the row was inserted into
     * @param idColumnName the name of the id column
     * @return the query to get the auto generated id of the last insert
     */
    public String makeAutoIdQuery(String tableName, String idColumnName);

    /**
     * @param sql any SELECT query
     * @param start the row to start on
     * @param limit the maximum number of rows to return
     * @param tableName the name of the table which the query was executed on
     * @return the SELECT based on the original with limits applied
     */
    public String makeLimitQuery(String sql, long start, long limit, String tableName);

}
