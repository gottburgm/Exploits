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
package org.jboss.test.mx.mxbean.test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import junit.framework.AssertionFailedError;

import org.jboss.logging.Logger;
import org.jboss.mx.mxbean.CompositeDataInvocationHandler;
import org.jboss.mx.mxbean.MXBeanUtils;
import org.jboss.test.BaseTestCase;
import org.jboss.test.mx.mxbean.support.InvalidInterface;
import org.jboss.test.mx.mxbean.support.SimpleInterface;
import org.jboss.util.UnexpectedThrowable;

/**
 * AbstractMXBeanTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractMXBeanTest extends BaseTestCase
{
   private static Logger staticLog = Logger.getLogger(AbstractMXBeanTest.class);

   public AbstractMXBeanTest(String name)
   {
      super(name);
   }
   
   // @fixme move to AbstractTestCase
   public static void checkThrowableDeep(Class<? extends Throwable> wrapperExpected, Class<? extends Throwable> deepExpected, Throwable throwable) throws Exception
   {
      assertNotNull(deepExpected);
      assertNotNull(throwable);
      
      Throwable original = throwable;
      
      if (wrapperExpected != null)
      {
         if (wrapperExpected.isInstance(original) == false)
         {
            if (original instanceof Exception)
               throw (Exception) original;
            else if (original instanceof Error)
               throw (Error) original;
            else
               throw new UnexpectedThrowable("UnexpectedThrowable", original);
         }
         staticLog.debug("Got expected " + wrapperExpected.getName() + "(" + original + ")");
      }
      
      while (throwable.getCause() != null)
         throwable = throwable.getCause();
      
      if (deepExpected.isInstance(throwable) == false)
      {
         if (original instanceof Exception)
            throw (Exception) original;
         else if (original instanceof Error)
            throw (Error) original;
         else
            throw new UnexpectedThrowable("UnexpectedThrowable", original);
      }
      else
      {
         staticLog.debug("Got expected " + deepExpected.getName() + "(" + throwable + ")");
      }
   }
   
   // @fixme move to AbstractTestCase
   public static void checkThrowableDeep(Class<? extends Throwable> expected, Throwable throwable) throws Exception
   {
      checkThrowableDeep(null, expected, throwable);
   }
   
   // @fixme move to AbstractTestCase
   public static <T> T assertInstanceOf(Class<T> expected, Object object) throws Exception
   {
      if (object == null)
         return null;
      assertTrue(object.getClass(). getName() + " is not an instance of " + expected.getName(), expected.isInstance(object));
      return expected.cast(object);
   }
   
   public static void checkArrayEquals(Object expected, Object actual)
   {
      Object[] a1 = (Object[]) expected;
      Object[] a2 = (Object[]) actual;
      if (Arrays.deepEquals(a1, a2) == false)
         throw new AssertionFailedError("Expected: " + a1 + "=" + Arrays.deepToString(a1) + " got " + a2 + "=" + Arrays.deepToString(a2));
   }
   
   @SuppressWarnings("unchecked")
   public static void checkCompositeDataHandlerEquals(Object expected, Object actual)
   {
      if (expected == null)
      {
         assertEquals(expected, actual);
         return;
      }

      CompositeDataInvocationHandler handler = (CompositeDataInvocationHandler) Proxy.getInvocationHandler(actual);
      CompositeData data = handler.getCompositeData();
      CompositeType type = data.getCompositeType();
      Set<String> names = type.keySet();
      Class clazz = expected.getClass();
      for (String name : names)
      {
         OpenType itemType = type.getType(name);
         try
         {
            Method method = MXBeanUtils.getCompositeDataMethod(clazz, name, itemType == SimpleType.BOOLEAN);
            Object expectedValue = method.invoke(expected, null);
            Object actualValue = handler.invoke(actual, method, null);
            assertEquals(expectedValue, actualValue);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Error e)
         {
            throw e;
         }
         catch (Throwable t)
         {
            throw new RuntimeException(t);
         }
      }
   }
   
   public static void checkValueEquals(Object expected, Object actual)
   {
      if (actual == null)
         assertEquals(expected, actual);
      else if (actual instanceof Proxy)
         checkCompositeDataHandlerEquals(expected, actual);
      else if (actual.getClass().isArray())
         checkArrayEquals(expected, actual);
      else
         assertEquals(expected, actual);
   }

   protected CompositeData createCompositeData(String name, String[] keys, Object[] values) throws Exception
   {
      assertNotNullArray("values", values);

      Class[] types = new Class[values.length];
      for (int i = 0; i < values.length; ++i)
         types[i] = values[i].getClass();
      
      return createCompositeData(name, keys, types, values);
   }
   
   protected CompositeData createCompositeData(String name, String[] keys, Class[] types, Object[] values) throws Exception
   {
      CompositeType compositeType = createCompositeType(name, keys, types);
      return new CompositeDataSupport(compositeType, keys, values);
   }
   
   protected CompositeData createCompositeData(String name, String[] keys, OpenType[] openTypes, Object[] values) throws Exception
   {
      CompositeType compositeType = createCompositeType(name, keys, openTypes);
      return new CompositeDataSupport(compositeType, keys, values);
   }
   
   protected CompositeType createCompositeType(String name, String[] keys, Class[] types) throws Exception
   {
      assertNotNull(name);
      assertNotNullArray("keys", keys);
      assertNotNullArray("types", types);
      assertEquals(keys.length, types.length);
      
      OpenType[] openTypes = new OpenType[types.length];
      for (int i = 0; i < types.length; ++i)
         openTypes[i] = MXBeanUtils.getOpenType(types[i]);

      return new CompositeType(name, name, keys, keys, openTypes);
   }
   
   protected CompositeType createCompositeType(String name, String[] keys, OpenType[] openTypes) throws Exception
   {
      assertNotNull(name);
      assertNotNullArray("keys", keys);
      assertNotNullArray("types", openTypes);
      assertEquals(keys.length, openTypes.length);
      
      return new CompositeType(name, name, keys, keys, openTypes);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, String[] keys, Object[] values) throws Exception
   {
      assertNotNull(intf);
      return createCompositeDataProxy(intf, intf.getName(), keys, values);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, String name, String[] keys, Object[] values) throws Exception
   {
      CompositeData compositeData = createCompositeData(name, keys, values);
      return createCompositeDataProxy(intf, compositeData);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, String[] keys, Class[] types, Object[] values) throws Exception
   {
      assertNotNull(intf);
      return createCompositeDataProxy(intf, intf.getName(), keys, types, values);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, String name, String[] keys, Class[] types, Object[] values) throws Exception
   {
      CompositeData compositeData = createCompositeData(name, keys, types, values);
      return createCompositeDataProxy(intf, compositeData);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, String name, String[] keys, OpenType[] openTypes, Object[] values) throws Exception
   {
      CompositeData compositeData = createCompositeData(name, keys, openTypes, values);
      return createCompositeDataProxy(intf, compositeData);
   }
   
   protected <T> T createCompositeDataProxy(Class<T> intf, CompositeData compositeData)
   {
      return MXBeanUtils.createCompositeDataProxy(intf, compositeData);
   }
   
   protected CompositeData createTestCompositeData() throws Exception
   {
      return createCompositeData("Test", SimpleInterface.KEYS, SimpleInterface.VALUES);
   }
   
   protected SimpleInterface createTestCompositeDataProxy() throws Exception
   {
      return createCompositeDataProxy(SimpleInterface.class, SimpleInterface.KEYS, SimpleInterface.VALUES);
   }
   
   protected SimpleInterface createNullCompositeDataProxy() throws Exception
   {
      return createCompositeDataProxy(SimpleInterface.class, SimpleInterface.KEYS, SimpleInterface.TYPES, SimpleInterface.NULL_VALUES);
   }
   
   protected InvalidInterface createInvalidCompositeDataProxy() throws Exception
   {
      return createCompositeDataProxy(InvalidInterface.class, SimpleInterface.KEYS, SimpleInterface.VALUES);
   }
   
   protected SimpleInterface createTestCompositeDataProxy(String name) throws Exception
   {
      return createCompositeDataProxy(SimpleInterface.class, name, SimpleInterface.KEYS, SimpleInterface.VALUES);
   }
   
   protected MBeanServer createMBeanServer()
   {
      return MBeanServerFactory.newMBeanServer();
   }
   
   protected void assertNotNullArray(String context, Object[] array) throws Exception
   {
      assertNotNull(context + " is null ", array);
      for (int i = 0; i < array.length; ++i)
         assertNotNull(context + "[" + i + "] is null", array[i]);
   }

   public static Date createDate(int year, int month, int day)
   {
      Calendar calender = Calendar.getInstance();
      calender.clear();
      calender.set(year, month-1, day, 0, 0, 0);
      return calender.getTime();
   }
   
   public static String getUpperName(String name)
   {
      return Character.toUpperCase(name.charAt(0)) + name.substring(1);
   }
}
