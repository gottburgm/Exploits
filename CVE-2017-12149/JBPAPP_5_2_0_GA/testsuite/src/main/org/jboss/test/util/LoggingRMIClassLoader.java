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
package org.jboss.test.util;

import java.net.MalformedURLException;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.Arrays;
import java.util.Collection;

import org.jboss.logging.Logger;

/**
 * Logs RMI classloading activity
 * 
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class LoggingRMIClassLoader
   extends RMIClassLoaderSpi
{
   private static final Logger log = Logger.getLogger(LoggingRMIClassLoader.class);
   
   // Attributes ----------------------------------------------------
   
   /**
    * The JVM implementation (we delegate most work to it)
    */
   RMIClassLoaderSpi delegate = RMIClassLoader.getDefaultProviderInstance();
   
   // Constructors --------------------------------------------------

   /**
    * Required constructor
    */
   public LoggingRMIClassLoader()
   {
   }
   
   // RMIClassLoaderSpi Implementation ------------------------------

   public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader cl)
      throws MalformedURLException, ClassNotFoundException
   {
      Collection c = null;
      try
      {
         if (interfaces != null)
            c = Arrays.asList(interfaces);
         Class result = delegate.loadProxyClass(codebase, interfaces, cl);
         log.debug("loadClass: codebase=" + codebase + " interfaces=" + c + " cl=" + cl + " result=" + result);
         return result;
      }
      catch (MalformedURLException e)
      {
         log.debug("loadClass: codebase=" + codebase + " interfaces=" + c + " cl=" + cl, e);
         throw e;
      }
      catch (ClassNotFoundException e)
      {
         log.debug("loadClass: codebase=" + codebase + " interfaces=" + c + " cl=" + cl, e);
         throw e;
      }
   }

   public Class loadClass(String codebase, String name, ClassLoader cl)
      throws MalformedURLException, ClassNotFoundException
   {
      try
      {
         Class result = delegate.loadClass(codebase, name, cl);
         log.debug("loadClass: codebase=" + codebase + " name=" + name + " cl=" + cl + " result=" + result);
         return result;
      }
      catch (MalformedURLException e)
      {
         log.debug("loadClass: codebase=" + codebase + " name=" + name + " cl=" + cl, e);
         throw e;
      }
      catch (ClassNotFoundException e)
      {
         log.debug("loadClass: codebase=" + codebase + " name=" + name + " cl=" + cl, e);
         throw e;
      }
   }

   public ClassLoader getClassLoader(String codebase)
      throws MalformedURLException
   {
      try
      {
         ClassLoader result = delegate.getClassLoader(codebase);
         log.debug("getClassLoader: codebase=" + codebase + " result=" + result);
         return result;
      }
      catch (MalformedURLException e)
      {
         log.debug("getClassLoader: codebase=" + codebase, e);
         throw e;
      }
   }

   public String getClassAnnotation(Class clazz)
   {
      String result = delegate.getClassAnnotation(clazz);
      log.debug("getClassAnnotation: class=" + clazz + " result=" + result);
      return result;
   }
}
