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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * VFS based ResourcePatternResolver.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class VFSResourcePatternResolver extends PathMatchingResourcePatternResolver
{
   private Logger log = Logger.getLogger(VFSResourcePatternResolver.class);

   public VFSResourcePatternResolver()
   {
      super(new VFSResourceLoader());
   }

   public VFSResourcePatternResolver(ClassLoader classLoader)
   {
      super(new VFSResourceLoader(classLoader));
   }

   protected Resource[] findPathMatchingResources(String locationPattern) throws IOException
   {
      if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX))
         locationPattern = locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length());
      String rootDirPath = determineRootDir(locationPattern);
      String subPattern = locationPattern.substring(rootDirPath.length());
      if (rootDirPath.startsWith("/"))
         rootDirPath = rootDirPath.substring(1);

      List<Resource> resources = new ArrayList<Resource>();
      Enumeration<URL> urls = getClassLoader().getResources(rootDirPath);
      while(urls.hasMoreElements())
         resources.addAll(getVFSResources(urls.nextElement(), subPattern));

      return resources.toArray(new Resource[resources.size()]);
   }

   /**
    * Get VFS resources.
    *
    * @param rootURL the root URL
    * @param subPattern the sub pattern
    * @return vfs resources list
    * @throws IOException for any error
    */
   protected List<Resource> getVFSResources(URL rootURL, String subPattern) throws IOException
   {
      log.debug("Scanning url: " + rootURL + ", sub-pattern: " + subPattern);
      VirtualFile root = VFS.getRoot(rootURL);
      PatternVirtualFileVisitor visitor = new PatternVirtualFileVisitor(subPattern);
      root.visit(visitor);
      if (log.isTraceEnabled())
         log.trace("Found resources: " + visitor);
      return visitor.getResources();
   }

   protected Resource convertClassLoaderURL(URL url)
   {
      try
      {
         VirtualFile file = VFS.getRoot(url);
         return new VFSResource(file);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get visitor attributes.
    * Allows for override, if necessary.
    * 
    * @return  visitor attributes
    */
   protected VisitorAttributes getVisitorAttributes()
   {
      return VisitorAttributes.RECURSE_LEAVES_ONLY;
   }

   private class PatternVirtualFileVisitor implements VirtualFileVisitor
   {
      private String subPattern;
      private List<Resource> resources = new ArrayList<Resource>();

      private PatternVirtualFileVisitor(String subPattern)
      {
         this.subPattern = subPattern;
      }

      public VisitorAttributes getAttributes()
      {
         return getVisitorAttributes();
      }

      public void visit(VirtualFile vf)
      {
         if (getPathMatcher().match(subPattern, vf.getPathName()))
            resources.add(new VFSResource(vf));
      }

      public List<Resource> getResources()
      {
         return resources;
      }

      public int size()
      {
         return resources.size();
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("sub-pattern: ").append(subPattern);
         buffer.append(", resources: ").append(resources);
         return buffer.toString();
      }
   }
}
