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
package org.jboss.test.ejb3.jbas6239.unit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Make sure the run-as on a MDB is picked up.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 105789 $
 */
public class RunAsMDBUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      TestSetup wrapper = new JBossTestSetup(new TestSuite(RunAsMDBUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            if (JMSDestinationsUtil.isHornetQ())
            {
            	redeploy("jbas6239hornetq.jar");
            }
            else
            {
            	redeploy("jbas6239.jar");
            }
         }
         protected void tearDown() throws Exception
         {
             if (JMSDestinationsUtil.isHornetQ())
             {
                undeploy("jbas6239hornetq.jar");
             }
             else
             {
                 undeploy("jbas6239.jar");
             }
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         
         }
      };
      return wrapper;
      
   }

   public RunAsMDBUnitTestCase(String name)
   {
      super(name);
   }

   protected <T> T lookup(String name, Class<T> cls) throws Exception
   {
      return cls.cast(getInitialContext().lookup(name));
   }
   
   public void testSendMessage() throws Exception
   {
      ConnectionFactory connFactory = lookup("ConnectionFactory", ConnectionFactory.class);
      Connection conn = connFactory.createConnection();
      conn.start();
      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TemporaryQueue replyQueue = session.createTemporaryQueue();
      TextMessage msg = session.createTextMessage("Hello world");
      msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      msg.setJMSReplyTo(replyQueue);
      Queue queue = lookup("queue/mdbtest", Queue.class);
      MessageProducer producer = session.createProducer(queue);
      producer.send(msg);
      MessageConsumer consumer = session.createConsumer(replyQueue);
      Message replyMsg = consumer.receive(5000);
      assertNotNull(replyMsg);
      if(replyMsg instanceof ObjectMessage)
      {
         Exception e = (Exception) ((ObjectMessage) replyMsg).getObject();
         throw e;
      }
      assertInstanceOf(replyMsg, TextMessage.class);
      String actual = ((TextMessage) replyMsg).getText();
      assertEquals("SUCCESS", actual);
      
      // TODO: check stateless.state
      
      consumer.close();
      producer.close();
      session.close();
      conn.stop();
   }
   
   public void testServerFound() throws Exception
   {
      serverFound();
   }
}
