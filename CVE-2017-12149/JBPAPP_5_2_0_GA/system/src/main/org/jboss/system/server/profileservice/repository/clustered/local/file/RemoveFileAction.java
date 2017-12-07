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
import java.io.IOException;

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification.Type;

/**
 * {@link SynchronizationAction} that removes a {@link File}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class RemoveFileAction extends AbstractLocalContentChangeAction
{
   private static final Logger log = Logger.getLogger(RemoveFileAction.class);

   /**
    * Create a new RemoveFileAction.
    */
   public RemoveFileAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(targetFile, context, modification);
   }


   // --------------------------------------------------------------  Protected


   @Override
   protected boolean modifyTarget() throws IOException
   {
      File target = getTargetFile();
      return target.exists() == false || target.delete();
   }
   
   @Override
   protected Logger getLogger()
   {
      return log;
   }


   @Override
   protected void doRollbackFromPrepared()
   {
      if (getRepositoryContentModification().getItem().isDirectory() == false)
      {
         super.doRollbackFromPrepared();
      }
      // else we assume there was a PrepareRmdir action that handled
      // the rollback of the directory removal
   }


   @Override
   protected void doRollbackFromRollbackOnly()
   {
      if (getRepositoryContentModification().getItem().isDirectory() == false)
      {
         super.doRollbackFromRollbackOnly();
      }
      // else we assume there was a PrepareRmdir action that handled
      // the rollback of the directory removal
   }


   @Override
   protected void updateContentMetadata()
   {
      ContentModification mod = getRepositoryContentModification();
      RepositoryItemMetadata modItem = mod.getItem();
      if (modItem.isRemoved())
      {
         // Just record it
         super.updateContentMetadata();
      }
      else if (mod.getType() == Type.REMOVE_FROM_CLUSTER)
      {
         // An addition has been rejected. We don't record the item as
         // removed in the metadata, we just remove it.
         RepositoryContentMetadata contentMetadata = getContext().getInProgressMetadata();
         RepositoryRootMetadata rmd = contentMetadata.getRepositoryRootMetadata(mod.getRootName());
         rmd.getContent().remove(modItem);         
      }
      else
      {
         // Add a record of the item, marked as removed
         RepositoryItemMetadata markedRemoved = getMarkedRemovedItem(mod);
         RepositoryContentMetadata contentMetadata = getContext().getInProgressMetadata();
         RepositoryRootMetadata rmd = contentMetadata.getRepositoryRootMetadata(mod.getRootName());
         rmd.getContent().add(markedRemoved);
      }
   }
   
   

}
