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

import java.util.Iterator;

import junit.framework.TestCase;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;

/**
 * Unit tests of {@link RepositoryRootMetadata}. 
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: 99052 $
 */
public class RepositoryRootMetadataUnitTestCase extends TestCase
{
   public void testIteratorRemoval()
   {
      RepositoryRootMetadata root = getNewRepositoryRootMetadata("A", "B");
      Iterator<RepositoryItemMetadata> it = root.getContent().iterator();
      it.hasNext();
      RepositoryItemMetadata toRemove = it.next();
      it.remove();
      assertNull(root.getItemMetadata(toRemove.getRelativePathElements()));
   }

   
   private static RepositoryRootMetadata getNewRepositoryRootMetadata(String ... itemNames)
   {
      RepositoryRootMetadata base = new RepositoryRootMetadata();
      base.setName("name");
      for (String itemName : itemNames)
      {
         RepositoryItemMetadata item = new RepositoryItemMetadata();
         item.setRelativePath(itemName);
         base.getContent().add(item);
      }
      
      return base;
   }
}
