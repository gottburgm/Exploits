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

package org.jboss.test.server.profileservice.clustered.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.local.JAXBRepositoryContentMetadataPersister;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;

/**
 * Tests of {@link JAXBRepositoryContentMetadataPersister}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class JAXBRepositoryContentMetadataPersisterUnitTestCase extends TestCase
{
   private Set<File> toDelete = new HashSet<File>();
   
   /**
    * Create a new JAXBRepositoryContentMetadataPersisterUnitTestCase.
    * 
    * @param name
    */
   public JAXBRepositoryContentMetadataPersisterUnitTestCase(String name)
   {
      super(name);
   }
   
   protected void tearDown() throws Exception
   {
      for (File f : toDelete)
      {
         f.delete();         
      }
   }
   
   public void testSerializationDeserialization() throws Exception
   {
      ProfileKey key = new ProfileKey("domain", "server", "name");
      RepositoryContentMetadata rcm = new RepositoryContentMetadata(key);
      RepositoryRootMetadata rrm = new RepositoryRootMetadata("normal");
      Collection<RepositoryItemMetadata> rims = rrm.getContent();
      
      RepositoryItemMetadata rim = new RepositoryItemMetadata();
      rim.setRelativePath("/item");
      rim.setOriginatingNode("192.168.100.1:1099");
      rim.setTimestamp(1);
      rims.add(rim);
      
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_item");
      rim.setRemoved(true);
      rim.setOriginatingNode("192.168.100.1:1099");
      rim.setTimestamp(2);
      rims.add(rim);
      
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/dir.sar");
      rim.setDirectory(true);
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setTimestamp(4);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/dir.sar/item.jar");
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setTimestamp(4);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/dir.sar/META-INF");
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setDirectory(true);
      rim.setTimestamp(3);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/dir.sar/META-INF/jboss-beans.xml");
      rim.setDirectory(true);
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setTimestamp(3);
      rims.add(rim);
      
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_dir.ear");
      rim.setDirectory(true);
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setTimestamp(7);
      rim.setRemoved(true);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_dir.ear/ejb.jar");
      rim.setOriginatingNode("192.168.100.2:1099");
      rim.setTimestamp(5);
      rim.setRemoved(true);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_dir.ear/META-INF");
      rim.setDirectory(true);
      rim.setOriginatingNode("192.168.100.3:1099");
      rim.setTimestamp(7);
      rim.setRemoved(true);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_dir.ear/META-INF/application.xml");
      rim.setDirectory(true);
      rim.setOriginatingNode("192.168.100.3:1099");
      rim.setTimestamp(7);
      rim.setRemoved(true);
      rims.add(rim);
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/removed_dir.ear/war.war");
      rim.setOriginatingNode("192.168.100.3:1099");
      rim.setTimestamp(6);
      rim.setRemoved(true);
      rims.add(rim);
      
      RepositoryRootMetadata emptyRRM = new RepositoryRootMetadata("empty");
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/");
      rim.setOriginatingNode("192.168.100.4:1099");
      rim.setTimestamp(10);
      rcm.setRepositories(Arrays.asList(new RepositoryRootMetadata[]{rrm, emptyRRM}));
      
      rim = new RepositoryItemMetadata();
      rim.setRelativePath("/");
      rim.setOriginatingNode("127.0.0.1:1099");
      rim.setTimestamp(20);
      emptyRRM.getContent().add(rim);
      
      File temp = new File(System.getProperty("java.io.tmpdir"));
      
      JAXBRepositoryContentMetadataPersister testee = new JAXBRepositoryContentMetadataPersister(temp.toURI());
      testee.store("test", rcm);
      
      // Ensure we clean up
      File stored = testee.getMetadataPath("test");
      stored.deleteOnExit();
      toDelete.add(stored);
      
      InputStreamReader isr = new InputStreamReader(new FileInputStream(stored));
      StringWriter writer = new StringWriter();
      int read;
      while ((read = isr.read()) != -1)
         writer.write(read);
      writer.close();
      System.out.println(writer.toString());
      
      RepositoryContentMetadata deserialized = testee.load("test");
      
      assertEquals(rcm, deserialized);
   }
   
   public void testSplit()
   {
      String x = "/";
      String[] split = x.split("/");
      System.out.println(split.length);
      for (String s : split)
         System.out.println(s);
   }

}
