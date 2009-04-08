/**
 * $Id$
 * $URL$
 * SearchTest.java - genericdao - Jul 13, 2008 5:03:58 PM - azeckoski
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
 * Testing the main search object
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SearchTest extends TestCase {

   Search s1 = new Search("title", "az");
   Search s2 = new Search("title", "az");
   Search s3 = new Search("title", "az", Restriction.NOT_EQUALS);

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#hashCode()}.
    */
   public void testHashCode() {
      int i1 = s1.hashCode();
      int i2 = s1.hashCode();
      assertEquals(i1, i2);

      int i3 = s2.hashCode();
      assertEquals(i1, i3);

      int i4 = s3.hashCode();
      assertNotSame(i1, i4);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setStart(long)}.
    */
   public void testSetStart() {
      s1.setStart(25);
      assertEquals(25, s1.getStart());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getStart()}.
    */
   public void testGetStart() {
      assertEquals(0, s1.getStart());
      assertEquals(0, s2.getStart());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setLimit(long)}.
    */
   public void testSetLimit() {
      s1.setLimit(25);
      assertEquals(25, s1.getLimit());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getLimit()}.
    */
   public void testGetLimit() {
      assertEquals(0, s1.getLimit());
      assertEquals(0, s2.getLimit());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#isConjunction()}.
    */
   public void testIsConjunction() {
      assertEquals(true, s1.isConjunction());
      assertEquals(true, s2.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setConjunction(boolean)}.
    */
   public void testSetConjunction() {
      s1.setConjunction(false);
      assertEquals(false, s1.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getRestrictions()}.
    */
   public void testGetRestrictions() {
      Restriction[] rs = s1.getRestrictions();
      assertNotNull(rs);
      assertEquals("title", rs[0].getProperty());
      assertEquals("az", rs[0].getValue());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setRestrictions(org.sakaiproject.genericdao.api.search.Restriction[])}.
    */
   public void testSetRestrictions() {
      Restriction[] rs = new Restriction[1];
      rs[0] = new Restriction("aaron","test");
      s1.setRestrictions(rs);
      assertEquals(1, s1.getRestrictions().length);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getOrders()}.
    */
   public void testGetOrders() {
      Order[] os = s1.getOrders();
      assertNotNull(os);
      assertTrue(os.length == 0);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setOrders(org.sakaiproject.genericdao.api.search.Order[])}.
    */
   public void testSetOrders() {
      Order[] os = new Order[1];
      os[0] = new Order("title", true);
      s1.setOrders(os);
      assertEquals(1, s1.getOrders().length);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getQueryString()}.
    */
   public void testGetQueryString() {
      String qs = s1.getQueryString();
      assertNull(qs);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#setQueryString(java.lang.String)}.
    */
   public void testSetQueryString() {
      s1.setQueryString("aaronz = testval");
      assertNotNull(s1.getQueryString());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search()}.
    */
   public void testSearch() {
      Search s = new Search();
      assertNotNull(s);
      assertTrue(s.isEmpty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Search)}.
    */
   public void testSearchSearch() {
      Search s = new Search(s1);
      assertNotNull(s);
      assertEquals(s1, s);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String)}.
    */
   public void testSearchString() {
      Search s = new Search("test");
      assertNotNull(s);
      assertEquals("test", s.getQueryString());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String, java.lang.Object)}.
    */
   public void testSearchStringObject() {
      Search s = new Search("aaron", Long.valueOf(3));
      assertNotNull(s);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", Long.valueOf(3)));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String, java.lang.Object, int)}.
    */
   public void testSearchStringObjectInt() {
      Search s = new Search("aaron", Long.valueOf(3), Restriction.NOT_EQUALS);
      assertNotNull(s);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", Long.valueOf(3), Restriction.NOT_EQUALS));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[])}.
    */
   public void testSearchStringArrayObjectArray() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4});
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah"));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[], boolean)}.
    */
   public void testSearchStringArrayObjectArrayBoolean() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4}, false);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah"));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4));
      assertFalse(s.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[], int[])}.
    */
   public void testSearchStringArrayObjectArrayIntArray() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4}, 
            new int[] {Restriction.NOT_EQUALS, Restriction.GREATER});
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah", Restriction.NOT_EQUALS));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4, Restriction.GREATER));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[], int[], boolean)}.
    */
   public void testSearchStringArrayObjectArrayIntArrayBoolean() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4}, 
            new int[] {Restriction.NOT_EQUALS, Restriction.GREATER}, false);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah", Restriction.NOT_EQUALS));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4, Restriction.GREATER));
      assertFalse(s.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[], int[], org.sakaiproject.genericdao.api.search.Order[])}.
    */
   public void testSearchStringArrayObjectArrayIntArrayOrderArray() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4}, 
            new int[] {Restriction.NOT_EQUALS, Restriction.GREATER}, 
            new Order[] {new Order("test", true)});
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah", Restriction.NOT_EQUALS));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4, Restriction.GREATER));
      assertTrue(s.getOrders().length == 1);
      assertEquals(s.getOrders()[0], new Order("test", true));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(java.lang.String[], java.lang.Object[], int[], org.sakaiproject.genericdao.api.search.Order[], long, long)}.
    */
   public void testSearchStringArrayObjectArrayIntArrayOrderArrayLongLong() {
      Search s = new Search(new String[] {"az","bz"}, new Object[] {"blah", 4}, 
            new int[] {Restriction.NOT_EQUALS, Restriction.GREATER}, 
            new Order[] {new Order("test", true)}, 10, 20);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("az", "blah", Restriction.NOT_EQUALS));
      assertEquals(s.getRestrictions()[1], new Restriction("bz", 4, Restriction.GREATER));
      assertTrue(s.getOrders().length == 1);
      assertEquals(s.getOrders()[0], new Order("test", true));
      assertEquals(10, s.getStart());
      assertEquals(20, s.getLimit());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction)}.
    */
   public void testSearchRestriction() {
      Search s = new Search(new Restriction("aaron", "test"));
      assertNotNull(s);
      assertEquals(1, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[])}.
    */
   public void testSearchRestrictionArray() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      });
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction, org.sakaiproject.genericdao.api.search.Order)}.
    */
   public void testSearchRestrictionOrder() {
      Search s = new Search(new Restriction("aaron", "test"), new Order("title"));
      assertNotNull(s);
      assertEquals(1, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order)}.
    */
   public void testSearchRestrictionArrayOrder() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order("title"));
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order[])}.
    */
   public void testSearchRestrictionArrayOrderArray() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order[] {
            new Order("title")
      });
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction, org.sakaiproject.genericdao.api.search.Order, long, long)}.
    */
   public void testSearchRestrictionOrderLongLong() {
      Search s = new Search(new Restriction("aaron", "test"), new Order("title"), 10, 20);
      assertNotNull(s);
      assertEquals(1, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
      assertEquals(10, s.getStart());
      assertEquals(20, s.getLimit());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order, long, long)}.
    */
   public void testSearchRestrictionArrayOrderLongLong() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order("title") );
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order[], long, long)}.
    */
   public void testSearchRestrictionArrayOrderArrayLongLong() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order[] {
            new Order("title")
      }, 10, 20);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
      assertEquals(10, s.getStart());
      assertEquals(20, s.getLimit());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order, long, long, boolean)}.
    */
   public void testSearchRestrictionArrayOrderLongLongBoolean() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order("title"), 10, 20, false);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
      assertEquals(10, s.getStart());
      assertEquals(20, s.getLimit());
      assertFalse(s.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#Search(org.sakaiproject.genericdao.api.search.Restriction[], org.sakaiproject.genericdao.api.search.Order[], long, long, boolean)}.
    */
   public void testSearchRestrictionArrayOrderArrayLongLongBoolean() {
      Search s = new Search(new Restriction[] {
            new Restriction("aaron", "test"),
            new Restriction("becky", "test")
      }, new Order[] {
            new Order("title")
      }, 10, 20, false);
      assertNotNull(s);
      assertEquals(2, s.getRestrictions().length);
      assertEquals(s.getRestrictions()[0], new Restriction("aaron", "test"));
      assertEquals(s.getRestrictions()[1], new Restriction("becky", "test"));
      assertEquals(1, s.getOrders().length);
      assertEquals(s.getOrders()[0], new Order("title"));
      assertEquals(10, s.getStart());
      assertEquals(20, s.getLimit());
      assertFalse(s.isConjunction());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#isEmpty()}.
    */
   public void testIsEmpty() {
      assertFalse(s1.isEmpty());
      assertTrue(new Search().isEmpty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#reset()}.
    */
   public void testReset() {
      s1.reset();
      assertTrue(s1.isEmpty());
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#equals(java.lang.Object)}.
    */
   public void testEqualsObject() {
      assertEquals(s1, s2);
      assertNotSame(s1, s3);
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#toString()}.
    */
   public void testToString() {
      assertNotNull(s1.toString());
      assertNotNull(s2.toString());
      assertNotNull(s3.toString());
   }



   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#addRestriction(org.sakaiproject.genericdao.api.search.Restriction)}.
    */
   public void testAddRestriction() {
      // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#addOrder(org.sakaiproject.genericdao.api.search.Order)}.
    */
   public void testAddOrder() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getRestrictionByProperty(java.lang.String)}.
    */
   public void testGetRestrictionByProperty() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#getRestrictionsProperties()}.
    */
   public void testGetRestrictionsProperties() {
   // TODO fail("Not yet implemented");
   }

   /**
    * Test method for {@link org.sakaiproject.genericdao.api.search.Search#copy(org.sakaiproject.genericdao.api.search.Search, org.sakaiproject.genericdao.api.search.Search)}.
    */
   public void testCopy() {
   // TODO fail("Not yet implemented");
   }

}
