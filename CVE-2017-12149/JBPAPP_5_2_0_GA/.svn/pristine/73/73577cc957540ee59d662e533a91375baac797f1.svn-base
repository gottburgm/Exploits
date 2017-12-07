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
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.local.ContentMetadataPersister;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManager;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManagerFactory;

/**
 * {@link LocalContentManagerFactory} that creates a
 * {@link FilesystemLocalContentManager}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class FilesystemLocalContentManagerFactory implements LocalContentManagerFactory<FileBasedSynchronizationActionContext>
{
   private ContentMetadataPersister metadataPersister;
   private URI tempDirURI;
   
   // -------------------------------------------------------------  Properties

   public ContentMetadataPersister getMetadataPersister()
   {
      return metadataPersister;
   }

   public void setMetadataPersister(ContentMetadataPersister metadataPersister)
   {
      this.metadataPersister = metadataPersister;
   }
   
   
   
   // --------------------------------------  RepositoryContentPersisterFactory
   
   public URI getTempDirURI()
   {
      return tempDirURI;
   }

   public void setTempDirURI(URI tempDirURI)
   {
      this.tempDirURI = tempDirURI;
   }

   public boolean accepts(Collection<URI> uris)
   {
      try
      {
         testURIs(uris);
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public LocalContentManager<FileBasedSynchronizationActionContext> getLocalContentManager(Map<String, URI> namedURIMap, ProfileKey profileKey,
         String localNodeName)
   {
      if (this.metadataPersister == null)
      {
         throw new IllegalStateException("Null metadataPersister; must configure a " + 
               ContentMetadataPersister.class.getSimpleName());
      }
      // Don't trust they called accept()
      testURIs(namedURIMap.values());
      // OK, looks good
      return new FilesystemLocalContentManager(namedURIMap, profileKey, localNodeName, metadataPersister, tempDirURI);
   }   
   
   // ----------------------------------------------------------------  Private
   
   /**
    * Confirms whether each element of <code>uris</code> can be passed to
    * {@link File}'s constructor, resulting in a <code>File</code> that
    * {@link File#exists() exists}.
    * 
    * @param uris the collection of uris
    * 
    * @throws IllegalArgumentException if any URI fails the above test.
    */
   private static void testURIs(Collection<URI> uris)
   {
      for (URI uri : uris)
      {
         File f = new File(uri);
         if (!f.exists())
         {
            throw new IllegalArgumentException("No file found for URI " + uri);
         }
      }     
   }

}
