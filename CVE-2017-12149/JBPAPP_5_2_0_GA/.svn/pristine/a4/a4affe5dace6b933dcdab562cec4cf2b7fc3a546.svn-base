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
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.local.AbstractLocalContentManager;
import org.jboss.system.server.profileservice.repository.clustered.local.ContentMetadataPersister;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManager;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.NoOpSynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.RemoteRemovalAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SimpleSynchronizationRemoteAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationId;
import org.jboss.system.server.profileservice.repository.clustered.sync.TwoPhaseCommitAction;

/**
 * {@link LocalContentManager} that persists to the local filesystem.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class FilesystemLocalContentManager extends AbstractLocalContentManager<FileBasedSynchronizationActionContext>
{
   private final File tmpDir;
   
   /**
    * Create a new FilesystemRepositoryContentPersister.
    * 
    * @param namedURIMap
    * @param storeName
    * @param localNodeName
    * @param contentMetadataPersister
    */
   protected FilesystemLocalContentManager(Map<String, URI> namedURIMap, ProfileKey profileKey, String localNodeName,
         ContentMetadataPersister contentMetadataPersister, URI tempDirURI)
   {
      super(namedURIMap, profileKey, localNodeName, contentMetadataPersister);
      
      if (tempDirURI != null)
      {
         this.tmpDir = new File(tempDirURI);
      }
      else
      {
         this.tmpDir = null;
      }
   }
   
   // --------------------------------------------------------------  Protected

   @Override
   protected FileBasedSynchronizationActionContext createSynchronizationActionContext(
         SynchronizationId<?> id, RepositoryContentMetadata toUpdate)
   {
      return new FileBasedSynchronizationActionContext(id, toUpdate, tmpDir, getStoreName());
   }
   
   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createPullFromClusterAction(
         ContentModification mod, boolean localLed)
   {
      File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
      if (localLed)
      {
         return new FileWriteAction(targetFile, getSynchronizationActionContext(), mod);
      }
      else
      {
         return new FileReadAction(targetFile, getSynchronizationActionContext(), mod);    
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createPushToClusterAction(ContentModification mod,
         boolean localLed)
   {
      File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
      if (localLed)
      {
         return new FileReadAction(targetFile, getSynchronizationActionContext(), mod); 
      }
      else
      {
         return new FileWriteAction(targetFile, getSynchronizationActionContext(), 
                                    mod);       
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createPushStreamToClusterAction(ContentModification mod, InputStream stream)
   {
      File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
      return new AddContentStreamAction(stream, targetFile, getSynchronizationActionContext(), mod);
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createRemoveFromClusterAction(
         ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new RemoveFileAction(targetFile, getSynchronizationActionContext(), 
                                     mod); 
      }
      else
      {
         // nothing to do on a remote node
         return new NoOpSynchronizationAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);         
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createRemoveToClusterAction(
         ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         return new RemoteRemovalAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);      
      }
      else
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new RemoveFileAction(targetFile, getSynchronizationActionContext(), 
                                     mod);       
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createDirectoryTimestampMismatchAction(ContentModification mod,
         boolean localLed)
   {
      if (localLed)
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new DirectoryTimestampUpdateAction(targetFile, getSynchronizationActionContext(), 
                                                   mod);
      }
      else
      {
         // nothing to do on a remote node
         return new NoOpSynchronizationAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);            
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createMkdirFromClusterAction(ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new MkDirAction(targetFile, getSynchronizationActionContext(), 
                                mod);
      }
      else
      {
         // nothing to do on a remote node
         return new NoOpSynchronizationAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);          
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createMkdirToClusterAction(ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         return new SimpleSynchronizationRemoteAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);
      }
      else
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new MkDirAction(targetFile, getSynchronizationActionContext(), 
                                mod);   
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createPrepareRmdirFromClusterAction(ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new InitiateRmdirAction(targetFile, getSynchronizationActionContext(), 
                                       mod);
      }
      else
      {
         // nothing to do on a remote node
         return new NoOpSynchronizationAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod);
      }
   }

   @Override
   protected TwoPhaseCommitAction<FileBasedSynchronizationActionContext> createPrepareRmdirToClusterAction(ContentModification mod, boolean localLed)
   {
      if (localLed)
      {
         boolean initiation = true;
         return new SimpleSynchronizationRemoteAction<FileBasedSynchronizationActionContext>(getSynchronizationActionContext(), mod, initiation);
      }
      else
      {
         File targetFile = FileUtil.getFileForItem(getRootURIForModification(mod), mod.getItem());
         return new InitiateRmdirAction(targetFile, getSynchronizationActionContext(), 
                                       mod);
      }
   }
   
   
}
