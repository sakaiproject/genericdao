/**
 * $Id$
 * $URL$
 * GenericTestObject.java - genericdao - May 18, 2008 4:34:33 PM - azeckoski
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

/**
 * This is a generic POJO for testing DAO methods
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class GenericTestObject {
	
	private Long id;
	private String title;
	private Boolean hiddenItem;

	/**
	 * Default constructor
	 */
	public GenericTestObject() {
	}

	/**
	 * Full constructor
	 */
	public GenericTestObject(String title, Boolean hiddenItem) {
		this.title = title;
		this.hiddenItem = hiddenItem;
	}

   public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
   public Boolean getHiddenItem() {
      return hiddenItem;
   }
   public void setHiddenItem(Boolean hiddenItem) {
      this.hiddenItem = hiddenItem;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj)
         return false;
      if (!(obj instanceof GenericTestObject))
         return false;
      else {
         GenericTestObject castObj = (GenericTestObject) obj;
         if (null == this.id || null == castObj.id)
            return false;
         else
            return (this.id.equals(castObj.id));
      }
   }

   @Override
   public int hashCode() {
      if (null == this.id)
         return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.id.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "GTO: id=" + id + ", title=" + title + ", hidden=" + hiddenItem;
   }
}
