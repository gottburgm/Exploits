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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.system.server.profile.repository.NoopProfile;
import org.jboss.system.server.profile.repository.metadata.EmptyProfileMetaData;

/**
 * A typed profile factory. This delegates the creation of profiles to 
 * the registered factories, based on the class name of the profile meta data. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class TypedProfileFactory extends AbstractProfileFactory implements ProfileFactory
{

   /** The profile factories. */
   private Map<String, AbstractProfileFactory> factories = new ConcurrentHashMap<String, AbstractProfileFactory>();
   
   /** The locally handled types. */
   public static final Collection<String> types;
   
   static
   {
      types = Arrays.asList(EmptyProfileMetaData.class.getName());
   }
   
   public String[] getTypes()
   {
      return types.toArray(new String[types.size()]);
   }
   
   public Profile createProfile(ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles) throws Exception
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(metaData == null)
         throw new IllegalArgumentException("Null profile meta data");
      
      String profileType = metaData.getClass().getName();
      if(types.contains(profileType))
      {
         return createNoopProfile(key, metaData, subProfiles);
      }
      // Delegate to registered factories
      return delegateCreateProfile(profileType, key, metaData, subProfiles);
   }
   
   /**
    * Delegate the creation of the profile to one of the registered
    * profile factories.
    * 
    * @param type the meta data type
    * @param key the profile key
    * @param metaData the profile meta data
    * @param subProfiles the sub profiles
    * @return the profile
    * @throws IllegalArgumentException if there is no factory registered for the meta data type
    * @throws Exception for any error
    */
   protected Profile delegateCreateProfile(String type, ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles) throws IllegalArgumentException, Exception
   {
      AbstractProfileFactory factory = this.factories.get(type);
      if(factory == null)
         throw new IllegalArgumentException("Unrecognized meta data type: " + type);
      
      return factory.createProfile(key, metaData, subProfiles);
   }
   
   /**
    * Create a empty profile. 
    * 
    * @param key the profile key
    * @param metaData the profile meta data
    * @param subProfiles the sub profiles
    * @return the profile
    */
   protected Profile createNoopProfile(ProfileKey key, ProfileMetaData metaData, List<ProfileKey> subProfiles)
   {
      return new NoopProfile(key, subProfiles);
   }
   
   /**
    * Add a profile factory.
    * 
    * @param factory the abstract profile factory to add
    */
   public void addProfileFactory(AbstractProfileFactory factory)
   {
      if(factory == null)
         throw new IllegalArgumentException("Null profile factory.");
      if(factory.getTypes() == null)
         throw new IllegalArgumentException("Null factory types.");
      
      for(String type : factory.getTypes())
         this.factories.put(type, factory);
   }
   
   /**
    * Remove a profile factory.
    * 
    * @param factory the abstract profile factory to remove
    */
   public void removeProfileFactory(AbstractProfileFactory factory)
   {
      if(factory == null)
         throw new IllegalArgumentException("Null profile factory.");
      if(factory.getTypes() == null)
         throw new IllegalArgumentException("Null factory types.");
      
      for(String type : factory.getTypes())
         this.factories.remove(type);
   }

}

