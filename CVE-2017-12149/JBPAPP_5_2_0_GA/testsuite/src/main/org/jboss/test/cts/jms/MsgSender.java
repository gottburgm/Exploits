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
package org.jboss.test.cts.jms;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MsgSender
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
  public final static String JMS_FACTORY="ConnectionFactory";
  public final static String QUEUE="queue/testQueue";

  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueSender qsender;
  private TextMessage msg;
  private Queue queue;

  public MsgSender ( )
  {
  }

  /**
   * Create all the necessary objects for receiving
   * messages from a JMS queue.
   */
  public void init(Context ctx, String queueName)
       throws NamingException, JMSException
  {
    qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
    qcon = qconFactory.createQueueConnection();
    qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    try 
    {
      queue = (Queue) ctx.lookup(queueName);
    } 
    catch (NamingException ne) 
    {
      queue = qsession.createQueue(queueName);
      ctx.bind(queueName, queue);
    }
    qcon.start();
  }

  /**
   * Close JMS objects.
   */
  public void close()
       throws JMSException
  {
  	if( qcon != null ) {
    	qsender.close();
    	qsession.close();
    	qcon.close();
		qcon = null;
	}
  }

  public void sendMsg( String message )
  {
    try
    {
	  init( new InitialContext(), QUEUE);
      log.debug("Sending a message.." );
      qsender = qsession.createSender(queue);
      msg = qsession.createTextMessage();
      msg.setText(message);
      qsender.send(msg);
	  close();
    }
    catch(Exception ex)
    {
	ex.printStackTrace( );
    }
  }

  private static InitialContext getInitialContext(String url)
       throws NamingException
  {
      //Hashtable env = new Hashtable();
      //env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
      //env.put(Context.PROVIDER_URL, url);
      //return new InitialContext(env);
      return new InitialContext();
  }

}






