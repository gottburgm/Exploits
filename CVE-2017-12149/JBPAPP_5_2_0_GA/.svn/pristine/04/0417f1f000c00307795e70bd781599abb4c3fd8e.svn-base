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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossClusteredTestCase;

/**
 * @author Brian Stansberry
 *
 */
public class HASingletonDeployerTestCase extends JBossClusteredTestCase
{
   public static final String SINGLETON_DEPLOYER_ONAME = "jboss.ha:service=TestHASingletonDeployer";
   public static final String SINGLETON_DEPLOYMENT = "jboss.system:service=HASingletonTestThreadPool";
   public static final String SINGLETON_DEPLOYER = "test-deploy-hasingleton-jboss-beans.xml";
   public static final String SINGLETON_BARRIER_ONAME = "jboss.ha:service=TestHASingletonDeployer,type=Barrier";
   public static final Integer CREATED_STATE = new Integer(ServiceMBean.CREATED);
   public static final Integer STARTED_STATE = new Integer(ServiceMBean.STARTED);
   public static final Integer STOPPED_STATE = new Integer(ServiceMBean.STOPPED);
   
   public static final String STD_SINGLETON_DEPLOYER_ONAME = "jboss.ha:service=HASingletonDeployer";
   public static final String STD_SINGLETON_BARRIER_ONAME = "jboss.ha:service=HASingletonDeployer,type=Barrier";
   
   /**
    * Create a new ProfileRepositoryHASingletonDeployerTestCase.
    * 
    * @param name
    */
   public HASingletonDeployerTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(HASingletonDeployerTestCase.class, SINGLETON_DEPLOYER);
   }
   
   /**
    * Accesses the test HASingletonDeployer beans deployed by the test wrapper
    * and validates that deployments are done on the master node and not on
    * the non-master.  Also validates that a Barrier has reached the start
    * state on the master and not on the non-master.  Then undeploys
    * the deployer on the master node and checks service failover. Then undeploys
    * on the other node and redeploys on node1, checking node1 became master
    * with the target services deployed and the Barrier started.  Then deploys 
    * on node0, confirming that node1 remained the master and node0 didn't
    * deploy the target services or bring a BARRIER to STARTED state. 
    * 
    * @throws Exception
    */
   public void testHASingletonDeployer() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      boolean node0Master = isMaster(adaptors[0], false);
      assertFalse(node0Master == isMaster(adaptors[1], false));
      int origMasterIndex = node0Master ? 0 : 1;
      int origNonMasterIndex = node0Master ? 1 : 0;
      
      assertTrue("singleton is not deployed on node " + origMasterIndex, isDeployed(SINGLETON_DEPLOYMENT, adaptors[origMasterIndex]));
      assertFalse("singleton is deployed on node " + origNonMasterIndex, isDeployed(SINGLETON_DEPLOYMENT, adaptors[origNonMasterIndex]));
      
      assertBarrierStatus(adaptors[0], node0Master, false);
      assertBarrierStatus(adaptors[1], !node0Master, false);
      
      undeploy(adaptors[origMasterIndex], SINGLETON_DEPLOYER);
      
      // The singleton is triggered asynchronously, so give it time to work
      sleep(2000);
      
      assertTrue(isDeployed(SINGLETON_DEPLOYMENT, adaptors[origNonMasterIndex]));
      assertFalse(isDeployed(SINGLETON_DEPLOYMENT, adaptors[origMasterIndex]));
      
      assertBarrierStatus(adaptors[origNonMasterIndex], true, false);
      assertFalse(isDeployed(SINGLETON_BARRIER_ONAME, adaptors[origMasterIndex]));
      
      undeploy(adaptors[origNonMasterIndex], SINGLETON_DEPLOYER);
      
      sleep(2000);
      
      assertFalse(isDeployed(SINGLETON_DEPLOYMENT, adaptors[0]));
      assertFalse(isDeployed(SINGLETON_DEPLOYMENT, adaptors[1]));
      
      assertFalse(isDeployed(SINGLETON_BARRIER_ONAME, adaptors[0]));
      assertFalse(isDeployed(SINGLETON_BARRIER_ONAME, adaptors[1]));
      
      deploy(adaptors[1], SINGLETON_DEPLOYER);
      
      sleep(2000);
      
      assertTrue(isDeployed(SINGLETON_DEPLOYMENT, adaptors[1]));
      assertFalse(isDeployed(SINGLETON_DEPLOYMENT, adaptors[0]));
      
      assertBarrierStatus(adaptors[1], true, false);
      assertFalse(isDeployed(SINGLETON_BARRIER_ONAME, adaptors[0]));
      
      deploy(adaptors[0], SINGLETON_DEPLOYER);
      
      sleep(2000);
      
      // per policy, node0 takes over as master
      assertTrue(isDeployed(SINGLETON_DEPLOYMENT, adaptors[origMasterIndex]));
      assertFalse(isDeployed(SINGLETON_DEPLOYMENT, adaptors[origNonMasterIndex]));
      
      assertBarrierStatus(adaptors[0], true, false);
      assertBarrierStatus(adaptors[1], false, false);
   }
   
   /**
    * Accesses the standard HASingletonDeployer beans, validating that one node
    * is the master and the other isn't, and that the HASingleto Barrier is 
    * STARTED on the master and CREATED on the non-master. A basic check that
    * the "all" config is correct. JBAS-6363. 
    * 
    * @throws Exception
    */
   public void testAllConfiguration() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      boolean node0Master = isMaster(adaptors[0], true);
      assertFalse(node0Master == isMaster(adaptors[1], true));
      
      assertBarrierStatus(adaptors[0], node0Master, true);
      assertBarrierStatus(adaptors[1], !node0Master, true);      
   }
   
   protected boolean isDeployed(String deployment, MBeanServerConnection server) throws Exception
   {
      return server.isRegistered(new ObjectName(deployment));
   }
   
   private boolean isMaster(MBeanServerConnection server, boolean stdTest)
   {
      try
      {
         ObjectName oname = new ObjectName(stdTest ? STD_SINGLETON_DEPLOYER_ONAME : SINGLETON_DEPLOYER_ONAME);
         return ((Boolean) server.invoke(oname, "isMasterNode", new Object[]{}, new String[]{})).booleanValue();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private void assertBarrierStatus(MBeanServerConnection server, boolean isMaster, boolean stdTest) throws Exception
   {
      ObjectName oname = new ObjectName(stdTest ? STD_SINGLETON_BARRIER_ONAME : SINGLETON_BARRIER_ONAME);
      Integer state = (Integer) server.getAttribute(oname, "State");
      if (isMaster)
      {
         assertEquals(STARTED_STATE, state);
      }
      else
      {
         assertTrue("State " + state + " is valid", CREATED_STATE.equals(state) || STOPPED_STATE.equals(state));
      }
   }

}
