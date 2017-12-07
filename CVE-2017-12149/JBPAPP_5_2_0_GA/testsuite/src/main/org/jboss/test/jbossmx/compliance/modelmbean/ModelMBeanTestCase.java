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
package org.jboss.test.jbossmx.compliance.modelmbean;

import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.test.jbossmx.compliance.modelmbean.support.Resource;
import org.jboss.test.jbossmx.compliance.modelmbean.support.Resource2;

/**
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 59536 $
 */
public class ModelMBeanTestCase extends TestCase
{
   public ModelMBeanTestCase(String s)
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
         log.debug("failed", t);
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
         log.debug("failed", t);
         fail("Setting resource object with 'ObjectReference' type failed: " + t.toString());
      }
   }
   
   public void testRMMSetModelMBeanInfo()
   {
      try
      {
         ModelMBean modelmbean = new RequiredModelMBean();
         modelmbean.setModelMBeanInfo(getModelMBeanInfo());
      }
      catch (Throwable t)
      {
         log.debug("failed", t);
         fail("Unable to set model mbean info for resource object: " + t.toString());
      }

   }

   public void testRMMInvocation()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         
         Resource resource = new Resource();
         ModelMBean modelmbean = new RequiredModelMBean();
         modelmbean.setModelMBeanInfo(getModelMBeanInfo());
         modelmbean.setManagedResource(resource, "ObjectReference");
         
         ObjectName name = new ObjectName("rmm:invocationTest=true");
         server.registerMBean(modelmbean, name);
         
         assertTrue(((Boolean)server.invoke(name, "isActive", new Object[] {}, new String[] {})).booleanValue());
      }
      catch (Throwable t) 
      {
         log.debug("failed", t);
         fail("RMMInvocation: " + t.toString());
      }
   }
   
   /**
    * A resource that implements an MBean interface at the same time.
    * 
    * Used to test that the fix for JBAS-1704 doesn't cause a problem
    * when a target resource *with* an mbean interface, too, registers
    * through a model mbean and exposes methods/attribute that are not
    * declared on the mbean interface.
    */
   public void testRMMResourceImplementsMBean()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();
         
         Resource2 resource = new Resource2();
         ModelMBean modelmbean = new RequiredModelMBean();
         modelmbean.setModelMBeanInfo(getModelMBeanInfo2());
         modelmbean.setManagedResource(resource, "ObjectReference");
         
         ObjectName name = new ObjectName("rmm:resourceImplementsMBean=true");
         server.registerMBean(modelmbean, name);
         
         server.setAttribute(name, new Attribute("pojoAttribute", new Integer(111)));
         
         assertEquals((Integer)server.getAttribute(name, "pojoAttribute"), new Integer(111));
         assertTrue(((Boolean)server.invoke(name, "pojoOperation", new Object[] {}, new String[] {})).booleanValue());
      }
      catch (Throwable t) 
      {
         log.debug("failed", t);
         fail("testRMMResourceImplementsMBean: " + t.toString());
      }      
   }
   
   private ModelMBeanInfo getModelMBeanInfo()
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

   private ModelMBeanInfo getModelMBeanInfo2()
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean BOOLEAN  = true;
      
      // build 'pojoAttribute' read-write attribute
      Descriptor descr2 = new DescriptorSupport();
      descr2.setField("name", "pojoAttribute");
      descr2.setField("descriptorType", "attribute");
      descr2.setField("getMethod", "getpojoAttribute");
      descr2.setField("setMethod", "setpojoAttribute");

      ModelMBeanAttributeInfo pojoAttributeInfo =
         new ModelMBeanAttributeInfo(
            "pojoAttribute",
            int.class.getName(),
            "A simple integer attribute.",
            READABLE, WRITABLE, !BOOLEAN,
            descr2
         );

      // JBAS-3614 - the jdk implementation of RequiredModelMBean
      // requires the attribute to be declared as operation(s), too.
      // This is not a requirement of the JMX 1.2 spec, though.
      
      // build 'getpojoAttribute' operation
      Descriptor descr21 = new DescriptorSupport();
      descr21.setField("name", "getpojoAttribute");
      descr21.setField("descriptorType", "operation");
      
      ModelMBeanOperationInfo getpojoAttributeOperation =
         new ModelMBeanOperationInfo(
            "getpojoAttribute",           // name
            "A simple operation.",        // description
            null,                         // signature
            int.class.getName(),          // return type
            MBeanOperationInfo.INFO,      // impact
            descr21                       // descriptor
         );
      
      // build 'setpojoAttribute' operation
      Descriptor descr22 = new DescriptorSupport();
      descr22.setField("name", "setpojoAttribute");
      descr22.setField("descriptorType", "operation");
      
      ModelMBeanOperationInfo setpojoAttributeOperation =
         new ModelMBeanOperationInfo(
            "setpojoAttribute",           // name
            "A simple operation.",        // description
            new MBeanParameterInfo[]      // signature
                  { new MBeanParameterInfo("int", int.class.getName(), "int setter") },
            void.class.getName(),         // return type
            MBeanOperationInfo.ACTION,    // impact
            descr22                       // descriptor
         );
      
      // build 'pojoOperation' operation
      Descriptor descr3 = new DescriptorSupport();
      descr3.setField("name", "pojoOperation");
      descr3.setField("descriptorType", "operation");

      ModelMBeanOperationInfo pojoOperationInfo =
         new ModelMBeanOperationInfo(
            "pojoOperation",              // name & description
            "A simple operation.",
            null,                         // signature
            boolean.class.getName(),      // return type
            MBeanOperationInfo.ACTION,    // impact
            descr3                        // descriptor
         );

      // MBean descriptor
      Descriptor descr4 = new DescriptorSupport();
      descr4.setField("name", RequiredModelMBean.class.getName());
      descr4.setField("descriptorType", "MBean");

      // create ModelMBeanInfo
      ModelMBeanInfo info = new ModelMBeanInfoSupport(
                               RequiredModelMBean.class.getName(),  // class name
                               "POJO",                              // description
                               new ModelMBeanAttributeInfo[] {      // attributes
                                     pojoAttributeInfo
                               },
                               null,                                // constructors
                               new ModelMBeanOperationInfo[] {      // operations
                                     getpojoAttributeOperation,
                                     setpojoAttributeOperation,
                                     pojoOperationInfo
                               },
                               null,                                // notifications
                               descr4                               // descriptor
                            );

      return info;
   }   
}
