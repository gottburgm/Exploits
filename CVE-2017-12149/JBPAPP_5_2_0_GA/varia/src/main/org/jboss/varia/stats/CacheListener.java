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
package org.jboss.varia.stats;

import org.jboss.ejb.plugins.cmp.jdbc2.schema.Cache;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.management.ObjectName;

/**
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class CacheListener
   extends ServiceMBeanSupport
   implements Cache.Listener, CacheListenerMBean
{
   private static final Logger log = Logger.getLogger(CacheListener.class);

   private ObjectName statsCollector;
   private ObjectName cacheName;
   private String tableName;

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getStatsCollector()
   {
      return statsCollector;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setStatsCollector(ObjectName statsCollector)
   {
      this.statsCollector = statsCollector;
   }

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getCacheName()
   {
      return cacheName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setCacheName(ObjectName cacheName)
   {
      this.cacheName = cacheName;
   }

   public void startService() throws Exception
   {
      tableName = cacheName.getKeyProperty("table");
      getServer().invoke(cacheName, "registerListener", new Object[]{this}, new String[]{Cache.Listener.class.getName()});
   }

   // Cache.Listener implementation

   public void contention(int partitionIndex, long time)
   {
      try
      {
         StatisticalItem item = new ContentionStats(tableName, partitionIndex, time);
         server.invoke(statsCollector, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }

   public void eviction(int partitionIndex, Object pk, int size)
   {
      try
      {
         StatisticalItem item = new EvictionStats(tableName, partitionIndex, pk);
         server.invoke(statsCollector, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }

   public void hit(int partitionIndex)
   {
      try
      {
         StatisticalItem item = new HitStats(tableName, partitionIndex);
         server.invoke(statsCollector, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }

   public void miss(int partitionIndex)
   {
      try
      {
         StatisticalItem item = new MissStats(tableName, partitionIndex);
         server.invoke(statsCollector, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }

   public static class HitStats extends AbstractStatisticalItem
   {
      public static final String NAME = "Cache Hits Per Transaction";

      private final String tableName;
      private final int partitionIndex;

      public HitStats(String name, int partitionIndex)
      {
         super(NAME);
         value = name + partitionIndex;
         this.tableName = name;
         this.partitionIndex = partitionIndex;
      }

      public String getTableName()
      {
         return tableName;
      }

      public int getPartitionIndex()
      {
         return partitionIndex;
      }
   }

   public static class MissStats extends AbstractStatisticalItem
   {
      public static final String NAME = "Cache Misses Per Transaction";

      public MissStats(String name, int partitionIndex)
      {
         super(NAME);
         value = name;
      }
   }

   public static class ContentionStats extends AbstractStatisticalItem
   {
      public static final String NAME = "Cache Contention Statistics Per Transaction";

      private long maxContentionTime;
      private long contentionTimeTotal;

      public ContentionStats(String name, int partitionIndex, long ms)
      {
         super(NAME);
         value = name;
         contentionTimeTotal = maxContentionTime = ms;
      }

      public void merge(StatisticalItem item)
      {
         super.merge(item);

         ContentionStats cs = (ContentionStats)item;
         if(cs.maxContentionTime > maxContentionTime)
         {
            maxContentionTime = cs.maxContentionTime;
         }
         contentionTimeTotal += cs.contentionTimeTotal;
      }

      public long getContentionTimeTotal()
      {
         return contentionTimeTotal;
      }

      public long getMaxContentionTime()
      {
         return maxContentionTime;
      }
   }

   public static class EvictionStats extends AbstractStatisticalItem
   {
      public static final String NAME = "Cache Eviction Statistics Per Transaction";

      private final String tableName;
      private final Object pk;

      public EvictionStats(String tableName, int partitionIndex, Object pk)
      {
         super(NAME);
         value = tableName;

         this.tableName = tableName;
         this.pk = pk;
      }

      public String getTableName()
      {
         return tableName;
      }

      public Object getPk()
      {
         return pk;
      }
   }
}
