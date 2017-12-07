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
package org.jboss.varia.stats.report;

import org.jboss.varia.stats.TxReport;
import org.jboss.varia.stats.CacheListener;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class CacheReportGenerator
   extends ReportGenerator
{
   protected void content(String reportName, StringBuffer content) throws Exception
   {
      StringBuffer contentionBuf = new StringBuffer();
      contentionBuf.append(
         "<table><tr><th>Lock Contention Per Table</th><th>Total time</th><th>Max time</th><th>count</th></tr>");
      int contentionTotal = 0;
      int contentionTimeTotal = 0;
      long maxContentionTime = 0;
      StringBuffer evictionBuf = new StringBuffer();
      evictionBuf.append("<table><tr><th>Eviction per table</th><th>count</th></tr>");
      int evictionTotal = 0;
      StringBuffer hitsBuf = new StringBuffer();
      hitsBuf.append("<table><tr><th>Hits per table</th><th>count</th></tr>");
      int hitsTotal = 0;
      StringBuffer missesBuf = new StringBuffer();
      missesBuf.append("<table><tr><th>Misses per table</th><th>count</th></tr>");
      int missesTotal = 0;

      StringBuffer reportsTable = new StringBuffer();
      reportsTable.append("<table><tr><th>Transaction started by</th><th>Total</th></tr>");

      int txTotal = 0;

      Map contention = new HashMap();
      Map eviction = new HashMap();
      Map hits = new HashMap();
      Map misses = new HashMap();

      Iterator reports = getReportsIterator();
      while(reports.hasNext())
      {
         TxReport report = (TxReport) reports.next();

         Map contentionMap = (Map) report.getStats().get(CacheListener.ContentionStats.NAME);
         Map evictionMap = (Map) report.getStats().get(CacheListener.EvictionStats.NAME);
         Map hitsMap = (Map) report.getStats().get(CacheListener.HitStats.NAME);
         Map missesMap = (Map) report.getStats().get(CacheListener.MissStats.NAME);

         txTotal += report.getCount();

         if(contentionMap == null && evictionMap == null && hitsMap == null && missesMap == null)
         {
            continue;
         }

         reportsTable.append("<tr><td>");

         boolean selected = report.getName().equals(reportName);
         if(!selected)
         {
            reportsTable.append("<a href='HtmlAdaptor?")
               .append("action=invokeOpByName&name=")
               .append(getServiceName())
               .append("&methodName=generate&")
               .append("argType=java.lang.String&arg0=")
               .append(report.getName())
               .append("'>");
         }

         reportsTable.append(report.getName());

         if(!selected)
         {
            reportsTable.append("</a>");
         }

         reportsTable.append("</td><td>")
            .append(report.getCount()).append("</td></tr>");

         if(selected || reportName == null || reportName.trim().length() == 0)
         {
            if(contentionMap != null)
            {
               for(Iterator items = contentionMap.values().iterator(); items.hasNext();)
               {
                  CacheListener.ContentionStats item = (CacheListener.ContentionStats) items.next();

                  Contention c = (Contention) contention.get(item.getValue());
                  if(c == null)
                  {
                     c = new Contention(item.getValue());
                     contention.put(c.tableName, c);
                  }

                  c.total += item.getContentionTimeTotal();
                  c.count += item.getCount();
                  if(c.maxTime < item.getMaxContentionTime())
                  {
                     c.maxTime = item.getMaxContentionTime();
                  }

                  contentionTotal += item.getCount();
                  contentionTimeTotal += item.getContentionTimeTotal();
                  if(item.getMaxContentionTime() > maxContentionTime)
                  {
                     maxContentionTime = item.getMaxContentionTime();
                  }
               }
            }

            if(evictionMap != null)
            {
               for(Iterator items = evictionMap.values().iterator(); items.hasNext();)
               {
                  CacheListener.EvictionStats item = (CacheListener.EvictionStats) items.next();

                  Eviction e = (Eviction) eviction.get(item.getTableName());
                  if(e == null)
                  {
                     e = new Eviction(item.getTableName());
                     eviction.put(e.tableName, e);
                  }
                  e.count += item.getCount();

                  evictionTotal += item.getCount();
               }
            }

            if(hitsMap != null)
            {
               for(Iterator items = hitsMap.values().iterator(); items.hasNext();)
               {
                  CacheListener.HitStats item = (CacheListener.HitStats) items.next();

                  Hit h = (Hit) hits.get(item.getTableName());
                  if(h == null)
                  {
                     h = new Hit(item.getTableName());
                     hits.put(h.tableName, h);
                  }
                  h.partitionHit(item.getPartitionIndex(), item.getCount());

                  hitsTotal += item.getCount();
               }
            }

            if(missesMap != null)
            {
               for(Iterator items = missesMap.values().iterator(); items.hasNext();)
               {
                  CacheListener.MissStats item = (CacheListener.MissStats) items.next();
                  Miss m = (Miss) misses.get(item.getValue());
                  if(m == null)
                  {
                     m = new Miss(item.getValue());
                     misses.put(m.tableName, m);
                  }
                  m.count += item.getCount();
                  missesTotal += item.getCount();
               }
            }
         }
      }

      reportsTable.append("<tr><td>");

      boolean select = reportName != null && reportName.trim().length() > 0;
      if(select)
      {
         reportsTable.append("<a href='HtmlAdaptor?")
            .append("action=invokeOpByName&name=")
            .append(getServiceName())
            .append("&methodName=generate&")
            .append("argType=java.lang.String&arg0=")
            .append("'>");
      }

      reportsTable.append("all transactions");

      if(select)
      {
         reportsTable.append("</a>");
      }

      reportsTable.append("</td><td>").append(txTotal).append("</td></tr></table>");

      for(Iterator i = contention.values().iterator(); i.hasNext();)
      {
         Contention c = (Contention) i.next();
         contentionBuf.append("<tr><td>").append(c.tableName).append("</td><td>")
            .append(c.total).append("</td><td>")
            .append(c.maxTime).append("</td><td>")
            .append(c.count).append("</td></tr>");
      }

      for(Iterator i = eviction.values().iterator(); i.hasNext();)
      {
         Eviction e = (Eviction) i.next();
         evictionBuf.append("<tr><td>").append(e.tableName).append("</td><td>")
            .append(e.count).append("</td></tr>");
      }

      StringBuffer partitionBuf = new StringBuffer();
      partitionBuf.append("<table>");
      for(Iterator i = hits.values().iterator(); i.hasNext();)
      {
         Hit h = (Hit) i.next();
         hitsBuf.append("<tr><td>").append(h.tableName).append("</td><td>")
            .append(h.count).append("</td></tr>");

         if(h.partitions != null && h.partitions.length > 0)
         {
            partitionBuf.append("<tr><td>");

            partitionBuf.append("<table><tr><th>Table: ").append(h.tableName).append("</th></tr>")
               .append("<tr><th>Partition index</th><th>count</th><th>%</th></tr>");
            for(int pI = 0; pI < h.partitions.length; ++pI)
            {
               final int hit = h.partitions[pI];
               partitionBuf.append("<tr><td>")
                  .append(pI).append("</td><td>")
                  .append(hit).append("</td><td>")
                  .append((int)(100*((double)hit/h.maxHitPerPartition))).append("</td></tr>");
            }
            partitionBuf.append("</table>");

            partitionBuf.append("</td></tr>");
         }
      }
      partitionBuf.append("</table>");

      for(Iterator i = misses.values().iterator(); i.hasNext();)
      {
         Miss m = (Miss) i.next();
         missesBuf.append("<tr><td>").append(m.tableName).append("</td><td>")
            .append(m.count).append("</td></tr>");
      }

      contentionBuf.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(contentionTimeTotal).append("</font></td><td><font color='red'>")
         .append(maxContentionTime).append("</font></td><td><font color='red'>")
         .append(contentionTotal).append("</font></td></tr>")
         .append("</table>");
      evictionBuf.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(evictionTotal).append("</font></td></tr>")
         .append("</table>");
      hitsBuf.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(hitsTotal).append("</font></td></tr>")
         .append("</table>");
      missesBuf.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(missesTotal).append("</font></td></tr>")
         .append("</table>");

      content.append("<table><tr valign='top'><td>")
         .append(reportsTable)
         .append("</td><td>").append(contentionBuf)
         .append("</td><td>").append(evictionBuf)
         .append("</td><td>").append(hitsBuf)
         .append("</td><td>").append(missesBuf)
         .append("</td></tr></table>");

      content.append(partitionBuf);
   }

   // Inner

   class Contention
   {
      public final String tableName;
      public int total;
      public long maxTime;
      public int count;

      public Contention(String tableName)
      {
         this.tableName = tableName;
      }
   }

   class Eviction
   {
      public final String tableName;
      public int count;

      public Eviction(String tableName)
      {
         this.tableName = tableName;
      }
   }

   class Hit
   {
      public final String tableName;
      public int count;
      private int[] partitions;
      private int maxHitPerPartition;

      public Hit(String tableName)
      {
         this.tableName = tableName;
      }

      public void partitionHit(int partitionIndex, int count)
      {
         if(partitions == null)
         {
            partitions = new int[partitionIndex + 1];
         }
         else if(partitions.length < partitionIndex + 1)
         {
            int[] tmp = partitions;
            partitions = new int[partitionIndex + 1];
            System.arraycopy(tmp, 0, partitions, 0, tmp.length);
         }
         partitions[partitionIndex] += count;
         this.count += count;

         if(maxHitPerPartition < partitions[partitionIndex])
         {
            maxHitPerPartition = partitions[partitionIndex];
         }
      }
   }

   class Miss
   {
      public final String tableName;
      public int count;

      public Miss(String tableName)
      {
         this.tableName = tableName;
      }
   }
}
