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
import javax.management.j2ee.statistics.JCAConnectionPoolStats;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * The JSR77.6.20 JCAConnectionPoolStats implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class JCAConnectionPoolStatsImpl extends JCAConnectionStatsImpl
        implements JCAConnectionPoolStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = 6867747857618271195L;

   // Constructors --------------------------------------------------
      
   public JCAConnectionPoolStatsImpl(String cfName, String mcfName,
                                     BoundedRangeStatistic freePoolSize, BoundedRangeStatistic poolSize)
   {
      this(cfName, mcfName, null, null, null, null, freePoolSize, poolSize, null);
   }

   public JCAConnectionPoolStatsImpl(String cfName, String mcfName,
                                     TimeStatistic waitTime, TimeStatistic useTime, CountStatistic closeCount,
                                     CountStatistic createCount, BoundedRangeStatistic freePoolSize,
                                     BoundedRangeStatistic poolSize, RangeStatistic waitingThreadCount)
   {
      super(cfName, mcfName, waitTime, useTime);
      if (closeCount == null)
      {
         closeCount = new CountStatisticImpl("CloseCount", "1",
                 "The number of connection closes");
      }
      if (createCount == null)
      {
         createCount = new CountStatisticImpl("CreateCount", "1",
                 "The number of connection creates");
      }
      if (waitingThreadCount == null)
      {
         waitingThreadCount = new RangeStatisticImpl("WaitingThreadCount",
                 "1", "The number of threads waiting for a connection");
      }
      super.addStatistic("CloseCount", closeCount);
      super.addStatistic("CreateCount", createCount);
      super.addStatistic("FreePoolSize", freePoolSize);
      super.addStatistic("PoolSize", poolSize);
      super.addStatistic("WaitingThreadCount", waitingThreadCount);
   }

   public CountStatistic getCloseCount()
   {
      CountStatistic cs = (CountStatistic) getStatistic("CloseCount");
      return cs;
   }

   public CountStatistic getCreateCount()
   {
      CountStatistic cs = (CountStatistic) getStatistic("CreateCount");
      return cs;
   }

   public BoundedRangeStatistic getFreePoolSize()
   {
      BoundedRangeStatistic brs = (BoundedRangeStatistic) getStatistic("FreePoolSize");
      return brs;
   }

   public BoundedRangeStatistic getPoolSize()
   {
      BoundedRangeStatistic brs = (BoundedRangeStatistic) getStatistic("PoolSize");
      return brs;
   }

   public RangeStatistic getWaitingThreadCount()
   {
      RangeStatistic rs = (RangeStatistic) getStatistic("WaitingThreadCount");
      return rs;
   }
}
