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

import javax.management.ObjectName;
import javax.transaction.RollbackException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.mbean.MTOperation;

/**
 * Multithreaded Tx JCA tests
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class MultiThreadedTxUnitTestCase extends JBossTestCase
{
   static String[] SIG = new String[] { String.class.getName(), new MTOperation[0][0].getClass().getName() };

   ObjectName mtMBean;

   public MultiThreadedTxUnitTestCase(String name)
   {
      super(name);

      try
      {
         mtMBean = new ObjectName("jboss.test:test=MultiThreadedTxUnitTestCase");
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
      Test t1 = getDeploySetup(MultiThreadedTxUnitTestCase.class, "mtjcatest.sar");
      Test t2 = getDeploySetup(t1, "mttestadapter-ds.xml");
      return getDeploySetup(t2, "jbosstestadapter.rar");
   }

   /**
    * Have thread0 control the tx and thread1 joining the tx.
    * Both threads get/enlist a connection and close it and
    * thread1 suspends the tx. Thread0 commits.
    */
   public void testEnlistConnsInSameTxButDifferentThreads() throws Exception
   {
      runTest(new MTOperation[][]
      {
            {
               // thread 0
               new MTOperation(MTOperation.CF_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 1),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),           
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1),               
               new MTOperation(MTOperation.TM_COMMIT)               
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.CF_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
               new MTOperation(MTOperation.TM_RESUME, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 2),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
               new MTOperation(MTOperation.TM_SUSPEND),                
               new MTOperation(MTOperation.XX_POST_SIGNAL, 999)                 
            }
      });
   }

   /**
    * Same like testEnlistConnsInSameTxButDifferentThreads()
    * but use a TrackByTx connection factory.
    */
   public void testEnlistConnsInSameTxButDifferentThreadsTrackByTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
            {
               // thread 0
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 1),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),           
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1),               
               new MTOperation(MTOperation.TM_COMMIT)               
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
               new MTOperation(MTOperation.TM_RESUME, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 2),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
               new MTOperation(MTOperation.TM_SUSPEND),                
               new MTOperation(MTOperation.XX_POST_SIGNAL, 999)                 
            }
      });
   }   

   /**
    * Same like testEnlistConnsInSameTxButDifferentThreadsTrackByTx()
    * but don't suspend in the second thread. It the transaction
    * reconsiliation policy of allowing only one associated thread
    * at commit time is active, we should get an exception.
    */
   public void testEnlistConnsInSameTxButDifferentThreadsTrackByTxDontSuspend() throws Exception
   {
      Boolean txSyncActive = new Boolean(false);
      try
      {
         ObjectName target = new ObjectName("jboss:service=TransactionSynchronization");
         txSyncActive = (Boolean)getServer().getAttribute(target, "EnforceOneThreadActiveAtCommit");
      }
      catch (Exception e)
      {
         // ignore
      }
      
      if (txSyncActive.booleanValue() == true)
      {      
         runTest(new MTOperation[][]
         {
            {
               // thread 0
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 1),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),           
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1),               
               new MTOperation(MTOperation.TM_COMMIT, -1, new RollbackException())               
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
               new MTOperation(MTOperation.TM_RESUME, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 2),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
               new MTOperation(MTOperation.XX_POST_SIGNAL, 999)                 
            }
         });
      }
      else
      {
         runTest(new MTOperation[][]
         {
            {
               // thread 0
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 1),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),           
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1),               
               new MTOperation(MTOperation.TM_COMMIT)               
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
               new MTOperation(MTOperation.TM_RESUME, 10),               
               new MTOperation(MTOperation.CF_GET_CONN, 2),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
               new MTOperation(MTOperation.XX_POST_SIGNAL, 999)                 
            }
         });         
      }
   }  
   
   /**
    * Have 3 threads getting connection both in the same
    * and in different transactions, or not tx.
    */
   public void testEnlistInSameOrDifferentOrNoTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 1),
            new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 888),   
            new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),   
            new MTOperation(MTOperation.TM_COMMIT)                
         }
         ,
         {
            // thread 1
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
            new MTOperation(MTOperation.TM_RESUME, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 2),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
            new MTOperation(MTOperation.TM_SUSPEND, 10),
            new MTOperation(MTOperation.TM_BEGIN, 20),               
            new MTOperation(MTOperation.CF_GET_CONN, 3),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 3),
            new MTOperation(MTOperation.TM_COMMIT),
            new MTOperation(MTOperation.XX_POST_SIGNAL, 888)                    
         }
         ,
         {
            // thread 2
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
            new MTOperation(MTOperation.TM_RESUME, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 4),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 4),
            new MTOperation(MTOperation.TM_SUSPEND, 10),
            new MTOperation(MTOperation.CF_GET_CONN, 5),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 5),
            new MTOperation(MTOperation.XX_POST_SIGNAL, 999)                  
         }            
      });
   }
   
   /**
    * Enlist the first connection (which is tracked by tx)
    * in a different thread from  the one that originally
    * started the transaction. 
    */
   public void testEnlistConnInOtherThreadThanTxBegun() throws Exception
   {
      runTest(new MTOperation[][]
      {
            {
               // thread 0
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 10),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 888),
               new MTOperation(MTOperation.CF_GET_CONN, 2),
               new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
               new MTOperation(MTOperation.TM_COMMIT)                
            }
            ,
            {
               // thread 1
               new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),
               new MTOperation(MTOperation.TM_RESUME, 10),                
               new MTOperation(MTOperation.CF_GET_CONN, 1),
               new MTOperation(MTOperation.XX_POST_SIGNAL, 888),            
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1),
               new MTOperation(MTOperation.TM_SUSPEND, 10),               
               new MTOperation(MTOperation.XX_POST_SIGNAL, 999)
            }
      });
   }

   /**
    * Simple test to just show in the logs how connections
    * are reused when track-by-tx is true
    */
   public void testShowConnReuseTrackByTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 1),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 1),
            new MTOperation(MTOperation.CF_GET_CONN, 2),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
            new MTOperation(MTOperation.CF_GET_CONN, 3),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 3),
            new MTOperation(MTOperation.CF_GET_CONN, 4),
            new MTOperation(MTOperation.CF_GET_CONN, 5),
            new MTOperation(MTOperation.CF_GET_CONN, 6),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 6),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 5),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 4),
            new MTOperation(MTOperation.CF_GET_CONN, 7),
            new MTOperation(MTOperation.CF_GET_CONN, 8),
            new MTOperation(MTOperation.CF_GET_CONN, 9),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 7),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 8),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 9),            
            new MTOperation(MTOperation.TM_COMMIT)                
         }
      });      
   }
   
   /**
    * Simple test to just show in the logs how connections
    * are reused when track-by-tx is false
    */
   public void testShowConnReuse() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.CF_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 1),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 1),
            new MTOperation(MTOperation.CF_GET_CONN, 2),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 2),
            new MTOperation(MTOperation.CF_GET_CONN, 3),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 3),
            new MTOperation(MTOperation.CF_GET_CONN, 4),
            new MTOperation(MTOperation.CF_GET_CONN, 5),
            new MTOperation(MTOperation.CF_GET_CONN, 6),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 6),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 5),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 4),
            new MTOperation(MTOperation.CF_GET_CONN, 7),
            new MTOperation(MTOperation.CF_GET_CONN, 8),
            new MTOperation(MTOperation.CF_GET_CONN, 9),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 7),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 8),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 9),            
            new MTOperation(MTOperation.TM_COMMIT)                
         }
      });      
   }
   
   /**
    * Close the connection inside a different transaction
    */
   public void testCloseConnectionInDifferentTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.CF_GET_CONN, 1),
            new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),               
            new MTOperation(MTOperation.TM_COMMIT)                
         }
         ,
         {
            // thread 1
            new MTOperation(MTOperation.TM_BEGIN, 20),                 
            new MTOperation(MTOperation.XX_WAIT_FOR_CONN, 1),
            new MTOperation(MTOperation.CN_CLOSE_CONN, 1),
            new MTOperation(MTOperation.XX_POST_SIGNAL, 999),
            new MTOperation(MTOperation.TM_COMMIT)                
         }
      });
   }
   
   /**
    * Thread0 begins a tx, creates a connection and waits.
    * N Threads resume thead0 tx and create and destroy 3
    * connections each. Thread0 waits for them and commits the tx.
    */
   public void testStressConnsMultipleThreadsInSameTx() throws Exception
   {
      final int numThreads = 60;
      
      MTOperation[][] stressTest = new MTOperation[numThreads + 1][];

      // thread 0
      MTOperation[] thread0 = new MTOperation[5 + numThreads];
      thread0[0] = new MTOperation(MTOperation.CF_BY_TX_LOOKUP);
      thread0[1] = new MTOperation(MTOperation.TM_BEGIN, 10);
      thread0[2] = new MTOperation(MTOperation.CF_GET_CONN, 0);
      for (int i = 0; i < numThreads; i++)
      {
         thread0[3+i] = new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, i+1);
      }
      thread0[3 + numThreads] = new MTOperation(MTOperation.CN_CLOSE_CONN, 0);
      thread0[4 + numThreads] = new MTOperation(MTOperation.TM_COMMIT);

      stressTest[0] = thread0;
      
      // threads 1 -> numThreads
      for (int i = 1; i <= numThreads; i++)
      {
         stressTest[i] = new MTOperation[] {
            new MTOperation(MTOperation.CF_BY_TX_LOOKUP),
            new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),
            new MTOperation(MTOperation.TM_RESUME, 10),
            new MTOperation(MTOperation.CF_GET_CONN, 1000+i),
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),
            new MTOperation(MTOperation.CF_GET_CONN, 2000+i),
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),
            new MTOperation(MTOperation.CF_GET_CONN, 3000+i),
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),       
            new MTOperation(MTOperation.CN_CLOSE_CONN, 3000+i),
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
            new MTOperation(MTOperation.CN_CLOSE_CONN, 2000+i),
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
            new MTOperation(MTOperation.CN_CLOSE_CONN, 1000+i),
            new MTOperation(MTOperation.TM_SUSPEND, 10),
            new MTOperation(MTOperation.XX_POST_SIGNAL, i)
         };
      }
      runTest(stressTest);
   }
   
   /**
    * Create multiple threads that get and close connections
    * within different transactions.
    */
   public void testStressMultipleThreadsDifferentTx() throws Exception
   {
      final int numThreads = 60;
      
      MTOperation[][] stressTest = new MTOperation[numThreads][];

      // threads 0 -> numThreads
      for (int i = 0; i < numThreads; i++)
      {
         stressTest[i] = new MTOperation[] {
               new MTOperation(MTOperation.CF_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 500+i),               
               new MTOperation(MTOperation.CF_GET_CONN, 1000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               // Uncomment to following to run out of connections!
               // new MTOperation(MTOperation.CF_GET_CONN, 2000+i),
               // new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               // new MTOperation(MTOperation.CN_CLOSE_CONN, 2000+i),
               // new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.CN_CLOSE_CONN, 1000+i),
               new MTOperation(MTOperation.TM_COMMIT)
         };
      }
      runTest(stressTest);
   }     
}
