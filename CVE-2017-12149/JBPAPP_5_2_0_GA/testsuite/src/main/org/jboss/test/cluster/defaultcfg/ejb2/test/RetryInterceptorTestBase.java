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
package org.jboss.test.cluster.defaultcfg.ejb2.test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.AssertionFailedError;

import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSessionHome;
import org.jboss.test.testbean.interfaces.AComplexPK;
import org.jboss.test.testbean.interfaces.EntityPK;
import org.jboss.test.testbean.interfaces.EntityPKHome;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;
import org.jnp.interfaces.NamingContext;

/**
 * Base class for tests of the RetryInterceptor.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision $
 */
public abstract class RetryInterceptorTestBase extends JBossClusteredTestCase
{   
   protected static final String DISCOVERY_TTL = System.getProperty("jbosstest.udp.ip_ttl", "1");
   protected static final String DISCOVERY_GROUP = System.getProperty("jbosstest.udpGroup");
   protected static final String DISCOVERY_PARTITION = System.getProperty("jbosstest.partitionName", "DefaultPartition");
   
   protected static Logger log;
   
   protected static File customJndiDir = null;
   protected static File customJndiProperties = null;
   
   static abstract class RetryCaller extends Thread
   {
      Properties env;
      Throwable failure;
      
      RetryCaller(Properties env)
      {
         this.env = env;
      }
      
      public void run()
      {
         ClassLoader tccl = Thread.currentThread().getContextClassLoader();
         try
         {
            if (customJndiProperties != null)
            {
               // Create a special classloader that will read in the 
               // customJndiProperties file and include it in any 
               // getResources("jndi.properties") request.
               // We use this to allow running the server with
               // HA-JNDI autodiscovery set to a custom address 
               URL[] urls = new URL[]{ customJndiDir.toURL() };
               ClassLoader cl = new URLClassLoader(urls, tccl);
               Thread.currentThread().setContextClassLoader(cl);
            }
            
            // Establish an initial context on this thread with the
            // given properties -- needed for JBoss NamingContextFactory
            // to work properly.  Meaningless otherwise            
            new InitialContext(env);
            
            executeTest();
         }
         catch (Throwable t)
         {
            log.error(this + " caught an exception", t);
            failure = t;
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(tccl);
         }
      }
      
      protected abstract void executeTest() throws Throwable;
      
   }
   
   static class MultiRetryCaller extends RetryCaller
   {
      StatelessSessionHome slsbh;
      StatelessSession slsb;
      StatefulSessionHome sfsbh;
      StatefulSession sfsb;
      NodeAnswer node1;
      EntityPKHome epkh;
      EntityPK epk;
      int other;
      AComplexPK pk;
      Logger log;
      Throwable failure;
      
      MultiRetryCaller(Properties env,
                  StatelessSessionHome slsbh, StatelessSession slsb,
                      StatefulSessionHome sfsbh, StatefulSession sfsb,
                      NodeAnswer node1,
                      EntityPKHome epkh, EntityPK epk,
                      int other, AComplexPK pk,
                      Logger log)
      {
         super(env);
         this.slsbh = slsbh;
         this.slsb = slsb;
         this.sfsbh = sfsbh;
         this.sfsb = sfsb;
         this.node1 = node1;
         this.epkh = epkh;
         this.epk = epk;
         this.other = other;
         this.pk = pk;
         this.log = log;
      }
      
      protected void executeTest() throws Throwable
      {
         // Test that the SFSB still works
         
         System.setProperty ("JBossCluster-DoFail", "once");
         NodeAnswer node2 = sfsb.getNodeState ();      
         log.debug (node2);
         
         assertTrue ("StatefulSession: Failover has occured", 
                     !node1.nodeId.equals(node2.nodeId));
         
         assertTrue ("StatefulSession: Value is identical on replicated node", 
                     "Bupple-Dupple".equals (node1.answer) &&
                     node1.answer.equals(node2.answer) );
         
         log.debug("StatefulSession: Retry successful");
         
         // Test that the SFSB Home still works
         System.setProperty ("JBossCluster-DoFail", "once");
         sfsb = (StatefulSession)sfsbh.create("Hippie-Dippie");
         
         node2 = sfsb.getNodeState ();      
         log.debug (node2);
         
         assertTrue ("StatefulSessionHome: Failover has occured", 
                     !node1.nodeId.equals (node2.nodeId));
         
         log.debug("StatefulSessionHome: Retry successful");
         
         // Test that the SLSB still works
         System.setProperty ("JBossCluster-DoFail", "once");
         log.debug("StatelessSession: Now making 1 call on server1 ");
         assertTrue("StatelessSession: Server1 has no calls", 0 == slsb.getCallCount());
         
         log.debug("StatelessSession: Retry successful");
         
         // Test that the SLSB Home still works
         System.setProperty ("JBossCluster-DoFail", "once");
         log.debug("Calling create on StatelessSessionHome...");
         slsb = slsbh.create();
         
         log.debug("StatelessSessionHome: Retry successful");
         
         // Test that the entity bean still works
         System.setProperty ("JBossCluster-DoFail", "once");
         log.debug("Retrieving other field again, should be " + other + "...");
         int newValue = epk.getOtherField();
         assertEquals("pkBean.getOtherField() correct:", other, newValue);
         
         log.debug("EntityBean: Retry successful");
         
         // Test the entity home still works
         System.setProperty ("JBossCluster-DoFail", "once");
         epk = epkh.findByPrimaryKey(pk);

         assertTrue("pkBean != null", epk != null);

         log.debug("EntityBeanHome: Retry successful");
      }
   }
   
   static class SFSBRetryCaller extends RetryCaller
   {
      StatefulSessionHome sfsbh;
      StatefulSession sfsb;
      Throwable failure;
      Logger log;
      
      SFSBRetryCaller(Properties env, StatefulSession sfsb, Logger log)
      {
         super(env);
         this.sfsb = sfsb;
         this.log = log;
      }
      
      protected void executeTest() throws Throwable
      {
         // Test that the SFSB still works
         System.setProperty ("JBossCluster-DoFail", "once");
         NodeAnswer node2 = sfsb.getNodeState ();      
         log.debug (node2);
         
         assertTrue ("StatefulSession: Failover has occured", node2 != null);
         
         log.debug("StatefulSession: Retry successful");
         
      }
   }
   static class DeferredRecoveryCaller extends RetryCaller
   {
      StatelessSession slsb;
      Object result;
   
      DeferredRecoveryCaller(Properties env, StatelessSession slsb)
      {
         super(env);
         this.slsb = slsb;
      }
      
      public void executeTest() throws Throwable
      {
         // Test that the SLSB still works
         System.setProperty ("JBossCluster-DoFail", "once");
         result = new Long(slsb.getCallCount()); 
      }
   }
   
   /**
    * Create a new RetryInterceptorUnitTestCase.
    * 
    * @param name
    */
   public RetryInterceptorTestBase(String name)
   {
      super(name);
      log = Logger.getLogger(getClass());
   }

   protected void executeRetryTest(RetryCaller caller) 
      throws InterruptedException, AssertionFailedError
   {
      caller.start();
      
      // Give the caller 15 secs to do its work
      caller.join(15000);
      
      boolean alive = caller.isAlive();
      if (alive)
         caller.interrupt();
      
      assertFalse("Retry calls completed", alive);
      
      if (caller.failure instanceof AssertionFailedError)
      {
         throw (AssertionFailedError) caller.failure;
      }
      else if (caller.failure != null)
      {
         fail(caller.failure.getClass().getName() + " " + caller.failure.getMessage());
      }
   }
   
   protected void deferredRecoveryTest(boolean expectSuccess) throws Exception
   {
      getLog().debug("+++ Enter testDeferredRecovery");
      
      configureCluster();
      
      Properties env = getNamingProperties("org.jboss.naming.NamingContextFactory", 
                                           false);
      InitialContext ctx = new InitialContext(env);
      
      getLog().debug("Looking up the home nextgen.StatelessSession" + getJndiSuffix()+"...");
      StatelessSessionHome  home =
            (StatelessSessionHome) ctx.lookup("nextgen_StatelessSession" + getJndiSuffix());
      if (home!= null ) getLog().debug("ok");
      getLog().debug("Calling create on StatelessSessionHome" + getJndiSuffix()+"...");
      StatelessSession slsb = home.create();

      getLog().debug("StatelessSession: Now making 1 call on server0 ");
      assertEquals("StatelessSession: Server0 has no calls", 0, slsb.getCallCount());
      
      // Undeploy the ear
      MBeanServerConnection[] adaptors = getAdaptors();
      undeploy(adaptors[0], "test-retry.ear");
      setDeployed0(false);
      
      sleep(1000);
      
      DeferredRecoveryCaller caller = new DeferredRecoveryCaller(env, slsb);
      caller.start();
      
      sleep(1000);
      
      if (caller.isAlive()) // don't bother deploying otherwise
      {
         deploy(adaptors[1], "test-retry.ear");
         setDeployed1(true);
      }
      
      // Give the caller 3 secs to complete (extremely generous)
      caller.join(3000);      
      
      if (caller.isAlive())
         fail("Caller did not complete");
      
      if (expectSuccess)
      {
         assertTrue("Caller retrieved a long", (caller.result instanceof Long));
         getLog().debug("StatefulSession: Retry successful");
      }
      else   
      {
         assertTrue("Caller failed as expected", caller.failure != null);
         getLog().debug("StatefulSession: Retry failed as expected");
      }
      
      getLog().debug("+++ Exit testDeferredRecovery");
   }
   
   /**
    * Tests that the retry interceptor works properly for an SFSB given
    * a particular JNDI client configuration.
    * 
    * @param ctx
    * @throws Exception
    */
   protected void sfsbTest(Context ctx, Properties env) throws Exception
   {
      configureCluster();
      
      getLog().debug("Looking up the home nextgen.StatefulSession" + getJndiSuffix()+"...");
      StatefulSessionHome  statefulSessionHome =
         (StatefulSessionHome) ctx.lookup("nextgen_StatefulSession" + getJndiSuffix());
      if (statefulSessionHome!= null ) getLog().debug("ok");
         getLog().debug("Calling create on StatefulSessionHome" + getJndiSuffix()+"...");
      StatefulSession statefulSession =
         (StatefulSession)statefulSessionHome.create("Bupple-Dupple");
      assertTrue("statefulSessionHome.create() != null", statefulSession != null);
      getLog().debug("ok");
      
      NodeAnswer node1 = statefulSession.getNodeState ();
      getLog ().debug (node1);
      
      // Reconfigure the cluster so the existing targets are invalid
      // BES -- don't bother; we test that functionality in testRetryInterceptor
      // just confirm that reestablishing the targets works
//      reconfigureCluster();
      
      // Make calls on the bean in another thread so we can terminate
      // after a reasonable period of time if the RetryInterceptor is looping
      SFSBRetryCaller caller = new SFSBRetryCaller(env, statefulSession, getLog());
      executeRetryTest(caller);
      
      getLog().debug("StatefulSession: Retry successful");
   }
   
   protected Properties getNamingProperties(String namingFactoryClass, boolean autoDiscovery)
      throws Exception
   {
      String[] urls = getHANamingURLs();
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, namingFactoryClass);
      env.setProperty(Context.PROVIDER_URL, urls[0]);
      // Don't let the discovery packet off the test server so we don't
      // get spurious responses from other servers on the network
      env.setProperty("jnp.discoveryTTL", DISCOVERY_TTL);
      
      if (!autoDiscovery)
      {
         env.setProperty("jnp.disableDiscovery", "true");
      }
      else
      {
         if (DISCOVERY_GROUP != null && "".equals(DISCOVERY_GROUP) == false)      
         {
            // Use the multicast address this test environment is using
            env.put(NamingContext.JNP_DISCOVERY_GROUP, DISCOVERY_GROUP);
         }
         if (DISCOVERY_PARTITION != null && "".equals(DISCOVERY_PARTITION) == false)
         {
            // Limit to the partition this test environment is using
            env.put(NamingContext.JNP_PARTITION_NAME, DISCOVERY_PARTITION);
         }
      }  
      
      return env;
   }
   
   protected String getJndiSuffix()
   {
      return "_Retry";
   }
   
   protected void configureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
   
      if (!isDeployed0())
   {
         deploy(adaptors[0], "test-retry.ear");
         getLog().debug("Deployed test-retry.ear on server0");
         setDeployed0(true);
   }
      if (isDeployed1())
      {
         undeploy(adaptors[1], "test-retry.ear");
         getLog().debug("Undeployed test-retry.ear on server1");
         setDeployed1(false);
      }
   
      sleep(2000);
   }
   
   protected void reconfigureCluster() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      deploy(adaptors[1], "test-retry.ear");
      setDeployed1(true);
      
      sleep(2000);
      
      undeploy(adaptors[0], "test-retry.ear");
      setDeployed0(false);
      
      sleep(2000);
   }


   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      if (customJndiProperties != null)
      {
         try
         {
            customJndiProperties.delete();
            if (customJndiProperties.exists())
               customJndiProperties.deleteOnExit();
         }
         catch (Exception e)
         {
            log.error("problem cleaning customJndiProperties", e);
         }
         
         customJndiProperties = null;
      }
      
      if (customJndiDir != null)
      {
         try
         {
            customJndiDir.delete();
            if (customJndiDir.exists())
               customJndiDir.deleteOnExit();
         }
         catch (Exception e)
         {
            log.error("problem cleaning customJndiDir", e);
         }
         
         customJndiProperties = null;
      }
      
      if (System.getProperty("JBossCluster-DoFail") != null)
         System.setProperty("JBossCluster-DoFail", "false");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      if (isDeployed0())
      {
         undeploy(adaptors[0], "test-retry.ear");
         getLog().debug("Undeployed test-retry.ear on server0");
         setDeployed0(false);
}
      if (isDeployed1())
      {
         undeploy(adaptors[1], "test-retry.ear");
         getLog().debug("Undeployed test-retry.ear on server1");
         setDeployed1(false);
      }
   }

   protected abstract boolean isDeployed0();

   protected abstract void setDeployed0(boolean deployed);

   protected abstract boolean isDeployed1();

   protected abstract void setDeployed1(boolean deployed);

}
