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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class TableReportGenerator
   extends ReportGenerator
{
   // ReportGenerator implementation

   protected void content(String reportName, StringBuffer content) throws Exception
   {
      StringBuffer reportsTable = new StringBuffer();
      reportsTable.append("<table><tr><th>Transaction started by</th><th>Total</th></tr>");

      Map tables = new HashMap();
      Map sqls = new HashMap();
      int txTotal = 0;

      Iterator reports = getReportsIterator();
      while(reports.hasNext())
      {
         TxReport report = (TxReport) reports.next();
         txTotal += report.getCount();
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
            generateReport(report, sqls, tables);
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

      StringBuffer tablesBuf = new StringBuffer();
      tablesBuf.append(
         "<table><tr><th>Table</th><th>selects</th><th>updates</th><th>inserts</th><th>deletes</th></tr>");
      int totalSelects = 0;
      int totalUpdates = 0;
      int totalInserts = 0;
      int totalDeletes = 0;
      for(Iterator tableIter = tables.values().iterator(); tableIter.hasNext();)
      {
         TableStats table = (TableStats) tableIter.next();
         tablesBuf.append("<tr><td>").append(table.name).append("</td><td>")
            .append(table.selects).append("</td><td>")
            .append(table.updates).append("</td><td>")
            .append(table.inserts).append("</td><td>")
            .append(table.deletes).append("</td></tr>");

         totalSelects += table.selects;
         totalUpdates += table.updates;
         totalInserts += table.inserts;
         totalDeletes += table.deletes;
      }
      tablesBuf.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(totalSelects).append("</font></td><td><font color='red'>")
         .append(totalUpdates).append("</font></td><td><font color='red'>")
         .append(totalInserts).append("</font></td><td><font color='red'>")
         .append(totalDeletes).append("</font></td></tr>")
         .append("</table>");

      StringBuffer itemsTable = new StringBuffer();
      itemsTable.append("<table><tr><th>SQL</th><th>Total</th></tr>");
      int totalStmt = 0;
      for(Iterator itemIter = sqls.values().iterator(); itemIter.hasNext();)
      {
         SqlStats sql = (SqlStats)itemIter.next();
         itemsTable.append("<tr><td>").append(sql.sql)
            .append("</td><td>").append(sql.total).append("</td></tr>");
         totalStmt += sql.total;
      }
      itemsTable.append("<tr><td><font color='red'>total</font></td><td><font color='red'>")
         .append(totalStmt).append("</font></td></tr></table>");

      content.append("<table><tr valign='top'><td>")
         .append(reportsTable)
         .append("</td><td>").append(tablesBuf)
         .append("</td><td>").append(itemsTable)
         .append("</td></tr></table>");
   }

   // Private

   private void generateReport(TxReport report, Map sqls, Map tables)
   {
      Map itemMap = (Map) report.getStats().get(TxReport.SqlStats.NAME);
      if(itemMap != null)
      {
         for(Iterator items = itemMap.values().iterator(); items.hasNext();)
         {
            StatisticalItem item = (StatisticalItem) items.next();
            String sql = item.getValue().toLowerCase();

            SqlStats sqlStats = (SqlStats)sqls.get(sql);
            if(sqlStats == null)
            {
               sqlStats = new SqlStats(sql);
               sqls.put(sql, sqlStats);
            }
            sqlStats.total += item.getCount();

            if(sql.startsWith("select "))
            {
               int fromStart = sql.indexOf("from ");
               if(fromStart == -1)
               {
                  throw new IllegalStateException("FROM not found in: " + sql);
               }

               String table = sql.substring(fromStart + "from ".length());
               int tableEnd = table.indexOf(' ');
               if(tableEnd != -1)
               {
                  table = table.substring(0, tableEnd);
               }

               TableStats tableStats = (TableStats) tables.get(table);
               if(tableStats == null)
               {
                  tableStats = new TableStats(table);
                  tables.put(table, tableStats);
               }
               tableStats.selects += item.getCount();
            }
            else if(sql.startsWith("update "))
            {
               String table = sql.substring("update ".length());
               int tableEnd = table.indexOf(' ');
               if(tableEnd == -1)
               {
                  throw new IllegalStateException("Could not find end of the table name: " + sql);
               }

               table = table.substring(0, tableEnd);

               TableStats tableStats = (TableStats) tables.get(table);
               if(tableStats == null)
               {
                  tableStats = new TableStats(table);
                  tables.put(table, tableStats);
               }
               tableStats.updates += item.getCount();
            }
            else if(sql.startsWith("insert into "))
            {
               String table = sql.substring("insert into ".length());
               int tableEnd = table.indexOf('(');
               if(tableEnd == -1)
               {
                  throw new IllegalStateException("Could not find end of the table name: " + sql);
               }

               table = table.substring(0, tableEnd).trim();
               TableStats tableStats = (TableStats) tables.get(table);
               if(tableStats == null)
               {
                  tableStats = new TableStats(table);
                  tables.put(table, tableStats);
               }
               tableStats.inserts += item.getCount();
            }
            else if(sql.startsWith("delete from "))
            {
               String table = sql.substring("delete from ".length());
               int tableEnd = table.indexOf(' ');
               if(tableEnd == -1)
               {
                  throw new IllegalStateException("Could not find end of the table name: " + sql);
               }

               table = table.substring(0, tableEnd);
               TableStats tableStats = (TableStats) tables.get(table);
               if(tableStats == null)
               {
                  tableStats = new TableStats(table);
                  tables.put(table, tableStats);
               }
               tableStats.deletes += item.getCount();
            }
            else
            {
               throw new IllegalStateException("Unrecognized sql statement: " + sql);
            }
         }
      }
   }

   // Inner

   private static class SqlStats
   {
      public final String sql;
      public int total;

      public SqlStats(String sql)
      {
         this.sql = sql;
      }
   }

   private static class TableStats
   {
      public final String name;
      public int selects;
      public int updates;
      public int inserts;
      public int deletes;

      public TableStats(String name)
      {
         this.name = name;
      }

      public boolean equals(Object o)
      {
         if(this == o) return true;
         if(!(o instanceof TableStats)) return false;

         final TableStats tableStats = (TableStats) o;

         if(!name.equals(tableStats.name)) return false;

         return true;
      }

      public int hashCode()
      {
         return name.hashCode();
      }
   }
}
