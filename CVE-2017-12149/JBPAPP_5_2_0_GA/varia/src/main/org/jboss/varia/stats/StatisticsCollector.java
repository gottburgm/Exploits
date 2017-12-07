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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.varia.stats.report.ReportGenerator;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 * @jmx:mbean name="jboss.stats:service=StatisticsCollector"
 * extends="org.jboss.system.ServiceMBean"
 */
public class StatisticsCollector
   extends ServiceMBeanSupport
   implements StatisticsCollectorMBean
{
   private final TxStatistics stats = new TxStatistics();

   private final Set reportGenerators = new HashSet();


   /**
    * @jmx.managed-operation
    */
   public void registerReportGenerator(ReportGenerator reportGenerator)
   {
      reportGenerators.add(reportGenerator);
   }

   /**
    * @jmx.managed-operation
    */
   public void unregisterReportGenerator(ReportGenerator reportGenerator)
   {
      reportGenerators.remove(reportGenerator);
   }

   /**
    * @jmx.managed-operation
    */
   public void clearStatistics()
   {
      stats.clear();
   }

   /**
    * @jmx.managed-operation
    */
   public void addStatisticalItem(StatisticalItem item)
   {
      stats.addStatisticalItem(item);
   }

   /**
    * @jmx.managed-operation
    */
   public Iterator reportsIterator()
   {
      return stats.getReports();
   }

   /**
    * @jmx.managed-operation
    */
   public TxStatistics txStatistics()
   {
      return stats;
   }

   /**
    * @jmx.managed-operation
    */
   public synchronized String reports()
   {
      StringBuffer buf = new StringBuffer();

      buf.append("<table><tr><th>Report</th><th>Description</th></tr>");
      for(Iterator generators = reportGenerators.iterator(); generators.hasNext();)
      {
         ReportGenerator generator = (ReportGenerator)generators.next();
         buf.append("<tr><td>")
            .append("<a href='HtmlAdaptor?")
            .append("action=invokeOpByName&name=")
            .append(generator.getServiceName())
            .append("&methodName=generate&")
            .append("argType=java.lang.String&arg0=")
            .append("'>")
            .append(generator.getName())
            .append("</a></td><td>")
            .append(generator.getDescription())
            .append("</td></tr>");
      }
      buf.append("</table>");

      /*
      buf.append("<table><tr valign='top'><td>");

      buf.append("<table>");
      buf.append("<tr><th>Transaction started by</th><th>total</th>");
      buf.append("</tr>");

      for(Iterator iter = stats.getReports(); iter.hasNext();)
      {
         TxReport report = (TxReport) iter.next();

         String name = report.getName();
         buf.append("<tr valign='top'>")
            .append("<td>");

         boolean anchor = !name.equals(reportName) && reportName != null;
         if(anchor)
         {
            buf.append("<a href='HtmlAdaptor?")
               .append("action=invokeOpByName&name=jboss.stats%3Aservice%3DStatisticsCollector&methodName=report&")
               .append("argType=java.lang.String&arg0=")
               .append(name)
               .append("'>");
         }

         buf.append(name)
            .append("</td><td>")
            .append(report.getCount())
            .append("</td>");

         if(anchor)
         {
            buf.append("</a>");
         }

         buf.append("</td></tr>");
      }

      buf.append("</table>");
      buf.append("</td><td>");

      TxReport report = stats.getReports(reportName);
      if(report != null)
      {
         buf.append("<table><tr>");

         String[] itemNames = stats.getCollectedItemNames();
         for(int i = 0; i < itemNames.length; ++i)
         {
            buf.append("<th>").append(itemNames[i]).append("</th>");
         }

         buf.append("</tr><tr valign='top'>");

         for(int i = 0; i < itemNames.length; ++i)
         {
            buf.append("<td>");
            String itemName = itemNames[i];

            Map itemMap = (Map) report.getStats().get(itemName);
            if(itemMap != null && !itemMap.isEmpty())
            {
               buf.append("<table width='100%'>")
                  .append("<tr><th>item</th><th>%</th><th>avg</th><th>min</th><th>max</th></tr>");

               for(Iterator itemIter = itemMap.values().iterator(); itemIter.hasNext();)
               {
                  StatisticalItem item = (StatisticalItem) itemIter.next();
                  buf.append("<tr><td>")
                     .append(item.getValue())
                     .append("</td><td>")
                     .append(100*((double)item.getMergedItemsTotal() / report.getCount()))
                     .append("</td><td>")
                     .append(((double) item.getCount()) / report.getCount())
                     .append("</td><td>")
                     .append(item.getMinCountPerTx())
                     .append("</td><td>")
                     .append(item.getMaxCountPerTx())
                     .append("</td>");
               }

               buf.append("</table>");
            }

            buf.append("</td>");
         }

         buf.append("</tr></table>");
      }

      buf.append("</td></tr></table>");

      buf.append("<ul>")
         .append("<li><b>Transaction started by</b> - the method which started the transaction</li>")
         .append("<li><b>total</b> - the total number of transactions in the run</li>")
         .append("<li><b>%</b> - the percentage of transactions this item took place in</li>")
         .append("<li><b>avg</b> - the average number of times the item took place in the given transaction</li>")
         .append("</ul>");
         */

      return buf.toString();
   }
}
