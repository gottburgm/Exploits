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
package org.jboss.system.server.profileservice.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.jboss.util.file.JarUtils;

/**
 * Deployment Utils.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DeploymentUtils
{

   /**
    * Try to unpack an inputStream.
    * This is a fork of {@link JarUtils#unjar}, but does not close the
    * InputStream itself, as this is done by the remote deploymentTarget.
    * 
    * @param in the InputStream
    * @param dest the destination file
    * @throws IOException
    */
   public static void unjar(InputStream in, File dest) throws IOException
   {
      if (!dest.exists())
      {
         dest.mkdirs();
      }
      if (!dest.isDirectory())
      {
         throw new IOException("Destination must be a directory.");
      }
      JarInputStream jin = new JarInputStream(in);
      byte[] buffer = new byte[1024];
      
      ZipEntry entry = jin.getNextEntry();
      while (entry != null)
      {
         String fileName = entry.getName();
         if (fileName.charAt(fileName.length() - 1) == '/')
         {
            fileName = fileName.substring(0, fileName.length() - 1);
         }
         if (fileName.charAt(0) == '/')
         {
            fileName = fileName.substring(1);
         }
         if (File.separatorChar != '/')
         {
            fileName = fileName.replace('/', File.separatorChar);
         }
         File file = new File(dest, fileName);
         if (entry.isDirectory())
         {
            // make sure the directory exists
            file.mkdirs();
            jin.closeEntry();
         } 
         else
         {
            // make sure the directory exists
            File parent = file.getParentFile();
            if (parent != null && !parent.exists())
            {
               parent.mkdirs();
            }
            
            // dump the file
            OutputStream out = new FileOutputStream(file);
            int len = 0;
            while ((len = jin.read(buffer, 0, buffer.length)) != -1)
            {
               out.write(buffer, 0, len);
            }
            out.flush();
            out.close();
            jin.closeEntry();
            file.setLastModified(entry.getTime());
         }
         entry = jin.getNextEntry();
      }
      /* Explicity write out the META-INF/MANIFEST.MF so that any headers such
      as the Class-Path are see for the unpackaged jar
      */
      Manifest mf = jin.getManifest();
      if (mf != null)
      {
         File file = new File(dest, "META-INF/MANIFEST.MF");
         File parent = file.getParentFile();
         if( parent.exists() == false )
         {
            parent.mkdirs();
         }
         OutputStream out = new FileOutputStream(file);
         mf.write(out);
         out.flush();
         out.close();
      }
   }
   
}

