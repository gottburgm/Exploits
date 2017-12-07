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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;

/**
 * Utility methods related to filesystem operations.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class FileUtil
{
   private static final Logger log = Logger.getLogger(FileUtil.class);
   
   public static void localMove(File source, File destination, long modifiedTime) throws IOException 
   {
      // if we can simply rename the file, all the better
      if(!source.renameTo(destination))
      {
         // otherwise, copy source to destination
         OutputStream out = new FileOutputStream(destination);
         InputStream in = new FileInputStream(source);
         byte buffer[] = new byte[32*1024];
         int bytesRead = 0;
         while(bytesRead > -1) // until we hit end of source file
         {  
            bytesRead = in.read(buffer);
            if(bytesRead > 0) 
            {
               out.write(buffer,0, bytesRead);
            }
         }
         in.close();
         out.close();
      }
      
      destination.setLastModified(modifiedTime);
   }
   
   public static File getFileForItem(URI rootURI, RepositoryItemMetadata item)
   {
      File f = new File(rootURI);
      for (String element : item.getRelativePathElements())
      {
         f = new File(f, element);
      }
      return f;
   }
   
   public static File createTempFile(String tmpDirName, String partitionName) throws IOException
   {
      if (tmpDirName == null)
      {
         return File.createTempFile(partitionName, "tmp");
      }
      else
      {
         return createTempFile(createTempDir(tmpDirName), partitionName);
      }
   }
   
   public static File createTempFile(File tmpDir, String partitionName) throws IOException
   {
      if (tmpDir.exists() == false)
      {
         tmpDir.mkdirs();
      }
      return File.createTempFile(partitionName, "tmp", tmpDir);      
   }
   
   public static File createTempDir(String tmpDirName) throws IOException
   {
      File dir = new File(tmpDirName);
      if (! dir.exists())
      {
         dir.mkdirs();
      }
      else if (! dir.isDirectory())
      {
         throw new IllegalStateException(dir + " already exists and is not a directory");
      }
      return dir;
   }
   
   public static void safeCloseStream(OutputStream os, Object id)
   {      
      try
      {
         os.close();
      }
      catch (IOException e)
      {
         log.trace("Failed to close temporary output stream for " + id, e);
      }
   }

   /**
    * Prevent instantiation. 
    */
   private FileUtil()
   {      
   }

}
