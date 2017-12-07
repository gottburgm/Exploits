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
package org.jboss.test.cluster.defaultcfg.ejb3.ustxsticky.test;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jboss.ha.framework.interfaces.GenericClusteringException;
import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.ejb3.ustxsticky.UserTransactionSticky;

/**
 * UserTransactionStickyTestCase.
 *
 * @author Paul Ferraro
 */
public class UserTransactionStickyUnitTestCase extends JBossClusteredTestCase
{
   private static final String deployment = "ustxsticky-ejb3.jar";

   private static final Logger log = Logger.getLogger(UserTransactionStickyUnitTestCase.class);

   public UserTransactionStickyUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(UserTransactionStickyUnitTestCase.class, deployment);
   }

   public void testSeveralTransactionalStickyCalls() throws Exception
   {
      test(1);
   }

   public void testMoreBeansInTransaction() throws Exception
   {
      test(3);
   }

   private void test(int beanCount) throws Exception
   {
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[0]);
      Context ctx = new InitialContext(env1);

      UserTransactionSticky[] beans = new UserTransactionSticky[beanCount];
      // Validate non-tx behavior
      for (int i = 0; i < beanCount; ++i)
      {
         beans[i] = (UserTransactionSticky) ctx.lookup("ejb3/UserTransactionSticky");
         try
         {
            beans[i].test(-1);
            beans[i].test(-1);
         }
         catch (Exception e)
         {
            e.printStackTrace(System.err);
            fail("Bean[" + i + "]");
         }
      }

      javax.transaction.UserTransaction tx = (javax.transaction.UserTransaction) ctx.lookup("UserTransaction");
      tx.begin();
      for (int i = 0; i < beanCount; ++i)
      {
         // Stick tx to a node
         beans[i] = (UserTransactionSticky) ctx.lookup("ejb3/UserTransactionSticky");
         try
         {
            beans[i].test(-1);
            beans[i].test(-1);
         }
         catch (Exception e)
         {
            e.printStackTrace(System.err);
            fail("Bean[" + i + "]");
         }
         try
         {
            // This would normally trigger failover, except tx stickiness should prevent this
            beans[i].test(GenericClusteringException.COMPLETED_NO);
            fail("Bean[" + i + "]");
         }
         catch (Exception e)
         {
            assertTrue("Bean[" + i + "] " + e.getMessage(), e.getMessage().startsWith("Current transaction is stuck to"));
         }
         try
         {
            // This would normally failover, except tx stickiness should prevent this
            beans[i].test(-1);
            fail("Bean[" + i + "]");
         }
         catch (Exception e)
         {
            assertTrue("Bean[" + i + "] " + e.getMessage(), e.getMessage().startsWith("Current transaction is stuck to"));
         }
      }
      tx.rollback();

      // Not in tx anymore
      for (int i = 0; i < beanCount; ++i)
      {
         beans[i] = (UserTransactionSticky) ctx.lookup("ejb3/UserTransactionSticky");
         try
         {
            beans[i].test(-1);
            beans[i].test(-1);
         }
         catch (Exception e)
         {
            e.printStackTrace(System.err);
            fail("Bean[" + i + "]");
         }
         try
         {
            // This should fail since there are no more cluster members left
            beans[i].test(GenericClusteringException.COMPLETED_NO);
            fail("Bean[" + i + "]");
         }
         catch (Exception e)
         {
            assertTrue("Bean[" + i + "] " + e.getMessage(), e.getMessage().startsWith("cluster invocation failed"));
         }
         try
         {
            // This should fail since there are no more cluster members left
            beans[i].test(-1);
            fail("Bean[" + i + "]");
         }
         catch (Exception e)
         {
            assertTrue("Bean[" + i + "] " + e.getMessage(), e.getMessage().startsWith("Unreachable?"));
         }
      }
   }
}
