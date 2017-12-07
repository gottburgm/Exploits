/*
* JBoss, Home of Professional Open Source
* Copyright 2009, JBoss Inc., and individual contributors as indicated
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

import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class AbstractDeployTestBase extends JBossTestCase
{

   /** A basic failing deployment. */
   final static String FAILING_DEPLOYMENT = "deployers-failing-jboss-beans.xml";
   /** A empty deployment, this will deploy ok. */
   final static String EMTPY_DEPLOYMENT = "deployers-empty-jboss-beans.xml";
   /** A simple nested deployment. */
   final static String NESTED_DEPLOYMENT = "profileservice-datasource.ear";
   
   /** The deployers target profile. */
   final static ProfileKey deployersKey = new ProfileKey("deployers");
   
   /** windoze. */
   final static boolean isWindoze;
   
   /** The deployment manager. */
   private DeploymentManager deployMgr;
   /** The management view. */
   private ManagementView mgtView;
   
   static
   {
      String osName = System.getProperty("os.name");
      isWindoze = osName.contains("Win");
   }
   
   public AbstractDeployTestBase(String name)
   {
      super(name);
   }
 
   static boolean isIgnoreRemove()
   {
      return isWindoze;
   }
   
   
   void deployFailed(boolean isCopyContent) throws Exception
   {
      DeploymentProgress start = distributeAndStart(FAILING_DEPLOYMENT, isCopyContent);
      assertFailed(start);
      assertDeploymentState(start.getDeploymentID(), DeploymentState.FAILED);
   }

   void deployEmpty(boolean isCopyContent) throws Exception
   {
      DeploymentProgress start = distributeAndStart(EMTPY_DEPLOYMENT, isCopyContent);
      assertComplete(start);
      assertDeploymentState(start.getDeploymentID(), DeploymentState.STARTED);
   }

   DeploymentProgress distributeAndStart(String deploymentName, boolean copyContent) throws Exception
   {
      return distributeAndStart(deploymentName, copyContent, true);
   }
  
   DeploymentProgress distributeAndStart(String deploymentName, boolean copyContent, boolean checkStopped) throws Exception
   {
      return distributeAndStart(deploymentName, deploymentName, copyContent, true);
   }
   
   /**
    * Distribute and start a deployment.
    * 
    * @param deployment the deployment
    * @param deploymentName the new (server side) deployment name
    * @param copyContent is copyContent
    * @param checkStopped check the if the deployment is stopped after distribute
    * @return the DeploymentProgress of the start operation
    * @throws Exception
    */
   DeploymentProgress distributeAndStart(String deployment, String deploymentName, boolean copyContent, boolean checkStopped) throws Exception
   {
      // The deployment manager
      DeploymentManager deployMgr = getDeploymentManager();

      // Distribute
      DeploymentProgress distribute = deployMgr.distribute(deploymentName,
            getDeployURL(deployment), copyContent);
      distribute.run();
      // Distribute always has to complete
      assertComplete(distribute);
      // check if the app is stopped
      if(checkStopped)
         assertDeploymentState(distribute.getDeploymentID(), DeploymentState.STOPPED);

      // Get the repository names
      String[] uploadedNames = distribute.getDeploymentID().getRepositoryNames();
      assertNotNull(uploadedNames);

      // Start
      DeploymentProgress start = deployMgr.start(uploadedNames);
      start.run();
      // Return the start deployment progress
      return start;
   }
   
   DeploymentProgress distributeAndStart(String deploymentName, DeploymentOption... options) throws Exception
   {
      return distributeAndStart(deploymentName, deploymentName, options);
   }
   
   /**
    * Distribute and start a deployment
    * 
    * @param deployment the deployment
    * @param deploymentName the new (server side) deployment name
    * @param options the deployment options
    * @return the DeploymentProgress of the start operation
    * @throws Exception
    */
   DeploymentProgress distributeAndStart(String deployment, String deploymentName, DeploymentOption... options) throws Exception
   {
      // The deployment manager
      DeploymentManager deployMgr = getDeploymentManager();

      // Distribute
      DeploymentProgress distribute = deployMgr.distribute(deploymentName,
            getDeployURL(deployment), options);
      distribute.run();
      // Distribute always has to complete
      assertComplete(distribute);
      // check if the app is stopped
      assertDeploymentState(distribute.getDeploymentID(), DeploymentState.STOPPED);

      // Get the repository names
      String[] uploadedNames = distribute.getDeploymentID().getRepositoryNames();
      assertNotNull(uploadedNames);

      // Start
      DeploymentProgress start = deployMgr.start(uploadedNames);
      start.run();
      // Return the start deployment progress
      return start;      
   }

   void redeployCheckComplete(String name) throws Exception
   {
      // The deployment manager
      DeploymentManager deployMgr = getDeploymentManager();

      // Redeploy
      DeploymentProgress redeploy = deployMgr.redeploy(name);
      redeploy.run();
      assertComplete(redeploy);
      assertDeploymentState(redeploy.getDeploymentID(), DeploymentState.STARTED);
   }

   void prepareCheckComplete(String name) throws Exception
   {
      // The deployment manager
      DeploymentManager deployMgr = getDeploymentManager();

      // Prepare
      DeploymentProgress prepare = deployMgr.prepare(name);
      prepare.run();
      assertComplete(prepare);
   }

   void stopAndRemove(String[] names) throws Exception
   {
      // The deployment manager
      DeploymentManager deployMgr = getDeploymentManager();

      try
      {
         DeploymentProgress stop = deployMgr.stop(names);
         stop.run();
         assertComplete(stop);
         assertDeploymentState(stop.getDeploymentID(), DeploymentState.STOPPED);
      }
      catch(Exception e)
      {
         log.debug("stopAndRemove Failed ", e);
         throw e;
      }
      finally
      {
         DeploymentProgress remove = deployMgr.remove(names);
         remove.run();
         // Don't check this on windows
         if(isIgnoreRemove() == false)
         {
            assertComplete(remove);
            String name = remove.getDeploymentID().getNames()[0];
            ManagementView mgtView = getManagementView();
            try
            {
               mgtView.getDeployment(name);
               fail("Did not see NoSuchDeploymentException");
            }
            catch(NoSuchDeploymentException ok)
            {
               //
            }
         }
      }
   }

   void assertFailed(DeploymentProgress progress) throws Exception
   {
      assertFalse(progress.getDeploymentStatus().isCompleted());
      assertTrue(progress.getDeploymentStatus().isFailed());
   }

   void assertDeploymentState(DeploymentID DtID, DeploymentState state) throws Exception
   {
      String name = DtID.getNames()[0];
      ManagementView mgtView = getManagementView();
      ManagedDeployment md = mgtView.getDeployment(name);
      assertNotNull(name, md);
      assertEquals("deployment: " + name, state, md.getDeploymentState());
      log.debug(md.getSimpleName() + " " + md.getTypes());
   }

   void assertComplete(DeploymentProgress progress) throws Exception
   {
      if(progress.getDeploymentStatus().isFailed())
      {
         throw new RuntimeException("deployment failed.", progress.getDeploymentStatus().getFailure());
      }
      //
      assertTrue(progress.getDeploymentStatus().isCompleted());
   }

   DeploymentManager getDeploymentManager() throws Exception
   {
      if(this.deployMgr == null)
      {
         this.deployMgr = getProfileService().getDeploymentManager();
      }
      return deployMgr;
   }

   ManagementView getManagementView() throws Exception
   {
      if(this.mgtView == null)
      {
         this.mgtView = getProfileService().getViewManager();
      }
      this.mgtView.load();
      return this.mgtView;
   }

   ProfileService getProfileService() throws Exception
   {
      InitialContext ctx = getInitialContext();
      return (ProfileService) ctx.lookup("ProfileService");
   }
   
}

