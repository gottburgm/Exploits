/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jms.inflow.dlq;

import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A Generic DLQ Handler
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class GenericDLQHandler extends JBossMQDLQHandler
{
   /** Resent messages Map<MessageID, Integer> */
   protected HashMap resent = new HashMap();

   public void messageDelivered(Message msg)
   {
      try
      {
         String id = msg.getJMSMessageID();
         if (id == null)
         {
            log.error("Message id is null? " + msg);
            return;
         }

         clearResentCounter(id);
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error processing delivery notification " + msg, t);
      }
   }

   protected boolean handleDelivery(Message msg)
   {
      // Check for JBossMQ specific
      boolean handled = super.handleDelivery(msg);
      if (handled)
         return true;
      
      try
      {
         if (msg.propertyExists(JMS_JBOSS_REDELIVERY_COUNT))
            return false;

         String id = msg.getJMSMessageID();
         if (id == null)
         {
            log.error("Message id is null? " + msg);
            return false;
         }

         int count = 0;
         
         try
         {
            if (msg.propertyExists(PROPERTY_DELIVERY_COUNT))
               count = msg.getIntProperty(PROPERTY_DELIVERY_COUNT) - 1;
         }
         catch (JMSException ignored)
         {
            count = incrementResentCounter(id);
         }
         
         int max = maxResent;
         if (msg.propertyExists(JMS_JBOSS_REDELIVERY_LIMIT))
            max = msg.getIntProperty(JMS_JBOSS_REDELIVERY_LIMIT);
         
         if (count > max)
         {
            warnDLQ(msg, count, max);
            clearResentCounter(id);
            return true;
         }
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error checking whether dlq should be used " + msg, t);
      }
      
      return false;
   }
   
   /**
    * Increment the resent counter for the message id
    * 
    * @param id the message id of the message
    */
   protected int incrementResentCounter(String id)
   {
      ResentInfo info;
      synchronized (resent)
      {
         info = (ResentInfo) resent.get(id);
         if (info == null)
         {
            info = new ResentInfo();
            resent.put(id, info);
         }
      }
      return ++info.count;
   }
   
   /**
    * Remove the resent counter for the message id
    * 
    * @param id the message id of the message
    */
   protected void clearResentCounter(String id)
   {
      synchronized (resent)
      {
         resent.remove(id);
      }
   }
   
   /**
    * Resent Info
    */
   protected static class ResentInfo
   {
      int count = 0;
   }
}
