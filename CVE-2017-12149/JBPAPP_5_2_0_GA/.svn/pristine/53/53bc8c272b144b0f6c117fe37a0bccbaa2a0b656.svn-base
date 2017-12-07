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

import org.jboss.logging.Logger;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.management.ManagedOperationProxyFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * The bootstrap deployment view.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class BootstrapProfileView extends AbstractProfileView
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(BootstrapProfileView.class);
   
   /** A fake profile key. */
   private static final ProfileKey key = new ProfileKey(BootstrapProfileView.class.getName());
   
   public BootstrapProfileView(ManagedOperationProxyFactory proxyFactory, Collection<ManagedDeployment> deployments)
   {
      super(proxyFactory);
      // Load the bootstrap deployments
      load(deployments);
   }
   
   @Override
   public ProfileKey getProfileKey()
   {
      return key;
   }
   
   @Override
   public boolean hasBeenModified(Profile profile)
   {
      return false;
   }

   protected void load(Collection<ManagedDeployment> deployments)
   {
      if(deployments == null)
         throw new IllegalArgumentException("null deployments.");
      
      for(ManagedDeployment deployment : deployments)
      {
         try
         {
            processManagedDeployment(deployment, DeploymentState.STARTED, 0, false);
         }
         catch(Exception e)
         {
            log.debug("Failed to process managed deployment " + deployment);
         }
      }
   }
   
}

