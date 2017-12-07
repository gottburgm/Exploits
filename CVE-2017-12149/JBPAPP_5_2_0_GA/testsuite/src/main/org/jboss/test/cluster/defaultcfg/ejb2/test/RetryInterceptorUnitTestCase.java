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

import java.util.Properties;
import java.util.Random;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.proxy.ejb.RetryInterceptor;
import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSessionHome;
import org.jboss.test.cluster.testutil.DBSetup;
import org.jboss.test.testbean.interfaces.AComplexPK;
import org.jboss.test.testbean.interfaces.EntityPK;
import org.jboss.test.testbean.interfaces.EntityPKHome;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;

/**
 * Tests the RetryInterceptor.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision $
 */
public class RetryInterceptorUnitTestCase extends RetryInterceptorTestBase
{   
   // NOTE: these variables must be static as apparently a separate instance
   // of this class is created for each test.
   private static boolean deployed0 = false;
   private static boolean deployed1 = false;
   
   /**
    * Create a new RetryInterceptorUnitTestCase.
    * 
    * @param name
    */
   public RetryInterceptorUnitTestCase(String name)
   {
      super(name);
      log = Logger.getLogger(getClass());
   }

   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(RetryInterceptorUnitTestCase.class, "cif-ds.xml");
   }
   
   /**
    * Tests that calls to bean and home calls to SLSB, SFSB, Entity beans
    * will still succeed after a cluster topology change that makes
    * the target list in FamilyClusterInfo completely invalid.
    * 
    * @throws Exception
    */
   public void testRetryInterceptor() throws Exception
   {
      getLog().debug("+++ Enter testRetryInterceptor");
      
      configureCluster();
      
      // Connect to the server0 HA-JNDI

      Properties env = getNamingProperties("org.jboss.naming.NamingContextFactory", 
                                           false);
      InitialContext ctx = new InitialContext(env);
      
      getLog().debug("Looking up the home nextgen.StatefulSession" + getJndiSuffix()+"...");
      StatefulSessionHome  sfsbHome =
         (StatefulSessionHome) ctx.lookup("nextgen_StatefulSession" + getJndiSuffix());
      if (sfsbHome!= null ) getLog().debug("ok");
         getLog().debug("Calling create on StatefulSessionHome" + getJndiSuffix()+"...");
      StatefulSession sfsb = (StatefulSession)sfsbHome.create("Bupple-Dupple");
      assertTrue("statefulSessionHome.create() != null", sfsb != null);
      getLog().debug("ok");
      
      NodeAnswer node1 = sfsb.getNodeState ();
      getLog ().debug (node1);
      
      getLog().debug("Looking up the home nextgen.StatelessSession" + getJndiSuffix()+"...");
      StatelessSessionHome  slsbHome =
      (StatelessSessionHome) ctx.lookup("nextgen_StatelessSession" + getJndiSuffix());
      if (slsbHome!= null ) getLog().debug("ok");
      getLog().debug("Calling create on StatelessSessionHome" + getJndiSuffix()+"...");
      StatelessSession slsb = slsbHome.create();

      getLog().debug("StatelessSession: Now making 1 call on server0 ");
      assertEquals("StatelessSession: Server0 has no calls", 0, slsb.getCallCount());
      
      getLog().debug("Looking up home for nextgen_EntityPK" + getJndiSuffix()+"...");
      EntityPKHome pkHome = (EntityPKHome) ctx.lookup("nextgen_EntityPK" + getJndiSuffix());
      assertTrue("pkHome != null", pkHome != null);
      getLog().debug("ok");

      getLog().debug("Calling find on the home...");
      EntityPK pkBean = null;

      Random rnd = new Random(System.currentTimeMillis());
      int anInt = rnd.nextInt(10);
      int other = rnd.nextInt(10000);
      AComplexPK pk = new AComplexPK(true, anInt, 100, 1000.0, "Marc");
      // Let's try to find the instance
      try 
      {
         pkBean =  pkHome.findByPrimaryKey(pk);
      } 
      catch (Exception e) 
      {
         getLog().debug("Did not find the instance will create it...");
         pkBean = pkHome.create(true, anInt, 100, 1000.0, "Marc");
      }


      assertTrue("pkBean != null", pkBean != null);
      getLog().debug("ok");

      getLog().debug("Setting otherField to " + other + "...");
      pkBean.setOtherField(other);
      getLog().debug("ok");
      
      
      // Reconfigure the cluster so the existing targets are invalid
      reconfigureCluster();
      
      // Make calls on the beans in another thread so we can terminate
      // after a reasonable period of time if the RetryInterceptor is looping
      MultiRetryCaller caller = new MultiRetryCaller(env, slsbHome, 
            slsb, sfsbHome, sfsb, node1, pkHome, pkBean, other,
            pk, getLog());      
      executeRetryTest(caller);

      getLog().debug("+++ Exit testRetryInterceptor");
   }
   
   public void testDeferredRecovery() throws Exception
   {
      deferredRecoveryTest(true);
   }
   
   /**
    * Tests that the retry interceptor works properly if the naming context
    * is established via RetryInterceptor.setRetryEnv()
    * and auto-discovery is disabled.
    *  
    * @throws Exception
    */
   public void testSetRetryEnv() throws Exception
   {
      getLog().debug("+++ Enter testSetRetryEnv");
      
      Properties env = getNamingProperties("org.jnp.interfaces.NamingContextFactory", false);
      try
      {
         RetryInterceptor.setRetryEnv(env);
         InitialContext ctx = new InitialContext(env);
         
         sfsbTest(ctx, env);
         
      }
      finally
      {
         RetryInterceptor.setRetryEnv(null);
      }
      
      getLog().debug("+++ Exit testSetRetryEnv");
   }

   protected boolean isDeployed0()
   {
      return deployed0;
   }

   protected void setDeployed0(boolean deployed)
   {
      deployed0 = deployed;
   }

   protected boolean isDeployed1()
   {
      return deployed1;
   }

   protected void setDeployed1(boolean deployed)
   {
      deployed1 = deployed;
   }

}
