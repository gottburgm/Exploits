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

import javax.jms.Message;
import javax.naming.Context;

import org.jboss.resource.adapter.jms.inflow.JmsActivation;

/**
 * A DLQ Handler that knows about JBossMQ redelivery properties
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JBossMQDLQHandler extends AbstractDLQHandler
{
   /** Properties copied from org.jboss.mq.SpyMessage */
   protected static final String JMS_JBOSS_REDELIVERY_COUNT = "JMS_JBOSS_REDELIVERY_COUNT";

   /** Properties copied from org.jboss.mq.SpyMessage */
   protected static final String JMS_JBOSS_REDELIVERY_LIMIT = "JMS_JBOSS_REDELIVERY_LIMIT";
   
   /** The maximum number of resends */
   protected int maxResent;

   public void setup(JmsActivation activation, Context ctx) throws Exception
   {
      super.setup(activation, ctx);
      maxResent = activation.getActivationSpec().getDLQMaxResent();
   }
   
   protected boolean handleDelivery(Message msg)
   {
      int max = maxResent;
      try
      {
         if (msg.propertyExists(JMS_JBOSS_REDELIVERY_LIMIT))
            max = msg.getIntProperty(JMS_JBOSS_REDELIVERY_LIMIT);

         if (msg.propertyExists(JMS_JBOSS_REDELIVERY_COUNT))
         {
            int count = msg.getIntProperty(JMS_JBOSS_REDELIVERY_COUNT);
         
            if (count > max)
            {
               warnDLQ(msg, count, max);
               return true;
            }
         }
      }
      catch (Throwable t)
      {
         log.warn("Unexpected error retrieving message properties " + msg, t);
      }
      return false;
   }
}
