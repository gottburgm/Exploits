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
package org.jboss.test.jmsra.test;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;

import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

import javax.management.ObjectName;

import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Test cases for JMS Resource Adapter using a <em>Topic</em> . <p>
 *
 * Created: Mon Apr 23 21:35:25 2001
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author    <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version   $Revision: 105321 $
 */
public class RaTopicUnitTestCase
       extends RaTest
{
   private final static String TOPIC_FACTORY = "ConnectionFactory";
   private final static String TOPIC = "topic/testTopic";
   private final static String JNDI = "TxTopicPublisher";

   /**
    * Constructor for the RaTopicUnitTestCase object
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public RaTopicUnitTestCase(String name) throws Exception
   {
      super(name, JNDI);
   }

   /**
    * #Description of the Method
    *
    * @param context        Description of Parameter
    * @exception Exception  Description of Exception
    */
   protected void init(final Context context) throws Exception
   {
      TopicConnectionFactory factory =
            (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);

      connection = factory.createTopicConnection();

      session = ((TopicConnection)connection).createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

      Topic topic = (Topic)context.lookup(TOPIC);

      consumer = ((TopicSession)session).createSubscriber(topic);
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(RaQueueUnitTestCase.class))
         {
            protected void setUp() throws Exception
            {
               super.setUp();
               JMSDestinationsUtil.setupBasicDestinations();
               deploy("jmsra.jar");
            }

             protected void tearDown() throws Exception
             {
                undeploy("jmsra.jar");
                JMSDestinationsUtil.destroyDestinations();
                super.tearDown();
             }
          };
   }


}
