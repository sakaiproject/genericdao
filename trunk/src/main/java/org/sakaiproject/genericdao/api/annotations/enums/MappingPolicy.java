/**
 * $Id$
 * $URL$
 * MappingPolicy.java - genericdao - May 19, 2008 11:12:40 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.annotations.enums;


/**
 * Defines the mapping policies allowed for automatic mapping
 * of persistent fields to database column names
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public enum MappingPolicy {
   /**
    * Indicates that the policy of turning the field names into
    * column names which use upper case characters and underscores should be used:
    * Example: myField -> MY_FIELD
    */
   UPPER_UNDERSCORES("upperUnderScores"),
   /**
    * Indicates that the fieldNames should become the column names as they are exactly:
    * Example: myField -> myField
    */
   FIELD_NAMES("fieldNames");

   private String policy;
   MappingPolicy(String policy) {
      this.policy = policy;
   }
   public String getPolicy() {
      return this.policy;
   }
}
