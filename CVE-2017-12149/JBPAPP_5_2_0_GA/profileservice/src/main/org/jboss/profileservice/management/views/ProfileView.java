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
package org.jboss.profileservice.management.views;

import java.util.Collection;
import java.util.HashSet;

import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.logging.Logger;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.plugins.ManagedDeploymentImpl;
import org.jboss.profileservice.management.ManagedOperationProxyFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.AbstractProfileDeployment;

/**
 * The profile view.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ProfileView extends AbstractProfileView
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(ProfileView.class);
   
   /** The main deployer. */
   private final MainDeployer mainDeployer;
   
   /** The profile key. */
   private final ProfileKey key;
   
   /** The last modified. */
   private final long lastModified;
   
   public ProfileView(Profile profile, ManagedOperationProxyFactory proxyFactory, MainDeployer mainDeployer)
   {
      super(proxyFactory);
      if(profile == null)
         throw new IllegalArgumentException("null profile");
      if(profile.getKey() == null)
         throw new IllegalArgumentException("null profile key");
      if(mainDeployer == null)
         throw new IllegalArgumentException("null main deployer");
      //
      this.key = profile.getKey();
      this.mainDeployer = mainDeployer;
      this.lastModified = profile.getLastModified();
      // Load the deployments
      load(profile.getDeployments());
   }
   
   public ProfileKey getProfileKey()
   {
      return this.key;
   }
   
   protected void load(Collection<ProfileDeployment> deployments)
   {
      if(deployments == null)
         throw new IllegalArgumentException("null deployments.");
      
      boolean trace = log.isTraceEnabled();
      for(ProfileDeployment deployment : deployments)
      {
         try
         {
            try
            {
               ManagedDeployment md = mainDeployer.getManagedDeployment(deployment.getName());
               processRootManagedDeployment(md, trace);
               
               // Cache the deployment types
               if(md.getTypes() != null && md.getTypes().isEmpty() == false)
                  ((AbstractProfileDeployment)deployment)
                     .addTransientAttachment(KnownDeploymentTypes.class.getName(), md.getTypes());
            }
            catch(DeploymentException e)
            {
               // FIXME Assume a undeployed (stopped) deployment
               ManagedDeployment md = createStoppedManagedDeployment(deployment);
               processManagedDeployment(md, DeploymentState.STOPPED, 0, trace);
            }
         }
         catch(Exception e)
         {
            log.debug("Failed to create ManagedDeployment for: " + deployment.getName(), e);
         }
      }
   }
   
   @SuppressWarnings("unchecked")
   protected ManagedDeployment createStoppedManagedDeployment(ProfileDeployment deployment)
   {
      String deploymentName = deployment.getName();
      ManagedDeployment md = new ManagedDeploymentImpl(deploymentName,
            deployment.getRoot().getName());
      
      // Try to get the cached deployment type 
      Collection<String> deploymentTypes = ((AbstractProfileDeployment)deployment)
         .getTransientAttachment(KnownDeploymentTypes.class.getName(), Collection.class);
      
      if(deploymentTypes != null && deploymentTypes.isEmpty() == false)
      {
         md.setTypes(new HashSet<String>(deploymentTypes));
      }
      else
      {
         int i = deploymentName.lastIndexOf(".");
         if(i != -1 && (i + 1) < deploymentName.length())
         {
            String guessedType = deploymentName.substring(i + 1, deploymentName.length());
            if(guessedType.endsWith("/"))
               guessedType = guessedType.substring(0, guessedType.length() -1 );
            md.setTypes(new HashSet<String>(1));
            md.addType(guessedType);
         }  
      }
      return md;
   }
   
   @Override
   protected void processManagedDeployment(ManagedDeployment md, DeploymentState state, int level, boolean trace)
         throws Exception
   {
      super.processManagedDeployment(md, state, level, trace);
      // Set the profile key
      md.setAttachment(ProfileKey.class.getName(), getProfileKey());
   }

   @Override
   public boolean hasBeenModified(Profile profile)
   {
      if(profile == null)
         throw new IllegalArgumentException("null profile.");
      
      return this.lastModified < profile.getLastModified();
   }
   
}

