/**
 * $Id$
 * $URL$
 * SetOnceClassLoaderBeanNameAutoProxyCreator.java - genericdao - May 7, 2008 10:31:14 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.springutil;

/**
 * This is needed to keep the {@link ClassLoader} from getting overwritten after we set it,
 * the {@link ClassLoader} can be set using the setter but can only be set one time<br/>
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class SetOnceClassLoaderBeanNameAutoProxyCreator extends CurrentClassLoaderBeanNameAutoProxyCreator {
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
