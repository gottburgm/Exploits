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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.invokerha.HAServiceRemote;

import junit.framework.Test;

/**
 * Tests for ha invoker.
 *
 * @author <a href="mailto:brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 91326 $
 */
public class HAInvokerUnitTestCase
   extends JBossClusteredTestCase
{
   private static boolean deployed0_ = true;
   private static boolean deployed1_ = true;
   
   public HAInvokerUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return JBossClusteredTestCase.getDeploySetup(HAInvokerUnitTestCase.class, "ha-invoker.sar");
   }

   public void testUnifiedHAProxyFailover()
      throws Exception
   {
      haProxyFailoverTest("jmx/HAServiceUnified");
   }

   public void testPooledHAProxyFailover()
      throws Exception
   {
      haProxyFailoverTest("jmx/HAServicePooled");
   }

   public void testJRMPHAProxyFailover()
      throws Exception
   {
      haProxyFailoverTest("jmx/HAService");
   }
   
   private void haProxyFailoverTest(String jndiName) throws Exception
   {
      getLog().debug("testHAProxyFailover"); 
      
      String[] urls = getNamingURLs();
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, urls[0]);
      Context ctx = new InitialContext(env);
      getLog().debug("Got InitialContext with " + urls[0]);
      
      HAServiceRemote remote = (HAServiceRemote) ctx.lookup(jndiName);
      getLog().debug("Found " + jndiName);
      assertEquals("Hello", remote.hello());
      String nodeA = remote.getClusterNode();
      assertNotNull("Got clusterNode", nodeA);
      getLog().debug("nodeA OK");
      // Invoke again to check it works with load balancing
      assertFalse("Requests load balanced", nodeA.equals(remote.getClusterNode()));
      getLog().debug("nodeA load balanced OK");
      
      // Undeploy from one node
      reconfigureCluster();
      
      // Test for JBAS-5164
      try
      {
         ctx.lookup(jndiName);
         fail("Known issue JBAS-5164.  Proxy not removed from JNDI on undeployed node");
      }
      catch (NamingException good) {}
      
      // Check it still works
      try
      {
         assertEquals("Hello", remote.hello());
         getLog().debug("OK after reconfigure");
      }
      catch (ServiceUnavailableException sue)
      {
         fail("Known issue JBAS-3194: " + sue.getMessage());
      }
   }   
   
   protected void setUp() throws Exception
   {
      super.setUp();
      configureCluster();
   }

   protected String getDeploymentName()
   {
      return "ha-invoker.sar";
   }

   protected void configureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      String warName = getDeploymentName();
      if (!deployed0_)
      {
         deploy(adaptors[0], warName);
         getLog().debug("Deployed " + warName + " on server0");
         deployed0_ = true;
      }
      if (!deployed1_)
      {
         deploy(adaptors[1], warName);
         getLog().debug("Deployed " + warName + " on server1");
         deployed1_ = true;
      }
   
      sleep(2000);
   }
   
   protected void reconfigureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      if (!deployed1_)
      {
         deploy(adaptors[1], getDeploymentName());
         deployed1_ = true;
         
         sleep(2000);
      }
      
      if (deployed0_)
      {
         undeploy(adaptors[0], getDeploymentName());
         deployed0_ = false;
         
         sleep(2000);
      }
   }

}
