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
package javax.management.j2ee.statistics;

/**
 * Specifies the statistics provided by a JMS session.
 * 
 * @author thomas.diesler@jboss.org
 */
public interface JMSSessionStats extends Stats
{
   /**
    * Returns an array of JMSProducerStats that provide statistics about the message producers associated with the referencing JMS session statistics.
    */
   public JMSProducerStats[] getProducers();

   /**
    * Returns an array of JMSConsumerStats that provide statistics about the message consumers associated with the referencing JMS session statistics.
    */
   public JMSConsumerStats[] getConsumers();

   /**
    * Number of messages exchanged.
    */
   public CountStatistic getMessageCount();

   /**
    * Number of pending messages.
    */
   public CountStatistic getPendingMessageCount();

   /**
    * Number of expired messages.
    */
   public CountStatistic getExpiredMessageCount();

   /**
    * Time spent by a message before being delivered.
    */
   public TimeStatistic getMessageWaitTime();


   /**
    * Number of durable subscriptions.
    */
   public CountStatistic getDurableSubscriptionCount();
}

