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

package org.jboss.system.server.profileservice.repository.clustered.local.file;

import java.io.File;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationActionContext;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationId;

/**
 * {@link SynchronizationActionContext} subclass that provides additional
 * contextual information useful to filesystem based actions.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class FileBasedSynchronizationActionContext extends SynchronizationActionContext
{
   private final File tempDir;
   private final String storeName;
   
   public FileBasedSynchronizationActionContext(SynchronizationId<?> id, 
         RepositoryContentMetadata inProgressMetadata, File tempDir, String storeName)
   {
      super(id, inProgressMetadata);
      if (storeName == null)
      {
         throw new IllegalArgumentException("Null storeName");
      }
      this.storeName = storeName;
      this.tempDir = tempDir;
   }

   public File getTempDir()
   {
      return tempDir;
   }

   public String getStoreName()
   {
      return storeName;
   }
   
   
}
