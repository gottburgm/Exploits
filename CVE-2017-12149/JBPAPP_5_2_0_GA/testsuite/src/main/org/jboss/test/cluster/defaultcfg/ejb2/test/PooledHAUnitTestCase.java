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
package org.jboss.test.cluster.defaultcfg.ejb2.test;

import java.util.Date;
import java.util.Properties;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSessionHome;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;
import org.jboss.invocation.pooled.interfaces.PooledInvokerProxy;
import org.jboss.invocation.pooled.interfaces.ServerAddress;
import junit.framework.Test;

/**
 * Tests of stateless/stateful HA behavior using the pooled invoker
 *
 * @author  <a href="mailto:sacha.labourey@jboss.org">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class PooledHAUnitTestCase extends JBossClusteredTestCase
{
   static boolean deployed = false;
   static Date startDate = new Date();

   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);

   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);

   public PooledHAUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(PooledHAUnitTestCase.class, "pooledha.jar");
      return t1;
   }

   /**
    * Test the equals/hashCode behavior for ServerAddress
    * @throws Exception
    */
   public void testServerAddressHashEquals()
      throws Exception
   {
      ServerAddress sa1 = new ServerAddress("127.0.0.1", 4445, false, 60, null);
      ServerAddress sa2 = new ServerAddress("127.0.0.1", 4445, false, 61, null);
      assertEquals(sa1, sa2);
      assertEquals(sa1.hashCode(), sa2.hashCode());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(sa1);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      sa2 = (ServerAddress) ois.readObject();
      assertEquals(sa1, sa2);
      assertEquals(sa1.hashCode(), sa2.hashCode());

      // Different tcpNoDelay should not be equal
      sa2 = new ServerAddress("127.0.0.1", 4445, true, 61, null);
      assertNotSame(sa1, sa2);
      // Different ports should not be equal
      sa2 = new ServerAddress("127.0.0.1", 4446, false, 60, null);
      assertNotSame(sa1, sa2);
      // Different host should not be equal
      sa2 = new ServerAddress("127.0.0.2", 4445, false, 60, null);
      assertNotSame(sa1, sa2);
   }

   public void testStatelessBeanLoadBalancing()
      throws Exception
   {
      log.debug("testStatelessBeanLoadBalancing - Trying the context...");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);

      log.debug("Looking up the home pooledha_StatelessSession..."+urls[0]);
      StatelessSessionHome homeHA =
         (StatelessSessionHome) ctx.lookup("pooledha_StatelessSession");
      log.debug("Looking up the home pooled_StatelessSession..."+urls[0]);
      StatelessSessionHome home =
         (StatelessSessionHome) ctx.lookup("pooled_StatelessSession");

      // Simple connection count stress test
      PooledInvokerProxy.clearStats();
      PooledInvokerProxy.clearPools();
      for(int n = 0; n < 100; n ++)
      {
         StatelessSession tmp = homeHA.create();
         StatelessSession tmp2 = home.create();
         tmp.getCallCount();
         tmp2.getCallCount();
         tmp.getCallCount();
         tmp2.getCallCount();
         int totalCount = PooledInvokerProxy.getTotalPoolCount();
         // N cluster nodes + 1 for unclustered
         int expectedCount = urls.length + 1;
         assertEquals("TotalPoolCount", expectedCount, totalCount);
         long usedPooled = PooledInvokerProxy.getUsedPooled();
         // iter * Ncalls - expectedCount (for the initial conn creation)
         int expectedUsedPooled = (n+1) * 6 - expectedCount;
         assertEquals("UsedPooled", expectedUsedPooled, usedPooled);
      }
      long inUseCount = PooledInvokerProxy.getInUseCount();
      assertEquals("InUseCount", 0, inUseCount);

      log.debug("Calling create on StatelessSessionHome...");
      StatelessSession statelessSession = homeHA.create();
      assertTrue("homeHA.create() != null", statelessSession != null);
      log.debug("Calling getEJBHome() on StatelessSession...");
      assertTrue("statelessSession.getEJBHome() != null", statelessSession.getEJBHome() != null);

      log.debug("Reseting the number of calls made on beans (making 2 calls)... ");
      for (int i=0; i<6; i++)
      {
         log.debug("Reseting number... ");
         statelessSession.resetNumberOfCalls ();
      }

      log.debug("- "+"Now making 20 calls on this remote... ");
      for (int i=0; i<20; i++)
      {
         log.debug("- "+" Calling remote... ");
         statelessSession.makeCountedCall ();
      }

      log.debug("- "+"Getting the number of calls that have been performed on each bean... ");
      long node1 = statelessSession.getCallCount();
      log.debug("- "+"One node has received: " + node1);

      long node2 = statelessSession.getCallCount();
      log.debug("- "+"The other node has received: " + node2);

      if (node1 == node2 && node1 == 10)
      {
         log.debug("- "+"Test is ok.");
      }
      else if( urls.length > 1 )
      {
         log.debug("- "+"Something wrong has happened! Calls seems not to have been load-balanced.");
         fail ("call count mismatch: "+node1+" != "+node2);
      }

      statelessSession.remove();
      log.debug("ok");
   }

   public void testStatefulBeanFailover()
      throws Exception
   {
      log.debug("testStatelessBeanLoadBalancing - Trying the context...");

      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);

      log.debug("Looking up the home pooledha_StatefulSession..."+urls[0]);
      StatefulSessionHome homeHA =
         (StatefulSessionHome) ctx.lookup("pooledha_StatefulSession");
      log.debug("Looking up the home pooled_StatelessSession..."+urls[0]);
      StatelessSessionHome home =
         (StatelessSessionHome) ctx.lookup("pooled_StatelessSession");

      // Simple connection count stress test
      PooledInvokerProxy.clearStats();
      PooledInvokerProxy.clearPools();
      for(int n = 0; n < 100; n ++)
      {
         StatefulSession tmp = (StatefulSession) homeHA.create();
         tmp.remove();
         tmp = (StatefulSession) homeHA.create();
         StatelessSession tmp2 = home.create();
         tmp.getNodeState();
         tmp2.getCallCount();
         tmp.getNodeState();
         tmp2.getCallCount();
         tmp.remove();
         int totalCount = PooledInvokerProxy.getTotalPoolCount();
         // N cluster nodes + 1 for unclustered
         int expectedCount = urls.length + 1;
         assertEquals("TotalPoolCount", expectedCount, totalCount);
         long usedPooled = PooledInvokerProxy.getUsedPooled();
         // iter * Ncalls - expectedCount (for the initial conn creation)
         int expectedUsedPooled = (n+1) * 9 - expectedCount;
         assertEquals("UsedPooled", expectedUsedPooled, usedPooled);
      }
      long inUseCount = PooledInvokerProxy.getInUseCount();
      assertEquals("InUseCount", 0, inUseCount);

      log.debug("Test Stateful Bean Failover");
      log.debug("Looking up the home nextgen.StatefulSession...");
      log.debug("Calling create on StatefulSessionHome...");
      StatefulSession statefulSession =
      (StatefulSession)homeHA.create("Bupple-Dupple");
      assertTrue("statefulSessionHome.create() != null", statefulSession != null);

      NodeAnswer node1 = statefulSession.getNodeState ();
      getLog ().debug (node1);

      // Now we switch to the other node, simulating a failure on node 1
      System.setProperty ("JBossCluster-DoFail", "once");
      NodeAnswer node2 = statefulSession.getNodeState ();
      log.debug (node2);
      assertEquals("Value is identical on replicated node1", "Bupple-Dupple", node1.answer);
      assertEquals("Value is identical on replicated node2", "Bupple-Dupple", node2.answer);

      // we change our name to see if it replicates to node 1
      statefulSession.setName ("Changed");

      // now we travel back on node 1
      System.setProperty ("JBossCluster-DoFail", "once");
      node1 = statefulSession.getNodeState ();
      log.debug(node1);

      assertEquals("Value is identical on replicated node1", "Changed", node1.answer);
      node2 = statefulSession.getNodeState ();
      log.debug(node2);
      assertEquals("Value is identical on replicated node2", "Changed", node2.answer);

      statefulSession.remove();

      int totalCount = PooledInvokerProxy.getTotalPoolCount();
      // N cluster nodes + 1 for unclustered
      int expectedCount = urls.length + 1;
      assertEquals("TotalPoolCount", expectedCount, totalCount);
   }
}
