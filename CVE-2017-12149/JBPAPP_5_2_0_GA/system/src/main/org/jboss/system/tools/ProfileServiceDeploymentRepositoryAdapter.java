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
package org.jboss.system.tools;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.MutableProfile;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.system.server.profileservice.hotdeploy.Scanner;
import org.jboss.system.server.profileservice.repository.HotDeploymentRepository;
import org.jboss.system.server.profileservice.repository.TypedProfileRepository;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Profile deployment repository adapter.
 *
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ProfileServiceDeploymentRepositoryAdapter implements DeploymentRepositoryAdapter
{
   private static final Logger log = Logger.getLogger(ProfileServiceDeploymentRepositoryAdapter.class);
   private static final ProfileKey profileName = new ProfileKey("deployment-scanner-profile");

   private Scanner scanner;
   private ProfileService ps;
   private TypedProfileRepository repositories;
   private StructureModificationChecker checker;

   private DeploymentScannerProfile profile;

   public ProfileServiceDeploymentRepositoryAdapter(Scanner scanner, ProfileService ps, StructureModificationChecker checker)
   {
      if (scanner == null)
         throw new IllegalArgumentException("Null scanner");
      if (ps == null)
         throw new IllegalArgumentException("Null profile service");
      if (checker == null)
         throw new IllegalArgumentException("Null structure checker");

      this.scanner = scanner;
      this.ps = ps;
      this.checker = checker;
   }
   
   public TypedProfileRepository getProfileRepository()
   {
      return repositories;
   }
   
   public void setProfileRepository(TypedProfileRepository repositories)
   {
      this.repositories = repositories;
   }

   /**
    * Create profile.
    *
    * @throws Exception for any error
    */
   public void create() throws Exception
   {
      this.profile = new DeploymentScannerProfile(checker);
      // Create the profile
      registerProfile();
   }

   /**
    * Stop profile.
    */
   public void destroy()
   {
      stopProfile();
   }

   /**
    * Register profile.
    *
    * @throws Exception for any error
    */
   protected void registerProfile() throws Exception
   {
      if(this.ps == null)
         throw new IllegalStateException("Null profile service.");

      // Register
      this.ps.registerProfile(profile);

      // Activate
      log.debug("Activating deployment scanner profile " + profileName);
      this.ps.activateProfile(profileName);
      this.ps.validateProfile(profileName);
      // Expose the profile to the DeploymentManager
      if(this.repositories != null)
         this.repositories.registerDeploymentRepository(profile);
   }

   public void resume()
   {
      scanner.resume();
   }

   public void suspend()
   {
      scanner.suspend();
   }

   /**
    * Stop deactivates and unregisters the transient deployments profile.
    */
   public void stopProfile()
   {
      // Unregister the profile
      if(this.repositories != null)
         this.repositories.unregisterDeploymentRepository(profile);
      try
      {
         // Deactivate
         log.debug("Deactivating deployment scanner profile: " + profileName);
         this.ps.deactivateProfile(profileName);
      }
      catch(Exception e)
      {
         log.debug("Failed to deactivate deployment scanner profile: ", e);
      }
      try
      {
         // Unregister
         log.debug("Unregistering transient profile: " + profileName);
         this.ps.unregisterProfile(profileName);
      }
      catch(Exception e)
      {
         log.debug("Failed to unregister deployment scanner profile: ", e);
      }
   }

   public void addURL(URL url) throws URISyntaxException
   {
      URI uri = url.toURI();
      Collection<URI> uris = profile.getURIs();
      if (uris.contains(uri) == false)
      {
         uris.add(uri);
      }
   }

   public void removeURL(URL url) throws URISyntaxException
   {
      URI uri = url.toURI();
      Collection<URI> uris = profile.getURIs();
      uris.remove(uri);
   }

   public boolean hasURL(URL url) throws URISyntaxException
   {
      URI uri = url.toURI();
      // TODO - this only checks this profile
      return profile.getURIs().contains(uri);
   }

   public String[] listDeployedURLs()
   {
      List<String> urls = new ArrayList<String>();

      Collection<ProfileKey> activeProfiles = ps.getActiveProfileKeys();
      if (activeProfiles != null && activeProfiles.isEmpty() == false)
      {
         for (ProfileKey key : activeProfiles)
         {
            // The profile
            Profile profile;
            try
            {
               profile = ps.getActiveProfile(key);
            }
            catch (NoSuchProfileException ignore)
            {
               continue;
            }
            Collection<ProfileDeployment> deployments = profile.getDeployments();
            if (deployments != null && deployments.isEmpty() == false)
            {
               for (ProfileDeployment pd : deployments)
               {
                  VirtualFile root = pd.getRoot();
                  if (root != null)
                  {
                     try
                     {
                        urls.add(root.toURL().toExternalForm());
                     }
                     catch (Exception e)
                     {
                        log.warn("Exception while reading root's URL: " + root);
                     }
                  }
               }
            }
         }
      }

      return urls.toArray(new String[urls.size()]);
   }

   public static class DeploymentScannerProfile extends HotDeploymentRepository implements MutableProfile
   {
      private volatile boolean enableHotDeployment;
      private Map<URI, List<VirtualFile>> oldCache = new ConcurrentHashMap<URI, List<VirtualFile>>();
      private Map<URI, List<VirtualFile>> newCache = new ConcurrentHashMap<URI, List<VirtualFile>>();

      public DeploymentScannerProfile(StructureModificationChecker checker)
      {
         super(profileName, new URI[0]);
         setChecker(checker);
      }

      /**
       * Expose uris.
       *
       * @return the uris
       */
      Collection<URI> getURIs()
      {
         return uris;
      }

      public void addDeployment(ProfileDeployment deployment) throws Exception
      {
         super.addDeployment(deployment.getName(), deployment);
      }

      public void enableModifiedDeploymentChecks(boolean flag)
      {
         this.enableHotDeployment = flag;
      }

      @Override
      public Collection<ModificationInfo> getModifiedDeployments() throws Exception
      {
         if(this.enableHotDeployment == false)
            return Collections.emptySet();

         return super.getModifiedDeployments();
      }
      
      @Override
      protected void checkForAdditions(List<ModificationInfo> modified) throws Exception
      {
         // clear new cache
         newCache.clear();
         // do real check
         super.checkForAdditions(modified);
         // remove the old stuff - what's left of it
         long lastModified = System.currentTimeMillis();
         for (List<VirtualFile> files : oldCache.values())
         {
            for (VirtualFile file : files)
            {
               // the key is URI
               String name = file.toURI().toString();
               // it still exists - remove it
               if (acceptsDeployment(name) == false)
               {
                  unlockRead();
                  ProfileDeployment previous;
                  try
                  {
                     // the actual removal, but we don't delete the file
                     previous = removeDeployment(name, false);
                  }
                  finally
                  {
                     lockRead();
                  }
                  ModificationInfo removed = new ModificationInfo(previous, lastModified, ModificationInfo.ModifyStatus.REMOVED);
                  modified.add(removed);
               }
            }
         }
         // switch new --> old
         oldCache.clear();
         oldCache.putAll(newCache);
      }

      @Override
      protected void applyAddedDeployments(URI applicationDir, List<ModificationInfo> modified, List<VirtualFile> added) throws Exception
      {
         // remove from old cache - it exists
         List<VirtualFile> files = oldCache.remove(applicationDir);
         // do real apply
         super.applyAddedDeployments(applicationDir, modified, added);
         // add to old + put to new
         if (files == null)
            files = new ArrayList<VirtualFile>(added);
         else
            files.addAll(added);
         newCache.put(applicationDir, files);
      }

      @Override
      protected List<String> findDeploymentContent(String name)
      {
         // FIXME this should not be done here
         // Try to find the VirtualFile, as we only add real file urls
         VirtualFile cached = getCachedVirtualFile(name);
         if(cached != null)
         {
            try {
               name = cached.toURI().toString();
            } catch(Exception ignore) { }
            return Collections.singletonList(name);
         }
         
         List<String> contents = new ArrayList<String>();
         for(URI uri : this.uris)
         {
            String cacheName = uri.toString(); 
            String fixedName = cacheName;
            if(cacheName.endsWith("/"))
               fixedName = cacheName.substring(0, cacheName.length() -1);
            
            if(fixedName.endsWith(name))
            {
               VirtualFile vf = getCachedVirtualFile(cacheName);
               if(vf != null)
               {
                  try {
                     contents.add(vf.toURI().toString()); 
                  } catch(Exception ignore) { }                  
               }
            }
         }
         return contents;
      }
      
      @Override
      public ProfileDeployment removeDeployment(String vfsPath) throws Exception
      {
         // We don't remove the actual deployment content
         return super.removeDeployment(vfsPath, false);
      }
      
      public ProfileKey getKey()
      {
         return profileName;
      }

      public Collection<ProfileKey> getSubProfiles()
      {
         return Collections.emptySet();
      }

      public boolean hasDeployment(String name)
      {
         // FIXME
         return false;
      }

      public boolean isMutable()
      {
         return true;
      }
   }
}