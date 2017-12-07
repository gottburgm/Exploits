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
package org.jboss.test.cmp2.commerce;

import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jboss.test.util.ejb.EJBTestCase;

public class TxTesterTest extends EJBTestCase {
   public static Test suite() {
      TestSuite testSuite = new TestSuite("TxTesterTest");
      testSuite.addTestSuite(TxTesterTest.class);
      return testSuite;
   }   

   public TxTesterTest(String name) {
      super(name);
   }

   private TxTesterHome txTesterHome;

   /**
    * Looks up all of the home interfaces and creates the initial data. 
    * Looking up objects in JNDI is expensive, so it should be done once 
    * and cached.
    * @throws Exception if a problem occures while finding the home interfaces,
    * or if an problem occures while createing the initial data
    */
   public void setUp() throws Exception {
      InitialContext jndi = new InitialContext();

      txTesterHome = 
            (TxTesterHome) jndi.lookup("commerce/TxTester"); 
   }

   public void testTxTester_none() throws Exception {
      TxTester txTester = null;
      try {
         txTester = txTesterHome.create();
         boolean result = txTester.accessCMRCollectionWithoutTx();

         if (!result)
            fail("Expected accessCMRCollectionWithoutTx to throw an exception");
      } finally {
         if(txTester != null) {
            txTester.remove();
         }
      }
   }
}
