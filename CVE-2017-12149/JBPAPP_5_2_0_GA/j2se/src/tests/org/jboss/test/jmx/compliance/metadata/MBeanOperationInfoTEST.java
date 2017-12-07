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

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests MBeanOperationInfo.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $ 
 */
public class MBeanOperationInfoTEST extends TestCase
{
   public MBeanOperationInfoTEST(String s)
   {
      super(s);
   }

   /**
    * Tests <tt>MBeanOperationInfo(String descr, Method m)</tt> constructor.
    */
   public void testConstructorWithMethod()
   {
      try 
      {
         Class c = this.getClass();
         Method m = c.getMethod("testConstructorWithMethod", new Class[0]);
         
         MBeanOperationInfo info = new MBeanOperationInfo("This is a description.", m);
         
         assertTrue(info.getDescription().equals("This is a description."));
         assertTrue(info.getName().equals(m.getName()));
         assertTrue(info.getReturnType().equals("void"));
         assertTrue(info.getSignature().length == 0);
         assertTrue(info.getImpact() == MBeanOperationInfo.UNKNOWN);
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
    * Tests <tt>MBeanOperationInfo(String name, String descr, MBeanParameterInfo[] sign, String returnType, int impact)</tt> constructor.
    */
   public void testConstructor()
   {
      try
      {
         MBeanOperationInfo info = new MBeanOperationInfo(
               "MyOperation",
               "This is a description.",
               new MBeanParameterInfo[] {
                        new MBeanParameterInfo("FooParam", "java.lang.Object", "description"),
                        new MBeanParameterInfo("BarParam", "java.lang.String", "description")
               },
               "java.util.StringBuffer",
               MBeanOperationInfo.INFO
         );
         
         assertTrue(info.getDescription().equals("This is a description."));
         assertTrue(info.getName().equals("MyOperation"));
         assertTrue(info.getReturnType().equals("java.util.StringBuffer"));
         assertTrue(info.getSignature().length == 2);
         assertTrue(info.getImpact() == MBeanOperationInfo.INFO);
         assertTrue(info.getSignature() [0].getName().equals("FooParam"));
         assertTrue(info.getSignature() [1].getName().equals("BarParam"));
         assertTrue(info.getSignature() [0].getDescription().equals("description"));
         assertTrue(info.getSignature() [1].getDescription().equals("description"));
         assertTrue(info.getSignature() [0].getType().equals("java.lang.Object"));
         assertTrue(info.getSignature() [1].getType().equals("java.lang.String"));
         
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
    * Tests the clone operation.
    */
   public void testClone()
   {
      try
      {
         MBeanOperationInfo info = new MBeanOperationInfo(
               "MyOperation",
               "This is a description.",
               new MBeanParameterInfo[] {
                        new MBeanParameterInfo("FooParam", "java.lang.Object", "description"),
                        new MBeanParameterInfo("BarParam", "java.lang.String", "description")
               },
               "java.util.StringBuffer",
               MBeanOperationInfo.ACTION_INFO
         );
         
         MBeanOperationInfo clone = (MBeanOperationInfo)info.clone();
         
         assertTrue(clone.getDescription().equals("This is a description."));
         assertTrue(clone.getName().equals("MyOperation"));
         assertTrue(clone.getReturnType().equals("java.util.StringBuffer"));
         assertTrue(clone.getSignature().length == 2);
         assertTrue(clone.getImpact() == MBeanOperationInfo.ACTION_INFO);
         assertTrue(clone.getSignature() [0].getName().equals("FooParam"));
         assertTrue(clone.getSignature() [1].getName().equals("BarParam"));
         assertTrue(clone.getSignature() [0].getDescription().equals("description"));
         assertTrue(clone.getSignature() [1].getDescription().equals("description"));
         assertTrue(clone.getSignature() [0].getType().equals("java.lang.Object"));
         assertTrue(clone.getSignature() [1].getType().equals("java.lang.String"));
         
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
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getDescription()</tt> accessor with <tt>null</tt> description.
    */
   public void testGetDescriptionNull()
   {
      try
      {
         MBeanOperationInfo info1 = new MBeanOperationInfo(
               "SomeName",
               null,
               new MBeanParameterInfo[] {
                        new MBeanParameterInfo("FooParam", "java.lang.Object", "description"),
                        new MBeanParameterInfo("BarParam", "java.lang.String", "description")
               },
               "java.util.StringBuffer",
               MBeanOperationInfo.ACTION_INFO
         );
         
         assertTrue(info1.getDescription() == null);
         
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
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getImpact()</tt> accessor with invalid value.
    */
   public void testGetImpactInvalid()
   {
      try
      {
         MBeanOperationInfo info1 = new MBeanOperationInfo(
               "SomeName",
               "some description",
               new MBeanParameterInfo[] {
                        new MBeanParameterInfo("FooParam", "java.lang.Object", "description"),
                        new MBeanParameterInfo("BarParam", "java.lang.String", "description")
               },
               "java.util.StringBuffer",
               -22342
         );
         
         // according to javadoc, getImpact() is only allowed to return a value that matches
         // either ACTION, ACTION_INFO, INFO or UNKNOWN constant value.
         if (info1.getImpact() != MBeanOperationInfo.ACTION)
            if (info1.getImpact() != MBeanOperationInfo.INFO)
               if (info1.getImpact() != MBeanOperationInfo.ACTION_INFO)
                  if (info1.getImpact() != MBeanOperationInfo.UNKNOWN)
                     
                     // JPL: This fails in RI. The spec doesn't define how invalid impact types should be
                     //      handled. This could be checked at construction time (early) or at getImpact()
                     //      invocation time (late). Since behaviour is not specified, I've opted to check
                     //      late and throw an JMRuntimeException in case there is an invalid impact value.
                     fail("FAILS IN RI: MBeanOperation.getImpact() is only allowed to return values that match either ACTION, ACTION_INFO, INFO or UNKNOWN constant values.");
      
         // should not reach here unless -22342 has somehow become a valid impact value (in which case this test should be modified)
         fail("ERROR IN TEST: invalid impact value test does not work correctly.");               
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Exception e)
      {
         return;
      }
      fail("Invalid impact");
   }
   
   /**
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getSignature()</tt> with <tt>null</tt> signature.
    */
   public void testGetSignatureNull()
   {
      try
      {
         MBeanOperationInfo info1 = new MBeanOperationInfo(
               "SomeName",
               "some description",
               null,
               "java.util.StringBuffer",
               MBeanOperationInfo.ACTION
         );
         
         assertTrue(info1.getSignature().length == 0);
         
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
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getSignature()</tt> with empty signature array.
    */
   public void testGetSignatureEmpty()
   {
      try
      {
         MBeanOperationInfo info1 = new MBeanOperationInfo(
               "SomeName",
               "some description",
               new MBeanParameterInfo[0],
               "java.util.StringBuffer",
               MBeanOperationInfo.ACTION
         );
         
         assertTrue(info1.getSignature().length == 0);
         
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
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getReturnType()</tt> with empty return type string.
    */
   public void testGetReturnTypeEmpty()
   {
      try
      {
         new MBeanOperationInfo(
               "SomeName",
               "some description",
               new MBeanParameterInfo[0],
               "",
               MBeanOperationInfo.ACTION
         );
      }
      catch (Exception e)
      {
         return;
      }
      fail("An empty return type is not a valid java identifier");
   }


   /**
    * Tests <tt>MBeanOperationInfo</tt> creation and <tt>getReturnType()</tt> with <tt>null</tt> return type.
    */
   public void testGetReturnTypeNull()
   {
      try
      {
         new MBeanOperationInfo(
               "SomeName",
               "some description",
               new MBeanParameterInfo[0],
               "",
               MBeanOperationInfo.ACTION
         );
      }
      catch (Exception e)
      {
         return;
      }
      fail("A null return type is not a valid java identifier");
   }

   public void testGetReturnTypeInvalid()
   {
      try
      {
         new MBeanOperationInfo(
               "SomeName",
               "some description",
               new MBeanParameterInfo[0],
               "invalid type",
               MBeanOperationInfo.ACTION
         );
      }
      catch (Exception e)
      {
         return;
      }
      fail("'invalid type' return type is not a valid java identifier");
   }
}
