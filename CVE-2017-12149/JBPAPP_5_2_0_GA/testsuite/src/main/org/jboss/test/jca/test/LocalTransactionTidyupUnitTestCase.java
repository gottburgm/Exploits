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

import javax.management.MBeanServer;
import javax.naming.InitialContext;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import junit.framework.Test;

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

/**
 * A LocalTransactionTidyupUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class LocalTransactionTidyupUnitTestCase extends EJBTestCase
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
      return JBossTestCase.getDeploySetup(LocalTransactionTidyupUnitTestCase.class, "jca-tests.jar");
   }

   public LocalTransactionTidyupUnitTestCase (String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
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
      cm.setLocalTransactions(true);
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

   public void testSimple() throws Exception
   {
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertNotNull(c);
      c.close();
   }

   public void testManualTransactionCommit() throws Exception
   {
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertNotNull(c);
      try
      {
         c.begin();
         assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         c.commit();
         assertEquals(TestManagedConnection.LOCAL_COMMITTED, c.getLocalState());
      }
      finally
      {
         c.close();
      }
   }

   public void testManualTransactionRollback() throws Exception
   {
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertNotNull(c);
      try
      {
         c.begin();
         assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         c.rollback();
         assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
      }
      finally
      {
         c.close();
      }
   }

   public void testManualTransactionForgetToCommitRollback() throws Exception
   {
      TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
      assertNotNull(c);
      try
      {
         c.begin();
         assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
      }
      finally
      {
         c.close();
         assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
      }
   }

   public void testJTATransactionCommit() throws Exception
   {
      tm.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
      }
      finally
      {
         tm.commit();
         if (c != null)
            assertEquals(TestManagedConnection.LOCAL_COMMITTED, c.getLocalState());
      }
   }

   public void testJTATransactionRollback() throws Exception
   {
      tm.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
      }
      finally
      {
         tm.rollback();
         if (c != null)
            assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
      }
   }

   public void testUserTransactionCommit() throws Exception
   {
      ut.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
      }
      finally
      {
         ut.commit();
         if (c != null)
            assertEquals(TestManagedConnection.LOCAL_COMMITTED, c.getLocalState());
      }
   }

   public void testUserTransactionRollback() throws Exception
   {
      ut.begin();
      TestConnection c = null;
      try
      {
         c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
         }
      }
      finally
      {
         ut.rollback();
         if (c != null)
            assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
      }
   }

   public void testLazyUserTransactionCommit() throws Exception
   {
      ccm.pushMetaAwareObject(this, null);
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_NONE, c.getLocalState());
            ut.begin();
            try
            {
               assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
            }
            finally
            {
               ut.commit();
               assertEquals(TestManagedConnection.LOCAL_COMMITTED, c.getLocalState());
            }
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_COMMITTED, c.getLocalState());
         }
      }
      finally
      {
         ccm.popMetaAwareObject(null);
      }
   }

   public void testLazyUserTransactionRollback() throws Exception
   {
      ccm.pushMetaAwareObject(this, null);
      try
      {
         TestConnection c = (TestConnection)cm.allocateConnection(mcf, cri);
         assertNotNull(c);
         try
         {
            assertEquals(TestManagedConnection.LOCAL_NONE, c.getLocalState());
            ut.begin();
            try
            {
               assertEquals(TestManagedConnection.LOCAL_TRANSACTION, c.getLocalState());
            }
            finally
            {
               ut.rollback();
               assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
            }
         }
         finally
         {
            c.close();
            assertEquals(TestManagedConnection.LOCAL_ROLLEDBACK, c.getLocalState());
         }
      }
      finally
      {
         ccm.popMetaAwareObject(null);
      }
   }
}
