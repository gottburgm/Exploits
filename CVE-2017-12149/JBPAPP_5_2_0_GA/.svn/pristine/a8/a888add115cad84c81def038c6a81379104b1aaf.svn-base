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

import org.jboss.system.server.profileservice.repository.clustered.sync.AbstractContentMetadataMutatorAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;

/**
 * RepositorySynchronizationAction that updates a directory lastModified time.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DirectoryTimestampUpdateAction extends AbstractContentMetadataMutatorAction<FileBasedSynchronizationActionContext>
{
   private final File targetDir;
   private final long rollbackTimestamp;
   
   /**
    * Create a new DirectoryTimestampUpdateAction.
    * 
    * @param targetFile the directory whose timestamp is to be changed
    * @param context the overall context of the modification
    * @param modification the modification
    */
   public DirectoryTimestampUpdateAction(File targetDir,
         FileBasedSynchronizationActionContext context,
         ContentModification modification)
   {
      super(context, modification);
      
      if (targetDir == null)
      {
         throw new IllegalArgumentException("Null targetDir");
      }
      if (targetDir.exists() == false)
      {
         throw new IllegalArgumentException(targetDir + " does not exist");
      }
      if (targetDir.isDirectory() == false)
      {
         throw new IllegalArgumentException(targetDir + " is not a directory");
      }
      this.targetDir = targetDir;
      this.rollbackTimestamp = targetDir.lastModified();
   }

   @Override
   protected void doCancel()
   {
      // no-op
   }

   @Override
   protected void doCommit()
   {
      updateContentMetadata();
   }

   @Override
   protected void doComplete() throws Exception
   {
      // no-op
   }

   @Override
   protected boolean doPrepare()
   {
      targetDir.setLastModified(getRepositoryContentModification().getItem().getTimestamp());
      return true;
   }

   @Override
   protected void doRollbackFromCancelled()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromComplete()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromOpen()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromPrepared()
   {
      targetDir.setLastModified(rollbackTimestamp);
   }

   @Override
   protected void doRollbackFromRollbackOnly()
   {
      targetDir.setLastModified(rollbackTimestamp);
   }

}
