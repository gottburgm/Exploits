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
package org.jboss.test.jbossmx.implementation.persistence;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Descriptor;
import javax.management.ObjectName;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.jboss.mx.modelmbean.ModelMBeanConstants;

import org.jboss.test.jbossmx.implementation.TestCase;

import org.jboss.test.jbossmx.implementation.persistence.support.Resource;

public class OnTimerPersistenceTestCase
   extends TestCase
   implements ModelMBeanConstants
{

   public OnTimerPersistenceTestCase(String s)
   {
      super(s);
   }

   public void testOnTimerCallback()
   {
      try
      {
         MBeanServer server = MBeanServerFactory.createMBeanServer();

         Descriptor descriptor = new DescriptorSupport();
         descriptor.setField(NAME, "Active");
         descriptor.setField(DESCRIPTOR_TYPE, ATTRIBUTE_DESCRIPTOR);
         descriptor.setField(PERSIST_POLICY, PP_ON_TIMER);
         descriptor.setField(PERSIST_PERIOD, "1000");

         ModelMBeanAttributeInfo attrInfo = new ModelMBeanAttributeInfo(
               "Active",
               boolean.class.getName(),
               "Test Attribute",
               IS_READABLE,
               !IS_WRITABLE,
               !IS_IS,
               descriptor
         );

         ModelMBeanInfo info = new ModelMBeanInfoSupport(
               Resource.class.getName(),
               "Test Resource",
               new ModelMBeanAttributeInfo[] { attrInfo },
               null,                      // constructors
               null,                      // operations
               null                       // notification
         );

         ModelMBean mmb = new RequiredModelMBean();
         mmb.setManagedResource(new Resource(), OBJECT_REF);
         mmb.setModelMBeanInfo(info);

         ObjectName oname = new ObjectName("test:name=OnTimerCallBack");
         server.registerMBean(mmb, oname);

         Thread.sleep(5000);
      }
      catch (Throwable t)
      {
         log.debug("failed", t);
         fail("Creating Required ModelMBean instance with default constructor failed: " + t.toString());
      }
   }

}
