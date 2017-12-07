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
package org.jboss.test.util.test;

import org.jboss.test.JBossTestCase;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BlockingMode;
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutFactory;
import org.jboss.util.timeout.TimeoutTarget;

import EDU.oswego.cs.dl.util.concurrent.WaitableInt;

/**
 * Unit tests for TimeoutFactory class.
 *
 * @author  <a href="mailto:genman@noderunner.net">Elias Ross</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class TimeoutFactoryTestCase extends JBossTestCase
{
   public TimeoutFactoryTestCase(String name)
   {
      super(name);
   }

   WaitableInt count = new WaitableInt(0);

   public void testBlocking() throws Exception
   {
      final int times = 5000;
      TT tt = new TT();
      for (int i = 0; i < times; i++)
      {
         TimeoutFactory.createTimeout(0, tt);
      }
      count.whenEqual(times, null);
      assertEquals(times, count.get());
   }
   public void testDefaultCtr() throws Exception
   {
      final int times = 5000;
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (Runnable)tt);
      }
      count.whenEqual(times, null);
      assertEquals(times, count.get());
   }

   public void testConsecutiveTimeouts() throws Exception
   {
      final int times = 1000;      
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long now = System.currentTimeMillis();
      for (int i = 0; i < 10; i++)
      {
         for (int j = 0; j < 100; j++)
         {
            tf.schedule(now + i*50, (TimeoutTarget)tt);
         }
      }
      count.whenEqual(times, null);
      assertEquals(times, count.get());      
   }
   
   public void testCancel() throws Exception
   {
      final int times = 100;
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long at = System.currentTimeMillis() + 300;
      for (int i = 0; i < times; i++)
      {
         Timeout t = tf.schedule(at, (TimeoutTarget)tt);
         t.cancel();
      }
      Thread.sleep(500);
      assertEquals(0, count.get());
   }

   public void testCancelFactory() throws Exception
   {
      final int times = 100;
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory();
      long at = System.currentTimeMillis() + 300;
      for (int i = 0; i < times; i++)
      {
         tf.schedule(at, (TimeoutTarget)tt);
      }
      tf.cancel();
      Thread.sleep(500);
      assertEquals(0, count.get());
   }

   public void testBlockingSmallThreadPool() throws Exception
   {
      final int times = 100;
      BasicThreadPool tp = new BasicThreadPool();
      tp.setMaximumQueueSize(1);
      tp.setMaximumPoolSize(1);
      tp.setBlockingMode(BlockingMode.RUN);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory(tp);
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      count.whenEqual(times, null);
      assertEquals(times, count.get());
   }
   
   public void testAbortingSmallThreadPool() throws Exception
   {
      final int times = 50;
      BasicThreadPool tp = new BasicThreadPool();
      tp.setMaximumQueueSize(1);
      tp.setMaximumPoolSize(1);
      TT tt = new TT();
      TimeoutFactory tf = new TimeoutFactory(tp);
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      Thread.sleep(500);
      getLog().debug("Executed: " + count.get() + ", scheduled: " + times);
      assertTrue("Executed " + count.get() + " < scheduled " + times, count.get() < times);
   }
   
   public void testFailedTarget() throws Exception
   {
      final int times = 50;
      TimeoutFactory tf = new TimeoutFactory();           
      TT tt = new TT();
      tt.fail = true;
      for (int i = 0; i < times; i++)
      {
         tf.schedule(0, (TimeoutTarget)tt);
      }
      Thread.sleep(500);
      assertEquals(count.get(), 0);
   }

   class TT implements TimeoutTarget, Runnable
   {

      boolean fail;

      public void timedOut(Timeout timeout)
      {
         assertTrue(timeout != null);
         run();
      }

      public void run()
      {
         if (fail)
            throw new Error("Fail");
         
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException e)
         {
         }
         count.increment();
      }
   }
}
