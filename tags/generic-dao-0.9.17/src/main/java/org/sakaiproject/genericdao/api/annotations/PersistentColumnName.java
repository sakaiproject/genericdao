/**
 * $Id$
 * $URL$
 * PersistentColumnName.java - genericdao - May 19, 2008 11:04:00 AM - azeckoski
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

import javax.persistence.Column;


/**
 * Indicates the name of the column which this field maps to,
 * if this is not specified then the default {@link PersistentColumnMappingPolicy} for this class will be used<br/>
 * Should be placed on a field/method in a persistent class<br/>
 * Similar to the JPA {@link Column} annotation (this is a subset of that functionality)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PersistentColumnName {
   String value();
}
