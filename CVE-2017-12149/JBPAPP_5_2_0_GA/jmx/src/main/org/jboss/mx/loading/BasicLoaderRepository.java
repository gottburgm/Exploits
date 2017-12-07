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
package org.jboss.mx.loading;

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a simple classloader repository for the MBean server. The basic
 * loader repository uses an unordered list of classloaders to try and load
 * the required class. There is no attempt made to resolve conflicts between
 * classes loaded by different classloaders. <p>
 *
 * A thread's context class loader is always searched first. Context class loader
 * is not required to be registered to the repository.
 *
 * @see org.jboss.mx.loading.LoaderRepository
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 */   
public class BasicLoaderRepository 
   extends LoaderRepository
{

   // Public --------------------------------------------------------

   /**
    * Loads a class from the repository. This method attempts to load the class
    * using all the classloader registered to the repository.
    *
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   public Class loadClass(String className) throws ClassNotFoundException
   {
      return loadClassWithout(null, className);
   }

   /**
    * Loads a class from the repository, excluding the given
    * classloader.
    *
    * @param skipLoader the classloader to exclude
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   public Class loadClassWithout(ClassLoader skipLoader, String className) throws ClassNotFoundException
   {
      // try ctx cl first
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      if (ctxLoader != skipLoader)
      {
         try
         {
            return ctxLoader.loadClass(className);
         }
         catch (ClassNotFoundException e)
         {
            // ignore and move on to the loader list
         }
      }

      Iterator it = loaders.iterator();
      while (it.hasNext())
      {
         ClassLoader cl = (ClassLoader) it.next();
         if (cl != skipLoader)
         {
            try
            {
               return cl.loadClass(className);
            }
            catch (ClassNotFoundException ignored)
            {
               // go on and try the next loader
            }
         }
      }

      // at last try a native
      Class clazz = getNativeClassForName(className);
      if (clazz != null) return clazz;

      throw new ClassNotFoundException(className);
   }

   /**
    * Loads a class from the repository, using the classloaders that were
    * registered before the given classloader.
    *
    * @param   stop      consult all the classloaders registered before this one
    *                    in an attempt to load a class
    * @param   className name of the class to load
    *
    * @return  loaded class instance
    *
    * @throws ClassNotFoundException if none of the consulted classloaders were
    *         able to load the requested class
    */
   public Class loadClassBefore(ClassLoader stop, String className) throws ClassNotFoundException
   {
      Iterator it = loaders.iterator();
      while (it.hasNext())
      {
         ClassLoader cl = (ClassLoader) it.next();
         if (cl == stop)
            break;

         try
         {
            return cl.loadClass(className);
         }
         catch (ClassNotFoundException ignored)
         {
            // go on and try the next loader
         }
      }

      // at last try a native
      Class clazz = getNativeClassForName(className);
      if (clazz != null) return clazz;

      throw new ClassNotFoundException(className);
   }

   public void addClassLoader(ClassLoader cl)
   {
      loaders.add(cl);
   }

   public boolean addClassLoaderURL(ClassLoader cl, URL url)
   {
      // This is a noop here
      return false;
   }

   public void removeClassLoader(ClassLoader cl)
   {
      loaders.remove(cl);
   }

   public RepositoryClassLoader newClassLoader(final URL url, boolean addToRepository)
      throws Exception
   {
      UnifiedClassLoader ucl = new UnifiedClassLoader(url);
      if( addToRepository )
         this.addClassLoader(ucl);
      return ucl;
   }
   public RepositoryClassLoader newClassLoader(final URL url, final URL origURL, boolean addToRepository)
      throws Exception
   {
      UnifiedClassLoader ucl = new UnifiedClassLoader(url, origURL);
      if( addToRepository )
         this.addClassLoader(ucl);
      return ucl;
   }
   public Class loadClass(String name, boolean resolve, ClassLoader cl)
      throws ClassNotFoundException
   {
      throw new ClassNotFoundException("loadClass(String,boolean,ClassLoader) not supported");
   }
   public URL getResource(String name, ClassLoader cl)
   {
      URL res = null;
      if( cl instanceof UnifiedClassLoader )
      {
         UnifiedClassLoader ucl = (UnifiedClassLoader) cl;
         res = ucl.getResourceLocally(name);
      }
      else
      {
         res = cl.getResource(name);
      }
      return res;
   }
   public void getResources(String name, ClassLoader cl, List urls)
   {
      Enumeration resURLs = null;
      try
      {
         if( cl instanceof UnifiedClassLoader )
         {
            UnifiedClassLoader ucl = (UnifiedClassLoader) cl;
            resURLs = ucl.findResourcesLocally(name);
         }
         else
         {
            resURLs = cl.getResources(name);
         }
         while( resURLs.hasMoreElements() )
            urls.add(resURLs.nextElement());
      }
      catch(Exception e)
      {
      }
   }
}
