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
package org.jboss.test.timer.ejb;

import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/** An MDB that obtains the TimerService during the ejbCreate callback. A
 * timer is created in onMessage using this TimerService. It delays for 1
 * seconds and continues once a second for 10 seconds.
 * 
 * @ejb.bean name="OnCreateTimerMDB"
 * description="OnCreateTimerMDB unit test bean"
 * destination-type="javax.jms.Queue"
 * acknowledge-mode="Auto-acknowledge" 
 * @ejb.resource-ref res-ref-name="jms/QCF" res-type="javax.jms.QueueConnectionFactory" res-auth="Container"
 * @jboss.destination-jndi-name name="queue/QueueC"
 * @jboss.resource-ref res-ref-name="jms/QCF" jndi-name="ConnectionFactory"
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class OnCreateTimerMessageBean implements MessageDrivenBean, MessageListener, TimedObject
{
   private static Logger log = Logger.getLogger(OnCreateTimerMessageBean.class);
   private MessageDrivenContext messageContext = null;
   private QueueConnection qc = null;
   private InitialContext ctx = null;
   private TimerService ts;

   static class ReplyInfo implements Serializable
   {
      private int msgID;
      private Queue replyTo;
      private Date first;
      private Date last;
      ReplyInfo(int msgID, Queue replyTo, Date first, Date last)
      {
         this.msgID = msgID;
         this.replyTo = replyTo;
         this.first = first;
         this.last = last;
      }
      boolean cancel(Date next)
      {
         return last.compareTo(next) < 0;
      }
      long getElapsed()
      {
         return System.currentTimeMillis() - first.getTime();
      }
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
      throws EJBException
   {
      messageContext = ctx;
   }

   public void ejbCreate()
   {
      try
      {
         ctx = new InitialContext();
         QueueConnectionFactory qcf = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
         qc = qcf.createQueueConnection();
         ts = messageContext.getTimerService();
      }
      catch (Exception e)
      {
         log.error("Failed to init timer", e);
         throw new EJBException("ejbCreate failed", e);
      }
   }

   public void ejbTimeout(Timer timer)
   {
      log.info("ejbTimeout(), timer: " + timer);
      ReplyInfo info = (ReplyInfo) timer.getInfo();
      Date next = timer.getNextTimeout();
      if( info.cancel(next) )
      {
         log.info("Cancelling timer");
         timer.cancel();
      }

      try
      {
         long elapsed = info.getElapsed();
         sendReply("ejbTimeout", info.msgID, elapsed, info.replyTo);
      }
      catch(Exception e)
      {
         log.error("Failed to send timer msg", e);
      }
   }

   public void ejbRemove() throws EJBException
   {
      try
      {
         qc.close();
         log.info("QueueConnection is closed.");
      }
      catch (JMSException e)
      {
         log.error("Failed to close connection", e);
      }
   }

   public void onMessage(Message message)
   {
      try
      {
         TextMessage msg = (TextMessage) message;
         log.info("onMessage() called, msg="+msg);
         int msgID = msg.getIntProperty("UNIQUE_ID");
         Queue replyTo = (Queue) message.getJMSReplyTo();
         sendReply("onMessage", msgID, 0, replyTo);
         // Start the reply timer
         this.initTimer(msgID, replyTo);         
      }
      catch (Exception e)
      {
         log.error("onMessage failure", e);
      }
   }

   public void initTimer(int msgID, Queue replyTo)
   {
      try
      {
         Date first = new Date(System.currentTimeMillis() + 1000);
         Date last = new Date(System.currentTimeMillis() + 11000);
         ReplyInfo info = new ReplyInfo(msgID, replyTo, first, last);
         Timer timer = ts.createTimer(first, 1000, info);
         log.info("Timer created with a timeout: " + first
            + " and with info: " + msgID
            + ", handle: "+timer.getHandle());
      }
      catch (Exception e)
      {
         log.info("Failed to init timer", e);
      }
      return;
   }

   private void sendReply(String msg, int msgID, long elapsed, Queue dest)
      throws JMSException
   {
      QueueSession qs = null;
      QueueSender sender = null;
      try
      {
         qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         sender = qs.createSender(dest);
         TextMessage reply = qs.createTextMessage();
         reply.setText(msg + " : " + msgID);
         reply.setIntProperty("UNIQUE_ID", msgID);
         reply.setLongProperty("Elapsed", elapsed);
         sender.send(reply, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 180000);
         log.info("Message sent");
      }
      finally
      {
         if (sender != null)
         {
            try
            {
               sender.close();
               log.info("QueueSender Closed");
            }
            catch (JMSException e)
            {
               log.error("Failed to close queue sender", e);
            }
         }
         if (qs != null)
         {
            try
            {
               qs.close();
               log.info("QueueSession Closed");
            }
            catch (JMSException e)
            {
               log.error("Failed to close queue session", e);
            }
         }
      }
   }
}
