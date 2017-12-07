/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.defaultcfg.profileservice.test;

import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.virtual.VFS;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class ClusteredDeploymentRepoAddContentTestCase 
      extends JBossClusteredTestCase implements ProgressListener
{   
   /** We use the default profile, defined by DeploymentManager to deploy apps. */
   public static final ProfileKey farmProfile = new ProfileKey("farm");
   
   protected ManagementView activeView;
   protected DeploymentManager deployMgr;
   private long eventCount = 0;

   /**
    * Create a new ClusteredDeploymentRepoAddContentTestCase.
    * 
    * @param name
    */
   public ClusteredDeploymentRepoAddContentTestCase(String name)
   {
      super(name);
   }

   public void testDeployment() throws Exception
   {
      String name = "farm-addedcontent-service.xml";
      ManagedDeploymentTester tester = new ManagedDeploymentTester()
      {         
         public void testManagedDeployment() throws Exception
         {
            boolean node0OK = false;
            boolean node1OK = false;
            
            MBeanServerConnection[] adaptors = getAdaptors();
            ObjectName oname = new ObjectName("jboss.system:service=FarmAddContentTestThreadPool");
            
            long deadline = System.currentTimeMillis() + 12000;
            do
            {
               if (!node0OK)
               {
                  try
                  {
                     node0OK = "FarmAddContentThreadPool".equals(adaptors[0].getAttribute(oname, "Name"));
                  }
                  catch (Exception ignored) {}
               }
               if (!node1OK)
               {
                  try
                  {
                     node1OK = "FarmAddContentThreadPool".equals(adaptors[1].getAttribute(oname, "Name"));
                  }
                  catch (Exception ignored) {}                  
               }
               
               if (node0OK && node1OK)
               {
                  break;
               }
               
               Thread.sleep(200);
            }
            while (System.currentTimeMillis() < deadline);
            
            assertTrue("node0 OK", node0OK);
            assertTrue("node1 OK", node1OK);
         }
         
      };
      testDeployment(name, "sar", tester);
//      testDeployment(name, "sar", null);
   }
   
   protected void testDeployment(String name, String type, ManagedDeploymentTester tester) throws Exception
   {
      DeploymentManager deployMgr = getDeploymentManager(getNamingContext(0));
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
            ManagementView mgtView = getManagementView(getNamingContext(0));
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
   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected DeploymentManager getDeploymentManager(Context ctx)
      throws Exception
   {
      if( deployMgr == null )
      {
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         deployMgr = ps.getDeploymentManager();
         deployMgr.loadProfile(getProfileKey());
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      return deployMgr;
   }

   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected ManagementView getManagementView(Context ctx)
      throws Exception
   {
      if( activeView == null )
      {
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         activeView = ps.getViewManager();
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      // Reload
      activeView.load();
      return activeView;
   }
   
   protected Context getNamingContext(int nodeIndex) throws Exception
   {
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[nodeIndex]);
      return new InitialContext(env1);
   }
   
   protected ProfileKey getProfileKey()
   {
      if(getProfileName() == null)
         return farmProfile;
      
      return new ProfileKey(getProfileName());
   }
   
   /**
    * @return the ProfileKey.name to use when loading the profile
    */
   protected String getProfileName()
   {
      return null;
   }

   private interface ManagedDeploymentTester
   {
      void testManagedDeployment() throws Exception;
   }

   public void progressEvent(ProgressEvent eventInfo)
   {
      eventCount ++;
      getLog().debug(eventInfo);
   }

}
