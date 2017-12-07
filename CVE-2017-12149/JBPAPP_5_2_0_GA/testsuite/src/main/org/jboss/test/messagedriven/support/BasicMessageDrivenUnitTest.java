/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.messagedriven.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.NamingException;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.naming.Util;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.jms.JMSTestAdmin;
import org.jboss.test.messagedriven.mbeans.TestMessageDrivenManagementMBean;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Basic tests of message driven beans 
 *
 * @author <a href="mailto:adrian@jboss.com>Adrian Brock</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public abstract class BasicMessageDrivenUnitTest extends JBossJMSTestCase implements ExceptionListener
{
   protected static final long WAIT_TIME = 5000L;
   protected static final long REPEATED_WAIT = 4;

   protected static ObjectName ejbParsingDeployer = ObjectNameFactory.create("jboss.ejb:service=EjbParsingDeployer");

   protected static ObjectName testQueue = JMSTestAdmin.getAdmin().createQueueJMXName("testQueue");
   protected static Properties testQueueProps = new Properties();
   protected static Properties testQueueNoDestinationTypeProps = new Properties();
   
   protected static ObjectName testTopic = JMSTestAdmin.getAdmin().createTopicJMXName("testTopic");
   protected static Properties testTopicProps = new Properties();
   protected static Properties testTopicNoDestinationTypeProps = new Properties();
   
   protected static ObjectName testDurableTopic = JMSTestAdmin.getAdmin().createTopicJMXName("testDurableTopic");
   protected static Properties testDurableTopicProps = new Properties();

   protected static ObjectName dlqJMXDestination = JMSTestAdmin.getAdmin().createQueueJMXName("DLQ");
    
   static
   {
      testQueueProps.put("destination", "testQueue");
      testQueueProps.put("destinationType", "javax.jms.Queue");

      testQueueNoDestinationTypeProps.put("destination", "testQueue");
      testQueueNoDestinationTypeProps.put("destinationType", "");

      testTopicProps.put("destination", "testTopic");
      testTopicProps.put("destinationType", "javax.jms.Topic");

      testTopicNoDestinationTypeProps.put("destination", "testTopic");
      testTopicNoDestinationTypeProps.put("destinationType", "");

      testDurableTopicProps.put("destination", "testDurableTopic");
      testDurableTopicProps.put("destinationType", "javax.jms.Topic");
      
      if (JMSDestinationsUtil.isHornetQ())
      {
          testDurableTopicProps.put("clientID", "DurableSubscriberExample");
      }
      testDurableTopicProps.put("durability", "Durable");
      testDurableTopicProps.put("subscriptionName", "messagedriven");
      testDurableTopicProps.put("user", "john");
      testDurableTopicProps.put("password", "needle");
   }
   
   protected Thread thread;
   protected boolean running = false;

   protected String mdbjar = JMSDestinationsUtil.isJBM() ? "testmessagedriven.jar" : "testmessagedriven-hornetq.jar"; 
   protected String mbeansar = "testmessagedriven.sar"; 

   protected ObjectName jmxDestination = ObjectNameFactory.create("does:not=exist"); 
   protected String connectionFactoryJNDI = "ConnectionFactory";
   protected Destination destination;
   protected Destination dlqDestination;
   protected Properties defaultProps;
   protected Properties props;

   protected Connection connection;
   protected Session session;
   protected HashMap producers = new HashMap();
   protected ArrayList<Object[]> messages = new ArrayList<Object[]>(); 

   public BasicMessageDrivenUnitTest(String name, ObjectName jmxDestination, Properties defaultProps)
   {
      super(name);
      this.jmxDestination = jmxDestination;
      this.defaultProps = defaultProps;
   }
   
   public void runTest(Operation[] ops, Properties props) throws Exception
   {
      startTest(props);
      try
      {
         for (int i = 0; i < ops.length; ++i)
         {
            try
            {
                  ops[i].run();
            }
            catch (Throwable e)
            {
               log.warn(e.getMessage(), e);
               throw new RuntimeException("Failure at operation " + i, e);
            }
         }
      }
      finally
      {
         stopTest();
      }
   }

   public String getMDBDeployment()
   {
      return mdbjar;
   }
   
   public ObjectName getJMXDestination()
   {
      return jmxDestination;
   }
   
   public ObjectName getDLQJMXDestination()
   {
      return dlqJMXDestination;
   }
   
   public Destination getDestination() throws Exception
   {
      if (destination != null)
         return destination;

      String name = (String)getAttribute(getJMXDestination(), "Name");
      
      destination = lookupDestination("/queue/" + name);
      
      if (destination == null)
      {
    	  destination = lookupDestination("/topic/" + name);
      }

      if (destination == null)
      {
    	  destination = lookupDestination(name);
      }
      
      if (destination == null)
      {
    	  throw new NamingException("Can't find destination name " + name);
      }

      return destination;
   }
   
   private Destination lookupDestination(String jndi)
   {
	   try
	   {
		   return (Destination) lookup(jndi, Destination.class);   
	   }
	   catch (Exception e)
	   {
		   return null;
	   }
   }
   
   public Destination getDLQDestination() throws Exception
   {
      if (dlqDestination != null)
         return dlqDestination;
      String jndiName = "/queue/" + getAttribute(getDLQJMXDestination(), "Name");
      dlqDestination = (Destination) lookup(jndiName, Destination.class);
      return dlqDestination;
   }
   
   public MessageProducer getMessageProducer() throws Exception
   {
      return getMessageProducer(getDestination());
   }
   
   public MessageProducer getMessageProducer(Destination destination) throws Exception
   {
      MessageProducer producer = (MessageProducer) producers.get(destination);
      if (producer == null)
         producer = getSession().createProducer(destination);
      return producer;
   }

   public Session getSession() throws Exception
   {
      if (session != null)
         return session;
      
      return getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
   }
   
   public Connection getConnection() throws Exception
   {
      if (connection != null)
         return connection;
      
      ConnectionFactory factory = (ConnectionFactory) lookup(connectionFactoryJNDI, ConnectionFactory.class);
      connection = factory.createConnection();
      connection.setExceptionListener(this);
      return connection;
   }
   
   public Connection getConnection(String user, String password, String clientID) throws Exception
   {
      if (connection != null)
         return connection;

      ConnectionFactory factory = (ConnectionFactory) lookup(connectionFactoryJNDI, ConnectionFactory.class);
      connection = factory.createConnection(user, password);
      if (clientID != null)
      {
         connection.setClientID(clientID);
      }
      connection.setExceptionListener(this);
      return connection;
   }
   
   
//   public Connection getConnection(String user, String password) throws Exception
//   {
//      return getConnection(user, password, null);
//   }
//   
   public void onException(JMSException e)
   {
      log.debug("Notified of error", e);
      Connection temp = connection;
      connection = null;
      try
      {
         if (temp != null)
            temp.close();
      }
      catch (JMSException ignored)
      {
         log.debug("Ignored ", ignored);
      }
   }
   
   public TextMessage getTestMessage() throws Exception
   {
      return getSession().createTextMessage();
   }
   
   protected void deployDestinations() throws Exception
   {
	   JMSDestinationsUtil.destroyDestinations();
      JMSDestinationsUtil.setupBasicDestinations();
   }
   
   protected void setUp() throws Exception
   {
	  super.setUp();
	  if (isDeployDestinations())
	     deployDestinations();
      try
      {
         deploy(mbeansar);
      }
      catch (Exception e)
      {
         if (isDeployDestinations())
            undeployDestinations();
         throw e;
      }
   }

   protected void tearDown() throws Exception
   {
      try
      {
         undeploy(mbeansar);
      }
      catch (Throwable t)
      {
         getLog().error("Error undeploying: " + mbeansar, t);
      }
      if (isDeployDestinations())
         JMSDestinationsUtil.destroyDestinations();
      super.tearDown();
   }
   
   protected boolean isDeployDestinations()
   {
      return true;
   }
   
   protected boolean isClearDestination()
   {
      return true;
   }
   
   protected void startTest(Properties props) throws Exception
   {
      this.props = props;
       
      if (isClearDestination())
         clearMessages(getJMXDestination());
      clearMessages(getDLQJMXDestination());
      tidyup(props);
      initProperties(props);
      
      deploy(getMDBDeployment());

      try
      {
//         // FIXME Need to wait for asynchrounous bootstrap of container
         Thread.sleep(5000);
         startReceiverThread();
      }
      catch (Exception e)
      {
         undeploy(getMDBDeployment());
         throw e;
      }
    }

   protected void stopTest()
   {
      if (connection != null)
      {
         try
         {
            connection.close();
         }
         catch (Exception ignored)
         {
         }
         connection = null;
      }
      stopReceiverThread();
      try
      {
         undeploy(getMDBDeployment());
      }
      catch (Throwable t)
      {
         getLog().error("Error undeploying: " + getMDBDeployment(), t);
      }
      try
      {
         if (isClearDestination())
            clearMessages(getJMXDestination());
         tidyup(props);
      }
      catch (Throwable t)
      {
         getLog().error("Error clearing messages: " + getJMXDestination(), t);
      }
      try
      {
         clearMessages(getDLQJMXDestination());
      }
      catch (Throwable t)
      {
         getLog().error("Error clearing messages: " + getDLQJMXDestination(), t);
      }
      
      try
      {
         JMSDestinationsUtil.destroyDestinations();
      }
      catch (Throwable t)
      {
         getLog().error("Error Destroying Queues", t);
      }
      
   }
   
   protected void clearMessages(ObjectName name) throws Exception
   {
      if (name != null)
      {
         getLog().info("Clearing messages " + name);
         try
         {
            getServer().invoke(name, "removeMessages", new Object[]{""}, new String[]{String.class.getName()});
         }
         catch (Throwable e)
         {
            // This is a work-around for https://jira.jboss.org/jira/browse/HORNETQ-376
            // It has been fixed on hornetQ. This line could be removed we upgraded HornetQ from 2.1.0.Beta3
            log.warn("Couldn't remove messages from " + name, e);
         }
      }
   }
   
   protected void tidyup(Properties props) throws Exception
   {
      String name = props.getProperty("subscriptionName");
      if (name != null)
      {
         String user = props.getProperty("user");

         String clientID = props.getProperty("clientID");
         
         log.info("ClientID = " + clientID + " on tidyUP");
         
         if (user != null)
         {
             
            log.info("Getting connection with clientID");
            String password = props.getProperty("password");
            getConnection(user, password, clientID);
         }
         else
            getConnection();
         try
         {
            
            
            Session session = getSession();
            try
            {
               session.unsubscribe(name);
            }
            catch (Throwable t)
            {
               log.warn("Unsubscribe failed: ", t);
            }
         }
         finally
         {
            try
            {
               connection.close();
            }
            catch (Exception ignored)
            {
            }
            connection = null;
         }
      }
   }
   
   protected void activate(ObjectName name) throws Exception
   {
      getServer().invoke(name, "startDelivery", new Object[0], new String[0]);
   }
   
   protected void deactivate(ObjectName name) throws Exception
   {
      getServer().invoke(name, "stopDelivery", new Object[0], new String[0]);
   }
   
   protected void start(ObjectName name) throws Exception
   {
      getServer().invoke(name, "create", new Object[0], new String[0]);
      getServer().invoke(name, "start", new Object[0], new String[0]);
   }
   
   protected void stop(ObjectName name) throws Exception
   {
      getServer().invoke(name, "stop", new Object[0], new String[0]);
      getServer().invoke(name, "destroy", new Object[0], new String[0]);
   }
   
   protected void initProperties(Properties props) throws Exception
   {
      getLog().info("Init properties " + props);
      getServer().invoke(TestMessageDrivenManagementMBean.OBJECT_NAME, "initProperties", new Object[] { props }, new String[] { Properties.class.getName() });
   }
   
   protected void waitMessages(int expected, long wait) throws Exception
   {
      synchronized (this)
      {
         if (wait != 0)
            wait(wait);
         
         for (int i = 0; i < REPEATED_WAIT && messages.size() < expected; ++i)
            wait(WAIT_TIME);
      }
   }
   
   protected ArrayList<Object[]> getMessages() throws Exception
   {
      synchronized (this)
      {
         return new ArrayList<Object[]>(messages);
      }
   }
   
   protected void startReceiverThread()
   {
      synchronized (this)
      {
         thread = new Thread(new ReceiverRunnable(), getClass().getName());
         thread.start();
         running = true;
      }
   }
   
   protected void stopReceiverThread()
   {
      synchronized (this)
      {
         running = false;
         while (thread != null)
         {
            try
            {
               this.notifyAll();
               this.wait();
            }
            catch (Throwable t)
            {
               getLog().error("Error waiting for receiver thread to stop " + thread, t);
            }
         }
      }
   }

   protected Object getAttribute(ObjectName name, String attribute) throws Exception
   {
      return getServer().getAttribute(name, attribute);
   }
   
   protected Object lookup(String jndiName, Class clazz) throws Exception
   {
      return Util.lookup(getInitialContext(), jndiName, clazz);
   }
   
   public class ReceiverRunnable implements Runnable
   {
      public void run()
      {
         try
         {
            while (true)
            {
               ArrayList result = (ArrayList) getAttribute(TestMessageDrivenManagementMBean.OBJECT_NAME, "Messages");
               log.info("Trying to get more results "  + result.size());
               synchronized (BasicMessageDrivenUnitTest.this)
               {
                  if (running == false)
                     break;
                  if (result.size() > 0)
                  {
                     messages.addAll(result);
                     BasicMessageDrivenUnitTest.this.notifyAll();
                  }
                  BasicMessageDrivenUnitTest.this.wait(WAIT_TIME);
               }
            }
         }
         catch (Throwable t)
         {
            getLog().error("Error in receiver thread " + thread, t);
         }
         
         synchronized (BasicMessageDrivenUnitTest.this)
         {
            thread = null;
            BasicMessageDrivenUnitTest.this.notifyAll();
         }
      }
   }
}
