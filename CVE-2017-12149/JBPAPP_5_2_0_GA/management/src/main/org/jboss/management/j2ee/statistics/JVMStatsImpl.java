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
package org.jboss.management.j2ee.statistics;

import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.JVMStats;

/**
 * The JSR77.6.32 JMVStats implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class JVMStatsImpl extends StatsBase
        implements JVMStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -7842397217562728796L;
   
   // Constructors --------------------------------------------------
   
   public JVMStatsImpl()
   {
      this(new CountStatisticImpl("UpTime", "MILLISECOND", "Time the VM has been running"),
              new BoundedRangeStatisticImpl("HeapSize", "Bytes", "Size of the VM's heap", 0, 0));
   }

   public JVMStatsImpl(CountStatistic upTime, BoundedRangeStatistic heapSize)
   {
      addStatistic("UpTime", upTime);
      addStatistic("HeapSize", heapSize);
   }

// Begin javax.management.j2ee.statistics.JVMStats interface methods
   public CountStatistic getUpTime()
   {
      CountStatisticImpl upTime = (CountStatisticImpl) getStatistic("UpTime");
      long now = System.currentTimeMillis();
      long elapsed = now - upTime.getStartTime();
      upTime.set(elapsed);
      return upTime;
   }

   public BoundedRangeStatistic getHeapSize()
   {
      BoundedRangeStatisticImpl heapSize = (BoundedRangeStatisticImpl) getStatistic("HeapSize");
      long totalMemory = Runtime.getRuntime().totalMemory();
      heapSize.set(totalMemory);
      return heapSize;
   }
// End javax.management.j2ee.statistics.JVMStats interface methods
}
