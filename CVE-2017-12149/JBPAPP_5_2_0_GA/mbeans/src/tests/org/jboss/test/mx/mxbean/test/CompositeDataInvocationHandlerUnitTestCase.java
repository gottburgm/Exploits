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

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.mxbean.CompositeDataInvocationHandler;
import org.jboss.test.mx.mxbean.support.InvalidInterface;
import org.jboss.test.mx.mxbean.support.SimpleInterface;

/**
 * CompositeDataInvocationHandlerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataInvocationHandlerUnitTestCase extends AbstractMXBeanTest
{
   public static Test suite()
   {
      return new TestSuite(CompositeDataInvocationHandlerUnitTestCase.class);
   }
   
   public CompositeDataInvocationHandlerUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testGetCompositeData() throws Exception
   {
      CompositeData compositeData = createTestCompositeData();
      CompositeDataInvocationHandler test = new CompositeDataInvocationHandler(compositeData);
      assertTrue(compositeData == test.getCompositeData());
   }
   
   public void testNullCompositeData() throws Exception
   {
      try
      {
         new CompositeDataInvocationHandler(null);
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowable(IllegalArgumentException.class, t);
      }
   }
   
   public void testToEquals() throws Exception
   {
      SimpleInterface test1 = createTestCompositeDataProxy("Test1");
      
      assertFalse(test1.equals(null));
      assertFalse(test1.equals(new Object()));
      assertTrue(test1.equals(test1));

      SimpleInterface test2 = createTestCompositeDataProxy("Test2");
      
      assertFalse(test1.equals(test2));
   }
   
   public void testHashCode() throws Exception
   {
      CompositeData compositeData = createTestCompositeData();
      SimpleInterface test = createCompositeDataProxy(SimpleInterface.class, compositeData);

      assertEquals(test.hashCode(), compositeData.hashCode());
   }
   
   public void testToString() throws Exception
   {
      CompositeData compositeData = createTestCompositeData();
      SimpleInterface test = createCompositeDataProxy(SimpleInterface.class, compositeData);

      assertEquals(test.toString(), compositeData.toString());
   }
   
   public void testGetSimpleTypes() throws Exception
   {
      SimpleInterface test = createTestCompositeDataProxy();
      assertEquals(SimpleInterface.bigDecimalValue, test.getBigDecimal());
      assertEquals(SimpleInterface.bigIntegerValue, test.getBigInteger());
      assertEquals(SimpleInterface.booleanValue, test.getBoolean());
      assertEquals(SimpleInterface.primitiveBooleanValue, test.isPrimitiveBoolean());
      assertEquals(SimpleInterface.byteValue, test.getByte());
      assertEquals(SimpleInterface.primitiveByteValue, test.getPrimitiveByte());
      assertEquals(SimpleInterface.characterValue, test.getCharacter());
      assertEquals(SimpleInterface.primitiveCharValue, test.getPrimitiveChar());
      assertEquals(SimpleInterface.dateValue, test.getDate());
      assertEquals(SimpleInterface.doubleValue, test.getDouble());
      assertEquals(SimpleInterface.primitiveDoubleValue, test.getPrimitiveDouble());
      assertEquals(SimpleInterface.floatValue, test.getFloat());
      assertEquals(SimpleInterface.primitiveFloatValue, test.getPrimitiveFloat());
      assertEquals(SimpleInterface.integerValue, test.getInteger());
      assertEquals(SimpleInterface.primitiveIntValue, test.getPrimitiveInt());
      assertEquals(SimpleInterface.longValue, test.getLong());
      assertEquals(SimpleInterface.primitiveLongValue, test.getPrimitiveLong());
      assertEquals(SimpleInterface.objectNameValue, test.getObjectName());
      assertEquals(SimpleInterface.shortValue, test.getShort());
      assertEquals(SimpleInterface.primitiveShortValue, test.getPrimitiveShort());
      assertEquals(SimpleInterface.stringValue, test.getString());
   }
   
   public void testNullPrimitives() throws Exception
   {
      SimpleInterface test = createNullCompositeDataProxy();
      assertNull(test.getBigDecimal());
      assertNull(test.getBigInteger());
      assertNull(test.getBoolean());
      try
      {
         test.isPrimitiveBoolean();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getByte());
      try
      {
         test.getPrimitiveByte();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getCharacter());
      try
      {
         test.getPrimitiveChar();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getDate());
      assertNull(test.getDouble());
      try
      {
         test.getPrimitiveDouble();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getFloat());
      try
      {
         test.getPrimitiveFloat();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getInteger());
      try
      {
         test.getPrimitiveInt();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getLong());
      try
      {
         test.getPrimitiveLong();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(test.getObjectName());
      assertNull(test.getShort());
      try
      {
         test.getPrimitiveShort();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
      assertNull(SimpleInterface.stringValue, test.getString());
   }
   
   public void testGetInvalid() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.getInvalid();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, InvalidKeyException.class, t);
      }
   }
   
   public void testNotAGetter() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.notAGetter();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
   
   public void testGetNoReturnType() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.getString();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
   
   public void testGetWithParameters() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.getString(null);
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
   
   public void testIsNotBoolean() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.isString();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
   
   public void testIsBoolean() throws Exception
   {
      InvalidInterface test = createInvalidCompositeDataProxy();
      try
      {
         test.isBoolean();
         fail("Should not be here");
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         checkThrowableDeep(IllegalArgumentException.class, t);
      }
   }
}
