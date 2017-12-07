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
package org.jboss.test.invokers.test;

import java.rmi.RemoteException;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.JMSException;
import javax.jms.QueueRequestor;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import javax.jms.ObjectMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.proxy.Interceptor;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.logging.Logger;

/** An example client side InvokerInterceptor used to dynamically override
 * the default InvokerInterceptor coming from the server to install one that
 * routes invocations to either the server side transport layer, or jms
 * depending on the invocation.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class InvokerInterceptor
   extends Interceptor
{
   private static Logger log = Logger.getLogger(InvokerInterceptor.class);
   private transient QueueConnection queConn;
   private transient QueueSession session;
   private transient QueueRequestor requestor;

   /** 
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      Object returnValue = null;
      InvocationContext ctx = invocation.getInvocationContext();
      String methodName = invocation.getMethod().getName();
      if( methodName.equals("doSomethingSlowly") )
      {
         returnValue = sendRecvJMS(invocation);
      }
      else
      {
         // Get the 
         Invoker invoker = ctx.getInvoker();
         returnValue = invoker.invoke(invocation);
         // If this is a remove close the jms connection
         if( methodName.equals("remove") )
         {
            try
            {
               if( requestor != null )
                  requestor.close();
               if( session != null )
                  session.close();
               if( queConn != null )
                  queConn.close();               
            }
            catch(Exception e)
            {
               log.error("Failed to close jms", e);
            }
         }
      }
      return returnValue;
   }

   /** 
    * @param invocation
    * @return The 
    */ 
   private synchronized Object sendRecvJMS(Invocation invocation)
      throws RemoteException
   {
      Object reply = null;
      log.info("sendRecvJMS");
      try
      {
         if( queConn == null )
         {
            InitialContext ctx = null;
               ctx = new InitialContext();
            QueueConnectionFactory qcf = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
            queConn = qcf.createQueueConnection();
            queConn.start();
            session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) ctx.lookup("queue/A");
            requestor = new QueueRequestor(session, queue);
         }
         // Send the invocation via jms
         Message msg = session.createObjectMessage(invocation.getArguments());
         msg.setStringProperty("ejbName", "BusinessSession");
         ObjectMessage replyMsg = (ObjectMessage) requestor.request(msg);
         reply = replyMsg.getObject();
      }
      catch (NamingException e)
      {
         log.error("sendRecvJMS", e);
         throw new RemoteException("sendRecvJMS", e);
      }
      catch (JMSException e)
      {
         log.error("sendRecvJMS", e);
         throw new RemoteException("sendRecvJMS", e);         
      }
      return reply;
   }
}
