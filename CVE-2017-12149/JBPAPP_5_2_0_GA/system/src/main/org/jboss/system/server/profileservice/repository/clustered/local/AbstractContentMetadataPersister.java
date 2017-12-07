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

package org.jboss.system.server.profileservice.repository.clustered.local;

import java.io.File;
import java.io.NotSerializableException;

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;

/**
 * Abstract base class for a {@link ContentMetadataPersister}.
 * 
 * @author Brian Stansberry
 */
public abstract class AbstractContentMetadataPersister
      implements ContentMetadataPersister
{ 
   private static final Logger log = Logger.getLogger(AbstractContentMetadataPersister.class); 
   
   private final File contentMetadataDir;
   
   /**
    * Create a new AbstractContentMetadataPersister.
    * 
    * @param dir directory in which content metadata should be persisted.
    *            Cannot be <code>null</code>.
    */
   public AbstractContentMetadataPersister(File dir)
   {
      if(dir == null)
         throw new IllegalArgumentException("Null store dir.");
      this.contentMetadataDir = dir;
   }

   // ------------------------------------------------ ContentMetadataPersister
   
   public RepositoryContentMetadata load(String baseName)
   {
      File attachmentsStore = getMetadataPath(baseName);
      if( attachmentsStore.exists() == false )
      {
         return null;
      }

      try
      {
         return loadMetadata(attachmentsStore);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void store(String baseName, RepositoryContentMetadata metadata)
   {
      File attachmentsStore = getMetadataPath(baseName);
      File attachmentsParent = attachmentsStore.getParentFile();
      if( attachmentsParent.exists() == false )
      {
         if( attachmentsParent.mkdirs() == false )
            throw new RuntimeException("Failed to create attachmentsParent: "+attachmentsParent.getAbsolutePath());
      }

      if( metadata != null )
      {
         try
         {
            saveMetadata(attachmentsStore, metadata);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch(NotSerializableException e)
         {
            // Log what is in the attachments
            StringBuilder tmp = new StringBuilder("Save failed with NSE, attachments contents: ");
            tmp.append(metadata).append(" to: ").append(attachmentsStore);
            log.error(tmp.toString());
            throw new RuntimeException(e);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }
   
   // ------------------------------------------------------------------ Public
   
   /**
    * Get the file where metadata is stored.
    * 
    * @param storeName the name of the store; identifies which metadata is desired
    * 
    * @return the file. Will not be <code>null</code>
    */
   public abstract File getMetadataPath(String storeName);

   // --------------------------------------------------------------  Protected
   
   /**
    * Actually load the metadata
    * 
    * @param metadataStore file where metadata is stored
    * 
    * @return the metadata
    */
   protected abstract RepositoryContentMetadata loadMetadata(File metadataStore) throws Exception;
   
   /**
    * Actually store the given metadata.
    * 
    * @param metadataStore file where metadata should be stored. Cannot be <code>null</code>
    * @param metadata the metadata. Cannot be <code>null</code>
    * 
    * @throws Exception
    */
   protected abstract void saveMetadata(File metadataStore, RepositoryContentMetadata metadata) throws Exception;
   
   /**
    * Get the base directory where content metadata is to be stored.
    * 
    * @return the directory. Will not be <code>null</code>.
    */
   protected File getContentMetadataDir()
   {
      return contentMetadataDir;
   }
}
