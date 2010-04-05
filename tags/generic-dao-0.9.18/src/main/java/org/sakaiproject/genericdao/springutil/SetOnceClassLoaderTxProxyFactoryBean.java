/**
 * $Id$
 * $URL$
 * ClassLoaderTxProxyFactoryBean.java - genericdao - May 3, 2008 6:57:09 PM - azeckoski
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

/**
 * This is needed to keep the {@link ClassLoader} from getting overwritten after we set it,
 * the {@link ClassLoader} can only be set one time using the setter method<br/>
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class SetOnceClassLoaderTxProxyFactoryBean extends CurrentClassLoaderTxProxyFactoryBean {
	private static final long serialVersionUID = 1L;

	boolean alreadySet = false;
	public void setProxyClassLoader(ClassLoader classLoader) {
		if (classLoader != null) {
			if (! alreadySet) {
				this.myClassLoader = classLoader;
				alreadySet = true;
			}
		}
	}

}
