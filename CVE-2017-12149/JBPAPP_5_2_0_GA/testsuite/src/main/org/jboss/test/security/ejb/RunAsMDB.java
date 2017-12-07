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
package org.jboss.test.security.ejb;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.security.interfaces.Entity;
import org.jboss.test.security.interfaces.EntityHome;

/** An MDB that takes the string from the msg passed to onMessage
 and invokes the echo(String) method on an internal Entity using
 the InternalRole assigned in the MDB descriptor run-as element.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class RunAsMDB implements MessageDrivenBean, MessageListener
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
   private MessageDrivenContext ctx = null;
   private InitialContext iniCtx;
   
   public RunAsMDB()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
      throws EJBException
   {
      this.ctx = ctx;
      try
      {
         iniCtx = new InitialContext();
      }
      catch(NamingException e)
      {
         throw new EJBException(e);
      }
   }
   
   public void ejbCreate()
   {
   }
   
   public void ejbRemove()
   {
      ctx = null;
   }

   public void onMessage(Message message)
   {
      log.error("RunAsMDB enter onMessage");
      Queue replyTo = null;
      try
      {
         replyTo = (Queue) message.getJMSReplyTo();
         String arg = message.getStringProperty("arg");
         EntityHome home = (EntityHome) iniCtx.lookup("java:comp/env/ejb/Entity");
         Entity bean = home.findByPrimaryKey(arg);
         String echo = bean.echo(arg);
         log.info("RunAsMDB echo("+arg+") -> "+echo);
         sendReply(replyTo, arg);
      }
      catch(Throwable e)
      {
         log.error("RunAsMDB onMessage failed", e);
         if( replyTo != null )
            sendReply(replyTo, "Failed, ex="+e.getMessage());
      }
      log.error("RunAsMDB onMessage Success");
   }
   private void sendReply(Queue replyTo, String info)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         QueueConnectionFactory queueFactory = (QueueConnectionFactory) ctx.lookup("java:comp/env/jms/QueFactory");
         QueueConnection queueConn = queueFactory.createQueueConnection();
         QueueSession session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         Message msg = session.createMessage();
         msg.setStringProperty("reply", info);
         QueueSender sender = session.createSender(replyTo);
         sender.send(msg);
         sender.close();
         session.close();
         queueConn.close();
         log.info("Sent reply");
      }
      catch(Exception e)
      {
         log.error("RunAsMDB:Failed to send reply", e);
      }
   }
}
