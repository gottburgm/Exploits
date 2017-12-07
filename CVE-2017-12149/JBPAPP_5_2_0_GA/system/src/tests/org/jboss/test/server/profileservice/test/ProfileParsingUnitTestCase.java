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

import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.FilteredProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.HotDeploymentProfileMetaData;
import org.jboss.system.server.profile.repository.metadata.ProfilesMetaData;
import org.jboss.test.server.profileservice.support.MavenProfileMetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;


/**
 * Basic xml parsing test case.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87932 $
 */
public class ProfileParsingUnitTestCase extends AbstractProfileServiceTestBase
{

   private static final DefaultSchemaResolver resolver = new DefaultSchemaResolver();
   
   static
   {
      // Add schema bindings
      resolver.addClassBinding("urn:jboss:profileservice:profiles:1.0", ProfilesMetaData.class);
      resolver.addClassBinding("urn:jboss:profileservice:profile:filtered:1.0", FilteredProfileMetaData.class);
      resolver.addClassBinding("urn:jboss:profileservice:profile:hotdeployment:1.0", HotDeploymentProfileMetaData.class);
      resolver.addClassBinding("urn:jboss:profileservice:profile:maven:1.0", MavenProfileMetaData.class);
   }
   
   
   public ProfileParsingUnitTestCase(String name)
   {
      super(name);
   }

   public void testParsing() throws Exception
   {
      try
      {
         File f = new File("src/resources/parsing-tests/parsing/test.xml");
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         
         ProfilesMetaData md = (ProfilesMetaData) unmarshaller.unmarshal(f.toURL().openStream(), resolver);
         assertNotNull(md);
         assertEquals("profiles", md.getName());
         
         assertNotNull(md.getProfiles());
         assertEquals(3, md.getProfiles().size());
         
         // test immutable
         ProfileMetaData immutable = md.getProfiles().get(0);
         assertTrue(immutable instanceof FilteredProfileMetaData);
         
         // test hotdeployment
         ProfileMetaData hotdeployment = md.getProfiles().get(1);
         assertTrue(hotdeployment instanceof HotDeploymentProfileMetaData);
         
         // test wildcard
         ProfileMetaData maven = md.getProfiles().get(2);
         assertTrue(maven instanceof MavenProfileMetaData);
         
         for(ProfileMetaData profile: md.getProfiles())
            log.debug(profile.getSource().getClass());
         
      }
      catch(Exception e)
      {
         getLog().error("failed", e);
         throw e;
      }
   }
   
}

