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

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool.PoolParams;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool.PoolByCri;
import org.jboss.resource.statistic.StatisticsReporter;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.adapter.TestConnectionRequestInfo;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;

/**
 * A StatisticsReporterUnitTestCase.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 85731 $
 */
public class StatisticsReporterUnitTestCase extends JBossTestCase
{

   Logger log = Logger.getLogger(getClass());

   ConnectionRequestInfo cri = new TestConnectionRequestInfo();

   CachedConnectionManager ccm = new CachedConnectionManager();

   public StatisticsReporterUnitTestCase(String name)
   {

      super(name);

   }

   private BaseConnectionManager2 getCM(InternalManagedConnectionPool.PoolParams pp) throws Exception
   {
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      BaseConnectionManager2 cm = new NoTxConnectionManager(ccm, poolingStrategy);
      poolingStrategy.setConnectionListenerFactory(cm);

      return cm;
   }
   
   private BaseConnectionManager2 getCriCM(InternalManagedConnectionPool.PoolParams pp){
      
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.PoolByCri(mcf, pp, true, null, log);
      BaseConnectionManager2 cm = new NoTxConnectionManager(ccm, poolingStrategy);
      poolingStrategy.setConnectionListenerFactory(cm);
      return cm;
   }

   public void testSimpleStatistics() throws Exception
   {

      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 5;
      pp.maxSize = 10;
      pp.blockingTimeout = 1000;
      pp.idleTimeout = 500;
      BaseConnectionManager2 cm = getCriCM(pp);
  
      cm.getManagedConnection(null, null);
      StatisticsReporter reporter = (StatisticsReporter) cm.getPoolingStrategy();
      Object stats = reporter.listStatistics();

      Thread.sleep(10000);
      stats = reporter.listStatistics();
      
      

   }

//   public static Test suite() throws Exception
//   {
//      ClassLoader loader = Thread.currentThread().getContextClassLoader();
//      URL resURL = loader.getResource("jca/stats/stats-ds.xml");
//      return getDeploySetup(StatisticsReporterUnitTestCase.class, resURL.toString());
//   }
}
