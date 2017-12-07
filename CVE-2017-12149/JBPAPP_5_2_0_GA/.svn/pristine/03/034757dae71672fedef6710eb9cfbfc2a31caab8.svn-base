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
package org.jboss.test.jca.test;

import junit.framework.*;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.JDBCStatementTestsConnectionSession;
import org.jboss.test.jca.interfaces.JDBCStatementTestsConnectionSessionHome;

/**
 * JDBCStatementTestsConnectionUnitTestCase.java
 *
 *
 * Created: Fri Feb 14 15:15:47 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class JDBCStatementTestsConnectionUnitTestCase extends JBossTestCase {
   public JDBCStatementTestsConnectionUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(JDBCStatementTestsConnectionUnitTestCase.class, "jcatest.jar");
      Test t2 = getDeploySetup(t1, "testadapter-ds.xml");
      Test t3 = getDeploySetup(t2, "testdriver-ds.xml");
      Test t4 = getDeploySetup(t3, "jbosstestdriver.jar");
      return getDeploySetup(t4, "jbosstestadapter.rar");
   }

   /** This test will probably fail with a class cast exception if run
    * twice. The DriverManager appears to be keeping a static
    * reference to the Driver class, so reloading the
    * jbosstestdriver.sar will result in incompatible classes being
    * used.
    */
   public void testJDBCStatementTestsConnection() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testConnectionObtainable();
   }

   public void testConfiguredQueryTimeout() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testConfiguredQueryTimeout();
   }

   public void testTransactionQueryTimeout() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testTransactionQueryTimeout();
   }

   public void testTransactionQueryTimeoutMarkedRollback() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testTransactionQueryTimeoutMarkedRollback();
   }

   public void testLazyAutoCommit() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testLazyAutoCommit();
   }

   public void testRollbackOnCloseNoTx() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testRollbackOnCloseNoTx();
   }

   public void testRollbackOnCloseManagedTx() throws Exception
   {
      JDBCStatementTestsConnectionSessionHome home =
         (JDBCStatementTestsConnectionSessionHome)getInitialContext().lookup("JDBCStatementTestsConnectionSession");
      JDBCStatementTestsConnectionSession s = home.create();
      s.testRollbackOnCloseManagedTx();
   }

}
