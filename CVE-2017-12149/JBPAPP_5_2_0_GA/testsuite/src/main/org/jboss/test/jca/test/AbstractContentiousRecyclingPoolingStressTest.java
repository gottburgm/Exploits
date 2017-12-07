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
package org.jboss.test.jca.test;

import javax.transaction.Status;
import javax.transaction.Transaction;

/**
 * Abstract contentious pooling stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class AbstractContentiousRecyclingPoolingStressTest extends AbstractPoolingStressTest
{
   TransactionState[] txStates;
   
   public void runConcurrentTest(ConcurrentRunnable[] runnables, ConcurrentTestCallback callback) throws Throwable
   {
      // HACK: The default config won't tweak this test
      int transactions = getBeanCount();
      int threads = getThreadCount();
      if (transactions >= threads)
         transactions = threads / 2;
      if (transactions == 0)
         transactions = 1;
      
      txStates = new TransactionState[transactions];
      for (int i = 0; i < txStates.length; ++i)
         txStates[i] = new TransactionState();
      super.runConcurrentTest(runnables, callback);
   }

   public abstract class ContentiousRecyclingPoolingRunnable extends ConcurrentRunnable
   {
      TransactionState txState;
      
      public ContentiousRecyclingPoolingRunnable()
      {
      }

      public void doStart() throws Throwable
      {
         txState = txStates[id % txStates.length];
      }

      public void doEnd() throws Throwable
      {
      }

      public void doRun() throws Throwable
      {
         waitDone();
         txState.begin();
         try
         {
            doRun1();
         }
         catch (Throwable t)
         {
            setFailure(t);
            tm.setRollbackOnly();
            throw t;
         }
         finally
         {
            tm.suspend();
            waitDone();
            txState.commit();
         }
      }
      
      public abstract void doRun1() throws Throwable;
   }

   public class TransactionState
   {
      Transaction tx;

      public TransactionState()
      {
      }
      
      public synchronized void begin() throws Throwable
      {
         if (tx == null)
         {
            tm.begin();
            tx = tm.getTransaction();
         }
         else
         {
            tm.resume(tx);
         }
      }
      
      public synchronized void commit() throws Throwable
      {
         if (tx != null)
         {
            tm.resume(tx);
            if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK)
               tm.rollback();
            else
               tm.commit();
            tx = null;
         }
      }
   }
   
   public AbstractContentiousRecyclingPoolingStressTest(String name)
   {
      super(name);
   }
}
