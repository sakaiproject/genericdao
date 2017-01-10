/**
 * $Id$
 * $URL$
 * JdbcGeneralGenericDao.java - genericdao - Apr 26, 2008 4:33:33 PM - azeckoski
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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.genericdao.api.GeneralGenericDao;
import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.mappers.StatementMapper;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.BasicTranslator;
import org.sakaiproject.genericdao.util.JDBCUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

/**
 * A spring JDBC based implementation of {@link GeneralGenericDao}
 * which can be extended to add more specialized DAO methods.
 * This should meet most DAO needs.
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class JdbcGeneralGenericDao extends JdbcBasicGenericDao implements GeneralGenericDao {

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
    public JdbcGeneralGenericDao() {
        super();
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
    public JdbcGeneralGenericDao(DataSource dataSource, boolean threadBoundDataSource, 
            String databaseType, boolean autoDDL, boolean autoCommitDDL, DataMapper[] dataMappers) {
        super(dataSource, threadBoundDataSource, databaseType, autoDDL, autoCommitDDL, dataMappers);
    }


    protected class MyPSS implements BatchPreparedStatementSetter {

        private NamesRecord namesRecord;
        private List<String> dataKeys = new ArrayList<String>();
        private List<Object> entities = new ArrayList<Object>();

        public int getBatchSize() {
            return entities.size();
        }

        public void setValues(PreparedStatement ps, int index) throws SQLException {
            Object entity = entities.get(index);
            Map<String, Object> data = makeMapFromEntity(entity);
            for (int i = 0; i < dataKeys.size(); i++) {
                String column = dataKeys.get(i);
                Object value = data.get(column);
                // convert value if needed
                value = JDBCUtils.convertColumn(namesRecord, column, value);
                ps.setObject((i+1), value);
            }
        }

        /**
         * @param dataKeys the data keys (column names)
         * @param entities the objects to batch, each will be converted to a map
         * @param namesRecord the namesRecord for the persistent classes
         */
        public MyPSS(List<String> dataKeys, List<Object> entities, NamesRecord namesRecord) {
            this.dataKeys = dataKeys;
            this.entities = entities;
            this.namesRecord = namesRecord;
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.finders.AllFinder#findAll(java.lang.Class, int, int)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> type, int firstResult, int maxResults) {
        checkClass(type);
        String tableName = getTableNameFromClass(type);
        String sql = makeSQL(getSelectTemplate(type), tableName, 
                StatementMapper.SELECT, tableName + ".*", StatementMapper.WHERE, "");
        // handle limit/offset
        if (firstResult > 0 || maxResults > 0) {
            firstResult = firstResult < 0 ? 0 : firstResult;
            maxResults = maxResults < 0 ? 0 : maxResults;
            sql = getDatabaseTranslator().makeLimitQuery(sql, firstResult, maxResults, tableName);
            getSpringJdbcTemplate().setMaxRows(firstResult + maxResults); // this limit is always ok to put in
        }
        if (showSQL) {
            logInfo("SQL="+sql);
        }
        List<Map<String, Object>> rMap = getSpringJdbcTemplate().queryForList(sql);
        getSpringJdbcTemplate().setMaxRows(0); // reset this to no limit
        List<T> results = new ArrayList<T>();

        // put the results into objects
        int counter = 0;
        // SPECIAL handling for DERBY
        boolean derby = BasicTranslator.DBTYPE_DERBY.equals(getDatabaseType());
        for (Map<String, Object> data : rMap) {
            if (derby) {
                // derby has to filter results after the fact... lame yes indeed
                if (counter < firstResult) {
                    counter++;
                    continue;
                }
                if (maxResults > 0 && results.size() >= maxResults) {
                    break;
                }
            }
            T entity = makeEntityFromMap(type, data);
            results.add(entity);
            counter++;
        }
        return results;
    }

    // OVERRIDES

    /**
     * MUST override this method
     */
    protected <T> int baseCountAll(Class<T> type) {
        String sql = makeSQL(getSelectTemplate(type), getTableNameFromClass(type), 
                StatementMapper.SELECT, "count(*)", StatementMapper.WHERE, "");
        if (showSQL) {
            logInfo("SQL="+sql);
        }
        long count = getSpringJdbcTemplate().queryForObject(sql, Long.class);
        return (int) count;
    }

    /**
     * MUST override this method
     */
    protected <T> int baseSaveSet(Class<?> type, Set<T> entities) {
        String idProp = getIdProperty(type);
        List<String> keys = new ArrayList<String>();
        NamesRecord nr = getNamesRecord(type);

        List<Object> newObjects = new ArrayList<Object>();
        List<Object> existingObjects = new ArrayList<Object>();
        for (Object object : entities) {
            Object id = ReflectUtils.getInstance().getFieldValue(object, idProp);
            if (id == null) {
                newObjects.add(object);
            } else {
                existingObjects.add(object);            
            }
        }

        Map<String, Class<?>> types = ReflectUtils.getInstance().getFieldTypes(type);
        StringBuilder update = new StringBuilder();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        int counter = 0;
        for (String key : types.keySet()) {
            if (idProp.equals(key)) {
                continue;
            }
            if (counter > 0) {
                update.append(',');
                // insert
                columns.append(',');
                values.append(',');
            }
            String column = nr.getColumnForProperty(key);
            update.append(column);
            update.append("=?");
            keys.add(column);
            // insert
            columns.append(column);
            values.append('?');
            counter++;
        }
        // make and do inserts
        int changes = 0;
        if (newObjects.size() > 0) {
            String sql = makeSQL(getInsertTemplate(type), getTableNameFromClass(type), 
                    StatementMapper.COLUMNS, columns.toString(), StatementMapper.VALUES, values.toString());
            if (showSQL) {
                logInfo("SQL="+sql+":\n BatchCreate="+keys);
            }
            getSpringJdbcTemplate().batchUpdate(sql, new MyPSS(keys, newObjects, nr));
        }
        // make and do updates
        if (existingObjects.size() > 0) {
            keys.add( nr.getColumnForProperty(idProp) );
            String sql = makeSQL(getUpdateTemplate(type), getTableNameFromClass(type), 
                    StatementMapper.UPDATE, update.toString(), StatementMapper.WHERE, "where " + getIdColumn(type) + " = ?");
            // do the batch update
            if (showSQL) {
                logInfo("SQL="+sql+":\n BatchUpdate="+keys);
            }
            getSpringJdbcTemplate().batchUpdate(sql, new MyPSS(keys, existingObjects, nr));
        }
        return changes;
    }

    /**
     * MUST override this method
     */
    protected <T> int baseDeleteSet(Class<T> type, Serializable[] ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            Object id = ids[i];
            if (id != null) {
                if (i > 0) { sb.append(','); }
                sb.append('?');
            }
        }
        String sql = makeSQL(getDeleteTemplate(type), getTableNameFromClass(type), 
                StatementMapper.WHERE, "where " + getIdColumn(type) + " in (" + sb + ")");
        if (showSQL) {
            logInfo("SQL="+sql+":\n BatchDelete="+ArrayUtils.arrayToString(ids));
        }
        int changes = getSpringJdbcTemplate().update(sql, ids);
        return changes;
    }

    // COMMON CODE

    public <T> List<T> findAll(Class<T> type) {
        return findAll(type, 0, 0);
    }

    public <T> int countAll(Class<T> type) {
        checkClass(type);
        int count = 0;

        // check the cache first
        boolean usedCache = false;
        String searchCacheName = getSearchCacheName(type);
        String cacheKey = "countAll::" + type.getName();
        if (getCacheProvider().exists(searchCacheName, cacheKey)) {
            Integer iCount = (Integer) getCacheProvider().get(searchCacheName, cacheKey);
            if (iCount != null) {
                count = iCount.intValue();
                usedCache = true;
            }
        }

        if (! usedCache) {
            count = baseCountAll(type);

            // cache the id results for the search
            getCacheProvider().put(searchCacheName, cacheKey, Integer.valueOf(count));
        }
        return count;
    }

    public <T> void deleteSet(Class<T> type, Serializable[] ids) {
        checkClass(type);
        if (ids.length > 0) {
            String operation = "deleteSet";
            beforeWrite(operation, type, ids, null);

            int changes = baseDeleteSet(type, ids);

            afterWrite(operation, type, ids, null, changes);

            // clear all removed items from the cache
            String cacheName = getCacheName(type);
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] != null) {
                    String key = ids[i].toString();
                    getCacheProvider().remove(cacheName, key);
                }
            }
            // clear the search caches
            getCacheProvider().clear(getSearchCacheName(type));
        }
    }

    public <T> void saveSet(Set<T> entities) {
        if (entities == null || entities.isEmpty()) {
            System.out.println("WARN: Empty list of entities for saveSet, nothing to do...");
        } else {
            Class<?> type = checkEntitySet(entities);

            String operation = "saveSet";
            beforeWrite(operation, type, null, entities.toArray());

            int changes = baseSaveSet(type, entities);

            afterWrite(operation, type, null, entities.toArray(), changes);

            // clear all saved items from the cache
            String cacheName = getCacheName(type);
            for (T t : entities) {
                Object id = baseGetIdValue(t);
                if (id != null) {
                    String key = id.toString();
                    getCacheProvider().remove(cacheName, key);
                }
            }
            // clear the search caches
            getCacheProvider().clear(getSearchCacheName(type));
        }
    }

    public <T> void deleteSet(Set<T> entities) {
        if (entities.size() > 0) {
            Class<?> type = checkEntitySet(entities);
            List<Object> ids = new ArrayList<Object>();
            for (T t : entities) {
                Object id = baseGetIdValue(t);
                if (id != null) {
                    ids.add(id);
                }
            }
            deleteSet(type, ids.toArray(new Serializable[ids.size()]));
        }
    }

    @SuppressWarnings("unchecked")
    public void saveMixedSet(Set[] entitySets) {
        for (int i=0; i<entitySets.length; i++) {
            checkEntitySet(entitySets[i]);
        }
        for (int i=0; i<entitySets.length; i++) {
            saveSet(entitySets[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public void deleteMixedSet(Set[] entitySets) {
        for (int i=0; i<entitySets.length; i++) {
            checkEntitySet(entitySets[i]);
        }
        for (int i=0; i<entitySets.length; i++) {
            deleteSet(entitySets[i]);
        }
    }

    /**
     * Validates the class type and the list of entities before performing
     * a batch operation (throws IllegalArgumentException)
     * 
     * @param entities a Set of persistent entities, should all be of the same type
     */
    protected Class<?> checkEntitySet(Set<?> entities) {
        Class<?> entityClass = null;
        Iterator<?> it = entities.iterator();
        while(it.hasNext()) {
            Object entity = it.next();
            if (entityClass == null) {
                entityClass = (Class<?>) findClass(entity);
            }
            if (! checkClass(entityClass).isInstance(entity)) {
                throw new IllegalArgumentException("Entity set item " +
                        entity.toString() + " is not of type: " + entityClass +
                        ", the type is: " + entity.getClass() +
                " (All items must be of consistent persistent type)");
            }
        }
        return entityClass;
    }

}
