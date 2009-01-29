/**
 * $Id$
 * $URL$
 * SimpleDataMapper.java - genericdao - Apr 25, 2008 4:29:45 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springjdbc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.azeckoski.reflectutils.ClassLoaderUtils;
import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.BasicTranslator;


/**
 * This class allows us to generate a {@link DataMapper} using Spring or anything else that
 * can set/inject strings to create an object, most of the functions are assumed to
 * be handled automatically by generic DAO
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class SimpleDataMapper implements DataMapper {

    /**
     * Default constructor which is used by Spring mostly,
     * you will need to at least set the 
     */
    public SimpleDataMapper() {}

    /**
     * This is primarily for use when using all gendao conventions and annotations with a class,
     * otherwise this will not include enough information to complete the mapping,
     * the tablename will be the classname if no annotation is used,
     * the id column must be ID if no annotation is used and the property must also be id
     * 
     * @param persistentType any class type to map as a persistent type
     */
    public SimpleDataMapper(Class<?> persistentType) {
        this.persistentType = persistentType;
    }

    /**
     * This is useful when adhering to some of the gendao conventions
     * but needing to specify your own id property and tableName
     * 
     * @param persistentType any class type to map as a persistent type
     * @param idPropertyName this is the property name matching the identifier for the table
     * @param tableName this is the name of the table that matches this persistent class
     */
    public SimpleDataMapper(Class<?> persistentType, String idPropertyName, String tableName) {
        this.idPropertyName = idPropertyName;
        this.persistentType = persistentType;
        this.tableName = tableName;
    }

    private boolean usePropertyNamesForColumns = false;
    public boolean isUsePropertyNamesForColumns() {
        return usePropertyNamesForColumns;
    }
    /**
     * (OPTIONAL)
     * This will cause the mapper to use the property names as is (case and characters)
     * for the columns names instead of transforming them (e.g. property: myThing => column: myThing),
     * default is false (that means property: myThing => column: MY_THING)
     */
    public void setUsePropertyNamesForColumns(boolean usePropertyNamesForColumns) {
        this.usePropertyNamesForColumns = usePropertyNamesForColumns;
    }

    protected String idPropertyName = null;
    /**
     * (OPTIONAL)
     * This is the name of the property on the persistent object which defines the unique identifier,
     * defaults to null if unset but that will be replaced by the proper id or the default of "id"
     */
    public void setIdPropertyName(String idPropertyName) {
        this.idPropertyName = idPropertyName;
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.DataMapper#getIdPropertyName()
     */
    public String getIdPropertyName() {
        return idPropertyName;
    }

    private Class<?> persistentType;
    /**
     * Allows setting the persistent type,
     * this should normally be done in the constructor and cannot be changed
     * once it has been set
     * @param persistentType this is the class that maps to the DB table
     * @throws IllegalArgumentException if the input is null or the value is already set
     */
    public void setPersistentType(Class<?> persistentType) {
        if (persistentType == null) {
            throw new IllegalArgumentException("The persistentType cannot be null");
        }
        if (this.persistentType != null) {
            throw new IllegalArgumentException("The persistentType has already been set and cannot be reset");
        }
        this.persistentType = persistentType;
    }
    /**
     * (REQUIRED)
     * set this to the class name (e.g. org.project.MyClass) 
     * and it will be converted into the class object
     * @param persistentClassname this is the fully qualified classname of the persistent type
     */
    public void setPersistentClassname(String persistentClassname) {
        persistentType = ClassLoaderUtils.getClassFromString(persistentClassname);
        if (persistentType == null) {
            throw new IllegalArgumentException(
                    "Invalid class name for persistentClassname, could not create class from string: " + persistentClassname);
        }
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.DataMapper#getPersistentType()
     */
    public Class<?> getPersistentType() {
        return persistentType;
    }

    protected String tableName;
    /**
     * (OPTIONAL)
     * set this to the name of the table,<br/>
     * defaults to a name built from the simple class name of the persistent class
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.DataMapper#getTableName()
     */
    public String getTableName() {
        if (tableName == null) {
            if (persistentType != null) {
                tableName = BasicTranslator.makeTableNameFromClass(persistentType);
            } else {
                throw new IllegalStateException("tablename and persistentType are both null, invalid DataMapper");
            }
        }
        return tableName;
    }

    private NamesRecord namesRecord = null;
    /**
     * (OPTIONAL)
     * Set this to a map of the persistent object properties to database column names,
     * String -> String,<br/>
     * example: "id" -> "ID", "title" -> "ITEM_TITLE"<br/>
     * defaults to autogenerated column names which are uppercased and underscored (as in example above)
     * @see SimpleDataMapper#setNamesUsed(String[])
     * @see SimpleDataMapper#setNamesRecord(NamesRecord)
     */
    public void setNamesMapping(Map<String, String> namesMapping) {
        if (namesMapping != null && ! namesMapping.isEmpty()) {
            namesRecord = new NamesRecord();
            for (Entry<String, String> entry : namesMapping.entrySet()) {
                String key = entry.getKey();
                String dbName = entry.getValue();
                if (dbName != null && ! "".equals(dbName)) {
                    namesRecord.setNameMapping(key, dbName);
                }
            }
        }
    }
    /**
     * (OPTIONAL)
     * Set this to an array of the persistent object properties which will be used for this mapping,
     * anything not listed will be ignored when forming queries,
     * this is the least work but depends on the more conventions and allows the least control <br/>
     * NOTE: Be sure to set {@link #setUsePropertyNamesForColumns(boolean)} before calling this method <br/>
     * Example: ["id", "title"],<br/>
     * "id" -> "ID", "title" -> "ITEM_TITLE"<br/>
     * defaults to autogenerated column names which are uppercased and underscored (as in example above)
     * depending on the setting for {@link #isUsePropertyNamesForColumns()}
     * @see SimpleDataMapper#setNamesMapping(Map)
     * @see SimpleDataMapper#setNamesRecord(NamesRecord)
     */
    public void setNamesUsed(String[] namesUsed) {
        if (namesUsed == null) {
            throw new IllegalArgumentException("NamesRecord cannot be null");
        }
        namesRecord = new NamesRecord();
        for (int i = 0; i < namesUsed.length; i++) {
            String propertyName = namesUsed[i];
            String columnName = propertyName;
            if (! isUsePropertyNamesForColumns()) {
                columnName = BasicTranslator.makeDBNameFromCamelCase(propertyName);
            }
            namesRecord.setNameMapping(propertyName, columnName);
        }
    }
    /**
     * (OPTIONAL)
     * Set the {@link NamesRecord} used for this mapping directly,
     * this controls the mapping of object properties to database columns,
     * this is more work than is probably needed but allows the most control, see the other methods
     * @see SimpleDataMapper#setNamesMapping(Map)
     * @see SimpleDataMapper#setNamesUsed(String[])
     */
    public void setNamesRecord(NamesRecord namesRecord) {
        if (namesRecord == null) {
            throw new IllegalArgumentException("NamesRecord cannot be null");
        }
        this.namesRecord = namesRecord;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.mappers.DataMapper#definePropertyToColumnMapping()
     */
    public NamesRecord getPropertyToColumnNamesMapping() {
        return namesRecord;
    }

    private Map<String, String> dbTypeToDDL = new HashMap<String, String>();
    public Map<String, String> getDbTypeToDDL() {
        return dbTypeToDDL;
    }

    /**
     * Convenience method which allows setting the DDL for a database type,
     * this will be appended to existing entries<br/>
     * Generally only useful if you only need to set a single DDL<br/>
     * 
     * @param databaseTypeConstant one of the database type constants from, e.g. {@link #DBTYPE_MYSQL},
     * this indicates which database your DDL should work for
     * @param ddl the ddl to execute to create the table(s) for this persistent object
     */
    public void addDBTypeAndDDL(String databaseTypeConstant, String ddl) {
        if (databaseTypeConstant == null || ddl == null) {
            throw new IllegalArgumentException("databaseTypeConstant and ddl cannot be null");
        }
        if (dbTypeToDDL == null) {
            dbTypeToDDL = new HashMap<String, String>();
        }
        dbTypeToDDL.put(databaseTypeConstant, ddl);
    }

    private Map<String, String> dbTypeToFilename = new HashMap<String, String>();
    public Map<String, String> getDbTypeToFilename() {
        return dbTypeToFilename;
    }

    /**
     * (REQUIRED/OPTIONAL)
     * Sets the map of databaseTypeConstant -> file containing the DDL script,
     * the DDL will be looked up and stored in the class and executed on service init<br/>
     * This must be set if the table does not already exist but if it does then this is optional<br/>
     * The first non-comment ('--') line will be run, and if successful, 
     * all other non-comment lines will be run. SQL statements may be on 
     * multiple lines but must have ';' terminators.<br/>
     * Can use the {@link #makeDDLTypeMap(String, String[])} method to make this more easily <br/>
     * The following keys will be replaced automatically:<br/>
     * {TABLENAME} - the value returned by {@link #getTableName()}<br/>
     * {ID} - the column name of the unique identifier<br/>
     * {IDSEQNAME} - (Oracle) a sequence name will be generated and inserted
     * based on the tablename for use in generating IDs,
     * if you want to specify your own sequence name then you will lose
     * the ability to have the ID inserted into newly created objects<br/>
     * <b>NOTE:</b> the file must be in your jar/war/package so it can
     * be located in the classloader,
     * use something like this in your maven pom.xml resources tag:
     * <xmp><resource>
        <directory>${basedir}/src/sql</directory>
        <includes>
          <include>** /*.sql</include>
        </includes>
      </resource></xmp>
     * Remove the space between ** and /* from the above sample<br/>
     * For example: src/sql/mysql.sql, src/sql/mysql/myproject.ddl<br/>
     * @see #setDBTypeToDDL(Map)
     * @see #makeDDLTypeMap(String, String[])
     */
    public void setDBTypeToFile(Map<String, String> dbTypeToFile) {
        if (dbTypeToFilename == null) {
            dbTypeToFilename = new HashMap<String, String>();
        }
        if (dbTypeToFile != null) {
            for (Entry<String, String> entry : dbTypeToFile.entrySet()) {
                String dbtype = entry.getKey().toUpperCase();
                String value = entry.getValue();
                if (value != null) {
                    this.dbTypeToFilename.put(dbtype, value);
                }
            }
        }
    }

    /**
     * (REQUIRED/OPTIONAL)
     * Sets the map of databaseTypeConstant -> DDL,
     * these will be appended to any existing values<br/>
     * This must be set if the table does not already exist but if it does then this is optional<br/>
     * The first non-comment ('--') line will be run, and if successful, 
     * all other non-comment lines will be run. SQL statements may be on 
     * multiple lines but must have ';' terminators.<br/>
     * The following keys will be replaced automatically:<br/>
     * {TABLENAME} - the value returned by {@link #getTableName()}<br/>
     * {ID} - the column name of the unique identifier<br/>
     * {IDSEQNAME} - (Oracle) a sequence name will be generated and inserted
     * based on the tablename for use in generating IDs,
     * if you want to specify your own sequence name then you will lose
     * the ability to have the ID inserted into newly created objects<br/>
     * @see #setDBTypeToFile(Map)
     */
    public void setDBTypeToDDL(Map<String, String> dbTypeToDDL) {
        if (dbTypeToDDL == null) {
            dbTypeToDDL = new HashMap<String, String>();
        }
        if (dbTypeToDDL != null) {
            for (Entry<String, String> entry : dbTypeToDDL.entrySet()) {
                String dbtype = entry.getKey().toUpperCase();
                String value = entry.getValue();
                if (value != null) {
                    this.dbTypeToDDL.put(dbtype, value);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.DataMapper#generateDDL(java.lang.String)
     */
    public String generateDDL(String databaseTypeConstant) {
        String ddl = null;
        if (dbTypeToDDL != null) {
            if (dbTypeToDDL.containsKey(databaseTypeConstant)) {
                ddl = dbTypeToDDL.get(databaseTypeConstant);
            }
        }
        return ddl;
        //throw new UnsupportedOperationException("No support for the database type: " + databaseTypeConstant);
    }

    /**
     * This is a convenience method which assists in creating the ddl type map,
     * it basically allows you to easily create the map if you have followed the convention
     * of placing all your ddl files in folders which are equivalent to the name
     * of the database type they are created for and named them all identically with
     * a name which is similar to the 
     * @param fileName the name of the file (e.g. users.sql)
     * @param types the types to include in the map, use the constants from {@link DatabaseTranslator} (e.g. "derby")
     * @return the map which can be given to the {@link #setDBTypeToFile(Map)} method
     */
    public static Map<String, String> makeDDLTypeMap(String fileName, String[] types) {
        if (fileName == null || "".equals(fileName)) {
            throw new IllegalArgumentException("fileName must not be null");
        }
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("Must include at least one type in the types array");
        }
        HashMap<String, String> dbTypeToFile = new HashMap<String, String>();
        for (String type : types) {
            dbTypeToFile.put(type, type.toLowerCase() + File.separator + fileName);
        }
        return dbTypeToFile;
    }

    /**
     * Convenience method which will take a filename and an array of DBTYPE String constants
     * (from {@link DatabaseTranslator}, example {@link DatabaseTranslator#DBTYPE_MYSQL})
     * and produce a map of standard DDL types to file paths <br/>
     * Will create a map like so: dbType -> [prefixPath/]dbType/fileName
     * 
     * @param fileName the name of the sql file (e.g. myTableDDL.sql)
     * @param dbTypes an array with 1 or more dbTypes which you have DDL files for
     * @param prefixPath (optional) the prefix to append before the dbType and fileName (should not have a leading "/"),
     * can be null or "" if npt used
     * @return the map of dbType to file paths
     * @throws IllegalArgumentException if the filename or dbTYpes are null or empty
     */
    public static Map<String, String> makeDDLMap(String fileName, String[] dbTypes, String prefixPath) {
        if (fileName == null || "".equals(fileName)) {
            throw new IllegalArgumentException("filename must be set and cannot be null");
        }
        if (dbTypes == null || dbTypes.length == 0) {
            throw new IllegalArgumentException("at least one dbType must be set and included, cannot be null or empty");
        }
        HashMap<String, String> dbTypeToFile = new HashMap<String, String>();
        if (prefixPath == null) {
            prefixPath = "";
        } else {
            if (! prefixPath.endsWith(File.separator)) {
                prefixPath += File.separator;
            }
        }
        for (int i = 0; i < dbTypes.length; i++) {
            String type = dbTypes[i];
            dbTypeToFile.put(type, prefixPath + type.toLowerCase() + File.separator + fileName);
        }
        return dbTypeToFile;
    }

}
