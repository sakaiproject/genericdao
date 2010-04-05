/**
 * $Id$
 * $URL$
 * Id.java - genericdao - May 19, 2008 10:56:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package javax.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the primary key property or field of an entity<br/>
 * <a href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Id.html">http://java.sun.com/javaee/5/docs/api/javax/persistence/Id.html</a>
 * <br/>
 * Reproduced from the java.persistence API
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Id { }
