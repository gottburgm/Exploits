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
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


public class ModelMBeanInfoSupportTEST extends TestCase
{
   public ModelMBeanInfoSupportTEST(String s)
   {
      super(s);
   }

   public void testSetDescriptors() throws Exception
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean ISIS     = true;
      
      RequiredModelMBean mbean = new RequiredModelMBean();
      
      ModelMBeanAttributeInfo attr1 = new ModelMBeanAttributeInfo(
            "Kissa",
            String.class.getName(),
            "Some attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanAttributeInfo attr2 = new ModelMBeanAttributeInfo(
            "Koira",
            String.class.getName(),
            "Another attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanConstructorInfo constr1 = new ModelMBeanConstructorInfo(
            "FirstConstructor",
            "Description of the first constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr2 = new ModelMBeanConstructorInfo(
            "SecondConstructor",
            "Description of the second constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr3 = new ModelMBeanConstructorInfo(
            "ThirdConstructor",
            "Description of the 3rd constructor",
            null
      );
      
      ModelMBeanOperationInfo operation = new ModelMBeanOperationInfo(
            "AnOperation",
            "The description",
            null,
            "AType",
            MBeanOperationInfo.ACTION
      );
      
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
            mbean.getClass().getName(),
            "some description",
            new ModelMBeanAttributeInfo[]    { attr1, attr2 },
            new ModelMBeanConstructorInfo[]  { constr1, constr2, constr3 },
            new ModelMBeanOperationInfo[]    { operation },
            null
      );
            
      Descriptor descr1 = info.getDescriptor("SecondConstructor", "constructor");
      
      assertEquals("SecondConstructor", descr1.getFieldValue("name"));
      assertEquals("constructor", descr1.getFieldValue("role"));
      
      Descriptor descr2 = null;
      
      Descriptor[] descr3 = info.getDescriptors("operation");
      
      assertEquals("operation", descr3[0].getFieldValue("descriptorType"));
      assertEquals("AnOperation", descr3[0].getFieldValue("name"));
      
      descr1.setField("someField", "someValue");
      descr3[0].setField("Yksi", "Kaksi");
      
      info.setDescriptors(new Descriptor[] { descr1, descr2, descr3[0] });
      
      descr1 = info.getDescriptor("SecondConstructor", "constructor");
      assertEquals("SecondConstructor", descr1.getFieldValue("name"));
      assertEquals("constructor", descr1.getFieldValue("role"));
      assertEquals("FAILS IN JBOSSMX", "someValue", descr1.getFieldValue("someField"));
      
      descr1 = info.getDescriptor("AnOperation", "operation");
      
      assertEquals("AnOperation", descr1.getFieldValue("name"));
      assertEquals("Kaksi", descr1.getFieldValue("Yksi"));
      
   }
   
   public void testGetDescriptor() throws Exception
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean ISIS     = true;
      
      RequiredModelMBean mbean = new RequiredModelMBean();
      
      ModelMBeanAttributeInfo attr1 = new ModelMBeanAttributeInfo(
            "Kissa",
            String.class.getName(),
            "Some attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanAttributeInfo attr2 = new ModelMBeanAttributeInfo(
            "Koira",
            String.class.getName(),
            "Another attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanConstructorInfo constr1 = new ModelMBeanConstructorInfo(
            "FirstConstructor",
            "Description of the first constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr2 = new ModelMBeanConstructorInfo(
            "SecondConstructor",
            "Description of the second constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr3 = new ModelMBeanConstructorInfo(
            "ThirdConstructor",
            "Description of the 3rd constructor",
            null
      );
      
      ModelMBeanOperationInfo operation = new ModelMBeanOperationInfo(
            "AnOperation",
            "The description",
            null,
            "AType",
            MBeanOperationInfo.ACTION
      );
      
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
            mbean.getClass().getName(),
            "some description",
            new ModelMBeanAttributeInfo[]    { attr1, attr2 },
            new ModelMBeanConstructorInfo[]  { constr1, constr2, constr3 },
            new ModelMBeanOperationInfo[]    { operation },
            null
      );

      Descriptor descr = info.getDescriptor("SecondConstructor", "constructor");

      try
      {
         assertTrue(descr.getFieldValue("descriptorType").equals("operation"));
      }
      catch (AssertionFailedError e) 
      {
         throw new AssertionFailedError(
               "FAILS IN JBOSSMX: We incorrectly return descriptor type " +
               "'constructor' here -- should be 'operation'"
         );
      }
      
   }
   
   
   public void testClone() throws Exception 
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean ISIS     = true;
      
      RequiredModelMBean mbean = new RequiredModelMBean();
      
      ModelMBeanAttributeInfo attr1 = new ModelMBeanAttributeInfo(
            "Kissa",
            String.class.getName(),
            "Some attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanAttributeInfo attr2 = new ModelMBeanAttributeInfo(
            "Koira",
            String.class.getName(),
            "Another attribute description",
            !READABLE, !WRITABLE, !ISIS
      );
      
      ModelMBeanConstructorInfo constr1 = new ModelMBeanConstructorInfo(
            "FirstConstructor",
            "Description of the first constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr2 = new ModelMBeanConstructorInfo(
            "SecondConstructor",
            "Description of the second constructor",
            null
      );
      
      ModelMBeanConstructorInfo constr3 = new ModelMBeanConstructorInfo(
            "ThirdConstructor",
            "Description of the 3rd constructor",
            null
      );
      
      ModelMBeanOperationInfo operation = new ModelMBeanOperationInfo(
            "AnOperation",
            "The description",
            null,
            "AType",
            MBeanOperationInfo.ACTION
      );
      
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
            mbean.getClass().getName(),
            "some description",
            new ModelMBeanAttributeInfo[]    { attr1, attr2 },
            new ModelMBeanConstructorInfo[]  { constr1, constr2, constr3 },
            new ModelMBeanOperationInfo[]    { operation },
            null
      );

      ModelMBeanInfo clone = (ModelMBeanInfo)info.clone();      
      
      assertTrue(clone.getDescriptors(null).length == info.getDescriptors(null).length);
      
      // FIXME: equality not implemented to match field, value pairs
      //assertTrue(clone.getDescriptor("FirstConstructor", "constructor")
      //               .equals(
      //           info.getDescriptor("FirstConstructor", "constructor"))
      //);
      
      assertTrue(
            clone.getDescriptor("AnOperation", "operation")
            .getFieldValue("descriptorType")
            .equals(
            info.getDescriptor("AnOperation", "operation")
            .getFieldValue("descriptorType"))
      );
      
      assertTrue(
            clone.getDescriptor("AnOperation", "operation")
            .getFieldValue("name")
            .equals(
            info.getDescriptor("AnOperation", "operation")
            .getFieldValue("name"))
      );
      
   }
   
}
