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

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.mbean.MTOperation;

/**
 * Multithreaded Tx JCA tests
 * over DefaultDS
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class MultiThreadedTxDsUnitTestCase extends JBossTestCase
{
   static String[] SIG = new String[] { String.class.getName(), new MTOperation[0][0].getClass().getName() };

   ObjectName mtMBean;

   public MultiThreadedTxDsUnitTestCase(String name)
   {
      super(name);

      try
      {
         // we can share the same target mbean with the
         // sister MultiThreadedTxUnitTestCase test
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
      Test t1 = getDeploySetup(MultiThreadedTxDsUnitTestCase.class, "mtjcatest.sar");
      Test t2 = getDeploySetup(t1, "testdriver-ds.xml");
      return getDeploySetup(t2, "jbosstestdriver.jar");
   }

   /**
    * Have two threads getting/closing connections within the same tx.
    * First thread begins/commits the tx. Use DefaultDS.
    */
   public void testTwoThreadsEnlistConnsInSameTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.DS_GET_CONN, 1),
            new MTOperation(MTOperation.DS_GET_CONN, 2),
            new MTOperation(MTOperation.DS_GET_CONN, 3),            
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 3),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 2),               
            new MTOperation(MTOperation.DS_CLOSE_CONN, 1),
            new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),            
            new MTOperation(MTOperation.TM_COMMIT)               
         }
         ,
         {
            // thread 1
            new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
            new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),               
            new MTOperation(MTOperation.TM_RESUME, 10),
            new MTOperation(MTOperation.DS_GET_CONN, 4),
            new MTOperation(MTOperation.DS_GET_CONN, 5),
            new MTOperation(MTOperation.DS_GET_CONN, 6),            
            new MTOperation(MTOperation.XX_SLEEP_RANDOM),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 6),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 5),               
            new MTOperation(MTOperation.DS_CLOSE_CONN, 4),
            new MTOperation(MTOperation.TM_SUSPEND, 10),
            new MTOperation(MTOperation.XX_POST_SIGNAL, 999)            
         }
      });
   }
   
   /**
    * Try to close a connection enlisted in one transaction
    * inside a different tx, and in the original tx, too.
    */
   public void testCloseConnTwiceInDifferentTx() throws Exception
   {
      runTest(new MTOperation[][]
      {
         {
            // thread 0
            new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 10),               
            new MTOperation(MTOperation.DS_GET_CONN, 1),
            new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, 999),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 1),            
            new MTOperation(MTOperation.TM_COMMIT)               
         }
         ,
         {
            // thread 1
            new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
            new MTOperation(MTOperation.TM_BEGIN, 20),            
            new MTOperation(MTOperation.XX_WAIT_FOR_CONN, 1),
            new MTOperation(MTOperation.DS_CLOSE_CONN, 1),
            new MTOperation(MTOperation.TM_COMMIT),
            new MTOperation(MTOperation.XX_POST_SIGNAL, 999)            
         }
      });
   }
   
   /**
    * Thread0 begins a tx, creates a connection and waits.
    * N Threads resume thead0 tx and create and destroy 3
    * connections each. Thread0 waits for them and commits the tx.
    * The DefaultDS is used.
    */
   public void testStressConnsMultipleThreadsInSameTx() throws Exception
   {
      final int numThreads = 60;
      
      MTOperation[][] stressTest = new MTOperation[numThreads + 1][];

      // thread 0
      MTOperation[] thread0 = new MTOperation[5 + numThreads];
      thread0[0] = new MTOperation(MTOperation.DS_DEFAULT_LOOKUP);
      thread0[1] = new MTOperation(MTOperation.TM_BEGIN, 10);
      thread0[2] = new MTOperation(MTOperation.DS_GET_CONN, 0);
      for (int i = 0; i < numThreads; i++)
      {
         thread0[3+i] = new MTOperation(MTOperation.XX_WAIT_FOR_SIGNAL, i+1);
      }
      thread0[3 + numThreads] = new MTOperation(MTOperation.DS_CLOSE_CONN, 0);
      thread0[4 + numThreads] = new MTOperation(MTOperation.TM_COMMIT);

      stressTest[0] = thread0;
      
      // threads 1 -> numThreads
      for (int i = 1; i <= numThreads; i++)
      {
         stressTest[i] = new MTOperation[] {
               new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
               new MTOperation(MTOperation.XX_WAIT_FOR_TX, 10),
               new MTOperation(MTOperation.TM_RESUME, 10),
               new MTOperation(MTOperation.DS_GET_CONN, 1000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 2000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 3000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),       
               new MTOperation(MTOperation.DS_CLOSE_CONN, 3000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
               new MTOperation(MTOperation.DS_CLOSE_CONN, 2000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
               new MTOperation(MTOperation.DS_CLOSE_CONN, 1000+i),
               new MTOperation(MTOperation.TM_SUSPEND, 10),
               new MTOperation(MTOperation.XX_POST_SIGNAL, i)
         };
      }
      runTest(stressTest);
   }
   
   /**
    * Create multiple threads that get and close connections
    * within different transactions. The DefaultDS is used.
    */
   public void testStressMultipleThreadsDifferentTx() throws Exception
   {
      final int numThreads = 60;
      
      MTOperation[][] stressTest = new MTOperation[numThreads][];

      // threads 0 -> numThreads
      for (int i = 0; i < numThreads; i++)
      {
         stressTest[i] = new MTOperation[] {
               new MTOperation(MTOperation.DS_DEFAULT_LOOKUP),
               new MTOperation(MTOperation.TM_BEGIN, 500+i),               
               new MTOperation(MTOperation.DS_GET_CONN, 1000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 2000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 3000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 4000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_GET_CONN, 5000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_CLOSE_CONN, 5000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),
               new MTOperation(MTOperation.DS_CLOSE_CONN, 4000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
               new MTOperation(MTOperation.DS_CLOSE_CONN, 3000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
               new MTOperation(MTOperation.DS_CLOSE_CONN, 2000+i),
               new MTOperation(MTOperation.XX_SLEEP_RANDOM),                
               new MTOperation(MTOperation.DS_CLOSE_CONN, 1000+i),
               new MTOperation(MTOperation.TM_COMMIT)
         };
      }
      runTest(stressTest);
   }   
}
