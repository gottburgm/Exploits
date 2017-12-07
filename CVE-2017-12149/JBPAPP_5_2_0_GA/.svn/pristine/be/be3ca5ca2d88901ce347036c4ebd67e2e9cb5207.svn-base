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
package org.jboss.test.deployers.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Basic DeploymentManager test.
 *
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DeploymentManagerUnitTestCase extends AbstractDeployTestBase
{

   /** A deployment picked up by the HDScanner. */
   private final static String HD_DEPLOYMENT = "hd-jboss-beans.xml";
   
   public DeploymentManagerUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test the available profiles.
    *
    * @throws Exception
    */
   public void testAvaiableProfiles() throws Exception
   {
      Collection<ProfileKey> keys = getDeploymentManager().getProfiles();
      assertNotNull(keys);
      log.debug("available keys: " + keys);
      keys.contains(new ProfileKey("applications"));
      keys.contains(deployersKey);
   }

   /**
    * Test a override of the applications, without
    * removing/stopping them before.
    *
    * @throws Exception
    */
   public void testDistributeOverride() throws Exception
   {
      if(isWindoze)
      {
         // ignore tests on windows platforms, since the remove will fail most likely 
         return;
      }
      final String deploymentName = getName() + ".ear";
      try
      {
         for(int i = 0; i < 5; i++)
         {
            //
            DeploymentProgress start = distributeAndStart(NESTED_DEPLOYMENT, deploymentName, true, false);
            assertComplete(start);
            
            // disable stopped check, as it was started before
            start = distributeAndStart(NESTED_DEPLOYMENT, deploymentName, true, false);
            assertComplete(start);
            assertDeploymentState(start.getDeploymentID(), DeploymentState.STARTED);

            Thread.sleep(5);
            
            redeployCheckComplete(deploymentName);
         }
      }
      catch(Exception e)
      {
         log.debug("Failed ", e);
         throw e;
      }
      stopAndRemove(new String[] { deploymentName });
   }

   /**
    * Basic copyContent test to the default location.
    *
    * @throws Exception
    */
   public void testCopyContent() throws Exception
   {
      try
      {
         // failed
         deployFailed(true);
         // complete
         deployEmpty(true);
         // Test redeploy
         assertTrue(getDeploymentManager().isRedeploySupported());
         redeployCheckComplete(EMTPY_DEPLOYMENT);
         // TODO implement prepare
         prepareCheckComplete(EMTPY_DEPLOYMENT);
      }
      catch(Exception e)
      {
         log.debug("Failed ", e);
         throw e;
      }
      stopAndRemove(new String[]
         { FAILING_DEPLOYMENT, EMTPY_DEPLOYMENT } );
   }

   /**
    * Basic noCopyContent test.
    *
    * @throws Exception
    */
   public void testNoCopyContent() throws Exception
   {
      try
      {
         // failed
         deployFailed(false);
         // complete
         deployEmpty(false);
         // test redeploy
         assertTrue(getDeploymentManager().isRedeploySupported());
         redeployCheckComplete(EMTPY_DEPLOYMENT);
         // TODO implement prepare
         prepareCheckComplete(EMTPY_DEPLOYMENT);
      }
      catch(Exception e)
      {
         log.error("Failed ", e);
         throw e;
      }
      stopAndRemove(new String[]
          { FAILING_DEPLOYMENT, EMTPY_DEPLOYMENT } );
   }

   /**
    * Test the deployment options
    * 
    * @throws Exception
    */
   public void testDeploymentOptions() throws Exception
   {
      final String deploymentName = getName() + ".ear";
      try
      {
         // Test exploded
         DeploymentProgress start = distributeAndStart(NESTED_DEPLOYMENT, deploymentName,
               new DeploymentOption[] { DeploymentOption.Explode});
         assertComplete(start);
         assertDeploymentState(start.getDeploymentID(), DeploymentState.STARTED);

         // Test fail if exists
         DeploymentProgress override = getDeploymentManager().distribute(deploymentName,
               getDeployURL(NESTED_DEPLOYMENT),
               new DeploymentOption[] { DeploymentOption.FailIfExists});
         override.run();
         assertFailed(override);
      }
      catch(Exception e)
      {
         log.error("Failed ", e);
         throw e;         
      }
      stopAndRemove(new String[] { deploymentName } );
   }
   
   /**
    * Test copyContent to the deployers target profile.
    *
    * @throws Exception
    */
   public void testDeployersDir() throws Exception
   {
      getDeploymentManager().loadProfile(deployersKey);
      try
      {
         // failed
         deployFailed(true);
         // complete
         deployEmpty(true);
         // Test redeploy
         assertTrue(getDeploymentManager().isRedeploySupported());
         redeployCheckComplete(EMTPY_DEPLOYMENT);
         // stop and remove
         stopAndRemove(new String[]
             { FAILING_DEPLOYMENT, EMTPY_DEPLOYMENT } );
      }
      catch(Exception e)
      {
         log.debug("Failed ", e);
         throw e;
      }
      finally
      {
         // Make sure that we release the profile
         getDeploymentManager().releaseProfile();
      }
   }

   /**
    * Test some conflicts when deleting a file manually.
    * This distributes a deployment into the deployers folder,
    * where we don't perform HDScanning. We delete the file
    * manually and then try to remove this file with the 
    * DeploymentManager.
    * 
    * @throws Exception
    */
   public void testRemoveConflict() throws Exception
   {
      final String deploymentName = getName() + ".ear";
      getDeploymentManager().loadProfile(deployersKey);
      try
      {
         DeploymentProgress start = distributeAndStart(NESTED_DEPLOYMENT, deploymentName, true, false);
         String deployed = start.getDeploymentID().getRepositoryNames()[0];
         
         // Delete the file manually
         VFS.init();
         VirtualFile f = VFS.getRoot(new URI(deployed));
         assertTrue(deployed, f.exists());
         boolean deleted = f.delete();
         if(isIgnoreRemove() == false)
         {
            assertTrue("deleted " + deployed, deleted);
         }
         stopAndRemove(new String[] { deploymentName });
      }
      finally
      {
         // Make sure that we release the profile
         getDeploymentManager().releaseProfile();
      }
   }
   
   /**
    * Test the hd deployment. This deployment will get copied
    * to the deploy folder after the server is started. This
    * deployment needs to get picked up by the HDScanner and
    * should be available to the ManagementView.
    *
    * @throws Exception
    */
   public void testHotDeploymentBeans() throws Exception
   {
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "ServerConfig");
      ManagedComponent mc = mgtView.getComponent("jboss.system:type=ServerConfig", type);
      assertNotNull(mc);
      ManagedProperty homeDirProperty = mc.getProperty("serverHomeDir");
      assertNotNull("serverHomeDir property exists", homeDirProperty);
      String homeDir = (String) ((SimpleValue) homeDirProperty.getValue()).getValue();
      assertNotNull(homeDir);
      
      // Manually copy the deployment
      File output = copyFile(new File(homeDir, "deploy/"), HD_DEPLOYMENT);
      // Wait for HDScanner
      Thread.sleep(8000);
      
      // Reload mgtView
      mgtView = getManagementView();
      ManagedDeployment md = mgtView.getDeployment(HD_DEPLOYMENT);
      assertNotNull("hd-beans not deployed", md);
      assertEquals("deployment started", DeploymentState.STARTED, md.getDeploymentState());

      output.delete();
      // Wait for HDScanner
      Thread.sleep(8000);
      try
      {
         getManagementView().getDeployment(HD_DEPLOYMENT);
         fail(HD_DEPLOYMENT + " not undeployed");
      }
      catch(NoSuchDeploymentException e)
      {
         // ok
      }
   }

   private File copyFile(File dir, String filename) throws Exception
   {
      File output = null;
      InputStream is = getDeployURL(filename).openStream();
      try
      {
         output = new File(dir, filename);
         FileOutputStream fos = new FileOutputStream(output);
         try
         {
            byte[] tmp = new byte[1024];
            int read;
            while((read = is.read(tmp)) > 0)
            {
               fos.write(tmp, 0, read);
            }
            fos.flush();
         }
         finally
         {
            fos.close();
         }         
      }
      finally
      {
         is.close();
      }
      return output;
   }

}
