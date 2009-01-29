/**
 * $Id$
 * $URL$
 * Column.java - genericdao - May 19, 2008 11:33:32 AM - azeckoski
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


/**
 * Is used to specify a mapped column for a persistent property or field. If no Column annotation is specified, the default values are applied.<br/>
 * <a href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Column.html">http://java.sun.com/javaee/5/docs/api/javax/persistence/Column.html</a>
 * <br/>
 * Reproduced from the java.persistence API
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public @interface Column {
   String columnDefinition() default "";
   boolean insertable() default true;
   int length() default 255;
   String name() default "";
   boolean nullable() default true;
   int precision() default 0;
   int scale() default 0;
   String table() default "";
   boolean unique() default false;
   boolean updateable() default true;
}
