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

import java.lang.reflect.Type;

import javax.management.openmbean.OpenType;

import junit.framework.AssertionFailedError;

import org.jboss.mx.mxbean.MXBeanUtils;

/**
 * CompositeDataTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class CompositeDataTest extends AbstractMXBeanTest
{
   public CompositeDataTest(String name)
   {
      super(name);
   }

   protected void constructReconstructTest(Object initialValue) throws Exception
   {
      constructReconstructTest(initialValue, initialValue);
   }

   protected void constructReconstructTest(Object initialValue, Type type) throws Exception
   {
      constructReconstructTest(initialValue, type, initialValue);
   }

   protected void constructReconstructTest(Object initialValue, Type type, Object expectedOpenData) throws Exception
   {
      constructReconstructTest(initialValue, type, expectedOpenData, initialValue);
   }

   protected void assertNullFailure(Type type) throws Exception
   {
      try
      {
         constructReconstructTest(null, type);
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

   protected void constructReconstructTest(Object initialValue, Object expectedOpenData) throws Exception
   {
      constructReconstructTest(initialValue, expectedOpenData, initialValue);
   }

   protected void constructReconstructTest(Object initialValue, Object expectedOpenData, Object expected) throws Exception
   {
      Type type = initialValue.getClass();
      constructReconstructTest(initialValue, type, expectedOpenData, expected);
   }

   protected void constructReconstructTest(Object initialValue, Type type, Object expectedOpenData, Object expected) throws Exception
   {
      OpenType openType = MXBeanUtils.getOpenType(type);
      constructReconstructTest(initialValue, type, openType, expectedOpenData, expected);
   }

   protected void constructReconstructTest(Object initialValue, Type type, OpenType openType, Object expectedOpenData, Object expected) throws Exception
   {
      Object openData = construct(openType, type, initialValue);
      checkOpenDataEquals(expectedOpenData, openData);
      if (initialValue != null)
         assertTrue(openData + " is not " + openType, openType.isValue(openData));
      
      Object finalValue = reconstruct(openType, type, openData);
      checkFinalEquals(expected, finalValue);
   }
   
   protected void checkOpenDataEquals(Object expected, Object actual)
   {
      checkEquals(expected, actual);
   }
   
   protected void checkFinalEquals(Object expected, Object actual)
   {
      checkEquals(expected, actual);
   }
   
   protected void checkEquals(Object expected, Object actual)
   {
      assertEquals(expected, actual);
   }

   protected Object construct(OpenType openType, Type type, Object value) throws Exception
   {
      return MXBeanUtils.construct(openType, value, getClass().getName() + "#" + getName());
   }
   
   protected Object reconstruct(OpenType openType, Type type, Object openData) throws Exception
   {
      return MXBeanUtils.reconstruct(openType, type, openData, getClass().getName() + "#" + getName());
   }
}
