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
package org.jboss.embedded.test.mdb.unit;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.DeploymentGroup;
import org.jboss.embedded.test.mdb.ExampleMDB;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class MdbTestCase extends TestCase
{
   public MdbTestCase()
   {
      super("BootstrapTestCase");
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(MdbTestCase.class);

      // setup test so that embedded JBoss is started/stopped once for all tests here.
      TestSetup wrapper = new TestSetup(suite)
      {
         protected void setUp()
         {
            long start = System.currentTimeMillis();
            try
            {
               startupEmbeddedJboss();
            }
            finally
            {
               System.out.println("Bootstrap took " + (System.currentTimeMillis() - start) + " (ms)");
            }

         }

         protected void tearDown()
         {
            shutdownEmbeddedJboss();
         }
      };

      return wrapper;
   }

   public static void startupEmbeddedJboss()
   {
      try
      {
         Bootstrap.getInstance().bootstrap();
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Failed to bootstrap", e);
      }
   }

   public static void shutdownEmbeddedJboss()
   {
      Bootstrap.getInstance().shutdown();
   }

   public void testSimpleEjb() throws Exception
   {
      sendMessage();
   }

   private static void sendMessage()
           throws DeploymentException, NamingException, JMSException, InterruptedException
   {
      DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();
      //group.addResource("mdbtest-service.xml");
      group.addClasspath("mdb-test.jar");
      group.process();

      ExampleMDB.executed = false;

      InitialContext ctx = new InitialContext();
      ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
      Destination destination = (Destination) ctx.lookup("queue/example");
      assertNotNull(destination);
      Connection conn = factory.createConnection();
      Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TextMessage message = session.createTextMessage("hello");
      MessageProducer producer = session.createProducer(destination);
      producer.send(message);
      session.close();
      conn.close();
      Thread.sleep(5000);
      assertTrue(ExampleMDB.executed);
   }

}
