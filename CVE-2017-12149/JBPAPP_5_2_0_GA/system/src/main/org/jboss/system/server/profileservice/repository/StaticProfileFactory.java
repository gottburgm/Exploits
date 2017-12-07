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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.AbstractProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.BasicSubProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.EmptyProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.FilteredProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.HotDeploymentProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.ImmutableProfileSourceMetaData;

/** 
 * A profile factory based on a static configuration.
 * This creates the legacy configuration: bootstrap, deployers, applications 
 * and the root profile.  
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 105720 $
 */
public class StaticProfileFactory extends AbstractBootstrapProfileFactory
{
   /** The bindings profile name. */
   private String bindingsName = "bindings";
   
   /** The bootstrap profile name. */
   private String bootstrapName = "bootstrap";
   
   /** The deployers profile name. */
   private String deployersName = "deployers";
   
   /** The applications profile name. */
   private String applicationsName = "applications";
   
   /** The bindings uri. */
   private URI bindingsURI;
   
   /** The bootstrap uri. */
   private URI bootstrapURI;
   
   /** The deployers uri. */
   private URI deployersURI;
   
   /** The attachment store uri. */
   private File attachmentStoreRoot;
   
   /** The application uris. */
   private List<URI> applicationURIs;
   
   /** Ignore non existing application URIs. */
   private boolean ignoreNonExistingApplicationURIs = false;

   public String getBootstrapName()
   {
      return bootstrapName;
   }
   
   public void setBootstrapName(String bootstrapName)
   {
      this.bootstrapName = bootstrapName;
   }
   
   public String getDeployersName()
   {
      return deployersName;
   }
   
   public void setDeployersName(String deployersName)
   {
      this.deployersName = deployersName;
   }
   
   public String getApplicationsName()
   {
      return applicationsName;
   }
   
   public void setApplicationsName(String applicationsName)
   {
      this.applicationsName = applicationsName;
   }
   
   public URI getBindingsURI()
   {
      return bindingsURI;
   }

   public void setBindingsURI(URI bindingsURI)
   {
      this.bindingsURI = bindingsURI;
   }
   
   public URI getBootstrapURI()
   {
      return bootstrapURI;
   }

   public void setBootstrapURI(URI bootstrapURI)
   {
      this.bootstrapURI = bootstrapURI;
   }

   public URI getDeployersURI()
   {
      return deployersURI;
   }

   public void setDeployersURI(URI deployersURI)
   {
      this.deployersURI = deployersURI;
   }

   public File getAttachmentStoreRoot()
   {
      return attachmentStoreRoot;
   }

   public void setAttachmentStoreRoot(File attachmentStoreRoot)
   {
      this.attachmentStoreRoot = attachmentStoreRoot;
   }

   public List<URI> getApplicationURIs()
   {
      if(applicationURIs == null)
         return Collections.emptyList();
      return applicationURIs;
   }
   
   public void setApplicationURIs(List<URI> applicationURIs)
   {
      this.applicationURIs = applicationURIs;
   }
   
   public boolean isIgnoreNonExistingApplicationURIs()
   {
      return ignoreNonExistingApplicationURIs;
   }
   
   public void setIgnoreNonExistingApplicationURIs(boolean ignoreNonExistingApplicationURIs)
   {
      this.ignoreNonExistingApplicationURIs = ignoreNonExistingApplicationURIs;
   }
   
   public void create() throws Exception
   {
      // Sanity checks
      if(this.bootstrapURI == null)
         throw new IllegalStateException("Null bootstrap uri.");
      if(this.deployersURI == null)
         throw new IllegalStateException("Null deployers uri.");
      if(this.applicationURIs == null)
         throw new IllegalStateException("Null application uris.");

      if(isIgnoreNonExistingApplicationURIs())
      {
         // Sanitize application URIss
         List<URI> applicationUris = new ArrayList<URI>();
         for(URI uri : getApplicationURIs())
         {
            File file = new File(uri);
            if(file.exists())
            {
               applicationUris.add(file.toURI());
            }
         }
         this.applicationURIs = applicationUris;
      }
   }
   
   public void start()
   {
      if(applicationURIs == null || applicationURIs.isEmpty())
      {
         throw new IllegalStateException("null or empty application URIs");
      }
   }

   /**
    * Create the legacy profiles, based on the injected uris. 
    * 
    * @param rootKey the key for the root profile.
    * @throws Exception
    */
   @Override
   protected void createProfileMetaData(ProfileKey rootKey, URL url) throws Exception
   {     
      if(rootKey == null)
         throw new IllegalArgumentException("Null root profile key.");
      
      String[] subprofileNames = new String[0];
      
      if (bindingsURI != null)
      {
         // Create bindings profile meta data
         ProfileKey bindingsKey = new ProfileKey(bindingsName);
         BasicProfileMetaData bindings = new FilteredProfileMetaData(
               null, null, bindingsName);
         bindings.setSource(createSource(new URI[]{ bindingsURI }, false));
         addProfile(bindingsKey, bindings);
         subprofileNames = new String[] { bindingsName };
      }
      // Create bootstrap profile meta data
      ProfileKey bootstrapKey = new ProfileKey(bootstrapName);
      ProfileMetaData bootstrap = createProfileMetaData(bootstrapName, false, 
            new URI[] { bootstrapURI }, subprofileNames );
      addProfile(bootstrapKey, bootstrap);
      
      subprofileNames = createSubprofileNames(subprofileNames, bootstrapName);
      
      // Create deployers profile meta data
      ProfileKey deployersKey = new ProfileKey(deployersName);
      ProfileMetaData deployers = createProfileMetaData(
            deployersName, false, new URI[] { deployersURI }, subprofileNames);
      addProfile(deployersKey, deployers);
      
      subprofileNames = createSubprofileNames(subprofileNames, deployersName);

      // Create applications profile meta data
      String[] rootSubProfiles = createApplicationProfiles(subprofileNames);
      
      // Create empty root profile;
      ProfileMetaData root = new EmptyProfileMetaData(
            null, null, rootKey.getName(), createSubProfileMetaData(rootSubProfiles)); 

      // Add to profile map
      addProfile(rootKey, root);
   }
   
   /**
    * Create the application sub profiles.
    * 
    * @param applicationsSubProfiles
    * @return the dependencies for the root profile
    */
   protected String[] createApplicationProfiles(String[] applicationsSubProfiles)
   {
      return new String[] { createApplicationProfile(applicationsSubProfiles) };
   }
   
   /**
    * Create the applications sub profile.
    * 
    * @param applicationsSubProfiles the dependencies for the application profile
    * @return the application profile name
    */
   protected String createApplicationProfile(String[] applicationsSubProfiles)
   {
      ProfileKey applicationsKey = new ProfileKey(applicationsName);
      URI[] applicationURIs = getApplicationURIs().toArray(new URI[getApplicationURIs().size()]);
      ProfileMetaData applications = createProfileMetaData(
            applicationsName, true, applicationURIs, applicationsSubProfiles);
      // Add to profile map
      addProfile(applicationsKey, applications);
      return applicationsName;
   }
   
   /**
    * Create a basic profile meta data. This profile will have it's own
    * DeploymentRepository and therefore exposed in the DeploymentManager
    * for deploy actions.
    * 
    * @param name the profile name.
    * @param isHotDeployment if hotDeployment is enabled.
    * @param uris the repository uris.
    * @param subProfiles a list of profile dependencies.
    * 
    * @return the profile meta data.
    */
   protected ProfileMetaData createProfileMetaData(String name, boolean isHotDeployment, URI[] uris, String[] subProfiles)
   {
      // Create profile
      BasicProfileMetaData metaData = new BasicProfileMetaData(null, null, name);
      
      // Create profile sources
      metaData.setSource(createSource(uris, isHotDeployment));
      
      // Set subProfiles
      metaData.setSubprofiles(createSubProfileMetaData(subProfiles));
      
      return metaData;
   }
   
   protected List<SubProfileMetaData> createSubProfileMetaData(String[] subProfiles)
   {
      List<SubProfileMetaData> subProfileList = new ArrayList<SubProfileMetaData>();
      if(subProfiles != null && subProfiles.length > 0)
      {
         for(String profileName : subProfiles)
         {
            subProfileList.add(new BasicSubProfileMetaData(null, null, profileName));
         }
      }
      return subProfileList;
   }
   
   
   /**
    * Create a profile repository source meta data.
    * 
    * @param uris the uris for the repository
    * @param isHotDeployment to create a hotDeployment profile
    * 
    * @return the profile source meta data.
    */
   protected ProfileSourceMetaData createSource(URI[] uris, boolean isHotDeployment)
   {
      AbstractProfileSourceMetaData source = null;
      if(isHotDeployment)
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
   
   /** Simple "copy array and add one more element" utility */
   private static String[] createSubprofileNames(String[] existing, String toAdd)
   {
      String[] subprofileNames = new String[existing.length + 1];
      System.arraycopy(existing, 0, subprofileNames, 0, existing.length);
      subprofileNames[subprofileNames.length - 1] = toAdd;
      return subprofileNames;
   }
}
