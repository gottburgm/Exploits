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

import org.jboss.tm.TransactionLocal;

import javax.transaction.Transaction;
import javax.transaction.Synchronization;
import javax.transaction.Status;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public class TxStatistics
{
   private final Map reports = new HashMap();
   private final Set collectedItemNames = new HashSet();

   private final TransactionLocal txReport = new TransactionLocal()
   {
      protected Object initialValue()
      {
         Transaction tx = getTransaction();
         TxReport report;
         if(tx != null)
         {
            report = new TxReport();
            try
            {
               tx.registerSynchronization(new TxSynchronization(report));
            }
            catch(Exception e)
            {
               e.printStackTrace();
               throw new IllegalStateException("Failed to register tx synchronization: " + e.getMessage());
            }
         }
         else
         {
            report = null;
         }
         return report;
      }
   };

   public String[] getCollectedItemNames()
   {
      return (String[])collectedItemNames.toArray(new String[collectedItemNames.size()]);
   }

   public Iterator getReports()
   {
      return reports.values().iterator();
   }

   public synchronized void clear()
   {
      reports.clear();
      collectedItemNames.clear();
   }

   public synchronized void addStatisticalItem(StatisticalItem item)
   {
      TxReport report = (TxReport) txReport.get();
      if(report != null)
      {
         boolean addedNew = report.addItem(item);
         if(addedNew)
         {
            collectedItemNames.add(item.getName());
         }
      }
   }

   private synchronized void addReport(TxReport report)
   {
      TxReport oldReport = (TxReport)reports.get(report.getName());
      if(oldReport == null)
      {
         reports.put(report.getName(), report);
      }
      else
      {
         oldReport.merge(report);
      }
   }

   public TxReport getReports(String reportName)
   {
      return (TxReport)reports.get(reportName);
   }

   // Inner

   private class TxSynchronization implements Synchronization
   {
      private final TxReport report;

      public TxSynchronization(TxReport report)
      {
         this.report = report;
      }

      public void beforeCompletion()
      {
      }

      public void afterCompletion(int status)
      {
         if(status != Status.STATUS_ROLLEDBACK)
         {
            try
            {
               addReport(report);
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}
