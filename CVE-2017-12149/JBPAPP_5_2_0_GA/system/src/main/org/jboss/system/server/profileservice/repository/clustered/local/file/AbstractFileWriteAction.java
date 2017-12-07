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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jboss.system.server.profileservice.repository.clustered.sync.ByteChunk;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;

/**
 * Base class for actions that write to a {@link File}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractFileWriteAction extends AbstractLocalContentChangeAction
{
   
   private File tempFile;
   private OutputStream stream;

   /**
    * Create a new FileWriteAction.
    * 
    * @param targetFile the file to write to
    * @param context the overall context of the modification
    * @param modification the modification
    */
   public AbstractFileWriteAction(File targetFile, FileBasedSynchronizationActionContext context, 
         ContentModification modification)
   {
      super(targetFile, context, modification);
   }

   // --------------------------------------------------------------  Protected
   
   protected void writeBytes(ByteChunk bytes) throws IOException
   {
      if (bytes == null)
      {
         throw new IllegalArgumentException("Null bytes");
      }
      if (bytes.getByteCount() < 0)
      {
         throw new IllegalArgumentException("Illegal byte count " + bytes.getByteCount());
      }
      OutputStream os = getOutputStream();
      os.write(bytes.getBytes(), 0, bytes.getByteCount());
   }

   @Override
   protected void doComplete() throws Exception
   {
      // Done writing
      safeCloseStream();
      super.doComplete();
   }
   
   @Override
   protected boolean modifyTarget() throws IOException
   {
      // Our temp file replaces targetFile
      FileUtil.localMove(getTempFile(), getTargetFile(), getRepositoryContentModification().getItem().getTimestamp());
      return true;
   }

   protected synchronized void safeCleanup(boolean cleanRollback)
   {
      super.safeCleanup(cleanRollback);
      safeCloseStream();
      if (tempFile != null)
      {
         tempFile.delete();
      }    
   }
   
   // ----------------------------------------------------------------  Private
   
   private synchronized OutputStream getOutputStream() throws IOException
   {
      State s = getState();
      if (s != State.OPEN && s != State.CANCELLED)
      {
         throw new IllegalStateException("Cannot write when state is " + s);
      }
      
      if (stream == null)
      {
         FileOutputStream fos = new FileOutputStream(getTempFile());
         stream = new BufferedOutputStream(fos);
      }
      return stream;
   }
   
   private File getTempFile() throws IOException
   {
      if (tempFile == null)
      {
         tempFile = createTempFile();
      }
      return tempFile;
   }
   
   private synchronized void safeCloseStream()
   {
      if (stream != null)
      {
         synchronized (stream)
         {
            try
            {
               stream.close();
            }
            catch (IOException e)
            {
               ContentModification mod = getRepositoryContentModification();
               getLogger().debug("Caught exception closing stream for " + mod.getRootName() + 
                     " " + mod.getItem().getRelativePath(), e);
            }
            finally
            {
               stream = null;
            }
         }
      }
   }

}
