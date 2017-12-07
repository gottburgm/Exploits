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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationService;

import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.server.support.Broadcaster;
import org.jboss.test.jmx.compliance.server.support.Test;

/**
 * Tests default domain handling in the Server.<p>
 *
 * TODO createMBean x4.
 * TODO deserialize x2 
 * TODO getAttribute x2 
 * TODO getMBeanInfo 
 * TODO instantiate x2
 * TODO isInstanceOf
 * TODO isRegistered
 * TODO removeNotificationListener x2
 * TODO setAttribute x2
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class DefaultDomainTestCase
  extends TestCase
{
   // Attributes ----------------------------------------------------------------

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public DefaultDomainTestCase(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   /**
    * Test Register in the "anonymous" default domain.
    */
   public void testDefaultDomainRegisterUnqualified()
   {
      MBeanServer server =null;
      ObjectName unqualifiedName = null;
      ObjectName qualifiedName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer();
         unqualifiedName = new ObjectName(":property=1");
         qualifiedName = new ObjectName("DefaultDomain:property=1");
         server.registerMBean(new Test(), unqualifiedName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      assertEquals("DefaultDomain", server.getDefaultDomain());

      try
      {
         server.getObjectInstance(unqualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Unqualified lookup failed");
      }

      try
      {
         server.getObjectInstance(qualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Qualified lookup failed");
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test Register in the "anonymous" default domain.
    */
   public void testDefaultDomainRegisterQualified()
   {
      MBeanServer server =null;
      ObjectName unqualifiedName = null;
      ObjectName qualifiedName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer();
         unqualifiedName = new ObjectName(":property=1");
         qualifiedName = new ObjectName("DefaultDomain:property=1");
         server.registerMBean(new Test(), qualifiedName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      assertEquals("DefaultDomain", server.getDefaultDomain());

      try
      {
         server.getObjectInstance(unqualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Unqualified lookup failed");
      }

      try
      {
         server.getObjectInstance(qualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Qualified lookup failed");
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test Register in a named default domain.
    */
   public void testMyDefaultDomainRegisterUnqualified()
   {
      MBeanServer server =null;
      ObjectName unqualifiedName = null;
      ObjectName qualifiedName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         unqualifiedName = new ObjectName(":property=1");
         qualifiedName = new ObjectName("MyDomain:property=1");
         server.registerMBean(new Test(), unqualifiedName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      assertEquals("MyDomain", server.getDefaultDomain());

      try
      {
         server.getObjectInstance(unqualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Unqualified lookup failed");
      }

      try
      {
         server.getObjectInstance(qualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Qualified lookup failed");
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test Register in a named default domain.
    */
   public void testMyDefaultDomainRegisterQualified()
   {
      MBeanServer server =null;
      ObjectName unqualifiedName = null;
      ObjectName qualifiedName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         unqualifiedName = new ObjectName(":property=1");
         qualifiedName = new ObjectName("MyDomain:property=1");
         server.registerMBean(new Test(), qualifiedName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      assertEquals("MyDomain", server.getDefaultDomain());

      try
      {
         server.getObjectInstance(unqualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Unqualified lookup failed");
      }

      try
      {
         server.getObjectInstance(qualifiedName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("Qualified lookup failed");
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test register qualified/unqualifed.
    */
   public void testRegisterQualifiedUnqualified()
   {
      duplicateRegister(":property=1", "MyDomain:property=1");
   }

   /**
    * Test register unqualified/qualifed.
    */
   public void testRegisterUnqualifiedQualified()
   {
      duplicateRegister("MyDomain:property=1", ":property=1");
   }

   /**
    * Test register unqualified/unqualified.
    */
   public void testRegisterUnqualifiedUnqualified()
   {
      duplicateRegister(":property=1", ":property=1");
   }

   /**
    * Test invoke qualified/unqualifed.
    */
   public void testInvokeQualifiedUnqualified()
   {
      invoke(":property=1", "MyDomain:property=1");
   }

   /**
    * Test invoke unqualified/qualifed.
    */
   public void testInvokeUnqualifiedQualified()
   {
      invoke("MyDomain:property=1", ":property=1");
   }

   /**
    * Test invoke unqualified/unqualified.
    */
   public void testInvokeUnqualifiedUnqualified()
   {
      invoke(":property=1", ":property=1");
   }

   /**
    * Test register qualified unregister unqualified.
    */
   public void testRegisterQualifiedUnregisterUnqualified()
   {
      unregister("MyDomain:property=1", ":property=1");
   }

   /**
    * Test register unqualified unregister qualifed.
    */
   public void testRegisterUnQualifiedUnregisterQualified()
   {
      unregister(":property=1", "MyDomain:property=1");
   }

   /**
    * Test register unqualified unregister unqualified.
    */
   public void testRegisterUnqualifiedUnregisterUnqualified()
   {
      unregister(":property=1", ":property=1");
   }

   /**
    * Add notification listenter. ObjectName, Listener
    */
   public void testAddNLUnqualifiedNameListenerRegisterQualified()
   {
      addNLNameListener("MyDomain:property=1", ":property=1");
   }

   /**
    * Add notification listenter. ObjectName, Listener
    */
   public void testAddNLQualifiedNameListenerRegisterUnqualified()
   {
      addNLNameListener(":property=1", "MyDomain:property=1");
   }

   /**
    * Add notification listenter. ObjectName, Listener
    */
   public void testAddNLUnqualifiedNameListenerRegisterUnqualified()
   {
      addNLNameListener(":property=1", ":property=1");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLUnqualifiedQualifiedRegisterUnqualifiedQualified()
   {
      addNLNameName(":property=1", "MyDomain:property=2",":property=1", "MyDomain:property=2");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLUnqualifiedQualifiedRegisterQualifiedQualified()
   {
      addNLNameName(":property=1", "MyDomain:property=2","MyDomain:property=1", "MyDomain:property=2");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLQualifiedQualifiedRegisterUnqualifiedQualified()
   {
      addNLNameName("MyDomain:property=1", "MyDomain:property=2",":property=1", "MyDomain:property=2");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLQualifiedUnqualifiedRegisterQualifiedUnqualified()
   {
      addNLNameName("MyDomain:property=1", ":property=2","MyDomain:property=1", ":property=2");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLQualifiedUnqualifiedRegisterQualifiedQualified()
   {
      addNLNameName("MyDomain:property=1", ":property=2","MyDomain:property=1", "MyDomain:property=2");
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   public void testAddNLQualifiedQualifiedRegisterQualifiedUnqualified()
   {
      addNLNameName("MyDomain:property=1", "MyDomain:property=2","MyDomain:property=1", ":property=2");
   }

   // Support -----------------------------------------------------------------

   /**
    * Test Duplicate Register.
    */
   private void duplicateRegister(String register, String test)
   {
      MBeanServer server =null;
      ObjectName registerName = null;
      ObjectName testName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         registerName = new ObjectName(register);
         testName = new ObjectName(test);
         server.registerMBean(new Test(), registerName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      boolean caught = false;
      try
      {
         server.registerMBean(new Test(), testName);
      }
      catch (InstanceAlreadyExistsException e)
      {
         caught = true;
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      if (caught == false)
         fail("Allows duplicate registration");

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test unregister
    */
   private void unregister(String register, String test)
   {
      MBeanServer server =null;
      ObjectName registerName = null;
      ObjectName testName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         registerName = new ObjectName(register);
         testName = new ObjectName(test);
         server.registerMBean(new Test(), registerName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      try
      {
         server.unregisterMBean(testName);
      }
      catch (InstanceNotFoundException e)
      {
         fail("FAILS IN RI: unregisterMBean doesn't add the default domain");
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Add notification listenter. ObjectName, Listener
    */
   private void addNLNameListener(String register, String test)
   {
      MBeanServer server =null;
      ObjectName registerName = null;
      ObjectName testName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         registerName = new ObjectName(register);
         testName = new ObjectName(test);
         server.registerMBean(new Broadcaster(), registerName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      try
      {
         server.addNotificationListener(testName, new RelationService(true), null, null);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Add notification listenter. ObjectName, ObjectName
    */
   private void addNLNameName(String register1, String register2, String test1, String test2)
   {
      MBeanServer server =null;
      ObjectName register1Name = null;
      ObjectName register2Name = null;
      ObjectName test1Name = null;
      ObjectName test2Name = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         register1Name = new ObjectName(register1);
         register2Name = new ObjectName(register2);
         test1Name = new ObjectName(test1);
         test2Name = new ObjectName(test2);
         server.registerMBean(new Broadcaster(), register1Name);
         server.registerMBean(new RelationService(true), register2Name);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      try
      {
         server.addNotificationListener(test1Name, test2Name, null, null);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Test Invoke.
    */
   private void invoke(String register, String test)
   {
      MBeanServer server =null;
      ObjectName registerName = null;
      ObjectName testName = null;
      try
      {
         server = MBeanServerFactory.createMBeanServer("MyDomain");
         registerName = new ObjectName(register);
         testName = new ObjectName(test);
         server.registerMBean(new Broadcaster(), registerName);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      try
      {
         server.invoke(testName, "doSomething", new Object[0], new String[0]);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }

      if (server != null)
         MBeanServerFactory.releaseMBeanServer(server);
   }
}
