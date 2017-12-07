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

import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * VFS based ResourceLoader.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class VFSResourceLoader extends DefaultResourceLoader
{
   public VFSResourceLoader()
   {
   }

   public VFSResourceLoader(ClassLoader classLoader)
   {
      super(classLoader);
   }

   public Resource getResource(String location)
   {
      Assert.notNull(location, "Location must not be null");
      if (location.startsWith(CLASSPATH_URL_PREFIX))
      {
         return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
      }
      else
      {
         try
         {
            // Try to parse the location as a URL...
            URL url = new URL(location);
            VirtualFile file = VFS.getRoot(url);
            return new VFSResource(file);
         }
         catch (Exception ex)
         {
            // No URL -> resolve as resource path.
            return getResourceByPath(location);
         }
      }
   }
}
