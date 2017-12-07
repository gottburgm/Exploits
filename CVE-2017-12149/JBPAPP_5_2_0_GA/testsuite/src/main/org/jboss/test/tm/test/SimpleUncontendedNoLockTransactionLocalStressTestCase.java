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
public class SimpleUncontendedNoLockTransactionLocalStressTestCase extends AbstractTransactionLocalStressTest
{
   public void testSimpleTransactionLocalStressTestcase() throws Throwable
   {
      SimpleTransactionLocalRunnable[] runnables = new SimpleTransactionLocalRunnable[getThreadCount()];
      for (int i = 0; i < runnables.length; ++i)
         runnables[i] = new SimpleTransactionLocalRunnable();

      runConcurrentTest(runnables, null);
   }
   
   public class SimpleTransactionLocalRunnable extends ConcurrentRunnable
   {
      Transaction tx;
      
      public void doStart()
      {
         try
         {
            tm.setTransactionTimeout(0);
            tm.begin();
            tx = tm.getTransaction();
         }
         catch (Throwable t)
         {
            failure = t;
         }
      }
      public void doEnd()
      {
         try
         {
            tm.commit();
         }
         catch (Throwable t)
         {
            failure = t;
         }
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
   
   public SimpleUncontendedNoLockTransactionLocalStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(SimpleUncontendedNoLockTransactionLocalStressTestCase.class, "transaction-test.jar");
   }
}
