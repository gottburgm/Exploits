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
package org.jboss.test.cts.test;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.NotSupportedException;
import javax.transaction.UserTransaction;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.CtsCmpHome;
import org.jboss.test.cts.interfaces.CtsCmp;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Tests of accessing the UserTransaction interface.
 *
 *  @author alex@jboss.org
 *  @version $Revision: 57211 $
 */
public class NestedUserTransactionTestCase extends JBossTestCase
{

   public NestedUserTransactionTestCase(String name)
   {
      super(name);
   }

   public void testWithDefaultJndiContext() throws Exception
   {
      System.setProperty(Context.PROVIDER_URL, getServerHost() + ":1099");
      InitialContext ctx = new InitialContext();
      UserTransaction ut = (UserTransaction) ctx.lookup("UserTransaction");

      ut.begin();
      try
      {
         ut.begin();
         fail("Attempt to start a nested user transaction should fail with NotSupportedException.");
      }
      catch(NotSupportedException e)
      {
         // expected
      }
      finally
      {
         ut.rollback();
      }
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(NestedUserTransactionTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("cts.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("cts.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
      
   }
}
