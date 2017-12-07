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
package org.jboss.test.jca.test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import org.jboss.test.util.ejb.EJBTestCase;

/**
 * Abstract concurrent stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 76092 $
 */
public class AbstractConcurrentStressTest extends EJBTestCase
{
   protected final Logger log = Logger.getLogger(getClass());
   
   private ArrayList done = new ArrayList();
   private int total;
   private Throwable failed = null;
   private AtomicInteger nextId = new AtomicInteger(0);
   
   public interface ConcurrentTestCallback
   {
      void finished() throws Throwable;
   }
   
   public void runConcurrentTest(ConcurrentRunnable[] runnables, ConcurrentTestCallback callback) throws Throwable
   {
      total = runnables.length;
      Thread[] threads = new Thread[total];
      for (int i = 0; i < total; ++i)
      {
         threads[i] = new Thread(runnables[i], getName() + "-" + runnables[i].id);
         threads[i].start();
      }
      for (int i = 0; i < total; ++i)
         threads[i].join();

      if (callback != null)
         callback.finished();
      
      if (failed != null)
         throw failed;
      
      for (int i = 0; i < total; ++i)
         runnables[i].doCheck();
   }
   
   public abstract class ConcurrentRunnable implements Runnable
   {
      private Throwable failure;
      protected int id;
      
      public ConcurrentRunnable()
      {
         id = nextId.incrementAndGet();
      }
      
      public abstract void doStart() throws Throwable;
      public abstract void doRun() throws Throwable;
      public abstract void doEnd() throws Throwable;
      public synchronized void setFailure(Throwable failure)
      {
         if (failure != null)
         {
            this.failure = failure;
            failed(failure);
            log.error("Error in " + this, failure);
         }
         if (failure instanceof RuntimeException)
            throw (RuntimeException) failure;
         else if (failure instanceof Error)
            throw (Error) failure;
         throw new RuntimeException(failure);
      }
      public void doCheck() throws Throwable
      {
         if (failure != null)
            throw failure;
      }
      
      public void run()
      {
         try
         {
            doStart();
         }
         catch (Throwable t)
         {
            setFailure(t);
         }
         waitDone();
         for (int i =0; i < getIterationCount(); ++i)
         {
            try
            {
               doRun();
            }
            catch (Throwable t)
            {
               setFailure(t);
               break;
            }
         }
         waitDone();
         try
         {
            doEnd();
         }
         catch (Throwable t)
         {
            setFailure(t);
         }
         waitDone();
      }
   }

   protected synchronized void failed(Throwable failure)
   {
      if (failed == null)
         failed = failure;
      for (int i = 0; i < done.size(); ++i)
         done.get(i).notify();
      done.clear();
   }
   
   protected synchronized void waitDone()
   {
      if (failed != null)
         return;
      if (done.size() < total - 1)
      {
         done.add(this);
         doWait();
      }
      else
      {
         for (int i = 0; i < done.size(); ++i)
            done.get(i).notify();
         done.clear();
      }
   }

   protected void doWait()
   {
      boolean interrupted = false;
      try
      {
         while (failed == null)
         {
            try
            {
               wait();
               return;
            }
            catch (InterruptedException e)
            {
               interrupted = true;
            }
         }
      }
      finally
      {
         if (interrupted)
            Thread.currentThread().interrupt();
      }
      
   }
   
   public AbstractConcurrentStressTest(String name)
   {
      super(name);
   }
}
