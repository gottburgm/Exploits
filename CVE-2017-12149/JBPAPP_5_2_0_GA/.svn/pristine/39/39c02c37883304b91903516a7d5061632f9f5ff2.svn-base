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
package org.jboss.system.server.profile.basic;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;

/**
 * Include/exclude visitor attributes.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class IncludeExcludeVisitorAttributes extends VisitorAttributes
{
   private Logger log = Logger.getLogger(getClass());

   private Set<String> includes;
   private Set<String> excludes;

   public IncludeExcludeVisitorAttributes(Set<String> includes, Set<String> excludes)
   {
      if (includes == null)
         includes = Collections.emptySet();
      if (excludes == null)
         excludes = Collections.emptySet();

      this.includes = includes;
      this.excludes = excludes;

      setIncludeRoot(false);
      setLeavesOnly(true);
      setRecurseFilter(new RecurseFilter());
   }

   private class RecurseFilter implements VirtualFileFilter
   {
      public boolean accepts(VirtualFile file)
      {
         try
         {
            URL url = file.toURL();
            String urlString = url.toExternalForm();

            for (String include : includes)
            {
               if (urlString.contains(include) == false)
                  return false;
            }

            for (String exclude : excludes)
            {
               if (urlString.contains(exclude))
                  return false;
            }

            return true;
         }
         catch (Exception e)
         {
            if (log.isTraceEnabled())
               log.trace("Exception while filtering file: " + file, e);

            return false;
         }
      }
   }
}