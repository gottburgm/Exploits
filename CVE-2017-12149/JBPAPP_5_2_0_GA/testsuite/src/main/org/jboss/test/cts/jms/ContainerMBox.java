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

import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

public class ContainerMBox
  implements MessageListener
{
  public final static String JMS_FACTORY="ConnectionFactory";
  public final static String QUEUE="queue/testQueue";

  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueReceiver qreceiver;
  private Queue queue;

  private Logger log;

  public static final String EJB_CREATE_MSG = "EJB_CREATE_MSG";
  public static final String EJB_POST_CREATE_MSG = "EJB_POST_CREATE_MSG";
  public static final String EJB_ACTIVATE_MSG = "EJB_ACTIVATE_MSG";
  public static final String EJB_PASSIVATE_MSG = "EJB_PASSIVATE_MSG";
  public static final String EJB_REMOVE_MSG = "EJB_REMOVE_MSG";
  public static final String EJB_LOAD_MSG = "EJB_LOAD_MSG";
  public static final String EJB_STORE_MSG = "EJB_STORE_MSG";
  public static final String SET_ENTITY_CONTEXT_MSG = "SET_ENTITY_CONTEXT_MSG";
  public static final String UNSET_ENTITY_CONTEXT_MSG = "UNSET_ENTITY_CONTEXT_MSG";

  private HashMap messageList = new HashMap( );

  public ContainerMBox ( )
  {
    log = Logger.getLogger(getClass().getName());
    try
    {
       init( new InitialContext(), QUEUE );
    }
    catch(Exception ex)
    {
       log.error("MBox could not get initial context", ex);
    }
  }

  // MessageListener interface
  public void onMessage(Message msg)
  {
    try 
    {
      String msgText;
      if (msg instanceof TextMessage) 
      {
        msgText = ((TextMessage)msg).getText();
      } 
      else 
      {
        msgText = msg.toString();
      }

      log.debug("[BEAN MESSAGE]: "+ msgText );
      messageList.put(msgText, "msg" );
    } 
    catch (JMSException jmse) 
    {
      log.error("problem receiving MBox message", jmse);
    }
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
    qreceiver = qsession.createReceiver(queue);
    qreceiver.setMessageListener(this);
    qcon.start();
  }

  /**
   * Close JMS objects.
   */
  public void close()
       throws JMSException
  {
    qreceiver.close();
    qsession.close();
    qcon.close();
  }

  public boolean messageReceived( String message )
  {
      return messageList.containsKey(message);
  }

  public void clearMessages( )
  {
      messageList = null;
      messageList = new HashMap();
  }

}






