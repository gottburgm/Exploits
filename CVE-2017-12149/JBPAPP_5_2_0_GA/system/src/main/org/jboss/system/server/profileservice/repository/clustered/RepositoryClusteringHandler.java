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

package org.jboss.system.server.profileservice.repository.clustered;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManager;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.InconsistentRepositoryStructureException;
import org.jboss.virtual.VirtualFile;

/**
 * Handles intra-cluster operations for a clustered DeploymentRepository.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface RepositoryClusteringHandler
{
   /** Gets the key identifying the Profile this handler is meant for */
   ProfileKey getProfileKey();
   /** Gets the name of the cluster partition this hander is associated with */
   String getPartitionName();
   /** Gets the unique id for this node within the cluster partition */
   String getLocalNodeName();
   
   /** 
    * Gets whether this handler allows a node to push content changes to the
    * cluster. If <code>true</code>, what changes will be accepted are an
    * implementation detail.
    * 
    * @return <code>true</code> if content changes are <strong>not</strong> allowed; 
    *         <code>false</code> otherwise
    */
   boolean isImmutable();
   
   /** 
    * Sets whether this handler allows a node to push content changes to the
    * cluster. If <code>true</code>, what changes will be accepted are an
    * implementation detail.
    * 
    * @param immutable <code>true</code> if content changes are <strong>not</strong> allowed; 
    *         <code>false</code> otherwise
    */
   void setImmutable(boolean immutable);
   
   /**
    * Handler should prepare itself for operation.
    * 
    * @param localContentManager object that handles repository content locally
    * 
    * @throws Exception
    */
   void initialize(LocalContentManager<?> localContentManager) throws Exception;
   
   /**
    * Notification that handler can perform clean up work as it will not 
    * be asked to coordinate further with the cluster.
    * 
    * @throws Exception
    */
   void shutdown() throws Exception;
   
   /**
    * Acquire a cluster-wide lock for this repository. Must not be invoked
    * if a {@link #lockLocally() local lock} is already held.
    * 
    * @return <code>true</code> if the lock was acquired, <code>false</code>
    *         if not
    */
   boolean lockGlobally();
   
   /**
    * Acquire a local-only lock for this repository. Will not be granted if
    * a node already owns a {@link #lockGlobally() cluster-wide lock}.
    * 
    * @return <code>true</code> if the lock was acquired, <code>false</code>
    *         if not
    */
   boolean lockLocally();
   
   /**
    * Release a cluster-wide lock obtained in {@link #lockGlobally()}.
    */
   void unlockGlobally();
   
   /**
    * Release a local lock obtained in {@link #lockLocally()}.
    */
   void unlockLocally();
   
   /**
    * Synchronize this node's repository content with the rest of the cluster.
    * This would typically involve a scan of repository content with any
    * detected changes being propagated to the cluster.
    * 
    * @param pullFromCluster <code>true</code> if the synchronization should
    *                        include pulling in changes from the cluster, e.g.
    *                        as part of the startup of a node or during a merge
    *                        following the healing of a cluster split;
    *                        <code>false</code> if the synchronization should
    *                        only consist of pushing local modifications to
    *                        the cluster, e.g. as part of a hot deployment scan
    *                        
    * @return metadata describing the local repository content after the
    *         synchronization
    *         
    * @throws InconsistentRepositoryStructureException 
    * @throws IOException
    */
   RepositoryContentMetadata synchronizeContent(boolean pullFromCluster) 
         throws InconsistentRepositoryStructureException, IOException;
   
   /**
    * Read the content from the given input stream and add it to the repository
    * across the cluster. This is used as part of processing of programmatic
    * changes to the repository content, as opposed to asking the
    * handler to deal with changes made manually.
    * 
    * @param vfsPath path describing where the content should be located in
    *                the repository
    * 
    * @param contentIS input stream from which the content can be read
    * 
    * @return metadata describing the new content
    * 
    * @throws IOException
    */
   RepositoryItemMetadata addDeploymentContent(String vfsPath, InputStream contentIS) throws IOException;
   
   /**
    * Remove content from the repository across the cluster. This is used as part of processing of programmatic
    * changes to the repository content, as opposed to asking the
    * handler to deal with changes made manually.
    * 
    * @param vf VirtualFile that contains the content
    * 
    * @throws Exception
    */
   void removeDeploymentContent(VirtualFile vf) throws Exception;
}
