/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.CachedConnectionManagerMBean;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.test.jca.adapter.TestConnectionFactory;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Abstract pooling stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 77478 $
 */
public class AbstractPoolingStressTest extends AbstractConcurrentStressTest
{
   Logger log = Logger.getLogger(getClass());
   Logger poolLog = Logger.getLogger(JBossManagedConnectionPool.class);

   protected TransactionManager tm;
   private CachedConnectionManager ccm;
   private TestManagedConnectionFactory mcf;
   private TxConnectionManager cm;
   private ConnectionRequestInfo cri;
   protected ConnectionFactory cf;

   @Override
   protected void setUp() throws Exception
   {
      log.debug("================> Start " + getName());
      tm = TransactionManagerLocator.getInstance().locate();
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");

      mcf = new TestManagedConnectionFactory();
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.maxSize = getMaxPoolSize();
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, poolLog);
      cri = new TestConnectionRequestInfo();
      cm = new TxConnectionManager(ccm, poolingStrategy, tm);
      if (isSticky())
      {
         cm.setInterleaving(false);
         mcf.setFailJoin(true);
      }
      else
      {
         cm.setInterleaving(true);
         mcf.setFailJoin(false);
      }
      mcf.setSleepInStart(200);
      mcf.setSleepInEnd(200);
      cm.setLocalTransactions(false);
      poolingStrategy.setConnectionListenerFactory(cm);
      cf = new TestConnectionFactory(new ConnectionManagerProxy(), mcf);
   }

   protected boolean isSticky()
   {
      return false;
   }
   
   protected int getMaxPoolSize()
   {
      return 20;
   }
   
   protected void tearDown() throws Exception
   {
      JBossManagedConnectionPool.OnePool pool = (JBossManagedConnectionPool.OnePool) cm.getPoolingStrategy();
      pool.shutdown();
      log.debug("================> End " + getName());
   }
   
   protected class ConnectionManagerProxy implements ConnectionManager
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 1L;

      public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException
      {
         return cm.allocateConnection(mcf, cri);
      }
   }
   
   public AbstractPoolingStressTest(String name)
   {
      super(name);
   }
}
