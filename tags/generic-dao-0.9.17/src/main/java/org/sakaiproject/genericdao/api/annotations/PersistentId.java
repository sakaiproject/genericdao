/**
 * $Id$
 * $URL$
 * EntityId.java - entity-broker - Apr 13, 2008 12:17:49 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.Id;

/**
 * Marks a getter method or field as the unique id for a persistent object,
 * the convention is to run toString on the return from the "getId" method
 * or the value in the "id" field<br/>
 * Should be placed on a field/method in a persistent class<br/>
 * <b>NOTE:</b> This annotation should only be used once in a class,
 * the getter method must take no arguments and return an object<br/>
 * Can also use the JPA {@link Id} annotation with the same effect
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PersistentId { }
