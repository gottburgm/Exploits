/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.deployment.jbpapp6517;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.deployment.MainDeployerMBean;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;


/**
 * @author bmaxwell
 *
 */
public abstract class CheckCompleteTestCase extends JBossTestCase
{
   public static String objectNameString = "jboss.system:service=MainDeployer";
   
   protected String testEar = "jbpapp-6517.ear";
   protected String testSar = "jbpapp-6517.sar";
   
   protected String checkCompleteDeployerAll = "file://" + new File("resources/org/jboss/test/deployment/jbpapp6517/checkSubDeploymentCompleteDeployerAll-jboss-beans.xml").getAbsolutePath();
   protected String checkCompleteDeployerEAR = "file://" + new File("resources/org/jboss/test/deployment/jbpapp6517/checkSubDeploymentCompleteDeployerEAR-jboss-beans.xml").getAbsolutePath();
   protected String checkCompleteDeployerSAR = "file://" + new File("resources/org/jboss/test/deployment/jbpapp6517/checkSubDeploymentCompleteDeployerSAR-jboss-beans.xml").getAbsolutePath();
   
   protected String earEjb3JMXName = "jboss.j2ee:ear=jbpapp-6517.ear,jar=jbpapp-6517-ejb.jar,name=HelloBean,service=EJB3";
   protected String sarEjb3JMXName = "jboss.j2ee:ear=jbpapp-6517.sar,jar=jbpapp-6517-ejb.jar,name=HelloBean,service=EJB3";

   private Object[] previousSecurity = new Object[2];
   
   public CheckCompleteTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      setSecurity(new SimplePrincipal("admin"), "admin");
   } 
   protected void tearDown() throws Exception
   {   
      super.tearDown();
      setSecurity((Principal)previousSecurity[0], previousSecurity[1]);      
   }
   
   // TODO the tests are moved into different files since there is a bug where we can't deploy/undeploy the deployer - move them back when bug is fixed
   
   // this is without the deployer
//   public void testDefaultWithoutCheckSubDeploymentCompleteDeployer()
//   {
//      // deploy ear & eat expected exception
//      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
//      // undeploy ear
//      // call fail if not valid
//      verifyCheckCompleteNotCalled(testEar, earEjb3JMXName);
//      
//      // Repeat for sar
//      verifyCheckCompleteNotCalled(testSar, sarEjb3JMXName);
//   }
//   
//   public void testCheckSubDeploymentCompleteDeployerAll()
//   {      
//      // deploy CheckSubDeploymentCompleteDeployer configured to check all
//      deployFailOnException(checkCompleteDeployerAll);
//      
//      // deploy ear & eat expected exception
//      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
//      // undeploy ear
//      // call fail if not valid
//      verifyCheckCompleteCalled(testEar, earEjb3JMXName);
//
//      // repeat for sar
//      verifyCheckCompleteCalled(testSar, sarEjb3JMXName);
//      
//      // undeploy CheckSubDeploymentCompleteDeployer configured to check all
//      undeployLogException(checkCompleteDeployerAll);      
//   }
//   
//   public void testCheckSubDeploymentCompleteDeployerEAROnly()
//   {
//      // deploy the CheckSubDeploymentCompleteDeployer that only acts on ears
//      deployFailOnException(checkCompleteDeployerEAR);
//      
//      // deploy ear & eat expected exception
//      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
//      // undeploy ear
//      // call fail if not valid
//      verifyCheckCompleteCalled(testEar, earEjb3JMXName);
//      
//      // same as above, except in the case of the sar, the ejb3 should still be deployed since the checkCompleteDeployerEAR should only check for .ear files 
//      verifyCheckCompleteNotCalled(testSar, sarEjb3JMXName);
//      
//      // undeploy the CheckSubDeploymentCompleteDeployer that only acts on ears
//      undeployLogException(checkCompleteDeployerEAR);
//   }
//   
//   public void testCheckSubDeploymentCompleteDeployerSAROnly()
//   {                  
//      // deploy the CheckSubDeploymentCompleteDeployer that only acts on sars
//      deployFailOnException(checkCompleteDeployerSAR);
//      
//      // deploy sar & eat expected exception
//      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
//      // undeploy sar
//      // call fail if not valid
//      verifyCheckCompleteCalled(testSar,sarEjb3JMXName);
//      
//      // same as above, except in the case of the ear, the ejb3 should still be deployed since the checkCompleteDeployerEAR should only check for .sar files 
//      verifyCheckCompleteNotCalled(testEar, earEjb3JMXName);
//      
//      // undeploy the CheckSubDeploymentCompleteDeployer that only acts on sars
//      undeployLogException(checkCompleteDeployerSAR);
//   }
   
   // helper methods from here on
   
   protected void verifyCheckCompleteNotCalled(String testApp, String jmxName)
   {      
      // deploy ear & eat expected exception
      deployTestAppEatException(testApp);    
      
      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
      boolean ejbExists = existsInJMX(jmxName);
      
      // undeploy ear
      undeployLogException(testApp);
      
      if ( ! ejbExists ) 
         fail("When the CheckSubDeploymentCompleteDeployer checkcomplete is not called, the test EJB3 in " + testApp + " should be deployed: " + jmxName);      
   }
   
   protected void verifyCheckCompleteCalled(String testApp, String jmxName)
   {      
      // deploy ear & eat expected exception
      deployTestAppEatException(testApp);    
      
      // check jmx or jndi - with CheckSubDeploymentCompleteDeployer the EJB3 should not be in jmx or jndi
      boolean ejbExists = existsInJMX(jmxName);
      
      // undeploy ear
      undeployLogException(testApp);
      
      if ( ejbExists ) 
         fail("When the CheckSubDeploymentCompleteDeployer checkcomplete is called, the test EJB3 in " + testApp + " should not be deployed: " + jmxName);      
   }
   
   // deploy/undeploy helper methods to eat exceptions and fail if necessary to make test code not need a lot of try/catch
   
   protected void undeployLogException(String path)
   {
      try 
      {
         undeploy(path);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   protected void deployFailOnException(String path)
   {
      try
      {
         deploy(path);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Failed to deploy: " + path);
      }
   }
   
   protected void deployTestAppEatException(String path)
   {
      try
      {
         deploy(path);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         // do nothing since it should fail to deploy
         return;
      }
      // fail test if no exception because it should have failed to deploy
      fail("Test app: " + path + " should have failed to deploy because of the microcontainer beans throwing exception in create");
   }
   
   // See if a jmxName exists in JMX - fail test on unexpected errors
   private boolean existsInJMX(String jmxName)
   {
      ObjectName objectName = null;
      MBeanServerConnection mbeanServer = null;
      try
      {
         mbeanServer = getRMIServer();
         objectName = new ObjectName(jmxName);
      }
      catch ( Exception e )
      {
         fail(e.getMessage());
      }
      
      try
      {
         MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectName);
      }
      catch ( InstanceNotFoundException infe )
      {         
         return false;
      }
      catch ( Exception e )
      {
         // fail if an exception occurs trying to call getMBeanInfo
         fail(e.getMessage());
      }
      return true;
   }
   
   // helper methods to get access to the mbean server and main deployer
   
   private static MBeanServerConnection getRMIServer() throws Exception
   {
     String connectorName = "jmx/rmi/RMIAdaptor";
     RMIAdaptor server = (RMIAdaptor) new InitialContext().lookup(connectorName);
     //invoke MainDeployer MBean service
     return server;
   }

   private void setSecurity(Principal username, Object password)
   {
      previousSecurity[0] = SecurityAssociation.getPrincipal();
      previousSecurity[1] = SecurityAssociation.getCredential();
      
      SecurityAssociation.setPrincipal(username);
      SecurityAssociation.setCredential(password);       
   }
}
