/**
 * $Id$
 * $URL$
 * Transient.java - genericdao - May 19, 2008 11:08:06 AM - azeckoski
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
 * This annotation specifies that the property or field is not persistent. 
 * It is used to annotate a property or field of an entity class, mapped superclass, or embeddable class.<br/>
 * <a href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Transient.html">http://java.sun.com/javaee/5/docs/api/javax/persistence/Transient.html</a>
 * <br/>
 * Reproduced from the java.persistence API
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Transient { }
