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
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;


/**
 * {@link SynchronizationAction} that modifies the node's metadata to insert
 * a missing {@link RepositoryItemMetadata} that tracks a removed item.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class RemovalMetadataInsertionAction<T extends SynchronizationActionContext> 
      extends NoOpSynchronizationAction<T>
{
   private static final Logger log = Logger.getLogger(RemovalMetadataInsertionAction.class);
   
   private RepositoryItemMetadata replaced;
   
   /**
    * Create a new RemovalMetadataInsertionAction.
    * 
    * @param synchronizationId
    * @param modification
    */
   public RemovalMetadataInsertionAction(T context,
         ContentModification modification)
   {
      super(context, modification);
      
      if (modification.getItem().isRemoved() == false)
      {
         throw new IllegalArgumentException("Item " + modification.getItem() + 
               " is not marked as removed");
      }
   }

   @Override
   protected boolean doPrepare()
   {
      boolean ok = false;
      ContentModification mod = getRepositoryContentModification();
      RepositoryContentMetadata toUpdate = getContext().getInProgressMetadata();
      RepositoryRootMetadata rmd = toUpdate.getRepositoryRootMetadata(mod.getRootName());
      if (rmd != null)
      {
         replaced = rmd.getItemMetadata(mod.getItem().getRelativePathElements());
         // BES 2009/04/20 I see no reason not to do this if replaced != null
//         if (replaced == null)
//         {
            rmd.getContent().add(mod.getItem());
            ok = true;
//         }
            
            if (log.isTraceEnabled())
            {
               log.trace("added removal metadata for " + mod.getItem().getRelativePath());
            }
      }
      return ok;
   }

   @Override
   protected void doRollbackFromComplete()
   {
      if (replaced != null)
      {
         ContentModification mod = getRepositoryContentModification();
         RepositoryContentMetadata toUpdate = getContext().getInProgressMetadata();
         RepositoryRootMetadata rmd = toUpdate.getRepositoryRootMetadata(mod.getRootName());
         if (rmd != null)
         {
            rmd.getContent().add(replaced);
         }         
      }      
   }

   @Override
   protected void doRollbackFromPrepared()
   {
      ContentModification mod = getRepositoryContentModification();
      RepositoryContentMetadata toUpdate = getContext().getInProgressMetadata();
      RepositoryRootMetadata rmd = toUpdate.getRepositoryRootMetadata(mod.getRootName());
      if (rmd != null)
      {
         rmd.getContent().remove(mod.getItem());
      }   
   }

}
