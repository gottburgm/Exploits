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
package org.jboss.hibernate.jmx;

import java.net.URL;
import java.util.Set;
import java.util.HashSet;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;

/**
 * Mapping visitor, matching .hbm.xml files.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class HibernateMappingVisitor implements VirtualFileVisitor
{
   private Set<URL> urls = new HashSet<URL>();

   public Set<URL> getUrls()
   {
      return urls;
   }

   private static class MappingVisitorAttributes extends VisitorAttributes
   {
      public MappingVisitorAttributes()
      {
         setIgnoreErrors(true);
         setIncludeHidden(false);
         setIncludeRoot(false);
         setLeavesOnly(false);
         setRecurseFilter(RECURSE_ALL);
      }
   }

   private static final VisitorAttributes MAPPING_ATTRIBUTES = new MappingVisitorAttributes();

   public VisitorAttributes getAttributes()
   {
      return MAPPING_ATTRIBUTES;
   }

   public void visit(VirtualFile vf)
   {
      try
      {
         if (isMapping(vf))
            urls.add(vf.toURL());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Visit failed: " + e);
      }
   }

   /**
    * Is virtual file a mapping file.
    *
    * @param vf the virtual file
    * @return true if virtual file is mapping
    */
   protected boolean isMapping(VirtualFile vf)
   {
      return vf.getName().indexOf(".hbm.xml") > 0;
   }
}
