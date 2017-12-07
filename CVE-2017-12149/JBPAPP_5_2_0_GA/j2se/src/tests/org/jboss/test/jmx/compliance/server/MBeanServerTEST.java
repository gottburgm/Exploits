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

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.MLet;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.jboss.test.jmx.compliance.server.support.BabarError;
import org.jboss.test.jmx.compliance.server.support.Base;
import org.jboss.test.jmx.compliance.server.support.BaseMBean;
import org.jboss.test.jmx.compliance.server.support.Broadcaster;
import org.jboss.test.jmx.compliance.server.support.Derived;
import org.jboss.test.jmx.compliance.server.support.Dynamic;
import org.jboss.test.jmx.compliance.server.support.ExceptionOnTheRun;
import org.jboss.test.jmx.compliance.server.support.LockedTest;
import org.jboss.test.jmx.compliance.server.support.LockedTest2;
import org.jboss.test.jmx.compliance.server.support.LockedTest3;
import org.jboss.test.jmx.compliance.server.support.MBeanListener;
import org.jboss.test.jmx.compliance.server.support.MyScreamingException;
import org.jboss.test.jmx.compliance.server.support.Test;
import org.jboss.test.jmx.compliance.server.support.Test2;
import org.jboss.test.jmx.compliance.server.support.Test3;
import org.jboss.test.jmx.compliance.server.support.Test4;
import org.jboss.test.jmx.compliance.server.support.Unrelated;
import org.jboss.test.jmx.compliance.server.support.UnrelatedMBean;

/**
 * Tests the MBean server impl. through the <tt>MBeanServer</tt> interface.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class MBeanServerTEST
   extends TestCase
{
   URL location;
   
   public MBeanServerTEST(String s) throws Exception
   {
      super(s);
      
      // Workout the output location for the dynamically loaded files
      location = getClass().getResource("/org/jboss/test/jmx/compliance/server/MBeanServerTEST.class");
      String jarPath = location.getPath();
      int i = jarPath.indexOf('!');
      if (i != -1)
      {
         jarPath = jarPath.substring(0, i);
         location = new URL(jarPath);
         location = new URL(location, "file:../");
      }
   }
   
   // MBeanServer invoke --------------------------------------------
   
   /**
    * Tests invoke with primitive boolean return type. <p>
    */
   public void testInvokeWithPrimitiveBooleanReturn() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      Boolean bool = (Boolean)server.invoke(name, "opWithPrimBooleanReturn", null, null);
      
      assertTrue(bool.booleanValue() == true);         
   }

   /**
    * Tests invoke with primitive long array return type. <p>
    */
   public void testInvokeWithPrimitiveLongArrayReturn() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      long[] array = (long[])server.invoke(name, "opWithPrimLongArrayReturn", null, null);
      
      assertTrue(array [0] == 1);
      assertTrue(array [1] == 2);
      assertTrue(array [2] == 3);
   }

   /**
    * Tests invoke with Long array return type. <p>
    */
   public void testInvokeWithLongArrayReturn() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      Long[] array = (Long[])server.invoke(name, "opWithLongArrayReturn", null, null);
      
      assertTrue(array [0].longValue() == 1);
      assertTrue(array [1].longValue() == 2);
      assertTrue(array [2].longValue() == 3);
   }
   
   /**
    * Tests invoke with primitive long return type. <p>
    */
   public void testInvokeWithPrimitiveLongReturn() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      Long l = (Long)server.invoke(name, "opWithPrimLongReturn", null, null);
      
      assertTrue(l.longValue() == 1234567890123l);         
   }

   /**
    * Tests invoke with primitive double return type. <p>
    */
   public void testInvokeWithPrimitiveDoubleReturn() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      Double d = (Double)server.invoke(name, "opWithPrimDoubleReturn", null, null);
      
      assertTrue(d.doubleValue() == 0.1234567890123d);
   }
   
   /**
    * Tests invoke with long signature. <p>
    */
   public void testInvokeWithLongSignature() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      server.invoke(name, "opWithLongSignature",
      new Object[] { new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5),
                     new Integer(6), new Integer(7), new Integer(8), new Integer(9), new Integer(10),
                     new Integer(11), new Integer(12), new Integer(13), new Integer(14), new Integer(15),
                     new Integer(16), new Integer(17), new Integer(18), new Integer(19), new Integer(20) },
      new String[] { "int", "int", "int", "int", "int", "int", "int", "int", "int", "int",
                     "int", "int", "int", "int", "int", "int", "int", "int", "int", "int" }
      );
   }

   /**
    * Tests invoke with mixed types in signature, especially types with double
    * byte code length and arrays. <p>
    */
   public void testInvokeWithMixedSignature() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName(":test=test");
      server.registerMBean(new Test(), name);
      
      server.invoke(name, "opWithMixedSignature", 
      new Object[] { new Integer(1), new Double(2.2D), new Long(333L), new Boolean(true), new Byte((byte)0x02),
                     new Short((short)6), new long[]{7L, 8L}, new Long[]{new Long(1L), new Long(2L)}, new Short((short)9), new Byte((byte)10),
                     new Long(11L), new Double(1.2D), new Integer(13), new Integer(14), new Integer(15),
                     new Integer(16), new Integer(17), new Integer(18), new Integer(19), new Integer(20) },
      new String[] { "int", "double", "long", "boolean", "byte", "short", "[J", "[Ljava.lang.Long;", "java.lang.Short", "java.lang.Byte",
                     "java.lang.Long", "java.lang.Double", "int", "int", "int", "int", "int", "int", "int", "int" }
      );         
   }

   
   /**
    * Attempts to invoke a method on an unregistered MBean; <tt>InstanceNotFoundException</tt> should occur.
    */
   public void testInvokeWithNonExistantMBean() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.invoke(new ObjectName(":mbean=doesnotexist"), "noMethod", null, null);

         // should not reach here
         fail("InstanceNotFoundException was not thrown from an invoke operation on a non-existant MBean.");
      }
      catch (InstanceNotFoundException e)
      {
         // should get here
      }
      
   }

   /**
    * Attempts to invoke a MBean operation that throws a business exception; <tt>MBeanException</tt> should be thrown.
    */
   public void testInvokeWithBusinessException() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.invoke(name, "operationWithException", null, null);

         // should not get here
         fail("MBeanException was not thrown.");
      }
      catch (MBeanException e)
      {
         // this is expected
         assertTrue(e.getTargetException() instanceof MyScreamingException);
      }
   }


   // MBeanServer getAttribute --------------------------------------
   
   public void testGetAttributeWithNonExistingAttribute() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.getAttribute(new ObjectName("JMImplementation:type=MBeanServerDelegate"), "Foo");

         // should not reach here
         fail("AttributeNotFoundexception was not thrown when invoking getAttribute() call on a non-existant attribute.");
      }
      catch (AttributeNotFoundException e)
      {
         // Expecting this.
      }
   }

   public void testGetAttributeWithBusinessException() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.getAttribute(name, "ThisWillScream");

         // should not reach here
         fail("Did not throw the screaming exception");
      }
      catch (MBeanException e)
      {
         // this is expected
         assertTrue(e.getTargetException() instanceof MyScreamingException);
      }
   }

   public void testGetAttributeWithNonExistingMBean() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:name=DoesNotExist");

         server.getAttribute(name, "Whatever");

         // should not reach here
         fail("InstanceNotFoundException was not thrown on a nonexistant MBean.");
      }
      catch (InstanceNotFoundException e)
      {
         // this is expected
      }
   }

   public void testGetAttributeWithUncheckedException() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.getAttribute(name, "ThrowUncheckedException");

         // should not reach here
         fail("RuntimeMBeanException was not thrown");
      }
      catch (RuntimeMBeanException e)
      {
         // this is expected
         assertTrue(e.getTargetException() instanceof ExceptionOnTheRun);
      }
   }

   public void testGetAttributeWithError() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.getAttribute(name, "Error");

         // should not reach here
         fail("Error was not thrown");
      }
      catch (RuntimeErrorException e)
      {
         // this is expected
         assertTrue(e.getTargetError() instanceof BabarError);
      }
   }

   
   // MBeanServer setAttribute --------------------------------------
   
   public void testSetAttributeWithNonExistingAttribute() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.setAttribute(new ObjectName("JMImplementation:type=MBeanServerDelegate"), new Attribute("Foo", "value"));

         // should not reach here
         fail("AttributeNotFoundexception was not thrown when invoking getAttribute() call on a non-existant attribute.");
      }
      catch (AttributeNotFoundException e)
      {
         // Expecting this.
      }
   }

   public void testSetAttributeWithBusinessException() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.setAttribute(name, new Attribute("ThisWillScream", "value"));

         // should not reach here
         fail("Did not throw the screaming exception");
      }
      catch (MBeanException e)
      {
         // this is expected
         assertTrue(e.getTargetException() instanceof MyScreamingException);
      }
   }

   public void testSetAttributeWithNonExistingMBean() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:name=DoesNotExist");

         server.setAttribute(name, new Attribute("Whatever", "nothing"));

         // should not reach here
         fail("InstanceNotFoundException was not thrown on a nonexistant MBean.");
      }
      catch (InstanceNotFoundException e)
      {
         // this is expected
      }
   }

   public void testSetAttributeWithUncheckedException() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.setAttribute(name, new Attribute("ThrowUncheckedException", "value"));

         // should not reach here
         fail("RuntimeMBeanException was not thrown");
      }
      catch (RuntimeMBeanException e)
      {
         // this is expected
         assertTrue(e.getTargetException() instanceof ExceptionOnTheRun);
      }
   }

   public void testSetAttributeWithError() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         ObjectName name = new ObjectName("test:test=test");
         server.registerMBean(new Test(), name);

         server.setAttribute(name, new Attribute("Error", "value"));

         // should not reach here
         fail("Error was not thrown");
      }
      catch (RuntimeErrorException e)
      {
         // this is expected
         assertTrue(e.getTargetError() instanceof BabarError);
      }
   }

   
   // MBeanServer instantiate ---------------------------------------
   
   /**
    * Tests instantiate(String className). Class defined by system classloader.
    */
   public void testInstantiateWithDefaultConstructor() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      Object o = server.instantiate("org.jboss.test.jmx.compliance.server.support.Test");
      
      assertTrue(o instanceof org.jboss.test.jmx.compliance.server.support.Test);
   }
   
   /**
    * Tests instantiate(String className) with constructor that throws a checked application exception.
    * Class defined by system classloader.
    */
    public void testInstantiateWithDefaultConstructorAndApplicationException() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest");
          
          // shouldn't get here
          fail("Instantiate should have thrown an MBeanException.");
       }
       catch (MBeanException e)
       {
          // this is expected
       }
    }
    
    /**
     * Tests instantiate(String className) with constructor that throws an unchecked application exception.
     * Class defined by the system classloader.
     */
    public void testInstantiateWithDefaultConstructorAndRuntimeException() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest2");
          
          // shouldn't get here
          fail("Instantiate should have thrown a RuntimeMBeanException.");
       }
       catch (RuntimeMBeanException e)
       {
          // this is expected
       }
    }
    
    /**
     * Tests instantiate(String className) with constructor that throws an error.
     * Class defined by the system classloader.
     */
    public void testInstantiateWithDefaultConstructorAndError() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest3");
          
          // shouldn't get here
          fail("Instantiate should have thrown a RuntimeErrorException.");
       }
       catch (RuntimeErrorException e)
       {
          // this is expected
       }
    }
    
    /**
     * Tests instantiate(String className) with constructor that fails with an unchecked exception in static init block.
     * Class defined by the system classloader.
     */
    public void testInstantiateWithDefaultConstructorAndExceptionInInit() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          
          // FAILS IN RI
          try
          {
             server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest4");
          }
          catch (ExceptionInInitializerError e)
          {
             // RI lets this error through unwrapped. In general, the MBean server is responsible
             // of wrapping all errors and exceptions from MBeans and resource classes with either
             // RuntimeErrorException, RuntimeMBeanException or MBeanException. The javadoc is unclear in 
             // this case should a ReflectionException or MBeanException be thrown (neither one can wrap an
             // Error though). JBossMX throws an RuntimeMBeanException in case of an unchecked exception in
             // static initializer and a RuntimeErrorException in case of an error in static initializer.
             fail("FAILS IN RI: MBeanServer fails to wrap an error or exception from a static initializer block correctly.");
          }
          
          // shouldn't get here
          fail("Instantiate should have thrown a RuntimeMBeanException.");
       }
       catch (RuntimeMBeanException e)
       {
          // this is expected
          
          assertTrue(e.getTargetException() instanceof NullPointerException);
       }
    }
    
    /**
     * Tests instatiante(String className) with constructor that fails with an error in static init block.
     * Class defined by the system classloader.
     */
    public void testInstantiateWithDefaultConstructorAndErrorInInit() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          
          // FAILS IN RI
          try
          {
             server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest5");
          }
          catch (BabarError e)
          {
             // RI lets this error through unwrapped. In general, the MBean server is responsible
             // of wrapping all errors and exceptions from MBeans and resource classes with either
             // RuntimeErrorException, RuntimeMBeanException or MBeanException. The javadoc is unclear in 
             // this case should a ReflectionException or MBeanException be thrown (neither one can wrap an
             // Error though). JBossMX throws an RuntimeMBeanException in case of an unchecked exception in
             // static initializer and a RuntimeErrorException in case of an error in static initializer.            
             fail("FAILS IN RI: MBeanServer fails to wrap an error or exception from a static initializer block correctly.");
          }
          
          // shouldn't get here
          fail("Instantiate should have thrown a RuntimeErrorException.");
       }
       catch (RuntimeErrorException e)
       {
          // this is expected
          
          assertTrue(e.getTargetError() instanceof org.jboss.test.jmx.compliance.server.support.BabarError);
       }
    }
    
    /**
     * Tests instantiate(String className) with unfound class.
     */
    public void testInstantiateWithDefaultConstructorAndUnknownClass() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("foo.Bar");
          
          // should not get here
          fail("Instantiate should have thrown a ReflectionException.");
       }
       catch (ReflectionException e)
       {
          // this is expected
          assertTrue(e.getTargetException() instanceof ClassNotFoundException);
       }
    }
    
    /**
     * Tests instantiate(String className) with class that doesn't have a default constructor.
     */
    public void testInstantiateWithMissingDefaultConstructor() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest6");
          
          // should not get here
          fail("Instantiate should have thrown a ReflectionException.");
       }
       catch (ReflectionException e)
       {
          // this is expected
       }
    }
    
    /**
     * Tests instantiate(String className) with protected (no access) no args constructor.
     */
    public void testInstantiateWithInaccessibleNoArgsConstructor() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.instantiate("org.jboss.test.jmx.compliance.server.support.ConstructorTest7");
          
          // should not get here
          fail("Instantiate should have thrown a ReflectionException.");
       }
       catch (ReflectionException e)
       {
          // this is expected
       }
    }

   /**
    * Tests instantiate(String className) with null class name. According to
    * javadoc, should throw RuntimeOperationsException wrapping IllegalArgException.
    */
   public void testInstantiateWithNullClassName() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate(null);

         // should not reach here
         fail("incorrect exception behavior");
      }
      catch (RuntimeOperationsException e)
      {
         // expected
         
         // check that it wraps IAE
         assertTrue(e.getTargetException() instanceof IllegalArgumentException);
      }
   }  

   /**
    * Tests instantiate(String className) with empty class name string. should
    * throw ReflectionException wrapping CNFE.
    */
   public void testInstantiateWithEmptyClassName() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate("");

         // should not reach here
         fail("incorrect exception/classloading behavior");
      }
      catch (ReflectionException e)
      {
         // expected
         
         // check that it wraps CNFE
         assertTrue(e.getTargetException() instanceof ClassNotFoundException);
      }
   }  

   /**
    * Tests instantiate(String className, ObjectName loader) with null class name. According to
    * javadoc, should throw RuntimeOperationsException wrapping IllegalArgException.
    */
   public void testInstantiateWithNullClassName2() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate(null, null);

         // should not reach here
         fail("incorrect exception behavior");
      }
      catch (RuntimeOperationsException e)
      {
         // expected
         
         // check that it wraps IAE
         assertTrue(e.getTargetException() instanceof IllegalArgumentException);
      }
   }  

   /**
    * Tests instantiate(String className, ObjectName loader) with empty class name string. should
    * throw ReflectionException wrapping CNFE.
    */
   public void testInstantiateWithEmptyClassName2() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate("", null);

         // should not reach here
         fail("incorrect exception/classloading behavior");
      }
      catch (ReflectionException e)
      {
         // expected
         
         // check that it wraps CNFE
         assertTrue(e.getTargetException() instanceof ClassNotFoundException);
      }
   }  

   /**
    * Tests instantiate(String className, Object[] args, String[] sign) with null
    * class name. According to javadoc, should throw RuntimeOperationsException
    * wrapping IllegalArgException.
    */
   public void testInstantiateWithNullClassName3() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate(null, null, null);

         // should not reach here
         fail("incorrect exception behavior");
      }
      catch (RuntimeOperationsException e)
      {
         // expected
         
         // check that it wraps IAE
         assertTrue(e.getTargetException() instanceof IllegalArgumentException);
      }
   }  

   /**
    * Tests instantiate(String className, Object[] args, String[] sign) with
    * empty class name string. should throw ReflectionException wrapping CNFE.
    */
   public void testInstantiateWithEmptyClassName3() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate("", null, null);

         // should not reach here
         fail("incorrect exception/classloading behavior");
      }
      catch (ReflectionException e)
      {
         // expected
         
         // check that it wraps CNFE
         assertTrue(e.getTargetException() instanceof ClassNotFoundException);
      }
   }  

   /**
    * Tests instantiate(String className, ObjectName loader, Object[] args, String[] sign)
    * with null class name. According to javadoc, should throw RuntimeOperationsException
    * wrapping IllegalArgException.
    */
   public void testInstantiateWithNullClassName4() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate(null, null, null, null);

         // should not reach here
         fail("incorrect exception behavior");
      }
      catch (RuntimeOperationsException e)
      {
         // expected
         
         // check that it wraps IAE
         assertTrue(e.getTargetException() instanceof IllegalArgumentException);
      }
   }  

   /**
    * Tests instantiate(String className, ObjectName loader, Object[] args, String[] sign)
    * with empty class name string. should throw ReflectionException wrapping CNFE.
    */
   public void testInstantiateWithEmptyClassName4() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerFactory.newMBeanServer();
         server.instantiate("", null, null, null);

         // should not reach here
         fail("incorrect exception/classloading behavior");
      }
      catch (ReflectionException e)
      {
         // expected
         
         // check that it wraps CNFE
         assertTrue(e.getTargetException() instanceof ClassNotFoundException);
      }
   }  
    
   /**
    * Tests instantiate(String className) classloading behaviour. According to
    * javadoc, DLR should be used to instantiate the class
    */
   public void testInstantiateWithDefaultLoaderRepository() throws Exception
   {
      // NOTE: 
      // the urls used here are relative to the location of the build.xml

      MBeanServer server = MBeanServerFactory.newMBeanServer();
      MLet mlet = new MLet();
      ObjectName name = new ObjectName(":test=test");
      
      // mlet cl to DLR
      try
      {
         mlet.addURL(new URL(location, "lib/jmxcompliance-Test.jar"));
         server.registerMBean(mlet, name);
      
         server.instantiate("org.jboss.test.jmx.compliance.server.support.AClass");
      }
      finally
      {
         try
         {
            server.unregisterMBean(name);
         }
         catch (Exception ignored) {}
      }
      
      //assertTrue(o.getClass().getClassLoader().equals(mlet));
   }  
   
   
   /**
    * Tests instantiate(String className, ObjectName loader) classloading behaviour. According to
    * javadoc, DLR should be used to instantiate the class. This should fail as
    * the MLet MBean is never added to the agent and therefore not in the DLR.
    */
   public void testInstantiateWithDefaultLoaderRepository2() throws Exception
   {
      try
      {
         // NOTE: 
         // the urls used here are relative to the location of the build.xml

         MBeanServer server = MBeanServerFactory.newMBeanServer();
         MLet mlet = new MLet();
      
         // mlet cl to DLR
         mlet.addURL("file:./output/etc/test/compliance/server/Test.jar");
         //server.registerMBean(mlet, new ObjectName(":test=test"));
      
         server.instantiate("org.jboss.test.jmx.compliance.server.support.AClass");
      
//
// FIXME: this test won't work until we have means to reset the JVM wide
//        loader repository
//

         // should not reach here
         //fail("incorrect classloading behavior");
         //assertTrue(o.getClass().getClassLoader().equals(mlet));
      }
      catch (ReflectionException e)
      {
         // expected
         
         // check that it wraps CNFE
         assertTrue(e.getTargetException() instanceof ClassNotFoundException);
      }
   }  
   
    // MBeanServer registerMBean ------------------------------------
    
    /**
     * Tests registering with null object name.
     */
    public void testRegisterNullObjectName() throws Exception
    {
       boolean caught = false;
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.registerMBean(new Test(), null);
       }
       catch (RuntimeOperationsException e)
       {
          if (e.getTargetException() instanceof IllegalArgumentException)
             caught = true;
          else
             fail("Wrong wrapped exception " + e.getTargetException());
       }
       if (caught == false)
          fail("Allowed to register with a null object name");
    }
    
    /**
     * Tests registering with a pattern object name.
     */
    public void testRegisterPatternObjectName() throws Exception
    {
       boolean caught = false;
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.registerMBean(new Test(), new ObjectName("Domai?:type=test"));
       }
       catch (RuntimeOperationsException e)
       {
          if (e.getTargetException() instanceof IllegalArgumentException)
             caught = true;
          else
             fail("Wrong wrapped exception " + e.getTargetException());
       }
       if (caught == false)
          fail("Allowed to register with a pattern object name");
    }
    
    /**
     * Tests registering into JMImplementation
     */
    public void testRegisterJMImplementationObjectName() throws Exception
    {
       boolean caught = false;
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.registerMBean(new Test(), new ObjectName("JMImplementation:type=test"));
       }
       catch (RuntimeOperationsException e)
       {
          if (e.getTargetException() instanceof IllegalArgumentException)
             caught = true;
          else
             fail("Wrong wrapped exception " + e.getTargetException());
       }
       if (caught == false)
          fail("Allowed to register into JMImplementation");
    }
    
    /**
     * Tests registering into JMImplementation using default domain
     */
    public void testRegisterJMImplementationDefaultDomainObjectName() throws Exception
    {
       boolean caught = false;
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer("JMImplementation");
          server.registerMBean(new Test(), new ObjectName(":type=test"));
       }
       catch (RuntimeOperationsException e)
       {
          if (e.getTargetException() instanceof IllegalArgumentException)
             caught = true;
          else
             fail("Wrong wrapped exception " + e.getTargetException());
       }
       if (caught == false)
          fail("Allowed to register into JMImplementation");
    }
    
    
   /**
    * Tests register for an MBean that throws unchecked exception from preRegister()
    */
   public void testRegisterMBeanOnExceptionFromPreRegister() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:foo=bar");
    
      try
      {
         server.registerMBean(new Test2(), name);
         
         // should not reach here
         fail("Test2 registered despite of throwing an exception from the preRegister() method.");
      }
      catch (MBeanRegistrationException e)
      {
         // expected
         assertTrue(!server.isRegistered(name));
         assertTrue(e.getTargetException() instanceof java.lang.RuntimeException);
      }
      catch (RuntimeMBeanException e)
      {
         fail("FAILS IN RI: RuntimeMBeanException instead of MBeanRegistrationException?");
      }
    }
    
    /**
     * Tests register for an MBean that throws checked exception from preRegister()
     */
    public void testRegisterMBeanOnExceptionFromPreRegister2() throws Exception
    {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:foo=bar");
      
      try
      {
         server.registerMBean(new Test3(), name);
         
         // should not reach here
         fail("Test3 registered despite of throwin an exception from the preRegister() method");
      }
      catch (MBeanRegistrationException e)
      {
         // expected
         assertTrue(!server.isRegistered(name));
         assertTrue(e.getTargetException() instanceof MyScreamingException);
      }
    }
    
    /**
     * Tests register for an MBean that throws an MBeanRegistrationException from
     * preRegister() method.
     */
    public void testRegisterMBeanOnExceptionFromPreRegister3() throws Exception
    {
       MBeanServer server = MBeanServerFactory.newMBeanServer();
       ObjectName name = new ObjectName("test:foo=bar");
       
       try
       {
          server.registerMBean(new Test4(), name);
          
          // should not reach here
          fail("Test4 registered despite of throwing an exception from the preRegister() method.");
       }
       catch (MBeanRegistrationException e)
       {
          // expected
          assertTrue(!server.isRegistered(name));
          assertTrue(e.getTargetException() instanceof MyScreamingException);
       }
    }
    
    
    // MBeanServer unregisterMBean ----------------------------------
    
    /**
     * Tests unregister the delegate.
     */
    public void testUnregisterDelegate() throws Exception
    {
       boolean caught = false;
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          server.unregisterMBean(new ObjectName("JMImplementation:type=MBeanServerDelegate"));
       }
       // REVIEW: This exception type isn't specified, but it is logical
       //         and agrees with the RI.
       // JPL: agreed
       catch (RuntimeOperationsException e)
       {
          caught = true;
       }
       if (caught == false)
          fail("Allowed to unregister the delegate");
    }
    
    /**
     * Tests basic register/unregister
     */
    public void testBasicUnregister() throws Exception
    {
       MBeanServer server = MBeanServerFactory.newMBeanServer();
       ObjectName name = new ObjectName("test:foo=bar");
       
       server.registerMBean(new Test(), name);
       server.unregisterMBean(name);
    }
    
    /**
     * Tests unregister with default domain name
     */
    public void testUnregisterWithDefaultDomainName() throws Exception
    {
       try
       {
          MBeanServer server = MBeanServerFactory.newMBeanServer();
          ObjectName name = new ObjectName(":foo=bar");
          
          server.registerMBean(new Test(), name);
          server.unregisterMBean(name);
       
       }
       catch (InstanceNotFoundException e)
       {
          // FAILS IN RI: RI throws InstanceNotFoundException if you try to
          // unregister with implicit default domain name
          fail("FAILS IN RI: RI throws InstanceNotFoundException when an existing MBean is unregistered with an implicit default domain name.");
       }
    }
    
    /**
     * Tests unregister with default domain name gotten from ObjectInstance at registration time.
     */
     public void testUnregisterWithObjectNameFromRegistration() throws Exception
     {
        try
        {
           MBeanServer server = MBeanServerFactory.newMBeanServer();
           ObjectName name = new ObjectName(":foo=bar");
           
           ObjectInstance oi = server.registerMBean(new Test(), name);
           name = oi.getObjectName();
           
           server.unregisterMBean(name);
        
        }
        catch (InstanceNotFoundException e)
        {
           // FAILS IN RI: RI throws InstanceNotFoundExceptin if you try yo
           // unregister with implicit default domain name
           fail("FAILS IN RI: RI throws InstanceNotFoundException when an existing MBean is unregistered with an implicit default domain name retrieved from the ObjectInstance returned at registration time.");
        }
     }
    
   /**
    * Tests unregister for an MBean that prevents unregistration by throwing an
    * unchecked exception from its preDeregister() method.
    */
   public void testUnregisterMBeanOnExceptionFromPreDeregister() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:foo=bar");
    
      server.registerMBean(new LockedTest(), name);

      try
      {
         server.unregisterMBean(name);
         
         // should not reach here
         fail("LockedTest unregistered despite of throwing an exception from the preDeregister() method.");
      }
      catch (MBeanRegistrationException e)
      {
         // expected, LockedTest should prevent unregistration
         assertTrue(server.isRegistered(name));
         assertTrue(e.getTargetException() instanceof java.lang.RuntimeException);
      }
      catch (RuntimeMBeanException e)
      {
         // FAILS IN RI: according to spec (v1.0, p. 117) any exception thrown from the
         // preDeregister() method is wrapped in MBeanRegistrationException by the agent.
         fail("FAILS IN RI: spec v1.0: any exception thrown from MBean's preDeregister() method should be wrapped in an MBeanRegistrationException by the agent.");
      }
    }
    
    /**
     * Tests unregister for an MBean that prevents unregistration by throwing a
     * checked exception from its preDeregister() method.
     */
    public void testUnregisterMBeanOnExceptionFromPreDeregister2() throws Exception
    {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName name = new ObjectName("test:foo=bar");
      
      server.registerMBean(new LockedTest2(), name);
      
      try
      {
         
         server.unregisterMBean(name);
       
         // should not reach here
         fail("LockedTest2 unregistered despite of throwin an exception from the preDeregister() method");
      }
      catch (MBeanRegistrationException e)
      {
         // expected
         assertTrue(server.isRegistered(name));
         assertTrue(e.getTargetException() instanceof MyScreamingException);
      }
    }
    
    /**
     * Tests unregister for an MBean that prevents unregistration by throwing a
     * MBeanRegistrationException from its preDeregister() method. This should
     * be rethrown by the agent as-is, and not wrapped into another MBeanRegistrationException.
     */
    public void testUnregisterMBeanOnExceptionFromPreDeregister3() throws Exception
    {
       MBeanServer server = MBeanServerFactory.newMBeanServer();
       ObjectName name = new ObjectName("test:foo=bar");
       
       server.registerMBean(new LockedTest3(), name);
       
       try
       {
          server.unregisterMBean(name);
          
          // should not reach here
          fail("LockedTest3 unregistered despite of throwing an exception from the preDeregister() method.");
       }
       catch (MBeanRegistrationException e)
       {
          // expected
          assertTrue(server.isRegistered(name));
          assertTrue(e.getTargetException() instanceof MyScreamingException);
       }
    }
    
   // MBeanServer NotificationListener Plain -----------------------
    
   /**
    * Tests basic listener registration to server delegate
    */
   public synchronized void testAddNotificationListenerToDelegate() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener {

         int notificationCount = 0;
         
         public void handleNotification(Notification notification, Object handback)
         {
            try
            {
               notificationCount++;

               assertTrue(handback instanceof String);
               assertTrue(handback.equals("MyHandback"));
               assertTrue(notification.getSource().equals(new ObjectName("JMImplementation:type=MBeanServerDelegate")));
            }
            catch (Exception e)
            {
               fail("Unexpected error: " + e.toString());
            }
         }
      }
      
      MyNotificationListener listener = new MyNotificationListener();
      
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, filter, "MyHandback"
      );       
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));
    
      assertTrue(listener.notificationCount == 1);
   }
    
   /**
    * Tests multiple listeners with different handbacks
    */
   public synchronized void testAddMultipleListeners()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         Object handback;
         int result = 0;
         public MyNotificationListener(Object handback)
         {
            this.handback = handback;
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result++;
            assertEquals(this.handback, handback);
            result++;
         }
      }
      
      MyNotificationListener listener1 = new MyNotificationListener("handback1");
      MyNotificationListener listener2 = new MyNotificationListener("handback2");
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener1, null, "handback1"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback2"
      );       
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertTrue(listener1.result == 2);
      assertTrue(listener2.result == 2);
   }
    
   /**
    * Tests one listener multiple handbacks
    */
   public synchronized void testAddListenerMultipleHandbacks()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         boolean result1 = false;
         boolean result2 = false;
         public void handleNotification(Notification notification, Object handback)
         {
            if (handback.equals("handback1"))
               result1 = true;
            else if (handback.equals("handback2"))
               result2 = true;
            else
               fail("Unexpected handback: " + handback);
         }
      }
      
      MyNotificationListener listener = new MyNotificationListener();
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, null, "handback1"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, null, "handback2"
      );       
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertTrue(listener.result1);
      assertTrue(listener.result2);
   }
    
   /**
    * Tests removing a notification listener including multiple handbacks
    */
   public synchronized void testRemoveListener()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         Object handback;
         int result = 0;
         public MyNotificationListener(Object handback)
         {
            this.handback = handback;
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result++;
            assertEquals(this.handback, handback);
            result++;
         }
      }
      
      class MyOtherNotificationListener implements NotificationListener
      {
         boolean result1 = false;
         boolean result2 = false;
         public void handleNotification(Notification notification, Object handback)
         {
            if (handback.equals("handback1"))
               result1 = true;
            else if (handback.equals("handback2"))
               result2 = true;
            else
               fail("Unexpected handback: " + handback);
         }
      }
      
      MyNotificationListener listener1 = new MyNotificationListener("handback1");
      MyOtherNotificationListener listener2 = new MyOtherNotificationListener();
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener1, null, "handback1"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback2"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback3"
      );       
      server.removeNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2
      );       
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertTrue(listener1.result == 2);
      assertTrue(listener2.result1 == false);
      assertTrue(listener2.result2 == false);
   }
        
   /**
    * Tests removing a notification listener triplet
    */
   public synchronized void testRemoveTriplet()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         Object handback;
         int result = 0;
         public MyNotificationListener(Object handback)
         {
            this.handback = handback;
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result++;
            assertEquals(this.handback, handback);
            result++;
         }
      }
      
      MyNotificationListener listener1 = new MyNotificationListener("handback1");
      MyNotificationListener listener2 = new MyNotificationListener("handback2");
      
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener1, null, "handback1"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback2"
      );       
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback3"
      );       
      server.removeNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener2, null, "handback3"
      );       
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertTrue(listener1.result == 2);
      assertTrue(listener1.result == 2);
   }

   /**
    * Tests removing a broadcaster
    */
   public synchronized void testRemoveBroadcaster()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         long result = 0;
         public MyNotificationListener()
         {
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result = notification.getSequenceNumber();
         }
      }

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener
      MyNotificationListener listener = new MyNotificationListener();
      server.addNotificationListener(broadcasterName, listener, null, null);

      // Test we get a notification
      broadcaster.doSomething();
      assertEquals(1, listener.result);

      // Remove the broadcaster
      server.unregisterMBean(broadcasterName);
      
      // This notification shouldn't work
      broadcaster.doSomething();
      try
      {
         assertEquals(1, listener.result);
      }
      catch (AssertionFailedError e)
      {
         fail("FAILS IN RI: Removing a notification broadcaster does not " +
              "remove the listeners registered against the object name.");
      }
   }
    
   /**
    * Tests adding the listener to different broadcasters
    */
   public synchronized void testAddListenerToTwoBroadcasters()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         long result = 0;
         public MyNotificationListener()
         {
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result++;
         }
      }

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener to the broadcaster
      MyNotificationListener listener = new MyNotificationListener();
      server.addNotificationListener(broadcasterName, listener, null, null);
      
      // Add the listener to the delegate
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, null, null
      );       

      // Test we get a notification from the broadcaster
      broadcaster.doSomething();
      assertEquals(1, listener.result);

      // Test we get a notification from the delegate
      server.registerMBean(new Test(), new ObjectName("Test:foo=bar"));
      assertEquals(2, listener.result);

      // Remove the broadcaster
      server.unregisterMBean(broadcasterName);
      assertEquals(3, listener.result);

      // Make sure we are still listening to the delegate
      server.unregisterMBean(new ObjectName("Test:foo=bar"));
      assertEquals(4, listener.result);
   }
    
   /**
    * Tests adding the listener to different broadcasters but remove one
    */
   public synchronized void testAddListenerToTwoBroadcastersRemoveOne()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      class MyNotificationListener implements NotificationListener
      {
         long result = 0;
         public MyNotificationListener()
         {
         }
         public void handleNotification(Notification notification, Object handback)
         {
            result++;
         }
      }

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener to the broadcaster
      MyNotificationListener listener = new MyNotificationListener();
      server.addNotificationListener(broadcasterName, listener, null, null);
      
      // Add the listener to the delegate
      server.addNotificationListener(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            listener, null, null
      );

      // Remove ourselves from the broadcaster
      server.removeNotificationListener(broadcasterName, listener);       

      // Test we get a notification from the broadcaster
      broadcaster.doSomething();
      assertEquals(0, listener.result);

      // Test we get a notification from the delegate
      server.registerMBean(new Test(), new ObjectName("Test:foo=bar"));
      assertEquals(1, listener.result);
   }
    
   // MBeanServer NotificationListener Object Name -----------------
    
   /**
    * Tests basic listener registration to server delegate
    */
   public synchronized void testaddMBeanToDelegate() throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener = new MBeanListener();
      ObjectName listenerName = new ObjectName("test:type=listener");
      server.registerMBean(listener, listenerName);
      
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
      
      ObjectName delegateName = 
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName, listenerName, filter, "MyHandback");
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));
    
      assertTrue(listener.count == 1);
      assertTrue(listener.source.equals(delegateName));
      assertTrue(listener.handback.equals("MyHandback"));
   }
    
   /**
    * Tests multiple listeners with different handbacks
    */
   public synchronized void testAddMBeanMultipleListeners()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);
      MBeanListener listener2 = new MBeanListener();
      ObjectName listenerName2 = new ObjectName("test:type=listener2");
      server.registerMBean(listener2, listenerName2);
      
      ObjectName delegateName = 
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName, listenerName1, null, "handback1");
      server.addNotificationListener(delegateName, listenerName2, null, "handback2");
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertEquals(1, listener1.count);
      assertEquals(listener1.source,delegateName);
      assertEquals(listener1.handback,"handback1");
      assertEquals(1, listener2.count);
      assertEquals(listener2.source,delegateName);
      assertEquals(listener2.handback,"handback2");
   }
    
   /**
    * Tests one listener multiple handbacks
    */
   public synchronized void testAddMBeanListenerMultipleHandbacks()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener = new MBeanListener("handback1", "handback2");
      ObjectName listenerName = new ObjectName("test:type=listener");
      server.registerMBean(listener, listenerName);
      
      ObjectName delegateName = 
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName, listenerName, null, "handback1");
      server.addNotificationListener(delegateName, listenerName, null, "handback2");
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));

      assertTrue(listener.count1 == 1);
      assertEquals(listener.source1,delegateName);
      assertEquals(listener.handback1,"handback1");
      assertTrue(listener.count2 == 1);
      assertEquals(listener.source2,delegateName);
      assertEquals(listener.handback2,"handback2");
   }
    
   /**
    * Tests removing a notification listener including multiple handbacks
    */
   public synchronized void testMBeanRemoveListener()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);
      MBeanListener listener2 = new MBeanListener("handback2", "handback3");
      ObjectName listenerName2 = new ObjectName("test:type=listener2");
      server.registerMBean(listener2, listenerName2);
      
      ObjectName delegateName = 
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName, listenerName1, null, "handback1");
      server.addNotificationListener(delegateName, listenerName2, null, "handback2");
      server.addNotificationListener(delegateName, listenerName2, null, "handback3");
      server.removeNotificationListener(delegateName, listenerName2);
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));
      assertTrue("Listener1 should get a notification", listener1.count == 1);
      assertTrue("Source should be the delegate", listener1.source.equals(delegateName));
      assertTrue("Listener1 should get handback1", listener1.handback.equals("handback1"));
      assertTrue("Listener2 should have no notiifcation", listener2.count == 0);
      assertTrue("Listener2 should have no notiifcation for handback2", listener2.count1 == 0);
      assertTrue("Listener2 should have no notiifcation for handback3", listener2.count2 == 0);
   }
    
   /**
    * Tests removing a notification listener triple
    */
   public synchronized void testMBeanRemoveTriplet()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);
      MBeanListener listener2 = new MBeanListener("handback2", "handback3");
      ObjectName listenerName2 = new ObjectName("test:type=listener2");
      server.registerMBean(listener2, listenerName2);
      
      ObjectName delegateName = 
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName, listenerName1, null, "handback1");
      server.addNotificationListener(delegateName, listenerName2, null, "handback2");
      server.addNotificationListener(delegateName, listenerName2, null, "handback3");
      server.removeNotificationListener(delegateName, listenerName2, null, "handback3");
    
      // force notification
      server.registerMBean(new Test(), new ObjectName(":foo=bar"));
      assertTrue("Listener1 should get a notification", listener1.count == 1);
      assertTrue("Source should be the delegate", listener1.source.equals(delegateName));
      assertTrue("Listener1 should get handback1", listener1.handback.equals("handback1"));
      assertTrue("Listener2 should get a notification", listener2.count1 == 1);
      assertTrue("Source should be the delegate", listener2.source1.equals(delegateName));
      assertTrue("Listener2 should get handback2", listener2.handback1.equals("handback2"));
      assertTrue("Listener2 should have no notiifcation for handback3", listener2.count2 == 0);
   }
    
   /**
    * Tests MBeanRedeploy notification
    */
   public synchronized void testMBeanRedeployNotification()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      MBeanListener listener = new MBeanListener();
      ObjectName listenerName = new ObjectName("test:type=listener");
      server.registerMBean(listener, listenerName);

      ObjectName broadcasterName = new ObjectName("test:type=Broadcaster");
      server.registerMBean(new Broadcaster(), broadcasterName);
      
      server.addNotificationListener(broadcasterName, listenerName, null, "handback1");
      server.removeNotificationListener(broadcasterName, listenerName, null, "handback1");
      server.unregisterMBean(broadcasterName);

      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      server.addNotificationListener(broadcasterName, listenerName, null, "handback2");
    
      // force notification
      broadcaster.doSomething();
      assertTrue("Listener should get a notification", listener.count == 1);
      assertTrue("Source should be the broadcaster", listener.source.equals(broadcasterName));
      assertTrue("Listener should get handback2", listener.handback.equals("handback2"));
   }
    
   /**
    * Tests removing a broadcaster
    */
   public synchronized void testMBeanRemoveBroadcaster()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener
      server.addNotificationListener(broadcasterName, listenerName1, null, null);

      // Test we get a notification
      broadcaster.doSomething();
      assertEquals(1, listener1.count);
      assertEquals(broadcasterName, listener1.source);

      // Remove the broadcaster
      server.unregisterMBean(broadcasterName);
      
      // This notification shouldn't work
      broadcaster.doSomething();
      try
      {
         assertEquals(1, listener1.count);
      }
      catch (AssertionFailedError e)
      {
         fail("FAILS IN RI: Removing a notification broadcaster does not " +
              "remove the listeners registered against the object name.");
      }
   }
    
   /**
    * Tests adding the listener to different broadcasters
    */
   public synchronized void testAddMBeanListenerToTwoBroadcasters()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener to the broadcaster
      server.addNotificationListener(broadcasterName, listenerName1, null, null);
      
      // Add the listener to the delegate
      ObjectName delegateName =
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName,listenerName1, null, null);       

      // Test we get a notification from the broadcaster
      broadcaster.doSomething();
      assertEquals(1, listener1.count);
      assertEquals(broadcasterName, listener1.source);

      try
      {
         // Test we get a notification from the delegate
         server.registerMBean(new Test(), new ObjectName("Test:foo=bar"));
         assertEquals(2, listener1.count);
         assertEquals(delegateName, listener1.source);

         // Remove the broadcaster
         server.unregisterMBean(broadcasterName);
         assertEquals(3, listener1.count);
         assertEquals(delegateName, listener1.source);

         // Make sure we are still listening to the delegate
         server.unregisterMBean(new ObjectName("Test:foo=bar"));
         assertEquals(4, listener1.count);
         assertEquals(delegateName, listener1.source);
      }
      catch (AssertionFailedError e)
      {
         fail("FAILS IN RI: Listener registered with ObjectName in MBeanServer " +
              "reports the wrong source for multiple broadcaster.");
      }
   }
    
   /**
    * Tests adding the listener to different broadcasters but remove one
    */
   public synchronized void testAddMBeanListenerToTwoBroadcastersRemoveOne()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      
      MBeanListener listener1 = new MBeanListener();
      ObjectName listenerName1 = new ObjectName("test:type=listener1");
      server.registerMBean(listener1, listenerName1);

      // Register the broadcaster
      ObjectName broadcasterName = new ObjectName("test:type=broadcaster");
      Broadcaster broadcaster = new Broadcaster();
      server.registerMBean(broadcaster, broadcasterName);
      
      // Add the listener to the broadcaster
      server.addNotificationListener(broadcasterName, listenerName1, null, null);
      
      // Add the listener to the delegate
      ObjectName delegateName =
         new ObjectName("JMImplementation:type=MBeanServerDelegate");
      server.addNotificationListener(delegateName,listenerName1, null, null);       

      // Remove ourselves from the broadcaster
      server.removeNotificationListener(broadcasterName, listener1);       

      // Test we get a notification from the broadcaster
      broadcaster.doSomething();
      assertEquals(0, listener1.count);

      // Test we get a notification from the delegate
      server.registerMBean(new Test(), new ObjectName("Test:foo=bar"));
      assertEquals(1, listener1.count);
      try
      {
         assertEquals(delegateName, listener1.source);   
      }
      catch (AssertionFailedError e)
      {
         fail("FAILS IN RI: Listener registered with ObjectName in MBeanServer " +
              "reports the wrong source for multiple broadcaster, " +
              "even when the broadcaster it reports has been removed.");
      }
   }
    
   public void testGetDomains()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();

      assertTrue("Only one domain at the start", server.getDomains().length == 1);
      assertEquals(server.getDomains()[0], "JMImplementation");

      server.registerMBean(new Test(), new ObjectName("Domain1:test=test1"));
      server.registerMBean(new Test(), new ObjectName("Domain1:test=test2"));
      server.registerMBean(new Test(), new ObjectName("Domain2:test=test1"));
      server.registerMBean(new Test(), new ObjectName("Domain3:test=test1"));

      assertTrue("Now four domains", server.getDomains().length == 4);
      List domains = Arrays.asList(server.getDomains());
      assertTrue("server has JMImplementation", domains.contains("JMImplementation"));
      assertTrue("server has Domain1", domains.contains("Domain1"));
      assertTrue("server has Domain2", domains.contains("Domain2"));
      assertTrue("server has Domain3", domains.contains("Domain3"));

      server.unregisterMBean(new ObjectName("Domain3:test=test1"));

      assertTrue("Now three domains", server.getDomains().length == 3);
      domains = Arrays.asList(server.getDomains());
      assertTrue("server has JMImplementation", domains.contains("JMImplementation"));
      assertTrue("server has Domain1", domains.contains("Domain1"));
      assertTrue("server has Domain2", domains.contains("Domain2"));
      assertTrue("server no longer has Domain3", domains.contains("Domain3") == false);

      server.unregisterMBean(new ObjectName("Domain1:test=test1"));

      assertTrue("Still three domains", server.getDomains().length == 3);
      domains = Arrays.asList(server.getDomains());
      assertTrue("server has JMImplementation", domains.contains("JMImplementation"));
      assertTrue("server still has Domain1", domains.contains("Domain1"));
      assertTrue("server has Domain2", domains.contains("Domain2"));
      assertTrue("server no longer has Domain3", domains.contains("Domain3") == false);
   }

   public void testIsInstanceOf()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName baseName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=Base");
      ObjectName derivedName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=Derived");
      ObjectName unrelatedName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=Unrelated");

      server.registerMBean(new Base(), baseName);
      server.registerMBean(new Derived(), derivedName);
      server.registerMBean(new Unrelated(), unrelatedName);

      assertTrue("Base is an instance Object",
         server.isInstanceOf(baseName, Object.class.getName()));
      assertTrue("Base is an instance BaseMBean",
         server.isInstanceOf(baseName, BaseMBean.class.getName()));
      assertTrue("Base is an instance Base",
         server.isInstanceOf(baseName, Base.class.getName()));
      assertTrue("Derived is an instance Object",
         server.isInstanceOf(derivedName, Object.class.getName()));
      assertTrue("Derived is an instance BaseMBean",
         server.isInstanceOf(derivedName, BaseMBean.class.getName()));
      assertTrue("Derived is an instance Base",
         server.isInstanceOf(derivedName, Base.class.getName()));
      assertTrue("Derived is an instance Derived",
         server.isInstanceOf(derivedName, Derived.class.getName()));
      assertTrue("Unrelated is an instance Object",
         server.isInstanceOf(unrelatedName, Object.class.getName()));
      assertTrue("Unrelated is an instance UnrelatedMBean",
         server.isInstanceOf(unrelatedName, UnrelatedMBean.class.getName()));
      assertTrue("Unrelated is an instance Unrelated",
         server.isInstanceOf(unrelatedName, Unrelated.class.getName()));

      assertTrue("Base is an not instance Derived",
         server.isInstanceOf(baseName, Derived.class.getName()) == false);
      assertTrue("Base is an not instance UnrelatedMBean",
         server.isInstanceOf(baseName, UnrelatedMBean.class.getName()) == false);
      assertTrue("Base is an not instance Unrelated",
         server.isInstanceOf(baseName, Unrelated.class.getName()) == false);
      assertTrue("Derived is an not instance UnrelatedMBean",
         server.isInstanceOf(derivedName, UnrelatedMBean.class.getName()) == false);
      assertTrue("Dervied is an not instance Unrelated",
         server.isInstanceOf(derivedName, Unrelated.class.getName()) == false);
      assertTrue("Unrelated is an not instance BaseMBean",
         server.isInstanceOf(unrelatedName, BaseMBean.class.getName()) == false);
      assertTrue("Unrelated is an not instance Base",
         server.isInstanceOf(unrelatedName, Base.class.getName()) == false);
      assertTrue("Unrelated is an not instance Derived",
         server.isInstanceOf(unrelatedName, Derived.class.getName()) == false);
   }

   public void testIsInstanceOfErrors()
      throws Exception
   {
      MBeanServer server = MBeanServerFactory.newMBeanServer();
      ObjectName baseName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=Base");
      ObjectName dynamicName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=Dynamic");
      ObjectName doesNotExistName = new ObjectName("MBeanServerTEST:type=testIsInstanceOf,name=DoesNotExist");

      server.registerMBean(new Base(), baseName);
      server.registerMBean(new Dynamic(), dynamicName);

      assertTrue("Base is not an instance of a class that does not exist",
         server.isInstanceOf(baseName, "does.not.exist") == false);

      assertTrue("FAILS IN RI: Not an instance if the getMBeanInfo reports a class name that does not exist",
         server.isInstanceOf(dynamicName, Object.class.getName()) == false);

      boolean caught = false;
      try
      {
         server.isInstanceOf(doesNotExistName, Object.class.getName());
      }
      catch (InstanceNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Should get an instance not found for a non-existent mbean", caught);
   }
}
