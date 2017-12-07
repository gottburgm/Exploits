/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.system.server.profileservice.repository;

import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;

import org.jboss.logging.Logger;

/**
 * A base AttachmentsSerializer that uses a file system based store.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public abstract class AbstractFileAttachmentsSerializer
{
   private static final Logger log = Logger.getLogger(AbstractFileAttachmentsSerializer.class);
   
   /** The attachment suffix */
   private static final String ATTACHMENT_SUFFIX = ".attachment";
   
   /** The deployment pre-processed attachments store dir */
   private final File attachmentsStoreDir;

   public AbstractFileAttachmentsSerializer(File dir)
   {
      if(dir == null)
         throw new IllegalArgumentException("Null store dir.");
      this.attachmentsStoreDir = dir;
   }
   
   public File getAttachmentsStoreDir()
   {
      return attachmentsStoreDir;
   }
   
   public <T> T loadAttachment(String baseName, Class<T> expected)
      throws Exception
   {
      if( attachmentsStoreDir == null )
         throw new IllegalStateException("attachmentsStoreDir has not been set");

      File attachmentsStore = getAttachmentPath(baseName);
      if( attachmentsStore.exists() == false )
      {
         return null;
      }

      return loadAttachment(attachmentsStore, expected);
   }

   public void saveAttachment(String baseName, Object attachment)
      throws Exception
   {
      if( attachmentsStoreDir == null )
         throw new IllegalStateException("attachmentsStoreDir has not been set");

      File attachmentsStore = getAttachmentPath(baseName);
      File attachmentsParent = attachmentsStore.getParentFile();
      if( attachmentsParent.exists() == false )
      {
         if( attachmentsParent.mkdirs() == false )
            throw new IOException("Failed to create attachmentsParent: "+attachmentsParent.getAbsolutePath());
      }

      if( attachment != null )
      {
         try
         {
            saveAttachment(attachmentsStore, attachment);
         }
         catch(NotSerializableException e)
         {
            // Log what is in the attachments
            StringBuilder tmp = new StringBuilder("Save failed with NSE, attachments contents: ");
            tmp.append(attachment).append(" to: ").append(attachmentsStore);
            log.error(tmp.toString());
            throw e;
         }
      }
   }
   
   protected File getAttachmentPath(String baseName)
   {
      final String vfsPath = baseName + ATTACHMENT_SUFFIX;
      return new File(attachmentsStoreDir, vfsPath);
   }

   protected abstract <T> T loadAttachment(File attachmentsStore, Class<T> expected) throws Exception;
   
   protected abstract void saveAttachment(File attachmentsStore, Object attachment) throws Exception;
}
