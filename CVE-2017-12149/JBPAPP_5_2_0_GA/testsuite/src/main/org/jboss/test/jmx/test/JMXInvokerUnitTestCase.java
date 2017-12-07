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
package org.jboss.test.jmx.test;

import javax.management.Attribute;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.invoker.CustomClass;
import org.jboss.test.jmx.invoker.InvokerTestMBean;
import org.jboss.test.jmx.invoker.RMIBadListener;
import org.jboss.test.jmx.invoker.RMIListener;

/**
 * Tests for the jmx invoker adaptor.
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:tom@jboss.com">Tom Elrod</a>
 * @version $Revision: 81036 $
 */
public class JMXInvokerUnitTestCase extends JBossTestCase
{
   public JMXInvokerUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      // JBAS-3605, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new JMXInvokerUnitTestCase("testGetSomething"));
      suite.addTest(new JMXInvokerUnitTestCase("testGetCustom"));
      suite.addTest(new JMXInvokerUnitTestCase("testGetCustomXMBean"));
      suite.addTest(new JMXInvokerUnitTestCase("testGetXMBeanInfo"));
      suite.addTest(new JMXInvokerUnitTestCase("testXMBeanDoSomething"));
      suite.addTest(new JMXInvokerUnitTestCase("testSetCustom"));
      suite.addTest(new JMXInvokerUnitTestCase("testClassNotFoundException"));
      suite.addTest(new JMXInvokerUnitTestCase("testNotification"));
      suite.addTest(new JMXInvokerUnitTestCase("testNotificationWithBadListener"));
      
      return getDeploySetup(suite, "invoker-adaptor-test.ear");
   }

   /**
    * The jmx object name name of the mbean under test
    * @return The name of the mbean under test
    * @throws MalformedObjectNameException
    */
   ObjectName getObjectName() throws MalformedObjectNameException
   {
      return InvokerTestMBean.OBJECT_NAME;
   }

   public void testGetSomething()
      throws Exception
   {
      log.info("+++ testGetSomething");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      assertEquals("something", server.getAttribute(getObjectName(), "Something"));
   }

   public void testGetCustom()
      throws Exception
   {
      log.info("+++ testGetCustom");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      CustomClass custom = (CustomClass) server.getAttribute(getObjectName(), "Custom");
      assertEquals("InitialValue", custom.getValue());
   }

   public void testGetCustomXMBean()
      throws Exception
   {
      log.info("+++ testGetCustomXMBean");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      ObjectName xmbean = new ObjectName("jboss.test:service=InvokerTest,type=XMBean");
      CustomClass custom = (CustomClass) server.getAttribute(xmbean, "Custom");
      assertEquals("InitialValue", custom.getValue());
   }
   public void testGetXMBeanInfo()
      throws Exception
   {
      log.info("+++ testGetXMBeanInfo");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      ObjectName xmbean = new ObjectName("jboss.test:service=InvokerTest,type=XMBean");
      MBeanInfo info = server.getMBeanInfo(xmbean);
      log.info("MBeanInfo: "+info);
   }
   public void testXMBeanDoSomething()
      throws Exception
   {
      log.info("+++ testXMBeanDoSomething");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      ObjectName xmbean = new ObjectName("jboss.test:service=InvokerTest,type=XMBean");
      Object[] args = {};
      String[] sig = {};
      CustomClass custom = (CustomClass) server.invoke(xmbean, "doSomething", args, sig);
      log.info("doSomething: "+custom);
   }

   public void testSetCustom()
      throws Exception
   {
      log.info("+++ testSetCustom");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      server.setAttribute(getObjectName(), new Attribute("Custom", new CustomClass("changed")));
      CustomClass custom = (CustomClass) server.getAttribute(getObjectName(), "Custom");
      assertEquals("changed", custom.getValue());
   }

   /**
    * Create an mbean whose class does not exist to test that the exception
    * seen from the adaptor is a ClassNotFoundException wrapped in a
    * ReflectionException
    * @throws Exception
    */
   public void testClassNotFoundException() throws Exception
   {
      log.info("+++ testClassNotFoundException");
      MBeanServerConnection server = (MBeanServerConnection) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      ObjectName name = new ObjectName("jboss.test:test=testClassNotFoundException");
      try
      {
         server.createMBean("org.jboss.text.jmx.DoesNotExist", name);
         fail("Was able to create org.jboss.text.jmx.DoesNotExist mbean");
      }
      catch (ReflectionException e)
      {
         Exception ex = e.getTargetException();
         assertTrue("ReflectionException.target is ClassNotFoundException",
            ex instanceof ClassNotFoundException);
      }
   }

   /** Test the remoting of JMX Notifications
    * @throws Exception
    */
   public void testNotification() throws Exception
   {
      log.info("+++ testNotification");
      RMIListener listener = new RMIListener(10);
      listener.export();
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      server.addNotificationListener(getObjectName(), listener, null, "runTimer");
      server.invoke(getObjectName(), "startTimer", null, null);
      synchronized( listener )
      {
         listener.wait(15000);
      }
      server.removeNotificationListener(getObjectName(), listener);
      listener.unexport();
      int count = listener.getCount();
      assertTrue("Received 10 notifications, count="+count, count == 10);
   }

   /** Test the remoting of JMX Notifications with a valid listener
    * and a bad listener that attempts to hang the service by sleeping
    * in the notification callback.
    *
    * @throws Exception
    */
   public void testNotificationWithBadListener() throws Exception
   {
      log.info("+++ testNotificationWithBadListener");
      RMIAdaptor server = (RMIAdaptor) getInitialContext().lookup("jmx/invoker/RMIAdaptor");
      // Add a bad listener
      RMIBadListener badListener = new RMIBadListener();
      badListener.export();
      server.addNotificationListener(getObjectName(), badListener, null, "BadListener");
      RMIListener listener = new RMIListener(10);
      listener.export();
      // Add a good listener
      server.addNotificationListener(getObjectName(), listener, null, "runTimer");
      server.invoke(getObjectName(), "startTimer", null, null);
      // Wait 25 seconds for the good listener events to complete
      synchronized( listener )
      {
         listener.wait(25000);
      }
      server.removeNotificationListener(getObjectName(), listener);
      listener.unexport();
      int count = listener.getCount();
      assertTrue("Received 10 notifications from Listener, count="+count,
         count == 10);
      count = badListener.getCount();
      assertTrue("Received >= 1 notifications from BadListener, count="+count,
         count >= 1);
      try
      {
         server.removeNotificationListener(getObjectName(), badListener);
         badListener.unexport();
      }
      catch(ListenerNotFoundException e)
      {

         log.debug("The BadListener was not found", e);
      }
   }
}
