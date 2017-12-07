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
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification.Type;

/**
 * Generates {@link ContentModification} objects from a comparison
 * of a current snapshot of a remote node's repository content to the official 
 * snapshot available on this node.  Intended for use when a new node joins the 
 * cluster or a split of the cluster heals.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class RemoteContentModificationGenerator extends AbstractContentModificationGenerator
{
   private final SynchronizationPolicy policy;
   private final RepositoryContentMetadata baseRemoteContent;
   private final boolean merge;

   /**
    * Create a new RemoteRepositoryContentModificationGenerator to handle
    * a cluster merge.
    * 
    * @param policy policy that decides whether to accept or reject changes
    *               from the remote repository
    */
   public RemoteContentModificationGenerator(SynchronizationPolicy policy)
   {
      if (policy == null)
      {
         throw new IllegalArgumentException("Null policy");
      }

      this.policy = policy;
      this.baseRemoteContent = null;
      this.merge = true;
   }

   /**
    * Create a new RemoteRepositoryContentModificationGenerator to handle a
    * cluster node join.
    * 
    * @param policy policy that decides whether to accept or reject changes
    *               from the remote repository
    * @param baseRemoteContent remote node's view of its content as of the
    *                          last time it was part of the cluster, or
    *                          <code>null</code> if the node was never part
    *                          of the cluster.
    */
   public RemoteContentModificationGenerator(SynchronizationPolicy policy,
         RepositoryContentMetadata baseRemoteContent)
   {
      if (policy == null)
      {
         throw new IllegalArgumentException("Null policy");
      }
      if (baseRemoteContent == null)
      {
         throw new IllegalArgumentException("Null baseRemoteContent");
      }

      this.policy = policy;
      this.baseRemoteContent = baseRemoteContent;
      this.merge = false;
   }
   
   // ----------------------------------------------------- Protected Overrides

   @Override
   protected void handleAddition(String rootName, 
         RepositoryItemMetadata item, 
         GeneratedModifications mods)
   {
      handleAddition(rootName, item, null, mods);
   }
   
   @Override
   protected void handleMissing(String rootName, RepositoryItemMetadata item, 
         GeneratedModifications mods)
   {
      if (item.isRemoved() == false)
      {
         handleRemoval(rootName, item, mods);
      }
      else 
      {
         // Remote node doesn't have file, just needs to add a missing RepositoryItemMetadata

         // This is a removal, so it can't be a child of an earlier attempted add.
         // So, drain any remaining prerejected adds
         drainPrerejectedAdds(mods);
         // A removal negates any preapprovedAdd as well
         mods.setPreapprovedAddParent(null);
         
         // 
         RepositoryItemMetadata prerejectedRemove = mods.getPrerejectedRemoveParent();
         if (prerejectedRemove != null && item.isChildOf(prerejectedRemove) == false)
         {
            mods.setPrerejectedRemoveParent(null);
         }
         
         // Don't lose track of pre-approval stack if there is one
         ContentModification preapprovedRemove = mods.peekPreapprovedRemoveParent();
         while (preapprovedRemove != null)
         {
            if (item.isChildOf(preapprovedRemove.getItem()))
            {
               // we're at the right level
               break;
            }
            else
            {
               // We're done with children of preapproved parent so add the cached
               // modification to the overall list. This will cause it to
               // get executed *after* its children 
               // (i.e. remove parent after removing children)
               mods.addModification(mods.popPreapprovedRemoveParent());
               // Start checking grandparent
               preapprovedRemove = mods.peekPreapprovedRemoveParent();
            }
         }
         
         ContentModification mod = new ContentModification(Type.REMOVAL_METADATA_FROM_CLUSTER, rootName, item);
         if (item.isDirectory())
         {
            mods.pushPreapprovedRemoveParent(mod);
         }
         else
         {
            mods.addModification(mod);
         }
      }
   }
   
   @Override
   protected void handleAddition(String rootName, 
         RepositoryItemMetadata item, RepositoryItemMetadata removedVersion, 
         GeneratedModifications mods)
   {
      // This is an add, so it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      mods.setPrerejectedRemoveParent(null);
      
      Boolean allow = null;

      // See if this addition is preapproved as part of adding a new tree
      RepositoryItemMetadata  preapprovedAdd = mods.getPreapprovedAddParent();
      if (preapprovedAdd != null)
      {
         if (item.isChildOf(preapprovedAdd))
         {
            // just keep the same one
            allow = Boolean.TRUE;
         }
         else
         {
            // Clean up
            mods.setPreapprovedAddParent(null);
         }
      }
      else
      {
         // Addition wasn't preapproved. See if it was prerejected as part
         // of rejecting adding a new tree
         
         ContentModification prerejectedParentMod = mods.peekPrerejectedAddParent();
         while (prerejectedParentMod != null)
         {
            if (item.isChildOf(prerejectedParentMod.getItem()))
            {
               // rejected
               allow = Boolean.FALSE;
               break;
            }
            else
            {
               // We're done with children of prerejected parent so add the cached
               // modification to the overall list. This will cause it to
               // get executed *after* its children 
               // (i.e. remove parent after removing children)
               mods.addModification(mods.popPrerejectedAddParent());
               // Start checking grandparent
               prerejectedParentMod = mods.peekPrerejectedAddParent();
            }
         }
      }
      
      if (allow == null)
      {
         // Check with our policy
         allow = Boolean.valueOf(isAdditionApproved(rootName, item, removedVersion));
      }
      
      if (allow.booleanValue())
      {         
         Type type = item.isDirectory() ? Type.MKDIR_TO_CLUSTER : Type.PUSH_TO_CLUSTER;
         mods.addModification(new ContentModification(type, rootName, item));
         
         if (mods.getPreapprovedAddParent() == null && item.isDirectory())
         {
            mods.setPreapprovedAddParent(item);
         }         
      }
      else
      {         
         // Addition not allowed; remote node must discard
         
         // If available use the removedVersion in ContentModifications to
         // help keep it around in metadata
         RepositoryItemMetadata modItem = removedVersion == null ? item : removedVersion;
         ContentModification mod = new ContentModification(Type.REMOVE_FROM_CLUSTER, rootName, modItem);
         if (item.isDirectory())
         {
            // Tell node to prepare for the removal
            mods.addModification(new ContentModification(Type.PREPARE_RMDIR_FROM_CLUSTER, rootName, modItem));
            // Push it on the stack so we execute it after dealing with
            // all the children we will now reject as well
            mods.pushPrerejectedAddParent(mod);
         }
         else
         {
            mods.addModification(mod);
         }
         // housekeeping
         mods.setPreapprovedAddParent(null);
      }
   }

   @Override
   protected void handleChangeFromDirectory(String rootName, 
         RepositoryItemMetadata base,
         RepositoryItemMetadata modified,
         GeneratedModifications mods)
   {
      // This is a modification, so it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      mods.setPrerejectedRemoveParent(null);
      // Also can't be child of an earlier add, so drain any remaining prerejected adds
      drainPrerejectedAdds(mods);
      mods.setPreapprovedAddParent(null);
      
      boolean allow = checkAllowUpdate(base, modified);
      if (allow)
      {
         // We're going to need to remove all the content that was
         // under "base" from all the nodes in the cluster before we
         // can push the replacement file. So, we put the PUSH_TO_CLUSTER
         // on the stack to be inserted once all the child removals
         // are done.
         ContentModification mod = new ContentModification(Type.PUSH_TO_CLUSTER, rootName, modified);
         mods.pushPreapprovedRemoveParent(mod);
      }
      else
      {
         // We're going to need to also reject the removal of all the content 
         // that is under "base".
         mods.setPrerejectedRemoveParent(base);         
         mods.addModification(new ContentModification(Type.MKDIR_FROM_CLUSTER, rootName, base));
      }
   }

   @Override
   protected void handleChangeToDirectory(String rootName, 
         RepositoryItemMetadata base,
         RepositoryItemMetadata modified,
         GeneratedModifications mods)
   {
      // This is a modification, so it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      mods.setPrerejectedRemoveParent(null);
      // Also can't be child of an earlier add, so drain any remaining prerejected adds
      drainPrerejectedAdds(mods);
      mods.setPreapprovedAddParent(null);
      
      boolean allow = checkAllowUpdate(base, modified);
      if (allow)
      {
         // We're going to need to add all the content that is
         // under "modified".
         mods.setPreapprovedAddParent(modified);         
         mods.addModification(new ContentModification(Type.MKDIR_TO_CLUSTER, rootName, modified));       
      }
      else
      {
         // We're going to need to also reject the addition of all the content 
         // that is under "modified".
         ContentModification mod = new ContentModification(Type.PULL_FROM_CLUSTER, rootName, base);
         mods.pushPrerejectedAddParent(mod);
      }
   }
   
   @Override
   protected void handleRemoval(String rootName, RepositoryItemMetadata item, GeneratedModifications mods)
   {
      // This is a removal, so it can't be a child of an earlier attempted add.
      // So, drain any remaining prerejected adds
      drainPrerejectedAdds(mods);
      mods.setPreapprovedAddParent(null);
      
      Boolean allow = null;

      // See if this removal is prerejected as part of removing a higher level tree
      RepositoryItemMetadata prerejected = mods.getPrerejectedRemoveParent();
      if (prerejected != null)
      {
         if (item.isChildOf(prerejected))
         {
            allow = Boolean.FALSE;
         }
         else
         {
            // Clean up
            mods.setPrerejectedRemoveParent(null);
         }
      }
      else
      {
         // Removal wasn't prerejected. See if it was preapproved as part
         // of approving removing a higher level tree
         ContentModification preapprovedRemove = mods.peekPreapprovedRemoveParent();
         while (preapprovedRemove != null)
         {
            if (item.isChildOf(preapprovedRemove.getItem()))
            {
               // approved
               allow = Boolean.TRUE;
               break;
            }
            else
            {
               // We're done with children of preapproved parent so add the cached
               // modification to the overall list. This will cause it to
               // get executed *after* its children 
               // (i.e. remove parent after removing children)
               mods.addModification(mods.popPreapprovedRemoveParent());
               // Start checking grandparent
               preapprovedRemove = mods.peekPreapprovedRemoveParent();
            }
         }
      }
      
      if (allow == null)
      {
         // Check with our policy
         
         if (merge)
         {
            allow = Boolean.valueOf(policy.acceptMergeRemoval(item, null));
         }
         else
         {            
            // See if the base version of the remote node was aware of the
            // item being removed
            RepositoryItemMetadata baseRemoteItem = getBaseRemoteItem(rootName, item);            
            allow = Boolean.valueOf(policy.acceptJoinRemoval(item, baseRemoteItem));         
         }
      }
      
      if (allow)
      {
         ContentModification mod = new ContentModification(Type.REMOVE_TO_CLUSTER, rootName, item);
         
         if (item.isDirectory())
         {
            // Tell cluster to prepare for the removal
            mods.addModification(new ContentModification(Type.PREPARE_RMDIR_TO_CLUSTER, rootName, item));
            // Push the actual removal on the stack to execute when
            // children are done
            mods.pushPreapprovedRemoveParent(mod);
         }
         else
         {
            mods.addModification(mod);
         }
      }
      else
      {         
         // Removal is rejected; tell remote node to pull ours
         Type type = item.isDirectory() ? Type.MKDIR_FROM_CLUSTER : Type.PULL_FROM_CLUSTER;
         mods.addModification(new ContentModification(type, rootName, item)); 
         
         if (mods.getPrerejectedRemoveParent() == null && item.isDirectory())
         {
            mods.setPrerejectedRemoveParent(item); 
         }
      }
      
   }

   @Override
   protected void handleSimpleModification(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods)
   {
      // If this is a modification, it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      mods.setPrerejectedRemoveParent(null);
      // Also can't be child of an earlier add, so drain any remaining prerejected adds
      drainPrerejectedAdds(mods);
      mods.setPreapprovedAddParent(null);
      
      if (checkAllowUpdate(base, modified))
      {
         mods.addModification(new ContentModification(Type.PUSH_TO_CLUSTER, rootName, modified));
      }
      else
      {
         mods.addModification(new ContentModification(Type.PULL_FROM_CLUSTER, rootName, base));
      }
   }

   @Override
   protected void handleDirectoryTimestampModification(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods)
   {
      // If this is a modification, it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      mods.setPrerejectedRemoveParent(null);
      // Also can't be child of an earlier add, so drain any remaining prerejected adds
      drainPrerejectedAdds(mods);
      mods.setPreapprovedAddParent(null);
      
      mods.addModification(new ContentModification(Type.DIR_TIMESTAMP_MISMATCH, rootName, base));
   }
   
   // ----------------------------------------------------------------  Private

   private boolean checkAllowUpdate(RepositoryItemMetadata current, RepositoryItemMetadata update)
   {
      boolean allow = false;
      if (merge)
      {
         allow = policy.acceptMergeUpdate(update, current);
      }
      else
      {
         allow = policy.acceptJoinUpdate(update, current);
      }
      return allow;
   }
   
   private RepositoryItemMetadata getBaseRemoteItem(String rootName, RepositoryItemMetadata item)
   {
      RepositoryItemMetadata existingItem = null;
      if (baseRemoteContent != null)
      {
         RepositoryRootMetadata existingRoot = baseRemoteContent.getRepositoryRootMetadata(rootName);
         if (existingRoot != null)
         {
            existingItem = existingRoot.getItemMetadata(item.getRelativePathElements());
         }
      }
      return existingItem;
   }

   private boolean isAdditionApproved(String rootName, RepositoryItemMetadata item,
         RepositoryItemMetadata removedVersion)
   {
      boolean allow;
      
      if (removedVersion == null)
      {
         if (merge)
         {
            allow = policy.acceptMergeAddition(item);
         }
         else
         {
            // See if the base version of the remote node was aware of the
            // item being added
            RepositoryItemMetadata baseRemoteItem = getBaseRemoteItem(rootName, item);
            allow = Boolean.valueOf(policy.acceptJoinAddition(item, baseRemoteItem));
         }         
      }
      else
      {
         if (merge)
         {
            allow = policy.acceptMergeReincarnation(item, removedVersion);
         }
         else
         {
            allow = policy.acceptJoinReincarnation(item, removedVersion);
         }         
      }

      return allow;
   }

   private void drainPrerejectedAdds(GeneratedModifications mods)
   {
      ContentModification prerejectedAdd;
      while ((prerejectedAdd = mods.popPrerejectedAddParent()) != null)
      {
         mods.addModification(prerejectedAdd);
      }
   }

}
