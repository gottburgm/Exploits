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
package org.jboss.deployment.vfs;

import java.util.Collections;
import java.util.Date;

import org.jboss.virtual.spi.VFSContext;
import org.jboss.virtual.spi.cache.CacheStatistics;
import org.jboss.virtual.spi.cache.VFSCache;
import org.jboss.virtual.spi.cache.VFSCacheFactory;

/**
 * Simple vfs cache statistics.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class VFSCacheStatistics implements VFSCacheStatisticsMBean
{
   private CacheStatistics statistics;

   public String listCachedContexts()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("<table><tr><th>VFSContext - root URI</th></tr>");
      for (VFSContext context : getStatistics().getCachedContexts())
      {
         builder.append("<tr>");
         builder.append("<td>").append(context.getRootURI()).append("</td>");
         builder.append("</tr>");
      }
      builder.append("</table>");
      return builder.toString();
   }

   public int size()
   {
      return getStatistics().size();
   }

   public Date lastInsert()
   {
      long lastInsert = getStatistics().lastInsert();
      return (lastInsert > 0) ? new Date(lastInsert) : null;
   }

   public String cacheToString()
   {
      return getStatistics().toString();
   }

   private CacheStatistics getStatistics()
   {
      if (statistics == null)
      {
         VFSCache cache = VFSCacheFactory.getInstance();
         if (cache instanceof CacheStatistics)
         {
            statistics = CacheStatistics.class.cast(cache);
         }
         else
         {
            statistics = new CacheStatistics()
            {
               public Iterable<VFSContext> getCachedContexts()
               {
                  return Collections.emptySet();
               }

               public int size()
               {
                  return -1;
               }

               public long lastInsert()
               {
                  return -1;
               }
            };
         }
      }
      return statistics;
   }

   @Override
   public String toString()
   {
      return "Noop Cache / Statistics";
   }
}
