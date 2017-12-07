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
package org.jboss.system;

import java.net.MalformedURLException;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

/**
 * An implementation of RMIClassLoaderSpi to workaround the
 * proxy ClassCastException problem in 1.4<p>
 *
 * <b>THIS IS A HACK!</b><p>
 *
 * Sun's implementation uses the caller classloader when
 * unmarshalling proxies. This is effectively jboss.jar since
 * that is where JRMPInvokerProxy lives. On a redeploy the
 * new interfaces are ignored because a proxy is already cached
 * against the classloader.<p>
 *
 * Another redeployment problem is that the getClassAnnotation(String)
 * will end up using the old deployment class loader and this can result
 * in NPEs do the class loader being destroyed.
 *
 * This class ignores Sun's guess at a suitable classloader and
 * uses the thread context classloader instead.<p>
 *
 * It has to exist in the system classloader so I have included it
 * in "system" for inclusion in run.jar<p>
 * 
 * @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85942 $
 */
public class JBossRMIClassLoader
   extends RMIClassLoaderSpi
{
   // Attributes ----------------------------------------------------

   /**
    * The JVM implementation (we delegate most work to it)
    */
   RMIClassLoaderSpi delegate = RMIClassLoader.getDefaultProviderInstance();
   
   // Constructors --------------------------------------------------

   /**
    * Required constructor
    */
   public JBossRMIClassLoader()
   {
   }
   
   // RMIClassLoaderSpi Implementation ------------------------------

   /*
    * Ignore the JVM, use the thread context classloader for proxy caching
    */
   public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader ignored)
      throws MalformedURLException, ClassNotFoundException
   {
      return delegate.loadProxyClass(codebase, interfaces, Thread.currentThread().getContextClassLoader());
   }

   /*
    * Just delegate
    */
   public Class<?> loadClass(String codebase, String name, ClassLoader ignored)
      throws MalformedURLException, ClassNotFoundException
   {
      return delegate.loadClass(codebase, name, Thread.currentThread().getContextClassLoader());
   }

   /*
    * Just delegate
    */
   public ClassLoader getClassLoader(String codebase)
      throws MalformedURLException
   {
      return delegate.getClassLoader(codebase);
   }

   /*
    * Try to delegate an default to the java.rmi.server.codebase on any
    * failure.
    */
   public String getClassAnnotation(Class<?> cl)
   {
      String annotation = null;
      try
      {
         annotation = delegate.getClassAnnotation(cl);
      }
      catch(Throwable t)
      {
         // Try the java.rmi.server.codebase property
         annotation = System.getProperty("java.rmi.server.codebase");
      }
      return annotation;
   }
}
