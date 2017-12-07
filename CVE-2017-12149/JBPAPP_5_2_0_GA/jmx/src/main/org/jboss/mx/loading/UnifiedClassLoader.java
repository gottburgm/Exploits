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
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Enumeration;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.loading.Translatable;

/**
* A ClassLoader which loads classes from a single URL in conjunction with
* the {@link LoaderRepository}. Notice that this classloader does
* not work independently of the repository. A repository reference
* must be provided via the constructor or the classloader must be explicitly
* registered to the repository before any attempt to load a class.
*
* At this point this is little more than an abstract class maintained as the
* interface for class loaders as the algorithm of the UnifiedLoaderRepository
* fails with deadlocks, and several other class loading exceptions in multi-
* threaded environments.
*
* @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="christoph.jung@jboss.org">Christoph G. Jung</a>
* @author <a href="scott.stark@jboss.org">Scott Stark</a>
* @author <a href="juha@jboss.org">Juha Lindfors</a>
* @author <a href="bill@jboss.org">Bill Burke</a>
* @version <tt>$Revision: 81022 $</tt>
*/
public class UnifiedClassLoader extends RepositoryClassLoader
   implements UnifiedClassLoaderMBean, Translatable
{
   // Static --------------------------------------------------------

   private static final Logger log = Logger.getLogger(UnifiedClassLoader.class);
   private ThreadLocal<String> delegatedClassName = new ThreadLocal<String>();

   // Attributes ----------------------------------------------------

   /** One URL per ClassLoader in our case */
   protected URL url = null;
   /** An optional URL from which url may have been copied. It is used to
    allow the security permissions to be based on a static url namespace. */
   protected URL origURL = null;

   // Constructors --------------------------------------------------
   /**
    * Construct a <tt>UnifiedClassLoader</tt> without registering it to the
    * classloader repository.
    *
    * @param url   the single URL to load classes from.
    */
   public UnifiedClassLoader(URL url)
   {
      this(url, (URL) null);
   }
   /**
    * Construct a <tt>UnifiedClassLoader</tt> without registering it to the
    * classloader repository.
    *
    * @param url   the single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    */
   public UnifiedClassLoader(URL url, URL origURL)
   {
      this(url, origURL, UnifiedClassLoader.class.getClassLoader());
   }

   /**  Construct a UnifiedClassLoader without registering with the
    * classloader repository.
    *
    * @param url   the single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    * @param parent the parent class loader to use
    */
   public UnifiedClassLoader(URL url, URL origURL, ClassLoader parent)
   {
      super(url == null ? new URL[]{} : new URL[] {url}, parent);

      if (log.isTraceEnabled())
         log.trace("New jmx UCL with url " + url);
      this.url = url;
      this.origURL = origURL;
   }

   /**
    * Construct a <tt>UnifiedClassLoader</tt> and registers it to the given
    * repository.
    *
    * @param   url   The single URL to load classes from.
    * @param   repository the repository this classloader delegates to
    */
   public UnifiedClassLoader(URL url, LoaderRepository repository)
   {
      this(url, null, repository);
   }
   /**
    * Construct a <tt>UnifiedClassLoader</tt> and registers it to the given
    * repository.
    * @param url The single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    * @param repository the repository this classloader delegates to
    * be a local copy or nested jar.
    */
   public UnifiedClassLoader(URL url, URL origURL, LoaderRepository repository)
   {
      this(url, origURL);

      // set the repository reference
      this.setRepository(repository);

      // register this loader to the given repository
      repository.addClassLoader(this);
   }

   /**
    * UnifiedClassLoader constructor that can be used to
    * register with a particular Loader Repository identified by ObjectName.
    *
    * @param url an <code>URL</code> value
    * @param server a <code>MBeanServer</code> value
    * @param repositoryName an <code>ObjectName</code> value
    * @exception Exception if an error occurs
    */
   public UnifiedClassLoader(final URL url, final MBeanServer server,
      final ObjectName repositoryName) throws Exception
   {
      this(url, null, server, repositoryName);
   }
   /**
    * UnifiedClassLoader constructor that can be used to
    * register with a particular Loader Repository identified by ObjectName.
    *
    * @param url an <code>URL</code> value
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    * @param server a <code>MBeanServer</code> value
    * @param repositoryName an <code>ObjectName</code> value
    * @exception Exception if an error occurs
    */
   public UnifiedClassLoader(final URL url, final URL origURL,
      final MBeanServer server, final ObjectName repositoryName) throws Exception
   {
      this(url, origURL);
      LoaderRepository rep = (LoaderRepository)server.invoke(repositoryName,
                    "registerClassLoader",
                    new Object[] {this},
                    new String[] {getClass().getName()});
      this.setRepository(rep);
   }

   // Public --------------------------------------------------------

   /** Obtain the ObjectName under which the UCL can be registered with the
    JMX server. This creates a name of the form "jmx.loading:UCL=hashCode"
    since we don't currently care that UCL be easily queriable.
    */
   public ObjectName getObjectName()
   {
      String name = "jmx.loading:UCL="+Integer.toHexString(super.hashCode());
      return ObjectNameFactory.create(name);
   }

   public void unregister()
   {
      super.unregister();
      this.origURL = null;
      this.url = null;
   }

   /** Get the URL associated with the UCL.
    */
   public URL getURL()
   {
      return url;
   }
   
   /** Get the original URL associated with the UCL. This may be null.
    */
   public URL getOrigURL()
   {
      return origURL;
   }
   
   public synchronized Class loadClassImpl(String name, boolean resolve, int stopAt)
      throws ClassNotFoundException
   {
      loadClassDepth ++;
      boolean trace = log.isTraceEnabled();

      if( trace )
         log.trace("loadClassImpl, name="+name+", resolve="+resolve);
      if( repository == null )
      {
         // If we have been undeployed we can still try locally
         try
         {
            /* We need validate we are not recursing trying to load a class
               that does not exist in the parent
            */
            String prevName = delegatedClassName.get();
            if( prevName != null )
            {
               if( prevName.equals(name) )
                  throw new ClassNotFoundException("Recursing on: "+name);
            }
            delegatedClassName.set(name);
            return super.loadClass(name, resolve);
         }
         catch (ClassNotFoundException ignored)
         {
         }
         finally
         {
            delegatedClassName.set(null);            
         }
         String msg = "Invalid use of destroyed classloader, UCL destroyed at:";
         throw new ClassNotFoundException(msg, this.unregisterTrace);
      }

      /* Since loadClass can be called from loadClassInternal with the monitor
         already held, we need to determine if there is a ClassLoadingTask
         which requires this UCL. If there is, we release the UCL monitor
         so that the ClassLoadingTask can use the UCL.
       */
      boolean acquired = attempt(1);
      while( acquired == false )
      {
         /* Another thread needs this UCL to load a class so release the
          monitor acquired by the synchronized method. We loop until
          we can acquire the class loading lock.
         */
        try
         {
            if( trace )
               log.trace("Waiting for loadClass lock");
            this.wait();
         }
         catch(InterruptedException ignore)
         {
         }
         acquired = attempt(1);
      }

      ClassLoadingTask task = null;
      try
      {
         Thread t = Thread.currentThread();
         // Register this thread as owning this UCL
         if( loadLock.holds() == 1 )
            LoadMgr3.registerLoaderThread(this, t);

         // Create a class loading task and submit it to the repository
         task = new ClassLoadingTask(name, this, t, stopAt);
         /* Process class loading tasks needing this UCL until our task has
            been completed by the thread owning the required UCL(s).
          */
         UnifiedLoaderRepository3 ulr3 = (UnifiedLoaderRepository3) repository;
         if( LoadMgr3.beginLoadTask(task, ulr3) == false )
         {
            while( task.threadTaskCount != 0 )
            {
               try
               {
                  LoadMgr3.nextTask(t, task, ulr3);
               }
               catch(InterruptedException e)
               {
                  // Abort the load or retry?
                  break;
               }
            }
         }
      }
      finally
      {
         // Unregister as the UCL owner to reschedule any remaining load tasks
         if( loadLock.holds() == 1 )
            LoadMgr3.endLoadTask(task);
         // Notify any threads waiting to use this UCL
         this.release();
         this.notifyAll();
         loadClassDepth --;
      }

      if( task.loadedClass == null )
      {
         if( task.loadException instanceof ClassNotFoundException )
            throw (ClassNotFoundException) task.loadException;
         else if( task.loadException instanceof NoClassDefFoundError )
            throw (NoClassDefFoundError) task.loadException;
         else if( task.loadException != null )
         {
            if( log.isTraceEnabled() )
               log.trace("Unexpected error during load of:"+name, task.loadException);
            String msg = "Unexpected error during load of: "+name
               + ", msg="+task.loadException.getMessage();
            ClassNotFoundException cnfe = new ClassNotFoundException(msg, task.loadException);
            throw cnfe;
         }
         // Assert that loadedClass is not null
         else
            throw new IllegalStateException("ClassLoadingTask.loadedTask is null, name: "+name);
      }

      return task.loadedClass;
   }

   // URLClassLoader overrides --------------------------------------

   /** Override the permissions accessor to use the CodeSource
    based on the original URL if one exists. This allows the
    security policy to be defined in terms of the static URL
    namespace rather than the local copy or nested URL.
    This builds a PermissionCollection from:
    1. The origURL CodeSource
    2. The argument CodeSource
    3. The Policy.getPermission(origURL CodeSource)

    This is necessary because we cannot define the CodeSource the
    SecureClassLoader uses to register the class under.

    @param cs the location and signatures of the codebase.
    */
   protected PermissionCollection getPermissions(CodeSource cs)
   {
      CodeSource permCS = cs;
      if( origURL != null )
      {
         permCS = new CodeSource(origURL, cs.getCertificates());
      }
      Policy policy = Policy.getPolicy();
      PermissionCollection perms = super.getPermissions(permCS);
      PermissionCollection perms2 = super.getPermissions(cs);
      PermissionCollection perms3 = policy.getPermissions(permCS);
      Enumeration iter = perms2.elements();
      while( iter.hasMoreElements() )
         perms.add((Permission) iter.nextElement());
      iter = perms3.elements();
      while( iter.hasMoreElements() )
         perms.add((Permission) iter.nextElement());
      if( log.isTraceEnabled() )
         log.trace("getPermissions, url="+url+", origURL="+origURL+" -> "+perms);
      return perms;
   }

   /**
    * Determine the protection domain. If we are a copy of the original
    * deployment, use the original url as the codebase.
    * @return the protection domain
    * @todo certificates and principles?
    */
   protected ProtectionDomain getProtectionDomain()
   {
      return getProtectionDomain(origURL != null ? origURL : url);
   }
}
