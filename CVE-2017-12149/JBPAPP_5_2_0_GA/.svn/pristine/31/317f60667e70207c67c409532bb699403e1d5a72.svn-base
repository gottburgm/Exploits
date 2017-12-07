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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profile.repository.metadata.ImmutableProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.BasicDeploymentRepository;
import org.jboss.system.server.profileservice.repository.DefaultDeploymentRepositoryFactory;
import org.jboss.test.BaseTestCase;

/**
 * Test the basic deployment repository.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DeploymentRepositoryUnitTestCase extends BaseTestCase
{

   /** The default key. */
   private static final ProfileKey defaultKey = new ProfileKey(ProfileKey.DEFAULT);
   
   public DeploymentRepositoryUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testSetUploadDir() throws Exception
   {
      File uri = new File("test1");
      
      BasicDeploymentRepository repository = new BasicDeploymentRepository(defaultKey, new URI[] {uri.toURI()});
      assertEquals(uri.toURI(), repository.getUploadUri());
      
      // Set a available uri
      repository.setUploadUri(uri.toURI());
      assertEquals(uri.toURI(), repository.getUploadUri());
      
      File uri2 = new File("test2");
      try
      {
         repository.setUploadUri(uri2.toURI());
         fail("should not be able to set a not managed uri as upload dir.");
      }
      catch(Exception ok)
      {
         log.debug("saw exception", ok);
      }
      assertEquals(uri.toURI(), repository.getUploadUri());
   }
   
   public void testDefaultRepositoryFactory() throws Exception
   {
      File uri1 = new File("test1");
      File uri2 = new File("test2");
      File upload = new File("upload");
      
      List<URI> uris = Collections.singletonList(upload.toURI());
      
      DefaultDeploymentRepositoryFactory f = new DefaultDeploymentRepositoryFactory();
      f.setUploadURIs(uris);
      
      List<String> sourceNames = Arrays.asList(uri1.toURI().toString(), upload.toURI().toString());
      ImmutableProfileSourceMetaData source = new ImmutableProfileSourceMetaData(sourceNames);
      
      // See if the upload directory is set correctly
      BasicDeploymentRepository repository = (BasicDeploymentRepository) f.createDeploymentRepository(defaultKey, source);
      assertEquals(upload.toURI(), repository.getUploadUri());
      
      // Change the factory upload uris
      f.setUploadURIs(Collections.singletonList(uri2.toURI()));
      repository = (BasicDeploymentRepository) f.createDeploymentRepository(defaultKey, source);
      assertEquals(uri1.toURI(), repository.getUploadUri());
   }

}
