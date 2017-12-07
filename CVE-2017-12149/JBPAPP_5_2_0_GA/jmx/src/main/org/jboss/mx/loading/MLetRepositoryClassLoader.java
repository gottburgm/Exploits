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

import javax.management.ObjectName;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;

/**
 * A RepositoryClassLoader that wraps an MLet.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81022 $
 */
class MLetRepositoryClassLoader extends RepositoryClassLoader
{
   // Constants -----------------------------------------------------

   /** The log */
   private static final Logger log = Logger.getLogger(MLetRepositoryClassLoader.class);

   // Attributes -----------------------------------------------------

   /** The MLet */
   private MLet mlet;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Create a new LoaderRepositoryClassLoader
    * 
    * @param urls the urls
    * @param parent the parent classloader
    */
   protected MLetRepositoryClassLoader(MLet mlet)
   {
      super(mlet.getURLs(), mlet);
      this.mlet = mlet;
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the ObjectName
    * 
    * @return the object name
    */
   public ObjectName getObjectName()
   {
      throw new UnsupportedOperationException("Not relevent");
   }

   /**
    * This method simply invokes the super.getURLs() method to access the
    * list of URLs that make up the RepositoryClassLoader classpath.
    * 
    * @return the urls that make up the classpath
    */
   public URL[] getClasspath()
   {
      return mlet.getURLs();
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
   
   public synchronized Class loadClassImpl(String name, boolean resolve, int stopAt)
      throws ClassNotFoundException
   {
      loadClassDepth ++;
      boolean trace = log.isTraceEnabled();

      if( trace )
         log.trace("loadClassImpl, name="+name+", resolve="+resolve);
      if( repository == null )
      {
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
         else if( task.loadException != null )
         {
            if( log.isTraceEnabled() )
               log.trace("Unexpected error during load of:"+name, task.loadException);
            String msg = "Unexpected error during load of: "+name
               + ", msg="+task.loadException.getMessage();
            throw new ClassNotFoundException(msg);
         }
         // Assert that loadedClass is not null
         else
            throw new IllegalStateException("ClassLoadingTask.loadedTask is null, name: "+name);
      }

      return task.loadedClass;
   }

   // URLClassLoader overrides --------------------------------------

   public Class loadClassLocally(String name, boolean resolve)
      throws ClassNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("loadClassLocally, " + this + " name=" + name);
      Class result = null;
      try
      {
         result = mlet.loadClass(name, null);
         return result;
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
   
   // Object overrides ----------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Package Private -----------------------------------------------
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
