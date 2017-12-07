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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.mxbean.MXBeanUtils;
import org.jboss.test.mx.mxbean.support.TestParameterizedType;

/**
 * CompositeDataMapUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataMapUnitTestCase extends CompositeDataTest
{
   private static final TestParameterizedType MAP_OF_STRINGS_TO_INTEGERS = new TestParameterizedType(Map.class, new Type[] { String.class, Integer.class });
   private static final TabularType TABLE_STRING_TO_INTEGER = MXBeanUtils.createMapType(String.class, Integer.class);
   
   public static Test suite()
   {
      return new TestSuite(CompositeDataMapUnitTestCase.class);
   }
   
   public CompositeDataMapUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testMap() throws Exception
   {
      Map<String, Integer> c = new LinkedHashMap<String, Integer>();
      c.put("one", 1);
      c.put("two", 2);
      c.put("three", 3);
      TabularDataSupport openData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      CompositeType entryType = TABLE_STRING_TO_INTEGER.getRowType();
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "one", 1 }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "two", 2 }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "three", 3 }));
      constructReconstructTest(c, MAP_OF_STRINGS_TO_INTEGERS, openData);
   }
   
   public void testMapNull() throws Exception
   {
      constructReconstructTest(null, MAP_OF_STRINGS_TO_INTEGERS);
   }
   
   public void testMapNullKey() throws Exception
   {
      Map<String, Integer> c = new LinkedHashMap<String, Integer>();
      c.put("one", 1);
      c.put(null, 2);
      c.put("three", 3);
      TabularDataSupport openData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      CompositeType entryType = TABLE_STRING_TO_INTEGER.getRowType();
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "one", 1 }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { null, 2 }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "three", 3 }));
      constructReconstructTest(c, MAP_OF_STRINGS_TO_INTEGERS, openData);
   }
   
   public void testMapNullValue() throws Exception
   {
      Map<String, Integer> c = new LinkedHashMap<String, Integer>();
      c.put("one", 1);
      c.put("two", null);
      c.put("three", 3);
      TabularDataSupport openData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      CompositeType entryType = TABLE_STRING_TO_INTEGER.getRowType();
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "one", 1 }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "two", null }));
      openData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "three", 3 }));
      constructReconstructTest(c, MAP_OF_STRINGS_TO_INTEGERS, openData);
   }
}
