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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.mx.loading.LoadMgr3.PkgClassLoader;
import org.jboss.mx.util.ObjectNameFactory;

/** A simple extension of UnifiedLoaderRepository3 that adds the notion of a
 * parent UnifiedLoaderRepository. Classes and resources are loaded from child
 * first and then the parent depending on the java2ParentDelegation flag.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public class HeirarchicalLoaderRepository3 extends UnifiedLoaderRepository3
{
   private static ObjectName DEFAULT_LOADER_OBJECT_NAME = ObjectNameFactory.create(DEFAULT_LOADER_NAME);

   /** A ClassLoader override that prevents a child class loader from looking
    * beyond its URLs for classes.
    */
   static class NoParentClassLoader extends ClassLoader
   {
      NoParentClassLoader()
      {
         super(HeirarchicalLoaderRepository3.class.getClassLoader());
      }

      /** Override to always return null to force the UCL to only load from
       * its URLs.
       * @param name
       * @return
       */
      public URL getResource(String name)
      {
         return null;
      }
      /** Override to always throw a CNFE to force the UCL to only load from
       * its URLs.
       * @param name
       * @param resolve
       * @return nothing
       * @throws ClassNotFoundException always
       */ 
      protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException
      {
         throw new ClassNotFoundException("NoParentClassLoader has no classes");
      }
      /** Override to always throw a CNFE to force the UCL to only load from
       * its URLs.
       * @param name
       * @return
       * @throws ClassNotFoundException
       */ 
      protected Class findClass(String name) throws ClassNotFoundException
      {
         throw new ClassNotFoundException("NoParentClassLoader has no classes");
      }
   }
   static class CacheClassLoader extends UnifiedClassLoader3
   {
      Class cacheClass;
      CacheClassLoader(Class cacheClass, LoaderRepository rep)
      {
         super(null, null, new NoParentClassLoader(), rep);
         this.cacheClass = cacheClass;
      }

      protected Class findClass(String name) throws ClassNotFoundException
      {
         Class c = cacheClass;
         if( name.equals(cacheClass.getName()) == false )
            c = null;
         return c;
      }
   }

   /** The repository to which we delegate if requested classes or resources
    are not available from this repository.
    */
   private UnifiedLoaderRepository3 parentRepository;
   /** A flag indicating if the standard parent delegation loading where the
    parent repository is used before this repository.
    */
   private boolean java2ParentDelegation;

   /** The package classloader */
   private PkgClassLoader packageClassLoader;

   /** Create a HeirarchicalLoaderRepository3 with an explicit parent.
    * 
    * @param parent
    * @throws AttributeNotFoundException
    * @throws InstanceNotFoundException
    * @throws MBeanException
    * @throws ReflectionException
    */ 
   public HeirarchicalLoaderRepository3(UnifiedLoaderRepository3 parent)
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
   {
      this.parentRepository = parent;
      init();
   }
   /** Create a HeirarchicalLoaderRepository3 with a parent obtained by querying
    * the server for the ServerConstants.DEFAULT_LOADER_NAME mbean.
    * 
    * @param server
    * @throws AttributeNotFoundException
    * @throws InstanceNotFoundException
    * @throws MBeanException
    * @throws ReflectionException
    */ 
   public HeirarchicalLoaderRepository3(MBeanServer server)
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
   {
      this(server, DEFAULT_LOADER_OBJECT_NAME);
   }
   /** Create a HeirarchicalLoaderRepository3 with a parent obtained by querying
    * the server for the parentName mbean.
    * 
    * @param server
    * @param parentName
    * @throws AttributeNotFoundException
    * @throws InstanceNotFoundException
    * @throws MBeanException
    * @throws ReflectionException
    */ 
   public HeirarchicalLoaderRepository3(MBeanServer server, ObjectName parentName)
      throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
   {
      this.parentRepository = (UnifiedLoaderRepository3) server.getAttribute(parentName,
                    "Instance");
      init();
   }

   /**
    * Initialisation
    */
   private void init()
   {
      // Include a class loader with a parent to the system class loader
      ClassLoader loader = RepositoryClassLoader.class.getClassLoader();
      RepositoryClassLoader ucl = null;
      if( loader instanceof RepositoryClassLoader )
         ucl = (RepositoryClassLoader) loader;
      else
         ucl = new UnifiedClassLoader3(null, null, HeirarchicalLoaderRepository3.this);
      packageClassLoader = new PkgClassLoader(ucl, 3);
   }

   // Public --------------------------------------------------------

   public RepositoryClassLoader newClassLoader(final URL url, boolean addToRepository)
      throws Exception
   {
      UnifiedClassLoader3 ucl = null;
      if( java2ParentDelegation == false )
         ucl = new UnifiedClassLoader3(url, null, new NoParentClassLoader(), this);
      else
         ucl = new UnifiedClassLoader3(url, null, this);

      if( addToRepository )
      {
         this.addClassLoader(ucl);
      }
      return ucl;
   }
   public RepositoryClassLoader newClassLoader(final URL url, final URL origURL, boolean addToRepository)
      throws Exception
   {
      UnifiedClassLoader3 ucl = null;
      if( java2ParentDelegation == false )
         ucl = new UnifiedClassLoader3(url, origURL, new NoParentClassLoader(), this);
      else
         ucl = new UnifiedClassLoader3(url, origURL, this);

      if( addToRepository )
      {
         this.addClassLoader(ucl);
      }
      return ucl;
   }

   /** Get the use parent first flag. This indicates whether the parent
    * repository is consulted first for resource and class loading or if the
    * HeirchicalLoaderRepository is consulted first.
    *
    * @return true if the parent repository is consulted first, false if the
    * HeirchicalLoaderRepository is consulted first.
    */
   public boolean getUseParentFirst()
   {
      return java2ParentDelegation;
   }
   /** Set the use parent first flag. This indicates whether the parent
    * repository is consulted first for resource and class loading or if the
    * HeirchicalLoaderRepository is consulted first.
    *
    * @param flag true if the parent repository is consulted first, false if the
    * HeirchicalLoaderRepository is consulted first.
    */
   public void setUseParentFirst(boolean flag)
   {
      java2ParentDelegation = flag;
   }

   /** Load a class using the repository class loaders.
    *
    * @param name The name of the class
    * @param resolve an obsolete unused parameter from ClassLoader.loadClass
    * @param scl The asking class loader
    * @return The loaded class
    * @throws ClassNotFoundException If the class could not be found.
    */
   public Class loadClass(String name, boolean resolve, ClassLoader scl)
      throws ClassNotFoundException
   {
      Class foundClass = null;

      if( java2ParentDelegation == true )
      {
         try
         {
            // Try the parent repository first
            foundClass = parentRepository.loadClass(name, resolve, scl);
         }
         catch(ClassNotFoundException e)
         {
            // Next try our repository
            if( foundClass == null )
               foundClass = super.loadClass(name, resolve, scl);
         }
      }
      else
      {
         try
         {
            // Try this repository first
            foundClass = super.loadClass(name, resolve, scl);
         }
         catch(ClassNotFoundException e)
         {
            // Next try our parent repository
            if( foundClass == null )
               foundClass = parentRepository.loadClass(name, resolve, scl);
         }
      }

      if( foundClass != null )
         return foundClass;

      /* If we reach here, all of the classloaders currently in the VM don't
         know about the class
      */
      throw new ClassNotFoundException(name);
   }

   /** Override getCachedClass to return the parent repository cached class
    * if java2ParentDelegation=true, followed by this repository's cached
    * value. Else, if java2ParentDelegation=false, only check this repository's
    * cache to attempt to load the class from the child repository before
    * going to the parent cache.
    * 
    * @param classname
    * @return the cached class if found, null otherwise
    */ 
   public Class getCachedClass(String classname)
   {
      Class clazz = null;
      if( java2ParentDelegation == true )
      {
         // Try the parent repository
         clazz = parentRepository.getCachedClass(classname);
         // Next try our parent repository
         if( clazz == null )
            clazz = super.getCachedClass(classname);
      }
      else
      {
         // Try this repository
         clazz = super.getCachedClass(classname);
      }
      return clazz;
   }

   /** Find a resource from this repository. This first looks to this
    * repository and then the parent repository.
    * @param name The name of the resource
    * @param scl The asking class loader
    * @return An URL for reading the resource, or <code>null</code> if the
    *          resource could not be found.
    */
   public URL getResource(String name, ClassLoader scl)
   {
      URL resource = null;

      if( java2ParentDelegation == true )
      {
         /* Try our parent repository. This cannot use the getResource method
         because we do not want the parent repository to load the resource via
         our scoped class loader
         */
         resource = getParentResource(name, scl);
         // Next try this repository
         if( resource == null )
            resource = super.getResource(name, scl);
      }
      else
      {
         // Try this repository
         resource = super.getResource(name, scl);
         // Next try our parent repository
         if( resource == null )
         {
            /* Try our parent repository. This cannot use the getResource method
            because we do not want the parent repository to load the resource via
            our scoped class loader
            */
            resource = getParentResource(name, scl);
         }
      }

      return resource;
   }

   /** Find all resource URLs for the given name. This is entails an
    * exhuastive search of this and the parent repository and is an expensive
    * operation.
    *
    * @param name the resource name
    * @param cl the requesting class loader
    * @param urls a list into which the located resource URLs will be placed
    */
   public void getResources(String name, ClassLoader cl, List urls)
   {
      if( java2ParentDelegation == true )
      {
         // Get the parent repository resources
         parentRepository.getResources(name, cl, urls);
         // Next get this repositories resources
         super.getResources(name, cl, urls);
      }
      else
      {
         // Get this repositories resources
         super.getResources(name, cl, urls);
         // Next get the parent repository resources
         parentRepository.getResources(name, cl, urls);
      }
   }

   /** Obtain a listing of the URLs for all UnifiedClassLoaders associated with
    *the repository
    */
   public URL[] getURLs()
   {
      URL[] ourURLs = super.getURLs();
      URL[] parentURLs = parentRepository.getURLs();
      int size = ourURLs.length + parentURLs.length;
      URL[] urls = new URL[size];
      System.arraycopy(ourURLs, 0, urls, 0, ourURLs.length);
      System.arraycopy(parentURLs, 0, urls, ourURLs.length, parentURLs.length);
      return urls;
   }

   /** Called by LoadMgr to locate a previously loaded class. This looks
    * first to this repository and then the parent repository.
    *@return the cached class if found, null otherwise
    */
   public Class loadClassFromCache(String name)
   {
      Class foundClass = null;

      if( java2ParentDelegation == true )
      {
         // Try this repository
         foundClass = parentRepository.loadClassFromCache(name);
         // Next try our parent repository
         if( foundClass == null )
            foundClass = super.loadClassFromCache(name);
      }
      else
      {
         // Try this repository
         foundClass = super.loadClassFromCache(name);
         /* We do not try the parent repository cache as this does not allow
         the child repository to override classes in the parent
         */
      }
      return foundClass;
   }

   /** Called by LoadMgr to obtain all class loaders. This returns a set of
    * PkgClassLoader with the HeirarchicalLoaderRepository3 ordered ahead of
    * the parent repository pkg class loaders
    *@return Set<PkgClassLoader>
    */
   public Set getPackageClassLoaders(String name)
   {
      Set pkgSet = super.getPackageClassLoaders(name);
      Set parentPkgSet = parentRepository.getPackageClassLoaders(name);
      GetClassLoadersAction action = new GetClassLoadersAction(name, pkgSet,
         parentPkgSet);
      Set theSet = (Set) AccessController.doPrivileged(action);
      return theSet;

   }

   public int compare(LoaderRepository lr)
   {
      if (lr == this)
         return 0;
      return reverseCompare(lr);
   }
   
   protected int reverseCompare(LoaderRepository lr)
   {
      // If it is not our parent we don't care
      if (lr != parentRepository)
         return 0;
      
      // The order depends upon the delegation model
      if (java2ParentDelegation)
         return +1;
      else
         return -1;
   }

   /** A subset of the functionality found in getResource(String, ClassLoader),
    * but this version queries the parentRepository and does not use the scl
    * to avoid leaking class loaders across scoped.
    * 
    * @param name - the resource name
    * @param scl - the requesting class loader
    * @return the resource URL if found, null otherwise
    */
   private URL getParentResource(String name, ClassLoader scl)
   {
      // Not found in classloader, ask the global cache
      URL resource = parentRepository.getResourceFromGlobalCache(name);

      // The cache has it, we are done
      if( resource != null )
         return resource;

      // Not visible in global cache, iterate on all classloaders
      resource = parentRepository.getResourceFromRepository(name, scl);

      return resource;
   }

   private class GetClassLoadersAction implements PrivilegedAction
   {
      private String name;
      Set pkgSet;
      Set parentPkgSet;

      GetClassLoadersAction(String name, Set pkgSet, Set parentPkgSet)
      {
         this.name = name;
         this.pkgSet = pkgSet;
         this.parentPkgSet = parentPkgSet;
      }

      public Object run()
      {
         // Build a set of PkgClassLoader
         Set theSet = ClassLoaderUtils.newPackageSet();
         if( pkgSet != null )
         {
            Iterator iter = pkgSet.iterator();
            while( iter.hasNext() )
            {
               RepositoryClassLoader ucl = (RepositoryClassLoader) iter.next();
               PkgClassLoader pkgUcl = new PkgClassLoader(ucl, 0);
               theSet.add(pkgUcl);
            }
         }

         if( java2ParentDelegation == false )
         {
            Class cacheClass = parentRepository.loadClassFromCache(name);
            if( cacheClass != null )
            {
               RepositoryClassLoader ucl = new CacheClassLoader(cacheClass, HeirarchicalLoaderRepository3.this);
               PkgClassLoader pkgUcl = new PkgClassLoader(ucl, 1);
               theSet.add(pkgUcl);
            }
         }

         if( parentPkgSet != null )
         {
            Iterator iter = parentPkgSet.iterator();
            while( iter.hasNext() )
            {
               RepositoryClassLoader ucl = (RepositoryClassLoader) iter.next();
               PkgClassLoader pkgUcl = new PkgClassLoader(ucl, 2);
               theSet.add(pkgUcl);
            }
         }

         if( java2ParentDelegation == false )
         {
            theSet.add(packageClassLoader);
         }

         return theSet;
      }
   }

}
