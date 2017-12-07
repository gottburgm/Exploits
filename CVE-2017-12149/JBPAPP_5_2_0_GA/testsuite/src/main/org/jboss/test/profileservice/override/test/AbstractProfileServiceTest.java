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
package org.jboss.test.profileservice.override.test;

import java.net.URL;

import javax.naming.InitialContext;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.JBossTestCase;
import org.jboss.virtual.VFS;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86164 $
 */
public abstract class AbstractProfileServiceTest extends JBossTestCase implements ProgressListener
{

   /** We use the default profile, defined by DeploymentManager to deploy apps. */
   public static final ProfileKey defaultProfile = new ProfileKey(ProfileKey.DEFAULT); 

   /** The deployment manager. */
   protected DeploymentManager deployMgr;

   public AbstractProfileServiceTest(String name)
   {
      super(name);
   }
   
   protected String getProfileName()
   {
      return null;
   }
   
   public void progressEvent(ProgressEvent eventInfo)
   {
      getLog().debug(eventInfo);
   }
   
   protected ManagedComponent getManagedComponent(ManagementView mgtView, String name) throws Exception
   {
      ComponentType type = new ComponentType("DataSource", "LocalTx");
      ManagedComponent ds = mgtView.getComponent(name, type);
      return ds;
   }
   
   protected String[] deployPackage(String name) throws Exception
   {
      DeploymentManager deployMgr = getDeploymentManager();
      URL contentURL = getDeployURL(name);

      assertNotNull(contentURL);
      
      String[] uploadedNames = {};
      try
      {
         uploadedNames = distribute(deployMgr, name, contentURL);
         try
         {
            start(deployMgr, uploadedNames);
         }
         catch(Exception e)
         {
            stop(deployMgr, uploadedNames);
            throw e;
         }
      }
      catch(Exception e)
      {
         undeploy(deployMgr, uploadedNames);
         throw e;
      }
      return uploadedNames;
   }
   
   
   protected void undeployPackage(String[] repositoryNames) throws Exception
   {
      DeploymentManager deployMgr = getDeploymentManager();
      try
      {
         stop(deployMgr, repositoryNames);
      }
      finally
      {
         undeploy(deployMgr, repositoryNames);
      }
   }
   
   protected ProfileKey getProfileKey()
   {
      if(getProfileName() == null)
         return defaultProfile;
      
      return new ProfileKey(getProfileName());
   }
   
   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected ManagementView getManagementView()
      throws Exception
   {
      InitialContext ctx = getInitialContext();
      ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
      ManagementView activeView = ps.getViewManager();
      activeView.load();
      // Init the VFS to setup the vfs* protocol handlers
      VFS.init();    
      return activeView;
   }
   
   /**
    * Obtain the ProfileService.ManagementView
    * @return
    * @throws Exception
    */
   protected DeploymentManager getDeploymentManager()
      throws Exception
   {
      if( deployMgr == null )
      {
         InitialContext ctx = getInitialContext();
         ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
         deployMgr = ps.getDeploymentManager();
         deployMgr.loadProfile(getProfileKey());
         // Init the VFS to setup the vfs* protocol handlers
         VFS.init();
      }
      return deployMgr;
   }
   
   protected String[] distribute(DeploymentManager deployMgr, String name, URL contentURL)
      throws Exception
   {
      DeploymentProgress progress = deployMgr.distribute(name, contentURL, true);
      getLog().debug("distribute: "+ contentURL);
      progress.addProgressListener(this);
      progress.run();
      
      assertCompleted(progress.getDeploymentStatus());
      
      return progress.getDeploymentID().getRepositoryNames();
   }
   
   protected void start(DeploymentManager deployMgr, String[] repositoryNames) throws Exception
   {
      DeploymentProgress progress = deployMgr.start(repositoryNames);
      progress.addProgressListener(this);
      progress.run();
      
      DeploymentStatus status = progress.getDeploymentStatus();
      assertCompleted(status);
   }
   
   protected void stop(DeploymentManager deployMgr, String[] repositoryNames) throws Exception
   {
      DeploymentProgress progress = deployMgr.stop(repositoryNames);
      progress.addProgressListener(this);
      progress.run();
      
      DeploymentStatus status = progress.getDeploymentStatus();
      assertCompleted(status);
   }
   
   protected void undeploy(DeploymentManager deployMgr, String[] repositoryNames) throws Exception
   {
      DeploymentProgress progress = deployMgr.remove(repositoryNames);
      progress.addProgressListener(this);
      progress.run();
      
      assertCompleted(progress.getDeploymentStatus());
   }
   
   protected void assertCompleted(DeploymentStatus status)
   {
      assertTrue("DeploymentStatus.isCompleted: " + status, status.isCompleted());
      assertFalse("DeploymentStatus.isFailed: " + status, status.isFailed());
   }
  
}