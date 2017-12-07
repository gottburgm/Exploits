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
package org.jboss.system.server.profileservice.repository;

import java.util.Collection;

import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;

/**
 * deploy/undeploy action. This deploys/undeploys the profile deployments
 * based on the lifecycle of the profile.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86190 $
 */
public class ProfileDeployAction extends AbstractProfileAction
{

   /** The profile deployment deployer. */
   private MainDeployerAdapter deployer;
  
   public ProfileDeployAction(MainDeployerAdapter deployer)
   {
      if(deployer == null)
         throw new IllegalArgumentException("Null deployer");
      this.deployer = deployer;
   }
   
   @Override
   public void install(Profile profile)
   {
      Collection<ProfileDeployment> deployments = profile.getDeployments();
      if (deployments != null && !deployments.isEmpty())
      {
         // Add deployments
         for (ProfileDeployment deployment : profile.getDeployments())
         {
            try
            {
               // Add deployment
               deployer.addDeployment(deployment);
            }
            catch(Exception e)
            {
               log.error("Failed to add deployment: " + deployment, e);
            }
         }
            
         // deploy
         deployer.process();
      }
   }

   @Override
   public void uninstall(Profile profile)
   {
      Collection<ProfileDeployment> deployments = profile.getDeployments();
      if (deployments != null && !deployments.isEmpty())
      {
         // remove deployments
         for (ProfileDeployment deployment : profile.getDeployments())
         {
            try
            {
               // remove deployment
               deployer.removeDeployment(deployment);
            }
            catch(Throwable t)
            {
               log.warn("failed to remove deployment: " + deployment, t);
            }
         }
         
         // undeploy
         deployer.process();
      }
   }
}
