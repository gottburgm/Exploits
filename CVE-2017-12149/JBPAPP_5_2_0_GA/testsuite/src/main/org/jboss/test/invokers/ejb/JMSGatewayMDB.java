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
package org.jboss.test.invokers.ejb;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.invokers.interfaces.BusinessObjectLocal;
import org.jboss.test.invokers.interfaces.BusinessObjectLocalHome;

/** An MDB that acts a an async 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class JMSGatewayMDB implements MessageDrivenBean, MessageListener
{
   static Logger log = Logger.getLogger(JMSGatewayMDB.class);
   private MessageDrivenContext ctx = null;
   private QueueConnection queConn;
   private QueueSession session;
   private Context enc;

   public void setMessageDrivenContext(MessageDrivenContext ctx)
   {
      this.ctx = ctx;
      try
      {
         InitialContext iniCtx = new InitialContext();
         enc = (Context) iniCtx.lookup("java:comp/env");
         QueueConnectionFactory factory = (QueueConnectionFactory) enc.lookup("jms/ConnectionFactory");
         queConn = factory.createQueueConnection();
         session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
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
   public void ejbRemove() throws EJBException
   {
      try
      {
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

   /**
    * 
    * @param message
    */ 
   public void onMessage(Message message)
   {
      log.info("onMessage, msg="+message);
      try
      {
         ObjectMessage objMsg = (ObjectMessage) message;
         Queue replyTo = (Queue) message.getJMSReplyTo();
         String ejbName = message.getStringProperty("ejbName");
         Object[] args = (Object[]) objMsg.getObject();
         Object ref = enc.lookup("ejb/"+ejbName);
         log.info("ejb/"+ejbName+" = "+ref);
         BusinessObjectLocalHome home = (BusinessObjectLocalHome) ref;
         BusinessObjectLocal bean = home.create();
         String reply = bean.doSomethingSlowly(args[0], (String) args[1]);
         reply = reply + "viaJMSGatewayMDB";
         sendReply(reply, replyTo);
      }
      catch(Exception e)
      {
         log.error("onMessage failure", e);
      }
   }

   private void sendReply(String reply, Queue replyTo) throws JMSException
   {
      QueueSender sender = session.createSender(replyTo);
      Message replyMsg = session.createObjectMessage(reply);
      sender.send(replyMsg);
   }
}
