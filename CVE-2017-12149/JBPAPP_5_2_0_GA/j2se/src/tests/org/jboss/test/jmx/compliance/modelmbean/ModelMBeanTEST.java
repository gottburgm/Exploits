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
package org.jboss.test.jmx.compliance.modelmbean;

import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.jboss.test.jmx.compliance.modelmbean.support.Resource;

import junit.framework.TestCase;

public class ModelMBeanTEST extends TestCase
{
   static class RMMListener implements NotificationListener
   {
      public void handleNotification(Notification msg, Object handback)
      {
         System.out.println("handleNotification, msg="+msg);
      }
   }

   public ModelMBeanTEST(String s)
   {
      super(s);
   }

   public void testRequiredModelMBeanConstructors()
   {
      try
      {
         new RequiredModelMBean();
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Creating Required ModelMBean instance with default constructor failed: " + t.toString());
      }
   }
   
   public void testRMMSetManagedResource()
   {
      try
      {
         ModelMBean modelmbean = new RequiredModelMBean();
         Resource resource = new Resource();
         modelmbean.setManagedResource(resource, "ObjectReference");
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Setting resource object with 'ObjectReference' type failed: " + t.toString());
      }
   }

   public void testRMMSetManagedResourceAfterRegister()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
         
      Resource resource = new Resource();
      ModelMBean modelmbean = new RequiredModelMBean();
      modelmbean.setModelMBeanInfo(getModelMBeanInfo());
       
      ObjectName name = new ObjectName("rmm:invocationTest=true");
      server.registerMBean(modelmbean, name);

      modelmbean.setManagedResource(resource, "ObjectReference");

      MBeanInfo info = server.getMBeanInfo(name);
      assertTrue("MBeanInfo != null", info != null);

      Object[] args = {};
      String[] sig = {};
      Boolean flag = (Boolean) server.invoke(name, "isActive", args, sig);
      assertTrue("isActive", flag.booleanValue());
   }

   public void testRMMSetManagedResourceAfterRegisterViaServer()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
         
      Resource resource = new Resource();
      ModelMBean modelmbean = new RequiredModelMBean();
      modelmbean.setModelMBeanInfo(getModelMBeanInfo());
       
      ObjectName name = new ObjectName("rmm:invocationTest=true");
      server.registerMBean(modelmbean, name);

      Object[] args = {resource, "ObjectReference"};
      String[] sig = {"java.lang.Object", "java.lang.String"};
      server.invoke(name, "setManagedResource", args, sig);

      MBeanInfo info = server.getMBeanInfo(name);
      assertTrue("MBeanInfo != null", info != null);

      args = new Object[]{};
      sig = new String[]{};
      Boolean flag = (Boolean) server.invoke(name, "isActive", args, sig);
      assertTrue("isActive", flag.booleanValue());
   }

   public void testCreatedRMMSetManagedResourceAfterRegisterViaServer()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName name = new ObjectName("rmm:invocationTest=true");
      ModelMBeanInfo info = getModelMBeanInfo();
      Object[] ctorArgs = { info };
      String[] ctorSig = { "javax.management.modelmbean.ModelMBeanInfo" };
      server.createMBean("javax.management.modelmbean.RequiredModelMBean",
         name, ctorArgs, ctorSig);

      Resource resource = new Resource();

      Object[] args = {resource, "ObjectReference"};
      String[] sig = {"java.lang.Object", "java.lang.String"};
      server.invoke(name, "setManagedResource", args, sig);

      info = (ModelMBeanInfo) server.getMBeanInfo(name);
      assertTrue("MBeanInfo != null", info != null);

      args = new Object[]{};
      sig = new String[]{};
      Boolean flag = (Boolean) server.invoke(name, "isActive", args, sig);
      assertTrue("isActive", flag.booleanValue());
   }

   public void testCreatedRMMSetManagedResourceAfterRegisterViaServerListener()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName name = new ObjectName("rmm:invocationTest=true");
      ModelMBeanInfo info = getModelMBeanInfo();
      Object[] ctorArgs = { info };
      String[] ctorSig = { "javax.management.modelmbean.ModelMBeanInfo" };
      server.createMBean("javax.management.modelmbean.RequiredModelMBean",
         name, ctorArgs, ctorSig);

      Resource resource = new Resource();

      Object[] args = {resource, "objectReference"};
      String[] sig = {"java.lang.Object", "java.lang.String"};
      server.invoke(name, "setManagedResource", args, sig);

      RMMListener listener = new RMMListener();
      server.addNotificationListener(name, listener, null, null);
   }

   public void testRMMSetModelMBeanInfo()
      throws Exception
   {
      ModelMBean modelmbean = new RequiredModelMBean();
      modelmbean.setModelMBeanInfo(getModelMBeanInfo());
   }

   public void testRMMInvocation()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
         
      Resource resource = new Resource();
      ModelMBean modelmbean = new RequiredModelMBean();
      modelmbean.setModelMBeanInfo(getModelMBeanInfo());
      modelmbean.setManagedResource(resource, "ObjectReference");
       
      ObjectName name = new ObjectName("rmm:invocationTest=true");
      server.registerMBean(modelmbean, name);

      MBeanInfo info = server.getMBeanInfo(name);
      assertTrue("MBeanInfo != null", info != null);

      assertTrue(((Boolean)server.invoke(name, "isActive", new Object[] {}, new String[] {})).booleanValue());
   }
   
   public void testRMMDefault()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
         
      Resource resource = new Resource();
      ModelMBean modelmbean = new RequiredModelMBean();
      modelmbean.setManagedResource(resource, "ObjectReference");
      modelmbean.setModelMBeanInfo(getModelMBeanInfo());
         
      ObjectName name = new ObjectName("rmm:test=default");
      server.registerMBean(modelmbean, name);
         
      assertTrue("Should be the default room", server.getAttribute(name, "Room").equals("D325"));
   }

   public ModelMBeanInfo getModelMBeanInfo()
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean BOOLEAN  = true;
      
      // build 'RoomName' read-write attribute
      Descriptor descr1 = new DescriptorSupport();
      descr1.setField("name", "Room");
      descr1.setField("descriptorType", "attribute");
      descr1.setField("displayName", "Room Number");
      descr1.setField("default", "D325");

      ModelMBeanAttributeInfo roomNameInfo =
         new ModelMBeanAttributeInfo(
            "Room",                        // attribute name
            String.class.getName(),        // attribute type
            "Room name or number.",        // description
            READABLE, WRITABLE, !BOOLEAN,  // read write
            descr1                         // descriptor
         );

      // build 'Active' read-only attribute
      Descriptor descr2 = new DescriptorSupport();
      descr2.setField("name", "Active");
      descr2.setField("descriptorType", "attribute");
      descr2.setField("getMethod", "isActive");
      descr2.setField("currencyTimeLimit", "10");

      ModelMBeanAttributeInfo activeInfo =
         new ModelMBeanAttributeInfo(
            "Active",
            boolean.class.getName(),
            "Printer state.",
            READABLE, !WRITABLE, !BOOLEAN,
            descr2
         );

      // build 'isActive' getter operation
      Descriptor descr3 = new DescriptorSupport();
      descr3.setField("name", "isActive");
      descr3.setField("descriptorType", "operation");
      descr3.setField("role", "getter");

      ModelMBeanOperationInfo isActiveInfo =
         new ModelMBeanOperationInfo(
            "isActive",                   // name & description
            "Checks if the printer is currently active.",
            null,                         // signature
            boolean.class.getName(),      // return type
            MBeanOperationInfo.INFO,      // impact
            descr3                        // descriptor
         );

      // MBean descriptor
      Descriptor descr4 = new DescriptorSupport();
      descr4.setField("name", RequiredModelMBean.class.getName());
      descr4.setField("descriptorType", "MBean");

      // create ModelMBeanInfo
      ModelMBeanInfo info = new ModelMBeanInfoSupport(
                               RequiredModelMBean.class.getName(),  // class name
                               "Printer",                           // description
                               new ModelMBeanAttributeInfo[] {      // attributes
                                  roomNameInfo,
                                  activeInfo
                               },
                               null,                                // constructors
                               new ModelMBeanOperationInfo[] {      // operations
                                  isActiveInfo
                               },
                               null,                                // notifications
                               descr4                               // descriptor
                            );

      return info;
   }

}
