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
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb2.basic.interfaces.NodeAnswer;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatefulSession;
import org.jboss.test.testbean.interfaces.StatefulSessionHome;

/**
 * Tests that a restart of ClusterPartition works properly.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class PartitionRestartUnitTestCase extends JBossClusteredTestCase
{
   /**
    * Create a new PartitionRestartUnitTestCase.
    * 
    * @param name
    */
   public PartitionRestartUnitTestCase(String name)
   {
      super(name);
   }         

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(PartitionRestartUnitTestCase.class, "partition-restart-jboss-beans.xml, partition-restart.jar");
      return t1;
   }   
   
   public void testStatefulBeanFailover() 
   throws Exception
   {       
      getLog().debug("testStatefulBeanFailover");
      
      MBeanServerConnection[] adaptors = this.getAdaptors(); 
      
      String[] urls = getNamingURLs();
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, urls[0]);
      Context ctx = new InitialContext(env);
      getLog().debug("OK");
      
      getLog().debug("");
      getLog().debug("Looking up the home nextgen.StatefulSession...");
      StatefulSessionHome  statefulSessionHome =
      (StatefulSessionHome) ctx.lookup("nextgen_StatefulSession");
      if (statefulSessionHome!= null ) getLog().debug("ok");
         getLog().debug("Calling create on StatefulSessionHome...");
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
      
      assertTrue ("Failover has occured", !node1.nodeId.equals (node2.nodeId));
      
      assertTrue ("Value is identical on replicated node", 
                  "Bupple-Dupple".equals (node1.answer) &&
                  node1.answer.equals (node2.answer) );

      // Stop and restart the ClusterPartition on node1
      restartPartition(adaptors[0]);
      
      // Let the cluster stabilize
      sleep(2000);
      
      // we change our name to see if it replicates to node 1
      //
      statefulSession.setName ("Changed");
      
      // now we travel back to node 1
      System.setProperty ("JBossCluster-DoFail", "once");
      node1 = statefulSession.getNodeState ();
      getLog ().debug (node1);
      
      assertTrue ("Failover has occured", !node1.nodeId.equals (node2.nodeId));
      
      assertTrue ("Value is identical on replicated node", "Changed".equals (node1.answer) );      
      
      statefulSession.remove();
      getLog().debug("ok");
   }
   
   
   protected void restartPartition(MBeanServerConnection adaptor) throws Exception
   {
      ObjectName partition = new ObjectName("jboss:service=RestartPartition");
      
      Object[] params = new Object[0];
      String[] types = new String[0];
      adaptor.invoke(partition, "stop", params, types);
      
      sleep(2000);
      
      adaptor.invoke(partition, "start", params, types);
      
      sleep(2000);
   }

}
