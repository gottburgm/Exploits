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
package org.jboss.test.jbossts.crash;

import org.jboss.logging.Logger;
import org.jboss.test.jbossts.recovery.ASFailureSpec;
import org.jboss.test.jbossts.recovery.TestASRecovery;
import org.jboss.test.jbossts.recovery.TestASRecoveryWithJPA;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Transaction;

@Stateless
public class CrashCMTBean implements CrashLocal, CrashRem {
   private static Logger log    = Logger.getLogger(CrashCMTBean.class);

   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public String testXA(String ... args)
   {
      return "Passed";
   }

   
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public String testXA(ASFailureSpec... specs)
   {
      log.info("CMT testXA called with " + specs.length + " specs");

      TestASRecovery xatest = new TestASRecovery();
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
         for (ASFailureSpec spec : specs)
            xatest.addResource(spec);

         xatest.startTest(tx);

         return "Passed";
      }
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public String testXA(String testEntityPK, boolean reverseOrder, ASFailureSpec... specs)
   {
      log.info("CMT testXA called with " + specs.length + " specs and testEntityPK=" + testEntityPK + " reverseOrder=" + reverseOrder);

      TestASRecoveryWithJPA xatest = new TestASRecoveryWithJPA();
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
         xatest.setTestEntityPK(testEntityPK);
         xatest.setReverseOrder(reverseOrder);
         
         for (ASFailureSpec spec : specs)
            xatest.addResource(spec);

         xatest.startTest(tx);

         return "Passed";
      }
   }
   
}
