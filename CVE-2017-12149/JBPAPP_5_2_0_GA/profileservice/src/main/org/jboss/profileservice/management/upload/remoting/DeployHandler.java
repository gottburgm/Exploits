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
package org.jboss.profileservice.management.upload.remoting;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.logging.Logger;
import org.jboss.profileservice.management.upload.AbstractTransientProfileManager;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.MutableProfile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * A profile service deploy subsystem handling transient deployments. 
 * The AbstractDeployHandler takes care of the profile deployments.
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87267 $
 */
public class DeployHandler extends AbstractDeployHandler
{
   /** The logger. */
   static final Logger log = Logger.getLogger(DeployHandler.class);

   /** The transient profile. */
   private MutableProfile transientProfile;
   
   /** The profile service. */
   private ProfileService ps;
   
   /** The transient deployments map. */
   private Map<String, VirtualFile> transientDeployments = new ConcurrentHashMap<String, VirtualFile>();
   
   public ProfileService getProfileService()
   {
      return ps;
   }
   
   public void setProfileService(ProfileService ps)
   {
      this.ps = ps;
   }
   
   public void start() throws Exception
   {
      // Set the transient profile
      this.transientProfile = (MutableProfile) ps.getActiveProfile(AbstractTransientProfileManager.TRANSIENT_PROFILE_KEY);
   }

   @Override
   protected String[] getRepositoryNames(String[] names, DeploymentRepository deploymentRepository) throws Exception
   {
      // get the transient repository names
      List<String> repositoryNames = getTransientRepositoryNames(names);
      
      // Add the results from the profile deployment repository
      for(String name : super.getRepositoryNames(names, deploymentRepository))
         repositoryNames.add(name);
      
      return repositoryNames.toArray( new String[repositoryNames.size()] );
   }

   /**
    * Distribute a transient (copyContent == false) deployment. 
    * 
    * @param dtID the deployment id
    * @return the name of the deployment
    * @throws IOException
    * @throws URISyntaxException 
    */
   @Override
   protected String[] distribute(DeploymentID dtID) throws Exception 
   {
      URL contentURL = dtID.getContentURL();
      log.info("Begin distribute, content url: " + contentURL);

      // Create the virtual file
      VirtualFile vf = VFS.getRoot(contentURL);
     
      // FIXME make deployment visible to management view
      ProfileDeployment deployment = createDeployment(vf);
      this.transientProfile.addDeployment(deployment);

      String name = deployment.getName();
      this.transientDeployments.put(name, vf);
      
      log.info("End distribute, " + name);
      return new String[] { name };
   }

   @Override
   protected ProfileDeployment scheduleStart(String name, DeploymentRepository deploymentRepository) throws Exception
   {
      String deploymentName = resolveDeploymentName(name);
      if(deploymentName != null)
      {
         //
         ProfileDeployment deployment = this.transientProfile.getDeployment(deploymentName);
         // FIXME update the timestamp
         this.transientProfile.addDeployment(deployment);
         return deployment;
      }
      else
      {
         // unlock 
         ProfileDeployment deployment = deploymentRepository.getDeployment(name);
         deploymentRepository.unlockDeploymentContent(deployment.getName());
         return deployment;
      }
   }
   
   @Override
   protected ProfileDeployment scheduleStop(String name, DeploymentRepository deploymentRepository) throws Exception
   {
      String deploymentName = resolveDeploymentName(name);
      if(deploymentName != null)
      {
         ProfileDeployment deployment = this.transientProfile.getDeployment(deploymentName);
         // FIXME update the timestamp
         this.transientProfile.addDeployment(deployment);
         return deployment; 
      }
      else
      {
         // Lock content
         ProfileDeployment deployment = deploymentRepository.getDeployment(name);
         deploymentRepository.lockDeploymentContent(deployment.getName());
         return deployment;
      }
   }
   
   @Override
   protected void removeDeployment(String name, DeploymentRepository deploymentRepository) throws Exception
   {
      String deploymentName = resolveDeploymentName(name);
      if(deploymentName != null)
      {
         // Remove from local cache
         this.transientDeployments.remove(deploymentName);
         // Remove from profile
         this.transientProfile.removeDeployment(deploymentName); 
      }
      else
      {
         // Remove deployment from repository
         deploymentRepository.removeDeployment(name);
      }
   }
   
   protected List<String> getTransientRepositoryNames(String[] names)
   {
      List<String> repositoryNames = new ArrayList<String>();
      for(String name : names)
      {
         if(this.transientDeployments.containsKey(name))
         {
            repositoryNames.add(name);
            continue;
         }
         for(VirtualFile vf : this.transientDeployments.values())
         {
            if(vf.getName().equals(name))
            {
               try
               {
                  repositoryNames.add(vf.toURI().toString());
               }
               catch(Exception ignored) { }
            }
         }
      }
      return repositoryNames;
   }
   
   /**
    * Try to resolve the deployment name.
    * 
    * @param name the name
    * @return the deployment name, null if there was no matching name
    * @throws IllegalStateException if multiple matching names were found
    */
   protected String resolveDeploymentName(String name)
   {
      String deploymentName = null;
      if(this.transientDeployments.containsKey(name))
      {
         deploymentName = name;
      }
      // Try to resolve the name
      if(deploymentName == null)
      {
         List<String> names = getTransientRepositoryNames(new String[]{ name });
         if(names.size() == 1)
         {
            deploymentName = names.get(0);
         }
         else if(names.size() > 1)
         {
            throw new IllegalStateException("Multiple matching deployments found for name: "+ name + " available " + names);
         }
      }
      return deploymentName;
   }
   
}
