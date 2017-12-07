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
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.CachedConnectionManagerMBean;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnection;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;
import org.jboss.test.jca.support.PoolHelper;
import org.jboss.test.jca.support.PoolHelper.PoolType;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Unit Tests for pooling strategies
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class PoolingUnitTestCase extends EJBTestCase
{

   Logger log = Logger.getLogger(getClass());

   protected static TransactionManager tm;
   static TestConnectionRequestInfo cri1 = new TestConnectionRequestInfo("info1");
   static TestConnectionRequestInfo cri2 = new TestConnectionRequestInfo("info2");

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(PoolingUnitTestCase.class, "jca-tests.jar");
   }
   
   public PoolingUnitTestCase (String name)
   {
      super(name);
   }

   @Override
   public void setUp()
   {
      tm = TransactionManagerLocator.getInstance().locate();
   }

   private ManagedConnectionPool getPool(PoolType type, boolean noTxnSeperatePools, ManagedConnectionFactory mcf, InternalManagedConnectionPool.PoolParams pp){
      
      return PoolHelper.getManagedConnectionPool(type, mcf, noTxnSeperatePools, pp, log);
   }

   private ManagedConnectionPool getOnePool(int maxSize)
      throws Exception
   {
     
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = maxSize;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 0;
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = getPool(PoolHelper.PoolType.ONE_POOL, false, mcf, pp);
      return poolingStrategy;
   }

   private ManagedConnectionPool getPoolByCri(int maxSize)
      throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = maxSize;
      pp.blockingTimeout = 10000;
      pp.idleTimeout = 10000;
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = getPool(PoolType.CRI_POOL, false, mcf, pp);
      return poolingStrategy;
   }
   
   private BaseConnectionManager2 getNoTxCM(ManagedConnectionPool poolingStrategy) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      CachedConnectionManager ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");
      BaseConnectionManager2 cm = new NoTxConnectionManager(ccm, poolingStrategy);
      poolingStrategy.setConnectionListenerFactory(cm);
      return cm;
   }
   
   private BaseConnectionManager2 getTxCM(ManagedConnectionPool poolingStrategy) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      CachedConnectionManager ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");
      BaseConnectionManager2 cm = new TxConnectionManager(ccm, poolingStrategy, tm);
      poolingStrategy.setConnectionListenerFactory(cm);
      return cm;
   }
   
   private BaseConnectionManager2 getTxTrackCM(ManagedConnectionPool poolingStrategy) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      CachedConnectionManager ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");
      TxConnectionManager cm = new TxConnectionManager(ccm, poolingStrategy, tm);
      cm.setTrackConnectionByTx(true);
      poolingStrategy.setConnectionListenerFactory(cm);
      return cm;
   }
   
   private TestConnection allocate(BaseConnectionManager2 cm, TestConnectionRequestInfo cri) throws Exception
   {
      JBossManagedConnectionPool.BasePool mcp = (JBossManagedConnectionPool.BasePool) cm.getPoolingStrategy();
      ManagedConnectionFactory mcf = mcp.getManagedConnectionFactory();
      return (TestConnection) cm.allocateConnection(mcf, cri);
   }

   private void shutdown(ManagedConnectionPool mcp)
   {
      JBossManagedConnectionPool.BasePool pool = (JBossManagedConnectionPool.BasePool) mcp;
      pool.shutdown();
   }
 
   public void testOnePoolNoTx() throws Exception
   {
      ManagedConnectionPool mcp = getOnePool(1);
      BaseConnectionManager2 cm = getNoTxCM(mcp);
      try
      {
         doOnePool(cm);
      }
      finally
      {
         shutdown(mcp);
      }
   }

   public void testOnePoolTx() throws Exception
   {
      ManagedConnectionPool mcp = getOnePool(1);
      BaseConnectionManager2 cm = getTxCM(mcp);
      try
      {
         // Test before a transaction
         doOnePool(cm);
         tm.begin();
         // Test during a transaction
         doOnePool(cm);
         tm.commit();
         // Test after a transaction
         doOnePool(cm);
      }
      finally
      {
         shutdown(mcp);
      }
   }

   public void testOnePoolTxTrack() throws Exception
   {
      ManagedConnectionPool mcp = getOnePool(1);
      BaseConnectionManager2 cm = getTxTrackCM(mcp);
      try
      {
         // Test before a transaction
         doOnePool(cm);
         tm.begin();
         // Test during a transaction
         doOnePool(cm);
         tm.commit();
         // Test after a transaction
         doOnePool(cm);
      }
      finally
      {
         shutdown(mcp);
      }
   }

   public void testTrackConnectionByTx() throws Exception
   {
      ManagedConnectionPool mcp = getOnePool(2);
      BaseConnectionManager2 cm = getTxTrackCM(mcp);
      try
      {
         tm.begin();
         TestConnection c1 = allocate(cm, cri1);
         TestManagedConnection mc1 = c1.getMC();
         c1.close();
         TestConnection c2 = allocate(cm, cri1);
         TestManagedConnection mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should be equal in same transaction", mc1.equals(mc2));
         Transaction tx1 = tm.suspend();
         tm.begin();
         c2 = allocate(cm, cri1);
         mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should not be equal in a different transaction", mc1.equals(mc2) == false);
         tm.commit();
         c2 = allocate(cm, cri1);
         mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should not be equal outside a transaction", mc1.equals(mc2) == false);
         tm.resume(tx1);
         c2 = allocate(cm, cri1);
         mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should still be equal in same transaction", mc1.equals(mc2));
         tm.commit();
         assertTrue("All connections should be recycled", mcp.getAvailableConnectionCount() == 2);
      }
      finally
      {
         shutdown(mcp);
      }
   }

   public void testTrackConnectionByTxAndCRI() throws Exception
   {
      ManagedConnectionPool mcp = getPoolByCri(2);
      BaseConnectionManager2 cm = getTxTrackCM(mcp);
      try
      {
         tm.begin();
         TestConnection c1 = allocate(cm, cri1);
         TestManagedConnection mc1 = c1.getMC();
         c1.close();
         TestConnection c2 = allocate(cm, cri1);
         TestManagedConnection mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should be equal in same transaction and criteria", mc1.equals(mc2));
         c2 = allocate(cm, cri2);
         mc2 = c2.getMC();
         c2.close();
         assertTrue("Connections should not be equal in same transaction but different criteria", mc1.equals(mc2) == false);
         tm.commit();
      }
      finally
      {
         shutdown(mcp);
      }
   }
   
   public void doOnePool(BaseConnectionManager2 cm) throws Exception
   {
      TestConnection c1 = allocate(cm, cri1);
      TestManagedConnection mc1 = c1.getMC();
      c1.close();
      TestConnection c2 = allocate(cm, cri1);
      TestManagedConnection mc2 = c2.getMC();
      c2.close();
      assertEquals("Should get the same connection for same criteria", mc1, mc2);
      c2 = allocate(cm, cri2);
      mc2 = c2.getMC();
      c2.close();
      assertEquals("Should get the same connection for different cri", mc1, mc2);
   }
}
