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
 * IdleRemover
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 85731 $
 */
public class IdleRemover 
{
   private final static Logger log = Logger.getLogger(IdleRemover.class);

   private final CopyOnWriteArrayList<IdleConnectionRemovalSupport> pools = new CopyOnWriteArrayList<IdleConnectionRemovalSupport>();

   private long interval = Long.MAX_VALUE;

   private long next = Long.MAX_VALUE;//important initialization!

   private static final IdleRemover remover = new IdleRemover();

   private final boolean trace = log.isTraceEnabled();

   public static void registerPool(IdleConnectionRemovalSupport mcp, long interval)
   {
      remover.internalRegisterPool(mcp, interval);
   }

   public static void unregisterPool(IdleConnectionRemovalSupport mcp)
   {
      remover.internalUnregisterPool(mcp);
   }
   
   /**
    * For testing
    */
   public static void waitForBackgroundThread()
   {
      synchronized (remover.pools)
      {
         return;
      }
   }
   
   private IdleRemover ()
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            Runnable runnable = new IdleRemoverRunnable();
            Thread removerThread = new Thread(runnable, "IdleRemover");
            removerThread.setDaemon(true);
            removerThread.start();
            return null;
         }
      });
   }
   
   private void internalRegisterPool(IdleConnectionRemovalSupport mcp, long interval)
   {
      if (trace)
         log.trace("internalRegisterPool: registering pool with interval " + interval + " old interval: " + this.interval);
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
               if (trace)
                  log.trace("internalRegisterPool: about to notify thread: old next: " + next + ", new next: " + maybeNext);
               pools.notify();
            }
         }
      }
   }

   private void internalUnregisterPool(IdleConnectionRemovalSupport mcp)
   {
      synchronized (pools)
      {
         pools.remove(mcp);
         if (pools.size() == 0) 
         {
            if (trace)
               log.trace("internalUnregisterPool: setting interval to Long.MAX_VALUE");
            interval = Long.MAX_VALUE;
         }
      }
   }

   /**
    * Change the context classloader to be where the idle remover was loaded from.<p>
    * 
    * This avoids holding a reference to the caller's classloader which may be undeployed.
    */
   private void setupContextClassLoader()
   {
      // Could be null if loaded from system classloader
      final ClassLoader cl = IdleRemover.class.getClassLoader();
      if (cl == null)
         return;
      
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
         Thread.currentThread().setContextClassLoader(cl);
      
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            Thread.currentThread().setContextClassLoader(cl);
            return null;
         }
      });
   }

   /**
    * Idle Remover background thread
    */
   private class IdleRemoverRunnable implements Runnable
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
                  if (trace)
                     log.trace("run: IdleRemover notifying pools, interval: " + interval);
                  for (IdleConnectionRemovalSupport pool : pools ) 
                     pool.removeIdleConnections();
                  next = System.currentTimeMillis() + interval;
                  if (next < 0) 
                     next = Long.MAX_VALUE;      
               }
               catch (InterruptedException ie)
               {
                  if (log.isDebugEnabled())
                     log.debug("run: IdleRemover has been interrupted, ending");
                  return;  
               }
               catch (RuntimeException e)
               {
                  log.warn("run: IdleRemover ignored unexpected runtime exception", e);
               }
               catch (Error e)
               {
                  log.warn("run: IdleRemover ignored unexpected error", e);
               }
            }
         }
      }
   }
}
