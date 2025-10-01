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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;
import org.sakaiproject.genericdao.api.CompleteGenericDao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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
   @Deprecated
   @SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject) {
      return findByExample(exampleObject, 0, 0);
   }

   /**
    * @deprecated
    */
   @Deprecated
   @SuppressWarnings("unchecked")
   public List findByExample(Object exampleObject, int firstResult, int maxResults) {
      Class<?> persistentClass = checkClass(exampleObject.getClass());
      return execute(session -> {
         CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
         @SuppressWarnings("unchecked")
         Class<Object> targetClass = (Class<Object>) persistentClass;
         CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(targetClass);
         Root<Object> root = criteriaQuery.from(targetClass);
         criteriaQuery.select(root);

         List<Predicate> predicates = new ArrayList<Predicate>();
         try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(persistentClass).getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
               if (descriptor.getReadMethod() == null || "class".equals(descriptor.getName())) {
                  continue;
               }
               Object value = descriptor.getReadMethod().invoke(exampleObject);
               if (value != null) {
                  predicates.add(criteriaBuilder.equal(root.get(descriptor.getName()), value));
               }
            }
         } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to evaluate example object", e);
         }

         if (!predicates.isEmpty()) {
            criteriaQuery.where(predicates.toArray(new Predicate[0]));
         }

         Query<Object> query = session.createQuery(criteriaQuery);
         if (firstResult > 0) {
            query.setFirstResult(firstResult);
         }
         if (maxResults > 0) {
            query.setMaxResults(maxResults);
         }
         return query.list();
      });
   }

}
