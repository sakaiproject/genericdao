/**
 * $Id$
 * $URL$
 * JdbcBasicGenericDao.java - genericdao - Apr 26, 2008 12:27:46 AM - azeckoski
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.azeckoski.reflectutils.ArrayUtils;
import org.sakaiproject.genericdao.api.BasicGenericDao;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.mappers.NamesRecord;
import org.sakaiproject.genericdao.api.mappers.StatementMapper;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;
import org.sakaiproject.genericdao.springjdbc.translators.BasicTranslator;
import org.sakaiproject.genericdao.util.JDBCUtils;
import org.sakaiproject.genericdao.util.JDBCUtils.QueryData;

/**
 * Spring JDBC based implementation of BasicGenericDao which can be extended to add more
 * specialized DAO methods.
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@SuppressWarnings("deprecation")
public class JdbcBasicGenericDao extends JdbcGenericDao implements BasicGenericDao {

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
    public JdbcBasicGenericDao() {
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
    public JdbcBasicGenericDao(DataSource dataSource, boolean threadBoundDataSource, 
            String databaseType, boolean autoDDL, boolean autoCommitDDL, DataMapper[] dataMappers) {
        super(dataSource, threadBoundDataSource, databaseType, autoDDL, autoCommitDDL, dataMappers);
    }


    protected QueryData makeQueryFromSearch(Class<?> type, Search search) {
        // Checks to see if the required params are set and throws exception if not
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }

        NamesRecord namesRecord = getNamesRecord(type);
        return JDBCUtils.makeSQLfromSearch(namesRecord, search);
    }


    // OVERRIDES

    protected <T> long baseCountBySearch(Class<T> type, Search search) {
        QueryData sd = makeQueryFromSearch(type, search);
        String sql = makeSQL(getSelectTemplate(type), getTableNameFromClass(type), 
                StatementMapper.SELECT, "count(*)", StatementMapper.WHERE, sd.getAfterTableSQL());
        if (showSQL) {
            logInfo("SQL="+sql+":\n Params="+ArrayUtils.arrayToString(sd.getArgs()));
        }
        long count = getSpringJdbcTemplate().queryForObject(sql, Long.class, sd.getArgs());
        return count;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> baseFindBySearch(Class<T> type, Search search) {
        String tableName = getTableNameFromClass(type);
        QueryData sd = makeQueryFromSearch(type, search);
        String sql = makeSQL(getSelectTemplate(type), tableName, 
                StatementMapper.SELECT, tableName + ".*", StatementMapper.WHERE, sd.getAfterTableSQL());
        // handle limit/offset
        int firstResult = (int) search.getStart();
        int maxResults = (int) search.getLimit();
        if (firstResult > 0 || maxResults > 0) {
            sql = getDatabaseTranslator().makeLimitQuery(sql, search.getStart(), search.getLimit(), tableName);
            getSpringJdbcTemplate().setMaxRows(firstResult + maxResults); // this limit is always ok to put in
        }
        if (showSQL) {
            logInfo("SQL="+sql+":\n Params="+ArrayUtils.arrayToString(sd.getArgs()));
        }
        List<Map<String, Object>> rMap = getSpringJdbcTemplate().queryForList(sql, sd.getArgs());
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

    protected <T> T baseFindOneBySearch(Class<T> type, Search search) {
        T item = null;
        search.setLimit(1); // only return 1 item
        List<T> items = baseFindBySearch(type, search);
        if (items.size() > 0) {
            item = items.get(0);
        }
        return item;
    }


    // COMMON CODE

    public <T> long countBySearch(Class<T> type, Search search) {
        checkClass(type);
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }
        long count = 0;

        // check the cache first
        boolean usedCache = false;
        String searchCacheName = getSearchCacheName(type);
        String cacheKey = "countBySearch::" + type.getName() + ":" + search.toString();
        if (getCacheProvider().exists(searchCacheName, cacheKey)) {
            Long lCount = (Long) getCacheProvider().get(searchCacheName, cacheKey);
            if (lCount != null) {
                count = lCount.longValue();
                usedCache = true;
            }
        }

        if (! usedCache) {
            count = baseCountBySearch(type, search);

            // cache the id results for the search
            getCacheProvider().put(searchCacheName, cacheKey, Long.valueOf(count));
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findBySearch(Class<T> type, Search search) {
        checkClass(type);
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }
        List<T> results = new ArrayList<T>();

        // check the cache first
        boolean usedCache = false;
        String cacheName = getCacheName(type);
        String searchCacheName = getSearchCacheName(type);
        String cacheKey = "findBySearch::" + type.getName() + ":" + search.toString();
        if (getCacheProvider().exists(searchCacheName, cacheKey)) {
            String[] resultIds = (String[]) getCacheProvider().get(searchCacheName, cacheKey);
            if (resultIds != null) {
                for (int i = 0; i < resultIds.length; i++) {
                    if (! getCacheProvider().exists(cacheName, resultIds[i])) {
                        usedCache = false;
                        break;
                    }
                    T entity = (T) getCacheProvider().get(cacheName, resultIds[i]);
                    results.add(entity);
                }
                usedCache = true;
            }
        }

        if (! usedCache) {
            String operation = "findBySearch";
            beforeRead(operation, type, null, search);

            results = baseFindBySearch(type, search);

            // run through the returned items for the interceptor and for caching
            List<String> keys = new ArrayList<String>();
            for (T entity : results) {
                Object id = baseGetIdValue(entity);
                // cache each returned item
                String key = id.toString();
                keys.add(key);
                getCacheProvider().put(cacheName, key, entity);
            }
            // cache the id results for the search
            String[] ids = keys.toArray(new String[keys.size()]);
            getCacheProvider().put(searchCacheName, cacheKey, ids);
            // call the after interceptor
            afterRead(operation, type, ids, search, results.toArray(new Object[results.size()]));
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public <T> T findOneBySearch(Class<T> type, Search search) {
        checkClass(type);
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }
        T entity = null;

        // check the cache first
        boolean usedCache = false;
        String cacheName = getCacheName(type);
        String searchCacheName = getSearchCacheName(type);
        String cacheKey = "findOneBySearch::" + type.getName() + ":" + search.toString();
        if (getCacheProvider().exists(searchCacheName, cacheKey)) {
            usedCache = true;
            String id = (String) getCacheProvider().get(searchCacheName, cacheKey);
            if (id != null) {
                if (getCacheProvider().exists(cacheName, id)) {
                    entity = (T) getCacheProvider().get(cacheName, id);
                }
            }
        }

        if (! usedCache) {
            String operation = "findOneBySearch";
            beforeRead(operation, type, null, search);

            search.setLimit(1); // only return 1 item

            entity = baseFindOneBySearch(type, search);

            String key = null;
            if (entity != null) {
                Serializable id = baseGetIdValue(entity);
                afterRead(operation, type, new Serializable[] {id}, search, new Object[] {entity});

                if (id != null) {
                    // cache the entity
                    key = id.toString();
                    getCacheProvider().put(cacheName, key, entity);
                }
            }
            // cache the search result
            getCacheProvider().put(searchCacheName, cacheKey, key);
        }
        return entity;
    }


    // DEPRECATED

    /**
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public int countByProperties(Class entityClass, String[] objectProperties, Object[] values) {
        int[] comparisons = new int[objectProperties.length];
        for (int i = 0; i < comparisons.length; i++) {
            comparisons[i] = Restriction.EQUALS;
        }
        return countByProperties(entityClass, objectProperties, values, comparisons);
    }

    /**
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public int countByProperties(Class entityClass, String[] objectProperties, Object[] values,
            int[] comparisons) {
        if (objectProperties.length != values.length || values.length != comparisons.length) {
            throw new IllegalArgumentException("All input arrays must be the same size");
        }
        Search search = new Search();
        for (int i = 0; i < values.length; i++) {
            search.addRestriction( new Restriction(objectProperties[i], values[i], comparisons[i]) );
        }
        return (int) countBySearch(entityClass, search);
    }

    /** 
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public List findByProperties(Class entityClass, String[] objectProperties, Object[] values) {
        int[] comparisons = new int[objectProperties.length];
        for (int i = 0; i < comparisons.length; i++)
            comparisons[i] = Restriction.EQUALS;

        return findByProperties(checkClass(entityClass), objectProperties, values, comparisons, 0, 0);
    }

    /** 
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
            int[] comparisons) {
        return findByProperties(entityClass, objectProperties, values, comparisons, null, 0, 0);
    }

    /** 
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
            int[] comparisons, String[] sortProperties) {
        return findByProperties(entityClass, objectProperties, values, comparisons, sortProperties, 0, 0);
    }

    /** 
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
            int[] comparisons, int firstResult, int maxResults) {
        return findByProperties(entityClass, objectProperties, values, comparisons, null, firstResult,
                maxResults);
    }

    /**
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public List findByProperties(Class entityClass, String[] objectProperties, Object[] values,
            int[] comparisons, String[] sortProperties, int firstResult, int maxResults) {
        if (objectProperties.length != values.length || values.length != comparisons.length) {
            throw new IllegalArgumentException("All input arrays must be the same size");
        }
        Search search = new Search();
        for (int i = 0; i < values.length; i++) {
            search.addRestriction( new Restriction(objectProperties[i], values[i], comparisons[i]) );
        }
        if (sortProperties != null) {
            for (int i = 0; i < sortProperties.length; i++) {
                int location = sortProperties[i].indexOf(" ");
                String property = sortProperties[i];
                if (location > 0) {
                    property = sortProperties[i].substring(0, location);
                }
                Order order = null;
                if (sortProperties[i].endsWith(ByPropsFinder.DESC)) {
                    order = new Order(property, false);
                } else {
                    order = new Order(property, true);
                }
                search.addOrder( order );
            }
        }
        search.setStart(firstResult);
        search.setLimit(maxResults);
        return findBySearch(entityClass, search);
    }

}
