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
package org.jboss.test.messagedriven.support;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

/**
 * Send a message
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class SendMessageOperation extends Operation
{
   public static final String MESSAGEID = "jboss_test_MESSAGEID";
   
   protected String id;
   
   public SendMessageOperation(BasicMessageDrivenUnitTest test, String id)
   {
      super(test);
      this.id = id;
   }

   public void run() throws Exception
   {
      int retries = 5;
      while (true)
      {
         try
         {
            MessageProducer producer = test.getMessageProducer();
            TextMessage message = test.getTestMessage();
            message.setText(id);
            message.setStringProperty(MESSAGEID, id);
            producer.send(message);
            // DONE
            return;
         }
         catch (JMSException e)
         {
            // Got an error, sleep then retry
            if (retries-- > 0)
               Thread.sleep(1000);
            else
               throw e;
         }
      }
   }
}
