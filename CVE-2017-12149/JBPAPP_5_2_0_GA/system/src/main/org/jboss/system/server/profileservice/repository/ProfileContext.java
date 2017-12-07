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

import org.jboss.dependency.plugins.AbstractControllerContext;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.ControllerContextActions;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;

/**
 * The ProfileServiceContext.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class ProfileContext extends AbstractControllerContext
{
   
   /** The profile meta data. */
   private ProfileMetaData metaData;
   
   /** The profile. */
   private Profile profile;
   
   protected static ProfileKey getProfileKey(Profile profile)
   {
      if(profile == null)
         throw new IllegalArgumentException("Null profile.");
      return profile.getKey();
   }
   
   public ProfileContext(Profile profile, ControllerContextActions actions)
   {
      super(getProfileKey(profile), actions);
      setMode(ControllerMode.ON_DEMAND);
      createDependencies(profile.getKey(), profile.getSubProfiles());
      this.profile = profile;
      // The profile
      setTarget(profile);
   }
   
   public ProfileContext(ProfileKey key, ProfileMetaData metaData, ControllerContextActions actions)
   {
      super(key, actions);
      
      if(this.metaData == null)
         throw new IllegalArgumentException("Null meta data.");
      
      this.metaData = metaData; 
   }

   public Profile getProfile()
   {
      return this.profile;
   }
   
   private void createDependencies(ProfileKey key, Collection<ProfileKey> subProfiles)
   {
      if(subProfiles != null && subProfiles.isEmpty() == false)
      {
         for(ProfileKey iDependOn : subProfiles)
         {
            getDependencyInfo().addIDependOn(createDependencyItem(key, iDependOn));
         }
      }
   }
   
   private DependencyItem createDependencyItem(ProfileKey key, ProfileKey iDependOn)
   {
      return new AbstractDependencyItem(key, iDependOn, ControllerState.DESCRIBED, ControllerState.INSTALLED);
   }
 
   @Override
   public String toString()
   {
      // FIXME
      return this.profile.toString();
   }
   
}

