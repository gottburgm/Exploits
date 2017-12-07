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
package org.jboss.test.cts.ejb;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.QueueConnectionFactory;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import org.jboss.logging.Logger;

/** An MDB that validates that no more than maxActiveCount MDB instances
 are active in the onMessage method.

@author Scott.Stark@jboss.org
@version $Revision: 105321 $
 */
public class StrictlyPooledMDB implements MessageDrivenBean, MessageListener
{
   private static Logger log = Logger.getLogger(StrictlyPooledMDB.class);
   /** The class wide max count of instances allows */
   private static int maxActiveCount = 5;
   /** The class wide count of instances active in business code */
   private static int activeCount;

   private MessageDrivenContext ctx = null;
   private QueueConnection queConn;
   private QueueSession session;
   private QueueSender sender;

   private static synchronized int incActiveCount()
   {
      return activeCount ++;
   }
   private static synchronized int decActiveCount()
   {
      return activeCount --;
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
      throws EJBException
   {
      this.ctx = ctx;
      try
      {
         InitialContext iniCtx = new InitialContext();
         Integer i = (Integer) iniCtx.lookup("java:comp/env/maxActiveCount");
         maxActiveCount = i.intValue();
         QueueConnectionFactory factory = (QueueConnectionFactory) iniCtx.lookup("java:/ConnectionFactory");
         queConn = factory.createQueueConnection();
         session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue) iniCtx.lookup("queue/B");
         sender = session.createSender(queue);
      }
      catch(Exception e)
      {
         log.error("Setup failure", e);
         throw new EJBException("Setup failure", e);
      }
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
      try
      {
         if( sender != null )
            sender.close();
         if( session != null )
            session.close();
         if( queConn != null )
            queConn.close();
      }
      catch(Exception e)
      {
         log.error("Failed to close JMS resources", e);
      }
   }

   public void onMessage(Message message)
   {
      int count = incActiveCount();

      for (int i = 0 ; i < 20; i++) log.debug("Begin onMessage, activeCount="+count+", ctx="+ctx);
      try
      {
         Message reply = null;
         if( count > maxActiveCount )
         {
            String msg = "IllegalState, activeCount > maxActiveCount, "
                  + count + " > " + maxActiveCount;
            // Send an exception
            Exception e = new IllegalStateException(msg);
            reply = session.createObjectMessage(e);
         }
         else
         {
            TextMessage tm = (TextMessage) message;
            // Send an ack
            reply = session.createTextMessage("Recevied msg="+tm.getText());
         }
         Thread.currentThread().sleep(1000);
         sender.send(reply);
      }
      catch(JMSException e)
      {
         log.error("Failed to send error message", e);
      }
      catch(InterruptedException e)
      {
      }
      finally
      {
         count = decActiveCount();
         log.debug("End onMessage, activeCount="+count+", ctx="+ctx);
      }
   }
}
