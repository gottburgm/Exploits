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
package org.jboss.resource.connectionmanager;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.logging.Logger;

/**
 * A ConnectionValidator that performs background validation of managed connections for an
 * InternalManagedConnectionPool.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 85731 $
 */
public class ConnectionValidator
{
   /** The log */
   private static final Logger log = Logger.getLogger(ConnectionValidator.class);
   
   /** The pools */
   private final CopyOnWriteArrayList<InternalManagedConnectionPool> pools = new CopyOnWriteArrayList<InternalManagedConnectionPool>();
   
   /** The interval */
   private long interval = Long.MAX_VALUE;

   /** The next */
   private long next = Long.MAX_VALUE;//important initialization!

   /** The validator */
   private static final ConnectionValidator validator = new ConnectionValidator();

   private ConnectionValidator()
   {
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            Runnable runnable = new ConnectionValidatorRunnable();
            Thread removerThread = new Thread(runnable, "ConnectionValidator");
            removerThread.setDaemon(true);
            removerThread.start();
            return null;
         }
      });
   }
   
   public static void registerPool(InternalManagedConnectionPool mcp, long interval)
   {
      validator.internalRegisterPool(mcp, interval);
   }
   
   public static void unRegisterPool(InternalManagedConnectionPool mcp)
   {
      validator.internalUnregisterPool(mcp);
   }
   
   private void internalRegisterPool(InternalManagedConnectionPool mcp, long interval)
   {
      synchronized (pools)
      {
         pools.addIfAbsent(mcp);
         
         if (interval > 1 && interval/2 < this.interval) 
         {
            this.interval = interval/2;
            long maybeNext = System.currentTimeMillis() + this.interval;
            if (next > maybeNext && maybeNext > 0) 
            {
               next = maybeNext;
               if (log.isDebugEnabled())
                  log.debug("internalRegisterPool: about to notify thread: old next: " + next + ", new next: " + maybeNext);
               pools.notify();
            }
         }
      }
   }
   
   private void internalUnregisterPool(InternalManagedConnectionPool mcp)
   {
      synchronized (pools)
      {
         pools.remove(mcp);
         if (pools.size() == 0) 
         {
            if (log.isDebugEnabled())
               log.debug("internalUnregisterPool: setting interval to Long.MAX_VALUE");
            interval = Long.MAX_VALUE;
         }
      }
   }
   
   private void setupContextClassLoader()
   {
      // Could be null if loaded from system classloader
      final ClassLoader cl = ConnectionValidator.class.getClassLoader();
      if (cl == null)
         return;
      
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
         Thread.currentThread().setContextClassLoader(cl);
      
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            Thread.currentThread().setContextClassLoader(cl);
            return null;
         }
      });
   }
   
   public static void waitForBackgroundThread()
   {
      synchronized (validator.pools)
      {
         return;
      }
   }

   private class ConnectionValidatorRunnable implements Runnable
   {
      public void run()
      {
         setupContextClassLoader();
         
         synchronized (pools)
         {
            while (true)
            {
               try
               {
                  pools.wait(interval);

                  if (log.isDebugEnabled())
                     log.debug("run: ConnectionValidator notifying pools, interval: " + interval);
        
                  for (InternalManagedConnectionPool mcp : pools)
                  {
                     mcp.validateConnections();
                  }

                  next = System.currentTimeMillis() + interval;
                  
                  if (next < 0)
                     next = Long.MAX_VALUE;
               }
               catch (InterruptedException e)
               {
                  log.info("run: ConnectionValidator has been interrupted, returning");
                  return;  
               }
               catch (RuntimeException e)
               {
                  log.warn("run: ConnectionValidator ignored unexpected runtime exception", e);
               }
               catch (Exception e)
               {
                  log.warn("run: ConnectionValidator ignored unexpected error", e);
               }
            }
         }
      }
   }
}
