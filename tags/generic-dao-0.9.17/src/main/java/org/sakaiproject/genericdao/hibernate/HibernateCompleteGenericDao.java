/******************************************************************************
 * HibernateCompleteDao.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2006 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.genericdao.hibernate;

import java.util.List;

import org.sakaiproject.genericdao.api.CompleteGenericDao;

/**
 * A Hibernate (http://hibernate.org/) based implementation of CompleteGenericDao
 * which can be extended to add more specialized DAO methods.
 * <p>
 * See the overview for installation/usage tips.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class HibernateCompleteGenericDao extends HibernateGeneralGenericDao implements CompleteGenericDao {

   /**
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject) {
      return findByExample(exampleObject, 0, 0);
   }

   /**
    * @deprecated
    */
   @SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject, int firstResult, int maxResults) {
      checkClass(exampleObject.getClass());
      List items = getHibernateTemplate().findByExample(exampleObject, firstResult, maxResults);
      return items;
   }

}
