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

import javax.management.ObjectName;
import javax.transaction.RollbackException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.tm.resource.MTOperation;

/**
 * Multithreaded tests for the transaction manager
 *
 * Based on TransactionManagerUnitTestCase
 *
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 89025 $
 */
public class MTTransactionManagerUnitTestCase extends JBossTestCase
{
   static String[] SIG = new String[] { String.class.getName(), new MTOperation[0][0].getClass().getName() };

   ObjectName mtMBean;

   public MTTransactionManagerUnitTestCase(String name)
   {
      super(name);

      try
      {
         mtMBean = new ObjectName("jboss.test:test=MTTransactionManagerUnitTestCase");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   public void runTest(MTOperation[][] ops) throws Exception
   {
      getServer().invoke(mtMBean, "testMTOperations", new Object[] { getName(), ops }, SIG);
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(MTTransactionManagerUnitTestCase.class, "mttest.sar"));
   }

   /**
    * Start a tx on one thread and commit it on another
    * withouth the 2nd thread actually associating itself
    * with the transaction.
    *
    * This is an error if the TransactionIntegrity plugin is active
    */
   public void testCommitTxStartedOnADifferentThread() throws Exception
   {
      if (isTransactionIntegrityActive())
      {
         runTest(new MTOperation[][]
           {
              {
                 // thread 0
                 new MTOperation(MTOperation.TM_BEGIN, 10),
                 new MTOperation(MTOperation.TM_GET_STATUS),
                 new MTOperation(MTOperation.XX_SLEEP_200), // other thread must commit
                 new MTOperation(MTOperation.TM_GET_STATUS)
              }
              ,
              {
                 // thread 1
                 new MTOperation(MTOperation.XX_WAIT_FOR, 10),
                 new MTOperation(MTOperation.TX_COMMIT, 10, new RollbackException()),
                 new MTOperation(MTOperation.TM_GET_STATUS),
              }
           });
      }
      else
      {
         runTest(new MTOperation[][]
         {
            {
               // thread 0
               new MTOperation(MTOperation.TM_BEGIN, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
               new MTOperation(MTOperation.XX_SLEEP_200), // other thread must commit
               new MTOperation(MTOperation.TM_GET_STATUS)
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.XX_WAIT_FOR, 10),
               new MTOperation(MTOperation.TX_COMMIT, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
            }
         });
      }
   }

   /**
    * Start a tx on one thread, then resume this tx and commit it from
    * another thread. Normally this is allowed, but if the
    * TransactionIntegrity policy is active, then the 2 threads associated
    * with the tx will be detected at commit time and an exception
    * will be thrown.
    */
   public void testResumeAndCommitTxStartedOnADifferentThread() throws Exception
   {
      if (isTransactionIntegrityActive())
      {
         runTest(new MTOperation[][]
         {
            {
               // thread 0
               new MTOperation(MTOperation.TM_BEGIN, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.TM_GET_STATUS),
               new MTOperation(MTOperation.XX_WAIT_FOR, 10),
               new MTOperation(MTOperation.TM_RESUME, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
               new MTOperation(MTOperation.TX_COMMIT, 10, new RollbackException()),
               new MTOperation(MTOperation.TM_GET_STATUS),
            }
         });
      }
      else
      {
         runTest(new MTOperation[][]
           {
              {
                 // thread 0
                 new MTOperation(MTOperation.TM_BEGIN, 10),
                 new MTOperation(MTOperation.TM_GET_STATUS),
              }
              ,
              {
                 // thread 1
                 new MTOperation(MTOperation.TM_GET_STATUS),
                 new MTOperation(MTOperation.XX_WAIT_FOR, 10),
                 new MTOperation(MTOperation.TM_RESUME, 10),
                 new MTOperation(MTOperation.TM_GET_STATUS),
                 new MTOperation(MTOperation.TX_COMMIT, 10),
                 new MTOperation(MTOperation.TM_GET_STATUS),
              }
        });
      }
   }

   /**
    * Start a tx on one thread and commit it on another thread
    * without the 2nd thread actually associating itself with
    * the transaction. The try to commit the tx on the 1st
    * thread as well, thus producing an exception.
    *
    * This only works when the TransactionIntegrity policy is innactive
    */
   public void testCommitSameTxInTwoThreads() throws Exception
   {
      if (!isTransactionIntegrityActive())
      {
         runTest(new MTOperation[][]
         {
            {
               // thread 0
               new MTOperation(MTOperation.TM_BEGIN, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
               new MTOperation(MTOperation.XX_SLEEP_200),
               new MTOperation(MTOperation.TM_GET_STATUS),

               // FIXME - JBTM-558
               new MTOperation(MTOperation.TM_COMMIT, -1, new RollbackException(), false)

            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.XX_WAIT_FOR, 10),
               new MTOperation(MTOperation.TX_COMMIT, 10),
               new MTOperation(MTOperation.TM_GET_STATUS),
            }
         });
      }
   }

   /**
    * Find out if the plugin is installed.
    *
    * This is specific to this particular plugin!
    */
   private boolean isTransactionIntegrityActive()
   {
      boolean isActive = false;
      try
      {
         ObjectName target = new ObjectName("jboss:service=TransactionManager,plugin=TransactionIntegrity");
         isActive = getServer().isRegistered(target);
      }
      catch (Exception ignore)
      {
         // empty
      }
      return isActive;
   }


}
