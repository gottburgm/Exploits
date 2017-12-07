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
package org.jboss.test.profileservice.test;

import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.test.profileservice.ejb2x.BeanHome;
import org.jboss.test.profileservice.ejb2x.BeanRemote;
import org.jboss.test.profileservice.ejb3x.BeanRemote3x;

/**
 * Profile service DeploymentManager tests
 * @author Scott.Stark@jboss.org
 * @version $Revision: 86808 $
 */
public class DeployUnitTestCase extends AbstractProfileServiceTest implements ProgressListener
{
   private ProgressEvent eventInfo;
   private long eventCount = 0;

   public DeployUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      DeploymentManager deployMgr = getDeploymentManager();
      deployMgr.loadProfile(getProfileKey());
   }

   @Override
   protected void tearDown() throws Exception
   {
      DeploymentManager deployMgr = getDeploymentManager();
      deployMgr.releaseProfile();
      super.tearDown();
   }

   public void progressEvent(ProgressEvent eventInfo)
   {
      eventCount ++;
      this.eventInfo = eventInfo;
      getLog().debug(eventInfo);
   }

   @SuppressWarnings("deprecation")
   protected void testDeployment(String name, String type, ManagedDeploymentTester tester) throws Exception
   {
      DeploymentManager deployMgr = getDeploymentManager();
      URL contentURL = getDeployURL(name);
      assertNotNull(contentURL);
      getLog().debug(contentURL);
      // TODO - hack to get off JDK's url handling
      String urlString = contentURL.toExternalForm();
      int p = urlString.indexOf(":/");
      contentURL = new URL("vfszip" + urlString.substring(p));
      getLog().debug(contentURL);

      DeploymentStatus status;
      DeploymentProgress progress = deployMgr.distribute(name, contentURL, true);
      progress.addProgressListener(this);
      progress.run();
      String[] uploadedNames = {};
      try
      {
         status = progress.getDeploymentStatus();
         assertTrue("DeploymentStatus.isCompleted: " + status, status.isCompleted());
         // It should not be running yet
         assertFalse("DeploymentStatus.isRunning: " + status, status.isRunning());
         assertFalse("DeploymentStatus.isFailed: " + status, status.isFailed());

         // Get the unique deployment name
         uploadedNames = progress.getDeploymentID().getRepositoryNames();
         getLog().debug("Uploaded deployment names: "+Arrays.asList(uploadedNames));
         // Now start the deployment
         progress = deployMgr.start(uploadedNames);
         progress.addProgressListener(this);
         progress.run();
         try
         {
            status = progress.getDeploymentStatus();
            assertTrue("DeploymentStatus.isCompleted: " + status, status.isCompleted());
            assertFalse("DeploymentStatus.isRunning: " + status, status.isRunning());
            assertFalse("DeploymentStatus.isFailed: " + status, status.isFailed());
            // Check for a
            ManagementView mgtView = getManagementView();
            ManagedDeployment deployment = mgtView.getDeployment(uploadedNames[0]);
            assertNotNull(deployment);
            getLog().info("Found " + type + " deployment: " + deployment);
            Set<String> types = deployment.getTypes();
            if (types != null && types.isEmpty() == false)
               assertTrue("Missing type: " + type + ", available: " + types, types.contains(type));
            if (tester != null)
            {
               tester.testManagedDeployment();
            }
         }
         finally
         {
            //Thread.sleep(15 * 1000); // 15 secs >> more than it takes for reaper to run :-)

            // Stop/remove the deployment
            progress = deployMgr.stop(uploadedNames);
            progress.addProgressListener(this);
            progress.run();
            status = progress.getDeploymentStatus();
            assertTrue("DeploymentStatus.isCompleted: " + status, status.isCompleted());
            assertFalse("DeploymentStatus.isFailed: " + status, status.isFailed());
         }
      }
      finally
      {
         progress = deployMgr.remove(uploadedNames);
         progress.addProgressListener(this);
         progress.run();
         status = progress.getDeploymentStatus();
         assertTrue("DeploymentStatus.isCompleted: " + status, status.isCompleted());
         assertFalse("DeploymentStatus.isFailed: " + status, status.isFailed());
      }
   }

   public void testWarDeployment() throws Exception
   {
      String name = "testWarDeployment.war";
      testDeployment(name, "war", null);
   }

   public void testEjb3xDeployment() throws Exception
   {
      String name = "testEjb3xDeployment.jar";
      ManagedDeploymentTester tester = new ManagedDeploymentTester()
      {
         public void testManagedDeployment() throws Exception
         {
            InitialContext ic = new InitialContext();
            BeanRemote3x bean = (BeanRemote3x) ic.lookup("BeanImpl3x/remote-org.jboss.test.profileservice.ejb3x.BeanRemote3x");
            String entry1 = (String) bean.getEnvEntry("entry1");
            assertEquals("entry1Value", entry1);
         }
      };
      testDeployment(name, "ejb3x", tester);
   }

   public void testEjb2xDeployment() throws Exception
   {
      String name = "testEjb2xDeployment.jar";
      ManagedDeploymentTester tester = new ManagedDeploymentTester()
      {
         public void testManagedDeployment() throws Exception
         {
            InitialContext ic = new InitialContext();
            BeanHome home = (BeanHome) ic.lookup("DeployUnitTestCase-testEjb2xDeployment");
            BeanRemote bean = home.create();
            String entry1 = (String) bean.getEnvEntry("entry1");
            assertEquals("entry1Value", entry1);
         }
      };
      testDeployment(name, "ejb2x", tester);
   }

   public void testEarDeployment() throws Exception
   {
      String name = "testEarDeployment.ear";
      ManagedDeploymentTester tester = new ManagedDeploymentTester()
      {
         public void testManagedDeployment() throws Exception
         {
            // Validate the ejb
            InitialContext ic = new InitialContext();
            BeanHome home = (BeanHome) ic.lookup("DeployUnitTestCase-testEjb2xDeployment");
            BeanRemote bean = home.create();
            String entry1 = (String) bean.getEnvEntry("entry1");
            assertEquals("entry1Value", entry1);
            // TODO, validate the war
         }
      };
      testDeployment(name, "ear", tester);
   }

   public void testMCBeansDeployment() throws Exception
   {
      String name = "testMCBeansDeployment.beans";
      testDeployment(name, "beans", null);
   }

   public void testSarDeployment() throws Exception
   {
      String name = "testSarDeployment.sar";
      testDeployment(name, "sar", null);
   }

   private interface ManagedDeploymentTester
   {
      void testManagedDeployment() throws Exception;
   }
}
