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
package org.jboss.test.txtimer.test;

import javax.ejb.EJBException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.txtimer.TimerImpl;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimedMockObject implements TimedObject
{
   // logging support
   protected static Logger log = Logger.getLogger(TimedMockObject.class);

   protected static TransactionManager transactionManager = TransactionManagerLocator.getInstance().locate();

   private int callCount;

   public int getCallCount()
   {
      return callCount;
   }

   /**
    * Invoked upon txtimer expiration.
    */
   public void ejbTimeout(Timer timer)
   {
      // this will normally happen in an interceptor
      registerTimerWithTx(timer);

      callCount ++;
      log.debug("ejbTimeout: " + callCount + ",txtimer=" + timer);

      // this will normally happen in an interceptor
      commitTx();
   }

   /**
    * Register the timer with the current Transaction
    */
   private void registerTimerWithTx(Timer timer)
   {
      try
      {
         Transaction tx = transactionManager.getTransaction();
         if (tx == null)
         {
            transactionManager.begin();
            tx = transactionManager.getTransaction();
         }

         TimerImpl txtimer = (TimerImpl) timer;
         tx.registerSynchronization(txtimer);
      }
      catch (Exception e)
      {
         throw new EJBException("Cannot register timer with Tx", e);
      }
   }

   /**
    * Commit the current Transaction
    */
   private void commitTx()
   {
      try
      {
         Transaction tx = transactionManager.getTransaction();
         tx.commit();
      }
      catch (Exception e)
      {
         throw new EJBException("Cannot commit Tx", e);
      }
   }
}
