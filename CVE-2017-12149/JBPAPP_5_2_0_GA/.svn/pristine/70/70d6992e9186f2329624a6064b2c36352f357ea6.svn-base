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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.system.server.profileservice.repository.clustered.ClusteredDeploymentRepository;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.AbstractContentModificationGenerator;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationActionContext;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationId;
import org.jboss.virtual.VirtualFile;

/**
 * Object responsible for the local persistence operations associated with a
 * {@link ClusteredDeploymentRepository}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface LocalContentManager<T extends SynchronizationActionContext>
{
   
   /**
    * Gets the "official" record of the contents of the persistent store.
    * This is the version persisted to disk following synchronization
    * of changes with the cluster. 
    * 
    * @return the content metadata
    */
   RepositoryContentMetadata getOfficialContentMetadata();
   
   /**
    * Scan the persistent store for the current content metadata. This
    * is not the "official" metadata that has been persisted, as any
    * changes between it and the {@link #getOfficialContentMetadata() official version}
    * have not been synchronized with the cluster. 
    * 
    * @return the content metadata
    */
   RepositoryContentMetadata getCurrentContentMetadata() throws IOException;
   
   /**
    * Initiate a process of synchronizing this node's persistent store with
    * the rest of the cluster
    * 
    * @param id a unique id for this cluster synchronization process
    * @param modifications the overall list of modifications that will occur during
    *                      this process
    * @param toInstall TODO
    * @param localLed <code>true</code> if this node is driving the synchronization,
    *                 <code>false</code> if another node is
    * @return list of {@link SynchronizationAction}s each of which
    *         can be executed by the caller to fulfill a portion of this node's role in
    *         the overall cluster synchronization
    *         
    * @throws IllegalStateException if another synchronization process has
    *                               been initiated and not yet completed
    */
   List<? extends SynchronizationAction<T>> initiateSynchronization(SynchronizationId<?> id, 
         List<ContentModification> modifications, RepositoryContentMetadata toInstall, boolean localLed);
   
   /**
    * Execute the prepare phase of the two phase commit process for the cluster 
    * synchronization that has been 
    * {@link #initiateSynchronization(SynchronizationId, List, RepsitoryContentMetadata, boolean) initialized}.
    * 
    * @param id id of the synchronization. Cannot be <code>null</code>
    * 
    * @return <code>true</code> if the prepare phase was successful; 
    *         <code>false</code> if not and the synchronization needs to be
    *         rolled back
    * 
    * @throws IllegalStateException if <code>id</code> is not equal to the
    *  id of an uncompleted synchronization started via 
    *  {@link #initiateSynchronization(SynchronizationId, List, RepsitoryContentMetadata, boolean)}
    */
   boolean prepareSynchronization(SynchronizationId<?> id);
   
   /**
    * Complete the two-phase commit process for the cluster synchronization that has been 
    * {@link #prepareSynchronization(SynchronizationId) prepared}. 
    * 
    * @param id id of the synchronization. Cannot be <code>null</code>
    * 
    * @throws IllegalStateException if <code>id</code> is not equal to the
    *  id of an uncompleted synchronization started via 
    *  {@link #initiateSynchronization(SynchronizationId, List, RepsitoryContentMetadata, boolean)}
    */
   void commitSynchronization(SynchronizationId<?> id);
   
   /**
    * Roll back the cluster synchronization. 
    * 
    * @param id id of the synchronization. Cannot be <code>null</code>
    * 
    * @throws IllegalStateException if <code>id</code> is not equal to the
    *  id of an uncompleted synchronization started via 
    *  {@link #initiateSynchronization(SynchronizationId, List, RepsitoryContentMetadata, boolean)}
    */
   void rollbackSynchronization(SynchronizationId<?> id);
   
   /**
    * Creates a new {@link RepositoryContentMetadata} with a child
    * {@link RepositoryRootMetadata} for each of this persister's URIs,
    * but no {@link RepositoryItemMetadata}s under those roots. When a node that
    * is starting for the first time does not have a persisted set of 
    * content metadata, this method should be used to create an object that can 
    * be used as a base to 
    * {@link AbstractContentModificationGenerator#getModificationList(RepositoryContentMetadata, RepositoryContentMetadata) generate a set of modifications}
    * needed to synchronize the node with the cluster.
    * 
    * @return a {@link RepositoryContentMetadata} with no grandchildren.
    */
   RepositoryContentMetadata createEmptyContentMetadata();
   
   /**
    * Install the result from the latest call to {@link #getCurrentContentMetadata()}
    * as the "official" content metadata. Intended for use during node startup
    * when the node discovers it is the only member of the cluster, and thus
    * that it's "current" content metadata is "official".
    *  
    * @throws IllegalStateException if no "current" content metadata is available, either
    *                               because {@link #getCurrentContentMetadata()} 
    *                               hasn't been called, or because a cluster
    *                               synchronization has been executed to completion
    *                               since that call.
    *
    */
   void installCurrentContentMetadata();
   
   /**
    * Gets a {@link RepositoryItemMetadata} that will describe an item that
    * may be added.
    * 
    * @param vfsPath path relative to one of this repository's root URIs
    * 
    * @return the item metadata
    * 
    * @throws IOException
    */
   RepositoryItemMetadata getItemForAddition(String vfsPath) throws IOException;
   
   /**
    * Generate content metadata that would reflect what the metadata would 
    * look like if an item with path vfsPath were added.
    * 
    * @param vfsPath
    * @param contentIS
    * @return
    */
   RepositoryContentMetadata getContentMetadataForAdd(RepositoryItemMetadata toAdd, InputStream contentIS) throws IOException;

   /**
    * Get a {@link VirtualFile} for the content indicated by <code>item</code>.
    * 
    * @param item metadata describing the content
    * @return the virtual file
    * 
    * @throws IOException
    */
   VirtualFile getVirtualFileForItem(RepositoryItemMetadata item) throws IOException;

   /**
    * Generate content metadata that would reflect what the metadata would 
    * look like if an item with path vfsPath were removed.
    * 
    * @param vfsPath path relative to one of this repository's root URIs
    * 
    * @return the content metadata
    * 
    * @throws IOException
    */
   RepositoryContentMetadata getContentMetadataForRemove(VirtualFile vf) throws IOException;
}
