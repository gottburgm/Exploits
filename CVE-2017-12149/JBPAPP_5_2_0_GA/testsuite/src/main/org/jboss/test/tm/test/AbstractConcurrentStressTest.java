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
package org.jboss.test.tm.test;

import java.util.ArrayList;

import org.jboss.test.util.ejb.EJBTestCase;

/**
 * Abstract concurrent stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class AbstractConcurrentStressTest extends EJBTestCase
{
   private ArrayList done = new ArrayList();
   private int total;
   
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
         threads[i] = new Thread(runnables[i], getName() + "-" + i);
         threads[i].start();
      }
      for (int i = 0; i < total; ++i)
         threads[i].join();

      if (callback != null)
         callback.finished();
      
      for (int i = 0; i < total; ++i)
         runnables[i].doCheck();
   }
   
   public abstract class ConcurrentRunnable implements Runnable
   {
      public Throwable failure;
      
      public abstract void doStart();
      public abstract void doRun();
      public abstract void doEnd();
      public void doCheck() throws Throwable
      {
         if (failure != null)
            throw failure;
      }
      
      public void run()
      {
         doStart();
         waitDone();
         for (int i =0; i < getIterationCount(); ++i)
            doRun();
         waitDone();
         doEnd();
         waitDone();
      }
   }

   protected synchronized void waitDone()
   {
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
         while (true)
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
