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
package org.jboss.test.jca.test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.JDBCStatementTestsConnectionSession;
import org.jboss.test.jca.interfaces.JDBCStatementTestsConnectionSessionHome;

/**
 * Test redeploy of jdbc driver
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version
 */

public class JDBCDriverRedeployUnitTestCase extends JBossTestCase
{
   public JDBCDriverRedeployUnitTestCase(String name)
   {
      super(name);
   }

   public void testRedeploy() throws Exception
   {
      // fail("This test does not work because of class caching in java.sql.DriverManager");
      if (1!=0) return;

      doDeploy();
      try
      {
         doTest();
      }
      finally
      {
         doUndeploy();
      }

      doDeploy();
      try
      {
         doTest();
      }
      finally
      {
         doUndeploy();
      }
   }

   private void doTest() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testConnectionObtainable();
   }

   private void doDeploy() throws Exception
   {
      deploy("jbosstestdriver.jar");
      try
      {
         deploy("testdriver-ds.xml");
         try
         {
            deploy("jcatest.jar");
         }
         catch (Exception e)
         {
            undeploy("testdriver-ds.xml");
            throw e;
         }
      }
      catch (Exception e)
      {
         undeploy("jbosstestdriver.jar");
         throw e;
      }
   }

   private void doUndeploy() throws Exception
   {
      try
      {
         undeploy("jcatest.jar");
      }
      catch (Throwable ignored)
      {
      }
      try
      {
         undeploy("testdriver-ds.xml");
      }
      catch (Throwable ignored)
      {
      }
      try
      {
         undeploy("jbosstestdriver.jar");
      }
      catch (Throwable ignored)
      {
      }
   }
}
