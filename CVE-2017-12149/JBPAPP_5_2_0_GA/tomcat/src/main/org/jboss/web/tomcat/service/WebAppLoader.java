/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.web.tomcat.service;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;
import org.jboss.logging.Logger;

/**
 * Override the tomcat WebappLoader to set the default class loader to the
 * WebAppClassLoader and pass the filtered packages to the WebAppClassLoader.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class WebAppLoader extends WebappLoader
{
   private static final Logger log = Logger.getLogger(WebAppLoader.class);

   private String[] filteredPackages = {
           "org.apache.commons.logging"
   };
   private List<URL> classpath;

   private TomcatInjectionContainer injectionContainer;

   public WebAppLoader()
   {
      super();
      setLoaderClass(WebAppClassLoader.class.getName());
   }

   public WebAppLoader(ClassLoader parent, Set<String> filteredPackages)
   {
      this(parent, filteredPackages, null);
   }
   public WebAppLoader(ClassLoader parent, Set<String> filteredPackages, TomcatInjectionContainer container)
   {
      super(parent);
      setLoaderClass(WebAppClassLoader.class.getName());
      if(filteredPackages != null)
      {
         this.filteredPackages = new String[filteredPackages.size()];
         filteredPackages.toArray(this.filteredPackages);
      }
      injectionContainer = container;
   }
   public WebAppLoader(ClassLoader parent, String[] filteredPackages, TomcatInjectionContainer container)
   {
      super(parent);
      setLoaderClass(WebAppClassLoader.class.getName());
      this.filteredPackages = filteredPackages;
      injectionContainer = container;
   }

   /**
    * Use an explicit classpath
    * 
    * @param classpath
    */
   public void setClasspath(List<URL> classpath)
   {
      this.classpath = classpath;
   }

   /**
    * Override to apply the filteredPackages to the jboss WebAppClassLoader
    *
    * @throws LifecycleException
    */
   public void start() throws LifecycleException
   {
      super.start();
      ClassLoader loader = getClassLoader();
      if (loader instanceof WebAppClassLoader)
      {
         WebAppClassLoader webLoader = (WebAppClassLoader) loader;
         webLoader.setFilteredPackages(filteredPackages);
         if( classpath != null )
         {
            for(URL url : classpath)
            {
               webLoader.addURL(url);
            }
         }
      }
      if (injectionContainer != null)
      {
         log.debug("injectionContainer enabled and processing beginning with Tomcat WebAppLoader");
         // we need to do this because the classloader is initialize by the web container and
         // the injection container needs the classloader so that it can build up Injectors and ENC populators
         injectionContainer.setClassLoader(getClassLoader());
         injectionContainer.processMetadata();
      }
   }
}
