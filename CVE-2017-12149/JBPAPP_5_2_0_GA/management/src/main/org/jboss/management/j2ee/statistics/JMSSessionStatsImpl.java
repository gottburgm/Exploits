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
import javax.management.j2ee.statistics.JMSConsumerStats;
import javax.management.j2ee.statistics.JMSProducerStats;
import javax.management.j2ee.statistics.JMSSessionStats;
import javax.management.j2ee.statistics.TimeStatistic;


/**
 * Represents the statistics provided by a JMS Session
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public final class JMSSessionStatsImpl extends StatsBase
        implements JMSSessionStats
{
   // Constants -----------------------------------------------------

   /** @since 4.0.2 */
   private static final long serialVersionUID = 7614059976793609889L;
   
   // Attributes ----------------------------------------------------
   private JMSProducerStats[] mProducers;
   private JMSConsumerStats[] mConsumers;
   private CountStatistic mMessageCount;
   private CountStatistic mPendingMessageCount;
   private CountStatistic mExpiredMessageCount;
   private TimeStatistic mMessageWaitTime;
   private CountStatistic mDurableSubscriptionCount;

   // Constructors --------------------------------------------------

   public JMSSessionStatsImpl(JMSProducerStats[] pProducers,
                              JMSConsumerStats[] pConsumers,
                              CountStatistic pMessageCount,
                              CountStatistic pPendingMessageCount,
                              CountStatistic pExpiredMessageCount,
                              TimeStatistic pMessageWaitTime,
                              CountStatistic pDurableSubscriptionCount)
   {
      mProducers = (pProducers != null ? pProducers : new JMSProducerStats[0]);
      mConsumers = (pConsumers != null ? pConsumers : new JMSConsumerStats[0]);
      mMessageCount = pMessageCount;
      super.addStatistic("MessageCount", mMessageCount);
      mPendingMessageCount = pPendingMessageCount;
      super.addStatistic("PendingMessageCount", mPendingMessageCount);
      mExpiredMessageCount = pExpiredMessageCount;
      super.addStatistic("ExpiredMessageCount", mExpiredMessageCount);
      mMessageWaitTime = pMessageWaitTime;
      super.addStatistic("MessageWaitTime", mMessageWaitTime);
      mDurableSubscriptionCount = pDurableSubscriptionCount;
      super.addStatistic("DurableSubscriptionCount", mDurableSubscriptionCount);
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JMSConnectionStats implementation -------

   public JMSProducerStats[] getProducers()
   {
      return mProducers;
   }

   public JMSConsumerStats[] getConsumers()
   {
      return mConsumers;
   }

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

   public CountStatistic getDurableSubscriptionCount()
   {
      return mDurableSubscriptionCount;
   }
}
