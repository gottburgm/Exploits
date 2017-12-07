/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3.jbpapp2260;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.test.ejb3.common.AbstractReplierMDB;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@MessageDriven(activationConfig = {
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jbpapp2260"),
      @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1"),
      @ActivationConfigProperty(propertyName = "dLQMaxResent", propertyValue = "1"),
      @ActivationConfigProperty(propertyName = "maxMessages", propertyValue = "50") })
public class TransactionMDB extends AbstractReplierMDB implements MessageListener
{
   private static final Logger log = Logger.getLogger(TransactionMDB.class);
   
   // this goes against spec, normally you would use BMT and UserTransaction
   @Resource(mappedName="java:/TransactionManager")
   private TransactionManager tm;
   
   private ConcurrentHashMap<String, AtomicInteger> stats = new ConcurrentHashMap<String, AtomicInteger>();
   
   public void onMessage(Message message)
   {
      try
      {
         log.info(Thread.currentThread() + " " + this + " " + tm.getTransaction());
         // The original plan was to get the tx in a rollback state and then
         // observe the next message failing. This has a drawback that communicating
         // back to the test case is a bit awkward.
//         if(message instanceof TextMessage)
//         {
//            String cmd = ((TextMessage) message).getText();
//            if(cmd.equals("abort"))
//            {
//               log.info("setting tx to rollback only");
//               tm.setRollbackOnly();
//            }
//            else if(cmd.equals("RuntimeException"))
//            {
//               log.info("throwing a RuntimeException");
//               throw new RuntimeException("throw it");
//            }
//         }
         // So instead let's keep track of the transactions.
         String key = tm.getTransaction().toString();
         if(stats.get(key) == null)
            stats.put(key, new AtomicInteger());
         stats.get(key).incrementAndGet();
         // a minimal sleep to make sure we build up some backlog
         sleep(10, MILLISECONDS);
         sendReply(message.getJMSReplyTo(), stats);
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
      catch (SystemException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private void sleep(int duration, TimeUnit unit)
   {
      long millis = MILLISECONDS.convert(duration, unit);
      try
      {
         Thread.sleep(millis);
      }
      catch(InterruptedException e)
      {
         // ignore
      }
   }
}
