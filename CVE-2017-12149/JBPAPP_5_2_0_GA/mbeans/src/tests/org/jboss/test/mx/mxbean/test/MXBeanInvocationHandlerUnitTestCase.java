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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.mxbean.MXBeanFactory;
import org.jboss.mx.mxbean.MXBeanUtils;
import org.jboss.test.mx.mxbean.support.CollectionsInterface;
import org.jboss.test.mx.mxbean.support.CollectionsMXBeanSupport;
import org.jboss.test.mx.mxbean.support.CollectionsMXBeanSupportMXBean;
import org.jboss.test.mx.mxbean.support.CompositeInterface;
import org.jboss.test.mx.mxbean.support.CompositeMXBeanSupport;
import org.jboss.test.mx.mxbean.support.CompositeMXBeanSupportMXBean;
import org.jboss.test.mx.mxbean.support.SimpleInterface;
import org.jboss.test.mx.mxbean.support.SimpleMXBeanSupport;
import org.jboss.test.mx.mxbean.support.SimpleMXBeanSupportMXBean;
import org.jboss.test.mx.mxbean.support.SimpleObject;
import org.jboss.test.mx.mxbean.support.TestEnum;

/**
 * MXBeanInvocationHandlerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanInvocationHandlerUnitTestCase extends AbstractMXBeanTest
{
   public static Test suite()
   {
      return new TestSuite(MXBeanInvocationHandlerUnitTestCase.class);
   }
   
   public MXBeanInvocationHandlerUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testGetSimpleAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      SimpleMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, SimpleMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      OpenType[] types = new OpenType[SimpleInterface.TYPES.length];
      for (int i = 0; i < types.length; ++i)
         types[i] = MXBeanUtils.getOpenType(SimpleInterface.TYPES[i]);
      checkAttributes(SimpleMXBeanSupportMXBean.class, proxy, support, SimpleInterface.KEYS, types, SimpleInterface.VALUES, info);
   }
   
   public void testSetSimpleAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      SimpleMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, SimpleMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      OpenType[] types = new OpenType[SimpleInterface.TYPES.length];
      for (int i = 0; i < types.length; ++i)
         types[i] = MXBeanUtils.getOpenType(SimpleInterface.TYPES[i]);
      proxy.setBigDecimal(SimpleInterface.bigDecimalChangedValue);
      proxy.setBigInteger(SimpleInterface.bigIntegerChangedValue);
      proxy.setBoolean(SimpleInterface.booleanChangedValue);
      proxy.setByte(SimpleInterface.byteChangedValue);
      proxy.setCharacter(SimpleInterface.characterChangedValue);
      proxy.setDate(SimpleInterface.dateChangedValue);
      proxy.setDouble(SimpleInterface.doubleChangedValue);
      proxy.setFloat(SimpleInterface.floatChangedValue);
      proxy.setInteger(SimpleInterface.integerChangedValue);
      proxy.setLong(SimpleInterface.longChangedValue);
      proxy.setObjectName(SimpleInterface.objectNameChangedValue);
      proxy.setPrimitiveBoolean(SimpleInterface.primitiveBooleanChangedValue);
      proxy.setPrimitiveByte(SimpleInterface.primitiveByteChangedValue);
      proxy.setPrimitiveChar(SimpleInterface.primitiveCharChangedValue);
      proxy.setPrimitiveDouble(SimpleInterface.primitiveDoubleChangedValue);
      proxy.setPrimitiveFloat(SimpleInterface.primitiveFloatChangedValue);
      proxy.setPrimitiveInt(SimpleInterface.primitiveIntChangedValue);
      proxy.setPrimitiveLong(SimpleInterface.primitiveLongChangedValue);
      proxy.setPrimitiveShort(SimpleInterface.primitiveShortChangedValue);
      proxy.setShort(SimpleInterface.shortChangedValue);
      proxy.setString(SimpleInterface.stringChangedValue);
      checkAttributes(SimpleInterface.class, proxy, support, SimpleInterface.KEYS, types, SimpleInterface.CHANGED_VALUES, info);
   }

   public void testSimpleInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      SimpleMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, SimpleMXBeanSupportMXBean.class);
      assertEquals("olleH", proxy.echoReverse("Hello"));
   }
   
   public void testGetCompositeAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CompositeMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CompositeMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      Object[] values = { "Simple", composite };
      checkAttributes(CompositeMXBeanSupportMXBean.class, proxy, support, CompositeInterface.KEYS, CompositeInterface.TYPES, values, info);
   }
   
   public void testSetCompositeAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CompositeMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CompositeMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      SimpleObject changed = new SimpleObject();
      changed.setBigDecimal(SimpleInterface.bigDecimalChangedValue);
      changed.setBigInteger(SimpleInterface.bigIntegerChangedValue);
      changed.setBoolean(SimpleInterface.booleanChangedValue);
      changed.setByte(SimpleInterface.byteChangedValue);
      changed.setCharacter(SimpleInterface.characterChangedValue);
      changed.setDate(SimpleInterface.dateChangedValue);
      changed.setDouble(SimpleInterface.doubleChangedValue);
      changed.setFloat(SimpleInterface.floatChangedValue);
      changed.setInteger(SimpleInterface.integerChangedValue);
      changed.setLong(SimpleInterface.longChangedValue);
      changed.setObjectName(SimpleInterface.objectNameChangedValue);
      changed.setPrimitiveBoolean(SimpleInterface.primitiveBooleanChangedValue);
      changed.setPrimitiveByte(SimpleInterface.primitiveByteChangedValue);
      changed.setPrimitiveChar(SimpleInterface.primitiveCharChangedValue);
      changed.setPrimitiveDouble(SimpleInterface.primitiveDoubleChangedValue);
      changed.setPrimitiveFloat(SimpleInterface.primitiveFloatChangedValue);
      changed.setPrimitiveInt(SimpleInterface.primitiveIntChangedValue);
      changed.setPrimitiveLong(SimpleInterface.primitiveLongChangedValue);
      changed.setPrimitiveShort(SimpleInterface.primitiveShortChangedValue);
      changed.setShort(SimpleInterface.shortChangedValue);
      changed.setString(SimpleInterface.stringChangedValue);
      proxy.setSimple("Changed");
      proxy.setComposite(changed);
      Object[] values = new Object[] { "Changed", changed };
      checkAttributes(CompositeMXBeanSupportMXBean.class, proxy, support, CompositeInterface.KEYS, CompositeInterface.TYPES, values, info);
   }

   public void testCompositeInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CompositeMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CompositeMXBeanSupportMXBean.class);
      composite = new SimpleObject();
      composite.setString("hello");
      SimpleInterface result = proxy.echoReverse(composite);
      assertEquals("olleh", result.getString());
   }
   
   public void testGetCollectionAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      String[] array = { "array" };
      Collection<String> collection = new ArrayList<String>();
      collection.add("collection");
      Set<String> set = new HashSet<String>();
      set.add("set");
      List<String> list = new ArrayList<String>();
      list.add("list");
      Map<String, Integer> map = new HashMap<String, Integer>();
      map.put("map", 1);
      TestEnum enumeration = TestEnum.FIRST;
      CollectionsMXBeanSupport support = new CollectionsMXBeanSupport(array, collection, set, list, map, enumeration);
      ObjectName objectName = CollectionsMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CollectionsMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CollectionsMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      Object[] values = { array, collection, set, list, map, enumeration };
      checkAttributes(CollectionsMXBeanSupportMXBean.class, proxy, support, CollectionsInterface.KEYS, CollectionsInterface.TYPES, values, info);
   }
   
   public void testSetCollectionAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      String[] array = { "array" };
      Collection<String> collection = new ArrayList<String>();
      collection.add("collection");
      Set<String> set = new LinkedHashSet<String>();
      set.add("set");
      List<String> list = new ArrayList<String>();
      list.add("list");
      Map<String, Integer> map = new LinkedHashMap<String, Integer>();
      map.put("map", 1);
      TestEnum enumeration = TestEnum.FIRST;
      CollectionsMXBeanSupport support = new CollectionsMXBeanSupport(array, collection, set, list, map, enumeration);
      ObjectName objectName = CollectionsMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CollectionsMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CollectionsMXBeanSupportMXBean.class);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);

      String[] changedArray = { "arrayChanged" };
      Collection<String> changedCollection = new ArrayList<String>();
      changedCollection.add("collectionChanged");
      Set<String> changedSet = new LinkedHashSet<String>();
      changedSet.add("setChanged");
      List<String> changedList = new ArrayList<String>();
      changedList.add("listChanged");
      Map<String, Integer> changedMap = new LinkedHashMap<String, Integer>();
      changedMap.put("mapChanged", 2);
      TestEnum changedEnumeration = TestEnum.SECOND;
      
      proxy.setArray(changedArray);
      proxy.setCollection(changedCollection);
      proxy.setEnum(changedEnumeration);
      proxy.setList(changedList);
      proxy.setMap(changedMap);
      proxy.setSet(changedSet);
      Object[] changedValues = { changedArray, changedCollection, changedSet, changedList, changedMap, changedEnumeration };
      checkAttributes(CollectionsMXBeanSupportMXBean.class, proxy, support, CollectionsInterface.KEYS, CollectionsInterface.TYPES, changedValues, info);
   }

   public void testCollectionInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      CollectionsMXBeanSupport support = new CollectionsMXBeanSupport();
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      CollectionsMXBeanSupportMXBean proxy = MXBeanFactory.makeProxy(server, objectName, CollectionsMXBeanSupportMXBean.class);
      List<String> list = new ArrayList<String>();
      list.add("one");
      list.add("two");
      list.add("three");
      List<String> expected = new ArrayList<String>();
      expected.add("three");
      expected.add("two");
      expected.add("one");
      List<String> result = proxy.echoReverse(list);
      assertEquals(expected, result);
   }

   protected void checkAttributes(Class intf, Object proxy, Object mxbean, String[] keys, OpenType[] types, Object[] values, OpenMBeanInfo info) throws Exception
   {
      MBeanAttributeInfo[] attributes = info.getAttributes();

      assertEquals(keys.length, values.length);
      assertEquals(keys.length, types.length);
      assertEquals(keys.length, attributes.length);
      
      Map<String, OpenMBeanAttributeInfo> mapping = new HashMap<String, OpenMBeanAttributeInfo>(attributes.length);
      for (int i = 0; i < attributes.length; ++i)
      {
         OpenMBeanAttributeInfo attribute = (OpenMBeanAttributeInfo) attributes[i];
         String name = attribute.getName();
         mapping.put(name, attribute);
      }
      
      for (int i = 0; i < keys.length; ++i)
      {
         String name = getUpperName(keys[i]);
         OpenMBeanAttributeInfo attribute = mapping.get(name);
         assertNotNull("Could not find key " + name + " in " + mapping.keySet(), attribute);
         Method method = MXBeanUtils.getCompositeDataMethod(intf, keys[i], types[i] == SimpleType.BOOLEAN);
         Object value = method.invoke(proxy, null);
         checkValueEquals(values[i], value);
         value = method.invoke(mxbean, null);
         checkValueEquals(values[i], value);
         assertEquals(types[i], attribute.getOpenType());
      }
   }
}
