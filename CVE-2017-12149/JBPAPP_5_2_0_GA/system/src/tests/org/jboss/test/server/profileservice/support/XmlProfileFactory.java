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
package org.jboss.test.server.profileservice.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.FilteredProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.HotDeploymentProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.ProfilesMetaData;
import org.jboss.system.server.profileservice.repository.AbstractBootstrapProfileFactory;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;

/**
 * A profile factory based which generates the ProfileMetaData based on .xml files.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87932 $
 */
public class XmlProfileFactory extends AbstractBootstrapProfileFactory
{
   /** The profiles directory name. */
   public final static String PROFILES_SUFFIX = ".profile";
   
   /** The attachment store uri. */
   private File attachmentStoreRoot;
   
   /** Profile directories */
   private Collection<VirtualFile> profileDirectories;
   
   /** The unmarshaller. */
   private Unmarshaller unmarshaller;
   
   /** The parsed file list */
   private Collection<ProfileKey> processedProfileKeys = new ArrayList<ProfileKey>();

   /** The default schema resolver. */
   private static final DefaultSchemaResolver resolver = new DefaultSchemaResolver();
   
   static
   {
      // Add schema bindings
      resolver.addClassBinding("urn:jboss:profileservice:profiles:1.0", ProfilesMetaData.class);
      resolver.addClassBinding("urn:jboss:profileservice:profile:filtered:1.0", FilteredProfileMetaData.class);
      resolver.addClassBinding("urn:jboss:profileservice:profile:hotdeployment:1.0", HotDeploymentProfileMetaData.class);
   }
   
   public XmlProfileFactory(URI[] profileDirectories) throws Exception
   {
      if(profileDirectories == null)
         throw new IllegalArgumentException("Null directories");
      this.profileDirectories = new ArrayList<VirtualFile>();
      for(URI uri : profileDirectories)
      {
         VirtualFile vf = VFS.getRoot(uri);
         if(vf == null)
            throw new IllegalArgumentException("Could not find uri: " + vf);
         if(vf.isLeaf())
            throw new IllegalArgumentException("Not a directory: " + vf);
         this.profileDirectories.add(vf);
      }
   }
   
   public Collection<VirtualFile> getProfileDirectories()
   {
      return profileDirectories;
   }
   
   public void setProfileDirectories(Collection<VirtualFile> profileDirectories)
   {
      this.profileDirectories = profileDirectories;
   }   
   
   public File getAttachmentStoreRoot()
   {
      return attachmentStoreRoot;
   }

   public void setAttachmentStoreRoot(File attachmentStoreRoot)
   {
      this.attachmentStoreRoot = attachmentStoreRoot;
   }
   
   /**
    * Create the profile meta data.
    * 
    * @param rootKey the key of the profile/
    * @param the url pointing to the profile file.
    * @throws Exception
    */
   @Override
   protected void createProfileMetaData(ProfileKey rootKey, URL url) throws Exception
   {
      // Create the unmarshaller
      this.unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      // Get the root profile virtual file
      VirtualFile vf = getProfileFile(rootKey, url);
      // Parse
      ProfilesMetaData rootProfiles = parse(vf);
      // Process
      processProfilesMetaData(rootProfiles);
   }
   
   /**
    * process the profile meta data.
    * 
    * @param metaData the profile meta data.
    * @throws Exception
    */
   protected void processProfileMetaData(ProfileMetaData metaData) throws Exception
   {
      // Create profileKey
      ProfileKey key = createProfileKey(metaData);      
      processProfileMetaData(key, metaData);
   }
   
   /**
    * process the profile meta data
    * 
    * @param key the profile key.
    * @param metaData the meta data.
    * 
    * @throws Exception
    */
   protected void processProfileMetaData(ProfileKey key, ProfileMetaData metaData) throws Exception
   {      
      // Don't process the same key twice
      if(this.processedProfileKeys.contains(key))
         return;

      // Add profile meta data
      addProfile(key, metaData);
      
      // Process sub profiles
      processSubProfiles(key, metaData);
   }
   
   /**
    * process the sub profiles.
    * 
    * @param key the profile key.
    * @param metaData the profile meta data
    * @throws Exception
    */
   protected void processSubProfiles(ProfileKey key, ProfileMetaData metaData) throws Exception
   {
      if(metaData.getSubprofiles() != null && metaData.getSubprofiles().isEmpty() == false)
      {
         for(SubProfileMetaData subProfile : metaData.getSubprofiles())
         {
            ProfileKey subProfileKey = createProfileKey(subProfile);
            processSubProfileMetaData(subProfileKey, subProfile);
         }
      }
   }
   
   /**
    * process the sub profile.
    * 
    * @param key the sub profile key.
    * @param metaData the sub profile meta data.
    * @throws Exception
    */
   protected void processSubProfileMetaData(ProfileKey key, SubProfileMetaData metaData) throws Exception
   {
      // get file
      VirtualFile vf = getProfileFile(key, null);
      // parse
      ProfilesMetaData profiles = parse(vf);
      // TODO maybe override the profiles key (as it should be the same - filename and profiles name
      // processProfilesMetaData(key, profiles);
      processProfilesMetaData(profiles);
   }
   
   /**
    * process the profiles meta data.
    * 
    * @param key the profiles key.
    * @param metaData the profiles meta data.
    * @throws Exception
    */
   protected void processProfilesMetaData(ProfileKey key, ProfilesMetaData metaData) throws Exception
   {
      if(metaData.getProfiles() != null && metaData.getProfiles().isEmpty() == false)
      {
         for(ProfileMetaData profile : metaData.getProfiles())
         {
            processProfileMetaData(profile);
         }
      }
      // Add the profiles for later resolution
      addProfiles(key, metaData.getProfiles());      
   }
   
   /**
    * Process the <profiles> meta data.
    * This will add dependencies based on the ordering of the xml.
    * 
    * @param profilesMetaData the profiles meta data
    */
   protected void processProfilesMetaData(ProfilesMetaData profilesMetaData) throws Exception
   {
      // The <profiles> key
      ProfileKey profilesKey = createProfileKey(profilesMetaData);
      processProfilesMetaData(profilesKey, profilesMetaData);
   }

   /**
    * get the virtual file of the profile.
    * 
    * @param key the profile key.
    * @param url the url pointing to the file
    * @return a resolved name if the url is null.
    * 
    * @throws Exception
    */
   protected VirtualFile getProfileFile(ProfileKey key, URL url) throws Exception
   {
      if(url != null)
         return VFS.getRoot(url);
      else
         return resolveFile(key.getName());
   }
   
   /**
    * Try to resolve the profile name.
    * 
    * @param name the profile name.
    * @return the virtual file
    * @throws Exception
    */
   protected VirtualFile resolveFile(String name) throws Exception
   {
      // ProfileKey.getName() + .profile
      if(name.endsWith(PROFILES_SUFFIX) == false)
         name = name + PROFILES_SUFFIX;
         
      VirtualFile vf = null;
      for(VirtualFile dir : profileDirectories)
      {
         vf = dir.getChild(name);
         if(vf != null)
            break;
      }
      if(vf == null)
         throw new FileNotFoundException("Could not find profile configuration file for: " + name);
      return vf;
   }
   
   /**
    * Parse a profile file.
    * 
    * @param vf the virtual file.
    * @return the <profiles> meta data
    * @throws JBossXBException
    * @throws IOException
    */
   protected ProfilesMetaData parse(VirtualFile vf) throws JBossXBException, IOException
   {
      if(log.isTraceEnabled())
         log.trace("parsing file: " + vf.getPathName());
      return (ProfilesMetaData) unmarshaller.unmarshal(vf.openStream(), resolver);
   }
   
   @Override
   protected void addProfile(ProfileKey key, ProfileMetaData metaData)
   {
      // Add to processed keys
      processedProfileKeys.add(key);
      // Add to parent
      super.addProfile(key, metaData);
   }
   
   @Override
   protected void addProfiles(ProfileKey key, List<ProfileMetaData> metaData)
   {
      // Add to processed keys
      processedProfileKeys.add(key);
      // Add to pared
      super.addProfiles(key, metaData);
   }
}

