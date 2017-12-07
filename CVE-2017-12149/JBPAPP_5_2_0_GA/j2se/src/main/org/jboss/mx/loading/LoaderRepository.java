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

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.jboss.util.loading.Translator;

import javax.management.loading.ClassLoaderRepository;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Abstract base class of all loader repository implementations
 * 
 * @see org.jboss.mx.loading.BasicLoaderRepository
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 */
public abstract class LoaderRepository
   implements ServerConstants, ClassLoaderRepository
{

   // Attributes ----------------------------------------------------
   protected static Vector loaders = new Vector();
   protected static LoaderRepository instance = null;
   protected Translator translator = null;

   /** The loaded classes cache, HashMap<String, Class>.
    * Access synchronized via this.classes monitor.
    */
   private ConcurrentReaderHashMap classes = new ConcurrentReaderHashMap();


   /**
    * Native signature to class map
    */
   private static HashMap nativeClassBySignature;

   // Static --------------------------------------------------------
   private static final Logger log = Logger.getLogger(LoaderRepository.class);

   /**
    * Construct the native class map
    */
   static
   {
      nativeClassBySignature = new HashMap();
      nativeClassBySignature.put("boolean", boolean.class);
      nativeClassBySignature.put("byte", byte.class);
      nativeClassBySignature.put("char", char.class);
      nativeClassBySignature.put("double", double.class);
      nativeClassBySignature.put("float", float.class);
      nativeClassBySignature.put("int", int.class);
      nativeClassBySignature.put("long", long.class);
      nativeClassBySignature.put("short", short.class);
      nativeClassBySignature.put("void", void.class);

      nativeClassBySignature.put("boolean[]", boolean[].class);
      nativeClassBySignature.put("byte[]", byte[].class);
      nativeClassBySignature.put("char[]", char[].class);
      nativeClassBySignature.put("double[]", double[].class);
      nativeClassBySignature.put("float[]", float[].class);
      nativeClassBySignature.put("int[]", int[].class);
      nativeClassBySignature.put("long[]", long[].class);
      nativeClassBySignature.put("short[]", short[].class);
   }

   // Public --------------------------------------------------------
   public Vector getLoaders()
   {
      return loaders;
   }

   public URL[] getURLs()
   {
      return null;
   }

   public Class getCachedClass(String classname)
   {
       return (Class)classes.get(classname);
   }

   public Translator getTranslator()
   {
      return translator;
   }

   public void setTranslator(Translator t)
   {
      translator = t;
   }

   /**
    * Compare two loader repository, by default we do no special ordering
    * 
    * @param lr the loader repository
    * @return -1, 0, 1 depending upon the order
    */
   public int compare(LoaderRepository lr)
   {
      if (lr == this)
         return 0;
      else
         return -lr.reverseCompare(this);
   }
   
   // BEGIN ClassLoaderRepository **************************************************************************************

   /**
    * Loads a class from the repository. This method attempts to load the class
    * using all the classloader registered to the repository.
    *
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   public abstract Class loadClass(String className) throws ClassNotFoundException;

   /**
    * Loads a class from the repository, excluding the given
    * classloader.
    *
    * @param loader the classloader to exclude
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   public abstract Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException;

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
   public abstract Class loadClassBefore(ClassLoader stop, String className) throws ClassNotFoundException;

   // END ClassLoaderRepository ****************************************************************************************

   /** Create RepositoryClassLoader and optionally add it to the repository
    * @param url the URL to use for class loading
    * @param addToRepository a flag indicating if the CL should be added to
    *    the repository
    * @return the UCL instance
    * @throws Exception
    */
   public abstract RepositoryClassLoader newClassLoader(final URL url, boolean addToRepository)
      throws Exception;
   /** Create RepositoryClassLoader and optionally add it to the repository
    * @param url the URL to use for class loading
    * @param origURL an orignal URL to use as the URL for the CL CodeSource.
    * This is useful when the url is a local copy that is difficult to use for
    * security policy writing.
    * @param addToRepository a flag indicating if the CL should be added to
    *    the repository
    * @return the CL instance
    * @throws Exception
    */
   public abstract RepositoryClassLoader newClassLoader(final URL url, final URL origURL,
      boolean addToRepository)
      throws Exception;

   /** Load the given class from the repository
    * @param name
    * @param resolve
    * @param cl
    * @return
    * @throws ClassNotFoundException
    */
   public abstract Class loadClass(String name, boolean resolve, ClassLoader cl)
      throws ClassNotFoundException;

   /** Find a resource URL for the given name
    *
    * @param name the resource name
    * @param cl the requesting class loader
    * @return The resource URL if found, null otherwise
    */
   public abstract URL getResource(String name, ClassLoader cl);
   /** Find all resource URLs for the given name. Since this typically
    * entails an exhuastive search of the repository it can be a relatively
    * slow operation.
    *
    * @param name the resource name
    * @param cl the requesting class loader
    * @param urls a list into which the located resource URLs will be placed
    */
   public abstract void getResources(String name, ClassLoader cl, List urls);

   /** Add a class loader to the repository
    */
   public abstract void addClassLoader(ClassLoader cl);
   /** Update the set of URLs known to be associated with a previously added
    * class loader.
    *
    * @param cl
    * @param url
    */
   public abstract boolean addClassLoaderURL(ClassLoader cl, URL url);
   /** Remove a cladd loader from the repository.
    * @param cl
    */
   public abstract void removeClassLoader(ClassLoader cl);

   /**
    * Return the class of a java native type
    * @return the class, or null if className is not a native class name
    */
   public static final Class getNativeClassForName(String className)
   {
      // Check for native classes
      return (Class)nativeClassBySignature.get(className);
   }

   /**
    * Allow subclasses to override the ordering
    * 
    * @param lr the loader repository
    * @return -1, 0, 1 depending upon the order
    */
   protected int reverseCompare(LoaderRepository lr)
   {
      return 0;
   }

   /**
    * Add a class to the the cache
    */
   void cacheLoadedClass(String name, Class cls, ClassLoader cl)
   {
       synchronized( classes )
       {
          // Update the global cache
          classes.put(name, cls);
          if( log.isTraceEnabled() )
          {
             log.trace("cacheLoadedClass, classname: "+name+", class: "+cls
                +", cl: "+cl);
          }

           /**
            * TODO: Adding this implementation is a hack for jmx 1.2 checkin.
            * Had to add this because need getCachedClass() to work.
            * However, this method does not add loaded classes to collection
            * to be unloaded when called to remove classloader.
            * Hopefully this will be a short term workaround.
            * Contact telrod@e2technologies.net if you have questions. -TME
            */
       }
   }

   void clear()
   {
      classes.clear();
   }
}
