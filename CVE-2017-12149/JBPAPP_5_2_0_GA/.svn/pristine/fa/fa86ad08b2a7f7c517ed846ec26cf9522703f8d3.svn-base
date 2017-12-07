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
package org.jboss.test.jmx.compliance.server;

import java.util.ArrayList;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.server.support.BroadcasterInvocationHandlerTest;
import org.jboss.test.jmx.compliance.server.support.EmitterInvocationHandlerTest;
import org.jboss.test.jmx.compliance.server.support.InvocationHandlerTest;
import org.jboss.test.jmx.compliance.server.support.InvocationHandlerTestMBean;
import org.jboss.test.jmx.compliance.server.support.ObjectInvocationHandlerTest;

/**
 * Tests the MBeanServerInvocationHandler
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class MBeanServerInvocationHandlerTestCase
   extends TestCase
   implements NotificationListener
{
   // Attributes ----------------------------------------------------------------

   private ObjectName invocationHandlerTestName;

   private ArrayList messages = new ArrayList();

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public MBeanServerInvocationHandlerTestCase(String s)
   {
      super(s);

      try
      {
         invocationHandlerTestName = new ObjectName("MBeanServerInvocationHandlerTestCase:type=InvocationHandlerTest");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   // Tests ---------------------------------------------------------------------

   public void testConstructor()
      throws Exception
   {
      MBeanServerFactory.newMBeanServer();
   }

   public void testGetter()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);

      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);
      assertEquals("Attribute", proxy.getAttribute());
   }

   public void testSetter()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      proxy.setAttribute("Changed");
      assertEquals("Changed", test.getAttribute());
   }

   public void testGetterPrimitiveBoolean()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);

      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals(false, test.isIsPrimitive());
      test.setIsPrimitive(true);

      assertEquals(true, proxy.isIsPrimitive());
   }

   public void testSetterPrimitiveBoolean()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals(false, test.isIsPrimitive());
      proxy.setIsPrimitive(true);

      assertEquals(true, test.isIsPrimitive());
   }

   public void testGetterTypeBoolean()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);

      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals(null, test.getType());
      test.setType(new Boolean(true));

      assertEquals(true, proxy.getType().booleanValue());
   }

   public void testSetterTypeBoolean()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals(null, test.getType());
      proxy.setType(new Boolean(true));

      assertEquals(true, test.getType().booleanValue());
   }

   public void testInvokeNoArgsNoReturn()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      proxy.invokeNoArgsNoReturn();
      assertTrue(test.invokeNoArgsNoReturnInvoked);
   }

   public void testInvokeNoArgs()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals("invokeNoArgs", proxy.invokeNoArgs());
   }

   public void testInvoke()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      assertEquals("parameter", proxy.invoke("parameter"));
   }

   public void testInvokeMixedParameters()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, false);

      Integer parameter = new Integer(20);
      assertEquals(parameter, proxy.invokeMixedParameters("parameter", 10, parameter));
   }

   public void testNotificationEmitterAdd()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      EmitterInvocationHandlerTest test = new EmitterInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      NotificationEmitter proxy = (NotificationEmitter) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      proxy.addNotificationListener(this, null, null);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 1);
   }

   public void testNotificationEmitterRemove()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      EmitterInvocationHandlerTest test = new EmitterInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      NotificationEmitter proxy = (NotificationEmitter) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      proxy.addNotificationListener(this, null, null);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 1);

      proxy.removeNotificationListener(this);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 0);
   }

   public void testNotificationEmitterRemoveTriplet()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      EmitterInvocationHandlerTest test = new EmitterInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      NotificationEmitter proxy = (NotificationEmitter) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType("test");
      Object handback = new Object();
      proxy.addNotificationListener(this, filter, handback);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 1);

      proxy.removeNotificationListener(this, filter, handback);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 0);
   }

   public void testNotificationEmitterRemoveTripletFailsOnBroadcaster()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      BroadcasterInvocationHandlerTest test = new BroadcasterInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      NotificationEmitter proxy = (NotificationEmitter) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType("test");
      Object handback = new Object();
      proxy.addNotificationListener(this, filter, handback);

      messages.clear();
      test.sendNotification();
      assertTrue(messages.size() == 1);

      try
      {
         proxy.removeNotificationListener(this, filter, handback);
         fail("FAILS IN JBOSSMX: removeNotificationListener(NotificationListener, NotificationFilter, Object) " +
              "should not work for a broadcaster");
      }
      catch (Exception ignored)
      {
      }
   }

   public void testGetNotificationInfo()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      EmitterInvocationHandlerTest test = new EmitterInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      NotificationEmitter proxy = (NotificationEmitter) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      MBeanNotificationInfo[] info = proxy.getNotificationInfo();
      assertEquals("test", info[0].getNotifTypes()[0]);
   }

   public void testToString()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectInvocationHandlerTest test = new ObjectInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      assertEquals("TOSTRING", proxy.toString());
   }

   public void testToStringFailsWhenNotExposed()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      try
      {
         proxy.toString();
         fail("toString() should not work when it is not exposed for management");
      }
      catch (Exception ignored)
      {
      }
   }

   public void testEquals()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectInvocationHandlerTest test = new ObjectInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      assertTrue(proxy.equals(new Object()));
   }

   public void testEqualsFailsWhenNotExposed()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      try
      {
         proxy.equals(new Object());
         fail("equals(Object) should not work when it is not exposed for management");
      }
      catch (Exception ignored)
      {
      }
   }

   public void testHashCode()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectInvocationHandlerTest test = new ObjectInvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      assertEquals(1234, proxy.hashCode());
   }

   public void testHashCodeFailsWhenNotExposed()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      InvocationHandlerTest test = new InvocationHandlerTest();
      server.registerMBean(test, invocationHandlerTestName);
      InvocationHandlerTestMBean proxy = (InvocationHandlerTestMBean) MBeanServerInvocationHandler.newProxyInstance(
         server, invocationHandlerTestName, InvocationHandlerTestMBean.class, true);

      try
      {
         proxy.hashCode();
         fail("hashCode() should not work when it is not exposed for management");
      }
      catch (Exception ignored)
      {
      }
   }

   // Notification Listener -----------------------------------------------------

   public void handleNotification(Notification notification, Object handback)
   {
      messages.add(notification);
   }
}
