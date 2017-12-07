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
package org.jboss.test.mdbsessionpoolclear.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.jms.JMSTestAdmin;
import org.jboss.test.mdbsessionpoolclear.bean.TestStatus;
import org.jboss.test.mdbsessionpoolclear.bean.TestStatusHome;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.logging.Logger;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/**
 * Sample client for the jboss container.
 * 
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: MDBUnitTestCase.java 105321 2010-05-28 03:04:35Z clebert.suconic@jboss.com $
 */
public class MDBUnitTestCase extends JBossJMSTestCase
{
   private static final Logger log = Logger.getLogger(MDBUnitTestCase.class);

   public MDBUnitTestCase(String name)
   {
      super(name);
   }
   

  
   public void testMdb() throws Exception
   {
      TestStatusHome statusHome = (TestStatusHome) getInitialContext().lookup("TestStatus");
      TestStatus status = statusHome.create();
      status.clear();
      
      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/mdbsessionpoolclearQueue");
      QueueConnectionFactory factory = getQueueConnectionFactory();
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World " + new Date());

      sender = session.createSender(queue);
      sender.send(msg);
      session.close();
      cnn.close();
      
      Thread.sleep(5 * 1000);
      
      assertEquals(1, status.queueFired());
      
      MBeanServerConnection server = getServer();
      
      ObjectName provider = null;
      ObjectName providerQuery = new ObjectName("jboss.j2ee:service=EJB,*");
      Iterator mbeans = server.queryMBeans(providerQuery, null).iterator();
      while (provider == null)
      {
         ObjectInstance providerInstance = (ObjectInstance)mbeans.next();
         String name = providerInstance.getObjectName().toString();
         if (name.contains("Mdb") && !name.contains("plugin"))
            provider = providerInstance.getObjectName();
      }
      
      Object[] params = {};
      String[] sig = {};
      Object success = server.invoke(provider, "stop", params, sig);
      
      ObjectName jmsContainerInvokerQuery = new ObjectName("jboss.j2ee:binding=my-message-driven-bean,*");
      Set mbeansSet = server.queryMBeans(jmsContainerInvokerQuery, null);
      assertEquals(1, mbeansSet.size());
      ObjectInstance jmsContainerInvokerInstance = (ObjectInstance)mbeansSet.iterator().next();
      ObjectName jmsContainerInvoker = jmsContainerInvokerInstance.getObjectName();
      //      int numActiveSessions = (Integer)server.getAttribute(jmsContainerInvoker, "NumActiveSessions");
      //      assertEquals(1, numActiveSessions);
      /*boolean forceClear = (Boolean)server.getAttribute(jmsContainerInvoker, "ForceClearOnShutdown");
      assertFalse(forceClear);
      int forceClearAttempts = (Integer)server.getAttribute(jmsContainerInvoker, "ForceClearAttempts");
      assertEquals(5, forceClearAttempts);
      long forceClearOnShutdownInterval = (Long)server.getAttribute(jmsContainerInvoker, "ForceClearOnShutdownInterval");
      assertEquals(30000, forceClearOnShutdownInterval); */
      
      Thread.sleep(2 * 1000);
      
      success = server.invoke(provider, "start", params, sig);

      Thread.sleep(60 * 1000);
      
      //      numActiveSessions = (Integer)server.getAttribute(jmsContainerInvoker, "NumActiveSessions");
      //      assertEquals(1, numActiveSessions);
      
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      msg = session.createTextMessage("Hello World " + new Date());

      sender = session.createSender(queue);
      
      sender.send(msg);
      session.close();
      cnn.close();
      
      Thread.sleep(10 * 1000);
      
      assertEquals(2, status.queueFired());
   }

   protected QueueConnectionFactory getQueueConnectionFactory()
         throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      } catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
   }

   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext();
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();

      suite.addTest(new MDBUnitTestCase("testMdb"));

      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            MDBUnitTestCase.deployQueue("mdbsessionpoolclearQueue");
            deploy("mdbsessionpoolclear.jar");
         }

         protected void tearDown() throws Exception
         {
            super.tearDown();

            
            JMSDestinationsUtil.purgeQueue("/queue/DLQ");

            try
            {
               undeploy("mdbsessionpoolclear.jar");
            }
            catch (Exception ignored)
            {
               getLog().warn("Unable to undeploy mdbsessionpoolclear.jar", ignored);
            }
            
            MDBUnitTestCase.undeployDestinations();
         }
      };

      return wrapper;
   }
}
