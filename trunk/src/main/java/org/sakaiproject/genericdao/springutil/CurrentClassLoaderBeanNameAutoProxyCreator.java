/**
 * $Id$
 * $URL$
 * CurrentClassLoaderBeanNameAutoProxyCreator.java - genericdao - May 7, 2008 10:24:18 AM - azeckoski
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.util.ClassUtils;

/**
 * This sets the {@link ClassLoader} for the {@link BeanNameAutoProxyCreator} to the current
 * one that this class exists in<br/>
 * Compatible with Spring 1.2.8+
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class CurrentClassLoaderBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator {
	private static final long serialVersionUID = 1L;

	protected ClassLoader myClassLoader = CurrentClassLoaderBeanNameAutoProxyCreator.class.getClassLoader();

	/*** only works with Spring 2.0.x
   @Override
   public void setBeanClassLoader(ClassLoader classLoader) {
      // this is basically ignoring the input classLoader and setting it to the one
      // which this class is currently part of
      super.setBeanClassLoader(myClassLoader);
   }
	 ***/

	// below needed to make this work with spring 1.2.8
	private boolean freezeProxy = false;
	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}
	@Override
	public boolean isFrozen() {
		return this.freezeProxy;
	}

	protected String[] interceptorNames = new String[0];
	@Override
	public void setInterceptorNames(String[] interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
	@Override
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	private boolean applyCommonInterceptorsFirst = true;
	@Override
	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
	}

	@SuppressWarnings("unchecked")
	protected boolean shouldProxyTargetClass(Class beanClass, String beanName) {
		// note that we are ignoring the ConfigurableListableBeanFactory check
		return isProxyTargetClass();
	}

	private Advisor[] resolveInterceptorNames() {
		Advisor[] advisors = new Advisor[this.interceptorNames.length];
		for (int i = 0; i < this.interceptorNames.length; i++) {
			Object next = getBeanFactory().getBean(this.interceptorNames[i]);
			advisors[i] = this.advisorAdapterRegistry.wrap(next);
		}
		return advisors;
	}

	@SuppressWarnings("unchecked")
	protected Advisor[] buildAdvisors(String beanName, Object[] specificInterceptors) {
		// Handle prototypes correctly...
		Advisor[] commonInterceptors = resolveInterceptorNames();

		List allInterceptors = new ArrayList();
		if (specificInterceptors != null) {
			allInterceptors.addAll(Arrays.asList(specificInterceptors));
			if (commonInterceptors != null) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
				}
				else {
					allInterceptors.addAll(Arrays.asList(commonInterceptors));
				}
			}
		}
		//      if (logger.isDebugEnabled()) {
			//         int nrOfCommonInterceptors = (commonInterceptors != null ? commonInterceptors.length : 0);
		//         int nrOfSpecificInterceptors = (specificInterceptors != null ? specificInterceptors.length : 0);
		//         logger.debug("Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors +
		//               " common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
		//      }

		Advisor[] advisors = new Advisor[allInterceptors.size()];
		for (int i = 0; i < allInterceptors.size(); i++) {
			advisors[i] = this.advisorAdapterRegistry.wrap(allInterceptors.get(i));
		}
		return advisors;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object createProxy(
			Class beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {

		ProxyFactory proxyFactory = new ProxyFactory();
		// Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
		proxyFactory.copyFrom(this);

		if (!shouldProxyTargetClass(beanClass, beanName)) {
			// Must allow for introductions; can't just set interfaces to
			// the target's interfaces only.
			Class[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass);
			for (int i = 0; i < targetInterfaces.length; i++) {
				proxyFactory.addInterface(targetInterfaces[i]);
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		for (int i = 0; i < advisors.length; i++) {
			proxyFactory.addAdvisor(advisors[i]);
		}

		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		return proxyFactory.getProxy(myClassLoader);
	}

}
