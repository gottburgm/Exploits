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
package org.jboss.test.cluster.multicfg.ejb2.test;


import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSessionHome;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;

import org.jboss.test.JBossClusteredTestCase;

/**
 * Test SLSB for load-balancing behaviour
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class BeanUnitTestCase extends JBossClusteredTestCase
{
   static boolean deployed = false;
   public static int test = 0;
   static Date startDate = new Date();
   
   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
   
   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);
   
   public BeanUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(BeanUnitTestCase.class, "testbeancluster.jar");
      return t1;
   }

   public void testStatelessBeanLoadBalancing() 
      throws Exception
   {       
      getLog().debug(++test+"- "+"Trying the context...");
      
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);
      
      getLog().debug("Test Stateless Bean load-balancing");
      getLog().debug("==================================");
      getLog().debug(++test+"- "+"Looking up the home nextgen.StatelessSession...");
      StatelessSessionHome  statelessSessionHome =
      (StatelessSessionHome) ctx.lookup("nextgen_StatelessSession");
      if (statelessSessionHome!= null ) getLog().debug("ok");
         getLog().debug(++test+"- "+"Calling create on StatelessSessionHome...");
      StatelessSession statelessSession =
      (StatelessSession)statelessSessionHome.create();
      assertTrue("statelessSessionHome.create() != null", statelessSession != null);
      getLog().debug("ok");
      
      getLog().debug(++test+"- "+"Calling getEJBHome() on StatelessSession...");
      assertTrue("statelessSession.getEJBHome() != null", statelessSession.getEJBHome() != null);
      getLog().debug("ok");
      
      getLog().debug(++test+"- "+"Reseting the number of calls made on beans (making 2 calls)... ");
      for (int i=0; i<6; i++)
      {
         getLog().debug(++test+"- "+" Reseting number... ");
         statelessSession.resetNumberOfCalls ();         
      }
      
      getLog().debug(++test+"- "+"Now making 20 calls on this remote... ");
      for (int i=0; i<20; i++)
      {
         getLog().debug(++test+"- "+" Calling remote... ");
         statelessSession.makeCountedCall ();         
      }
      
      getLog().debug(++test+"- "+"Getting the number of calls that have been performed on each bean... ");
      long node1 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"One node has received: " + node1);
      
      long node2 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"The other node has received: " + node2);
      
      if (node1 == node2 &&
          node1 == 10)
      {
         getLog().debug(++test+"- "+"Test is ok.");
      }
      else
      {
         getLog().debug(++test+"- "+"Something wrong has happened! Calls seems not to have been load-balanced.");
         fail ("Calls have not been correctly load-balanced on the SLSB remote interface.");
      }
      
      statelessSession.remove();
      getLog().debug("ok");
   }
   
   public void testRoundRobin() throws Exception
   {
      getLog().debug("+++ Enter testRoundRobin");
      
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);
      
      // Do 4 home lookups in case different lookups lead to different patterns
      for (int i = 0; i < 4; i++)
      {
         StatelessSessionHome  home =
            (StatelessSessionHome) ctx.lookup("nextgen_StatelessSession");
         assertTrue("home != null", home != null);
         getLog().debug("lookup " + i + " ok");
         
         String last = null;
         for (int j = 0; j < 10; j++)
         {
            StatelessSession slsb = home.create();
            assertTrue("slsb != null", slsb != null);
            getLog().debug("create " + j + " ok");
            
            for (int k = 0; k < 3; k++)
            {
               String cur = slsb.getBindAddress();
               if (cur == null)
               {
                  getLog().debug("jboss.bind.address property not set; aborting");
                  return;
               }
               
               assertFalse("Target switched", cur.equals(last));
               last = cur;
            }
            
            getLog().debug("target switching " + j + " ok");
         }
      }
      
      getLog().debug("+++ Exit testRoundRobin");
   }

   public void testStatelessBeanColocation() throws Exception
   {       
      getLog().debug(++test+"- "+"Trying the context...");
      
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);
      
      getLog().debug("Test Stateless Bean colocation");
      getLog().debug("==================================");
      getLog().debug(++test+"- "+"Looking up the home nextgen.StatelessSession...");
      StatelessSessionHome  statelessSessionHome =
      (StatelessSessionHome) ctx.lookup("nextgen_StatelessSession");
      if (statelessSessionHome!= null ) getLog().debug("ok");
         getLog().debug(++test+"- "+"Calling create on StatelessSessionHome...");
      StatelessSession statelessSession = (StatelessSession)statelessSessionHome.create();
      assertTrue("statelessSessionHome.create() != null", statelessSession != null);
      getLog().debug("ok");

      getLog().debug(++test+"- "+"reset number of calls");
      statelessSession.resetNumberOfCalls();
      statelessSession.resetNumberOfCalls();

      // This should make two calls on the same node
      getLog().debug(++test+"- "+"callBusinessMethodB");
      String jndiURL = urls[0] + "/nextgen_StatelessSession";
      statelessSession.callBusinessMethodB(jndiURL);
      
      getLog().debug(++test+"- "+"Getting the number of calls that have been performed on each bean... ");
      long node1 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"One node has received: " + node1);
      
      long node2 = statelessSession.getCallCount();
      getLog().debug(++test+"- "+"The other node has received: " + node2);
      
      if ((node1 == 2 && node2 == 0) || (node1 == 0 && node2 == 2))
      {
         getLog().debug(++test+"- "+"Test is ok.");
      }
      else
      {
         getLog().debug(++test+"- "+"Something wrong has happened! Calls should have been colocated.");
         fail ("Calls have not been correctly colocated.");
      }
      getLog().debug("ok");
   }            
   
   public void testStatefulBeanFailover() 
   throws Exception
   {       
      getLog().debug(++test+"- "+"Trying the context...");
      
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      InitialContext ctx = new InitialContext(env1);
      
      getLog().debug("Test Stateful Bean Failover");
      getLog().debug("==================================");
      getLog().debug(++test+"- "+"Looking up the home nextgen.StatefulSession...");
      StatefulSessionHome  statefulSessionHome =
      (StatefulSessionHome) ctx.lookup("nextgen_StatefulSession");
      if (statefulSessionHome!= null ) getLog().debug("ok");
         getLog().debug(++test+"- "+"Calling create on StatefulSessionHome...");
      StatefulSession statefulSession =
      (StatefulSession)statefulSessionHome.create("Bupple-Dupple");
      assertTrue("statefulSessionHome.create() != null", statefulSession != null);
      getLog().debug("ok");
      
      NodeAnswer node1 = statefulSession.getNodeState ();
      getLog ().debug (node1);
      
      // Now we switch to the other node, simulating a failure on node 1
      //
      System.setProperty ("JBossCluster-DoFail", "once");
      NodeAnswer node2 = statefulSession.getNodeState ();      
      getLog ().debug (node2);
      
      assertTrue ("No failover has occured!", !node1.nodeId.equals (node2.nodeId));
      
      assertTrue ("Value is not identical on replicated node", "Bupple-Dupple".equals (node1.answer) &&
                     node1.answer.equals (node2.answer) );

      // we change our name to see if it replicates to node 1
      //
      statefulSession.setName ("Changed");
      
      // now we travel back on node 1
      //
      System.setProperty ("JBossCluster-DoFail", "once");
      node1 = statefulSession.getNodeState ();
      getLog ().debug (node1);
      
      assertTrue ("No failover has occured!", !node1.nodeId.equals (node2.nodeId));
      
      assertTrue ("Value is not identical on replicated node", "Changed".equals (node1.answer) );      
      
      statefulSession.remove();
      getLog().debug("ok");
   }  
}
