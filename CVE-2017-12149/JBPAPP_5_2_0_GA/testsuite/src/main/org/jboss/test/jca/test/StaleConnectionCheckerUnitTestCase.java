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

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jdbc.StaleConnectionException;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.resource.adapter.jdbc.local.LocalManagedConnectionFactory;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;
import org.jboss.resource.connectionmanager.NoTxConnectionManager;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.jdbc.TestConnection;

public class StaleConnectionCheckerUnitTestCase extends JBossTestCase
{
   Logger log = Logger.getLogger(PreFillPoolingUnitTestCase.class);

   public StaleConnectionCheckerUnitTestCase(String name)
   {
      super(name);
      
   }
   public void testNullStaleConnectionChecker() throws Exception
   {
    
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setStaleConnectionCheckerClassName("non-stale");
      mcf.setExceptionSorterClassName("org.jboss.test.jca.support.MockExceptionSorter");      
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;

      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);
      Connection conn = (Connection)noTxn.allocateConnection(mcf, null);
      Object proxy = ((WrappedConnection)conn).getUnderlyingConnection();
      TestConnection uc = (TestConnection) Proxy.getInvocationHandler(proxy);
      uc.setFail(true);
      
      try
      {
         conn.createStatement().execute("blah");
         
      }catch(SQLException e)
      {
         //Normal 
         assertFalse("Should not be StaleConnectionException", e instanceof StaleConnectionException);
         
      }

   }
   
   public void testNonStaleConnection() throws Exception
   {
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setStaleConnectionCheckerClassName("org.jboss.test.jca.support.MockStaleConnectionCheckerFalse");
      mcf.setExceptionSorterClassName("org.jboss.test.jca.support.MockExceptionSorter");      

      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;

      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);
      Connection conn = (Connection)noTxn.allocateConnection(mcf, null);
      Object proxy = ((WrappedConnection)conn).getUnderlyingConnection();
      TestConnection uc = (TestConnection) Proxy.getInvocationHandler(proxy);
      uc.setFail(true);
      
      try
      {
         conn.createStatement().execute("blah");
         
      }catch(SQLException e)
      {
         //Normal 
         assertFalse("Should not be StaleConnectionException", e instanceof StaleConnectionException);
         assertTrue("Connection error should be fatal", mcp.getConnectionDestroyedCount() > 0);
         
      }
      
      assertTrue("Exception should be falal", mcp.getConnectionDestroyedCount() > 0);      

   }
   
   
   public void testStaleConnection() throws Exception
   {
      
      LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
      
      mcf.setDriverClass("org.jboss.test.jca.jdbc.TestDriver");
      mcf.setConnectionURL("jdbc:jboss-test-adapter");
      mcf.setStaleConnectionCheckerClassName("org.jboss.test.jca.support.MockStaleConnectionCheckerTrue");
      InternalManagedConnectionPool.PoolParams pp = new InternalManagedConnectionPool.PoolParams();
      pp.minSize = 1;
      pp.maxSize = 2;
      pp.blockingTimeout = 10000;

      ManagedConnectionPool mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, false, log);
      NoTxConnectionManager noTxn = new NoTxConnectionManager(null, mcp);
      mcp.setConnectionListenerFactory(noTxn);
      Connection conn = (Connection)noTxn.allocateConnection(mcf, null);
      Object proxy = ((WrappedConnection)conn).getUnderlyingConnection();
      TestConnection uc = (TestConnection) Proxy.getInvocationHandler(proxy);
      uc.setFail(true);
      
      try
      {
         conn.createStatement().execute("blah");
         
      }catch(SQLException e)
      {
         //Normal 
         assertTrue("Should be StaleConnectionException", e instanceof StaleConnectionException);
         
      }
      
      assertTrue("StaleConnectionException should not destroy connection", mcp.getConnectionDestroyedCount() == 0);      
      
   }

}
