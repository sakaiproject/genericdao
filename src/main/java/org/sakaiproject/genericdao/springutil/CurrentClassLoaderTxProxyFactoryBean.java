/**
 * $Id$
 * $URL$
 * CurrentClassLoaderTxProxyFactoryBean.java - genericdao - May 3, 2008 7:22:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springutil;

import org.springframework.aop.framework.AopProxy;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * This sets the {@link ClassLoader} for the {@link TransactionProxyFactoryBean} to the current
 * one that this class exists in<br/>
 * Compatible with Spring 1.2.8+
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class CurrentClassLoaderTxProxyFactoryBean extends TransactionProxyFactoryBean {
	private static final long serialVersionUID = 1L;

	protected transient ClassLoader myClassLoader = CurrentClassLoaderTxProxyFactoryBean.class.getClassLoader();

	protected boolean spring12x = false;
    protected boolean spring20x = false;

	public CurrentClassLoaderTxProxyFactoryBean() {
	    super();
	    try {
	        // only works with Spring 2.5.x - from Zach Thomas
	        super.setProxyClassLoader(myClassLoader);
	    } catch (NoSuchMethodError e) {
            System.out.println("Warning: Spring 2.5.x method (setProxyClassLoader) not found, falling back to spring 2.0.x method");
	        // try the spring 2.0.x version now
	        try {
	            setBeanClassLoader(myClassLoader);
	            spring20x = true;
	        } catch (NoSuchMethodError e1) {
	            System.out.println("Warning: Spring 2.0.x method (setBeanClassLoader) not found, falling back to spring 1.2.x method");
	            spring12x = true;
	        }
        }
	}

	/*** only works with Spring 2.0.x **/
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
	    if (spring20x) {
    	    // this is basically ignoring the input classLoader and setting it to the one
    	    // which this class is currently part of
    	    super.setBeanClassLoader(myClassLoader);
        } else {
            super.setBeanClassLoader(classLoader);
	    }
	}

	// needed to make this work with spring 1.2.8
	@Override
	protected Object getProxy(AopProxy aopProxy) {
	    if (spring12x) {
	        return aopProxy.getProxy(myClassLoader);
	    } else {
	        return super.getProxy(aopProxy);
	    }
	}

}
