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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.varia.stats.TxStatistics;

import javax.management.ObjectName;
import java.util.Iterator;

/**
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public abstract class ReportGenerator
   extends ServiceMBeanSupport
   implements ReportGeneratorMBean
{
   protected ObjectName statsCollector;

   protected String name;
   protected String description;

   /**
    * @jmx.managed-attribute
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getName()
   {
      return name;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getDescription()
   {
      return description;
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
   public ObjectName getStatsCollector()
   {
      return statsCollector;
   }

   public void startService() throws Exception
   {
      try
      {
         server.invoke(
            statsCollector,
            "registerReportGenerator",
            new Object[]{this},
            new String[]{ReportGenerator.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to register report generator.");
         throw e;
      }
   }

   public void stopService() throws Exception
   {
      try
      {
         server.invoke(statsCollector,
            "unregisterReportGenerator",
            new Object[]{this},
            new String[]{ReportGenerator.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to unregister report generator.");
         throw e;
      }
   }

   /**
    * @jmx.managed-operation
    */
   public String generate(String reportName) throws Exception
   {
      StringBuffer content = new StringBuffer();
      content.append("<a href='HtmlAdaptor?")
         .append("action=invokeOpByName&name=")
         .append(statsCollector)
         .append("&methodName=reports")
         .append("'>Back to report list</a>");

      content(reportName, content);

      return content.toString();
   }

   // Protected

   protected abstract void content(String reportName, StringBuffer buf) throws Exception;

   protected Iterator getReportsIterator()
      throws Exception
   {
      try
      {
         return (Iterator) server.invoke(statsCollector, "reportsIterator", new Object[]{}, new String[]{});
      }
      catch(Exception e)
      {
         log.error("Failed to invoke getReportsIterator() operation.");
         throw e;
      }
   }

   protected TxStatistics getTxStatistics()
      throws Exception
   {
      try
      {
         return (TxStatistics) server.invoke(statsCollector, "txStatistics", new Object[]{}, new String[]{});
      }
      catch(Exception e)
      {
         log.error("Failed to invoke getTxStatistics() operation.");
         throw e;
      }
   }
}
