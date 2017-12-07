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

import javax.transaction.Transaction;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * A simple transaction local stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class SimpleNoLockTransactionLocalStressTestCase extends AbstractTransactionLocalStressTest
{
   public void testSimpleTransactionLocalStressTestcase() throws Throwable
   {
      tm.setTransactionTimeout(0);
      tm.begin();
      final Transaction tx = tm.suspend();
      SimpleTransactionLocalRunnable[] runnables = new SimpleTransactionLocalRunnable[getThreadCount()];
      for (int i = 0; i < runnables.length; ++i)
         runnables[i] = new SimpleTransactionLocalRunnable(tx);

      runConcurrentTest(runnables, new ConcurrentTestCallback()
            {
               public void finished() throws Throwable
               {
                  tm.resume(tx);
                  tm.commit();
               }
            });
   }
   
   public class SimpleTransactionLocalRunnable extends ConcurrentTransactionLocalRunnable
   {
      public SimpleTransactionLocalRunnable(Transaction tx)
      {
         super(tx);
      }

      public void doRun()
      {
         try
         {
            local.set(this);
            local.get();
         }
         catch (Throwable t)
         {
            failure = t;
         }
      }
   }
   
   public SimpleNoLockTransactionLocalStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(SimpleNoLockTransactionLocalStressTestCase.class, "transaction-test.jar");
   }
}
