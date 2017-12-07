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
package org.jboss.test.jbossmessaging.ra;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;


/**
 * Test cases for JMS Resource Adapter use a <em>Queue</em> . <p>
 *
 * Created: Mon Apr 23 21:35:25 2001
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author    <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version   $Revision: 105321 $
 */
public class RaQueueUnitTestCase
       extends RaTest
{
   private final static String QUEUE_FACTORY = "ConnectionFactory";
   private final static String QUEUE = "queue/testQueue";
   private final static String JNDI = "TxPublisher";

   /**
    * Constructor for the RaQueueUnitTestCase object
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public RaQueueUnitTestCase(String name) throws Exception
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
      QueueConnectionFactory factory =
            (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);

      connection = factory.createQueueConnection();

      session = ((QueueConnection)connection).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      Queue queue = (Queue)context.lookup(QUEUE);

      consumer = ((QueueSession)session).createReceiver(queue);
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
