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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;

/**
 * Policy to decide how to handle content updates from nodes attempting
 * to join the cluster or from cluster merges. The policy is consulted on
 * the "authoritative" node, i.e. the master node for the service on the
 * cluster.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface SynchronizationPolicy
{
   
   /**
    * Should the item represented by <code>toAdd</code> that is available
    * on a newly <i>joining</i> node be accepted for use around the cluster 
    * when the cluster's records show no record of an item with the same path? 
    * Such a case potentially could mean newly joining node was unaware of an 
    * earlier removal that occurred while it was offline and that the cluster
    * has also {@link #purgeRemovedItems(RepositoryContentMetadata) purged
    * from its records}.
    * 
    * @param toAdd  the item to add
    * @param joinersPrevious information, if available, on the timestamp of the
    *                        item that was present on the joining node when
    *                        it stopped. May be <code>null</code>, indicating
    *                        the joining node was unaware of the item when stopped.
    * @return <code>true</code> if the addition should be accepted
    */
   boolean acceptJoinAddition(RepositoryItemMetadata toAdd, 
                              RepositoryItemMetadata joinersPrevious);
   
   /**
    * Should the item represented by <code>reincarnation</code> that is available
    * on a newly <i>joining</i> node be accepted for use around the cluster when 
    * the cluster's records show an item with the same path was previously 
    * removed? Such a case potentially could mean the newly joining node 
    * was unaware of a removal that occurred while it was offline.
    * 
    * @param reincarnation  the new version of the item
    * @param current the cluster's current version of the item, showing when
    *        it was removed and by whom. The "when" should reflect this
    *        node's time of removal, not the time on the node that originated
    *        the removal.
    * @return <code>true</code> if the reincarnation should be accepted
    */
   boolean acceptJoinReincarnation(RepositoryItemMetadata reincarnation, 
                               RepositoryItemMetadata current);
   
   /**
    * Should the item represented by <code>toRemove</code>that is unavailable
    * on a merging set of nodes be removed from around the cluster when 
    * the cluster's records show an item with the same path?  Such a case 
    * potentially could mean the newly joining node was unaware of a new
    * deployment of the item that occurred while it was offline.
    * 
    * @param current the cluster's current version of the item
    * @param joinersItem the joining node's view of item to remove. May be null, 
    *                 indicating the sender is unaware of the item. If not null,
    *                 the timestamp of this item should reflect when the item 
    *                 was removed, if known. If the time the item was removed is 
    *                 not known, the timestamp should reflect the last known 
    *                 timestamp of the item that was removed.
    * @return <code>true</code> if the removal should be accepted
    */
   boolean acceptJoinRemoval(RepositoryItemMetadata current, 
                             RepositoryItemMetadata joinersItem);
   /**
    * Should the item represented by <code>update</code> that is available
    * on a newly <i>joining</i> node be accepted for use around the cluster when the 
    * cluster's records show an item with the same path with a different version? 
    * Such a case potentially could mean the newly joining node was unaware of 
    * changes that occurred while it was offline. 
    * 
    * @param update  the new version of the item
    * @param current the cluster's current version of the item
    * @return <code>true</code> if the update should be accepted
    */
   boolean acceptJoinUpdate(RepositoryItemMetadata update, 
                            RepositoryItemMetadata current);
   
   /**
    * Should the item represented by <code>toAdd</code> that is available
    * on a merging set of nodes be accepted for use around the cluster when the 
    * cluster's records show no record of an item with the same path? Such a 
    * case potentially could mean the merging nodes were unaware of an earlier 
    * removal that occurred while the cluster was split and that the cluster
    * has also {@link #purgeRemovedItems(RepositoryContentMetadata) purged
    * from its records}.
    * 
    * @param toAdd  the item to add
    * @return <code>true</code> if the addition should be accepted
    */
   boolean acceptMergeAddition(RepositoryItemMetadata toAdd);
   
   
   /**
    * Should the item represented by <code>reincarnation</code> that is available
    * on a merging set of nodes be accepted for use around the cluster when the 
    * cluster's records show an item with the same path was previously removed? 
    * Such a case potentially could mean the merging nodes were unaware of a
    * removal that occurred while the cluster was split.
    * 
    * @param reincarnation  the new version of the item
    * @param current the cluster's current version of the item, showing when
    *        it was removed and by whom
    * @return <code>true</code> if the reincarnation should be accepted
    */
   boolean acceptMergeReincarnation(RepositoryItemMetadata reincarnation, 
                               RepositoryItemMetadata current);
   
   /**
    * Should the item represented by <code>toRemove</code> that is unavailable
    * on a newly <i>joining</i> node be removed from around the cluster when 
    * the cluster's records show an item with the same path?  Such a case 
    * potentially could mean the newly joining node was unaware of a new
    * deployment of the item that occurred  while the cluster was split.
    * 
    * @param current the cluster's current version of the item
    * @param mergersView the merging node's view of item to remove. May be null, 
    *                 indicating the sender is unaware of the item. If not null,
    *                 the timestamp of this item should reflect when the item 
    *                 was removed, if known. If the time the item was removed is 
    *                 not known, the timestamp should reflect the last known 
    *                 timestamp of the item that was removed.
    * @return <code>true</code> if the removal should be accepted
    */
   boolean acceptMergeRemoval(RepositoryItemMetadata current, 
                              RepositoryItemMetadata mergersView);
   

   /**
    * Should the item represented by <code>update</code> that is available
    * on a merging set of nodes be accepted for use around the cluster when the 
    * cluster's records show an item with the same path with a different version? 
    * Such a case potentially could mean the merging nodes were unaware of 
    * changes that occurred while the cluster was split.
    * 
    * @param update  the new version of the item
    * @param current the cluster's current version of the item
    * @return <code>true</code> if the update should be accepted
    */
   boolean acceptMergeUpdate(RepositoryItemMetadata update, 
                             RepositoryItemMetadata current);
   
   /**
    * Request that the policy remove any {@link RepositoryItemMetadata} objects
    * that are listed as {@link RepositoryItemMetadata#isRemoved() removed}
    * if the policy no longer wishes to consider them in its decision making.
    * Used to prevent perpetual growth in the size of the RepositoryContentMetadata
    * by eventually purging records of removed items.
    * 
    * @param content the content. Cannot be <code>null</code>.
    * 
    * @return <code>true</code> if any items were purged, <code>false</code>
    *         if not
    */
   boolean purgeRemovedItems(RepositoryContentMetadata content);
}
