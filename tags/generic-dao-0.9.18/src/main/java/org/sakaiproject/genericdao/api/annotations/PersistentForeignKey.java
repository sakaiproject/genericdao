/**
 * $Id$
 * $URL$
 * PersistentForeignKey.java - genericdao - May 19, 2008 11:53:53 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that this column represents a foreign key value which points
 * to another persistent object, if this is a foreign key in the database
 * to a non-persistent table then use {@link PersistentTransient} to indicate it should be ignored<br/>
 * You can optionally specify a foreign table name and foreign id column manually if desired<br/>
 * Should be placed on a field/method in a persistent class<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PersistentForeignKey {
   /**
    * The name of the foreign table to map to may be specified,
    * otherwise the table is determined by looking at the type of the column this
    * annotation is on and finding the persistent class and therefore the table name for it
    */
   String foreignTableName() default "";
   /**
    * The name of the column in the foreign table to map to the column referred to by this annotation,
    * otherwise this is determined by looking at the type of the column this
    * annotation is on and finding the persistent class and therefore the persistent id for it
    */
   String foreignIdColumn() default "";
}
