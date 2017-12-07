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


import java.lang.reflect.Method;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;

import org.jboss.test.jmx.compliance.metadata.support.Trivial;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests MBeanAttributeInfo.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $ 
 */
public class MBeanAttributeInfoTEST extends TestCase
{
   public MBeanAttributeInfoTEST(String s)
   {
      super(s);
   }

   /**
    * Tests <tt>MBeanAttributeInfo(String name, String descr, Method getter, Method setter)</tt> constructor.
    */
   public void testConstructorWithAccessorMethods()
   {
      try 
      {
         Class c = Trivial.class;
         Method getter = c.getMethod("getSomething", new Class[0]);
         Method setter = c.getMethod("setSomething", new Class[] { String.class });
         
         MBeanAttributeInfo info = new MBeanAttributeInfo("Something", "a description", getter, setter);
         
         assertTrue(info.getDescription().equals("a description"));
         assertTrue(info.getName().equals("Something"));
         assertTrue(info.getType().equals("java.lang.String"));
         assertTrue(info.isReadable() == true);
         assertTrue(info.isWritable() == true);
         assertTrue(info.isIs() == false);         
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }
 
   /**
    * Tests <tt>MBeanAttributeInfo(String name, String descr, Method getter, Method setter)</tt> with misplaced accessor methods.
    */
   public void testConstructorWithMisplacedAccessorMethods()
   {
      try
      {
         Class c = Trivial.class;
         Method getter = c.getMethod("getSomething", new Class[0]);
         Method setter = c.getMethod("setSomething", new Class[] { String.class });
         
         new MBeanAttributeInfo("Something", "a description", setter, getter);
         
         // shouldn't reach here
         fail("Introspection exception should have been thrown.");
      }
      catch (IntrospectionException e)
      {
         // this is expected
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }
   
   /**
    * Tests <tt>MBeanAttributeInfo(String name, String descr, Method getter, Method setter)</tt> with invalid getter method.
    */
   public void testConstructorWithInvalidGetterMethod()
   {
      try
      {
         Class c = Trivial.class;
         Method getter = c.getMethod("getSomethingInvalid", new Class[] { Object.class });
         Method setter = c.getMethod("setSomethingInvalid", new Class[] { String.class });
         
         new MBeanAttributeInfo("Something", "a description", getter, setter);
         
         // shouldn't reach here
         fail("Introspection exception should have been thrown.");
      }
      catch (IntrospectionException e)
      {
         // this is expected
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }
   
   /**
    * Tests <tt>MBeanAttributeInfo(String name, String descr, Method getter, Method setter)</tt> with invalid getter method (void return type).
    */
   public void testConstructorWithInvalidGetterMethod2()
   {
      try
      {
         Class c = Trivial.class;
         Method getter = c.getMethod("getSomethingInvalid2", new Class[] { } );
         Method setter = c.getMethod("setSomethingInvalid2", new Class[] { String.class });
         
         new MBeanAttributeInfo("Something", "a description", getter, setter);
         
         // shouldn't reach here
         fail("Introspection exception should have been thrown.");
      }
      catch (IntrospectionException e)
      {
         // this is expected
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         fail("Unexpected error: " + t.toString());
      }
   }

   public void testConstructorWithNonBooleanIsIs()
      throws Exception
   {
      try
      {
         new MBeanAttributeInfo("name", "type", "description", true, true, true);
      }
      catch (Exception e)
      {
         return;
      }
      fail("isIs is only allowed for boolean types");
   }

   public void testConstructorWithPrimitiveBooleanIsIs()
      throws Exception
   {
      new MBeanAttributeInfo("name", Boolean.TYPE.getName(), "description", true, true, true);
   }

   public void testConstructorWithObjectBooleanIsIs()
      throws Exception
   {
      new MBeanAttributeInfo("name", Boolean.class.getName(), "description", true, true, true);
   }

   public void testHashCode()
      throws Exception
   {
      MBeanAttributeInfo info1 = new MBeanAttributeInfo("name", "type", "description", true, true, false);
      MBeanAttributeInfo info2 = new MBeanAttributeInfo("name", "type", "description", true, true, false);

      assertTrue("Different instances with the same hashcode are equal", info1.hashCode() == info2.hashCode());
   }

   public void testEquals()
      throws Exception
   {
      MBeanAttributeInfo info = new MBeanAttributeInfo("name", "type", "description", true, true, false);

      assertTrue("Null should not be equal", info.equals(null) == false);
      assertTrue("Only MBeanAttributeInfo should be equal", info.equals(new Object()) == false);

      MBeanAttributeInfo info2 = new MBeanAttributeInfo("name", "type", "description", true, true, false);

      assertTrue("Different instances of the same data are equal", info.equals(info2));
      assertTrue("Different instances of the same data are equal", info2.equals(info));

      info2 = new MBeanAttributeInfo("name2", "type", "description", true, true, false);

      assertTrue("Different instances with different names are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different names are not equal", info2.equals(info) == false);

      info2 = new MBeanAttributeInfo("name", "type2", "description", true, true, false);

      assertTrue("Different instances with different types are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different types are not equal", info2.equals(info) == false);

      info2 = new MBeanAttributeInfo("name", "type", "description2", true, true, false);

      assertTrue("Different instances with different descriptions are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different descritpions are not equal", info2.equals(info) == false);

      info2 = new MBeanAttributeInfo("name", "type", "description", false, true, false);

      assertTrue("Different instances with different readables are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different readables are not equal", info2.equals(info) == false);

      info2 = new MBeanAttributeInfo("name", "type", "description", true, false, false);

      assertTrue("Different instances with different writables are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different writables are not equal", info2.equals(info) == false);

      info2 = new MBeanAttributeInfo("name", Boolean.TYPE.getName(), "description", true, true, true);

      assertTrue("Different instances with different isIs are not equal", info.equals(info2) == false);
      assertTrue("Different instances with different isIs are not equal", info2.equals(info) == false);
   }
}
