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

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification.Type;


/**
 * Generates {@link ContentModification} object from a comparison
 * of a current snapshot of local repository content to a official snapshot.
 * This generator will only generate modifications that push content to the
 * cluster or that tell the cluster to remove content; no modifications
 * pulling content from the cluster or removing local content will be
 * generated.
 * <p>This generator should only be used when the node has been fully
 * synchronized with the cluster; it assumes any changes are acceptable
 * to the cluster.</p>
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class LocalContentModificationGenerator extends AbstractContentModificationGenerator
{
   private static final Logger log = Logger.getLogger(LocalContentModificationGenerator.class);
   
   @Override
   protected void handleAddition(String rootName, 
         RepositoryItemMetadata item, GeneratedModifications mods)
   {      
      // If this is an addition, it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      Type type = item.isDirectory() ? Type.MKDIR_TO_CLUSTER : Type.PUSH_TO_CLUSTER;
      mods.addModification(new ContentModification(type, rootName, item));
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + type + " modification for " + item.getRelativePath());
      }
   }

//   @Override
//   protected void handleMatch(String rootName, 
//         RepositoryItemMetadata base, RepositoryItemMetadata modified, GeneratedModifications mods)
//   {      
//      drainPreapprovedRemovals(mods);
//      if (base.equals(modified) == false)
//      {
//         if (base.isDirectory() != modified.isDirectory())
//         {
//            // Swapped exploded for zipped or vice versa
//            if (!base.isRemoved())
//            {
//               mods.addModification(new ContentModification(Type.REMOVE_TO_CLUSTER, rootName, base));
//            }
//         }
//         mods.addModification(new ContentModification(Type.PUSH_TO_CLUSTER, rootName, modified));
//      }
//   }

   @Override
   protected void handleMissing(String rootName, 
         RepositoryItemMetadata item, GeneratedModifications mods)
   {
      if (!item.isRemoved())
      {
         handleRemoval(rootName, item, mods);
      }
      // else it's the brain-dead case where we have metadata recording the
      // removal but no file -- 'cause it's removed! In which case there's no
      // need to do anything.
   }

   @Override
   protected void handleChangeToDirectory(String rootName, RepositoryItemMetadata base, RepositoryItemMetadata modified,
         GeneratedModifications mods)
   {
      // This is a modification, so it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      
      mods.addModification(new ContentModification(Type.MKDIR_TO_CLUSTER, rootName, modified));  
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + Type.MKDIR_TO_CLUSTER + " modification for " + modified.getRelativePath());
      }    
   }

   @Override
   protected void handleAddition(String rootName, RepositoryItemMetadata modified, RepositoryItemMetadata base,
      GeneratedModifications mods)
   {
      handleAddition(rootName, modified, mods);      
   }

   @Override
   protected void handleChangeFromDirectory(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods)
   {
      // This is a modification, so it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      
      // We're going to need to remove all the content that was
      // under "base" from all the nodes in the cluster before we
      // can push the replacement file. So, we put the PUSH_TO_CLUSTER
      // on the stack to be inserted once all the child removals
      // are done.
      ContentModification mod = new ContentModification(Type.PUSH_TO_CLUSTER, rootName, modified);
      mods.pushPreapprovedRemoveParent(mod); 
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + Type.PUSH_TO_CLUSTER + " modification for " + modified.getRelativePath());
      }    
      
   }

   @Override
   protected void handleDirectoryTimestampModification(String rootName, RepositoryItemMetadata base,
         RepositoryItemMetadata modified, GeneratedModifications mods)
   {
      // If this is a modification, it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      
      mods.addModification(new ContentModification(Type.DIR_TIMESTAMP_MISMATCH, rootName, base));  
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + Type.DIR_TIMESTAMP_MISMATCH + " modification for " + base.getRelativePath());
      }         
   }

   @Override
   protected void handleSimpleModification(String rootName, RepositoryItemMetadata base, RepositoryItemMetadata modified,
         GeneratedModifications mods)
   {
      // If this is a modification, it can't be the child of an earlier removal.
      // So, drain any remaining preapproved removals
      drainPreapprovedRemovals(mods);
      
      mods.addModification(new ContentModification(Type.PUSH_TO_CLUSTER, rootName, modified));  
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + Type.PUSH_TO_CLUSTER + " modification for " + modified.getRelativePath());
      }         
   }

   @Override
   protected void handleRemoval(String rootName, RepositoryItemMetadata item, GeneratedModifications mods)
   {
      ContentModification removedParent = mods.peekPreapprovedRemoveParent();
      while (removedParent != null && item.isChildOf(removedParent.getItem()) == false)
      {
         mods.addModification(mods.popPreapprovedRemoveParent());
         removedParent = mods.peekPreapprovedRemoveParent();
      }
      ContentModification removal = new ContentModification(Type.REMOVE_TO_CLUSTER, rootName, item);
      if (item.isDirectory())
      {
         // Tell cluster to prepare for the removal
         mods.addModification(new ContentModification(Type.PREPARE_RMDIR_TO_CLUSTER, rootName, item));
         // Push the actual removal on the stack to execute when
         // children are done
         mods.pushPreapprovedRemoveParent(removal);  
         
         if (log.isTraceEnabled())
         {
            log.trace("created " + Type.PREPARE_RMDIR_TO_CLUSTER + " modification for " + item.getRelativePath());
         }         
      }
      else
      {
         mods.addModification(removal);
      }  
      
      if (log.isTraceEnabled())
      {
         log.trace("created " + Type.REMOVE_TO_CLUSTER + " modification for " + item.getRelativePath());
      }         
   }

}
