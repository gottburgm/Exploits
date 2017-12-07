/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.test.jbossts.jms;

import org.jboss.logging.Logger;
import org.jboss.test.jbossts.recovery.ASFailureSpec;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Transaction;

@Stateless
public class JMSCrashCMTBean implements JMSCrashRem {
   private static Logger log    = Logger.getLogger(JMSCrashCMTBean.class);

   
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public String testXA(String connectionFactoryJNDIName, String message, boolean reverseOrder, ASFailureSpec... specs)
   {
      log.info("CMT testXA called with " + specs.length + " specs and message=" + message + " and reverseOrder=" + reverseOrder);

      TestASRecoveryWithJMS xatest = new TestASRecoveryWithJMS();
      Transaction tx;

      try
      {
         tx = com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
      }
      catch (javax.transaction.SystemException e)
      {
         tx = null;
      }

      if (tx == null)
      {
         log.error("CMT testXA called without a transaction");

         return "Failed";
      }
      else
      {
         xatest.setReverseOrder(reverseOrder);
         xatest.setMessage(message);
         xatest.setConnectionFactoryJNDIName(connectionFactoryJNDIName);
         
         for (ASFailureSpec spec : specs)
            xatest.addResource(spec);

         return xatest.startTest(tx) ? "Passed" : "Failed";
      }
   }
}
