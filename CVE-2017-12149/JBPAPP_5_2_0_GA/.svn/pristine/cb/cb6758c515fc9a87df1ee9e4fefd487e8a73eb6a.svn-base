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
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationInitiationAction;

/**
 * {@link SynchronizationAction} that does nothing normally, but restores
 * a removed directory during the rollback phase.
 * <p>
 * The intent is this action would execute at the start of a processing of
 * removing a directory tree, followed by other actions to remove the contents
 * of the tree, followed by a {@link RemoveFileAction} to remove the directory.
 * This action does nothing during that sequence. But, during a rollback
 * of the overall synchronization, it restores the removed directory, ensuring
 * the directory is in place when the child removals roll back.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class InitiateRmdirAction 
      extends AbstractLocalContentChangeAction
      implements SynchronizationInitiationAction<FileBasedSynchronizationActionContext>
{
   private static final Logger log = Logger.getLogger(InitiateRmdirAction.class);

   /**
    * Create a new PrepareRmdirAction.
    */
   public InitiateRmdirAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(targetFile, context, modification);
   }


   // --------------------------------------------------------------  Protected


   @Override
   protected boolean modifyTarget() throws IOException
   {
      return true;
   }
   
   @Override
   protected Logger getLogger()
   {
      return log;
   }
   

}
