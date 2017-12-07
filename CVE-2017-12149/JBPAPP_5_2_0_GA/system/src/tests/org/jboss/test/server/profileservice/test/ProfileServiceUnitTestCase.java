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
package org.jboss.test.server.profileservice.test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.dependency.plugins.AbstractController;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.system.server.profileservice.repository.AbstractBootstrapProfileFactory;
import org.jboss.system.server.profileservice.repository.AbstractProfileService;
import org.jboss.system.server.profileservice.repository.MainDeployerAdapter;
import org.jboss.test.server.profileservice.support.MockAttachmentStore;
import org.jboss.test.server.profileservice.support.MockMainDeployer;
import org.jboss.test.server.profileservice.support.XmlProfileFactory;
import org.jboss.virtual.plugins.context.jar.JarUtils;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86218 $
 */
public class ProfileServiceUnitTestCase extends AbstractProfileServiceTestBase
{

   private ProfileService profileService;
   
   public ProfileServiceUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      // Create profile service
      MockMainDeployer mainDeployer = new MockMainDeployer();
      MainDeployerAdapter adapter = new MainDeployerAdapter();
      adapter.setMainDeployer(mainDeployer);
      adapter.setAttachmentStore(new MockAttachmentStore());
      AbstractController parentController = new AbstractController();
      AbstractProfileService profileService = new AbstractProfileService(parentController);
      profileService.setDeployer(adapter);
      // 
      profileService.create();
      this.profileService = profileService;
   }
   
   public void testProfileService() throws Exception
   {
      // Set the server base url as system property for the repository generation.
      File serverBaseDir = new File("src/resources/server-root/");
      System.setProperty("jboss.server.base.url", serverBaseDir.toURL().toExternalForm());
      
      // Setup profile locations
      File f = new File("src/resources/parsing-tests/");
      File one = new File(f, "config/profiles");
      File two = new File(f, "common/profiles");
      
      // 
      AbstractBootstrapProfileFactory profileFactory = new XmlProfileFactory(
            new URI[] { one.toURI(), two.toURI() });
      profileFactory.setProfileFactory(createProfileFactory());
      
      // Clear jar suffixes.
      JarUtils.clearSuffixes();
      
      // A list of profile keys
      List<ProfileKey> keys = new ArrayList<ProfileKey>();
      
      // Parse
      Collection<Profile> profiles = profileFactory.createProfiles(new ProfileKey("default"), null);
      for(Profile profile : profiles)
      {
         // Register
         profileService.registerProfile(profile);
         ProfileKey key = profile.getKey();
         keys.add(key);
         try
         {
            // This is the default behavior. For custom profiles this could be different
            profileService.getActiveProfile(key);
            fail("profile already registered  "+ key);
         }
         catch(NoSuchProfileException e)
         {
            // ok
         }
      }
      
      // All profiles should be registered now
      for(ProfileKey key : keys)
         profileService.getProfile(key);
      
      try
      {
         // Activate profile
         ProfileKey key = new ProfileKey("default");
         profileService.activateProfile(key);
         profileService.validateProfile(key);
      }
      catch(Exception e)
      {
         getLog().error("failed to activate: ", e);
         throw e;
      }

      // Test the default profile
      Profile active = profileService.getActiveProfile(new ProfileKey("default"));
      assertNotNull(active);
      
      // All profiles should be active now
      for(ProfileKey key : keys)
         profileService.getActiveProfile(key);
      
      // Assert default profile
      assertActive("default");
      // Assert bootstrap profile
      assertActive("bootstrap");
      // Assert seam profile
      assertActive("seam-deployers");
      // Assert ejb3-deployers profile
      assertActive("ejb3-deployers");
      // Assert ejb3-runtime profile
      assertActive("ejb3-runtime");
      // Assert web-deployers profile
      assertActive("jboss-web-deployers");
      // Assert web-runtime profile
      assertActive("jboss-web-runtime");
      // Assert clustering-deployers
      assertActive("clustering-deployers");
      // Assert hasingleton
      assertActive("clustering-runtime");
      // Assert metadata-deployers profile
      assertActive("metadata-deployer-beans");
      
      for(ProfileKey key : profileService.getActiveProfileKeys())
      {
         profileService.deactivateProfile(key);
         try
         {
            profileService.getActiveProfile(key);
            fail("profile still active. " + key);
         }
         catch(NoSuchProfileException e)
         {
            // OK
         }
      }
      // No active profiles
      assertTrue(profileService.getActiveProfileKeys().isEmpty());
      
      for(ProfileKey key : profileService.getProfileKeys())
      {
         profileService.unregisterProfile(key);
         try
         {
            profileService.getProfile(key);
            fail("profile not unregistered " + key);
         }
         catch(NoSuchProfileException e)
         {
            // OK
         }
      }
      // No registred profiles
      assertTrue(profileService.getProfileKeys().isEmpty());
   }
   
   protected void assertActive(String profile) throws Exception
   {
      assertNotNull(profileService.getActiveProfile(new ProfileKey(profile)));
   }

}

