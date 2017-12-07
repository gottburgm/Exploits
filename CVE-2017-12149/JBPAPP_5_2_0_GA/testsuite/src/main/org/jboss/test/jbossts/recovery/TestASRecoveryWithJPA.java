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
package org.jboss.test.jbossts.recovery;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;

import org.jboss.test.jbossts.crash.TestEntityHelper;
import org.jboss.test.jbossts.crash.TestEntityHelperLocal;

/**
 * Extends TestASRecovery class by adding EJB3 entity update.
 */
public class TestASRecoveryWithJPA extends TestASRecovery
{
   private String   testEntityPK = null;

   /**
    * Primary key of test entity.
    * Typically depends on the name of a particular test.
    * 
    * @param testEntityPK
    */
   public void setTestEntityPK(String testEntityPK)
   {
      this.testEntityPK = testEntityPK;
   }

   /**
    * Calls update of a test entity.
    */
   @Override
   protected boolean addTxResources(Transaction tx)
   {
      if (super.addTxResources(tx))
      {
         TestEntityHelperLocal testEntityHelper;
         try
         {
            testEntityHelper = (TestEntityHelperLocal) new InitialContext().lookup(TestEntityHelper.LOCAL_JNDI_NAME);

            return testEntityHelper.updateTestEntity(testEntityPK);
         }
         catch (NamingException e)
         {
            e.printStackTrace();
         }
      }

      return false;
   }

}
