/**
 * $Id$
 * $URL$
 * OrderTest.java - genericdao - Jul 13, 2008 4:05:32 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.search;

import org.sakaiproject.genericdao.api.search.Order;

import junit.framework.TestCase;

/**
 * Testing the order class
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class OrderTest extends TestCase {

   Order o1 = new Order("title");
   Order o2 = new Order("title");
   Order o3 = new Order("title", false);

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#hashCode()}.
    */
   public void testHashCode() {
      int i1 = o1.hashCode();
      int i2 = o1.hashCode();
      assertEquals(i1, i2);

      int i3 = o2.hashCode();
      assertEquals(i1, i3);

      int i4 = o3.hashCode();
      assertNotSame(i1, i4);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#getProperty()}.
    */
   public void testGetProperty() {
      assertEquals("title", o1.getProperty());
      assertEquals("title", o2.getProperty());
      assertEquals("title", o3.getProperty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#setProperty(java.lang.String)}.
    */
   public void testSetProperty() {
      o1.setProperty("aaronz");
      assertEquals("aaronz", o1.getProperty());
      o2.setProperty("aaronz");
      assertEquals("aaronz", o2.getProperty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#isAscending()}.
    */
   public void testIsAscending() {
      assertEquals(true, o1.isAscending());
      assertEquals(true, o2.isAscending());
      assertEquals(false, o3.isAscending());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#setAscending(boolean)}.
    */
   public void testSetAscending() {
      o1.setAscending(false);
      assertEquals(false, o1.isAscending());
      o3.setAscending(true);
      assertEquals(true, o3.isAscending());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#Order(java.lang.String)}.
    */
   public void testOrderString() {
      Order os = new Order("thing");
      assertNotNull(os);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#Order(java.lang.String, boolean)}.
    */
   public void testOrderStringBoolean() {
      Order os = new Order("thing", true);
      assertNotNull(os);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#equals(java.lang.Object)}.
    */
   public void testEqualsObject() {
      assertEquals(o1, o2);
      assertNotSame(o1, o3);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Order#toString()}.
    */
   public void testToString() {
      assertNotNull(o1.toString());
      assertNotNull(o2.toString());
      assertNotNull(o3.toString());
   }

}
