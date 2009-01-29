/**
 * $Id$
 * $URL$
 * BasicDataInterceptor.java - genericdao - May 16, 2008 8:06:23 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.genericdao.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.genericdao.api.interceptors.ReadInterceptor;
import org.sakaiproject.genericdao.api.interceptors.WriteInterceptor;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * An example DAO interceptor, mostly for testing purposes but is an ok example of what one might look like,
 * this intercepts DAO operations on {@link GenericTestObject}
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class BasicDataInterceptor implements ReadInterceptor, WriteInterceptor {
   public String lastIntercept;
   public String lastOperation;
   public Serializable[] lastIds;
   public Search lastSearch;
   public Object[] lastEntities;
   public int lastChanges;

   protected List<Intercept> intercepts = new Vector<Intercept>();
   /**
    * @return the list of all intercepts made by this interceptor
    */
   public List<Intercept> getIntercepts() {
      return new ArrayList<Intercept>(intercepts);
   }
   /**
    * @return the most recent intercept or null if there are none
    */
   public Intercept getLastIntercept() {
      return intercepts.size() > 0 ? intercepts.get(intercepts.size()-1) : null;
   }
   /**
    * reset the list of intercepts
    */
   public void reset() {
      intercepts.clear();
      lastIntercept = null;
      lastOperation = null;
      lastIds = null;
      lastSearch = null;
      lastEntities = null;
      lastChanges = 0;
   }

   private void setLasts(String method, String operation, Serializable[] ids, Search search,
         Object[] entities, int changes) {
      this.lastIntercept = method;
      this.lastOperation = operation;
      this.lastIds = ids;
      this.lastSearch = search;
      this.lastEntities = entities;
      this.lastChanges = changes;
      intercepts.add( new Intercept(method, operation, ids, search, entities, changes) );
   }

   public Class<?> interceptType() {
      return GenericTestObject.class;
   }

   public void afterRead(String operation, Serializable[] ids, Search search, Object[] entities) {
      setLasts("afterRead", operation, ids, search, entities, 0);
   }

   public void beforeRead(String operation, Serializable[] ids, Search search) {
      setLasts("beforeRead", operation, ids, search, null, 0);
   }

   public void afterWrite(String operation, Serializable[] ids, Object[] entities, int changes) {
      setLasts("afterWrite", operation, ids, null, entities, changes);
   }

   public void beforeWrite(String operation, Serializable[] ids, Object[] entities) {
      setLasts("beforeWrite", operation, ids, null, entities, 0);
   }

   public class Intercept {
      public String intercept;
      public String operation;
      public Serializable[] ids;
      public Search search;
      public Object[] entities;
      public int changes;
      public Intercept(String intercept, String operation, Serializable[] ids, Search search,
            Object[] entities, int changes) {
         this.intercept = intercept;
         this.operation = operation;
         this.ids = ids;
         this.search = search;
         this.entities = entities;
         this.changes = changes;
      }
      @Override
      public String toString() {
         return "intercept=" + intercept + ",operation=" + operation + ",ids=" + ids + ",search=" + search + ",changes=" + changes;
      }
   }

}
