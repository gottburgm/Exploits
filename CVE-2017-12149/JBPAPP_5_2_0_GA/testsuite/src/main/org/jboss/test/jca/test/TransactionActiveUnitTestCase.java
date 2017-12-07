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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.jca.interfaces.TransactionActiveHome;
import org.jboss.test.jca.interfaces.TransactionActiveRemote;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * TransactionActiveUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 106016 $
 */
public class TransactionActiveUnitTestCase extends JBossTestCase
{
   public TransactionActiveUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSetup wrapper = new JBossTestSetup(new TestSuite(TransactionActiveUnitTestCase.class))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            redeploy("jca-txactive-ejb.jar");
         }
         protected void tearDown() throws Exception
         {
            undeploy("jca-txactive-ejb.jar");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         
         }
      };
      return wrapper;
   }

   public void testJDBCTransactionActive() throws Exception
   {
      TransactionActiveHome home = (TransactionActiveHome) getInitialContext().lookup("test/ejbs/TxActiveBean");
      TransactionActiveRemote remote = home.create();
      remote.setupDatabase();
      remote.changeDatabase();
      remote.checkDatabase();
   }

   public void testJMSTransactionActive() throws Exception
   {
      TransactionActiveHome home = (TransactionActiveHome) getInitialContext().lookup("test/ejbs/TxActiveBean");
      TransactionActiveRemote remote = home.create();
      remote.setupQueue();
      remote.changeQueue();
      remote.checkQueue();
   }
   
   public void testEmptyRollback() throws Exception
   {
       TransactionActiveHome home = (TransactionActiveHome) getInitialContext().lookup("test/ejbs/TxActiveBean");
       TransactionActiveRemote remote = home.create();
       remote.emptyRollback();
       
       InitialContext ctx = new InitialContext();
       
       ConnectionFactory cf = (ConnectionFactory) ctx.lookup("/ConnectionFactory");
       Connection conn = cf.createConnection();
       conn.start();
       Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
       Queue queueA = (Queue) ctx.lookup("/queue/A");
       MessageConsumer cons = sess.createConsumer(queueA);
       assertNotNull(cons.receive(5000));
       
       conn.close();
   }
}
