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

import java.util.List;

import junit.framework.TestCase;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.RemoteContentModificationGenerator;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification.Type;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class RemoteContentModificationGeneratorUnitTestCase extends TestCase
{

   /**
    * Create a new RemoteContentModificationGeneratorUnitTestCase.
    * 
    * @param name
    */
   public RemoteContentModificationGeneratorUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testJoinAdditionToEmpty() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item = new RepositoryItemMetadata();
      item.setRelativePath("item");
      item.setOriginatingNode("localhost");
      item.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(new MockSynchronizationPolicy(), getNewRepositoryContentMetadata());
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(1, mods.size());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(0).getType());
      assertEquals(item, mods.get(0).getItem());
   }
   
   public void testMergeAdditionToEmpty() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item = new RepositoryItemMetadata();
      item.setRelativePath("item");
      item.setOriginatingNode("localhost");
      item.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(new MockSynchronizationPolicy());
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(1, mods.size());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(0).getType());
      assertEquals(item, mods.get(0).getItem());
   }
   
   public void testSimpleJoinAddition() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      base.getRepositories().iterator().next().getContent().add(item1);

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item1);
      
      RepositoryItemMetadata item2 = new RepositoryItemMetadata();
      item2.setRelativePath("item2");
      item2.setOriginatingNode("localhost");
      item2.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item2);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(new MockSynchronizationPolicy(), 
                                                getNewRepositoryContentMetadata());
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(1, mods.size());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(0).getType());
      assertEquals(item2, mods.get(0).getItem());
   }
   
   public void testSimpleMergeAddition() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      base.getRepositories().iterator().next().getContent().add(item1);

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item1);
      
      RepositoryItemMetadata item2 = new RepositoryItemMetadata();
      item2.setRelativePath("item2");
      item2.setOriginatingNode("localhost");
      item2.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item2);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(new MockSynchronizationPolicy());
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(1, mods.size());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(0).getType());
      assertEquals(item2, mods.get(0).getItem());
   }
   
   public void testJoinSwap() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      base.getRepositories().iterator().next().getContent().add(item1);

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item2 = new RepositoryItemMetadata();
      item2.setRelativePath("item2");
      item2.setOriginatingNode("localhost");
      item2.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item2);
      
      MockSynchronizationPolicy policy = new MockSynchronizationPolicy();
      policy.setAllowJoinRemovals(Boolean.FALSE);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(policy, getNewRepositoryContentMetadata());
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(2, mods.size());
      assertEquals(Type.PULL_FROM_CLUSTER, mods.get(0).getType());
      assertEquals(item1, mods.get(0).getItem());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(1).getType());
      assertEquals(item2, mods.get(1).getItem());
   }
   
   public void testMergeSwap() throws Exception
   {
      RepositoryContentMetadata base = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item1 = new RepositoryItemMetadata();
      item1.setRelativePath("item1");
      item1.setOriginatingNode("localhost");
      item1.setTimestamp(1);
      base.getRepositories().iterator().next().getContent().add(item1);

      RepositoryContentMetadata modified = getNewRepositoryContentMetadata();
      
      RepositoryItemMetadata item2 = new RepositoryItemMetadata();
      item2.setRelativePath("item2");
      item2.setOriginatingNode("localhost");
      item2.setTimestamp(1);
      modified.getRepositories().iterator().next().getContent().add(item2);
      
      MockSynchronizationPolicy policy = new MockSynchronizationPolicy();
      policy.setAllowMergeRemovals(Boolean.FALSE);
      
      RemoteContentModificationGenerator testee = 
         new RemoteContentModificationGenerator(policy);
      
      List<ContentModification> mods = testee.getModificationList(base, modified);
      
      assertEquals(2, mods.size());
      assertEquals(Type.PULL_FROM_CLUSTER, mods.get(0).getType());
      assertEquals(item1, mods.get(0).getItem());
      assertEquals(Type.PUSH_TO_CLUSTER, mods.get(1).getType());
      assertEquals(item2, mods.get(1).getItem());
   }

   private static RepositoryContentMetadata getNewRepositoryContentMetadata()
   {
     return getNewRepositoryContentMetadata("farm");
   }
   
   private static RepositoryContentMetadata getNewRepositoryContentMetadata(String ... rootNames)
   {
      RepositoryContentMetadata base = new RepositoryContentMetadata();
      base.setDomain("domain");
      base.setServer("server");
      base.setName("name");
      for (String rootName : rootNames)
      {
         RepositoryRootMetadata root = new RepositoryRootMetadata();
         root.setName(rootName);
         base.getRepositories().add(root);
      }
      
      return base;
   }
}
