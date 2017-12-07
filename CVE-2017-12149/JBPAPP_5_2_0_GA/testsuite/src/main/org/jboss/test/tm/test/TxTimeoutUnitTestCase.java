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

import javax.transaction.TransactionRolledbackException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.tm.interfaces.TxTimeout;
import org.jboss.test.tm.interfaces.TxTimeoutHome;

/**
 * Tests for transaction timeout
 * 
 * JBAS-4011, the arjuna transaction manager does not allow the
 * setting of the global default tx timeout after the tx manager
 * is started, so we won't test the default timeout setting (300secs).
 * 
 * @author adrian@jboss.com
 * @version $Revision: 60502 $
 */
public class TxTimeoutUnitTestCase
   extends JBossTestCase
{
/*
   private Integer oldTimeout;
   private ObjectName tmService = ObjectNameFactory.create("jboss:service=TransactionManager");
*/   
   public TxTimeoutUnitTestCase(String name)
   {
      super(name);
   }
/*
   public void testDefaultTimeout() throws Exception
   {
      TxTimeout bean = getBean();
      try
      {
         bean.testDefaultTimeout();
         fail("Expected TransactionRolledbackException");
      }
      catch (TransactionRolledbackException expected)
      {
      }
   }
*/
   public void testOverriddenTimeoutExpires() throws Exception
   {
      TxTimeout bean = getBean();
      try
      {
         bean.testOverriddenTimeoutExpires();
         fail("Expected TransactionRolledbackException");
      }
      catch (TransactionRolledbackException expected)
      {
      }
   }

   public void testOverriddenTimeoutDoesNotExpire() throws Exception
   {
      TxTimeout bean = getBean();
      bean.testOverriddenTimeoutDoesNotExpire();
   }
   
   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(TxTimeoutUnitTestCase.class, "txtimeouttest.jar"));
   }
/*
   protected void setUp() throws Exception
   {
      setTxTimeout(new Integer(10));
   }
   
   protected void tearDown() throws Exception
   {
      setTxTimeout(oldTimeout);
   }
   
   private void setTxTimeout(Integer timeout) throws Exception
   {
      oldTimeout = (Integer) getServer().getAttribute(tmService, "TransactionTimeout");
      getServer().setAttribute(tmService, new Attribute("TransactionTimeout", timeout));
   }
*/   
   private TxTimeout getBean() throws Exception
   {
      TxTimeoutHome home = (TxTimeoutHome) getInitialContext().lookup("jbosstest/tm/TxTimeout");
      return home.create();
   }
}
