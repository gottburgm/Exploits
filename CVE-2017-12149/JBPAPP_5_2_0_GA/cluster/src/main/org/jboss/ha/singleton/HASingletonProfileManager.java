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
package org.jboss.ha.singleton;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.AbstractProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicSubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.HotDeploymentProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.ImmutableProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.AbstractProfileFactory;

/**
 * Extends {@link HASingletonProfileActivator} by actually creating and
 * registering a {@link Profile} from a configurable set of URIs during
 * the {@link #start()} phase, deregistering it in the {@link #stop()} phase.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class HASingletonProfileManager extends HASingletonProfileActivator implements HASingletonProfileManagerMBean
{   
   private AbstractProfileFactory profileFactory;
   
   /** The list of URIs to scan */
   private List<URI> uriList = new CopyOnWriteArrayList<URI>();
   
   /**
    * Create a new HASingletonProfileManager.
    *
    */
   public HASingletonProfileManager()
   {
      super();
   }
   
   // ----------------------------------------------------------  Properties
   
   public AbstractProfileFactory getProfileFactory()
   {
      return profileFactory;
   }

   public void setProfileFactory(AbstractProfileFactory profileFactory)
   {
      this.profileFactory = profileFactory;
   }

   /**
    * Set the uri list
    * 
    * @param list the list
    * @throws IOException
    */
   public void setURIList(final List<URI> list) throws IOException
   {
      if (list == null)
      {
         throw new NullPointerException("list argument cannot be null");
      }
   
      // start out with a fresh list
      uriList.clear();
   
      for(int n = 0; n < list.size(); n ++)
      {
         URI uri = list.get(n);
         if (uri == null)
         {
            throw new IllegalArgumentException("list element["+n+"] is null");
         }

         if( uriList.add(uri) == true )
         {
            log.debug("Added URI: " + uri);
         }  
      }
      log.debug("URI list: " + uriList);
   }

   /**
    * Get the uri list
    * 
    * @return the list
    */
   public List<URI> getURIList()
   {
      return new ArrayList<URI>(uriList);
   }

   

   // -----------------------------------------------------------------  Public

   /**
    * Builds a profile from the {@link #getURIList() URI list} and registers
    * it under the configured {@link #getProfileKey()}.
    */
   public void start() throws Exception
   {    
      if (this.profileFactory == null)
      {
         throw new IllegalStateException("Must configure profileFactory");
      } 
      
      if (getProfileService() == null)
      {
         throw new IllegalStateException("Must configure profileService");
      }
      
      URI[] rootURIs = uriList.toArray(new URI[uriList.size()]);
      // TODO add dependencies on bootstrap profiles
      String[] rootSubProfiles = new String[0];
      // Create a hotdeployment profile
      ProfileMetaData metadata = createProfileMetaData(true, rootURIs, rootSubProfiles);
      
      Profile profile = profileFactory.createProfile(getProfileKey(), metadata);
      getProfileService().registerProfile(profile);      
   }
   
   /**
    * Unregisters the profile registered in {@link #start()}.
    */
   public void stop() throws Exception
   {      
      ProfileService profSvc = getProfileService();
      ProfileKey profKey = getProfileKey();
      if (profSvc != null &&  profKey != null)
      {
         try
         {
            // Inactivate first if needed
            if (profSvc.getActiveProfileKeys().contains(profKey))
            {
               releaseProfile();
            }
            
            profSvc.unregisterProfile(profKey);
         }
         catch (NoSuchProfileException e)
         {
            log.warn("Could not unregister unknown profile " + profKey);
         }
      }
   }
   
   // ----------------------------------------------------------------  Private

   
   /**
    * Create a profile meta data.
    * 
    * @param name the profile name.
    * @param repositoryType the repository type.
    * @param uris the repository uris.
    * @param subProfiles a list of profile dependencies.
    * @return the profile meta data.
    */
   private ProfileMetaData createProfileMetaData(boolean hotDeployment, URI[] uris, String[] subProfiles)
   {
      // Create profile
      BasicProfileMetaData metaData = new BasicProfileMetaData();
      metaData.setDomain(getProfileDomain());
      metaData.setServer(getProfileServer());
      metaData.setName(getProfileName());
      
      // Create profile sources
      ProfileSourceMetaData source = createSource(uris, hotDeployment);
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
    * @param type the repository type.
    * @param uri the uri
    * @return the profile source meta data.
    */
   protected ProfileSourceMetaData createSource(URI[] uris, boolean hotDeployment)
   {
      AbstractProfileSourceMetaData source = null;
      if(hotDeployment)
      {
         source = new HotDeploymentProfileSourceMetaData();
      }
      else
      {
         source = new ImmutableProfileSourceMetaData();
      }
      List<String> sources = new ArrayList<String>();
      for(URI uri : uris)
         sources.add(uri.toString());
      source.setSources(sources);
      return source;
   }
   
}
