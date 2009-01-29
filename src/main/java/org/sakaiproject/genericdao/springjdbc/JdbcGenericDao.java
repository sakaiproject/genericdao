/**
 * $Id$
 * $URL$
 * JdbcGenericDao.java - genericdao - Apr 18, 2008 10:07:08 AM - azeckoski
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ClassFields;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.sakaiproject.genericdao.api.GenericDao;
import org.sakaiproject.genericdao.api.annotations.PersistentColumnMappingPolicy;
import org.sakaiproject.genericdao.api.annotations.PersistentColumnName;
import org.sakaiproject.genericdao.api.annotations.PersistentId;
import org.sakaiproject.genericdao.api.annotations.PersistentTransient;
import org.sakaiproject.genericdao.api.annotations.enums.MappingPolicy;
import org.sakaiproject.genericdao.api.caching.CacheProvider;
import org.sakaiproject.genericdao.api.interceptors.DaoOperationInterceptor;
import org.sakaiproject.genericdao.api.interceptors.ReadInterceptor;
import org.sakaiproject.genericdao.api.interceptors.WriteInterceptor;
import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.EntityColumnMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.mappers.StatementMapper;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;
import org.sakaiproject.genericdao.base.caching.NonCachingCacheProvider;
import org.sakaiproject.genericdao.springjdbc.translators.BasicTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.DB2Translator;
import org.sakaiproject.genericdao.springjdbc.translators.DerbyTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.HSQLDBTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.MSSQLTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.MySQLTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.OracleTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.PostgresTranslator;
import org.sakaiproject.genericdao.springutil.SmartDataSourceWrapper;
import org.sakaiproject.genericdao.util.JDBCUtils;
import org.sakaiproject.genericdao.util.ThreadboundConnectionsDataSourceWrapper;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SmartDataSource;

/**
 * A Spring JDBC (http://hibernate.org/) based implementation of GenericDao
 * which can be extended to add more specialized DAO methods.
 * <p>
 * Note: This implementation is so simple it is unlikely to be useful
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class JdbcGenericDao extends JdbcDaoSupport implements GenericDao {

    private String databaseType = DataMapper.DBTYPE_HSQLDB;
    /**
     * This should be set to the type of database being used
     * and determines which DDL to run when creating the tables,
     * defaults to {@link DataMapper#DBTYPE_HSQLDB} "HSQLDB",
     * will fixup case as needed
     */
    public void setDatabaseType(String databaseType) {
        if (databaseType == null || databaseType.length() == 0) {
            throw new IllegalArgumentException("databaseType cannot be null or empty");
        }
        if (DatabaseTranslator.DBTYPE_DB2.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_DB2;
        } else if (DatabaseTranslator.DBTYPE_DERBY.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_DERBY;
        } else if (DatabaseTranslator.DBTYPE_HSQLDB.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_HSQLDB;
        } else if (DatabaseTranslator.DBTYPE_MSSQL.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_MSSQL;
        } else if (DatabaseTranslator.DBTYPE_MYSQL.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_MYSQL;
        } else if (DatabaseTranslator.DBTYPE_ORACLE.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_ORACLE;
        } else if (DatabaseTranslator.DBTYPE_POSTGRES.equalsIgnoreCase(databaseType)) {
            this.databaseType = DatabaseTranslator.DBTYPE_POSTGRES;
        } else {
            this.databaseType = databaseType.toUpperCase();
        }
    }
    /**
     * @return a constant indicating the type of database: DBTYPE_*
     */
    protected String getDatabaseType() {
        return databaseType;
    }

    /**
     * @return the jdbcTemplate that is currently in use,
     * allows for overriding and support DAO-4 (http://jira.sakaiproject.org/jira/browse/DAO-4)
     */
    public JdbcTemplate getSpringJdbcTemplate() {
        return super.getJdbcTemplate();
    }

    protected boolean showSQL = false;
    /**
     * Enable SQL debugging which will show all SQL statements and DDL statements being executed by printing them out
     * 
     * @param showSQL if true then all SQL and DDL statements will be printed
     */
    public void setShowSQL(boolean showSQL) {
        this.showSQL = showSQL;
    }
    /**
     * @return true if show SQL is enabled, not a good idea for production
     */
    public boolean isShowSQL() {
        return showSQL;
    }

    private boolean autoDDL = true;
    /**
     * @return true if automatic DDL execution is enabled
     */
    public boolean isAutoDDL() {
        return autoDDL;
    }
    /**
     * Set to true to cause defined DDL to be executed
     * 
     * @param autoDDL set to true to cause the DDL to be executed,
     * if false then no DDL will be executed
     */
    public void setAutoDDL(boolean autoDDL) {
        this.autoDDL = autoDDL;
    }

    private boolean autoCommitDDL = false;
    /**
     * @return true if DDL automatic commits are enabled
     */
    public boolean isAutoCommitDDL() {
        return autoCommitDDL;
    }
    /**
     * Allows control over whether the DDL should be committed after each statement as it is read in,
     * this should probably be disabled if you are using a transaction manager and should be enabled if
     * you are managing your own transactions
     * 
     * @param autoCommitDDL true to enable auto commits on DDL excecution
     */
    public void setAutoCommitDDL(boolean autoCommitDDL) {
        this.autoCommitDDL = autoCommitDDL;
    }

    private boolean autoCommitOperations = false;
    /**
     * @return true if automatic commit is enabled for all generic DAO write operations
     */
    public boolean isAutoCommitOperations() {
        return autoCommitOperations;
    }
    /**
     * Allows control over whether the DAO should do automatic commits for all generic DAO write operations
     * 
     * @param autoCommitOperations true to enable automatic commits for all generic dao operations,
     * this should be false if you are using a transaction manager
     */
    public void setAutoCommitOperations(boolean autoCommitOperations) {
        this.autoCommitOperations = autoCommitOperations;
    }

    /**
     * This is a special case DataSource setter method which will wrap the DataSource,
     * you must use this in the case where the {@link DataSource} you are using is not
     * a Spring controlled on {@link SmartDataSource}, this will wrap the spring
     * DataSource automatically so that the auto-commit and manual transactions will work,
     * without this wrapper your connections will be closed after each jdbcTemplate method runs
     * and nothing will ever be committed (unless the connection is set to auto-commit) <br/>
     * WARNING: if you use this then you are responsible for committing TXs and closing your connections 
     * using the {@link #closeConnection()} and {@link #commitTransaction()} or {@link #rollbackTransaction()}
     * methods <br/>
     * Is not thread-bound
     * @see #setNonSpringDataSource(DataSource, boolean)
     * 
     * @param dataSource any non-spring {@link DataSource}
     */
    public void setNonSpringDataSource(DataSource dataSource) {
        setNonSpringDataSource(dataSource, false);
    }

    /**
     * This is a special case DataSource setter method which will wrap the DataSource,
     * you must use this in the case where the {@link DataSource} you are using is not
     * a Spring controlled on {@link SmartDataSource}, this will wrap the spring
     * DataSource automatically so that the auto-commit and manual transactions will work,
     * without this wrapper your connections will be closed after each jdbcTemplate method runs
     * and nothing will ever be committed (unless the connection is set to auto-commit) <br/>
     * WARNING: if you use this then you are responsible for committing TXs and closing your connections 
     * using the {@link #closeConnection()} and {@link #commitTransaction()} or {@link #rollbackTransaction()}
     * methods
     * 
     * @param dataSource any non-spring {@link DataSource}
     * @param threadBound if true then the connections will be thread-bound and only one returned
     * for the current thread no matter how many times getConnection is called, 
     * if false then they will return a new connection for each call
     */
    public void setNonSpringDataSource(DataSource dataSource, boolean threadBound) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource cannot be null");
        }
        if (dataSource instanceof SmartDataSource) {
            setDataSource(dataSource);
        } else {
            // wrap it up
            DataSource wrapper = dataSource;
            if (threadBound) {
                wrapper = new ThreadboundConnectionsDataSourceWrapper(wrapper);
            }
            wrapper = new SmartDataSourceWrapper(wrapper);
            setDataSource( wrapper );
        }
    }


    /**
     * preserve the order the classes are read in, 
     * presumably this is also the dependency order 
     */
    private Map<Class<?>, DataMapper> dataMappers;
    /**
     * This tells the DAO about your persistent classes and their associated tables,
     * it is the major configuration path for the DAO
     * @param dataMappers a list of all {@link DataMapper}s that this DAO uses,
     * ideally this is no more than a few per DAO but it should include all tables which are directly linked
     */
    public void setDataMappers(List<DataMapper> dataMappers) {
        if (this.dataMappers == null) {
            this.dataMappers = new ConcurrentHashMap<Class<?>, DataMapper>();
            this.classes = new Vector<Class<?>>();
        }
        for (DataMapper dataMapper : dataMappers) {
            Class<?> type = dataMapper.getPersistentType();
            this.dataMappers.put(type, dataMapper);
            this.classes.add(type);
            getNamesRecord(type); // prime the names record
        }
    }

    /**
     * Get a data mapper for a specific class,
     * will always return a data mapper
     */
    protected DataMapper getDataMapper(Class<?> type) {
        DataMapper dm = dataMappers.get(type);
        if (dm == null && classes.contains(type)) {
            // make a Simple DM, this assumes the necessary tables already exist
            dm = new SimpleDataMapper(type);
            dataMappers.put(type, dm); // place it in the map
        }
        if (dm == null) {
            throw new IllegalArgumentException("type is not a persistent class type: " + type);
        }
        return dm;
    }

    // names mapping cache
    private Map<Class<?>, NamesRecord> namesRecordsCache = new ConcurrentHashMap<Class<?>, NamesRecord>();
    /**
     * Get a names mapping for a specific class type,
     * uses caching, will always return a mapping
     */
    public NamesRecord getNamesRecord(Class<?> type) {
        NamesRecord nr = namesRecordsCache.get(type);
        if (nr == null) {
            // get a names record from the data mapper
            DataMapper dm = getDataMapper(type);
            nr = dm.getPropertyToColumnNamesMapping();
            if (nr != null) {
                nr.setIdentifierProperty( findIdProperty(type) );
                if (nr.getForeignKeyPropertyNames().isEmpty()) {
                    // check for foreign keys and add them (only complete fields though)
                    Map<String, Class<?>> types = ReflectUtils.getInstance().getFieldTypes(type, FieldsFilter.COMPLETE);
                    for (Entry<String, Class<?>> entry : types.entrySet()) {
                        // special handling for foreign keys identified by persistent types inside this object
                        String property = entry.getKey();
                        Class<?> pType = entry.getValue();
                        if (getPersistentClasses().contains(pType)) {
                            // this is another persistent object so this must be a foreign key
                            String pId = findIdProperty(pType);
                            String fkProp = property + "." + pId;
                            String column = nr.getColumnForProperty(property);
                            if (column != null) {
                                nr.setForeignKeyMapping(fkProp, column);
                            }
                        }
                    }
                }
                namesRecordsCache.put(type, nr);
            }
        }
        if (nr == null) {
            boolean usePropertyNamesForColumns = false;
            DataMapper dm = getDataMapper(type);

            ReflectUtils reflectUtils = ReflectUtils.getInstance();
            // try to get the mapping from the class using annotations
            ClassFields<?> classFields = reflectUtils.analyzeClass(type);
            if (classFields.getClassAnnotations().contains(PersistentColumnMappingPolicy.class)) {
                for (Annotation classAnnote : classFields.getClassAnnotations()) {
                    if (PersistentColumnMappingPolicy.class.equals(classAnnote.annotationType())) {
                        MappingPolicy mp = ((PersistentColumnMappingPolicy)classAnnote).policy();
                        if (MappingPolicy.FIELD_NAMES.equals(mp)) {
                            usePropertyNamesForColumns = true;
                        } else if (MappingPolicy.UPPER_UNDERSCORES.equals(mp)) {
                            usePropertyNamesForColumns = false;
                        }
                    }
                    if (dm instanceof SimpleDataMapper) {
                        // override the setting
                        ((SimpleDataMapper)dm).setUsePropertyNamesForColumns(usePropertyNamesForColumns);
                    }
                }
            } else {
                // no annotation so get the setting from the data mapper
                if (dm instanceof SimpleDataMapper) {
                    usePropertyNamesForColumns = ((SimpleDataMapper)dm).isUsePropertyNamesForColumns();
                }
            }

            // create a names mapping using reflection
            nr = new NamesRecord();
            Map<String, Class<?>> types = reflectUtils.getFieldTypes(type, FieldsFilter.COMPLETE);
            for (String property : types.keySet()) {
                String column = property;
                // check for transient annotation
                try {
                    Annotation annotation = classFields.getFieldAnnotation(PersistentTransient.class, property);
                    if (annotation != null) {
                        // skip this one
                        continue;
                    }
                } catch (FieldnameNotFoundException e) {
                    // nothing to do
                }
                if (! usePropertyNamesForColumns) {
                    column = BasicTranslator.makeDBNameFromCamelCase(property);
                }
                // check for annotation override to column name
                try {
                    PersistentColumnName annotation = (PersistentColumnName) classFields.getFieldAnnotation(PersistentColumnName.class, property);
                    if (annotation != null) {
                        column = annotation.value();
                    }
                } catch (FieldnameNotFoundException e) {
                    // nothing to do
                }
                nr.setNameMapping(property, column);
                // special handling for foreign keys identified by persistent types inside this object
                Class<?> pType = types.get(property);
                if (getPersistentClasses().contains(pType)) {
                    // this is another persistent object so this must be a foreign key
                    String pId = findIdProperty(pType);
                    String fkProp = property + "." + pId;
                    nr.setForeignKeyMapping(fkProp, column);
                }
            }
            // add in the special id marker and make sure the id is set right
            nr.setIdentifierProperty( findIdProperty(type) );
            namesRecordsCache.put(type, nr);
            if (dm instanceof SimpleDataMapper) {
                // put this NamesRecord back into the DataMapper
                ((SimpleDataMapper)dm).setNamesRecord(nr);
            }
        }
        return nr;
    }

    private DatabaseTranslator databaseTranslator = null;
    /**
     * Force the current database translator to be this one
     */
    public void setDatabaseTranslator(DatabaseTranslator databaseTranslator) {
        if (databaseTranslator != null) {
            this.databaseTranslator = databaseTranslator;
        }
    }
    protected DatabaseTranslator getDatabaseTranslator() {
        if (this.databaseTranslator == null) {
            databaseTranslator = new HSQLDBTranslator();
        }
        return this.databaseTranslator;
    }

    /**
     * Initialize the database translator
     */
    protected void initDatabaseTranslator() {
        String type = getDatabaseType();
        if (this.databaseTranslator == null) {
            if (DatabaseTranslator.DBTYPE_DB2.equalsIgnoreCase(type)) {
                this.databaseTranslator = new DB2Translator();
            } else if (DatabaseTranslator.DBTYPE_DERBY.equalsIgnoreCase(type)) {
                this.databaseTranslator = new DerbyTranslator();
            } else if (DatabaseTranslator.DBTYPE_HSQLDB.equalsIgnoreCase(type)) {
                this.databaseTranslator = new HSQLDBTranslator();
            } else if (DatabaseTranslator.DBTYPE_MSSQL.equalsIgnoreCase(type)) {
                this.databaseTranslator = new MSSQLTranslator();
            } else if (DatabaseTranslator.DBTYPE_MYSQL.equalsIgnoreCase(type)) {
                this.databaseTranslator = new MySQLTranslator();
            } else if (DatabaseTranslator.DBTYPE_ORACLE.equalsIgnoreCase(type)) {
                this.databaseTranslator = new OracleTranslator();
            } else if (DatabaseTranslator.DBTYPE_POSTGRES.equalsIgnoreCase(type)) {
                this.databaseTranslator = new PostgresTranslator();
            } else {
                throw new UnsupportedOperationException("No translator for this database type: " + type);
            }
        }
    }

    /**
     * Initialize the persistent classes
     */
    protected void initPersistentClasses() {
        // init the list of classes and mappers related to them and execute DDL if needed
        for (Class<?> type : getPersistentClasses()) {
            DataMapper dm = getDataMapper(type);
            if (autoDDL) {
                InputStream ddlIS = null;
                // try to get DDL from the mapper first
                String ddl = dm.generateDDL(getDatabaseType());
                if ( ddl == null || "".equals(ddl) ) {
                    if (dm instanceof SimpleDataMapper) {
                        // try loading from a file if set
                        SimpleDataMapper sdm = (SimpleDataMapper) dm;
                        String filepath = sdm.getDbTypeToFilename().get(getDatabaseType());
                        if (filepath != null) {
                            // cleanup filename
                            if (filepath.startsWith("/")) {
                                filepath = filepath.substring(1);
                            }
                            // try looking in the classloader of the thread first
                            ddlIS = getInputStreamFromClassloader(Thread.currentThread().getContextClassLoader(), filepath);
                            if (ddlIS == null) {
                                // next try the classloader for this DAO
                                ddlIS = getInputStreamFromClassloader(this.getClass().getClassLoader(), filepath);
                            }
                            if (ddlIS == null) {
                                // next try the classloader for persistent type
                                ddlIS = getInputStreamFromClassloader(type.getClassLoader(), filepath);
                            }
                            if (ddlIS == null) {
                                // we got a filename but did not find the file contents, we need to die
                                throw new IllegalArgumentException("Could not find find DDL file resource ("+filepath+") for DB ("+getDatabaseType()+") in any searched classloader, cannot execute DDL");
                            }
                        }
                    } else {
                        // nothing to do here: we have a blank ddl and this is not a simple mapper so just continue on -AZ
                    }
                } else {
                    // turn DDL into an IS
                    ddlIS = new ByteArrayInputStream(ddl.getBytes());
                }
                if (ddlIS != null) {
                    // execute the ddl
                    executeDDLforType(ddlIS, type);
                }
            }
        }
    }

    /**
     * Get the input stream from this classloader
     */
    private InputStream getInputStreamFromClassloader(ClassLoader cl, String filename) {
        InputStream ddlIS = null;
        if (cl != null) {
            ddlIS = cl.getResourceAsStream(filename);
        }
        return ddlIS;
    }

    /**
     * Starts the DAO using the settings that have been pushed into it so far <br/>
     * There is no need to trigger this if it is being controlled by spring as spring
     * will call {@link #initDao()} automatically which calls this method
     * @throws RuntimeException if the dao fails to start
     */
    public void startup() {
        try {
            initDao();
        } catch (Exception e) {
            throw new RuntimeException("Failed to startup the dao: " + e.getMessage(), e);
        }
    }

    /**
     * Default constructor - does nothing and leaves the object in an incomplete state,
     * you need to at least set the following:
     * {@link #setDataSource(DataSource)} <br/>
     * {@link #setAutoDDL(boolean)} <br/>
     * {@link #setAutoCommitDDL(boolean)} <br/>
     * {@link #setDatabaseType(String)} <br/>
     * {@link #setDataMappers(List)} <br/>
     * <br/>
     * This does not actually start the DAO, run {@link #startup()} to start it <br/>
     * Note that this will be started automatically by Spring if this is created as a Spring bean,
     * no actions are necessary and setting an init method is not needed
     */
    public JdbcGenericDao() {
    }

    /**
     * Complete constructor, sets all required values for running the DAO,
     * does not actually start the DAO, run {@link #startup()} to start it <br/>
     * Note that this will be started automatically by Spring if this is created as a Spring bean,
     * no actions are necessary and setting an init method is not needed
     * 
     * @param dataSource the DataSource to use with this DAO
     * @param threadBoundDataSource if true then the DataSource will be bound to threads and 
     * only unbound and closed when {@link #closeConnection()} is called,
     * otherwise a new DataSource is obtained each time, 
     * this has no effect if the DataSource is a Spring DataSource
     * @param databaseType the databaseType that this DAO is connecting to (use constants in {@link DatabaseTranslator})
     * @param autoDDL if true then DDL is executed on DAO startup (can be run manually if desired)
     * @param autoCommitDDL if true then commit is executed after each DDL file is executed, if false then you need a TX manager to do this for you
     * @param dataMappers the data mappers which map this DAO to the tables
     */
    public JdbcGenericDao(DataSource dataSource, boolean threadBoundDataSource, 
            String databaseType, boolean autoDDL, boolean autoCommitDDL, DataMapper[] dataMappers) {
        setRequiredSettings(dataSource, threadBoundDataSource, databaseType, autoDDL, autoCommitDDL, dataMappers);
    }

    /**
     * Set all required settings for running the DAO,
     * can be used in the cases where using the complete constructor is inconvenient <br/>
     * This does not actually start the DAO, run {@link #startup()} to start it <br/>
     * Note that this will be started automatically by Spring if this is created as a Spring bean,
     * no actions are necessary and setting an init method is not needed
     * 
     * @param dataSource the DataSource to use with this DAO
     * @param threadBoundDataSource if true then the DataSource will be bound to threads and 
     * only unbound and closed when {@link #closeConnection()} is called,
     * otherwise a new DataSource is obtained each time, 
     * this has no effect if the DataSource is a Spring DataSource
     * @param databaseType the databaseType that this DAO is connecting to (use constants in {@link DatabaseTranslator})
     * @param autoDDL if true then DDL is executed on DAO startup (can be run manually if desired)
     * @param autoCommitDDL if true then commit is executed after each DDL file is executed, if false then you need a TX manager to do this for you
     * @param dataMappers the data mappers which map this DAO to the tables
     */
    public void setRequiredSettings(DataSource dataSource, boolean threadBoundDataSource, 
            String databaseType, boolean autoDDL, boolean autoCommitDDL, DataMapper[] dataMappers) {
        if (dataSource != null) {
            // correctly sets the datasource if non-spring or spring
            setNonSpringDataSource(dataSource, threadBoundDataSource);
        }
        setDatabaseType(databaseType);
        if (dataMappers == null || dataMappers.length == 0) {
            throw new IllegalArgumentException("DataMappers must be set and must be greater than one");
        }
        ArrayList<DataMapper> mappers = new ArrayList<DataMapper>();
        for (int i = 0; i < dataMappers.length; i++) {
            mappers.add(dataMappers[i]);
        }
        setDataMappers(mappers);
        setAutoDDL(autoDDL);
        setAutoCommitDDL(autoCommitDDL);
    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        // now we run our own inits
        // init the database translator based on the type
        initDatabaseTranslator();
        // init the list of classes and mappers related to them and execute DDL if needed
        initPersistentClasses();
        // init the caches
        initCaches();
    }

    /**
     * Find the tablename from the classname
     */
    protected String getTableNameFromClass(Class<?> type) {
        String tableName = getDataMapper(type).getTableName();
        if ("".equals(tableName) || tableName == null) {
            // generate the table name based on defaults
            tableName = BasicTranslator.makeTableNameFromClass(type);
        }
        return tableName;
    }

    /**
     * Make an entity of type T from a map of DB data
     * @param <T>
     * @param type a persistent entity type
     * @param data a map of column names to data values
     * @return an entity of type T with the data from the map in the entity
     */
    @SuppressWarnings("unchecked")
    protected <T> T makeEntityFromMap(Class<T> type, Map<String, Object> data) {
        if (type == null || data == null) {
            throw new NullPointerException("type and data cannot be null");
        }
        T entity = null;
        DataMapper dm = getDataMapper(type);
        if (dm != null && dm instanceof EntityColumnMapper) {
            entity = (T) ((EntityColumnMapper)dm).mapColumnsToObject(data);
        }
        if (entity == null) {
            // use reflection to construct and push the values into the object
            Map<String, Class<?>> types = ReflectUtils.getInstance().getFieldTypes(type, FieldsFilter.WRITEABLE);
            NamesRecord nr = getNamesRecord(type);
            entity = ReflectUtils.getInstance().constructClass(type);
            for (Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                String property = nr.getPropertyForColumn(key);
                if (property != null) {
                    Object value = entry.getValue();
                    // special handling for persistent types inside this type
                    Class<?> pType = null;
                    if (types.containsKey(property)) {
                        pType = types.get(property);
                        if (! getPersistentClasses().contains(pType)) {
                            pType = null;
                        }
                    }
                    if (pType != null) {
                        // this is another persistent object so this must be a foreign key
                        String pId = getIdProperty(pType);
                        // TODO use the old way once reflect utils can build the path automatically
                        //                property = property + "." + pId;
                        //                if (value != null 
                        //                && value.getClass().isAssignableFrom(pType)) {
                        //                value = ReflectUtil.getInstance().getFieldValue(value, pId);
                        //                }
                        // this will create the object for us for now
                        Object pValue = value;
                        if (value != null 
                                && value.getClass().isAssignableFrom(pType)) {
                            pValue = ReflectUtils.getInstance().getFieldValue(value, pId);
                        }
                        value = ReflectUtils.getInstance().constructClass(pType);
                        ReflectUtils.getInstance().setFieldValue(value, pId, pValue);
                    }
                    ReflectUtils.getInstance().setFieldValue(entity, property, value);
                }
            }            
        }
        return entity;
    }

    /**
     * Make a map of column names -> column values from an entity
     * @param entity
     * @return a map of the data in a persistent entity which has the column names as keys
     */
    protected Map<String, Object> makeMapFromEntity(Object entity) {
        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }
        Class<?> type = entity.getClass();
        Map<String, Object> data = null;
        DataMapper dm = getDataMapper(type);
        if (dm != null && dm instanceof EntityColumnMapper) {
            data = ((EntityColumnMapper)dm).mapObjectToColumns(entity);
        }
        if (data == null || data.isEmpty()) {
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            // get data from object using reflection
            Map<String, Class<?>> types = ReflectUtils.getInstance().getFieldTypes(type, FieldsFilter.ALL); // faster
            Map<String, Object> objectValues = ReflectUtils.getInstance().getObjectValues(entity, FieldsFilter.READABLE, false);
            NamesRecord nr = getNamesRecord(type);
            for (Entry<String, Object> entry : objectValues.entrySet()) {
                String property = entry.getKey();
                String dbName = nr.getColumnForProperty(property);
                if (dbName != null) {
                    Object value = entry.getValue();
                    // special handling for persistent types inside this type
                    Class<?> pType = null;
                    if (types.containsKey(property)) {
                        pType = types.get(property);
                        if (! getPersistentClasses().contains(pType)) {
                            pType = null;
                        }
                    }
                    if (pType != null) {
                        // this is another persistent object so this must be a foreign key
                        String pId = getIdProperty(pType);
                        value = ReflectUtils.getInstance().getFieldValue(value, pId);
                    }
                    data.put(dbName, value);
                }
            }
        }
        return data;
    }

    // *********** Helper methods

    /**
     * Logs a message to standard out, allows easy control over where logging is going later on
     * 
     * @param message the message to output
     */
    protected void logInfo(String message) {
        System.out.println("INFO: [GenericDao] " + message);
    }

    /**
     * Logs a message to standard out, allows easy control over where logging is going later on
     * 
     * @param message the message to output
     */
    protected void logWarn(String message) {
        System.out.println("WARN: [GenericDao] " + message);
    }

    /**
     * This will determine the id property correctly (but fairly inefficiently) so this should be cached,
     * use {@link #getIdProperty(Class)} to get the id property for a class
     * @param type a persistent type
     * @return
     */
    protected String findIdProperty(Class<?> type) {
        String idProp = null;
        DataMapper dm = getDataMapper(type);
        if (dm != null) {
            idProp = dm.getIdPropertyName();
        }
        if (classes.contains(type)) {
            if (idProp == null) {
                // look for the annotation
                idProp = ReflectUtils.getInstance().getFieldNameWithAnnotation(type, PersistentId.class);
            }
            if (idProp == null) {
                idProp = "id";
            }
        }
        return idProp;
    }

    /**
     * @param type a persistent type
     * @return the id column name
     */
    public String getIdColumn(Class<?> type) {
        String idProp = getIdProperty(type);
        String idColumn = getNamesRecord(type).getColumnForProperty(idProp);
        if (idColumn == null && classes.contains(type)) {
            idColumn = "ID";
        }
        return idColumn;
    }

    /**
     * @param object a persistent object
     * @return the value in the id property for the supplied object
     */
    protected Object getIdValue(Object object) {
        Class<?> entityClass = object.getClass();
        Object id = null;
        String idProp = getIdProperty(entityClass);
        if (idProp != null) {
            id = ReflectUtils.getInstance().getFieldValue(object, idProp);
        }
        return id;
    }

    /**
     * This helper method will convert the incoming data if it needs to be
     * converted for the given column, otherwise it will do nothing to the value
     * @param type the persistent class type
     * @param column the name of the column this value is associated with
     * @param value the value to convert
     * @return the converted value or the original value if no conversion needed
     */
    protected Object convertColumn(Class<?> type, String column, Object value) {
        if (type != null && column != null) {
            NamesRecord namesRecord = getNamesRecord(type);
            value = JDBCUtils.convertColumn(namesRecord, column, value);
        }
        return value;
    }

    /**
     * @return the template to use when generating insert statements,
     * uses the {@link StatementMapper#TABLE_NAME}, {@link StatementMapper#COLUMNS} and {@link StatementMapper#VALUES} constants to indicate replacements
     */
    public String getInsertTemplate(Class<?> type) {
        DataMapper dm = getDataMapper(type);
        String template = null;
        if (dm instanceof StatementMapper) {
            template = ((StatementMapper)dm).getInsertTemplate();
        }
        if (template == null || "".equals(template)) {
            template = StatementMapper.BASIC_INSERT;
        }
        return template;      
    }

    /**
     * @return the template to use when generating selects,
     * uses the {@link StatementMapper#SELECT}, {@link StatementMapper#TABLE_NAME}, and {@link StatementMapper#WHERE} constants to indicate replacements
     */
    public String getSelectTemplate(Class<?> type) {
        DataMapper dm = getDataMapper(type);
        String template = null;
        if (dm instanceof StatementMapper) {
            template = ((StatementMapper)dm).getSelectTemplate();
        }
        if (template == null || "".equals(template)) {
            template = StatementMapper.BASIC_SELECT;
        }
        return template;      
    }

    /**
     * @return the template to use when generating updates,
     * uses the {@link StatementMapper#TABLE_NAME}, {@link StatementMapper#UPDATE} and {@link StatementMapper#WHERE} constants to indicate replacements
     */
    public String getUpdateTemplate(Class<?> type) {
        DataMapper dm = getDataMapper(type);
        String template = null;
        if (dm instanceof StatementMapper) {
            template = ((StatementMapper)dm).getUpdateTemplate();
        }
        if (template == null || "".equals(template)) {
            template = StatementMapper.BASIC_UPDATE;
        }
        return template;      
    }

    /**
     * @return the template to use when generating deletes,
     * uses the {@link StatementMapper#TABLE_NAME} and {@link StatementMapper#WHERE} constants to indicate replacements
     */
    public String getDeleteTemplate(Class<?> type) {
        DataMapper dm = getDataMapper(type);
        String template = null;
        if (dm instanceof StatementMapper) {
            template = ((StatementMapper)dm).getDeleteTemplate();
        }
        if (template == null || "".equals(template)) {
            template = StatementMapper.BASIC_DELETE;
        }
        return template;      
    }


    // ********* DDL methods

    /**
     * Clear the table associated with this DAO persistent class
     * @param type any persistent type
     */
    public void clearDataForType(Class<?> type) {
        String sql = "TRUNCATE TABLE " + getTableNameFromClass(type);
        try {
            getSpringJdbcTemplate().execute(sql);
            if (showSQL) {
                logInfo("SQL="+sql);
            }
        } catch (DataAccessException e) {
            // ok, try doing the clear using a delete from which is much slower
            sql = "DELETE FROM " + getTableNameFromClass(type);
            if (showSQL) {
                logInfo("SQL="+sql);
            }
            getSpringJdbcTemplate().execute(sql);
        } finally {
            commitTransaction();
        }
    }

    /**
     * Drop the table associated with this persistent type
     * @param type any persistent type
     */
    public void dropTableForType(Class<?> type) {
        String sql = "DROP TABLE " + getTableNameFromClass(type);
        try {
            if (showSQL) {
                logInfo("SQL="+sql);
            }
            getSpringJdbcTemplate().execute("DROP TABLE " + getTableNameFromClass(type));
            commitTransaction();
        } catch (DataAccessException e) {
            rollbackTransaction();
            throw e;
        }
    }

    /**
     * Execute a DDL (database definition language) script, this will execute
     * a set of DDL commands which are fed in via an InputStream<br/>
     * The first non-comment ('--') line will be run, and if successful, 
     * all other non-comment lines will be run. SQL statements may be on 
     * multiple lines but must have ';' terminators.<br/>
     * <br/>
     * <b>NOTE:</b> The script should be located in the current ClassLoader,
     * in other words, it must be a visible resource that is packaged with
     * the code that is running this method<br/>
     * Recommended (Example) usage:<br/>
     * 1) Place the script into the src folder of your service impl project like so:
     * <code>impl/src/sql/oracle/myscript.sql</code><br/>
     * 2) Load the sql files into the jar by setting up the maven pom like so:
     * <xmp>
<resources>
...
   <resource>
      <directory>${basedir}/src/sql</directory>
      <includes>
         <include>** /*.sql</include>
      </includes>
   </resource>
...
</resources></xmp> 
     * <b>Note:</b> remove the extra space between "**" and "/*.sql"<br/>
     * 3) Execute the ddl script in the init method of your DAO (when it first starts up):
     * <code>ClassLoader loader = this.getClass().getClassLoader();</code><br/>
     * <code>String ddlResource = getDatabaseType() + "/myscript.sql";</code><br/>
     * <code>InputStream stream = loader.getResourceAsStream(ddlResource);</code><br/>
     * <code>executeDDL(stream);</code><br/>
     * 
     * @param sqlDDL the sql ddl commands to execute
     * @throws IllegalArgumentException if ddl is invalid and cannot be executed
     */
    public void executeDDL(InputStream sqlDDL) {
        if (sqlDDL == null) {
            throw new IllegalArgumentException("sqlDDL cannot be null");
        }

        executeDDLforType(sqlDDL, null);
    }

    private void executeDDLforType(InputStream sqlDDL, Class<?> type) {
        // Now run the DDL commands if possible
        try {
            if (isAutoCommitDDL()) {
                commitTransaction(); // start the transaction
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(sqlDDL));
            try {
                // read the first line, skipping any '--' comment lines
                boolean firstLine = true;
                StringBuffer buf = new StringBuffer();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    line = line.trim();
                    if (line.startsWith("--")) continue;
                    if (line.length() == 0) continue;

                    // add the line to the buffer
                    buf.append(' ');
                    buf.append(line);

                    // process if the line ends with a ';'
                    boolean process = line.endsWith(";");

                    if (!process) continue;

                    // remove trailing ';'
                    buf.setLength(buf.length() - 1);

                    String ddl = buf.toString().trim();
                    // FIXME do replacements even if we do not know the type
                    if (type != null) {
                        // handle ddl replacements
                        ddl = handleTypeReplacements(type, ddl);
                    }

                    // run the first line as the test - if it fails, we are done
                    if (firstLine) {
                        firstLine = false;
                        try {
                            if (showSQL) {
                                logInfo("DDL="+ddl);
                            }
                            getSpringJdbcTemplate().execute(ddl);
                        } catch (DataAccessException e) {
                            //log.info("Could not to execute first DDL ("+ddl+"), skipping the rest");
                            logInfo("Could not execute first DDL line, skipping the rest: " + e.getMessage() + ":" + e.getCause());
                            //e.printStackTrace();
                            return;
                        }
                    } else {
                        // run other lines, until done - any one can fail (we will report it)
                        try {
                            if (showSQL) {
                                logInfo("DDL="+ddl);
                            }
                            getSpringJdbcTemplate().execute(ddl);
                        } catch (DataAccessException e) {
                            throw new IllegalArgumentException("Failed while executing ddl: " + e.getMessage(), e);
                        }
                    }
                    if (isAutoCommitDDL()) {
                        commitTransaction();
                    }

                    // clear the buffer for next
                    buf.setLength(0);
                }
            } catch (IOException any) {
                throw new RuntimeException("Failure while processing DDL", any);
            } finally {
                try {
                    r.close();
                } catch (IOException any) {
                    //log.warn("Failure while closing DDL inputstream reader", any);
                }
                // close the connection used for this DDL
                if (isAutoCommitDDL()) {
                    closeConnection();
                }
            }
        } finally {
            try {
                sqlDDL.close();
            } catch (IOException any) {
                //log.warn("Failure while closing inputstream", any);
            }
        }
    }

    // *********** SQL methods

    /**
     * Make SQL from a template
     * 
     * @param sqlTemplate pass in an SQL template (one of the BASIC_* ones or make your own),
     * the replacement names will be replaced with the replacement values
     * @param tableName the name of the table for this SQL
     * @param replacements a sequence of replacement names (probably {@link #COLUMNS}, {@link #WHERE}, etc.) and values,
     * alternating like so: name,value,name,value
     * @return the SQL with all replacements made
     */
    protected String makeSQL(String sqlTemplate, String tableName, String... replacements) {
        String sql = sqlTemplate;
        sql = sql.replace(StatementMapper.TABLE_NAME, tableName);
        for (int i = 0; i < replacements.length; i++) {
            if (replacements.length < i + 1) {
                break;
            }
            sql = sql.replace(replacements[i], replacements[i+1]);
            i++;
        }
        // put in the select replacement last in case it was already replaced
        sql = sql.replace(StatementMapper.SELECT, "*");
        return sql;
    }

    /**
     * Handle the replacements of DDL constants in the supplied DDL with real values
     * based on the supplied type<br/>
     * The following keys will be replaced automatically:<br/>
     * {TABLENAME} - the value returned by {@link #getTableName()}<br/>
     * {ID} - the column name of the unique identifier<br/>
     * {TABLENAME:org.domain.MyClass} - the value returned by {@link #getTableName()} for the persistent type MyClass<br/>
     * {ID:org.domain.MyClass} - the column name of the unique identifier for the persistent type MyClass<br/>
     * {COLUMNNAME:propertyName} - the column name which maps to the propertyName<br/>
     * {IDSEQNAME} - (Oracle) a sequence name will be generated and inserted
     * based on the table name for use in generating IDs,
     * if you want to specify your own sequence name then you will lose
     * the ability to have the ID inserted into newly created objects<br/>
     * 
     * @param type a persistent class type
     * @param ddl database definition statements (can also be SQL)
     * @return the DDL string with the {vars} replaced
     */
    protected String handleTypeReplacements(Class<?> type, String ddl) {
        //StringBuilder sb = new StringBuilder(ddl);
        if (type == null || ddl == null) {
            throw new IllegalArgumentException("Type and ddl cannot be null");
        }
        String tableName = getTableNameFromClass(type);
        ddl = ddl.replace(DataMapper.DDL_TABLENAME, tableName);
        ddl = ddl.replace(DataMapper.DDL_ID_COLUMN, getIdColumn(type));
        if (DataMapper.DBTYPE_ORACLE.equals(getDatabaseType())) {
            ddl = ddl.replace(DataMapper.DDL_ID_SEQNAME, OracleTranslator.getOracleSeqName(tableName) );
        }
        // now check for the other types and handle them if needed
        if (ddl.contains(DataMapper.DDL_TABLENAME_TYPE_PREFIX)) {
            // resolve the classname for a tablename
            List<Class<?>> pClasses = getPersistentClasses();
            for (Class<?> pType : pClasses) {
                // attempt to replace all parent tablenames
                String tName = getTableNameFromClass(pType);
                ddl = ddl.replace(DataMapper.DDL_TABLENAME_TYPE_PREFIX + pType.getName() + "}", tName);
                ddl = ddl.replace(DataMapper.DDL_TABLENAME_TYPE_PREFIX + pType.getSimpleName() + "}", tName);
            }
        }
        if (ddl.contains(DataMapper.DDL_ID_TYPE_PREFIX)) {
            // resolve the classname for a tablename
            List<Class<?>> pClasses = getPersistentClasses();
            for (Class<?> pType : pClasses) {
                // attempt to replace all parent tablenames
                String pId = getIdColumn(pType);
                ddl = ddl.replace(DataMapper.DDL_ID_TYPE_PREFIX + pType.getName() + "}", pId);
                ddl = ddl.replace(DataMapper.DDL_ID_TYPE_PREFIX + pType.getSimpleName() + "}", pId);
            }
        }
        if (ddl.contains(DataMapper.DDL_COLUMN_PREFIX)) {
            // resolve the classname for a tablename
            NamesRecord nr = getNamesRecord(type);
            for (String property : nr.getPropertyNames()) {
                // attempt to replace all property names
                String column = nr.getColumnForProperty(property);
                ddl = ddl.replace(DataMapper.DDL_COLUMN_PREFIX + property + "}", column);
            }
            // TODO support for columns by type?
        }
        return ddl;
    }

    /**
     * Create comparison SQL but converts the value object to a string
     * 
     * @param column the name of a database column
     * @param comparisonConstant the comparison constant (e.g. EQUALS)
     * @param value the value to compare the property to
     * @return a string representing the SQL snippet (e.g. propA = ?)
     */
    protected String makeComparisonSQL(String column, int comparisonConstant, Object value) {
        return JDBCUtils.makeComparisonSQL(column, comparisonConstant, value);
    }

    /**
     * @param params a set of params to add this value to
     * @param column the name of a database column
     * @param comparisonConstant the comparison constant (e.g. EQUALS)
     * @param value the value to compare the property to
     * @return a string representing the SQL snippet (e.g. propA = ?)
     */
    protected String makeComparisonSQL(List<Object> params, String column, int comparisonConstant, Object value) {
        return JDBCUtils.makeComparisonSQL(params, column, comparisonConstant, value);
    }


    // ** Transaction methods

    /**
     * This will do a commit on the current DB Connection on this thread,
     * allows the developer to force a commit immediately <br/>
     * Remember to close your connection if you are completely done with it
     * 
     * @return true if it committed successfully
     */
    public boolean commitTransaction() {
        boolean success = false;
        Connection conn = getConnection();
        boolean previousAutocommit = false;
        try {
            previousAutocommit = conn.getAutoCommit();
            conn.commit();
            conn.setAutoCommit(previousAutocommit);
            success = true;
        } catch (SQLException e) {
            logWarn("Could not commit sucessfully: " + e.getMessage());
            success = false;
            try {
                conn.setAutoCommit(previousAutocommit);
            } catch (SQLException e1) {
                // nothing to do here but continue
            }
        }
        // removed: try-finally-releaseConnection(conn);
        return success;
    }

    /**
     * This will do a rollback on the DB Connection on this thread,
     * allows the developer to force an immediate rollback <br/>
     * Remember to close your connection if you are completely done with it
     * 
     * @return true if the rollback executed successfully
     */
    public boolean rollbackTransaction() {
        boolean success = false;
        Connection conn = getConnection();
        boolean previousAutocommit = false;
        try {
            previousAutocommit = conn.getAutoCommit();
            conn.rollback();
            conn.setAutoCommit(previousAutocommit);
            success = true;
        } catch (SQLException e) {
            logWarn("Could not rollback sucessfully: " + e.getMessage());
            success = false;
            try {
                conn.setAutoCommit(previousAutocommit);
            } catch (SQLException e1) {
                // nothing to do here but continue
            }
        }
        // removed: try-finally-releaseConnection(conn);
        return success;
    }

    /**
     * Allows a developer to do a manual release of the connection,
     * this will cause a new connection to be obtained the next time a method is executed,
     * anything not yet committed for this connection will be rolled back and lost,
     * normally if you are using a DataSource which pools connections then you do not
     * need to worry about this too much, just do it at the end of your work unit
     * 
     * @return true if the connection was closed
     */
    public boolean closeConnection() {
        boolean success = false;
        try {
            Connection conn = getConnection();
            try {
                conn.rollback();
            } catch (Exception e) {
                // oh well, keep going
            }
            DataSourceUtils.doReleaseConnection(conn, getDataSource());
            success = true;
        } catch (CannotGetJdbcConnectionException e) {
            logWarn("Could not close connection sucessfully: " + e.getMessage());
        } catch (SQLException e) {
            logWarn("Could not close connection sucessfully: " + e.getMessage());
        }
        return success;
    }

    // PUBLIC IMPLS

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.GenericDao#getIdProperty(java.lang.Class)
     */
    public String getIdProperty(Class<?> type) {
        String idProp;
        try {
            idProp = getNamesRecord(type).getIdProperty();
            if (idProp == null && classes.contains(type)) {
                idProp = "id";
            }
        } catch (IllegalArgumentException e) {
            idProp = null;
        }
        return idProp;
    }

    /**
     * MUST be overridden
     */
    @SuppressWarnings("unchecked")
    protected <T> T baseFindById(Class<T> type, Serializable id) {
        String idColumn = getIdColumn(type);
        String sql = makeSQL(getSelectTemplate(type), getTableNameFromClass(type), StatementMapper.WHERE, "where " + getIdColumn(type) + " = ?");
        T entity = null;
        try {
            // convert the type if needed
            try {
                id = (Serializable) convertColumn(type, idColumn, id);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("all ids must be Serializable, change the property type defined in the names record for the id to be a Serializable type: " + e.getMessage(), e);
            }
            // get the map of values
            Object[] params = new Object[] {id};
            if (showSQL) {
                logInfo("SQL="+sql+":\n Params="+ArrayUtils.arrayToString(params));
            }
            Map<String, Object> data = getSpringJdbcTemplate().queryForMap(sql, params);
            entity = makeEntityFromMap(type, data);
        } catch (IncorrectResultSizeDataAccessException e) {
            entity = null;
        }

        return entity;
    }

    /**
     * MUST be overridden
     */
    protected Serializable baseCreate(Class<?> type, Object object) {
        StringBuilder rKeys = new StringBuilder();
        StringBuilder rVals = new StringBuilder();
        ArrayList<Object> replacementValues = new ArrayList<Object>();

        Map<String, Object> values = makeMapFromEntity(object);
        String idColumn = getIdColumn(type);
        String tableName = getTableNameFromClass(type);
        Object idValue = null;
        int counter = 0;
        for (Entry<String, Object> entry : values.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            // convert the value if needed
            value = convertColumn(type, column, value);
            // handle the id column specially
            if (idColumn.equals(column)) {
                idValue = value;
                if (idValue == null) {
                    continue; // expect to autogenerate an id
                }
                // id was specified so use that instead of autogenerating
            }
            if (counter > 0) {
                rKeys.append(",");
                rVals.append(",");
            }
            rKeys.append(column);
            rVals.append("?");
            replacementValues.add(value);
            counter++;
        }

        String sql = makeSQL(getInsertTemplate(type), tableName, StatementMapper.COLUMNS, rKeys.toString(), 
                StatementMapper.VALUES, rVals.toString());
        if (showSQL) {
            logInfo("SQL="+sql+":\n Params="+replacementValues);
        }
        int rows = getSpringJdbcTemplate().update(sql, replacementValues.toArray(new Object[replacementValues.size()]));
        if (rows <= 0) {
            throw new RuntimeException("Could not create DB insert based on object: " + object);
        }

        String idProperty = getIdProperty(type);
        Class<?> idType = ReflectUtils.getInstance().getFieldType(type, idProperty);
        if (idValue == null) {
            // now get the new id and put it in the object
            try {
                String idSQL = getDatabaseTranslator().makeAutoIdQuery(tableName, idColumn);
                if (showSQL) {
                    logInfo("SQL="+idSQL);
                }
                idValue = getSpringJdbcTemplate().queryForObject(idSQL, idType);
                if (idValue == null) {
                    throw new NullPointerException("Null value for new id retrieved from DB");
                }
                ReflectUtils.getInstance().setFieldValue(object, idProperty, idValue);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Could not get back the autogenerated ID of the newly inserted object: " + object + " :" + e.getMessage(), e);
            }
        }
        if (! Serializable.class.isAssignableFrom(idValue.getClass())) {
            idValue = idValue.toString();
        }
        return (Serializable) idValue;
    }

    /**
     * MUST be overridden
     */
    protected void baseUpdate(Class<?> type, Object id, Object object) {
        String idColumn = getIdColumn(type);
        StringBuilder update = new StringBuilder();
        ArrayList<Object> replacementValues = new ArrayList<Object>();

        Map<String, Object> values = makeMapFromEntity(object);
        int counter = 0;
        for (Entry<String, Object> entry : values.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            // convert the value if needed
            value = convertColumn(type, column, value);
            if (idColumn.equals(column)) {
                continue; // skip the id on the first pass
            }
            if (counter > 0) {
                update.append(",");
            }
            update.append(column);
            update.append("=?");
            replacementValues.add(value);
            counter++;
        }
        // add the id in at the end
        replacementValues.add(id);

        // do the update
        String sql = makeSQL(getUpdateTemplate(type), getTableNameFromClass(type), 
                StatementMapper.UPDATE, update.toString(), StatementMapper.WHERE, "where " + idColumn + " = ?");
        if (showSQL) {
            logInfo("SQL="+sql+":\n Params="+replacementValues);
        }
        int rows = getSpringJdbcTemplate().update(sql, replacementValues.toArray(new Object[replacementValues.size()]));
        if (rows <= 0) {
            throw new RuntimeException("Could not update entity based on object: " + object);
        }
    }

    /**
     * MUST be overridden
     */
    protected <T> boolean baseDelete(Class<T> type, Serializable id) {
        String idColumn = getIdColumn(type);
        String sql = makeSQL(getDeleteTemplate(type), getTableNameFromClass(type), 
                StatementMapper.WHERE, "where " + idColumn + " = ?");
        // convert the type if needed
        try {
            id = (Serializable) convertColumn(type, idColumn, id);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("all ids must be Serializable, change the property type defined in the names record for the id to be a Serializable type: " + e.getMessage(), e);
        }
        Object[] params = new Object[] {id};
        if (showSQL) {
            logInfo("SQL="+sql+":\n Params="+ArrayUtils.arrayToString(params));
        }
        int rows = getSpringJdbcTemplate().update(sql, params);
        return rows > 0;
    }


    /**
     * Override this if desired
     */
    protected Serializable baseGetIdValue(Object object) {
        Class<?> type = object.getClass();
        String idProp = getIdProperty(type);
        Object idValue = null;
        if (idProp == null) {
            throw new IllegalArgumentException("Could not find id property for this class type: " + type);
        } else {
            idValue = ReflectUtils.getInstance().getFieldValue(object, idProp);
        }
        Serializable serialId = null;
        if (idValue != null) {
            if (idValue instanceof Serializable) {
                serialId = (Serializable) idValue;
            } else {
                serialId = idValue.toString();
            }
        }
        return serialId;
    }

    /**
     * Find the class type of a persistent object,
     * needed in the case that we are working with something that wraps it's objects<br/>
     * Override this if desired
     * @param entity a persistent entity
     * @return the persistent class type OR null if it cannot be found
     */
    protected Class<?> findClass(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot find class type of null entity object");
        }
        Class<?> type = entity.getClass();
        return type;
    }

    // COMMON CODE

    private List<Class<?>> classes;
    /**
     * This does a nice bit of exception handling for us and verifies that
     * this class is valid to perform a DAO operation with
     * @param type class type of the persistent object to check
     * @return A valid entityClass type resolved to be the same as
     * the ones usable by this DAO
     */
    protected Class<?> checkClass(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type cannot be null");
        }

        if (classes == null) {
            throw new NullPointerException("persistent classes must be set");
        }

        for (Iterator<Class<?>> i = classes.iterator(); i.hasNext();) {
            Class<?> concrete = (Class<?>) i.next();
            if (concrete.isAssignableFrom(type)) {
                return concrete;
            }
        }
        throw new IllegalArgumentException("Could not resolve this class " + 
                type + " as part of the set of persistent objects: " +
                classes.toString());
    }

    private CacheProvider cacheProvider;
    /**
     * @return the current cache provider
     */
    protected CacheProvider getCacheProvider() {
        if (cacheProvider == null) {
            cacheProvider = new NonCachingCacheProvider();
        }
        return cacheProvider;
    }
    /**
     * Set the cache provider to an implementation of {@link CacheProvider},
     * this will be set to {@link NonCachingCacheProvider} if this is not set explicitly
     * @param cacheProvider
     */
    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }
    /**
     * @return the cache name used for storing this type of persistent object
     */
    protected String getCacheName(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return type.getName();
    }
    /**
     * @return the cachename used for storing search results for this type of object
     */
    protected String getSearchCacheName(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        return "search:" + type.getName();
    }
    /**
     * Creates all the caches for the current set of persistent types,
     * this should be called in the init for the generic dao being used after persistent classes are loaded
     */
    protected void initCaches() {
        for (Class<?> type : classes) {
            getCacheProvider().createCache(getCacheName(type));
            getCacheProvider().createCache(getSearchCacheName(type));
        }
    }

    // INTERCEPTOR methods

    private Map<Class<?>, ReadInterceptor> readInterceptors = new ConcurrentHashMap<Class<?>, ReadInterceptor>();
    private Map<Class<?>, WriteInterceptor> writeInterceptors = new ConcurrentHashMap<Class<?>, WriteInterceptor>();
    /**
     * Adds the provided interceptor to the current set of interceptors
     * @param interceptor
     */
    public void addInterceptor(DaoOperationInterceptor interceptor) {
        if (interceptor != null) {
            Class<?> type = interceptor.interceptType();
            if (type != null) {
                if (ReadInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                    readInterceptors.put(type, (ReadInterceptor) interceptor);
                }
                if (WriteInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                    writeInterceptors.put(type, (WriteInterceptor) interceptor);
                }
            }
        }
    }
    /**
     * Removes the provided interceptor from the current set of interceptors
     * @param interceptor
     */
    public void removeInterceptor(DaoOperationInterceptor interceptor) {
        if (interceptor != null) {
            Class<?> type = interceptor.interceptType();
            if (type != null) {
                if (ReadInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                    readInterceptors.remove(type);
                }
                if (WriteInterceptor.class.isAssignableFrom(interceptor.getClass())) {
                    writeInterceptors.remove(type);
                }
            }
        }
    }
    /**
     * Makes the given interceptor the only active interceptor,
     * removes all others and adds only this one
     * @param interceptor
     */
    public void setInterceptor(DaoOperationInterceptor interceptor) {
        if (interceptor != null) {
            readInterceptors.clear();
            writeInterceptors.clear();
            addInterceptor(interceptor);
        }
    }

    protected void beforeRead(String operation, Class<?> type, Serializable[] ids, Search search) {
        if (type != null) {
            ReadInterceptor interceptor = readInterceptors.get(type);
            if (interceptor != null) {
                interceptor.beforeRead(operation, ids, search);
            }
        }
    }

    protected void afterRead(String operation, Class<?> type, Serializable[] ids, Search search, Object[] entities) {
        if (type != null) {
            ReadInterceptor interceptor = readInterceptors.get(type);
            if (interceptor != null) {
                interceptor.afterRead(operation, ids, search, entities);
            }
        }
    }

    protected void beforeWrite(String operation, Class<?> type, Serializable[] ids, Object[] entities) {
        // first handle autocommit
        if (isAutoCommitOperations()) {
            commitTransaction();
        }
        if (type != null) {
            WriteInterceptor interceptor = writeInterceptors.get(type);
            if (interceptor != null) {
                interceptor.beforeWrite(operation, ids, entities);
            }
        }
    }

    protected void afterWrite(String operation, Class<?> type, Serializable[] ids, Object[] entities, int changes) {
        if (type != null) {
            WriteInterceptor interceptor = writeInterceptors.get(type);
            if (interceptor != null) {
                interceptor.afterWrite(operation, ids, entities, changes);
            }
        }
        // finally handle autocommit
        if (isAutoCommitOperations()) {
            commitTransaction();
        }
    }

    // ********* PUBLIC methods ****************


    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.GenericDao#getPersistentClasses()
     */
    public List<Class<?>> getPersistentClasses() {
        return new ArrayList<Class<?>>(classes);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.GenericDao#invokeTransactionalAccess(java.lang.Runnable)
     */
    public void invokeTransactionalAccess(Runnable toinvoke) {
        toinvoke.run();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.GenericDao#findById(java.lang.Class, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public <T> T findById(Class<T> type, Serializable id) {
        checkClass(type);
        if (id == null) {
            throw new IllegalArgumentException("id must be set to find persistent object");
        }

        T entity = null;
        // check cache first
        String key = id.toString();
        String cacheName = getCacheName(type);
        if (getCacheProvider().exists(cacheName, key)) {
            entity = (T) getCacheProvider().get(cacheName, key);
        } else {
            // not in cache so go to the DB

            // before interceptor
            String operation = "findById";
            beforeRead(operation, type, new Serializable[] {id}, null);

            entity = baseFindById(type, id);

            // now put the item in the cache
            getCacheProvider().put(cacheName, key, entity);

            // after interceptor
            afterRead(operation, type, new Serializable[] {id}, null, new Object[] {entity});
        }
        return entity;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#create(java.lang.Object)
     */
    public void create(Object object) {
        Class<?> type = findClass(object);
        checkClass(type);

        String operation = "create";
        beforeWrite(operation, type, null, new Object[] {object});

        Serializable idValue = baseCreate(type, object);

        // clear the search caches
        getCacheProvider().clear(getSearchCacheName(type));

        afterWrite(operation, type, new Serializable[] {idValue}, new Object[] {object}, 1);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#update(java.lang.Object)
     */
    public void update(Object object) {
        Class<?> type = findClass(object);
        checkClass(type);

        Serializable idValue = baseGetIdValue(object);
        if (idValue == null) {
            throw new IllegalArgumentException("Could not get an id value from the supplied object, cannot update without an id: " + object);
        }

        String operation = "update";
        beforeWrite(operation, type, new Serializable[] {idValue}, new Object[] {object});

        baseUpdate(type, idValue, object);

        // clear the search caches
        getCacheProvider().clear(getSearchCacheName(type));
        // clear the cache entry since this was updated
        String key = idValue.toString();
        String cacheName = getCacheName(type);
        getCacheProvider().remove(cacheName, key);

        afterWrite(operation, type, new Serializable[] {idValue}, new Object[] {object}, 1);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#save(java.lang.Object)
     */
    public void save(Object object) {
        Serializable id = baseGetIdValue(object);
        if (id == null) {
            create(object);
        } else {
            update(object);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#delete(java.lang.Object)
     */
    public void delete(Object object) {
        Class<?> type = findClass(object);
        Serializable id = baseGetIdValue(object);
        delete(type, id);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.modifiers.BasicModifier#delete(java.lang.Class, java.io.Serializable)
     */
    public <T> boolean delete(Class<T> type, Serializable id) {
        checkClass(type);

        String operation = "delete";
        beforeWrite(operation, type, new Serializable[] {id}, null);

        boolean removed = baseDelete(type, id);

        if (removed) {
            // clear the search caches
            getCacheProvider().clear(getSearchCacheName(type));
            // clear this from the cache
            String key = id.toString();
            String cacheName = getCacheName(type);
            getCacheProvider().remove(cacheName, key);

            afterWrite(operation, type, new Serializable[] {id}, null, 1);
        }
        return removed;
    }

}
