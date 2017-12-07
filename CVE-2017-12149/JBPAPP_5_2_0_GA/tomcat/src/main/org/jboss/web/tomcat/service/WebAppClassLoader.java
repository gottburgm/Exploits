/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import org.apache.catalina.loader.WebappClassLoader;
import org.jboss.logging.Logger;
import org.jboss.proxy.compiler.IIOPStubCompiler;

/**
 * Subclass the tomcat web app class loader to override the filter method to
 * exclude classes which cannot be override by the web app due to their use in
 * the tomcat web container/integration.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class WebAppClassLoader extends WebappClassLoader
{
   static Logger log = Logger.getLogger(WebAppClassLoader.class);

   private String[] filteredPackages =
   {"org.apache.commons.logging"};

   public WebAppClassLoader()
   {
   }

   public WebAppClassLoader(ClassLoader parent)
   {
      super(parent);
   }

   public String[] getFilteredPackages()
   {
      return filteredPackages;
   }

   public void setFilteredPackages(String[] pkgs)
   {
      this.filteredPackages = pkgs;
   }

   /*
    * (non-Javadoc)
    * @see org.apache.catalina.loader.WebappClassLoader#addURL(java.net.URL)
    */
   @Override
   public void addURL(URL url)
   {
      super.addURL(url);
   }

   @Override 
   public URL[] getURLs()
   {
      return new URL[0]; // Optimize away the Memory Allocation
   }

   /*
    * (non-Javadoc)
    * @see org.apache.catalina.loader.WebappClassLoader#findClass(java.lang.String)
    */
   @Override
   public Class findClass(String name) throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("findClass(" + name + ") called");

      try
      {
         return super.findClass(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         // try to dynamically generate the class if it is a stub
         int start = name.lastIndexOf('.') + 1;
         if (name.charAt(start) == '_' && name.endsWith("_Stub"))
            return generateStub(name);
         throw cnfe;
      }
   }

   /**
    * <p>
    * Generate iiop stubs dynamically for any name ending in _Stub.
    * </p>
    * 
    * @param name   a <code>String</code> representing the stub fully-qualified class name.
    * @return   the <code>Class</code> of the generated stub.
    * @throws ClassNotFoundException    if the stub class could not be created.
    */
   private Class generateStub(String name) throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      int start = name.lastIndexOf('.') + 1;
      String pkg = name.substring(0, start);
      String interfaceName = pkg + name.substring(start + 1, name.length() - 5);

      // This is a workaround for a problem in the RMI/IIOP
      // stub loading code in SUN JDK 1.4.x, which prepends
      // "org.omg.stub." to classes in certain name spaces,
      // such as "com.sun". This non-compliant behavior
      // results in failures when deploying SUN example code,
      // including ECPerf and PetStore, so we remove the prefix.
      if (interfaceName.startsWith("org.omg.stub.com.sun."))
         interfaceName = interfaceName.substring(13);

      Class intf = super.loadClass(interfaceName);
      if (trace)
         log.trace("loaded class " + interfaceName);

      byte[] code = IIOPStubCompiler.compile(intf, name);
      if (trace)
         log.trace("compiled stub class for " + interfaceName);

      Class clz = super.defineClass(name, code, 0, code.length);
      if (trace)
         log.trace("defined stub class for " + interfaceName);

      super.resolveClass(clz);
      try
      {
         clz.newInstance();
      }
      catch (Throwable t)
      {
         ClassNotFoundException cnfe = new ClassNotFoundException(interfaceName, t);
         throw cnfe;
      }
      if (trace)
         log.trace("resolved stub class for " + interfaceName);
      return clz;
   }

   /**
    * Overriden to filter out classes in the packages listed in the
    * filteredPackages settings.
    * 
    * @param name
    * @return true if the class should be loaded from the parent class loader,
    *         false if it can be loaded from this class loader.
    */
   protected boolean filter(String name)
   {
      boolean excludeClass = super.filter(name);
      if (excludeClass == false)
      {
         // Check class against our filtered packages
         int length = filteredPackages != null ? filteredPackages.length : 0;
         for (int n = 0; n < length; n++)
         {
            String pkg = filteredPackages[n];
            if (name.startsWith(pkg))
            {
               excludeClass = true;
               break;
            }
         }
      }
      log.trace("filter name=" + name + ", exclude=" + excludeClass);
      return excludeClass;
   }
}
