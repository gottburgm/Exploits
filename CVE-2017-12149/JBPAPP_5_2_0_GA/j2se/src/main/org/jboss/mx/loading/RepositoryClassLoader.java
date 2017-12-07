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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collections;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.logging.Logger;
import org.jboss.util.loading.Translator;
import org.jboss.util.collection.SoftSet;

import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * A RepositoryClassLoader.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81019 $
 */
public abstract class RepositoryClassLoader extends URLClassLoader implements RealClassLoader
{
   // Constants -----------------------------------------------------

   /** The log */
   private static final Logger log = Logger.getLogger(RepositoryClassLoader.class);

   /** The value returned by {@link #getURLs}. */
   private static final URL[] EMPTY_URL_ARRAY = {};

   // Attributes -----------------------------------------------------

   /** Reference to the repository. */
   protected LoaderRepository repository = null;
   /** The location where unregister is called from */
   protected Exception unregisterTrace;

   /** The relative order in which this class loader was added to the respository */
   private int addedOrder;
   
   /** The parent classloader */
   protected ClassLoader parent = null;
   
   /** Names of classes which have resulted in CNFEs in loadClassLocally */
   private Set classBlackList = Collections.synchronizedSet(new SoftSet());
   /** Names of resources that were not found in loadResourceLocally */
   private Set resourceBlackList = Collections.synchronizedSet(new HashSet());
   /** A HashMap<String, URL> for resource found in loadResourceLocally */
   private ConcurrentReaderHashMap resourceCache = new ConcurrentReaderHashMap();
   
   /** Lock */
   protected ReentrantLock loadLock = new ReentrantLock();

   /** A debugging variable used to track the recursive depth of loadClass() */
   protected int loadClassDepth;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new LoaderRepositoryClassLoader
    * 
    * @param urls the urls
    * @param parent the parent classloader
    */
   protected RepositoryClassLoader(URL[] urls, ClassLoader parent)
   {
      super(urls, parent);
      this.parent = parent;
      // Check the blacklist mode
      String mode = ClassToStringAction.getProperty("org.jboss.mx.loading.blacklistMode", null);
      if( mode == null || mode.equalsIgnoreCase("HashSet") )
      {
         classBlackList = Collections.synchronizedSet(new HashSet());
         resourceBlackList = Collections.synchronizedSet(new HashSet());
      }
      else if( mode.equalsIgnoreCase("SoftSet") )
      {
         classBlackList = Collections.synchronizedSet(new SoftSet());
         resourceBlackList = Collections.synchronizedSet(new SoftSet());
      }
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the ObjectName
    * 
    * @return the object name
    */
   public abstract ObjectName getObjectName();

   public boolean isValid()
   {
      return getLoaderRepository() != null;
   }
   
   /**
    * Get the loader repository for this classloader
    */
   public LoaderRepository getLoaderRepository()
   {
      return repository;
   }

   /**
    * Set the loader repository
    * 
    * @param repository the repository
    */
   public void setRepository(LoaderRepository repository)
   {
      log.debug("setRepository, repository="+repository+", cl=" + this);
      this.repository = repository;
   }

   public Class<?> getCachedClass(String name)
   {
      LoaderRepository repository = this.repository;
      if (repository == null)
         return null;
      return repository.getCachedClass(name);
   }

   public URL getCachedResource(String name)
   {
      // Not Implemented
      return null;
   }

   public void clearBlackList(String name)
   {
      //Not Implemented yet
   }

   /**
    * Get the order this classloader was added to the repository
    * 
    * @return the order
    */
   public int getAddedOrder()
   {
      return addedOrder;
   }

   /**
    * Set the order this classloader was added to the repository
    * 
    * @param addedOrder the added order
    */
   public void setAddedOrder(int addedOrder)
   {
      this.addedOrder = addedOrder;
   }

   /** 
    * Called to attempt to load a class from the set of URLs associated with this classloader.
    */
   public Class loadClassLocally(String name, boolean resolve)
      throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("loadClassLocally, " + this + " name=" + name);
      if( name == null || name.length() == 0 )
         throw new ClassNotFoundException("Null or empty class name");

      Class result = null;
      try
      {
         if (isClassBlackListed(name))
         {
            if( trace )
               log.trace("Class in blacklist, name="+name);
            throw new ClassNotFoundException("Class Not Found(blacklist): " + name);
         }

         // For java classes go straight to the system classloader
         if (name.startsWith("java."))
         {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            return systemClassLoader.loadClass(name);
         }

         try
         {
            result = super.loadClass(name, resolve);
            return result;
         }
         catch (ClassNotFoundException cnfe)
         {
            addToClassBlackList(name);
            // If this is an array class, use Class.forName to resolve it
            if( name.charAt(0) == '[' )
            {
               result = Class.forName(name, true, this);
               removeFromClassBlackList(name);
               return result;
            }
            if( trace )
               log.trace("CFNE: Adding to blacklist: "+name);
            throw cnfe;
         }
      }
      finally
      {
         if (trace)
         {
            if (result != null)
               log.trace("loadClassLocally, " + this + " name=" + name + " class=" + result + " cl=" + result.getClassLoader());
            else
               log.trace("loadClassLocally, " + this + " name=" + name + " not found");
         }
      }
   }

   /**
   * Provides the same functionality as {@link java.net.URLClassLoader#getResource}.
   */
   public URL getResourceLocally(String name)
   {
      URL resURL = (URL) resourceCache.get(name);
      if (resURL != null)
         return resURL;
      if (isResourceBlackListed(name))
         return null;
      resURL = super.getResource(name);
      if( log.isTraceEnabled() == true )
         log.trace("getResourceLocally("+this+"), name="+name+", resURL:"+resURL);
      if (resURL == null)
         addToResourceBlackList(name);
      else
         resourceCache.put(name, resURL);
      return resURL;
   }

   /**
    * Get the URL associated with the UCL.
    * 
    * @return the url
    */
   public URL getURL()
   {
      URL[] urls = super.getURLs();
      if (urls.length > 0)
         return urls[0];
      else
         return null;
   }
   
   public void unregister()
   {
      log.debug("Unregistering cl=" + this);
      if (repository != null)
         repository.removeClassLoader(this);
      clearBlacklists();
      resourceCache.clear();
      repository = null;
      this.unregisterTrace = new Exception();
   }

   /**
    * This method simply invokes the super.getURLs() method to access the
    * list of URLs that make up the RepositoryClassLoader classpath.
    * 
    * @return the urls that make up the classpath
    */
   public URL[] getClasspath()
   {
      return super.getURLs();
   }

   /**
    * Return all library URLs associated with this RepositoryClassLoader
    *
    * <p>Do not remove this method without running the WebIntegrationTestSuite
    */
   public URL[] getAllURLs()
   {
      return repository.getURLs();
   }

   /**
    * Black list a class 
    * 
    * @param name the name of the class
    */
   public void addToClassBlackList(String name)
   {
      classBlackList.add(name);
   }

   /**
    * Remove class from black list 
    * 
    * @param name the name of the class
    */
   public void removeFromClassBlackList(String name)
   {
      classBlackList.remove(name);
   }
   
   /**
    * Is the class black listed?
    * 
    * @param name the name of the class
    * @return true when the class is black listed, false otherwise
    */
   public boolean isClassBlackListed(String name)
   {
      return classBlackList.contains(name);
   }
    
   /**
    * Clear any class black list.
    */
   public void clearClassBlackList()
   {
      classBlackList.clear();
   }

   /**
    * Black list a resource 
    * 
    * @param name the name of the resource
    */
   public void addToResourceBlackList(String name)
   {
      resourceBlackList.add(name);
   }

   /**
    * Remove resource from black list 
    * 
    * @param name the name of the resource
    */
   public void removeFromResourceBlackList(String name)
   {
      resourceBlackList.remove(name);
   }
   
   /**
    * Is the resource black listed?
    * 
    * @param name the name of the resource
    * @return true when the resource is black listed, false otherwise
    */
   public boolean isResourceBlackListed(String name)
   {
      return resourceBlackList.contains(name);
   }
   
   /**
    * Clear any resource blacklist.
    */
   public void clearResourceBlackList()
   {
      resourceBlackList.clear();
   }

   /**
    * Clear all blacklists
    */
   public void clearBlacklists()
   {
      clearClassBlackList();
      clearResourceBlackList();
   }


   // URLClassLoader overrides --------------------------------------

   /** The only caller of this method should be the VM initiated
   * loadClassInternal() method. This method attempts to acquire the
   * UnifiedLoaderRepository2 lock and then asks the repository to
   * load the class.
   *
   * <p>Forwards request to {@link LoaderRepository}.
   */
   public Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("loadClass " + this + " name=" + name+", loadClassDepth="+loadClassDepth);
      Class clazz = null;
      try
      {
         if (repository != null)
         {
            clazz = repository.getCachedClass(name);
            if (clazz != null)
            {
               if( log.isTraceEnabled() )
               {
                  StringBuffer buffer = new StringBuffer("Loaded class from cache, ");
                  ClassToStringAction.toString(clazz, buffer);
                  log.trace(buffer.toString());
               }
               return clazz;
            }
         }
         clazz = loadClassImpl(name, resolve, Integer.MAX_VALUE);
         return clazz;
      }
      finally
      {
         if (trace)
         {
            if (clazz != null)
               log.trace("loadClass " + this + " name=" + name + " class=" + clazz + " cl=" + clazz.getClassLoader());
            else
               log.trace("loadClass " + this + " name=" + name + " not found");
         }
      }
   }

   /** The only caller of this method should be the VM initiated
   * loadClassInternal() method. This method attempts to acquire the
   * UnifiedLoaderRepository2 lock and then asks the repository to
   * load the class.
   *
   * <p>Forwards request to {@link LoaderRepository}.
   */
   public Class loadClassBefore(String name)
      throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("loadClassBefore " + this + " name=" + name);
      Class clazz = null;
      try
      {
         clazz = loadClassImpl(name, false, addedOrder);
         return clazz;
      }
      finally
      {
         if (trace)
         {
            if (clazz != null)
               log.trace("loadClassBefore " + this + " name=" + name + " class=" + clazz + " cl=" + clazz.getClassLoader());
            else
               log.trace("loadClassBefore " + this + " name=" + name + " not found");
         }
      }
   }

   public abstract Class loadClassImpl(String name, boolean resolve, int stopAt)
      throws ClassNotFoundException;

   /**
   * Attempts to load the resource from its URL and if not found
   * forwards to the request to {@link LoaderRepository}.
   */
   public URL getResource(String name)
   {
      if (repository != null)
         return repository.getResource(name, this);
      return null;
   }

   /** Find all resource URLs for the given name. This overrides the
    * URLClassLoader version to look for resources in the repository.
    *
    * @param name the name of the resource
    * @return Enumeration<URL>
    * @throws java.io.IOException
    */
   public Enumeration findResources(String name) throws IOException
   {
      Vector resURLs = new Vector();
      if( repository == null )
      {
         String msg = "Invalid use of destroyed classloader, UCL destroyed at:";
         IOException e = new IOException(msg);
         e.initCause(this.unregisterTrace);
         throw e;
      }
      repository.getResources(name, this, resURLs);
      return resURLs.elements();
   }

   /**
   * Provides the same functionality as {@link java.net.URLClassLoader#findResources}.
   */
   public Enumeration findResourcesLocally(String name) throws IOException
   {
      return super.findResources(name);
   }

    /** Called by loadClassLocally to find the requested class within this
     * class loaders class path.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
   protected Class findClass(String name) throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("findClass, name="+name);
      if (isClassBlackListed(name))
      {
         if( trace )
            log.trace("Class in blacklist, name="+name);
         throw new ClassNotFoundException("Class Not Found(blacklist): " + name);
      }

      if( repository == null )
      {
         String msg = "Invalid use of destroyed classloader, UCL destroyed at:";
         ClassNotFoundException e = new ClassNotFoundException(msg);
         e.initCause(this.unregisterTrace);
         throw e;
      }
      Translator translator = repository.getTranslator();
      if (translator != null)
      {
         // Obtain the transformed class bytecode 
         try
         {
            // Obtain the raw bytecode from the classpath
            URL classUrl = getClassURL(name);
            byte[] rawcode = loadByteCode(classUrl);
            URL codeSourceUrl = getCodeSourceURL(name, classUrl);
            ProtectionDomain pd = getProtectionDomain(codeSourceUrl);
            byte[] bytecode = translator.transform(this, name, null, pd, rawcode);
            // If there was no transform use the raw bytecode
            if( bytecode == null )
               bytecode = rawcode;
            // Define the class package and instance
            definePackage(name);
            return defineClass(name, bytecode, 0, bytecode.length, pd);
         }
         catch(ClassNotFoundException e)
         {
            throw e;
         }
         catch (Throwable ex)
         {
            throw new ClassNotFoundException(name, ex);
         }
      }

      Class clazz = null;
      try
      {
         clazz = findClassLocally(name);
      }
      catch(ClassNotFoundException e)
      {
         if( trace )
            log.trace("CFNE: Adding to blacklist: "+name);
         addToClassBlackList(name);
         throw e;
      }
      return clazz;
   }

   /**
    * Find the class
    * 
    * @param name the name of the class
    * @return the class
    */
   protected Class findClassLocally(String name) throws ClassNotFoundException
   {
      return super.findClass(name);
   }
   
   /**
    * Define the package for the class if not already done
    *
    * @todo this properly
    * @param className the class name
    */
   protected void definePackage(String className)
   {
      int i = className.lastIndexOf('.');
      if (i == -1)
         return;

      try
      {
         definePackage(className.substring(0, i), null, null, null, null, null, null, null);
      }
      catch (IllegalArgumentException alreadyDone)
      {
      }
   }

   /** Append the given url to the URLs used for class and resource loading
    * @param url the URL to load from
    */
   public void addURL(URL url)
   {
      if( url == null )
         throw new IllegalArgumentException("url cannot be null");

      if( repository.addClassLoaderURL(this, url) == true )
      {
         log.debug("Added url: "+url+", to ucl: "+this);
         // Strip any query parameters
         String query = url.getQuery();
         if( query != null )
         {
            String ext = url.toExternalForm();
            String ext2 = ext.substring(0, ext.length() - query.length() - 1);
            try
            {
               url = new URL (ext2);
            }
            catch(MalformedURLException e)
            {
               log.warn("Failed to strip query from: "+url, e);
            }
         }
         super.addURL(url);
         clearBlacklists();
      }
      else if( log.isTraceEnabled() )
      {
         log.trace("Ignoring duplicate url: "+url+", for ucl: "+this);
      }
   }   

   /**
   * Return an empty URL array to force the RMI marshalling subsystem to
   * use the <tt>java.server.codebase</tt> property as the annotated codebase.
   *
   * <p>Do not remove this method without discussing it on the dev list.
   *
   * @return Empty URL[]
   */
   public URL[] getURLs()
   {
      return EMPTY_URL_ARRAY;
   }

   public Package getPackage(String name)
   {
      return super.getPackage(name);
   }
   
   public Package[] getPackages()
   {
      return super.getPackages();
   }
   
   // Object overrides ----------------------------------------------

   /**
    * This is here to document that this must delegate to the
    * super implementation to perform identity based equality. Using
    * URL based equality caused conflicts with the Class.forName(String,
    * boolean, ClassLoader).
    */
   public final boolean equals(Object other)
   {
      return super.equals(other);
   }

   /**
    * This is here to document that this must delegate to the
    * super implementation to perform identity based hashing. Using
    * URL based hashing caused conflicts with the Class.forName(String,
    * boolean, ClassLoader).
    */
   public final int hashCode()
   {
      return super.hashCode();
   }

   /**
   * Returns a string representation.
   */
   public String toString()
   {
      return super.toString() + "{ url=" + getURL() + " }";
   }
   
   // Protected -----------------------------------------------------

   /** Attempt to acquire the class loading lock. This lock must be acquired
    * before a thread enters the class loading task loop in loadClass. This
    * method maintains any interrupted state of the calling thread.
    *@see #loadClass(String, boolean)
    */
   protected boolean attempt(long waitMS)
   {
      boolean acquired = false;
      boolean trace = log.isTraceEnabled();
      // Save and clear the interrupted state of the incoming thread
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         acquired = loadLock.attempt(waitMS);
      }
      catch(InterruptedException e)
      {
      }
      finally
      {
         // Restore the interrupted state of the thread
         if( threadWasInterrupted )
            Thread.currentThread().interrupt();
      }
      if( trace )
         log.trace("attempt("+loadLock.holds()+") was: "+acquired+" for :"+this);
      return acquired;
   }
   
   /** Acquire the class loading lock. This lock must be acquired
    * before a thread enters the class loading task loop in loadClass.
    *@see #loadClass(String, boolean)
    */
   protected void acquire()
   {
      // Save and clear the interrupted state of the incoming thread
      boolean threadWasInterrupted = Thread.interrupted();
      try
      {
         loadLock.acquire();
      }
      catch(InterruptedException e)
      {
      }
      finally
      {
         // Restore the interrupted state of the thread
         if( threadWasInterrupted )
            Thread.currentThread().interrupt();
      }
      if( log.isTraceEnabled() )
         log.trace("acquired("+loadLock.holds()+") for :"+this);
   }
   /** Release the class loading lock previous acquired through the acquire
    * method.
    */
   protected void release()
   {
      if( log.isTraceEnabled() )
         log.trace("release("+loadLock.holds()+") for :"+this);
      loadLock.release();
      if( log.isTraceEnabled() )
         log.trace("released, holds: "+loadLock.holds());
   }

   /** Obtain the bytecode for the indicated class from this class loaders
    * classpath.
    * 
    * @param classname
    * @return the bytecode array if found
    * @exception ClassNotFoundException - if the class resource could not
    *    be found
    */ 
   protected byte[] loadByteCode(String classname)
      throws ClassNotFoundException, IOException
   {
      byte[] bytecode = null;
      URL classURL = getClassURL(classname);

      // Load the class bytecode
      InputStream is = null;
      try
      {
         is = classURL.openStream();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] tmp = new byte[1024];
         int read = 0;
         while( (read = is.read(tmp)) > 0 )
         {
            baos.write(tmp, 0, read);
         }
         bytecode = baos.toByteArray();
      }
      finally
      {
         if( is != null )
            is.close();
      }

      return bytecode;
   }

   /** Obtain the bytecode for the indicated class from this class loaders
    * classpath.
    *
    * @param classURL
    * @return the bytecode array if found
    * @exception ClassNotFoundException - if the class resource could not
    *    be found
    */
   protected byte[] loadByteCode(URL classURL)
      throws ClassNotFoundException, IOException
   {
      byte[] bytecode = null;
      // Load the class bytecode
      InputStream is = null;
      try
      {
         is = classURL.openStream();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] tmp = new byte[1024];
         int read = 0;
         while( (read = is.read(tmp)) > 0 )
         {
            baos.write(tmp, 0, read);
         }
         bytecode = baos.toByteArray();
      }
      finally
      {
         if( is != null )
            is.close();
      }

      return bytecode;
   }
   
   /**
    * Determine the protection domain. If we are a copy of the original
    * deployment, use the original url as the codebase.
    * @return the protection domain
    * @todo certificates and principles?
    */
   protected ProtectionDomain getProtectionDomain(URL codesourceUrl)
   {
      Certificate certs[] = null;
      CodeSource cs = new CodeSource(codesourceUrl, certs);
      PermissionCollection permissions = Policy.getPolicy().getPermissions(cs);
      if (log.isTraceEnabled())
         log.trace("getProtectionDomain, url=" + codesourceUrl +
                   " codeSource=" + cs + " permissions=" + permissions);
      return new ProtectionDomain(cs, permissions);
   }

   // Package Private -----------------------------------------------
   
   // Private -------------------------------------------------------

   private URL getCodeSourceURL(String classname, URL classURL) throws java.net.MalformedURLException
   {
      String classRsrcName = classname.replace('.', '/') + ".class";
      String urlAsString = classURL.toString();
      int idx = urlAsString.indexOf(classRsrcName);
      if (idx == -1) return classURL;
      urlAsString = urlAsString.substring(0, idx);
      return new URL(urlAsString);
   }

   private URL getClassURL(String classname) throws ClassNotFoundException
   {
      String classRsrcName = classname.replace('.', '/') + ".class";
      URL classURL = this.getResourceLocally(classRsrcName);
      if( classURL == null )
      {
         String msg = "Failed to find: "+classname+" as resource: "+classRsrcName;
         throw new ClassNotFoundException(msg);
      }
      return classURL;
   }

   // Inner classes -------------------------------------------------
}
