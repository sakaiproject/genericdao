/**
 * $Id$
 * $URL$
 * GenericDaoTest.java - genericdao - Apr 25, 2008 5:28:03 PM - azeckoski
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

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.api.translators.DatabaseTranslator;
import org.sakaiproject.genericdao.test.AbstractTestCrazyGenericDao;
import org.sakaiproject.genericdao.test.CrazyTestObject;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.GenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReallyCrazyGenericDaoTest extends AbstractTestCrazyGenericDao {

    private static JdbcGeneralGenericDao jdbcGenericDao; 

    @SuppressWarnings("deprecation")
    @BeforeClass
    public static void initDAO() {
        // create and startup embedded HSQLDB
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:hsqldb:.","sa","");
        assertNotNull(dataSource);
        
        // create and set the data mappers (one for each table)
        SimpleDataMapper crazyMapper = new SimpleDataMapper(CrazyTestObject.class, "id", "CRAZY_TEST_OBJECT2");
        crazyMapper.setDBTypeToFile( SimpleDataMapper.makeDDLMap("gto.sql", 
                new String[] {DatabaseTranslator.DBTYPE_HSQLDB}, "sql") );

        // start up the dao class with threadbound datasource and auto-commit
        jdbcGenericDao = new JdbcGeneralGenericDao(dataSource, true, 
                DatabaseTranslator.DBTYPE_HSQLDB, true, true, new DataMapper[] {crazyMapper});
        jdbcGenericDao.setAutoCommitOperations(false);
        jdbcGenericDao.startup();

        jdbcGenericDao.commitTransaction(); // clear anything pending

        // set the var used for testing
        genericDao = jdbcGenericDao;
    }

    @Before
    public void before() {
        assertNotNull(jdbcGenericDao);
        // preload the data
        initData();
        preloadGTOs(jdbcGenericDao);
    }

    @After
    public void after() {
        jdbcGenericDao.rollbackTransaction();
        jdbcGenericDao.closeConnection();
    }

}
