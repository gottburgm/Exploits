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
package org.jboss.test.jbossmessaging.perf;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;

import org.jboss.test.JBossJMSTestCase;

/**
 * Reconnect stress
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author
 * @version
 */

public class JMSReconnectStressTestCase extends JBossJMSTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";

   public JMSReconnectStressTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testReconnectStress() throws Throwable
   {
      InitialContext ctx = new InitialContext();
      QueueConnectionFactory qcf = (QueueConnectionFactory) ctx.lookup(QUEUE_FACTORY);
      
      ReconnectThread[] threads = new ReconnectThread[getThreadCount()];
      for (int i = 0; i < threads.length; ++i)
         threads[i] = new ReconnectThread(qcf, "Reconnect-"+i);
      for (int i = 0; i < threads.length; ++i)
         threads[i].start();
      for (int i = 0; i < threads.length; ++i)
         threads[i].join();
      for (int i = 0; i < threads.length; ++i)
      {
         if (threads[i].error != null)
            throw threads[i].error;
      }
   }
   
   public class ReconnectThread extends Thread
   {
      public Throwable error;
      public QueueConnectionFactory qcf;
      
      public ReconnectThread(QueueConnectionFactory qcf, String name)
      {
         super(name);
         this.qcf = qcf;
      }
      
      public void run()
      {
         QueueConnection c = null;
         try
         {
            for (int i = 0; i < getIterationCount(); ++i)
            {
               log.info(Thread.currentThread() + " connect " + i);
               c = qcf.createQueueConnection();
               log.info(Thread.currentThread() + " close " + i);
               c.close();
               c = null;
            }
         }
         catch (Throwable t)
         {
            if (c != null)
            {
               try
               {
                  c.close();
               }
               catch (Throwable ignored)
               {
                  log.warn("Ignored: ", ignored);
               }
            }
         }
      }
   }
}
