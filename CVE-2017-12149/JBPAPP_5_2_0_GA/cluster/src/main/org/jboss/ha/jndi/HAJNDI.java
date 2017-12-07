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
package org.jboss.ha.jndi;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.jndi.spi.DistributedTreeManager;
import org.jboss.logging.Logger;
import org.jnp.interfaces.Naming;

/**
 *  Provides the Naming implemenation. Lookups will look for Names in 
 *  the injected DistributedTreeManager and if not found will distributedTreeManager to the local 
 *  InitialContext. If still not found, a group RPC will be sent to the cluster
 *  using the provided partition.  All other Naming operations distributedTreeManager to the 
 *  DistributedTreeManager.
 *
 *  @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *  @author Scott.Stark@jboss.org
 *  @author Brian Stansberry
 *  @author Galder Zamarreño
 *  
 *  @version $Revision: 112915 $
 */
public class HAJNDI
   implements org.jnp.interfaces.Naming
{
   private static final Logger log = Logger.getLogger(HAJNDI.class);
   
   /** @since 1.12.2.4, jboss-3.2.2 */
   static final long serialVersionUID = -6277328603304171620L;
   
   // Attributes --------------------------------------------------------
   
   private final HAPartition partition;
   private final DistributedTreeManager distributedTreeManager;
   private final Naming localNamingInstance;
   private boolean missingLocalNamingLogged;

   protected static final int MAX_CONCURRENT_REQUESTS = Integer.MAX_VALUE;
   protected Semaphore startStopSemaphore = new Semaphore(0, true);

   // Constructor --------------------------------------------------------
  
   public HAJNDI(HAPartition partition, DistributedTreeManager distributedTreeManager, Naming localNamingInstance)
   {
      if (partition == null)
      {
         throw new IllegalArgumentException("Null partition");
      }
      
      if (distributedTreeManager == null)
      {
         throw new IllegalArgumentException("Null distributedTreeManager");
      }
      
      if (localNamingInstance == null)
      {
         log.debug("No localNamingInstance provided; injecting a local naming instance is recommended");
      }
      
      this.partition = partition;
      this.distributedTreeManager = distributedTreeManager;
      this.localNamingInstance = localNamingInstance;
   }
   
   // Public --------------------------------------------------------

   public void init()
   {
      log.debug("HAJNDI registering RPC Handler with HAPartition");
      this.partition.registerRPCHandler("HAJNDI", this);
      this.distributedTreeManager.init();

      // Start accepting requests
      startStopSemaphore.release(MAX_CONCURRENT_REQUESTS);
   }

   public void shutdown()
   {
      log.debug("HAJNDI unregistering RPCHandler with HAPartition");
      this.partition.unregisterRPCHandler("HAJNDI", this);

      // wait for current requests to finish
      try
      {
         startStopSemaphore.acquire(MAX_CONCURRENT_REQUESTS);
      }
      catch ( InterruptedException e )
      {
         Thread.currentThread().interrupt();
      }

      this.distributedTreeManager.shutdown();
   }

   /**
    * Performs a lookup against the local Naming service. This method is only
    * public so HAPartition can invoke on it via reflection.
    * 
    * @param name the name
    * @return     the object bound locally under name
    * @throws NamingException
    */
   public Object lookupLocally(Name name) throws NamingException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("lookupLocally, name="+name);
      }

      // We cannot do InitialContext().lookup(name) because
      // we get ClassNotFound errors and ClassLinkage errors.
      // So, what we prefer to use an injected local Naming instance
      try
      {
         if (localNamingInstance != null)
         {
            return localNamingInstance.lookup(name);
         }

         return new InitialContext().lookup(name);
      }
      catch (NameNotFoundException e)
      {
         if (trace)
         {
            log.trace("lookupLocally failed, name=" + name, e);
         }
         throw e;
      }
      catch (NamingException e)
      {
         if (!logMissingLocalNamingInstance(e) && trace)
         {
            log.trace("lookupLocally failed, name=" + name, e);
         }
         throw e;
      }
      catch (java.rmi.RemoteException e)
      {
         NamingException ne = new NamingException("unknown remote exception");
         ne.setRootCause(e);
         if( trace )
         {
            log.trace("lookupLocally failed, name=" + name, e);
         }
         throw ne;
      }
      catch (RuntimeException e)
      {
         if (!logMissingLocalNamingInstance(e) && trace)
         {
            log.trace("lookupLocally failed, name=" + name, e);
         }
         throw e;
      }
      catch (Error e)
      {
         logMissingLocalNamingInstance(e);
         throw e;
      }
   }

   // Naming implementation -----------------------------------------
   

   public synchronized void bind(Name name, Object obj, String className) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         this.distributedTreeManager.bind(name, obj, className);
      }
      finally
      {
         startStopSemaphore.release();
      }
   }

   public synchronized void rebind(Name name, Object obj, String className) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         this.distributedTreeManager.rebind(name, obj, className);
      }
      finally
      {
         startStopSemaphore.release();
      }
   }

   public synchronized void unbind(Name name) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         this.distributedTreeManager.unbind(name);
      }
      finally
      {
         startStopSemaphore.release();
      }
   }

   public Object lookup(Name name) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         Object binding = this.distributedTreeManager.lookup(name);
         if (binding == null)
         {
            try
            {
               binding = lookupLocally(name);
            }
            catch (NameNotFoundException nne)
            {
               binding = lookupRemotely(name);
               if (binding == null)
               {
                  throw nne;
               }
            }
         }
         return binding;
      }
      finally
      {
         startStopSemaphore.release();
      }
   }

   public Collection<NameClassPair> list(Name name) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         return this.distributedTreeManager.list(name) ;
      }
      finally
      {
         startStopSemaphore.release();
      }
   }
    
   public Collection<Binding> listBindings(Name name) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         return this.distributedTreeManager.listBindings(name);
      }
      finally
      {
         startStopSemaphore.release();
      }
   }
   
   public javax.naming.Context createSubcontext(Name name) throws NamingException
   {
      if (! startStopSemaphore.tryAcquire())
         throw new ServiceUnavailableException ( "HAJNDI service is not running" );

      try
      {
         return this.distributedTreeManager.createSubcontext(name);
      }
      finally
      {
         startStopSemaphore.release();
      }
   }
   
   // ----------------------------------------------------------------  Private
   
   private Object lookupRemotely(Name name) throws NameNotFoundException
   {
      boolean trace = log.isTraceEnabled();
      
      // if we get here, this means we need to try on every node.
      Object[] args = new Object[1];
      args[0] = name;
      List<?> rsp = null;
      Exception cause = null;
      try
      {
         if (trace)
         {
            log.trace("calling lookupLocally(" + name + ") on HAJNDI cluster");
         }
         rsp = this.partition.callMethodOnCluster("HAJNDI", "lookupLocally", args, new Class[] { Name.class }, true, new LookupSucceededFilter());
      }
      catch (Exception ignored)
      {
         if (trace)
         {
            log.trace("Clustered lookupLocally("+name+") failed", ignored);
         }
         cause = ignored;
      }

      if (trace)
      {
         log.trace("Returned results size: "+ (rsp != null ? rsp.size() : 0));
      }
      if (rsp == null || rsp.size() == 0)
      {
         NameNotFoundException nnfe2 = new NameNotFoundException(name.toString());
         nnfe2.setRootCause(cause);
         throw nnfe2;
      }

      for (int i = 0; i < rsp.size(); i++)
      {
         Object result = rsp.get(i);
         if (trace)
         {
            String type = (result != null ? result.getClass().getName() : "null");
            log.trace("lookupLocally, i="+i+", value="+result+", type="+type);
         }
         // Ignore null and Exception return values
         if ((result != null) && !(result instanceof Exception))
         {
            return result;
         }
      }
      
      return null;
   }
   
   /**
    * One time and one time only logs a WARN if localNamingInstance was
    * not passed to our constructor and an error condition occurred while
    * attempting to do a local lookup via new InitialContext().
    * 
    * @param t the error that occurred
    * @return <code>true</code> if this method logged a WARN
    */
   private boolean logMissingLocalNamingInstance(Throwable t)
   {
      if (localNamingInstance == null && !missingLocalNamingLogged)
      {
         log.warn("No localNamingInstance configured and lookup via new InitialContext() failed; " +
               "injecting a local naming instance is recommended", t);
         missingLocalNamingLogged = true;
         return true;
      }
      return false;
   }
}
