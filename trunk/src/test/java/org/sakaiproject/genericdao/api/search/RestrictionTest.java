/**
 * $Id$
 * $URL$
 * RestrictionTest.java - genericdao - Jul 13, 2008 4:45:30 PM - azeckoski
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

import junit.framework.TestCase;


/**
 * Testing Restriction
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RestrictionTest extends TestCase {

   String title = "aaronz";
   Restriction r1 = new Restriction("title", title);
   Restriction r2 = new Restriction("title", title);
   Restriction r3 = new Restriction("title", title, Restriction.NOT_EQUALS);

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#hashCode()}.
    */
   public void testHashCode() {
      int i1 = r1.hashCode();
      int i2 = r1.hashCode();
      assertEquals(i1, i2);

      int i3 = r2.hashCode();
      assertEquals(i1, i3);

      int i4 = r3.hashCode();
      assertNotSame(i1, i4);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#getProperty()}.
    */
   public void testGetProperty() {
      assertEquals("title", r1.getProperty());
      assertEquals("title", r2.getProperty());
      assertEquals("title", r3.getProperty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#setProperty(java.lang.String)}.
    */
   public void testSetProperty() {
      r1.setProperty("aaronz");
      assertEquals("aaronz", r1.getProperty());
      r2.setProperty("aaronz");
      assertEquals("aaronz", r2.getProperty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#getValue()}.
    */
   public void testGetValue() {
      assertEquals("aaronz", r1.getValue());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#setValue(java.lang.Object)}.
    */
   public void testSetValue() {
      r1.setValue("aaronz222");
      assertEquals("aaronz222", r1.getValue());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#getComparison()}.
    */
   public void testGetComparison() {
      assertEquals(Restriction.EQUALS, r1.getComparison());
      assertEquals(Restriction.EQUALS, r2.getComparison());
      assertEquals(Restriction.NOT_EQUALS, r3.getComparison());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#setComparison(int)}.
    */
   public void testSetComparison() {
      r1.setComparison(Restriction.NOT_EQUALS);
      assertEquals(Restriction.NOT_EQUALS, r1.getComparison());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#Restriction(java.lang.String, java.lang.Object)}.
    */
   public void testRestrictionStringObject() {
      Restriction rest = new Restriction("az", "stuff");
      assertNotNull(rest);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#Restriction(java.lang.String, java.lang.Object, int)}.
    */
   public void testRestrictionStringObjectInt() {
      Restriction rest = new Restriction("az", "stuff", Restriction.GREATER);
      assertNotNull(rest);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#equals(java.lang.Object)}.
    */
   public void testEqualsObject() {
      assertEquals(r1, r2);
      assertNotSame(r1, r3);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Restriction#toString()}.
    */
   public void testToString() {
      assertNotNull(r1.toString());
      assertNotNull(r2.toString());
      assertNotNull(r3.toString());
   }

}
