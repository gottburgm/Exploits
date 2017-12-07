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
package org.jboss.test.jca.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TxUtils;

/**
 * ExecuteJMSDuringRollbackStressTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 105810 $
 */
public class ExecuteJMSDuringRollbackStressTestCase extends EJBTestCase
{
    protected final Logger log = Logger.getLogger(getClass());

    private static final long WAIT = 1000l;

    private ConnectionFactory cf;

    private Queue queue;

    private CountDownLatch latch;

    private TransactionManager tm;

    private AtomicReference<Transaction> transaction = new AtomicReference<Transaction>();

    public void testExecuteJMSDuringRollback() throws Throwable
    {
        queue = (Queue) new InitialContext().lookup("queue/testQueue");
        cf = (ConnectionFactory) new InitialContext()
                .lookup("java:TestJmsLocal");
        setupQueue();
        // FIXME This test fails after 5 iterations with JBoss Messaging
        for (int i = 0; i < 4; ++i)
        {
            log.info("Running " + getName() + " iteration=" + i);
            latch = new CountDownLatch(2);

            ExecuteJMS executeJMS = new ExecuteJMS();
            Thread thread1 = new Thread(executeJMS);
            thread1.start();

            Rollback rollback = new Rollback();
            Thread thread2 = new Thread(rollback);
            thread2.start();

            thread1.join();
            thread2.join();
            if (executeJMS.error != null)
            {
                log.error(executeJMS.error);
                throw executeJMS.error;
            }
            if (rollback.error != null)
            {
                log.error(rollback.error);
                throw rollback.error;
            }
            checkQueue();
        }
    }

    public class ExecuteJMS extends TestRunnable
    {
        private Connection c;
        private MessageProducer p;
        private Message m;

        public void setup() throws Throwable
        {
            tm.begin();
            transaction.set(tm.getTransaction());

            try
            {
                c = cf.createConnection();
                c.start();
                Session s = c.createSession(true, Session.SESSION_TRANSACTED);
                MessageConsumer r = s.createConsumer(queue);
                r.receive(WAIT);
                r.close();
                p = s.createProducer(queue);
                m = s.createTextMessage("100");
            } catch (Throwable t)
            {
                try
                {
                    if (c != null)
                        c.close();
                } catch (Exception ignored)
                {
                }
                try
                {
                    tm.rollback();
                } catch (Exception ignored)
                {
                    log.warn("Ignored", ignored);
                }
                throw t;
            }
        }

        public void test() throws Throwable
        {
            try
            {
                p.send(m);
            } catch (JMSException expected)
            {
            } finally
            {
                try
                {
                    if (c != null)
                        c.close();
                } catch (Exception ignored)
                {
                }
                try
                {
                    synchronized (transaction)
                    {
                        if (TxUtils.isActive(tm))
                            tm.rollback();
                        else
                            tm.suspend();
                    }
                } catch (Exception ignored)
                {
                }
            }

            tm.begin();
            try
            {
                Connection c = cf.createConnection();
                try
                {
                    c.start();
                    c.createSession(true, Session.SESSION_TRANSACTED);
                } finally
                {
                    try
                    {
                        c.close();
                    } catch (Exception ignored)
                    {
                    }
                }
            } finally
            {
                tm.commit();
            }
        }
    }

    public class Rollback extends TestRunnable
    {
        public void test() throws Throwable
        {
            Transaction tx = transaction.get();
            if (tx != null)
            {
                try
                {
                    synchronized (transaction)
                    {
                        if (TxUtils.isActive(tx))
                            tx.rollback();
                    }
                } catch (Exception ignored)
                {
                }
            }
        }
    }

    protected void setupQueue() throws Throwable
    {
        log.info("setupQueue");
        tm.begin();
        try
        {
            Connection c = cf.createConnection();
            try
            {
                c.start();
                Session s = c.createSession(true, Session.SESSION_TRANSACTED);
                MessageConsumer mc = s.createConsumer(queue);
                while (mc.receive(WAIT) != null)
                    ;
                mc.close();

                MessageProducer p = s.createProducer(queue);
                Message m = s.createTextMessage("101");
                p.send(m);
            } finally
            {
                try
                {
                    c.close();
                } catch (Exception ignored)
                {
                }
            }
        } finally
        {
            tm.commit();
        }
    }

    protected void checkQueue() throws Throwable
    {
        log.info("checking queue");
        tm.begin();
        try
        {
            Connection c = cf.createConnection();
            try
            {
                c.start();
                Session s = c.createSession(true, Session.SESSION_TRANSACTED);
                MessageConsumer mc = s.createConsumer(queue);
                Message m = mc.receive(WAIT);
                if (m == null || m instanceof TextMessage == false)
                    throw new RuntimeException("Expected one text message: "
                            + m);
                String value = ((TextMessage) m).getText();
                if ("101".equals(value) == false)
                    throw new RuntimeException(
                            "Message should have text 101 got: " + value);
                if (mc.receive(WAIT) != null)
                    throw new RuntimeException("Did not expect two messages");
            } catch (Throwable t)
            {
                log.error("Error checking queue", t);
                throw t;
            } finally
            {
                try
                {
                    c.close();
                } catch (Exception ignored)
                {
                }
            }
        } finally
        {
            tm.rollback();
        }
    }

    public class TestRunnable implements Runnable
    {
        public Throwable error;

        public void setup() throws Throwable
        {
        }

        public void test() throws Throwable
        {
        }

        public void run()
        {
            try
            {
                setup();
            } catch (Throwable t)
            {
                error = t;
                latch.countDown();
                return;
            }
            latch.countDown();
            try
            {
                latch.await();
            } catch (InterruptedException e)
            {
                log.warn("Ignored", e);
            }
            try
            {
                test();
            } catch (Throwable t)
            {
                error = t;
            }
        }
    }

    protected void setUp() throws Exception
    {
        tm = TransactionManagerLocator.getInstance().locate();
    }

    public ExecuteJMSDuringRollbackStressTestCase(String name)
    {
        super(name);
    }

    public static Test suite() throws Exception
    {
        final String deployUnit = JMSDestinationsUtil.isJBM()?"jcaexecutejmsrollback.jar":"jcaexecutejmsrollbackHornetQ.jar";
        
        return new JBossTestSetup(new TestSuite(
                ExecuteJMSDuringRollbackStressTestCase.class))
        {
            public void setUp() throws Exception
            {
                super.setUp();
                JMSDestinationsUtil.setupBasicDestinations();
                deploy(deployUnit);

            }

            public void tearDown() throws Exception
            {
                undeploy(deployUnit);
                JMSDestinationsUtil.destroyDestinations();
            }
        };

    }
}
