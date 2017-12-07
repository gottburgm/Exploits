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

package org.jboss.system.server.profileservice;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicSubProfileMetaData;
import org.jboss.system.server.profileservice.repository.StaticProfileFactory;
import org.jboss.system.server.profileservice.repository.clustered.metadata.ClusteredProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.clustered.metadata.HotDeploymentClusteredProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.clustered.metadata.ImmutableClusteredProfileSourceMetaData;

/**
 * Expands upon the StaticProfileFactory to include a subprofiles
 * for farmed content.
 * 
 * @author Brian Stansberry
 */
public class StaticClusteredProfileFactory extends StaticProfileFactory
{

   /** The deploy-hasingleton profile name. */
   private static final String HASINGLETON_NAME = "deploy-hasingleton";
   
   /** The farm profile name. */
   private static final String FARM_NAME = "farm";
   
   /** The hasingleton uris. */
   private List<URI> hasingletonURIs;
   
   /** The farm uris. */
   private List<URI> farmURIs;
   
//   private String partitionName;

   public List<URI> getHASingletonURIs()
   {
      return hasingletonURIs;
   }

   public void setHASingletonURIs(List<URI> hasingletonURIs)
   {
      this.hasingletonURIs = hasingletonURIs;
   }

   public List<URI> getFarmURIs()
   {
      return farmURIs;
   }

   public void setFarmURIs(List<URI> farmURIs)
   {
      this.farmURIs = farmURIs;
   }

//   public String getPartitionName()
//   {
//      return partitionName;
//   }
//
//   public void setPartitionName(String partitionName)
//   {
//      this.partitionName = partitionName;
//   }
//
//   @Override
//   public void create() throws Exception
//   {
//      super.create();
//      
//      if (this.farmURIs != null || this.hasingletonURIs != null)
//      {
//         if (this.partitionName == null)
//         {
//            throw new IllegalStateException("Null partition name.");
//         }
//      }
//   }
   
   /**
    * Create the cluster profiles, including the application profile from the
    * StaticProfileFactory.
    * 
    */
   @Override
   protected String[] createApplicationProfiles(String[] applicationsSubProfiles)
   {
      // Create the application profile
      String applicationProfileName = super.createApplicationProfile(applicationsSubProfiles);
      
      // Create the farm profile
      ProfileMetaData farm = null;
      if (getFarmURIs() != null)
      {
         ProfileKey farmKey = new ProfileKey(FARM_NAME);
         URI[] farmURIs = getFarmURIs().toArray(new URI[getFarmURIs().size()]);
         String[] farmSubProfiles = new String[] { applicationProfileName };
         farm = createClusteredProfileMetaData(
               FARM_NAME, true, farmURIs, farmSubProfiles);
         addProfile(farmKey, farm);         
      }
      // Create the hasingleton profile
      if (getHASingletonURIs() != null)
      {
         ProfileKey hasingletonKey = new ProfileKey(HASINGLETON_NAME);
         URI[] hasingletonURIs = getHASingletonURIs().toArray(new URI[getHASingletonURIs().size()]);
         // Note HASingleton can't depend on others or it will get undeployed
         // prematurely
         String[] hasingletonSubProfiles = new String[0];
         ProfileMetaData hasingletons = createProfileMetaData(
               HASINGLETON_NAME, true, hasingletonURIs, hasingletonSubProfiles);
         addProfile(hasingletonKey, hasingletons);         
      }
      // Return the dependencies for the root profile
      return farm == null ? new String[] { applicationProfileName } : new String[] { FARM_NAME };
   }

   private ProfileMetaData createClusteredProfileMetaData(String name, boolean hotDeployment, URI[] uris, String[] subProfiles)
   {
      // Create profile
      BasicProfileMetaData metaData = new BasicProfileMetaData();
      metaData.setName(name);
      // Create profile sources
      ProfileSourceMetaData source = createClusteredSource(uris, hotDeployment);
      metaData.setSource(source);
      
      List<SubProfileMetaData> profileList = new ArrayList<SubProfileMetaData>();
      for(String subProfile : subProfiles)
      {
         BasicSubProfileMetaData md = new BasicSubProfileMetaData();
         md.setName(subProfile);
         profileList.add(md);
      }
      metaData.setSubprofiles(profileList);
      
      return metaData;
   }
   
   /**
    * Create a profile repository source meta data.
    * 
    * @param uris the uris for the repository
    * @param hotDeployment to create a hotDeployment profile
    * 
    * @return the profile source meta data.
    */
   protected ProfileSourceMetaData createClusteredSource(URI[] uris, boolean hotDeployment)
   {
      ClusteredProfileSourceMetaData source = null;
      if(hotDeployment)
      {
         source = new HotDeploymentClusteredProfileSourceMetaData();
      }
      else
      {
         source = new ImmutableClusteredProfileSourceMetaData();
      }
      
//      source.setPartitionName(getPartitionName());
      
      List<String> sources = new ArrayList<String>();
      for(URI uri : uris)
         sources.add(uri.toString());
      source.setSources(sources);
      return source;
   }

}
