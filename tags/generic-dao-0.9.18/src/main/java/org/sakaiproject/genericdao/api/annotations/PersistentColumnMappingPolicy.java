/**
 * $Id$
 * $URL$
 * PersistentColumnMappingPolicy.java - genericdao - May 19, 2008 11:11:45 AM - azeckoski
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

import org.sakaiproject.genericdao.api.annotations.enums.MappingPolicy;


/**
 * Defines a persistent column mapping policy for this class,
 * this will default to using the {@link MappingPolicy#FIELD_NAMES} policy
 * by default or if this annotation is not set on the persistent class<br/>
 * Should be placed on the persistent class<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PersistentColumnMappingPolicy {
   MappingPolicy policy() default MappingPolicy.FIELD_NAMES;
}
