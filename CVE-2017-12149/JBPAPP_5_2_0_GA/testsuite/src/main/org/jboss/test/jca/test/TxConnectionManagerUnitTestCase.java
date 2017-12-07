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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.ConnectionListener;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool.BasePool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TxUtils;

import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnection;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;

/**
 * Unit test for class TxConnectionManager
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 85945 $
 */
public class TxConnectionManagerUnitTestCase extends EJBTestCase
{
   private static final Logger log = Logger.getLogger(TxConnectionManagerUnitTestCase.class);
   private Subject subject = new Subject();
   private ConnectionRequestInfo cri = new TestConnectionRequestInfo();
   private CachedConnectionManager ccm = new CachedConnectionManager();
   private TestManagedConnectionFactory mcf;
   private TxConnectionManager cm;
   private TransactionManager tm;
   private int txTimeout;

   /**
    * Creates a new <code>TxConnectionManagerUnitTestCase</code> instance.
    * @param name test name
    */
   public TxConnectionManagerUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TxConnectionManagerUnitTestCase("testAllocateConnection"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testConnectionEventListenerConnectionClosed"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testSynchronizationAfterCompletion"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testSynchronizationAfterCompletionTxTimeout"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testGetManagedConnection"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testGetManagedConnectionTimeout"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testGetManagedConnectionTrackByTx"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testGetManagedConnectionTimeoutTrackByTx"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testConnectionError"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testConnectionErrorTrackByTx"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testSimulateConnectionError"));
      suite.addTest(new TxConnectionManagerUnitTestCase("testSimulateConnectionErrorTrackByTx"));

      return JBossTestCase.getDeploySetup(suite, "jca-tests.jar");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      tm = TransactionManagerLocator.getInstance().locate();

      mcf = new TestManagedConnectionFactory();

      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();

      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);

      cm = new TxConnectionManager(ccm, poolingStrategy, tm);

      poolingStrategy.setConnectionListenerFactory(cm);

      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName onTM = new ObjectName("jboss:service=TransactionManager");

      txTimeout = ((Integer)server.getAttribute(onTM, "TransactionTimeout")).intValue();
   }

   @Override
   protected void tearDown() throws Exception
   {
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool)cm.getPoolingStrategy();
      pool.shutdown();

      tm.setTransactionTimeout(txTimeout);
      tm = null;
      cm = null;
      mcf = null;

      super.tearDown();
   }

   /**
    * Test that a connection can be allocated
    * @exception Exception If an error occurs
    */
   public void testAllocateConnection() throws Exception
   {
      log.info("----------------------");
      log.info("testAllocateConnection");
      log.info("----------------------");

      tm.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         try
         {
            assertTrue("Connection not enlisted in tx!", c.isInTx());
         }
         finally
         {
            c.close();
         }
      }
      finally
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            if (TxUtils.isActive(tx))
               tm.commit();
            else
               tm.rollback();
         }
         else
            fail("Transaction is null");
      }
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   /**
    * Test
    * @exception Exception If an error occurs
    */
   public void testConnectionEventListenerConnectionClosed() throws Exception
   {
      log.info("-------------------------------------------");
      log.info("testConnectionEventListenerConnectionClosed");
      log.info("-------------------------------------------");

      tm.begin();
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         c.close();
      }
      finally
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            if (TxUtils.isActive(tx))
               tm.commit();
            else
               tm.rollback();
         }
         else
            fail("Transaction is null");
      }
   }

   /**
    * Test
    * @exception Exception If an error occurs
    */
   public void testSynchronizationAfterCompletion() throws Exception
   {
      log.info("----------------------------------");
      log.info("testSynchronizationAfterCompletion");
      log.info("----------------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      tm.begin();
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         c.close();
      }
      finally
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            if (TxUtils.isActive(tx))
               tm.commit();
            else
               tm.rollback();
         }
         else
            fail("Transaction is null");
      }
   }

   /**
    * Test
    * @exception Exception If an error occurs
    */
   public void testSynchronizationAfterCompletionTxTimeout() throws Exception
   {
      log.info("-------------------------------------------");
      log.info("testSynchronizationAfterCompletionTxTimeout");
      log.info("-------------------------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      tm.setTransactionTimeout(2);

      TestConnection c = null;
      try
      {
         tm.begin();

         assertEquals("1", 0, cm.getPoolingStrategy().getInUseConnectionCount());

         c = (TestConnection)cm.allocateConnection(mcf, cri);
         
         assertEquals("2", 1, cm.getPoolingStrategy().getInUseConnectionCount());
         
         Thread.sleep(2500L);
         
         Transaction tx = tm.getTransaction();
         if (tx != null && TxUtils.isActive(tx))
            fail("TX is still active");
         
         c.close();
         c = null;
         
         assertEquals("3", 0, cm.getPoolingStrategy().getInUseConnectionCount());
      }
      finally
      {
         if (c != null)
         {
            c.close();
            fail("Connection wasnt closed");
         }
         
         assertNotNull(tm);
            
         if (!TxUtils.isCompleted(tm))
         {
            tm.rollback();
            fail("Tx was still active");
         }
      }
   }

   /**
    * Test that a connection can be obtained from getManagedConnection
    * and enlisted/delisted in a transaction
    * @exception Exception If an error occurs
    */
   public void testGetManagedConnection() throws Exception
   {
      log.info("------------------------");
      log.info("testGetManagedConnection");
      log.info("------------------------");


      ConnectionListener cl = null;
      try
      {
         assertNotNull(tm);
         tm.begin();

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());

         cl = cm.getManagedConnection(subject, cri);

         assertEquals(1, cm.getPoolingStrategy().getInUseConnectionCount());
         
         assertNotNull(cl);
         cl.enlist();
         
         assertNotNull(tm);
         tm.commit();
         
         cl.delist();
         cm.returnManagedConnection(cl, false);
         cl = null;

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());
      }
      finally
      {
         if (cl != null)
         {
            cm.returnManagedConnection(cl, true);
            fail("ConnectionListener wasnt returned to the pool");
         }
         
         assertNotNull(tm);
            
         if (TxUtils.isUncommitted(tm))
         {
            tm.rollback();
         }
      }
   }

   /**
    * Test that a connection can be obtained from getManagedConnection
    * and enlisted in a transaction that suffer timeout
    * @exception Exception If an error occurs
    */
   public void testGetManagedConnectionTimeout() throws Exception
   {
      log.info("-------------------------------");
      log.info("testGetManagedConnectionTimeout");
      log.info("-------------------------------");


      ConnectionListener cl = null;
      try
      {
         assertNotNull(tm);
         tm.setTransactionTimeout(2);
         tm.begin();

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());

         cl = cm.getManagedConnection(subject, cri);

         assertEquals(1, cm.getPoolingStrategy().getInUseConnectionCount());
         
         assertNotNull(cl);
         cl.enlist();
         
         Thread.sleep(2500L);

         cl.delist();
         cm.returnManagedConnection(cl, false);

         cl = null;

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());
      }
      finally
      {
         if (cl != null)
         {
            cm.returnManagedConnection(cl, true);
            fail("ConnectionListener wasnt returned to the pool");
         }
         
         assertNotNull(tm);
            
         if (!TxUtils.isCompleted(tm))
         {
            tm.rollback();
            fail("Tx was still active");
         }
      }
   }

   /**
    * Test that a connection can be obtained from getManagedConnection
    * and enlisted/delisted in a transaction with track-by-tx enabled
    * @exception Exception If an error occurs
    */
   public void testGetManagedConnectionTrackByTx() throws Exception
   {
      log.info("---------------------------------");
      log.info("testGetManagedConnectionTrackByTx");
      log.info("---------------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      ConnectionListener cl = null;
      try
      {
         assertNotNull(tm);
         tm.begin();

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());

         cl = cm.getManagedConnection(subject, cri);

         assertEquals(1, cm.getPoolingStrategy().getInUseConnectionCount());

         assertNotNull(cl);
         cl.enlist();
         
         assertNotNull(tm);
         tm.commit();
         
         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());
         
         cl = null;
      }
      finally
      {
         if (cl != null)
         {
            cm.returnManagedConnection(cl, true);
            fail("ConnectionListener wasnt returned to the pool");
         }
         
         assertNotNull(tm);
            
         if (TxUtils.isUncommitted(tm))
         {
            tm.rollback();
         }
      }
   }

   /**
    * Test that a connection can be obtained from getManagedConnection
    * and enlisted in a transaction that suffer timeout with track-by-tx enabled
    * @exception Exception If an error occurs
    */
   public void testGetManagedConnectionTimeoutTrackByTx() throws Exception
   {
      log.info("----------------------------------------");
      log.info("testGetManagedConnectionTimeoutTrackByTx");
      log.info("----------------------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      ConnectionListener cl = null;
      try
      {
         assertNotNull(tm);
         tm.setTransactionTimeout(2);
         tm.begin();

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());

         cl = cm.getManagedConnection(subject, cri);

         assertEquals(1, cm.getPoolingStrategy().getInUseConnectionCount());

         assertNotNull(cl);
         cl.enlist();

         Thread.sleep(2500L);

         assertEquals(0, cm.getPoolingStrategy().getInUseConnectionCount());
         
         cl = null;
      }
      finally
      {
         if (cl != null)
         {
            cm.returnManagedConnection(cl, true);
            fail("ConnectionListener wasnt returned to the pool");
         }
         
         assertNotNull(tm);
            
         if (!TxUtils.isCompleted(tm))
         {
            tm.rollback();
            fail("Tx was still active");
         }
      }
   }

   /**
    * Test a connection error
    * @exception Exception If an error occurs
    */
   public void testConnectionError() throws Exception
   {
      log.info("-------------------");
      log.info("testConnectionError");
      log.info("-------------------");

      tm.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      c.fireConnectionError();
      try
      {
         c.close();
      }
      catch (Exception ignored)
      {
      }

      try
      {
         tm.commit();
         fail("Should not be here");
      }
      catch (RollbackException expected)
      {
      }

      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   /**
    * Test a connection error with track-by-tx enabled
    * @exception Exception If an error occurs
    */
   public void testConnectionErrorTrackByTx() throws Exception
   {
      log.info("----------------------------");
      log.info("testConnectionErrorTrackByTx");
      log.info("----------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      tm.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      c.fireConnectionError();
      try
      {
         c.close();
      }
      catch (Exception ignored)
      {
      }

      try
      {
         tm.commit();
         fail("Should not be here");
      }
      catch (RollbackException expected)
      {
      }

      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   /**
    * Test a connection error
    * @exception Exception If an error occurs
    */
   public void testSimulateConnectionError() throws Exception
   {
      log.info("---------------------------");
      log.info("testSimulateConnectionError");
      log.info("---------------------------");

      tm.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);

      try
      {
         c.simulateConnectionError();
         fail("No exception thrown");
      }
      catch (Exception expected)
      {
      }

      c.close();

      try
      {
         tm.commit();
         fail("Should not be here");
      }
      catch (RollbackException expected)
      {
      }

      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   /**
    * Test a connection error with track-by-tx enabled
    * @exception Exception If an error occurs
    */
   public void testSimulateConnectionErrorTrackByTx() throws Exception
   {
      log.info("------------------------------------");
      log.info("testSimulateConnectionErrorTrackByTx");
      log.info("------------------------------------");

      // track-by-tx = true
      cm.setTrackConnectionByTx(true);

      tm.begin();
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);

      try
      {
         c.simulateConnectionError();
         fail("No exception thrown");
      }
      catch (Exception expected)
      {
      }

      c.close();

      try
      {
         tm.commit();
         fail("Should not be here");
      }
      catch (RollbackException expected)
      {
      }

      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }
}
