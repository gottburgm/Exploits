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
import org.jboss.varia.stats.StatisticalItem;
import org.jboss.varia.stats.TxStatistics;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class GeneralReportGenerator
   extends ReportGenerator
{
   public void content(String reportName, StringBuffer buf) throws Exception
   {
      buf.append("<table><tr valign='top'><td>");

      buf.append("<table>");
      buf.append("<tr><th>Transaction started by</th><th>total</th>");
      buf.append("</tr>");

      TxStatistics stats = getTxStatistics();
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
               .append("action=invokeOpByName&name=")
               .append(getServiceName())
               .append("&methodName=generate&")
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
         .append("<li><b>min</b> - the minimum number of times the item took place in the given transaction</li>")
         .append("<li><b>max</b> - the maximum number of times the item took place in the given transaction</li>")
         .append("</ul>");
   }
}
