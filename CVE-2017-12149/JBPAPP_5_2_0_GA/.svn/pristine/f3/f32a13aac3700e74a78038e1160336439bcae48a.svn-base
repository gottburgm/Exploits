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
package org.jboss.test.tm.test;

import javax.ejb.EJBTransactionRolledbackException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.tm.ejb.TxTimeoutAnnot;

/**
 * Tests for transaction timeout annotation
 * EJB3 version of TxTimeoutUnitTestCase
 * 
 * JBAS-4011, the arjuna transaction manager does not allow the
 * setting of the global default tx timeout after the tx manager
 * is started, so we won't test the default timeout setting (300secs).
 * 
 * @author adrian@jboss.com
 * @author pskopek@redhat.com
 * @version $Revision: 60502 $
 */
public class TxTimeoutAnnotUnitTestCase
   extends JBossTestCase
{
	
   public TxTimeoutAnnotUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Tests whether @TransactionTimeout expires system sends EJBTransactionRolledbackException 
    * and transaction is rolled back.
    * 
    * @throws Exception
    */
   public void testOverriddenTimeoutExpires() throws Exception
   { 
      TxTimeoutAnnot bean = getBean();
      try
      {
    	 bean.testOverriddenTimeoutExpires();
         fail("Expected TransactionRolledbackException");
      }
      catch (EJBTransactionRolledbackException expected)
      {
    	 log.debug("Expected exception caught - Hurray!");
      }
   }

   /**
    * Tests whether @TransactionTimeout expires greater that wait time of doesn't make transaction to roll back 
    * or stay in different status that STATUS_ACTIVE.
    * 
    * 
    * @throws Exception
    */
   public void testOverriddenTimeoutDoesNotExpire() throws Exception
   {
      TxTimeoutAnnot bean = getBean();
      bean.testOverriddenTimeoutDoesNotExpire();
      log.debug("Done: testOverriddenTimeoutDoesNotExpire");
   }
   
   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(TxTimeoutAnnotUnitTestCase.class, "txtimeoutannottest.jar"));
   }
   
   private TxTimeoutAnnot getBean() throws Exception
   {
      return (TxTimeoutAnnot) getInitialContext().lookup("TxTimeoutAnnotBean/remote");
   }
}
