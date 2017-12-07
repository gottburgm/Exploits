/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ejb3.jbpapp6855.unit;

import junit.framework.Test;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.ejb3.jbpapp6855.SimpleMDBWithAUnSupportedActivationConfigProp;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

/**
 * Tests activation config properties on MDBs.
 * <p/>
 * User: Jaikiran Pai
 */
public class MDBActivationConfigPropTestCase extends JBossJMSTestCase
{

   public MDBActivationConfigPropTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MDBActivationConfigPropTestCase.class, "jbpapp-6855.jar");

   }

   /**
    * Tests that a MDB referencing a unsupported activation config property does not run into
    * deployment failures
    *
    * @throws Exception
    * @see https://issues.jboss.org/browse/JBPAPP-6855
    */
   public void testUnsupportedActivationConfigProp() throws Exception
   {
      ConnectionFactory connFactory = null;
      Connection conn = null;
      Session session = null;
      MessageProducer producer = null;
      MessageConsumer consumer = null;
      try
      {
         connFactory = lookup("ConnectionFactory", ConnectionFactory.class);
         conn = connFactory.createConnection();
         conn.start();
         session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
         TemporaryQueue replyQueue = session.createTemporaryQueue();
         // Create and send a message to the queue on which the MDB is listening
         TextMessage msg = session.createTextMessage("Hello world");
         msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
         msg.setJMSReplyTo(replyQueue);
         Queue queue = lookup(SimpleMDBWithAUnSupportedActivationConfigProp.QUEUE_JNDI_NAME, Queue.class);
         producer = session.createProducer(queue);
         // send the message
         producer.send(msg);
         // wait for the reply on the reply queue
         consumer = session.createConsumer(replyQueue);
         Message replyMsg = consumer.receive(5000);
         assertNotNull("No reply received from MDB", replyMsg);
         assertInstanceOf(replyMsg, TextMessage.class);
         String actual = ((TextMessage) replyMsg).getText();
         assertEquals("Unexpected reply from MDB", SimpleMDBWithAUnSupportedActivationConfigProp.REPLY, actual);

      }
      finally
      {
         if (consumer != null)
         {
            consumer.close();
         }
         if (producer != null)
         {
            producer.close();
         }
         if (session != null)
         {
            session.close();
         }
         if (conn != null)
         {
            conn.close();
         }
      }
   }

   protected <T> T lookup(String name, Class<T> cls) throws Exception
   {
      return cls.cast(getInitialContext().lookup(name));
   }

}
