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
package org.jboss.profileservice.management.upload;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.MutableProfile;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.system.server.profile.repository.AbstractImmutableProfile;

/**
 * The AbstractTransientProfileManager maintains a profile for transient deployments.
 * Transient deployments are copyContent = false, therefore they are not
 * getting deployed again after AS is restarted.
 * The aim of the transient profile is to expose those deployments to the
 * ManagementView.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87083 $
 */
public class AbstractTransientProfileManager
{
   /** The transient profile name. */
   public static final String TRANSIENT_PROFILE_NAME = "transient-deployment-profile";
   
   /** The transient profile key. */
   public static final ProfileKey TRANSIENT_PROFILE_KEY = new ProfileKey(TRANSIENT_PROFILE_NAME);
   
   /** The profile service. */
   protected ProfileService ps;
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractTransientProfileManager.class);
   
   public ProfileService getProfileService()
   {
      return this.ps;
   }
   
   public void setProfileService(ProfileService ps)
   {
      this.ps = ps;
   }

   /**
    * Start registers and activates the transient deployments profile
    * 
    * @throws Exception for any error
    */
   public void start() throws Exception
   {
      if(this.ps == null)
         throw new IllegalStateException("Null profile service.");
      
      // Create the transient deployment profile
      Profile profile = createTransientProfile();
      // Register
      this.ps.registerProfile(profile);
      // Activate
      {
         log.debug("activating transient profile " + TRANSIENT_PROFILE_NAME);
         this.ps.activateProfile(TRANSIENT_PROFILE_KEY);
         this.ps.validateProfile(TRANSIENT_PROFILE_KEY);
      }
   }
   
   /**
    * Stop deactivates and unregisters the transient deployments profile.
    */
   public void stop()
   {
      try
      {
         // Deactivate
         log.debug("deactivating transient profile: " + TRANSIENT_PROFILE_NAME);
         this.ps.deactivateProfile(TRANSIENT_PROFILE_KEY);
      }
      catch(Exception e)
      {
         log.debug("Failed to deactivate transient profile: ", e);
      }
      try
      {
         // Unregister
         log.debug("unregistering transient profile: " + TRANSIENT_PROFILE_NAME);
         this.ps.unregisterProfile(TRANSIENT_PROFILE_KEY);
      }
      catch(Exception e)
      {
         log.debug("Failed to unregister transient profile: ", e);
      }
   }
   
   /**
    * Create the transient profile.
    * 
    * @return the transient profile
    * @throws Exception for any error
    */
   protected Profile createTransientProfile() throws Exception
   {
      return new TransientDeploymentProfile(TRANSIENT_PROFILE_KEY);
   }
   
   /**
    * The transient deployments profile. 
    */
   public static class TransientDeploymentProfile extends AbstractImmutableProfile implements MutableProfile
   {
      public TransientDeploymentProfile(ProfileKey key)
      {
         super(key, new URI[0]);
      }

      public void addDeployment(ProfileDeployment deployment) throws Exception
      {
         if(deployment == null)
            throw new IllegalArgumentException("Null deployment.");
         super.addDeployment(deployment.getName(), deployment);
      }

      public void enableModifiedDeploymentChecks(boolean flag)
      {
         //
      }

      public Collection<ModificationInfo> getModifiedDeployments() throws Exception
      {
         return Collections.emptySet();
      }
      
      @Override
      public boolean isMutable()
      {
         return false;
      }
   }
   
}
