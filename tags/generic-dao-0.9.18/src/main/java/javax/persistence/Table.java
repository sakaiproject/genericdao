/**
 * $Id$
 * $URL$
 * Table.java - genericdao - May 19, 2008 11:27:56 AM - azeckoski
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
 * This annotation specifies the primary table for the annotated entity.<br/>
 * If no Table annotation is specified for an entity class, the default values apply.<br/>
 * <a href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Table.html">http://java.sun.com/javaee/5/docs/api/javax/persistence/Table.html</a>
 * <br/>
 * Reproduced from the java.persistence API
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Table {
   String name();
   String schema() default "";
   String catalog() default "";
}
