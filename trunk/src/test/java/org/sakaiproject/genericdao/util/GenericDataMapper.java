/**
 * $Id$
 * $URL$
 * GenericDataMapper.java - genericdao - May 31, 2008 12:03:58 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.util;

import java.lang.reflect.Type;

import org.sakaiproject.genericdao.api.mappers.DataMapper;
import org.sakaiproject.genericdao.util.TypeReference;

/**
 * Allows a developer to use generics to define the persistent type when working with
 * datamappers, you must run the empty constructor when constructing your class or this
 * will fail to work, if you are not defining any constructors you do not have to do anything,
 * if you decide to define some constructors then you must call super() in your constructor
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public abstract class GenericDataMapper<T> implements DataMapper {

    private GenericType genTypeRef = new GenericType();
    private TypeReference<T> typeRef = new TypeReference<T>() {};

    /* (non-Javadoc)
     * @see org.sakaiproject.genericdao.api.mappers.DataMapper#getPersistentType()
     */
    public Class<?> getPersistentType() {
        @SuppressWarnings("unused")
        Type t1 = typeRef.getType();
        Type t = genTypeRef.getType();
        return (Class<?>) t;
    }

    protected class GenericType extends TypeReference<T> {}

}
