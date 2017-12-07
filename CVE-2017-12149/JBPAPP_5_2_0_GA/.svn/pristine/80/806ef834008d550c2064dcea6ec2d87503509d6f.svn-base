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
package org.jboss.test.jmx.compliance.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import junit.framework.TestCase;

/**
 * MBean Info tests.<p>
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class MBeanInfoTEST
  extends TestCase
{
   // Static --------------------------------------------------------------------

   // Attributes ----------------------------------------------------------------

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public MBeanInfoTEST(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   public void testMBeanInfo()
      throws Exception
   {
      MBeanInfo info = new MBeanInfo(
         "name", "description", null, null, null, null);
      assertEquals("name", info.getClassName());
      assertEquals("description", info.getDescription());
      assertEquals(0, info.getAttributes().length);
      assertEquals(0, info.getConstructors().length);
      assertEquals(0, info.getNotifications().length);
      assertEquals(0, info.getOperations().length);

      info = new MBeanInfo(
         "name", "description", new MBeanAttributeInfo[0], new MBeanConstructorInfo[0],
         new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
      assertEquals("name", info.getClassName());
      assertEquals("description", info.getDescription());
      assertEquals(0, info.getAttributes().length);
      assertEquals(0, info.getConstructors().length);
      assertEquals(0, info.getNotifications().length);
      assertEquals(0, info.getOperations().length);

      MBeanParameterInfo[] parms = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo(
            "name", "type", "description")
      };

      MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name", "type", "description", true, true, false)
      };
      MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name", "description", parms)
      };
      MBeanOperationInfo[] operations = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };
      MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(new String[] { "type1", "type" }, "name", "description")
      };
      info = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);
      assertEquals("name", info.getClassName());
      assertEquals("description", info.getDescription());
      assertEquals(1, info.getAttributes().length);
      assertEquals(1, info.getConstructors().length);
      assertEquals(1, info.getNotifications().length);
      assertEquals(1, info.getOperations().length);
   }

   public void testEquals()
      throws Exception
   {
      MBeanParameterInfo[] parms = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo(
            "name", "type", "description")
      };

      MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name", "type", "description", true, true, false)
      };
      MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name", "description", parms)
      };
      MBeanOperationInfo[] operations = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };
      MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(new String[] { "type1", "type" }, "name", "description")
      };
      MBeanInfo info = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);

      assertTrue("Null is not equal to any instance", info.equals(null) == false);
      assertTrue("Instance is only equal another MBeanInfo instance", info.equals(new Object()) == false);
      assertTrue("Instance should equal itself", info.equals(info));

      MBeanInfo info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);
      assertTrue("Instances with same values should be equal", info.equals(info2));
      assertTrue("Instances with same values should be equal", info2.equals(info));

      info2 = new MBeanInfo(
         "name2", "description", attributes, constructors,
         operations, notifications);
      assertTrue("Instances with different class names are not equal", info.equals(info2) == false);
      assertTrue("Instances with different class names are not equal", info2.equals(info) == false);

      info2 = new MBeanInfo(
         "name", "description2", attributes, constructors,
         operations, notifications);
      assertTrue("Instances with different descriptions are not equal", info.equals(info2) == false);
      assertTrue("Instances with different descriptions are not equal", info2.equals(info) == false);

      MBeanAttributeInfo[] attributes2 = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name2", "type", "description", true, true, false)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes2, constructors,
         operations, notifications);
      assertTrue("Instances with different attributes are not equal", info.equals(info2) == false);
      assertTrue("Instances with different attributes are not equal", info2.equals(info) == false);

      attributes2 = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name2", "type", "description", true, true, false),
         new MBeanAttributeInfo(
         "name3", "type", "description", true, true, false)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes2, constructors,
         operations, notifications);
      assertTrue("Instances with different numbers of attributes are not equal", info.equals(info2) == false);
      assertTrue("Instances with different numbers of attributes are not equal", info2.equals(info) == false);

      info2 = new MBeanInfo(
         "name", "description", null, constructors,
         operations, notifications);
      assertTrue("Instances with and without attributes are not equal", info.equals(info2) == false);
      assertTrue("Instances with and without attributes are not equal", info2.equals(info) == false);

      MBeanConstructorInfo[] constructors2 = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name2", "description", parms)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors2,
         operations, notifications);
      assertTrue("Instances with different constructors are not equal", info.equals(info2) == false);
      assertTrue("Instances with different constructors are not equal", info2.equals(info) == false);

      constructors2 = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name2", "description", parms),
         new MBeanConstructorInfo(
         "name3", "description", parms)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors2,
         operations, notifications);
      assertTrue("Instances with different numbers of constructors are not equal", info.equals(info2) == false);
      assertTrue("Instances with different numbers of constructors are not equal", info2.equals(info) == false);

      info2 = new MBeanInfo(
         "name", "description", attributes, null,
         operations, notifications);
      assertTrue("Instances with and without constructors are not equal", info.equals(info2) == false);
      assertTrue("Instances with and without constructors are not equal", info2.equals(info) == false);

      MBeanOperationInfo[] operations2 = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name2", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations2, notifications);
      assertTrue("Instances with different operations are not equal", info.equals(info2) == false);
      assertTrue("Instances with different operations are not equal", info2.equals(info) == false);

      operations2 = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name2", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO),
         new MBeanOperationInfo(
         "name3", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations2, notifications);
      assertTrue("Instances with different numbers of operations are not equal", info.equals(info2) == false);
      assertTrue("Instances with different numbers of operations are not equal", info2.equals(info) == false);

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         null, notifications);
      assertTrue("Instances with and without operations are not equal", info.equals(info2) == false);
      assertTrue("Instances with and without operations are not equal", info2.equals(info) == false);

      MBeanNotificationInfo[] notifications2 = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(new String[] { "type", "type" }, "name2", "description")
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications2);
      assertTrue("Instances with different notifications are not equal", info.equals(info2) == false);
      assertTrue("Instances with different notifications are not equal", info2.equals(info) == false);

      notifications2 = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(new String[] { "type", "type" }, "name2", "description"),
         new MBeanNotificationInfo(new String[] { "type", "type" }, "name3", "description")
      };

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications2);
      assertTrue("Instances with different numbers of notifications are not equal", info.equals(info2) == false);
      assertTrue("Instances with different numbers of notifications are not equal", info2.equals(info) == false);

      info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, null);
      assertTrue("Instances with and without notifications are not equal", info.equals(info2) == false);
      assertTrue("Instances with and without notifications are not equal", info2.equals(info) == false);
   }

   public void testHashCode()
      throws Exception
   {
      MBeanParameterInfo[] parms = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo(
            "name", "type", "description")
      };

      MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name", "type", "description", true, true, false)
      };
      MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name", "description", parms)
      };
      MBeanOperationInfo[] operations = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };
      MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(
         new String[] { "type1", "type" }, "name", "description")
      };
      MBeanInfo info1 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);
      MBeanInfo info2 = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);

      assertTrue("Different instances with the same hashcode are equal", info1.hashCode() == info2.hashCode());
   }

   public void testSerialization()
      throws Exception
   {
      MBeanParameterInfo[] parms = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo(
            "name", "type", "description")
      };

      MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[]
      {
         new MBeanAttributeInfo(
         "name", "type", "description", true, true, false)
      };
      MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[]
      {
         new MBeanConstructorInfo(
         "name", "description", parms)
      };
      MBeanOperationInfo[] operations = new MBeanOperationInfo[]
      {
         new MBeanOperationInfo(
         "name", "description", parms,
         "type", MBeanOperationInfo.ACTION_INFO)
      };
      MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]
      {
         new MBeanNotificationInfo(
         new String[] { "type1", "type" }, "name", "description")
      };
      MBeanInfo info = new MBeanInfo(
         "name", "description", attributes, constructors,
         operations, notifications);
      // Serialize it
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(info);
    
      // Deserialize it
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      MBeanInfo result = (MBeanInfo) ois.readObject();

      assertEquals(info.getClassName(), result.getClassName());
      assertEquals(Arrays.asList(info.getAttributes()), Arrays.asList(result.getAttributes()));
      assertEquals(Arrays.asList(info.getConstructors()), Arrays.asList(result.getConstructors()));
      assertEquals(Arrays.asList(info.getOperations()), Arrays.asList(result.getOperations()));

      // UGLY!
      MBeanNotificationInfo origNotification = info.getNotifications()[0];
      MBeanNotificationInfo resultNotification = result.getNotifications()[0];
      assertEquals(origNotification.getName(), resultNotification.getName());
      assertEquals(origNotification.getDescription(), resultNotification.getDescription());
      assertEquals(Arrays.asList(origNotification.getNotifTypes()), Arrays.asList(resultNotification.getNotifTypes()));
   }
}
