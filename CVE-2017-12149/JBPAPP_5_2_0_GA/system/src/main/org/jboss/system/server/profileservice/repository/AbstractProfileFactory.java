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

import java.util.ArrayList;
import java.util.List;

import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileKeyMetaData;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;

/**
 * A abstract profile factory.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public abstract class AbstractProfileFactory implements ProfileFactory
{   
   /**
    * This is used by the BoostrapProfileFactory, as it already created the
    * subProfiles list. 
    * 
    * @param key the profile key
    * @param metaData the profile meta data
    * @param subProfiles the sub profiles list
    * @return the profile
    * @throws Exception
    */
   public abstract Profile createProfile(ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles) throws Exception;
   
   public Profile createProfile(ProfileKey key, ProfileMetaData metaData) throws Exception
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(metaData == null)
         throw new IllegalArgumentException("Null profile meta data.");
      
      // 
      return createProfile(key, metaData, createSubProfiles(metaData));
   }
   
   protected List<ProfileKey> createSubProfiles(ProfileMetaData metaData)
   {
      List<ProfileKey> subProfiles = new ArrayList<ProfileKey>();
      if(metaData.getSubprofiles() != null && metaData.getSubprofiles().isEmpty() == false)
      {
         for(SubProfileMetaData subProfile : metaData.getSubprofiles())
         {
            subProfiles.add(createKey(subProfile));
         }
      }
      return subProfiles;
   }
   
   protected ProfileKey createKey(ProfileKeyMetaData metaData)
   {
      return new ProfileKey(metaData.getDomain(), metaData.getServer(), metaData.getName());
   }   
}
