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
import javax.management.j2ee.statistics.JMSProducerStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * Represents a statistics provided by a JMS message producer
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public final class JMSProducerStatsImpl
        extends JMSEndpointStatsImpl
        implements JMSProducerStats
{
   // Constants -----------------------------------------------------

   /** @since 4.0.2 */
   private static final long serialVersionUID = 2471566045202131110L;
   
   // Attributes ----------------------------------------------------

   private String mDestination;

   // Constructors --------------------------------------------------

   public JMSProducerStatsImpl(String pDestination,
                               CountStatistic pMessageCount,
                               CountStatistic pPendingMessageCount,
                               CountStatistic pExpiredMessageCount,
                               TimeStatistic pMessageWaitTime)
   {
      super(pMessageCount, pPendingMessageCount, pExpiredMessageCount, pMessageWaitTime);
      mDestination = pDestination;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JMSProducerStats implementation ---------

   public String getDestination()
   {
      return mDestination;
   }
}
