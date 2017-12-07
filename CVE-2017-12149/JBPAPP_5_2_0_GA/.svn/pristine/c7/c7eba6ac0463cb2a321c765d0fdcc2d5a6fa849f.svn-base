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

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.server.support.BuilderTest;
import org.jboss.test.jmx.compliance.server.support.DelegateListener;
import org.jboss.test.jmx.compliance.server.support.MBeanServerReplaced;
import org.jboss.test.jmx.compliance.server.support.MBeanServerWrapper;
import org.jboss.test.jmx.compliance.server.support.TestMBeanServerBuilder;
import org.jboss.test.jmx.compliance.server.support.TestMBeanServerDelegate;


/**
 * Tests the MBeanServerBuilder
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class MBeanServerBuilderTestCase
  extends TestCase
{
   // Attributes ----------------------------------------------------------------

   private ObjectName name;
   private ObjectName delegateName;
   private String defaultBuilder;

   private static final String DEFAULT_DOMAIN = "default";
   private static final String BUILDER_PROPERTY = "javax.management.builder.initial";

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public MBeanServerBuilderTestCase(String s)
   {
      super(s);
      try
      {
         name = new ObjectName("server:test=MBeanServerBuilder");
         delegateName = new ObjectName("JMImplementation:type=MBeanServerDelegate");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.toString());
      }

      defaultBuilder = System.getProperty(BUILDER_PROPERTY, "javax.management.MBeanServerBuilder");
   }

   // Tests ---------------------------------------------------------------------

   public void testNewMBeanServerDelegate()
      throws Exception
   {
      MBeanServerBuilder builder = new MBeanServerBuilder();
      MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
      assertNotNull(delegate);
   }

   public void testNewMBeanServer()
      throws Exception
   {
      MBeanServerBuilder builder = new MBeanServerBuilder();
      MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
      MBeanServer server = builder.newMBeanServer(DEFAULT_DOMAIN, null, delegate);
      assertTrue(server.getDefaultDomain().equals(DEFAULT_DOMAIN));
   }

   public void testNewMBeanServerViaMBeanServerFactory()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer(DEFAULT_DOMAIN);
      assertTrue(server.getDefaultDomain().equals(DEFAULT_DOMAIN));
   }

   public void testPreRegisterGetsTheOriginalMBeanServer()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer(DEFAULT_DOMAIN);

      BuilderTest test = new BuilderTest();
      server.registerMBean(test, name);

      assertTrue("Registered", server.isRegistered(name));
      assertTrue("Got the correct MBeanServer", test.server.equals(server));
   }

   public void testPreRegisterGetsTheOuterMBeanServer()
      throws Exception
   {
      MBeanServerBuilder builder = new MBeanServerBuilder();
      MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
      MBeanServer wrapper = MBeanServerWrapper.getWrapper();
      MBeanServer server = builder.newMBeanServer(DEFAULT_DOMAIN, wrapper, delegate);

      MBeanServerWrapper.getHandler(wrapper).server = server;

      BuilderTest test = new BuilderTest();
      wrapper.registerMBean(test, name);

      assertTrue("Registered", server.isRegistered(name));
      assertTrue("Got the correct MBeanServer", test.server.equals(wrapper));
      assertTrue("Wrapper invoked", MBeanServerWrapper.getHandler(wrapper).invoked);
   }

   public void testMBeanServerDelegateReplaced()
      throws Exception
   {
      MBeanServerBuilder builder = new MBeanServerBuilder();
      TestMBeanServerDelegate delegate = new TestMBeanServerDelegate();
      MBeanServer server = builder.newMBeanServer(DEFAULT_DOMAIN, null, delegate);

      server.getAttribute(delegateName, "MBeanServerId");

      assertTrue("Delegate replaced", delegate.invoked);
   }

   public void testReplaceMBeanServerBuilderMBeanServerDelegate()
      throws Exception
   {
      System.setProperty(BUILDER_PROPERTY, TestMBeanServerBuilder.class.getName());
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();

         DelegateListener listener = new DelegateListener();
         server.addNotificationListener(delegateName, listener, null, null);

         BuilderTest test = new BuilderTest();
         server.registerMBean(test, name);

         assertTrue("Delegate replaced", listener.userData.equals("replaced"));
      }
      finally
      {
         System.setProperty(BUILDER_PROPERTY, defaultBuilder);
      }
   }

   public void testReplaceMBeanServerBuilderMBeanServer()
      throws Exception
   {
      System.setProperty(BUILDER_PROPERTY, TestMBeanServerBuilder.class.getName());
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();

         boolean caught = false;
         try
         {
            server.queryMBeans(null, null);
         }
         catch (MBeanServerReplaced e)
         {
            caught = true;
         }

         assertTrue("MBeanServer replaced", caught);
      }
      finally
      {
         System.setProperty(BUILDER_PROPERTY, defaultBuilder);
      }
   }
}
