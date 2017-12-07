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

import java.io.PrintWriter;
import java.io.StringWriter;
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
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.test.security.interfaces.ProjRepository;
import org.jboss.test.security.interfaces.ProjRepositoryHome;

/** An MDB that invokes several methods on the ProjRepository session bean,
 each of which require a seperate role to test the assignement of multiple
 roles to the run-as identity.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class RunAsWithRolesMDB implements MessageDrivenBean, MessageListener
{
   static Logger log = Logger.getLogger(RunAsWithRolesMDB.class);
   
   private MessageDrivenContext ctx = null;
   private InitialContext iniCtx;
   
   public RunAsWithRolesMDB()
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
      log.error("RunAsWithRolesMDB onMessage enter");
      Queue replyTo = null;
      try
      {
         replyTo = (Queue) message.getJMSReplyTo();
         String name = message.getStringProperty("name");
         ProjRepositoryHome home = (ProjRepositoryHome) iniCtx.lookup("java:comp/env/ejb/ProjRepository");
         NameParser parser = iniCtx.getNameParser("");
         Name projName = parser.parse(name);
         // This requires the ProjectAdmin role
         ProjRepository bean = home.create(projName);
         // This requires CreateFolder role
         Name programs = parser.parse("/Programs Files");
         bean.createFolder(programs);
         // This requires DeleteFolder role
         bean.deleteFolder(programs, true);
         sendReply(replyTo, "Role tests ok");
         // cannot remove because of JBAS-3946
         // bean.remove();
      }
      catch(Throwable e)
      {
         log.error("RunAsWithRolesMDB onMessage:failed", e);
         if( replyTo != null )
         {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sendReply(replyTo, "Failed, ex=\n"+sw.toString());
         }
      }
      log.error("RunAsWithRolesMDB onMessage exit");
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
         log.error("RunAsWithRolesMDB:Failed to send reply", e);
      }
   }
}
