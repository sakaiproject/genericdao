/**
 * $Id$
 * $URL$
 * Entity.java - genericdao - May 19, 2008 11:44:32 AM - azeckoski
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
 * Specifies that the class is an entity. This annotation is applied to the entity class.<br/>
 * <a href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Entity.html">http://java.sun.com/javaee/5/docs/api/javax/persistence/Entity.html</a>
 * <br/>
 * Reproduced from the java.persistence API
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
   String name() default "";
}
