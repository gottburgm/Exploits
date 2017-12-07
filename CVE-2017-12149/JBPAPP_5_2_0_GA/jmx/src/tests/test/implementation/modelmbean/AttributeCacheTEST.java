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
package test.implementation.modelmbean;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.Descriptor;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;

import junit.framework.TestCase;

import org.jboss.mx.modelmbean.XMBean;
import org.jboss.mx.modelmbean.XMBeanConstants;

import test.implementation.modelmbean.support.Test;

/**
 * Tests attribute caching and operation mapping for XMBean.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 */
public class AttributeCacheTEST extends TestCase implements XMBeanConstants
{
   public AttributeCacheTEST(String s)
   {
      super(s);
   }

   /**
    * Tests that attribute values are not cached if nothing is declared in xml.
    *
    * This test uses the xmbean.dtd
    */
   public void testImplicitDisabledAttributeCaching() throws Exception
   {
   
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);
      
      for (int i = 0; i < 10; ++i)
      {
         server.setAttribute(name, new Attribute("Something", "foo"));
         server.getAttribute(name, "Something");
      }
      
      assertTrue(resource.getFooCount() == 10);
      assertTrue(resource.getBarCount() == 10);
   }

   /**
    * Tests that attribute values are not cached if currencyTimeLimit = 0
    *
    * This test uses the xmbean.dtd
    */
   public void testExplicitDisabledAttributeCaching() throws Exception
   {
   
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface2.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);

      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);
      
      for (int i = 0; i < 8; ++i)
      {
         server.setAttribute(name, new Attribute("Something", "foo"));
         server.getAttribute(name, "Something");
      }

      assertTrue(resource.getFooCount() == 8);
      assertTrue(resource.getBarCount() == 8);
      
   }

   /**
    * Tests attribute that is never stale (currencyTimeLimit = -1)
    *
    * This test uses the xmbean.dtd
    */
   public void testNeverStaleAttributeCaching() throws Exception
   {
   
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface3.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);
      
      for (int i = 0; i < 11; ++i)
      {
         server.setAttribute(name, new Attribute("Something", "foo"));
         server.getAttribute(name, "Something");
      }

      assertTrue(resource.getFooCount() == 11);
      assertTrue(resource.getBarCount() == 0);
   }

   /**
    * Tests attribute that caches the value for 10 secs.
    *
    * This test uses the xmbean.dtd
    */
   public void testCachedAttribute() throws Exception
   {
   
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface4.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);
      
      for (int i = 0; i < 7; ++i)
      {
         server.setAttribute(name, new Attribute("Something", "foo"));
         server.getAttribute(name, "Something");
      }

      assertTrue(resource.getFooCount() == 7);
      assertTrue(resource.getBarCount() == 0);
   }

   /**
    * Tests attribute that caches the value for 1 secs.
    *
    * This test uses the xmbean.dtd
    */
   public void testCachedAttribute2() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface5.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);
      
      server.getAttribute(name, "Something");

      assertTrue(resource.getBarCount() == 1);
      
      server.setAttribute(name, new Attribute("Something", "yksi"));

      assertTrue(resource.getFooCount() == 1);
      
      String str = (String)server.getAttribute(name, "Something");
      
      assertTrue(resource.getBarCount() == 1);
      assertTrue(str.equals("yksi"));
      
      try { Thread.sleep(1100); } catch (Throwable t) {}
      
      server.getAttribute(name, "Something");
      
      assertTrue(resource.getBarCount() == 2);
      
      server.setAttribute(name, new Attribute("Something", "kaksi"));
      
      assertTrue(resource.getFooCount() == 2);
      
      try { Thread.sleep(1100); } catch (Throwable t) {}
      
      str = (String)server.getAttribute(name, "Something");
      
      assertTrue(resource.getBarCount() == 3);
      assertTrue(str.equals("kaksi"));
      
      str = (String)server.getAttribute(name, "Something");
      
      assertTrue(resource.getBarCount() == 3);
      assertTrue(str.equals("kaksi"));
   }

   /**
    * Tests attribute change notifications
    */
   public void testAttributeChangeNotifications() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      Test resource = new Test();
      Descriptor d = new DescriptorSupport();
      d.setField(RESOURCE_REFERENCE, resource);
      d.setField(RESOURCE_TYPE, "file:./src/main/test/implementation/modelmbean/support/xml/TrivialManagementInterface5.xml");
      d.setField(SAX_PARSER, "org.apache.crimson.parser.XMLReaderImpl");

      XMBean mmb = new XMBean(d, DESCRIPTOR);
      
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(mmb, name);

      class MyNotificationListener implements NotificationListener
      {
         int notifCount = 0;
         
         public void handleNotification(Notification notification, Object handback)
         {
            AttributeChangeNotification notif = (AttributeChangeNotification)notification;
            
            assertTrue(notif.getNewValue().equals("somevalue"));
            
            notifCount++;
         }
      }
      
      MyNotificationListener listener = new MyNotificationListener();
      server.addNotificationListener(name, listener, null, null);
      
      for (int i = 0; i < 10; ++i)
         server.setAttribute(name, new Attribute("Something", "somevalue"));
         
      assertTrue(listener.notifCount == 10);
   }
   
}
