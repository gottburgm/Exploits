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

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.CachedConnectionManagerMBean;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnection;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TxUtils;

/**
 * XATxConnectionManagerUnitTestCase.java
 *
 *
 * Created: Mon Jan 14 00:43:40 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 81036 $
 */
public class XATxConnectionManagerUnitTestCase extends EJBTestCase
{
   Logger poolLog = Logger.getLogger(JBossManagedConnectionPool.class);
   Logger log = Logger.getLogger(getClass());

   private TransactionManager tm;
   private UserTransaction ut;
   private CachedConnectionManager ccm;
   private TestManagedConnectionFactory mcf;
   private TxConnectionManager cm;
   private ConnectionRequestInfo cri;

   private int poolSize = 5;

   public static Test suite() throws Exception
   {
      // JBAS-3603, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new XATxConnectionManagerUnitTestCase("testGetConnection"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testEnlistInExistingTx"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testEnlistCheckedOutConnectionInNewTx"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testReconnectConnectionHandlesOnNotification"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testEnlistAfterMarkRollback"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testBrokenConnectionAndTrackByTx"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testFailedStartTx"));
      suite.addTest(new XATxConnectionManagerUnitTestCase("testFailedEndTx"));

      return JBossTestCase.getDeploySetup(suite, "jca-tests.jar");
   }
   
   public XATxConnectionManagerUnitTestCase (String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      log.debug("================> Start " + getName());
      tm = TransactionManagerLocator.getInstance().locate();
      InitialContext ctx = new InitialContext();
      ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");

      mcf = new TestManagedConnectionFactory();
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = poolSize;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, poolLog);
      cri = new TestConnectionRequestInfo();
      cm = new TxConnectionManager(ccm, poolingStrategy, tm);
      cm.setLocalTransactions(false);
      cm.setInterleaving(true);
      poolingStrategy.setConnectionListenerFactory(cm);
   }

   protected void tearDown() throws Exception
   {
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      pool.shutdown();
      ut = null;
      log.debug("================> End " + getName());
      super.tearDown();
   }

   public void testGetConnection() throws Exception
   {
      log.info("testGetConnection");
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertTrue("Connection is null", c != null);
      c.close();
   }

   public void testEnlistInExistingTx() throws Exception
   {
      log.info("testEnlistInExistingTx");
      ut.begin();
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
         assertTrue("Connection still enlisted in tx!", !c.isInTx());
      }
      finally
      {
         if (TxUtils.isActive(ut))
            ut.commit();
         else
            ut.rollback();
      }
      assertTrue("Connection still enlisted in tx!", !c.isInTx());
   }

   public void testEnlistCheckedOutConnectionInNewTx() throws Exception
   {
      log.info("testEnlistCheckedOutConnectionInNewTx");
      Object key = this;
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key, unshared);
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         try
         {
            assertTrue("Connection already enlisted in tx!", !c.isInTx());
            ut.begin();
            try
            {
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               if (TxUtils.isActive(ut))
                  ut.commit();
               else
                  ut.rollback();
            }
            assertTrue("Connection still enlisted in tx!", !c.isInTx());
         }
         finally
         {
            c.close();
         }
      }
      finally
      {
         ccm.popMetaAwareObject(unshared);
         
      }
   }

   /** Tests the spec required behavior of reconnecting connection
    * handles left open on return from an ejb method call.  Since this
    * behavior is normally turned off, we must set SpecCompliant on
    * the ccm to true first.
    */
   public void testReconnectConnectionHandlesOnNotification() throws Exception
   {
      log.info("testReconnectConnectionHandlesOnNotification");
      ccm.setSpecCompliant(true);
      Object key1 = new Object();
      Object key2 = new Object();
      Set unshared = new HashSet();
      ccm.pushMetaAwareObject(key1, unshared);
      try
      {
         TestConnection c = null;
         ut.begin();
         try
         {
            ccm.pushMetaAwareObject(key2, unshared);
            try
            {
               c = (TestConnection)cm.allocateConnection(mcf, cri);
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               ccm.popMetaAwareObject(unshared);//key2
            }
         }
         finally
         {
            if (TxUtils.isActive(ut))
               ut.commit();
            else
               ut.rollback();
         }
         ut.begin();
         try
         {
            ccm.pushMetaAwareObject(key2, unshared);
            try
            {
               assertTrue("Connection not enlisted in tx!", c.isInTx());
            }
            finally
            {
               ccm.popMetaAwareObject(unshared);//key2
            }
         }
         finally
         {
            if (TxUtils.isActive(ut))
               ut.commit();
            else
               ut.rollback();
         }
         assertTrue("Connection still enlisted in tx!", !c.isInTx());
         ccm.pushMetaAwareObject(key2, unshared);
         try
         {
            if (c != null)
               c.close();
         }
         finally
         {
            ccm.popMetaAwareObject(unshared);//key2
         }
      }
      finally
      {
         ccm.popMetaAwareObject(unshared);//key1
      }
  }

  public void testEnlistAfterMarkRollback() throws Exception
  {
     // Get a transaction and mark it for rollback
     tm.begin();
     try
     {
        tm.setRollbackOnly();
        // Allocate a connection upto the pool size all should fail
        for (int i = 0; i < poolSize; ++i)
        {
           try
           {
              cm.allocateConnection(mcf, cri);
              fail("Should not be allowed to allocate a connection with setRollbackOnly()");
           }
           catch (Exception e)
           {
              log.debug("Error allocating connection", e);
           }
        }
     }
     finally
     {
        tm.rollback();
     }

     // We should be able to get a connection now
     testGetConnection();
  }

  public void testBrokenConnectionAndTrackByTx() throws Exception
  {
     log.info("testBrokenConnectionAndTrackByTx");
     cm.setTrackConnectionByTx(true);
     ut.begin();
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
        ut.commit();
        fail("Should not be here");
     }
     catch (RollbackException expected)
     {
     }
     assertTrue("Connection still enlisted in tx!", !c.isInTx());
  }
  
  public void testFailedStartTx() throws Exception
  {
     TestManagedConnection.setFailInStart(false, XAException.XAER_RMFAIL);
     tm.begin();
     TestConnection conn = null;
     TestConnection conn2 = null;
     
     try
     {
        assertTrue("Connection in pool!", cm.getPoolingStrategy().getConnectionCount() == 0);
        conn = (TestConnection)cm.allocateConnection(mcf, cri);
        
        //One should have been created
        assertTrue(cm.getPoolingStrategy().getConnectionCount() == 1);

        TestManagedConnection.setFailInStart(true, XAException.XAER_RMFAIL);
        
        conn2 = (TestConnection)cm.allocateConnection(mcf, cri);
        
        fail("Should not be here.");
     
     }
     catch (Throwable e)
     {
     }      
     conn.close();
     tm.rollback();
     assertTrue(conn2 == null);            
     assertTrue(cm.getPoolingStrategy().getConnectionCount() == 1);
  }
  
  public void testFailedEndTx() throws Exception
  {
     TestManagedConnection.setFailInStart(false, XAException.XAER_RMFAIL);
     TestManagedConnection.setFailInEnd(false, XAException.XAER_RMFAIL);
     tm.begin();
     TestConnection conn = null;
     TestConnection conn2 = null;
     
     try
     {
        assertTrue("Connection in pool!", cm.getPoolingStrategy().getConnectionCount() == 0);
        conn = (TestConnection)cm.allocateConnection(mcf, cri);
        
        //One should have been created
        assertTrue(cm.getPoolingStrategy().getConnectionCount() == 1);
        conn.close();

        TestManagedConnection.setFailInEnd(true, XAException.XAER_RMFAIL);
        
        conn2 = (TestConnection)cm.allocateConnection(mcf, cri);
        conn2.close();
        tm.commit();
        
        fail("Should not be here.");
     
     }
     catch (Throwable e)
     {
     }      
     
     TestManagedConnection.setFailInEnd(false, 0);
     TestManagedConnection.setFailInStart(false, 0);

     assertTrue(conn2.getMCIsNull());     
     assertTrue("Connection count" + cm.getPoolingStrategy().getConnectionCount(), cm.getPoolingStrategy().getConnectionCount() == 0);
     assertTrue("Failed endTx should destroy Connection", cm.getPoolingStrategy().getConnectionDestroyedCount() > 0);

  }
  
  
}
