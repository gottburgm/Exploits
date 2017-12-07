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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.AbstractBootstrapProfileFactory;
import org.jboss.test.server.profileservice.support.XmlProfileFactory;
import org.jboss.virtual.plugins.context.jar.JarUtils;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public class BootstrapProfileFactoryUnitTestCase extends AbstractProfileServiceTestBase
{
   
   /** The profiles */
   Map<String, Profile> profileMap;
   
   public BootstrapProfileFactoryUnitTestCase(String name)
   {
      super(name);
   }

   public void testParsing() throws Exception
   {
      enableTrace("org.jboss.system.server.profileservice.repository");
      
      // Set the server base url as system property for the repository generation.
      File serverBaseDir = new File("src/resources/server-root/");
      System.setProperty("jboss.server.base.url", serverBaseDir.toURL().toExternalForm());
      
      // Setup profile locations
      File f = new File("src/resources/parsing-tests/");
      File one = new File(f, "config/profiles");
      File two = new File(f, "common/profiles");
      
      // The xml profile factory
      AbstractBootstrapProfileFactory profileFactory = new XmlProfileFactory(
            new URI[] { one.toURI(), two.toURI() });
      profileFactory.setProfileFactory(createProfileFactory());
      
      // Clear jar suffixes.
      JarUtils.clearSuffixes();
      
      // Parse
      Collection<Profile> profiles = profileFactory.createProfiles(new ProfileKey("default"), null);
      assertNotNull(profiles);
      
      // 2 profiles in default.xml
      // 1 profile in seam.xml
      // 2 profiles in clustering.xml
      // 2 profiles in ejb3.xml
      // 2 profiles in web.xml
      assertEquals(10, profiles.size());
      
      this.profileMap = new HashMap<String, Profile>();
      for(Profile profile : profiles)
      {
         assertNotNull(profile.getKey());
         String name = profile.getKey().getName();
         assertNotNull(name);
         this.profileMap.put(name, profile);
      }
      
      // Assert Profiles
      assertProfiles();
      
      // Try to load the profiles, this will try to resolve the deploymentNames 
      // when loading the filtered repository in serverBaseDir.
      for(Profile profile : profiles)
      {
         Method m = null;
         try
         {
            m = profile.getClass().getMethod("create", new Class[0]);
         }
         catch(NoSuchMethodException ignore)
         {
            return;
         }
         boolean isAccessible = m.isAccessible();
         try
         {
            m.setAccessible(true);
            m.invoke(profile, new Object[0]);
         }
         finally
         {
            m.setAccessible(isAccessible);
         }
      }

      // TODO assert metadata
   }
   
   protected void assertProfiles()
   {
      // Assert bootstrap profile
      assertProfile("bootstrap", 0);

      // Assert ejb3-runtime profile
      assertProfile("metadata-deployer-beans", 1,
            new String[] { "bootstrap" } );
      
      // Assert ejb3-deployers profile
      assertProfile("ejb3-deployers", 2,
            new String[] { "metadata-deployer-beans", "bootstrap" } );

      // Assert ejb3-runtime profile
      assertProfile("ejb3-runtime", 3,
            new String[] { "ejb3-deployers", "metadata-deployer-beans", "bootstrap" } );
      
      // Assert seam profile
      assertProfile("seam-deployers", 4,
            new String[] { "ejb3-deployers", "ejb3-runtime", "metadata-deployer-beans", "bootstrap" });
    
      // Assert web-deployers profile
      assertProfile("jboss-web-deployers", 5,
            new String[] { "seam-deployers", "ejb3-deployers", "ejb3-runtime", "metadata-deployer-beans", "bootstrap" } );
      
      // Assert web-runtime profile
      assertProfile("jboss-web-runtime", 6,
            new String[] { "jboss-web-deployers",
            "seam-deployers", "ejb3-deployers", "ejb3-runtime", "metadata-deployer-beans", "bootstrap" } );
      
      // Assert clustering-deployers
      assertProfile("clustering-deployers", 7, new String[] {
            "bootstrap",
            "metadata-deployer-beans",
            "ejb3-deployers",
            "ejb3-runtime",
            "seam-deployers",
            "jboss-web-deployers",
            "jboss-web-runtime"});
      
      // Assert hasingleton
      assertProfile("clustering-runtime", 8, new String[] {
            "bootstrap",
            "metadata-deployer-beans",
            "ejb3-deployers",
            "ejb3-runtime",
            "seam-deployers",
            "jboss-web-deployers",
            "jboss-web-runtime",
            "clustering-deployers"});
      
      // Assert default profile
      assertProfile("default", 9, new String[] {
               "bootstrap",
               "metadata-deployer-beans",
               "ejb3-deployers",
               "ejb3-runtime",
               "seam-deployers",
               "jboss-web-deployers",
               "jboss-web-runtime",
               "clustering-deployers",
               "clustering-runtime"});
   }
   
   protected void assertProfile(String name, int dependencySize, String[] dependencyNames)
   {
      Profile profile = profileMap.get(name);
      assertNotNull("Null profile: " + name, profile);
      assertNotNull("Null dependencies for profile: " + name, profile.getSubProfiles());
      assertDependenciesSize(dependencySize, profile);
      assertDependencies(dependencyNames, profile.getSubProfiles());
   }
   
   protected void assertProfile(String name, int dependencySize)
   {
      assertProfile(name, dependencySize, new String[0]);
   }
   
   protected void assertDependenciesSize(int size, Profile profile)
   {
      assertEquals("dependency size for profile: " + profile.getKey(), size, profile.getSubProfiles().size());
   }

   protected void assertDependencies(String[] dependencyNames, Collection<ProfileKey> subProfiles)
   {
      if(dependencyNames == null || dependencyNames.length == 0)
         return;
      
      List<String> iDependOn = new ArrayList<String>();
      for(ProfileKey key : subProfiles)
      {
         iDependOn.add(key.getName());
      }
      // Assert dependency keys
      for(String name : dependencyNames)
      {
         assertTrue("contains name: " + name, iDependOn.contains(name));
      }
   }
   
}

