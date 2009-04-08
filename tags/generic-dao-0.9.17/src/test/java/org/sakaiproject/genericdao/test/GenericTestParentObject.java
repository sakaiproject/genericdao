/**
 * $Id$
 * $URL$
 * GenericTestParentObject.java - genericdao - May 9, 2008 10:04:28 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.test;

import org.sakaiproject.genericdao.api.annotations.PersistentId;

/**
 * This is a test object which contains the other test object in a tree like structure
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class GenericTestParentObject {

   @PersistentId
   private Long uid;
   private String title;
   private GenericTestObject gto;

   public GenericTestParentObject() {}

   public GenericTestParentObject(String title, GenericTestObject gto) {
      this.title = title;
      this.gto = gto;
   }
   
   @PersistentId
   public Long getUid() {
      return uid;
   }
   
   public void setUid(Long uid) {
      this.uid = uid;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public GenericTestObject getGto() {
      return gto;
   }

   public void setGto(GenericTestObject gto) {
      this.gto = gto;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj)
         return false;
      if (!(obj instanceof GenericTestParentObject))
         return false;
      else {
         GenericTestParentObject castObj = (GenericTestParentObject) obj;
         if (null == this.uid || null == castObj.uid)
            return false;
         else
            return (this.uid.equals(castObj.uid));
      }
   }

   @Override
   public int hashCode() {
      if (null == this.uid)
         return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.uid.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "GTPO: uid=" + uid + ", title=" + title + ", gto=" + gto;
   }

}
