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
import org.jboss.system.server.profileservice.repository.clustered.sync.ByteChunk;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationWriteAction;

/**
 * {@link SynchronizationWriteAction} that writes to a {@link File}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class FileWriteAction extends AbstractFileWriteAction
      implements SynchronizationWriteAction<FileBasedSynchronizationActionContext>
{
   private static final Logger log = Logger.getLogger(FileWriteAction.class);

   /**
    * Create a new FileWriteAction.
    * 
    * @param targetFile the file to write to
    * @param context the overall context of the modification
    * @param modification the modification
    */
   public FileWriteAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(targetFile, context, modification);
   }

   // -----------------------------------  RepositorySynchronizationWriteAction
   
   public void writeBytes(ByteChunk bytes) throws IOException
   {
      super.writeBytes(bytes);
   }

   // --------------------------------------------------------------  Protected
   
   @Override
   protected Logger getLogger()
   {
      return log;
   }

}
