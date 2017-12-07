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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileRepository;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.system.server.profile.repository.AbstractProfile;
import org.jboss.system.server.profile.repository.metadata.BasicProfileMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class BasicProfileFactory extends AbstractProfileFactory implements ProfileFactory
{
   /** The deployment repository factory. */
   private ProfileRepository profileRepository;
   
   /** The handled meta data types. */
   public static final Collection<String> types;
   
   static
   {
      types = Arrays.asList(BasicProfileMetaData.class.getName());
   }

   public String[] getTypes()
   {
      return types.toArray(new String[types.size()]);
   }
   
   public ProfileRepository getProfileRepository()
   {
      return profileRepository;
   }
   
   public void setProfileRepository(ProfileRepository profileRepository)
   {
      this.profileRepository = profileRepository;
   }
   
   public Profile createProfile(ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles) throws Exception
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(metaData == null)
         throw new IllegalArgumentException("Null profile meta data.");
      
      // Start to create the profile
      DeploymentRepository repository = profileRepository.createProfileDeploymentRepository(key, metaData);
      
      // Create the profile
      AbstractProfile profile = new AbstractProfile(repository, key);
      
      // Copy the sub-profile keys
      profile.setSubProfiles(subProfiles);
      
      return profile;
   }
}

