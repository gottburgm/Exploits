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

import org.jboss.management.j2ee.StatisticsConstants;

import javax.management.j2ee.statistics.JCAConnectionStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class JCAConnectionStatsImpl extends StatsBase
        implements JCAConnectionStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -5409299496765142153L;

   // Private Data --------------------------------------------------
   
   private String cfName;
   private String mcfName;

   // Constructors --------------------------------------------------
      
   public JCAConnectionStatsImpl(String cfName, String mcfName)
   {
      this(cfName, mcfName, null, null);
   }

   public JCAConnectionStatsImpl(String cfName, String mcfName,
                                 TimeStatistic waitTime, TimeStatistic useTime)
   {
      if (waitTime == null)
      {
         waitTime = new TimeStatisticImpl("WaitTime", StatisticsConstants.MILLISECOND,
                 "Time spent waiting for a connection to be available");
      }
      if (useTime == null)
      {
         useTime = new TimeStatisticImpl("UseTime", StatisticsConstants.MILLISECOND,
                 "Time spent using the connection");
      }
      super.addStatistic("WaitTime", waitTime);
      super.addStatistic("UseTime", useTime);
      this.cfName = cfName;
      this.mcfName = mcfName;
   }

   public String getConnectionFactory()
   {
      return cfName;
   }

   public String getManagedConnectionFactory()
   {
      return mcfName;
   }

   public TimeStatistic getWaitTime()
   {
      TimeStatistic ts = (TimeStatistic) getStatistic("WaitTime");
      return ts;
   }

   public TimeStatistic getUseTime()
   {
      TimeStatistic ts = (TimeStatistic) getStatistic("UseTime");
      return ts;
   }
}
