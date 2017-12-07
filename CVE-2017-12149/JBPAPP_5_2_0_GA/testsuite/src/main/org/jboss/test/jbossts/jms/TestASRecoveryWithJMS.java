/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossts.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.transaction.Transaction;

import org.jboss.logging.Logger;
import org.jboss.test.jbossts.recovery.TestASRecovery;


/**
 * Extends TestASRecovery class with adding JMS.
 */
public class TestASRecoveryWithJMS extends TestASRecovery
{
   private static Logger log = Logger.getLogger(TestASRecoveryWithJMS.class);

   private String connectionFactoryJNDIName = "java:/JmsXA";
   private String message = null;


   /**
    * Enlists JMS into the active transaction.
    */
   @Override
   protected boolean addTxResources(Transaction tx)
   {
      if (super.addTxResources(tx))
      {
         return sendMessage(message);
      }

      return false;
   }

   public boolean sendMessage(String message)
   {
      log.info("sending a message...");

      InitialContext ic = null;
      Connection conn = null;
      try
      {
         ic = new InitialContext();

         ConnectionFactory connectionFactory = (ConnectionFactory) ic.lookup(connectionFactoryJNDIName);
         Queue testQueue = (Queue) ic.lookup("queue/crashRecoveryQueue");

         conn = connectionFactory.createConnection();
         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(testQueue);

         producer.send(session.createTextMessage(message));
         log.info("message sent");

         return true;
      }
      catch (Exception e)
      {
         log.error(e);
         return false;
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (JMSException e)
            {
               log.warn(e);
            }
         }
      }
   }

   /**
    * Sets a message content.
    * 
    * @param message
    */
   public void setMessage(String message)
   {
      this.message = message;
   }

   public void setConnectionFactoryJNDIName(String connectionFactoryJNDIName) 
   {
      this.connectionFactoryJNDIName = connectionFactoryJNDIName;
   }

}
