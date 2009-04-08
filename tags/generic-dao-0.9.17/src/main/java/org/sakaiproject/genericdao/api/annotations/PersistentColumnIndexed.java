/**
 * $Id$
 * $URL$
 * PersistentColumnIndexed.java - genericdao - May 19, 2008 11:49:47 AM - azeckoski
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
 * Indicates that a persistent column should be indexed,
 * can optionally specify a name for the index (should be less than 30 chars)<br/>
 * Should be placed on a field/method in a persistent class<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PersistentColumnIndexed {
   /**
    * The optional name of the index,
    * the default from the database will be used otherwise
    */
   String name() default "";
}
