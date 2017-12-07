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
package org.jboss.mx.loading;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.util.Classes;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArraySet;

/** A repository of class loaders that form a flat namespace of classes
 * and resources. This version uses UnifiedClassLoader3 instances. Class
 * and resource loading is synchronized by the acquiring the monitor to the
 * associated repository structure monitor. See the variable javadoc comments
 * for what monitor is used to access a given structure.
 *
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 68584 $
 * just a hint... xdoclet not really used
 * @jmx.name="JMImplementation:service=UnifiedLoaderRepository,name=Default"
 */
public class UnifiedLoaderRepository3 extends LoaderRepository
   implements MBeanRegistration, NotificationBroadcaster,
      UnifiedLoaderRepository3MBean
{
   // Static --------------------------------------------------------
   private static final Logger log = Logger.getLogger(UnifiedLoaderRepository3.class);
   /** Used to provide a relative ordering of UCLs based on the order in
    * which they are added to the repository */
   private static int addedCount;
   /** The jmx notification behavior mode. This is set by the
    * org.jboss.mx.loading.UnifiedLoaderRepository.notifyMode system
    * property.
    */
   // Send notification with the ClassLoader as the user data
   private static final int LEGACY_MODE = 0;
   // Send notification with the ClassLoader as the user data wrapped in a WeakReference
   private static final int WEAK_REFERENCE_MODE = 1;
   // Don't send any notifications
   private static final int NO_NOTIFICATION_MODE = 2;
   private static int NOTIFICATION_MODE;

   // Attributes ----------------------------------------------------

   /** HashSet<UCL> of classloaders in the repository.
    * Access synchronized via this.classLoaders monitor.
    */
   private CopyOnWriteArraySet classLoaders = new CopyOnWriteArraySet();
   /** HashSet<UCL> of class loaders in the repository that have a dynamic
    * URL associated with them. Such a class loader is added to every package
    * class loader set in #getPackageClassLoaders(String).
    */
   private HashSet dynamicClassLoaders = new HashSet();
   /** A HashMap<ClassLoader, UCL> of foreign (non-UCL) classloaders that
    * have been added to the repository as the key and the value the UCL
    * actually used by the ULR.
    * Access synchronized via this.classLoaders monitor.
    */
   private HashMap nonUCLClassLoader = new HashMap();

   /** A HashSet<URL> used to check for duplicate URLs. Previously this was handled
    by the UCL.equals, but this caused problems with Class.forName(String,
    boolean, ClassLoader) caching.
    Access synchronized via this.classLoaders monitor.
    */
   private HashSet classLoaderURLs = new HashSet();

   /** The loaded classes cache, HashMap<String, Class>.
    * Access synchronized via this.classes monitor.
    */
   private ConcurrentReaderHashMap classes = new ConcurrentReaderHashMap();

   /** HashMap<UCL, HashSet<String>> class loaders to the set of class names
    * loaded via the UCL.
    * Access synchronized via this.classes monitor.
    */
   private HashMap loaderToClassesMap = new HashMap();

   /** HashMap<UCL, HashMap<String, URL>> class loaders to the set of
    * resource names they looked up.
    * Access synchronized via this.loaderToResourcesMap monitor.
    */
   private HashMap loaderToResourcesMap = new HashMap();

   /** HashMap<String, ResourceInfo(URL, UCL)> of global resources not unique
    * to a UCL
    * Access synchronized via this.loaderToResourcesMap monitor.
    */
   private HashMap globalResources = new HashMap();

   /** A HashMap<String, Set<UCL>> of package names to the set of
    * ClassLoaders which have classes in the package.
    * Access synchronized via this.packagesMap monitor.
    */
   private ConcurrentReaderHashMap packagesMap = new ConcurrentReaderHashMap();

   /** A HashMap<UCL, String[]> of class loaders to the array of pckages names
    * they serve
    * Access synchronized via this.packagesMap monitor.
    */
   private HashMap<RepositoryClassLoader, List<String>> loaderToPackagesMap = new HashMap<RepositoryClassLoader, List<String>>();

   /**
    * The sequenceNumber used to number notifications.
    */
   private long sequenceNumber = 0;

   /**
    * We delegate our notification sending to a support object.
    */
   private final JBossNotificationBroadcasterSupport broadcaster = new JBossNotificationBroadcasterSupport();

   /**
    * The NotificationInfo we emit.
    */
   private MBeanNotificationInfo[] info;

   static
   {
      // JBAS-4593 notification behavior
      String value = ClassToStringAction.getProperty("org.jboss.mx.loading.UnifiedLoaderRepository.notifyMode", "0");
      NOTIFICATION_MODE = Integer.valueOf(value).intValue();
      switch(NOTIFICATION_MODE)
      {
         case LEGACY_MODE:
         case WEAK_REFERENCE_MODE:
         case NO_NOTIFICATION_MODE:
         break;
         default:
            log.warn("Invalid org.jboss.mx.loading.UnifiedLoaderRepository.notifyMode("
                  +value+"), defaulting to LEGACY_MODE");
            NOTIFICATION_MODE = LEGACY_MODE;
         break;
      }
   }

   // Public --------------------------------------------------------

   public RepositoryClassLoader newClassLoader(final URL url, boolean addToRepository)
           throws Exception
   {
      UnifiedClassLoader3 ucl = new UnifiedClassLoader3(url, null, this);
      if (addToRepository)
         this.registerClassLoader(ucl);
      return ucl;
   }

   public RepositoryClassLoader newClassLoader(final URL url, final URL origURL, boolean addToRepository)
           throws Exception
   {
      UnifiedClassLoader3 ucl = new UnifiedClassLoader3(url, origURL, this);
      if (addToRepository)
         this.registerClassLoader(ucl);
      return ucl;
   }

   public int getCacheSize()
   {
      return classes.size();
   }

   public int getClassLoadersSize()
   {
      return classLoaders.size();
   }

   public void flush()
   {
      synchronized (classes)
      {
         classes.clear();
      }
   }

   public Class getCachedClass(String classname)
   {
      return (Class) classes.get(classname);
   }

   /** Unlike other implementations of LoaderRepository, this method does
    * nothing but ask the UnifiedClassLoader3 to load the class as UCL3s
    * do not use this method.
    */
   public Class loadClass(String name, boolean resolve, ClassLoader cl) throws ClassNotFoundException
   {
      RepositoryClassLoader rcl = getRepositoryClassLoader(cl, name);
      return rcl.loadClass(name, resolve);
   }

   /** Called by LoadMgr to obtain all class loaders for the given className
    * @return Set<UnifiedClassLoader3>, may be null
    */
   public Set getPackageClassLoaders(String className)
   {
      String pkgName = ClassLoaderUtils.getPackageName(className);
      
      // Don't try to load java.* classes, it is impossible
      if (pkgName.startsWith("java."))
         return null;

      Set pkgSet = (Set) packagesMap.get(pkgName);
      if (dynamicClassLoaders.size() > 0)
      {
         Set<RepositoryClassLoader> newSet = ClassLoaderUtils.newPackageSet();
         if(pkgSet != null)
            newSet.addAll(pkgSet);
         pkgSet = newSet;
         pkgSet.addAll(dynamicClassLoaders);
      }
      return pkgSet;
   }

   private String getResourcePackageName(String rsrcName)
   {
      int index = rsrcName.lastIndexOf('/');
      String pkgName = rsrcName;
      if (index > 0)
         pkgName = rsrcName.substring(0, index);
      return pkgName.replace('/', '.');
   }

   /** Lookup a Class from the repository cache.
    * @param name the fully qualified class name
    * @return the cached Class if found, null otherwise
    */
   public Class loadClassFromCache(String name)
   {
      Class cls = null;
      synchronized (classes)
      {
         cls = (Class) classes.get(name);
      }
      return cls;
   }

   /** Add a Class to the repository cache.
    * @param name the fully qualified class name
    * @param cls the Class instance
    * @param cl the repository UCL
    */
   public void cacheLoadedClass(String name, Class cls, ClassLoader cl)
   {
      synchronized (classes)
      {
         // Update the global cache
         Object prevClass = classes.put(name, cls);
         if (log.isTraceEnabled())
         {
            log.trace("cacheLoadedClass, classname: " + name + ", class: " + cls
                    + ", ucl: " + cl + ", prevClass: " + prevClass);
         }

         // Update the cache for this classloader
         // This is used to cycling classloaders
         HashSet loadedClasses = (HashSet) loaderToClassesMap.get(cl);
         if (loadedClasses == null)
         {
            loadedClasses = new HashSet();
            loaderToClassesMap.put(cl, loadedClasses);
         }
         loadedClasses.add(name);
      }
   }

   Class loadClassFromClassLoader(String name, boolean resolve, RepositoryClassLoader cl)
   {
      try
      {
         Class cls = cl.loadClassLocally(name, resolve);
         cacheLoadedClass(name, cls, cl);
         return cls;
      }
      catch (ClassNotFoundException x)
      {
         // The class is not visible by the calling classloader
         if(log.isTraceEnabled())
            log.trace("Failed to load class: "+name, x);
      }
      return null;
   }

   /**
    * Loads a resource following the Unified ClassLoader architecture
    */
   public URL getResource(String name, ClassLoader cl)
   {
      // getResource() calls are not synchronized on the classloader from JDK code.
      // First ask the cache (of the calling classloader)
      URL resource = getResourceFromCache(name, cl);

      // The resource was already loaded by the calling classloader, we're done
      if (resource != null)
         return resource;

      // Not found in cache, ask the calling classloader
      resource = getResourceFromClassLoader(name, cl);

      // The calling classloader sees the resource, we're done
      if (resource != null)
         return resource;

      // Not found in classloader, ask the global cache
      resource = getResourceFromGlobalCache(name);

      // The cache has it, we are done
      if (resource != null)
         return resource;

      // Not visible in global cache, iterate on all classloaders
      resource = getResourceFromRepository(name, cl);

      // Some other classloader sees the resource, we're done
      if (resource != null)
         return resource;

      // This resource is not visible
      return null;
   }

   /** Find all resource URLs for the given name. This is entails an
    * exhuastive search of the repository and is an expensive operation.
    *
    * @param name the resource name
    * @param cl the requesting class loader
    * @param urls a list into which the located resource URLs will be placed
    */
   public void getResources(String name, ClassLoader cl, List urls)
   {
      // Go through all class loaders
      Iterator iter = classLoaders.iterator();
      while (iter.hasNext() == true)
      {
         ClassLoader nextCL = (ClassLoader) iter.next();
         if (nextCL instanceof RepositoryClassLoader)
         {
            RepositoryClassLoader ucl = (RepositoryClassLoader) nextCL;
            try
            {
               Enumeration resURLs = ucl.findResourcesLocally(name);
               while (resURLs.hasMoreElements())
               {
                  Object res = resURLs.nextElement();
                  urls.add(res);
               }
            }
            catch (IOException ignore)
            {
            }
         }
      }
   }

   /** As opposed to classes, resource are not looked up in a global cache,
    * since it is possible that 2 classloaders have the same resource name
    * (ejb-jar.xml), a global cache will overwrite. Instead we look in the
    * classloader's cache that we mantain to cycle the classloaders
    * @param name the resource name
    * @param cl the repository classloader
    * @return the resource URL if found, null otherwise
    */
   private URL getResourceFromCache(String name, ClassLoader cl)
   {
      URL resource = null;
      synchronized (loaderToResourcesMap)
      {
         if (loaderToResourcesMap.containsKey(cl))
         {
            HashMap resources = (HashMap) loaderToResourcesMap.get(cl);
            resource = (URL) resources.get(name);
         }
      }
      return resource;
   }

   private URL getResourceFromClassLoader(String name, ClassLoader cl)
   {
      URL resource = null;
      if (cl instanceof RepositoryClassLoader)
      {
         RepositoryClassLoader ucl = (RepositoryClassLoader) cl;
         resource = ucl.getResourceLocally(name);
         cacheLoadedResource(name, resource, cl);
      }
      return resource;
   }

   /** Check for a resource in the global cache
    * Synchronizes access to globalResources using the loaderToResourcesMap monitor
    * @param name
    * @return
    */
   protected URL getResourceFromGlobalCache(String name)
   {
      ResourceInfo ri = null;
      synchronized (loaderToResourcesMap)
      {
         ri = (ResourceInfo) globalResources.get(name);
      }
      URL resource = null;
      if (ri != null)
         resource = ri.url;
      return resource;
   }

   protected URL getResourceFromRepository(String name, ClassLoader cl)
   {
      // Get the set of class loaders from the packages map
      String pkgName = getResourcePackageName(name);
      Iterator i = null;
      Set pkgSet = (Set) this.packagesMap.get(pkgName);
      if (pkgSet != null)
      {
         i = pkgSet.iterator();
      }
      if (i == null)
      {
         // If no pkg match was found just go through all class loaders
         i = classLoaders.iterator();
      }

      URL url = null;
      while (i.hasNext() == true)
      {
         ClassLoader classloader = (ClassLoader) i.next();
         if (classloader.equals(cl))
         {
            continue;
         }

         if (classloader instanceof RepositoryClassLoader)
         {
            url = ((RepositoryClassLoader) classloader).getResourceLocally(name);
            if (url != null)
            {
               cacheLoadedResource(name, url, classloader);
               cacheGlobalResource(name, url, classloader);
               break;
            }
            else
            {
               // Do nothing, go on with next classloader
            }
         }
      }
      return url;
   }

   /** Update the loaderToResourcesMap
    * @param name the resource name
    * @param url the resource URL
    * @param cl the UCL
    */
   private void cacheLoadedResource(String name, URL url, ClassLoader cl)
   {
      // Update the cache for this classloader only
      // This is used for cycling classloaders
      synchronized (loaderToResourcesMap)
      {
         HashMap resources = (HashMap) loaderToResourcesMap.get(cl);
         if (resources == null)
         {
            resources = new HashMap();
            loaderToResourcesMap.put(cl, resources);
         }
         resources.put(name, url);
      }
   }

   /** Update cache of resources looked up via one UCL, buf found in another UCL
    * @param name the resource name
    * @param url the resource URL
    * @param cl the UCL
    */
   private void cacheGlobalResource(String name, URL url, ClassLoader cl)
   {
      synchronized (loaderToResourcesMap)
      {
         globalResources.put(name, new ResourceInfo(url, cl));
      }
   }

   /** This is a utility method a listing of the URL for all UnifiedClassLoaders
    * associated with the repository. It is never called in response to
    * class or resource loading.
    */
   public URL[] getURLs()
   {
      HashSet classpath = new HashSet();
      Set tmp = classLoaders;
      for (Iterator iter = tmp.iterator(); iter.hasNext();)
      {
         Object obj = iter.next();
         if (obj instanceof RepositoryClassLoader)
         {
            RepositoryClassLoader cl = (RepositoryClassLoader) obj;
            URL[] urls = cl.getClasspath();
            int length = urls != null ? urls.length : 0;
            for (int u = 0; u < length; u++)
            {
               URL path = urls[u];
               classpath.add(path);
            }
         }
      } // for all ClassLoaders

      URL[] cp = new URL[classpath.size()];
      classpath.toArray(cp);
      return cp;
   }

   /** A utility method that iterates over all repository class loaders and
    * display the class information for every UCL that contains the given
    * className
    */
   public String displayClassInfo(String className)
   {
      /* We have to find the class as a resource as we don't want to invoke
      loadClass(name) and cause the side-effect of loading new classes.
      */
      String classRsrcName = className.replace('.', '/') + ".class";

      int count = 0;
      Class loadedClass = this.loadClassFromCache(className);
      StringBuffer results = new StringBuffer(className + " Information\n");
      if (loadedClass != null)
      {
         results.append("Repository cache version:");
         Classes.displayClassInfo(loadedClass, results);
      }
      else
      {
         results.append("Not loaded in repository cache\n");
      }
      Set tmp = classLoaders;
      for (Iterator iter = tmp.iterator(); iter.hasNext();)
      {
         URLClassLoader cl = (URLClassLoader) iter.next();
         URL classURL = cl.findResource(classRsrcName);
         if (classURL != null)
         {
            results.append("\n\n### Instance" + count + " found in UCL: " + cl + "\n");
            count++;
         }
      }

      // Also look to the parent class loaders of the TCL
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      URLClassLoader[] stack = ClassLoaderUtils.getClassLoaderStack(tcl);
      for (int s = 0; s < stack.length; s++)
      {
         URLClassLoader cl = stack[s];
         URL classURL = cl.findResource(classRsrcName);
         if (classURL != null)
         {
            results.append("\n\n### Instance" + count + " via UCL: " + cl + "\n");
            count++;
         }
      }

      return results.toString();
   }

   // LoaderRepository overrides ------------------------------------

   /** First tries to load from any UCL in the ULR, and if the
    * class is not found, next tries the current thread context
    * class loader.
    * @param className - the class to load
    */
   public Class loadClass(String className) throws ClassNotFoundException
   {
      // Try to load from a UCL in the ULR first
      ClassLoader scl = Thread.currentThread().getContextClassLoader();
      ClassLoader ucl = null;
      if (classLoaders.size() > 0)
         ucl = (ClassLoader) this.classLoaders.iterator().next();
      try
      {
         if (ucl != null)
            return loadClass(className, false, ucl);
      }
      catch (ClassNotFoundException ignore)
      {
         // go on and try the next loader
      }

      try
      {
         // If there is no class try the TCL
         return scl.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         // go on and try the next loader
      }

      // at last try a native
      Class clazz = getNativeClassForName(className);
      if (clazz != null) return clazz;

      throw new ClassNotFoundException(className);
   }

   /**
    * Loads a class from the repository, excluding the given
    * classloader.
    *
    * @param loader the classloader to exclude
    * @param className the class to load
    * @return the found class
    * @exception ClassNotFoundException when there is no such class
    */
   public Class loadClassWithout(ClassLoader loader, String className)
           throws ClassNotFoundException
   {
      throw new ClassNotFoundException("NYI");
   }

   /**
    * Loads a class from the repository, using the classloaders that were
    * registered before the given classloader.
    *
    * @param stop      consult all the classloaders registered before this one
    *                  in an attempt to load a class
    * @param className name of the class to load
    * @return loaded class instance
    * @throws ClassNotFoundException if none of the consulted classloaders were
    *                                able to load the requested class
    */
   public Class loadClassBefore(ClassLoader stop, String className) throws ClassNotFoundException
   {
      RepositoryClassLoader stopAt = getRepositoryClassLoader(stop, className);
      return stopAt.loadClassBefore(className);
   }

   /**
    * Get any wrapping classloader for the passed classloader
    * 
    * @param cl the wrapped classloader
    * @return the wrapping classloader or null if not wrapped
    */
   public RepositoryClassLoader getWrappingClassLoader(ClassLoader cl)
   {
      synchronized (classLoaders)
      {
         return (RepositoryClassLoader) nonUCLClassLoader.get(cl);
      }
   }
   
   /** Add a class loader to the repository.
    */
   public void addClassLoader(ClassLoader loader)
   {
      // if you come to us as UCL we send you straight to the orbit
      if (loader instanceof RepositoryClassLoader)
         addRepositoryClassLoader((RepositoryClassLoader) loader);
      else if (loader instanceof MLet)
      {
         addMLetClassLoader((MLet) loader);
      }
      else if (loader instanceof URLClassLoader)
      {
         addURLClassLoader((URLClassLoader) loader);
      }
      else
      {
         log.warn("Tried to add non-URLClassLoader.  Ignored");
      } // end of else
   }

   public boolean addClassLoaderURL(ClassLoader cl, URL url)
   {
      RepositoryClassLoader ucl = (RepositoryClassLoader) cl;
      boolean added = false;
      synchronized (classLoaders)
      {
         // Strip any query parameter
         String query = url.getQuery();
         if (query != null)
         {
            String ext = url.toExternalForm();
            String ext2 = ext.substring(0, ext.length() - query.length() - 1);
            try
            {
               url = new URL(ext2);
            }
            catch (MalformedURLException e)
            {
               log.warn("Failed to strip query from: " + url, e);
            }
         }

         // See if the URL is associated with a class loader
         if (classLoaderURLs.contains(url) == false)
         {
            updatePackageMap(ucl, url);
            classLoaderURLs.add(url);
            added = true;
            // Check for a dynamic URL
            if (query != null && query.indexOf("dynamic=true") >= 0)
               dynamicClassLoaders.add(ucl);
         }
      }
      return added;
   }
   
   /** Add a UCL to the repository.
    * This sychronizes on classLoaders.
    * @param cl
    */
   private void addRepositoryClassLoader(RepositoryClassLoader cl)
   {
      cl.setRepository(this);
      // See if this URL already exists
      URL url = cl.getURL();
      boolean added = false;
      synchronized (classLoaders)
      {
         boolean exists = false;
         if (cl instanceof UnifiedClassLoader)
            exists = classLoaderURLs.contains(url);
         // If already present will not be added
         if (!exists)
         {
            if (url != null)
               classLoaderURLs.add(url);
            added = classLoaders.add(cl);
         }
         if (added)
         {
            log.debug("Adding " + cl);
            addedCount++;
            cl.setAddedOrder(addedCount);
            updatePackageMap(cl);
         }
         else
         {
            log.debug("Skipping duplicate " + cl);
         }
      }
   }

   private void addMLetClassLoader(MLet loader)
   {
      MLetRepositoryClassLoader rcl = new MLetRepositoryClassLoader(loader);
      synchronized (classLoaders)
      {
         nonUCLClassLoader.put(loader, rcl);
      }
      addRepositoryClassLoader(rcl);
   }

   private void addURLClassLoader(URLClassLoader loader)
   {
      URL[] urls = loader.getURLs();
      int count = urls != null && urls.length > 0 ? urls.length : 0;
      URL origURL = count > 0 ? urls[0] : null;
      UnifiedClassLoader3 ucl3 = new UnifiedClassLoader3(origURL, origURL, this);
      addRepositoryClassLoader(ucl3);
      synchronized (classLoaders)
      {
         nonUCLClassLoader.put(loader, ucl3);
      }
      for (int i = 1; i < count; i++)
      {
         this.addClassLoaderURL(ucl3, urls[i]);
      }
   }
   
   /** Walk through the class loader URL to see what packages it is capable
    of handling
    */
   private void updatePackageMap(RepositoryClassLoader cl)
   {
      try
      {
         URL url = cl.getURL();
         PackageMapper listener = new PackageMapper(cl);
         ClassLoaderUtils.updatePackageMap(url, listener);
      }
      catch (Exception e)
      {
         if (log.isTraceEnabled())
            log.trace("Failed to update pkgs for cl=" + cl, e);
         else
            log.debug("Failed to update pkgs for cl=" + cl+", "+e.getMessage());
      }
   }

   /** Walk through the new URL to update the packages the ClassLoader is
    * capable of handling
    */
   private void updatePackageMap(RepositoryClassLoader cl, URL url)
   {
      try
      {
         PackageMapper listener = new PackageMapper(cl);
         ClassLoaderUtils.updatePackageMap(url, listener);
      }
      catch (Exception e)
      {
         if (log.isTraceEnabled())
            log.trace("Failed to update pkgs for cl=" + cl, e);
         else
            log.debug("Failed to update pkgs for cl=" + cl, e);
      }
   }

   /** Remove the class loader from the repository. This synchronizes on the
    * this.classLoaders
    */
   public void removeClassLoader(ClassLoader loader)
   {
      ArrayList removeNotifications = new ArrayList();
      ClassLoader cl = loader;
      synchronized (classLoaders)
      {
         if ((loader instanceof RepositoryClassLoader) == false)
         {
            cl = (ClassLoader) nonUCLClassLoader.remove(loader);
         }
         if (cl instanceof RepositoryClassLoader)
         {
            RepositoryClassLoader ucl = (RepositoryClassLoader) cl;
            if (getTranslator() != null)
               getTranslator().unregisterClassLoader(ucl);
            URL[] urls = ucl.getClasspath();
            for (int u = 0; u < urls.length; u++)
               classLoaderURLs.remove(urls[u]);
         }
         boolean dynamic = dynamicClassLoaders.remove(cl);
         boolean removed = classLoaders.remove(cl);
         log.debug("UnifiedLoaderRepository removed(" + removed + ") " + cl);

         // Take care also of the cycling mapping for classes
         HashSet loadedClasses = null;
         boolean hasLoadedClasses = false;
         synchronized (classes)
         {
            hasLoadedClasses = loaderToClassesMap.containsKey(cl);
            if (hasLoadedClasses)
               loadedClasses = (HashSet) loaderToClassesMap.remove(cl);
            // This classloader has loaded at least one class
            if (loadedClasses != null)
            {
               // Notify that classes are about to be removed
               for (Iterator iter = loadedClasses.iterator(); iter.hasNext();)
               {
                  String className = (String) iter.next();
                  Notification n = new Notification(CLASS_REMOVED, this,
                          getNextSequenceNumber(), className);
                  removeNotifications.add(n);
               }

               // Remove the classes from the global cache
               for (Iterator i = loadedClasses.iterator(); i.hasNext();)
               {
                  String cls = (String) i.next();
                  this.classes.remove(cls);
               }
            }
         }

         // Take care also of the cycling mapping for resources
         synchronized (loaderToResourcesMap)
         {
            if (loaderToResourcesMap.containsKey(cl))
            {
               HashMap resources = (HashMap) loaderToResourcesMap.remove(cl);

               // Remove the resources from the global cache that are from this classloader
               if (resources != null)
               {
                  for (Iterator i = resources.keySet().iterator(); i.hasNext();)
                  {
                     String name = (String) i.next();
                     ResourceInfo ri = (ResourceInfo) globalResources.get(name);
                     if (ri != null && ri.cl == cl)
                        globalResources.remove(name);
                  }
               }
            }
         }

         // Clean up the package name to class loader mapping
         if (dynamic == false)
         {
            List<String> pkgNames = loaderToPackagesMap.remove(cl);
            if( pkgNames != null )
            {
               for(String pkgName : pkgNames)
               {
                  Set pkgSet = (Set) packagesMap.get(pkgName);
                  if (pkgSet != null)
                  {
                     Set<RepositoryClassLoader> newSet = ClassLoaderUtils.newPackageSet();
                     newSet.addAll(pkgSet);
                     pkgSet = newSet;

                     pkgSet.remove(cl);
                     packagesMap.put(pkgName, newSet);
                     if (pkgSet.isEmpty())
                        packagesMap.remove(pkgName);
                  }               
               }
            }
         }
         else
         {
            // A dynamic classloader could end up in any package set
            loaderToPackagesMap.remove(cl);
            for (Iterator i = packagesMap.entrySet().iterator(); i.hasNext();)
            {
               Map.Entry entry = (Map.Entry) i.next();
               Set pkgSet = (Set) entry.getValue();
               if(pkgSet.contains(cl))
               {
                  if(pkgSet.size() > 1)
                  {
                     Set<RepositoryClassLoader> newSet = ClassLoaderUtils.newPackageSet();
                     newSet.addAll(pkgSet);
                     newSet.remove(cl);
                     packagesMap.put(entry.getKey(), newSet);
                  }
                  else
                  {
                     pkgSet = Collections.emptySet();
                  }
               }
               if (pkgSet.isEmpty())
                  i.remove();
            }
         }
      }

      // Send the class removal notfications outside of the synchronized block
      for (int n = 0; n < removeNotifications.size(); n++)
      {
         Notification msg = (Notification) removeNotifications.get(n);
         broadcaster.sendNotification(msg);
      }

      if (NOTIFICATION_MODE == LEGACY_MODE || NOTIFICATION_MODE == WEAK_REFERENCE_MODE)
      {
         Notification msg = new Notification(CLASSLOADER_REMOVED, this, getNextSequenceNumber());
         if (NOTIFICATION_MODE == WEAK_REFERENCE_MODE)
            msg.setUserData(new WeakReference(cl));
         else
            msg.setUserData(cl);
         broadcaster.sendNotification(msg);
      }
   }

   /**
    * This method provides an mbean-accessible way to add a
    * UnifiedClassloader, and sends a notification when it is added.
    *
    * @param ucl an <code>UnifiedClassLoader</code> value
    * @return a <code>LoaderRepository</code> value
    *
    * @jmx.managed-operation
    */
   public LoaderRepository registerClassLoader(RepositoryClassLoader ucl)
   {
      addClassLoader(ucl);
      if (NOTIFICATION_MODE == LEGACY_MODE || NOTIFICATION_MODE == WEAK_REFERENCE_MODE)
      {
         Notification msg = new Notification(CLASSLOADER_ADDED, this, getNextSequenceNumber());
         if (NOTIFICATION_MODE == WEAK_REFERENCE_MODE)
            msg.setUserData(new WeakReference(ucl));
         else
            msg.setUserData(ucl);
         broadcaster.sendNotification(msg);
      }

      return this;
   }

   /**
    * @jmx.managed-operation
    */
   public LoaderRepository getInstance()
   {
      return this;
   }

   // implementation of javax.management.NotificationBroadcaster interface

   /**
    * addNotificationListener delegates to the broadcaster object we hold.
    *
    * @param listener a <code>NotificationListener</code> value
    * @param filter a <code>NotificationFilter</code> value
    * @param handback an <code>Object</code> value
    * @exception IllegalArgumentException if an error occurs
    */
   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter, Object handback) throws IllegalArgumentException
   {
      broadcaster.addNotificationListener(listener, filter, handback);
   }

   /**
    *
    * @return <description>
    */
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      if (info == null)
      {
         if (NOTIFICATION_MODE != NO_NOTIFICATION_MODE)
         {
            info = new MBeanNotificationInfo[]{
               new MBeanNotificationInfo(new String[]{"CLASSLOADER_ADDED"},
                       "javax.management.Notification",
                       "Notification that a classloader has been added to the extensible classloader"),
               new MBeanNotificationInfo(new String[]{"CLASS_REMOVED"},
                       "javax.management.Notification",
                       "Notification that a class has been removed from the extensible classloader")
   
            };
         }
         else
         {
            info = new MBeanNotificationInfo[0];
         }
      }
      return info;
   }

   /**
    * removeNotificationListener delegates to our broadcaster object
    *
    * @param listener a <code>NotificationListener</code> value
    * @exception ListenerNotFoundException if an error occurs
    */
   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      broadcaster.removeNotificationListener(listener);
   }

   // MBeanRegistration
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister()
      throws Exception
   {
   }

   public void postDeregister()
   {
      log.debug("postDeregister, clearing all references");
      classLoaders.clear();
      dynamicClassLoaders.clear();
      nonUCLClassLoader.clear();
      classLoaderURLs.clear();
      classes.clear();
      loaderToClassesMap.clear();
      loaderToResourcesMap.clear();
      globalResources.clear();
      packagesMap.clear();
      loaderToPackagesMap.clear();
   }

   private synchronized long getNextSequenceNumber()
   {
      return sequenceNumber++;
   }


   private RepositoryClassLoader getRepositoryClassLoader(ClassLoader cl, String name) throws ClassNotFoundException
   {
      if (cl instanceof RepositoryClassLoader)
         return (RepositoryClassLoader) cl;
      else
      {
         RepositoryClassLoader rcl = getWrappingClassLoader(cl);
         if (rcl == null)
            throw new ClassNotFoundException("Class not found " + name + " (Unknown classloader " + cl + ")");
         return rcl;
      }
   }

   private class PackageMapper implements ClassLoaderUtils.PkgNameListener
   {
      private RepositoryClassLoader loader;
      PackageMapper(RepositoryClassLoader loader)
      {
         this.loader = loader;
      }
      public void addPackage(String pkgName)
      {
         // Skip the standard J2EE archive directories
         if( pkgName.startsWith("META-INF") || pkgName.startsWith("WEB-INF") )
            return;

         Set<RepositoryClassLoader> pkgSet = (Set<RepositoryClassLoader>) packagesMap.get(pkgName);
         if( pkgSet == null )
         {
            pkgSet = ClassLoaderUtils.newPackageSet();
            packagesMap.put(pkgName, pkgSet);
         }
         if( pkgSet.contains(loader) == false )
         {
            // Make a copy of the pkgSet to avoid concurrent mods
            Set<RepositoryClassLoader> newSet = ClassLoaderUtils.newPackageSet();
            newSet.addAll(pkgSet);
            pkgSet = newSet;
            // Add the class loader
            pkgSet.add((RepositoryClassLoader)loader);
            packagesMap.put(pkgName, newSet);
            List<String> loaderPkgNames = loaderToPackagesMap.get(loader);
            if( loaderPkgNames == null )
            {
               loaderPkgNames = new ArrayList<String>();
               loaderToPackagesMap.put((RepositoryClassLoader)loader, loaderPkgNames);
            }
            loaderPkgNames.add(pkgName);

            // Anytime more than one class loader exists this may indicate a problem
            if( pkgSet.size() > 1 )
            {
               log.debug("Multiple class loaders found for pkg: "+pkgName);
            }
            if( log.isTraceEnabled() )
               log.trace("Indexed pkg: "+pkgName+", UCL: "+loader);
         }
      }
   }
}
