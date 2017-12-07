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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.transaction.TransactionRolledbackException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.XAExceptionSession;
import org.jboss.test.jca.interfaces.XAExceptionSessionHome;
import org.jboss.test.jca.interfaces.XAExceptionTestSession;
import org.jboss.test.jca.interfaces.XAExceptionTestSessionHome;

/**
 * XAExceptionUnitTestCase.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class XAExceptionUnitTestCase extends JBossTestCase
{
   public XAExceptionUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(XAExceptionUnitTestCase.class, "jcatest.jar");
      Test t2 = getDeploySetup(t1, "testadapter-ds.xml");
      return getDeploySetup(t2, "jbosstestadapter.rar");
   }

   public void testXAExceptionToTransactionRolledbackException() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.testXAExceptionToTransactionRolledbackException();
      }
      catch (TransactionRolledbackException tre)
      {
         getLog().info("testXAExceptionToRollbackException passed");
         return;
      }
      fail("expected TransactionRolledbackException not thrown");
   }

   public void testXAExceptionToTransactionRolledbackExceptionOnServer() throws Exception
   {
      XAExceptionTestSessionHome xth = (XAExceptionTestSessionHome)getInitialContext().lookup("test/XAExceptionTestSessionHome");
      XAExceptionTestSession xt = xth.create();
      xt.testXAExceptionToTransactionRolledbackException();
   }

   public void testXAExceptionToTransactionRolledbackLocalExceptionOnServer() throws Exception
   {
      XAExceptionTestSessionHome xth = (XAExceptionTestSessionHome)getInitialContext().lookup("test/XAExceptionTestSessionHome");
      XAExceptionTestSession xt = xth.create();
      xt.testXAExceptionToTransactionRolledbackLocalException();
   }

   public void testRMERRInOnePCToTransactionRolledbackException() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.testRMERRInOnePCToTransactionRolledbackException();
      }
      catch (TransactionRolledbackException tre)
      {
         getLog().info("testXAExceptionToRollbackException passed");
         return;
      }
      fail("expected TransactionRolledbackException not thrown");
   }

   public void testRMERRInOnePCToTransactionRolledbackExceptionOnServer() throws Exception
   {
      XAExceptionTestSessionHome xth = (XAExceptionTestSessionHome)getInitialContext().lookup("test/XAExceptionTestSessionHome");
      XAExceptionTestSession xt = xth.create();
      xt.testRMERRInOnePCToTransactionRolledbackException();
   }

   public void testXAExceptionToTransactionRolledbacLocalkExceptionOnServer() throws Exception
   {
      XAExceptionTestSessionHome xth = (XAExceptionTestSessionHome)getInitialContext().lookup("test/XAExceptionTestSessionHome");
      XAExceptionTestSession xt = xth.create();
      xt.testXAExceptionToTransactionRolledbacLocalkException();
   }

   public void testSimulateConnectionError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      x.simulateConnectionError();
   }

   public void testSimulateConnectionErrorWithTwoHandles() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      x.simulateConnectionErrorWithTwoHandles();
   }

   public void testGetConnectionResourceError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateError("getConnectionResource", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   public void testGetConnectionRuntimeError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateError("getConnectionRuntime", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   public void testCreateManagedConnectionResourceError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateFactoryError("createManagedConnectionResource", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   public void testCreateManagedConnectionRuntimeError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateFactoryError("createManagedConnectionRuntime", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   public void testMatchManagedConnectionResourceError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateFactoryError("matchManagedConnectionResource", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   public void testMatchManagedConnectionRuntimeError() throws Exception
   {
      XAExceptionSessionHome xh = (XAExceptionSessionHome)getInitialContext().lookup("test/XAExceptionSessionHome");
      XAExceptionSession x = xh.create();
      try
      {
         x.simulateFactoryError("matchManagedConnectionRuntime", 10);
      }
      finally
      {
         flushConnections();
      }
   }

   protected void flushConnections()
   {
      try
      {
         MBeanServerConnection server = getServer();
         server.invoke(new ObjectName("jboss.jca:service=ManagedConnectionPool,name=JBossTestCF"), "flush", new Object[0], new String[0]);
      }
      catch (Exception e)
      {
         log.warn("Unable to flush connections", e);
      }
   }
}
