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
package org.jboss.test.jca.mbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.tm.TxUtils;

/**
 * MultiThreaded Operations that can be executed concurrently.
 * 
 * Based on Operation class.
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class MTOperation implements Serializable
{
   // Static Data ---------------------------------------------------
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   
   /** Available Operations */
   public static final int TM_GET_STATUS = 0;
   public static final int TM_BEGIN = 1;
   public static final int TM_SUSPEND = 2;
   public static final int TM_RESUME = 3;
   public static final int TM_COMMIT = 4;
   public static final int TX_COMMIT = 5;
   public static final int TX_REGISTER_SYNC = 6;
   
   public static final int CF_LOOKUP = 10;
   public static final int CF_BY_TX_LOOKUP = 11;
   public static final int CF_GET_CONN = 12;
   public static final int CN_CLOSE_CONN = 13;
   
   public static final int DS_TEST_LOOKUP = 15;
   public static final int DS_DEFAULT_LOOKUP = 16;
   public static final int DS_GET_CONN = 17;
   public static final int DS_CLOSE_CONN = 18;
   
   public static final int XX_SLEEP_200 = 20;
   public static final int XX_SLEEP_RANDOM = 21;
   public static final int XX_POST_SIGNAL = 22;
   public static final int XX_WAIT_FOR_SIGNAL = 23;
   public static final int XX_WAIT_FOR_TX = 24;
   public static final int XX_WAIT_FOR_CONN = 25;
   
   /** The Logger */
   protected static Logger log;

   /** TM instance */
   protected static TransactionManager tm = null;
   
   /** Shared connections */
   protected static Map connections = Collections.synchronizedMap(new HashMap());
   
   /** Active Transactions */
   protected static Map transactions = Collections.synchronizedMap(new HashMap());
   
   /** Used for signaling between threads */
   protected static Set signals = Collections.synchronizedSet(new HashSet());
   
   /** Shared reference to a connection factory */
   protected static ConnectionFactory cf = null;
   
   /**Shared reference to a DataSource */
   protected static DataSource ds = null;
   
   /** Set when the first unexpected throwable is encounter in any thread */
   protected static boolean testMarkedForExit;
   
   // Protected Data ------------------------------------------------
   
   /** An id for this transaction */
   protected Integer id;
   
   /** The operation to execute */
   protected int op;
   
   /** Set when an exception is expected */
   protected Throwable throwable;
   
   // Static Methods ------------------------------------------------
   
   /**
    * Setup static objects for the test
    */
   public static void init(Logger log) throws Exception
   {
      MTOperation.log = log;
      
      if (getTM().getTransaction() != null)
      {
         throw new IllegalStateException("Invalid thread association " + getTM().getTransaction());
      }
      connections.clear();
      transactions.clear();
      signals.clear();
      
      // clear the exit flag
      setTestMarkedForExit(false);
   }   

   /**
    * Lazy TransactionManager lookup
    */
   public static TransactionManager getTM() throws Exception
   {
      if (tm == null)
      {
         tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
      }
      return tm;
   }   
   
   /**
    * Cleanup
    */
   public static void destroy()
   {
      connections.clear();
      transactions.clear();
      signals.clear();
   }
   
   /**
    * Returns true if the test is marked for exit
    */
   public static boolean isTestMarkedForExit()
   {
      return testMarkedForExit;
   }
   
   /**
    * Tell the threads to exit
    */
   public static void setTestMarkedForExit(boolean testMarkedForExit)
   {
      MTOperation.testMarkedForExit = testMarkedForExit;
   }
   
   /**
    * Used by waiting threads to stop execution
    * 
    * @throws Exception if the test if marked to exit
    */
   public static void checkTestMarkedForExit() throws Exception
   {
      if (testMarkedForExit)
      {
         throw new MarkedForExitException();
      }
   }
   
   /**
    * Exception used for early existing
    */
   private static class MarkedForExitException extends Exception
   {
      // empty
   }
   
   // Constructors --------------------------------------------------
   
   public MTOperation(int op)
   {
      this(op, 0);
   }
   
   public MTOperation(int op, int id)
   {
      this.id = new Integer(id);
      this.op = op;
   }

   public MTOperation(int op, int id, Throwable throwable)
   {
      this.id = new Integer(id);
      this.op = op;
      this.throwable = throwable;
   }
   
   // Public Methods ------------------------------------------------
   
   public void perform() throws Exception
   {
      Throwable caught = null;
      try
      {
         switch (op)
         {
            case TM_GET_STATUS:
               tmGetStatus();
               break;
            
            case TM_BEGIN:
               tmBegin();
               break;
               
            case TM_SUSPEND:
               tmSuspend();
               break;
               
            case TM_RESUME:
               tmResume();
               break;
               
            case TM_COMMIT:
               tmCommit();
               break;
               
            case TX_COMMIT:
               txCommit();
               break;
               
            case TX_REGISTER_SYNC:
               txRegisterSync();
               break;
               
            case XX_SLEEP_200:
               xxSleep200();
               break;
               
            case XX_SLEEP_RANDOM:
               xxSleepRandom();
               break;
               
            case XX_POST_SIGNAL:
               xxPostSignal();
               break;
               
            case XX_WAIT_FOR_SIGNAL:
               xxWaitForSignal();
               break;
               
            case XX_WAIT_FOR_TX:
               xxWaitForTx();
               break;
               
            case XX_WAIT_FOR_CONN:
               xxWaitForConn();
               break;
               
            case CF_LOOKUP:
               cfLookup();
               break;
               
            case CF_BY_TX_LOOKUP:
               cfByTxLookup();
               break;
               
            case DS_TEST_LOOKUP:
               dsTestLookup();
               break;
               
            case DS_DEFAULT_LOOKUP:
               dsDefaultLookup();
               break;
               
            case DS_GET_CONN:
               dsGetConn();
               break;
               
            case DS_CLOSE_CONN:
               dsCloseConn();
               break;
               
            case CF_GET_CONN:
               cfGetConn();
               break;
               
            case CN_CLOSE_CONN:
               cnCloseConn();
               break;
               
            default:
               throw new IllegalArgumentException("Invalid operation " + op);
         }
      }
      catch (MarkedForExitException e)
      {
         log.info(tid() + "Early exit");
         return;
      }
      catch (Throwable t)
      {
         caught = t;
      }

      // expected an exception but caught none
      if (throwable != null && caught == null)
      {
         setTestMarkedForExit(true);
         throw new Exception("Expected throwable ", throwable);
      }
      
      // expected an exception but caught the wrong one
      if (throwable != null && (throwable.getClass().isAssignableFrom(caught.getClass())) == false)
      {
         log.warn("Caught wrong throwable", caught);
         setTestMarkedForExit(true);
         throw new Exception("Expected throwable " + throwable + " caught ", caught);
      }
      
      // did not expect an exception bug caught one
      if (throwable == null && caught != null)
      {
         log.warn("Caught unexpected throwable", caught);
         setTestMarkedForExit(true);
         throw new Exception("Unexpected throwable ", caught);
      }
   }

   public void cfLookup() throws Exception
   {
      log.info(tid() + " CF_LOOKUP");
      InitialContext ctx = new InitialContext();
      cf = (ConnectionFactory)ctx.lookup("java:JBossTestCF");
   }
   
   public void cfByTxLookup() throws Exception
   {
      log.info(tid() + " CF_BY_TX_LOOKUP");
      InitialContext ctx = new InitialContext();
      cf = (ConnectionFactory)ctx.lookup("java:JBossTestCFByTx");
   } 
    
   public void cfGetConn() throws Exception
   {
      log.info(tid() + " CF_GET_CONN (" + id + ")");
      Connection conn = cf.getConnection();
      connections.put(id, conn);
   }
   
   public void cnCloseConn() throws Exception
   {
      log.info(tid() + " CN_CLOSE_CONN (" + id + ")");
      Connection conn = (Connection)connections.get(id);
      conn.close();
   }
   
   public void dsTestLookup() throws Exception
   {
      log.info(tid() + " DS_TEST_LOOKUP");
      InitialContext ctx = new InitialContext();
      ds = (DataSource)ctx.lookup("java:StatementTestsConnectionDS");      
      
   }
   
   public void dsDefaultLookup() throws Exception
   {
      log.info(tid() + " DS_DEFAULT_LOOKUP");
      InitialContext ctx = new InitialContext();
      ds = (DataSource)ctx.lookup("java:DefaultDS");      
   }
   
   public void dsGetConn() throws Exception
   {
      log.info(tid() + " DS_GET_CONN (" + id + ")");
      java.sql.Connection conn = ds.getConnection();
      connections.put(id, conn);
   }
   
   public void dsCloseConn() throws Exception
   {
      log.info(tid() + " DS_CLOSE_CONN (" + id + ")");
      java.sql.Connection conn = (java.sql.Connection)connections.get(id);
      conn.close();
   }
   
   public void tmGetStatus() throws Exception
   {
      log.info(tid() + " " + TxUtils.getStatusAsString(getTM().getStatus()));      
   }

   public void tmBegin() throws Exception
   {
      log.info(tid() + " TM_BEGIN (" + id + ")");
      getTM().begin();
      Transaction tx = getTM().getTransaction();
      synchronized (transactions)
      {
         transactions.put(id, tx);
         transactions.notifyAll();
      }
   }
   
   public void tmSuspend() throws Exception
   {
      log.info(tid() + " TM_SUSPEND (" + id + ")");
      Transaction tx = getTM().suspend();
      transactions.put(id, tx);
   }
   
   public void tmResume() throws Exception
   {
      log.info(tid() + " TM_RESUME (" + id + ")");
      Transaction tx = (Transaction)transactions.get(id);
      if (tx == null)
      {
         throw new IllegalStateException("Tx not found:" + id);
      }
      else
      {
         getTM().resume(tx);
      }
   }
   
   public void tmCommit() throws Exception
   {
      log.info(tid() + " TM_COMMIT");
      getTM().commit();
   }
   
   public void txCommit() throws Exception
   {
      log.info(tid() + " TX_COMMIT (" + id + ")"); 
      Transaction tx = (Transaction)transactions.get(id);
      if (tx == null)
      {
         throw new IllegalStateException("Tx not found: " + id);
      }
      else
      {
         tx.commit();
      }
   }
   
   public void txRegisterSync() throws Exception
   {
      log.info(tid() + " TX_REGISTER_SYNC (" + id + ")");
      Transaction tx = (Transaction)transactions.get(id);
      if (tx == null)
      {
         throw new IllegalStateException("Tx not found: " + id);
      }
      Synchronization sync = new Synchronization()
      {
         public void beforeCompletion()
         {
            log.info(tid() + " beforeCompletion() called");
         }
         
         public void afterCompletion(int status)
         {
            log.info (tid() + " afterCompletion(" + TxUtils.getStatusAsString(status) + ") called");
         }         
      };
      tx.registerSynchronization(sync);
   }
   
   public void xxWaitForTx() throws Exception
   {
      log.info(tid() + " XX_WAIT_FOR_TX (" + id + ")");
      
      Transaction tx = (Transaction)transactions.get(id);
      while (tx == null)
      {
         checkTestMarkedForExit();
         
         log.info(tid() + " Sleeping for 100 msecs");
         
         synchronized (transactions)
         {
            try
            {
               transactions.wait(100);
            }
            catch (InterruptedException ignore) {}
         }
         tx = (Transaction)transactions.get(id);
      }
      log.info(tid() + " Got it");
   }
   
   public void xxWaitForConn() throws Exception
   {
      log.info(tid() + " XX_WAIT_FOR_CONN (" + id + ")");
      
      boolean contained = connections.containsKey(id);
      while (contained == false)
      {
         checkTestMarkedForExit();
         
         log.info(tid() + " Sleeping for 100 msecs");
         
         synchronized (connections)
         {
            try
            {
               connections.wait(100);
            }
            catch (InterruptedException ignore) {}
         }
         contained = connections.containsKey(id);
      }
      log.info(tid() + " Got it");      
   }
   
   public void xxSleep200() throws Exception
   {
      log.info(tid() + " XX_SLEEP_200");
      Thread.sleep(200);
   }
   
   public void xxSleepRandom() throws Exception
   {
      long random = Math.round((Math.random() * 100));
      log.info(tid() + " XX_SLEEP_RANDOM (" + random + ")");      
      Thread.sleep(random);
   }
   
   public void xxPostSignal() throws Exception
   {
      log.info(tid() + " XX_POST_SIGNAL (" + id + ")");
      synchronized (signals)
      {
         signals.add(id);
         signals.notifyAll();
      }
   }
   
   public void xxWaitForSignal() throws Exception
   {
      log.info(tid() + " XX_WAIT_FOR_SIGNAL (" + id + ")");
      
      boolean posted = signals.contains(id);
      while (posted == false)
      {
         checkTestMarkedForExit();
         
         log.info(tid() + " Signal not posted, waiting...");
         
         synchronized (signals)
         {
            try
            {
               signals.wait(100);
            }
            catch (InterruptedException ignore) {}
         }
         posted = signals.contains(id);
      }
      log.info(tid() + " Got it!");      
   }
   
   private String tid()
   {
      return Thread.currentThread().getName();
   }
   
}
