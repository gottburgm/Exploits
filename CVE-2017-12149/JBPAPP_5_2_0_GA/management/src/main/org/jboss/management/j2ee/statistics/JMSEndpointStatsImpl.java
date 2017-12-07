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

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.JMSEndpointStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * Represents a statistics provided by a JMS message producer or a
 * JMS message consumer
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public abstract class JMSEndpointStatsImpl extends StatsBase
        implements JMSEndpointStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -7305748957998405748L;
   
   // Attributes ----------------------------------------------------
   private CountStatistic mMessageCount;
   private CountStatistic mPendingMessageCount;
   private CountStatistic mExpiredMessageCount;
   private TimeStatistic mMessageWaitTime;

   // Constructors --------------------------------------------------

   public JMSEndpointStatsImpl(CountStatistic pMessageCount,
                               CountStatistic pPendingMessageCount,
                               CountStatistic pExpiredMessageCount,
                               TimeStatistic pMessageWaitTime)
   {
      mMessageCount = pMessageCount;
      super.addStatistic("MessageCount", mMessageCount);
      mPendingMessageCount = pPendingMessageCount;
      super.addStatistic("PendingMessageCount", mPendingMessageCount);
      mExpiredMessageCount = pExpiredMessageCount;
      super.addStatistic("ExpiredMessageCoun", mExpiredMessageCount);
      mMessageWaitTime = pMessageWaitTime;
      super.addStatistic("MessageWaitTime", mMessageWaitTime);
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JMSConnectionStats implementation -------

   public CountStatistic getMessageCount()
   {
      return mMessageCount;
   }

   public CountStatistic getPendingMessageCount()
   {
      return mPendingMessageCount;
   }

   public CountStatistic getExpiredMessageCount()
   {
      return mExpiredMessageCount;
   }

   public TimeStatistic getMessageWaitTime()
   {
      return mMessageWaitTime;
   }
}
