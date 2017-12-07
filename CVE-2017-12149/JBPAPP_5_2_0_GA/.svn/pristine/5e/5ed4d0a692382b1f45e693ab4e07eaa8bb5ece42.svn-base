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

/**
 * {@link SynchronizationAction} that makes a directory.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class MkDirAction extends AbstractLocalContentChangeAction
{
   private static final Logger log = Logger.getLogger(MkDirAction.class);

   /**
    * Create a new MkDirAction.
    * 
    * @param targetFile the directory to create
    * @param context the overall context of the modification
    * @param modification the modification
    */
   public MkDirAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(targetFile, context, modification);
   }


   // --------------------------------------------------------------  Protected


   @Override
   protected boolean modifyTarget() throws IOException
   {
      File target = getTargetFile();
      
      boolean ok = (target.exists() == false || target.delete());
      if (ok)
      {
         ok = target.mkdirs();
         if (ok)
         {
            target.setLastModified(getRepositoryContentModification().getItem().getTimestamp());
         }
      }
      return ok;
   }
   
   @Override
   protected Logger getLogger()
   {
      return log;
   }

}
