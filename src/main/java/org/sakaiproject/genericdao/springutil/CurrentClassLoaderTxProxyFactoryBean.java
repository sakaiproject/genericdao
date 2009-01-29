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

	protected ClassLoader myClassLoader = CurrentClassLoaderTxProxyFactoryBean.class.getClassLoader();

	/*** only works with Spring 2.0.x
   @Override
   public void setBeanClassLoader(ClassLoader classLoader) {
      // this is basically ignoring the input classLoader and setting it to the one
      // which this class is currently part of
      super.setBeanClassLoader(myClassLoader);
   }
	 ***/

	// needed to make this work with spring 1.2.8
	@Override
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy(myClassLoader);
	}

}
