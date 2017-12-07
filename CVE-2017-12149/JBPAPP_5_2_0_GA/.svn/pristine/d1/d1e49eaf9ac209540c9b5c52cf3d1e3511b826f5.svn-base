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
package org.jboss.deployment;

import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Exclude virtual file by file name and path.
 *
 * @author ales.justin@jboss.org
 */
public class FileNameVirtualFileFilter implements VirtualFileFilter
{
   private Logger log = Logger.getLogger(getClass());
   private Map<String, Set<String>> excludes;

   public FileNameVirtualFileFilter(Map<String, Set<String>> excludes)
   {
      if (excludes == null || excludes.isEmpty())
         throw new IllegalArgumentException("Null or empty excludes.");

      this.excludes = excludes;
   }

   /**
    * Do we accept file.
    *
    * If pathName contains any of the keys,
    *   * if the value is null - then do exclude
    *   * if value is not null - only exclude if it value contains simple name
    *
    * @param file the virtual file
    * @return false if file is excluded by excludes map, true other wise
    */
   public boolean accepts(VirtualFile file)
   {
      String pathName = getPathName(file);
      for (Map.Entry<String, Set<String>> entry : excludes.entrySet())
      {
         String key = entry.getKey();
         if (pathName.contains(key))
         {
            String simpleName = file.getName();
            Set<String> value = entry.getValue();
            if (value == null || value.contains(simpleName))
            {
               if (log.isTraceEnabled())
                  log.trace("Excluding " + pathName);
               
               return false;
            }
         }
      }
      return true;
   }
   
   /**
    * Get the path name for the VirtualFile.
    * 
    * @param file the virtual file
    * @return the path name
    */
   private String getPathName(VirtualFile file)
   {
      try
      {
         // prefer the URI, as the pathName might
         // return an empty string for temp virtual files
         return file.toURI().toString();
      }
      catch(Exception e)
      {
         return file.getPathName();
      }
   }
}