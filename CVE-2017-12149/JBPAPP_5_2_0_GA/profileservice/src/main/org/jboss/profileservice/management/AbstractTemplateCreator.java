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
package org.jboss.profileservice.management;

import java.net.URL;

import org.jboss.deployers.spi.management.DeploymentTemplate;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.virtual.VirtualFile;

/**
 * A basic template creator, which applies and distributes the template.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 110335 $
 */
public class AbstractTemplateCreator
{

   /** Some filtered characters. */
   private static final char[] filtered = new char[] { '/', '\\', ':', '*', '?', '"', '<', '>', '|' };
   private static final char replace = '_';
   
   /** The deployment manager. */
   private DeploymentManager deploymentMgr;
   
   /** The default profile key. */
   private ProfileKey defaultKey = new ProfileKey(ProfileKey.DEFAULT);
   
   public DeploymentManager getDeploymentManager()
   {
      return deploymentMgr;
   }
   
   public void setDeploymentManager(DeploymentManager deploymentMgr)
   {
      this.deploymentMgr = deploymentMgr;
   }
   
   public ProfileKey getDefaulProfiletKey()
   {
      return defaultKey;
   }
   
   public void setDefaultProfileKey(ProfileKey defaultKey)
   {
      this.defaultKey = defaultKey;
   }
   
   public String applyTemplate(DeploymentTemplate template, String deploymentBaseName, DeploymentTemplateInfo info)
      throws Exception
   {
      if(template == null)
         throw new IllegalArgumentException("Null deployment template.");
      if(deploymentBaseName == null)
         throw new IllegalArgumentException("Null deployment name.");
      deploymentBaseName = deploymentBaseName.trim();
      if(deploymentBaseName.length() == 0)
         throw new IllegalArgumentException("emtpy deployment base name");
      if(info == null)
         throw new IllegalArgumentException("Null deployment template info.");

      // Load the deployment manager
      this.deploymentMgr.loadProfile(defaultKey);
      // The virtual file
      VirtualFile base = null;
      // Deploy the deployment
      String[] repositoryNames = null;
      try
      {
         // Apply the template
         String deploymentName = template.getDeploymentName(fixDeploymentName(deploymentBaseName));
         if(deploymentName == null)
            throw new IllegalStateException("getDeploymentName returned a null value.");
         // Wrap info to exclude all removed properties
         FilteredDeploymentTemplateInfo filterInfo = new FilteredDeploymentTemplateInfo(info);
         base = template.applyTemplate(filterInfo);
         if(base == null)
            throw new IllegalStateException("applyTemplate returned null virtual file.");

         try
         {
            // Distribute
            repositoryNames = distribute(deploymentName, base.toURL());
         }
         catch(Exception e)
         {
            try
            {
               // Try to remove
               if(repositoryNames != null)
                  remove(repositoryNames);
            }
            catch(Exception ignore) { }
            // Rethrow
            throw e;
         }

         try
         {
            // Start the deployment
            start(repositoryNames);
         }
         catch(Exception e)
         {
            try
            {
               // Try to stop
               stop(repositoryNames);
            }
            catch(Exception ignore)
            {
               //
            }
            try
            {
               // Try to remove
               remove(repositoryNames);
            }
            catch(Exception ignore)
            {
               //
            }
            // Rethrow
            throw e;
         }
      }
      finally
      {
         // Release the deployment manager
         this.deploymentMgr.releaseProfile();
         
         // Remove the temp file
         if(base != null)
            base.delete();
      }
      return repositoryNames[0];
   }
   
   protected String fixDeploymentName(String name)
   {
      String fixed = name;
      for(char c : filtered)
         fixed = fixed.replace(c, replace);
      return fixed;
   }
   
   protected String[] distribute(String name, URL url) throws Exception
   {
      // Distribute deployment content, Fail if the deployment already exists.
      DeploymentProgress progress = this.deploymentMgr.distribute(name, url, DeploymentOption.FailIfExists);
      progress.run();
      
      // 
      checkComplete(progress);
      
      return progress.getDeploymentID().getRepositoryNames();      
   }
 
   protected void start(String[] names) throws Exception
   {
      DeploymentProgress progress = this.deploymentMgr.start(names);
      progress.run();
      
      checkComplete(progress);
   }
   
   protected void stop(String[] names) throws Exception
   {
      DeploymentProgress progress = this.deploymentMgr.stop(names);
      progress.run();
      
      checkComplete(progress);      
   }
   
   protected void remove(String[] names) throws Exception
   {
      DeploymentProgress progress = this.deploymentMgr.remove(names);
      progress.run();
      
      checkComplete(progress);      
   }
   
   protected void checkComplete(DeploymentProgress progress) throws Exception
   {
      if(progress.getDeploymentStatus().isFailed())
      {
         throw new RuntimeException("Failed to process template.", progress.getDeploymentStatus().getFailure());
      }      
   }
   
}
