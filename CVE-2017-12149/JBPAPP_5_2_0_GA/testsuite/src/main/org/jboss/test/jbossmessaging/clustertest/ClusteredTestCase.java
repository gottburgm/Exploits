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
package org.jboss.test.jbossmessaging.clustertest;

import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

import org.jboss.jms.client.FailoverEvent;
import org.jboss.jms.client.FailoverListener;
import org.jboss.jms.client.JBossConnection;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.JBossTestClusteredServices;
import org.jboss.test.JBossTestClusteredSetup;
import org.jboss.test.JBossTestSetup;
import org.jboss.util.id.GUID;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

import junit.framework.Test;

/**
 * 
 * Each failover test would require a whole shutdown cycle, what means.. start, run test, kill, assert.. (for each test).
 * Instead of doing that we run tests in parallel, using this little framework defined by ParallelTestContainer and assert all of them in a single server kill.
 * Also: System.exit(-1); wasn't enough to perform a server crash, as it was causing a regular shutdown, so we are using some reflection to call java.lang.Shutdown#halt.
 * @author <a href="clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 */
public class ClusteredTestCase extends ParallelTestContainer
{
    private static final Logger log = Logger.getLogger(ClusteredTestCase.class);
    
    CountDownLatch latchCluster = new CountDownLatch(1);
    CountDownLatch latchFailover = new CountDownLatch(getTests().length);
    CountDownLatch latchServerAlreadyKilled = new CountDownLatch(1);

    public ParallelTest[] getTests()
    {
        return new ParallelTest[]
        { new ClusterTestSimple(latchCluster), 
            new FailoverTestSimple(latchFailover, latchServerAlreadyKilled),
            new FailoverTestSimpleTransacted("testFailoverTransactedKillOnCommit", true, latchFailover, latchServerAlreadyKilled, "testDistributedQueueC"),
            new FailoverTestSimpleTransacted("testFailoverTransactedKillOnMessageReceive", false, latchFailover, latchServerAlreadyKilled, "testDistributedQueueD"),
            new FailoverTestSessionWithOneTransactedPersistentMessageFailover(latchFailover, latchServerAlreadyKilled),
            new FailoverSessionWithOneTransactedNonPersistentMessageFailover(latchFailover, latchServerAlreadyKilled)};
    }

    public void doTest() throws Exception
    {
        JBossTestClusteredServices testServices = new JBossTestClusteredServices(ClusteredTestCase.class);
        testServices.setUp();
        
        
        
        MBeanServerConnection rmi = testServices.getAdaptor(1);
        
        ObjectName name = new ObjectName("test:name=JBMKillService");
        if (testServices.getServerCount() != 2)
        {
            throw new Exception ("This test requires 2 servers but it got with " + testServices.getServerCount());
        }
        
        latchCluster.await();
        
        log.info("Clustered tests have finished");
        
        latchFailover.await(10, java.util.concurrent.TimeUnit.SECONDS);

        if (latchCluster.getCount() != 0)
        {
        	throw new IllegalStateException ("Test didn't finish properly");
        }
        
        log.info("Ready to kill");

        rmi.invoke(name, "kill", new Integer[]{1000}, new String[]{"int"});
        
        Thread.sleep(1000);
        
        log.info("Server already killed");
        latchServerAlreadyKilled.countDown();
        
    }


    
    protected static int getServerId(Connection conn)
    {
        return ((JBossConnection) conn).getServerID();
    }

    protected Connection createConnectionOnServer(ConnectionFactory factory,
            int serverId) throws Exception
    {
        int count = 0;

        while (true)
        {
            if (count++ > 10)
                throw new IllegalStateException(
                        "Cannot make connection to node " + serverId);

            Connection connection = factory.createConnection();

            if (getServerId(connection) == serverId)
            {
                return connection;
            } else
            {
                connection.close();
            }
        }
    }
    
    abstract class FailoverTest extends ParallelTest
    {
        Context ctx;
        ConnectionFactory cf;
        CountDownLatch latch;
        CountDownLatch latchAlreadyKilled;

        public FailoverTest(String name, CountDownLatch latch, CountDownLatch latchAlreadyKilled)
        {
            super(name);
            this.latch = latch;
            this.latchAlreadyKilled = latchAlreadyKilled;
        }

        public void setUp() throws Exception
        {
            super.setUp();
            ctx = new InitialContext();
            cf = (ConnectionFactory) ctx.lookup("/ClusteredConnectionFactory");
        }

        public abstract void runTest() throws Throwable;
        
    }

    class FailoverTestSimple extends FailoverTest
    {


        public FailoverTestSimple(CountDownLatch latch, CountDownLatch latchAlreadyKilled)
        {
            super("testSimpleFailover", latch, latchAlreadyKilled);
        }

        public void setUp() throws Exception
        {
            super.setUp();
        }

        public void tearDown() throws Exception
        {
            super.tearDown();
        }

        public void runTest() throws Exception
        {

            boolean latchCalled = false;
            Connection conn0 = null;
            try
            {
                conn0 = createConnectionOnServer(cf, 1);
                Queue queue = (Queue)ctx.lookup("/queue/testDistributedQueueA");
                
                
                
                Session session0 = conn0.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session0.createConsumer(queue);
                MessageProducer producer = session0.createProducer(queue);
                conn0.start();
                
                String strmsg = "Hello from " + this.getName() + " guid="+ new GUID().toString();

                producer.send(session0.createTextMessage(strmsg));
                
                latchCalled = true;
                latch.countDown();
                latchAlreadyKilled.await();
                
                TextMessage msg = (TextMessage)consumer.receive(50000);
                assertNotNull("message not received", msg);
                assertEquals(strmsg, msg.getText());
                
            } 
            finally
            {
                try
                {
                    if (conn0 != null)
                    {
                        conn0.close();
                    }
                }
                catch (Throwable ignored)
                {
                    // It should never happen, but better to send it to System.out than just ignore it
                    ignored.printStackTrace();
                }

                if (!latchCalled) 
                {
                    latch.countDown();
                }
            
            }
        }

    }
    
    class FailoverTestSimpleTransacted extends FailoverTest
    {

        String jndiQueue;
        boolean killOnTransaction;

        public FailoverTestSimpleTransacted(String name, boolean killOnTransaction, CountDownLatch latch, CountDownLatch latchAlreadyKilled, String jndiQueue)
        {
            super(name, latch, latchAlreadyKilled);
            this.killOnTransaction = killOnTransaction;
            this.jndiQueue = jndiQueue;
        }

        public void setUp() throws Exception
        {
            super.setUp();
        }

        public void tearDown() throws Exception
        {
            super.tearDown();
        }

        public void runTest() throws Exception
        {

            boolean latchCalled = false;
            Connection conn0 = null;
            try
            {
                conn0 = createConnectionOnServer(cf, 1);
                Queue queue = (Queue)ctx.lookup("/queue/" + jndiQueue);
                
                Session session0 = conn0.createSession(true, Session.CLIENT_ACKNOWLEDGE);
                MessageConsumer consumer = session0.createConsumer(queue);
                MessageProducer producer = session0.createProducer(queue);
                conn0.start();
                
                String strmsg = "Hello from " + this.getName() + " guid="+ new GUID().toString();

                producer.send(session0.createTextMessage(strmsg));
                session0.commit();
                

                if (!killOnTransaction)
                {
                    latchCalled = true;
                    latch.countDown();
                    latchAlreadyKilled.await();
                    
                    log.info("After kill on (kill on receive)");
                }
                
                TextMessage msg = (TextMessage)consumer.receive(50000);
                
                msg.acknowledge();
                
                if (killOnTransaction)
                {
                    latchCalled = true;
                    latch.countDown();
                    latchAlreadyKilled.await();
                    log.info("After kill on (kill on commit)");
                }
                
                session0.commit();
                
                assertNotNull("message not received", msg);
                assertEquals(strmsg, msg.getText());
                
                msg =  (TextMessage)consumer.receive(1000);
                assertNull("Message Queue should be empty by now!", msg);
                
            } 
            finally
            {
                try
                {
                    if (conn0 != null)
                    {
                        conn0.close();
                    }
                }
                catch (Throwable ignored)
                {
                    // It should never happen, but better to send it to System.out than just ignore it
                    ignored.printStackTrace();
                }

                if (!latchCalled)
                {
                    latch.countDown();
                }
            }
        }

    }

    class ClusterTestSimple extends ParallelTest
    {

        Context ctx;
        ConnectionFactory cf;
        CountDownLatch latch;

        public ClusterTestSimple(CountDownLatch latch)
        {
            super("testSimpleCluster");

            this.latch = latch;
        }

        public void setUp() throws Exception
        {
            ctx = new InitialContext();
            cf = (ConnectionFactory) ctx.lookup("/ClusteredConnectionFactory");
        }

        public void tearDown() throws Exception
        {
            super.tearDown();
        }

        public void runTest() throws Exception
        {

            Connection conn0 = null, conn1 = null;
            try
            {
                conn0 = createConnectionOnServer(cf, 0);
                conn1 = createConnectionOnServer(cf, 1);
                Queue queue = (Queue)ctx.lookup("/queue/testDistributedQueueB");
                
                Session session1 = conn1.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session1.createConsumer(queue);
                conn1.start();
                
                
                Session session0 = conn0.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session0.createProducer(queue);
                
                String strmsg = "Hello from " + this.getName() + " guid="+ new GUID().toString();
                
                producer.send(session0.createTextMessage(strmsg));
                
                
                TextMessage msg = (TextMessage)consumer.receive(50000);
                assertNotNull("message not received", msg);
                assertEquals(strmsg, msg.getText());
                
            } 
            finally
            {
                try
                {
                    if (conn0 != null)
                    {
                        conn0.close();
                    }
                }
                catch (Throwable ignored)
                {
                    // It should never happen, but better to send it to System.out than just ignore it
                    ignored.printStackTrace();
                }
                try
                {
                    if (conn1 != null)
                    {
                        conn1.close();
                    }
                }
                catch (Throwable ignored)
                {
                    // It should never happen, but better to send it to System.out than just ignore it
                    ignored.printStackTrace();
                }
                
                latch.countDown();
            }
        }

    }
    
    class FailoverTestSessionWithOneTransactedPersistentMessageFailover extends FailoverTest
    {

        public FailoverTestSessionWithOneTransactedPersistentMessageFailover(
                CountDownLatch latch,
                CountDownLatch latchAlreadyKilled)
        {
            super("testSessionWithOneTransactedPersistentMessageFailover", latch, latchAlreadyKilled);
        }
        
        public void setUp() throws Exception
        {
            super.setUp();
        }
        
        public void runTest() throws Throwable
        {
            boolean latchAlreadyCalled = false;
            Connection conn = null;

            Queue queue = (Queue) ctx.lookup("/queue/testDistributedQueueE");
            
            try
            {
               conn = createConnectionOnServer(cf, 1);

               conn.start();

               Session session = conn.createSession(true, Session.SESSION_TRANSACTED);

               // send 2 transacted messages (one persistent and one non-persistent) but don't commit
               MessageProducer prod = session.createProducer(queue);

               prod.setDeliveryMode(DeliveryMode.PERSISTENT);
               prod.send(session.createTextMessage("clik-persistent"));

               // close the producer
               prod.close();

               log.info("producer closed");

               // create a consumer on the same local queue (creating a consumer AFTER failover will end
               // up getting messages from a local queue, not a failed over queue; at least until
               // redistribution is implemented.

               Session session2 = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
               MessageConsumer cons = session2.createConsumer(queue);

               // register a failover listener
               SimpleFailoverListener failoverListener = new SimpleFailoverListener();
               ((JBossConnection)conn).registerFailoverListener(failoverListener);

               latchAlreadyCalled = true;
               latch.countDown();
               latchAlreadyKilled.await();

               // wait for the client-side failover to complete

               while(true)
               {
                  FailoverEvent event = failoverListener.getEvent(50000);
                  if (event != null && FailoverEvent.FAILOVER_COMPLETED == event.getType())
                  {
                     break;
                  }
                  if (event == null)
                  {
                     fail("Did not get expected FAILOVER_COMPLETED event");
                  }
               }

               // failover complete
               assertEquals(0, getServerId(conn));

               // commit the failed-over session
               session.commit();

               // make sure messages made it to the queue

               TextMessage tm = (TextMessage)cons.receive(2000);
               assertNotNull(tm);
               assertEquals("clik-persistent", tm.getText());
            }
            finally
            {
               if (!latchAlreadyCalled)
               {
                   latch.countDown();
               }
               if (conn != null)
               {
                  conn.close();
               }
            }
        }
        
    }
    
    class FailoverSessionWithOneTransactedNonPersistentMessageFailover extends FailoverTest
    {

        public FailoverSessionWithOneTransactedNonPersistentMessageFailover(
                CountDownLatch latch,
                CountDownLatch latchAlreadyKilled)
        {
            super("testSessionWithOneTransactedNonPersistentMessageFailover", latch, latchAlreadyKilled);
        }
        
        public void setUp() throws Exception
        {
            super.setUp();
        }
        
        public void runTest() throws Throwable
        {
            Connection conn = null;
            
            boolean latchCalled = false;
            
            Queue queue = (Queue) ctx.lookup("queue/testDistributedQueueF");

            try
            {
               conn = createConnectionOnServer(cf, 1);

               conn.start();

               Session session = conn.createSession(true, Session.SESSION_TRANSACTED);

               MessageProducer prod = session.createProducer(queue);

               prod.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
               prod.send(session.createTextMessage("clik-non-persistent"));

               // close the producer
               prod.close();

               // create a consumer on the same local queue (creating a consumer AFTER failover will end
               // up getting messages from a local queue, not a failed over queue; at least until
               // redistribution is implemented.

               Session session2 = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
               MessageConsumer cons = session2.createConsumer(queue);

               // register a failover listener
               SimpleFailoverListener failoverListener = new SimpleFailoverListener();
               ((JBossConnection)conn).registerFailoverListener(failoverListener);

               latchCalled = true;
               latch.countDown();
               latchAlreadyKilled.await();

               // wait for the client-side failover to complete

               while(true)
               {
                  FailoverEvent event = failoverListener.getEvent(30000);
                  if (event != null && FailoverEvent.FAILOVER_COMPLETED == event.getType())
                  {
                     break;
                  }
                  if (event == null)
                  {
                     fail("Did not get expected FAILOVER_COMPLETED event");
                  }
               }

               // failover complete

               assertEquals(0, getServerId(conn));

               // commit the failed-over session
               session.commit();

               // make sure messages made it to the queue

               TextMessage tm = (TextMessage)cons.receive(2000);
               assertNotNull(tm);
               assertEquals("clik-non-persistent", tm.getText());
            }
            finally
            {
               if (!latchCalled)
               {
                   latch.countDown();
               }
               if (conn != null)
               {
                  conn.close();
               }
            }
        }
    }
    
    
    public static Test suite() throws Exception
    {
        ClassLoader loader = ClusteredTestCase.class.getClassLoader(); 

        final String destinations = loader.getResource("jbossmessaging/test-clustered-destinations-full-service.xml").toString();
        final String jarNames = destinations + "," + "jbm-killservice.sar";
        final Test test = new ClusteredTestCase();
        
        
        JBossTestSetup wrapper = new JBossTestClusteredSetup(test, jarNames)
        {

           // Since the server will be killed, we can't undeploy using the regular ClusteredSetup, or we would get a false error
           protected void tearDown() throws Exception
           {
              
              if (jarNames != null)
              {
                 JBossTestClusteredServices clusteredDelegate = (JBossTestClusteredServices) delegate;
                 
                 // deploy the comma seperated list of jars
                 StringTokenizer st = new StringTokenizer(jarNames, ", ");
                 String[] depoyments = new String[st.countTokens()];
                 for (int i = depoyments.length - 1; i >= 0; i--)
                    depoyments[i] = st.nextToken();
                 for (int i = 0; i < depoyments.length; i++)
                 {
                    String jarName = depoyments[i];
                    this.getLog().debug("Attempt undeploy of " + jarName);
                    clusteredDelegate.undeploy(clusteredDelegate.getAdaptor(0), jarName);
                    this.getLog().debug("undeployed package: " + jarName);
                 }            
              }
              
              JBossTestClusteredServices testServices = (JBossTestClusteredServices) delegate;
              try
              {
                 testServices.getAdaptor(1).invoke(new ObjectName("jboss.system:type=Server"), "shutdown", new Object[]{}, new String[]{});
                 log.info("Shut down jbm-cluster2");
              }
              catch (Exception ignored)
              {
                 // expected if the test worked and shut down a server
                 log.info("Could not shut down jbm-cluster2; probably already killed -- " + ignored.getMessage());
              }
           }
        };
        return wrapper;
        
    }
    
    
    // Inner classes --------------------------------------------------------------------------------
    
    protected class SimpleFailoverListener implements FailoverListener
    {
       private LinkedQueue buffer;

       public SimpleFailoverListener()
       {
          buffer = new LinkedQueue();
       }

       public void failoverEventOccured(FailoverEvent event)
       {
          try
          {
             buffer.put(event);
          }
          catch(InterruptedException e)
          {
             throw new RuntimeException("Putting thread interrupted while trying to add event " +
                "to buffer", e);
          }
       }

       /**
        * Blocks until a FailoverEvent is available or timeout occurs, in which case returns null.
        */
       public FailoverEvent getEvent(long timeout) throws InterruptedException
       {
          return (FailoverEvent)buffer.poll(timeout);
       }
    }
    
}
