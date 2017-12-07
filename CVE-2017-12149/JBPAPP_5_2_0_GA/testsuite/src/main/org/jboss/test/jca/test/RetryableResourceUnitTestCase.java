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

import javax.resource.spi.ManagedConnectionFactory;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.ConnectionListener;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool.BasePool;
import org.jboss.test.jca.adapter.TestManagedConnectionFactory;

/**
 * Unit test for the RetryableResourceException
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RetryableResourceUnitTestCase extends TestCase
{
   private Logger log = Logger.getLogger(getClass());

   private CachedConnectionManager ccm = new CachedConnectionManager();

   /**
    * Creates a new <code>RetryableResourceUnitTestCase</code> instance.
    * @param name test name
    */
   public RetryableResourceUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Get a BaseConnectionManager2 object with an OnePool connection pool
    * @param pp The pool parameters
    * @return BaseConnectionManager2 instance
    * @exception Exception Thrown if an error occurs
    */
   private BaseConnectionManager2 getCM(InternalManagedConnectionPool.PoolParams pp) throws Exception
   {
      ManagedConnectionFactory mcf = new TestManagedConnectionFactory();
      ManagedConnectionPool poolingStrategy = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      BaseConnectionManager2 cm = new NoTxConnectionManager(ccm, poolingStrategy);
      poolingStrategy.setConnectionListenerFactory(cm);
      
      if (pp.prefill)
      {
         BasePool bp = (BasePool)poolingStrategy;
         bp.prefill(null, null, false);
      }
      
      return cm;
   }

   /**
    * Shutdown the BaseConnectionManager2
    * @param cm The instance
    */
   private void shutdown(BaseConnectionManager2 cm)
   {
      ManagedConnectionPool pool = cm.getPoolingStrategy();
      pool.shutdown();
   }

   
   public void testHashCodes() throws Exception
   {
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 0;
      pp.maxSize = 5;
      pp.blockingTimeout = 100;
      pp.idleTimeout = 500;

      BaseConnectionManager2 cm = getCM(pp);
      try
      {
         // Get a connection
         ConnectionListener cl = cm.getManagedConnection(null, null);
         assertTrue("1: Got a null connection!", cl.getManagedConnection() != null);
         assertTrue("1: InternalManagedConnectionPool", cl.getContext() instanceof InternalManagedConnectionPool);
         InternalManagedConnectionPool imcp = (InternalManagedConnectionPool)cl.getContext();
         cm.returnManagedConnection(cl, true);

         // Shutdown the pool in order to trigger the RetryableResourceException
         imcp.shutdown();

         // Get a connection - new internal managed pool is created
         cl = cm.getManagedConnection(null, null);
         assertTrue("2: Got a null connection!", cl.getManagedConnection() != null);
         cm.returnManagedConnection(cl, true);
      }
      finally
      {
         shutdown(cm);
      }
   }
}
