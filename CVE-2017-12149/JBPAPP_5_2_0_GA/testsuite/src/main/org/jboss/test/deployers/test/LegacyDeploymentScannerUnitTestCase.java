/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Inc., and individual contributors as indicated
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
package org.jboss.test.deployers.test;

import java.net.URL;

import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.NoSuchDeploymentException;

/**
 * Test the JBossTools legacy DeploymentScanner usage.
 *
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class LegacyDeploymentScannerUnitTestCase extends AbstractDeployTestBase
{

   /** The deployment scanner MBean name. */
   private static final String DEPLOYMENT_SCANNER_MBEAN = "jboss.deployment:flavor=URL,type=DeploymentScanner";
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new LegacyDeploymentScannerUnitTestCase("testDeploy"));
      suite.addTest(new LegacyDeploymentScannerUnitTestCase("testlistDeployedURLs"));      
      suite.addTest(new LegacyDeploymentScannerUnitTestCase("testDeploymentMgrRedeploy"));     
      suite.addTest(new LegacyDeploymentScannerUnitTestCase("testUndeploy"));
      
      return suite;
   }
   
   public LegacyDeploymentScannerUnitTestCase(String name)
   {
      super(name);
   }

   public void testDeploy() throws Exception
   {
      final ObjectName scanner = new ObjectName(DEPLOYMENT_SCANNER_MBEAN);      

      // The deployment
      URL deployment = getDeployURL(EMTPY_DEPLOYMENT);
      assertNotNull(EMTPY_DEPLOYMENT, deployment);
      
      // Suspend
      invoke(scanner, "stop" , new Object[0], new String[0]);
      try
      {
         // Add the deployment
         invoke(scanner, "addURL", new Object[] { deployment }, new String[] { URL.class.getName() });
      }
      finally
      {
         // Resume
         invoke(scanner, "start" , new Object[0], new String[0]);
      }
      
      // Wait for HDScanner
      Thread.sleep(8000);
      
      ManagedDeployment managedDeployment = getManagementView().getDeployment(EMTPY_DEPLOYMENT);
      assertNotNull(managedDeployment);

   }
   
   public void testUndeploy() throws Exception
   {
      final ObjectName scanner = new ObjectName(DEPLOYMENT_SCANNER_MBEAN); 

      // The deployment
      URL deployment = getDeployURL(EMTPY_DEPLOYMENT);
      assertNotNull(EMTPY_DEPLOYMENT, deployment);
      
      // Suspend
      invoke(scanner, "stop" , new Object[0], new String[0]);
      try
      {
         // Add the deployment
         invoke(scanner, "removeURL", new Object[] { deployment }, new String[] { URL.class.getName() });
      }
      finally
      {
         // Resume
         invoke(scanner, "start" , new Object[0], new String[0]);
      }
      
      // Wait for HDScanner
      Thread.sleep(8000);
      
      try
      {
         ManagedDeployment managedDeployment = getManagementView().getDeployment(EMTPY_DEPLOYMENT);
         fail("deployment not undeployed " + managedDeployment);
      }
      catch(NoSuchDeploymentException ok)
      {
         log.debug("saw NoSuchDeploymentException");
      }
   }
   
   public void testlistDeployedURLs() throws Exception
   {
      final ObjectName scanner = new ObjectName(DEPLOYMENT_SCANNER_MBEAN);
      
      String[] deployments = (String[]) invoke(scanner, "listDeployedURLs", new Object[0], new String[0]);
      
      assertNotNull(deployments);
      assertTrue(deployments.length > 1);
   }

   public void testDeploymentMgrRedeploy() throws Exception
   {
      // Test redeploy using deploymentManager
      DeploymentManager deployMgr = getDeploymentManager();
      // deployMgr.loadProfile(scannerProfile);
      try
      {
         String name = deployMgr.getRepositoryNames(new String[] { EMTPY_DEPLOYMENT })[0];
         redeployCheckComplete(name);
      }
      finally
      {
         deployMgr.releaseProfile();
      }      
   }
   
}
