/**
 * $Id$
 * $URL$
 * ResourceFinder.java - blog-wow - May 6, 2008 9:59:05 PM - azeckoski
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Allows for easy resolution of resources in the current {@link ClassLoader},
 * (which may not be the {@link ClassLoader} being used by Spring)<br/>
 * Primarily useful when you need to locate resources while running in a Sakai component
 * (like HBM files or SQL files)<br/>
 * In Sakai, this allows us to find resources in our pack since the Sakai context {@link ClassLoader} is wrong,
 * too bad it is not correct, that would be cool, but it is wrong and it is not cool<br/>
 * Takes a list of paths to resources and turns them into legitimate resources<br/>
 * <xmp><bean class="org.sakaiproject.genericdao.springutil.ResourceFinder" factory-method="getResources">
       <constructor-arg>
           <list>
               <value>org/sakaiproject/blogwow/dao/hbm/BlogWowBlog.hbm.xml</value>
               <value>org/sakaiproject/blogwow/dao/hbm/BlogWowEntry.hbm.xml</value>
               <value>org/sakaiproject/blogwow/dao/hbm/BlogWowComment.hbm.xml</value>
           </list>
       </constructor-arg>
   </bean></xmp>
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public class ResourceFinder {

   private static List<Resource> makeResources(List<String> paths) {
      List<Resource> rs = new ArrayList<Resource>();
      if (paths != null && !paths.isEmpty()) {
         ClassLoader cl = ResourceFinder.class.getClassLoader();
         for (String path : paths) {
            Resource r = new ClassPathResource(path, cl);
            if (r.exists()) {
               rs.add(r);
            }
         }
      }
      return rs;
   }

   /**
    * Resolves a list of paths into resources within the current classloader
    * @param paths a list of paths to resources (org/sakaiproject/mystuff/Thing.xml)
    * @return an array of Spring Resource objects
    */
   public static Resource[] getResources(List<String> paths) {
      List<Resource> l = makeResources(paths);
      return l.toArray(new Resource[l.size()]);
   }

   /**
    * Resolves a list of paths into resources within the current classloader
    * @param paths a list of paths to resources (org/sakaiproject/mystuff/Thing.xml)
    * @return an array of File objects
    */
   public static File[] getFiles(List<String> paths) {
      List<Resource> rs = makeResources(paths);
      File[] files = new File[rs.size()];
      for (int i = 0; i < rs.size(); i++) {
         Resource r = rs.get(i);
         try {
            files[i] = r.getFile();
         } catch (IOException e) {
            throw new RuntimeException("Failed to get file for: " + r.getFilename(), e);
         }
      }
      return files;
   }

   /**
    * Resolves a list of paths into resources within the current classloader
    * @param paths a list of paths to resources (org/sakaiproject/mystuff/Thing.xml)
    * @return an array of InputStreams
    */
   public static InputStream[] getInputStreams(List<String> paths) {
      List<Resource> rs = makeResources(paths);
      InputStream[] streams = new InputStream[rs.size()];
      for (int i = 0; i < rs.size(); i++) {
         Resource r = rs.get(i);
         try {
            streams[i] = r.getInputStream();
         } catch (IOException e) {
            throw new RuntimeException("Failed to get inputstream for: " + r.getFilename(), e);
         }
      }
      return streams;
   }

}
