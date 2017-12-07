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
package org.jboss.test.jmsra.bean;

import javax.ejb.*;
import javax.jms.*;
import javax.naming.*;

import org.jboss.test.util.ejb.SessionSupport;

public class JMSSessionBean extends SessionSupport
{
   public void sendToQueueAndTopic()
   {
      try
      {
         InitialContext ctx = new InitialContext();

         QueueConnectionFactory qcf = (QueueConnectionFactory) ctx.lookup("java:/JmsXA");
         Queue q = (Queue) ctx.lookup("queue/testQueue");
         QueueConnection qc = qcf.createQueueConnection();
         QueueSession qs = null;
         try
         {
            qs = qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = qs.createSender(q);
            sender.send(qs.createMessage());
         }
         finally
         {
            if (qs != null)
               qs.close();
            if (qc != null)
               qc.close();
         }

         TopicConnectionFactory tcf = (TopicConnectionFactory) ctx.lookup("java:/JmsXA");
         Topic t = (Topic) ctx.lookup("topic/testTopic");
         TopicConnection tc = tcf.createTopicConnection();
         TopicSession ts = null;
         try
         {
            ts = tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
            TopicPublisher publisher = ts.createPublisher(t);
            publisher.publish(ts.createMessage());
         }
         finally
         {
            if (ts != null)
               ts.close();
            if (tc != null)
               tc.close();
         }
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
}