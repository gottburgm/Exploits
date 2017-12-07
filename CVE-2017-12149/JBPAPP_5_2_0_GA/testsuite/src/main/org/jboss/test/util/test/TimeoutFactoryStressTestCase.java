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
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutFactory;
import org.jboss.util.timeout.TimeoutTarget;

import EDU.oswego.cs.dl.util.concurrent.WaitableInt;

/**
 * TimeoutFactoryStressTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class TimeoutFactoryStressTestCase extends JBossTestCase
{
    WaitableInt count = new WaitableInt(0);
    TimeoutFactory factory = TimeoutFactory.getSingleton();
   
   public void testStress() throws Exception
   {
      Thread[] threads = new Thread[getThreadCount()];
      for (int i = 0; i < threads.length; ++i)
         threads[i] = new Thread(new MyRunnable(), "Test thread " + i);
      for (int i = 0; i < threads.length; ++i)
         threads[i].start();
      count.whenEqual(threads.length, null);
   }

   class MyRunnable implements Runnable
   {
      public void run()
      {
         factory.createTimeout(System.currentTimeMillis() + 10, instance);
      }
   }
   
   MyTimeout instance = new MyTimeout();
   
   class MyTimeout implements TimeoutTarget, Runnable
   {
      public void timedOut(Timeout timeout)
      {
         assertTrue(timeout != null);
         run();
      }

      public void run()
      {
         count.increment();
      }
   }

   /**
    * Create a new TimeoutFactoryStressTestCase.
    * 
    * @param name the test name
    */
   public TimeoutFactoryStressTestCase(String name)
   {
      super(name);
   }
}
