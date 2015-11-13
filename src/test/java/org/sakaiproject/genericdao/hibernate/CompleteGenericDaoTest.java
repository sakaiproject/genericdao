/**
 * $Id$
 * $URL$
 * GenericDaoTest.java - genericdao - May 18, 2008 4:34:33 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.genericdao.api.CompleteGenericDao;
import org.sakaiproject.genericdao.test.GenericTestObject;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.BeforeTransaction;

/**
 * Testing the {@link org.sakaiproject.genericdao.api.CompleteGenericDao}
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
@ContextConfiguration(locations={"/spring-common.xml","/spring-hibernate.xml"})
public class CompleteGenericDaoTest extends
AbstractTransactionalJUnit4SpringContextTests {

    protected CompleteGenericDao genericDao;

    private GenericTestObject gto1;
    private GenericTestObject gto2;
    private GenericTestObject gto3;
    private GenericTestObject gto4;
    private GenericTestObject gto5;

    // the values to return for fake data
    private final static String TEST_TITLE = "aaronz test object";

    // run this before each test starts
    @Before
    public void onSetUp() throws Exception {
    	// get the GenericDaoFinder from the spring context (you should inject this)
    	genericDao = (CompleteGenericDao) applicationContext.
    			getBean("org.sakaiproject.genericdao.dao.CompleteGenericDao");
    	if (genericDao == null) {
    		throw new RuntimeException("onSetUp: CompleteGenericDao could not be retrieved from spring context");
    	}

    	gto1 = new GenericTestObject(TEST_TITLE, Boolean.FALSE);
        gto2 = new GenericTestObject(TEST_TITLE + "2", Boolean.FALSE);
        gto3 = new GenericTestObject(TEST_TITLE + "3", Boolean.FALSE);
        gto4 = new GenericTestObject(TEST_TITLE + "4", Boolean.TRUE);
        gto5 = new GenericTestObject(TEST_TITLE + "5", Boolean.TRUE);

        // preload data
        genericDao.save(gto1);
        genericDao.save(gto2);
        genericDao.save(gto3);
        genericDao.save(gto4);
        genericDao.save(gto5);
    }


    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#countAll(java.lang.Class)}.
     */
    @Test
    public void testCountAll() {
        int count = genericDao.countAll(GenericTestObject.class);
        Assert.assertEquals(5, count);
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#findAll(java.lang.Class)}.
     */
    @Test
    public void testFindAllClass() {
        List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        Assert.assertTrue(l.contains(gto1));
        Assert.assertTrue(l.contains(gto2));
        Assert.assertTrue(l.contains(gto3));
        Assert.assertTrue(l.contains(gto4));
        Assert.assertTrue(l.contains(gto5));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#findAll(java.lang.Class, int, int)}.
     */
    @Test
    public void testFindAllClassIntInt() {
        List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class, 0, 2);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#findByExample(java.lang.Object)}.
     */
    @Test
    @SuppressWarnings({ "unchecked" })
    public void testFindByExampleObject() {
        GenericTestObject gto = new GenericTestObject();
        gto.setHiddenItem(Boolean.FALSE);
        List<GenericTestObject> l = genericDao.findByExample(gto);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains(gto1));
        Assert.assertTrue(l.contains(gto2));
        Assert.assertTrue(l.contains(gto3));
        Assert.assertTrue(! l.contains(gto4));
        Assert.assertTrue(! l.contains(gto5));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#findByExample(java.lang.Object, int, int)}.
     */
    @Test
    @SuppressWarnings({ "unchecked" })
    public void testFindByExampleObjectIntInt() {
        GenericTestObject gto = new GenericTestObject();
        gto.setHiddenItem(Boolean.FALSE);
        List l = genericDao.findByExample(gto,0,2);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(! l.contains(gto4));
        Assert.assertTrue(! l.contains(gto5));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#deleteMixedSet(java.util.Set[])}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteMixedSet() {
        Set deleteSet = new HashSet();
        deleteSet.add(gto2);
        deleteSet.add(gto3);
        Set[] setArray = new Set[] { deleteSet };
        genericDao.deleteMixedSet(setArray);

        List l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(! l.contains(gto2));
        Assert.assertTrue(! l.contains(gto3));

        // Now check that invalid objects cause failure
        deleteSet = new HashSet();
        deleteSet.add(gto1);
        Set deleteFailSet = new HashSet();
        deleteFailSet.add("string"); // non-matching object type
        deleteFailSet.add("string2"); // non-matching object type
        setArray = new Set[] { deleteSet, deleteFailSet };
        try {
            genericDao.deleteMixedSet(setArray);
            Assert.fail("Should have thrown an exception before getting here");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        } catch (Exception e) {
            Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        }

        l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains(gto1));
        Assert.assertTrue(! l.contains(gto2));
        Assert.assertTrue(! l.contains(gto3));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#deleteSet(java.util.Set)}.
     */
    @Test
    public void testDeleteSet() {
        Set<GenericTestObject> deleteSet = new HashSet<GenericTestObject>();
        deleteSet.add(gto1);
        deleteSet.add(gto2);
        genericDao.deleteSet(deleteSet);

        List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(! l.contains(gto1));
        Assert.assertTrue(! l.contains(gto2));

        // Now try to cause various Exceptions
        // test no longer needed
        //		Set<GenericTestObject> deleteFailSet = new HashSet<GenericTestObject>();
        //		deleteFailSet.add(gto4);
        //		deleteFailSet.add("string"); // non-matching object type
        //		try {
        //			genericDao.deleteSet(deleteFailSet);
        //			Assert.fail("Should have thrown an exception before getting here");
        //		} catch (IllegalArgumentException e) {
        //			Assert.assertNotNull(e);
        //		} catch (Exception e) {
        //			Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        //		}
        //
        //		l = genericDao.findAll(GenericTestObject.class);
        //		Assert.assertNotNull(l);
        //		Assert.assertEquals(3, l.size());
        //		Assert.assertTrue(l.contains(gto4));

        Set<String> deleteFailSet = new HashSet<String>();
        deleteFailSet.add("string"); // non-persistent object type
        try {
            genericDao.deleteSet(deleteFailSet);
            Assert.fail("Should have thrown an exception before getting here");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        } catch (Exception e) {
            Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        }

        l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains(gto4));

        // had to remove this test because it depends on order -AZ
        //		GenericTestObject gto = new GenericTestObject("title", Boolean.TRUE);
        //		deleteFailSet = new HashSet();
        //		// I don't like that order is important for this test to work... -AZ
        //		deleteFailSet.add(gto); // object is not in the DB
        //		deleteFailSet.add(gto4);
        //		try {
        //			genericDao.deleteSet(deleteFailSet);
        //			Assert.fail("Should have thrown an exception before getting here");
        //		} catch (InvalidDataAccessApiUsageException e) {
        //			Assert.assertNotNull(e);
        //		} catch (Exception e) {
        //			Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        //		}
        //
        //		l = genericDao.findAll(GenericTestObject.class);
        //		Assert.assertNotNull(l);
        //		Assert.assertEquals(3, l.size());
        //		Assert.assertTrue(l.contains(gto4));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveMixedSet(java.util.Set[])}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSaveMixedSet() {
        GenericTestObject gtoA = new GenericTestObject("titleA", Boolean.TRUE);
        GenericTestObject gtoB = new GenericTestObject("titleB", Boolean.FALSE);
        Set saveSet = new HashSet();
        saveSet.add(gtoA);
        saveSet.add(gtoB);
        Set[] setArray = new Set[] { saveSet };
        genericDao.saveMixedSet(setArray);

        List l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
        Assert.assertTrue(l.contains(gtoA));
        Assert.assertTrue(l.contains(gtoB));

        // Now check that invalid objects cause failure
        Set saveFailSet = new HashSet();
        saveFailSet.add("string"); // non-matching object type
        saveFailSet.add("string2"); // non-matching object type
        GenericTestObject gtoD = new GenericTestObject("titleD", Boolean.TRUE);
        saveSet = new HashSet();
        saveSet.add(gtoD);
        setArray = new Set[] { saveSet, saveFailSet };
        try {
            genericDao.saveMixedSet(setArray);
            Assert.fail("Should have thrown an exception before getting here");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        } catch (Exception e) {
            Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        }

        l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
        Assert.assertTrue(! l.contains(gtoD));
    }

    /**
     * Test method for {@link org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao#saveSet(java.util.Set)}.
     */
    @Test
    public void testSaveSet() {
        GenericTestObject gtoA = new GenericTestObject("titleA", Boolean.TRUE);
        GenericTestObject gtoB = new GenericTestObject("titleB", Boolean.FALSE);
        Set<GenericTestObject> saveSet = new HashSet<GenericTestObject>();
        saveSet.add(gtoA);
        saveSet.add(gtoB);
        genericDao.saveSet(saveSet);

        List<GenericTestObject> l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
        Assert.assertTrue(l.contains(gtoA));
        Assert.assertTrue(l.contains(gtoB));


        // Now try to cause various Exceptions
        // test no longer needed
        //		GenericTestObject gtoC = new GenericTestObject("titleC", Boolean.TRUE);
        //		saveSet = new HashSet<GenericTestObject>();
        //		saveSet.add(gtoC);
        //		saveSet.add("string"); // mixed types
        //		try {
        //			genericDao.saveSet(saveSet);
        //			Assert.fail("Should have thrown an exception before getting here");
        //		} catch (IllegalArgumentException e) {
        //			Assert.assertNotNull(e);
        //		} catch (Exception e) {
        //			Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        //		}
        //
        //		l = genericDao.findAll(GenericTestObject.class);
        //		Assert.assertNotNull(l);
        //		Assert.assertEquals(7, l.size());
        //		Assert.assertTrue(! l.contains(gtoC));

        Set<String> failSaveSet = new HashSet<String>();
        failSaveSet.add("string"); // not a persistent type
        try {
            genericDao.saveSet(failSaveSet);
            Assert.fail("Should have thrown an exception before getting here");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        } catch (Exception e) {
            Assert.fail("Threw wrong exception: ("+e.getCause()+"): " + e.getMessage());
        }

        l = genericDao.findAll(GenericTestObject.class);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
    }

}
