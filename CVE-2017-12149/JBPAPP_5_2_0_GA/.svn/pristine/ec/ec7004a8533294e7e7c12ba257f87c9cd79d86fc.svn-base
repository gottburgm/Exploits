/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.spring.io;

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VFSUtils;
import org.springframework.core.io.Resource;

/**
 * VFS based Resource.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class VFSResource implements Resource
{
   private VirtualFile file;

   public VFSResource(VirtualFile file)
   {
      if (file == null)
         throw new IllegalArgumentException("Null file");
      this.file = file;
   }

   public boolean exists()
   {
      try
      {
         return file.exists();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean isOpen()
   {
      return false;
   }

   public boolean isReadable()
   {
      try
      {
         return file.getSize() > 0;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public long lastModified()
   {
      try
      {
         return file.getLastModified();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public URL getURL() throws IOException
   {
      try
      {
         return file.toURL();
      }
      catch (URISyntaxException e)
      {
         IOException ioe = new IOException(e.getMessage());
         ioe.initCause(e);
         throw ioe;
      }
   }

   public URI getURI() throws IOException
   {
      try
      {
         return file.toURI();
      }
      catch (URISyntaxException e)
      {
         IOException ioe = new IOException(e.getMessage());
         ioe.initCause(e);
         throw ioe;
      }
   }

   public File getFile() throws IOException
   {
      try
      {
         return new File(VFSUtils.getCompatibleURI(file));
      }
      catch (IOException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         IOException ioe = new IOException(e.getMessage());
         ioe.initCause(e);
         throw ioe;
      }
   }

   @SuppressWarnings("deprecation")
   public Resource createRelative(String relativePath) throws IOException
   {
      return new VFSResource(file.findChild(relativePath));
   }

   public String getFilename()
   {
      return file.getName();
   }

   public String getDescription()
   {
      return file.toString();
   }

   public InputStream getInputStream() throws IOException
   {
      return file.openStream();
   }

   public String toString()
   {
      return getDescription();
   }
}
