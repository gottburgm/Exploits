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
package org.jboss.resource.adapter.jms.inflow;

import javax.jms.Message;
import javax.naming.Context;

/**
 * An interface for DLQ Handling
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public interface DLQHandler
{
   /** JMS property name holding original destination. */
   static final String JBOSS_ORIG_DESTINATION = "JBOSS_ORIG_DESTINATION";

   /** JMS property name holding original JMS message id. */
   static final String JBOSS_ORIG_MESSAGEID = "JBOSS_ORIG_MESSAGEID";

   /** Standard property for delivery count */
   static final String PROPERTY_DELIVERY_COUNT = "JMSXDeliveryCount";

   /**
    * Set up the DLQ
    * 
    * @param activation the activation
    * @param ctx the naming context
    * @throws Exception for any error
    */
   void setup(JmsActivation activation, Context ctx) throws Exception;

   /**
    * Tear down the DLQ
    */
   void teardown();
   
   /**
    * Check whether the DLQ should handle the message
    * 
    * @param msg the message about to be delivered
    * @return true if the message is handled and should not be delivered
    */
   boolean handleRedeliveredMessage(Message msg);
   
   /**
    * Notification that the message was delivered
    * 
    * @param msg the message that was delivered
    */
   void messageDelivered(Message msg);
}
