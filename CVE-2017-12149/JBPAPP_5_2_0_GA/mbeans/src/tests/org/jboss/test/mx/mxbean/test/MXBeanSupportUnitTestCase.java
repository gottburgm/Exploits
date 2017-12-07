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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import junit.framework.Test;
import junit.framework.TestSuite;

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
 * MXBeanSupportUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanSupportUnitTestCase extends AbstractMXBeanTest
{
   private static final TabularType TABLE_STRING_TO_INTEGER = MXBeanUtils.createMapType(String.class, Integer.class);

   public static Test suite()
   {
      return new TestSuite(MXBeanSupportUnitTestCase.class);
   }
   
   public MXBeanSupportUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testGetSimpleAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      OpenType[] types = new OpenType[SimpleInterface.TYPES.length];
      for (int i = 0; i < types.length; ++i)
         types[i] = MXBeanUtils.getOpenType(SimpleInterface.TYPES[i]);
      checkAttributes(server, objectName, SimpleMXBeanSupportMXBean.class, support, SimpleInterface.KEYS, types, SimpleInterface.VALUES, SimpleInterface.VALUES, info);
   }
   
   public void testSetSimpleAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      String[] keys = SimpleInterface.KEYS;
      Object[] values = SimpleInterface.VALUES;
      Object[] changedValues = SimpleInterface.CHANGED_VALUES;
      AttributeList list = new AttributeList(keys.length);
      OpenType[] types = new OpenType[SimpleInterface.TYPES.length];
      for (int i = 0; i < types.length; ++i)
      {
         types[i] = MXBeanUtils.getOpenType(SimpleInterface.TYPES[i]);
         String name = getUpperName(keys[i]);
         server.setAttribute(objectName, new Attribute(name, changedValues[i]));
         Attribute attribute = new Attribute(name, values[i]);
         list.add(attribute);
      }
      checkAttributes(server, objectName, SimpleMXBeanSupportMXBean.class, support, keys, types, SimpleInterface.CHANGED_VALUES, SimpleInterface.CHANGED_VALUES, info);
      
      setAttributes(server, objectName, list);
      checkAttributes(server, objectName, SimpleMXBeanSupportMXBean.class, support, keys, types, SimpleInterface.VALUES, SimpleInterface.VALUES, info);
   }

   public void testSimpleInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleMXBeanSupport support = new SimpleMXBeanSupport();
      ObjectName objectName = SimpleMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      assertEquals("olleH", server.invoke(objectName, "echoReverse", new Object[] { "Hello" }, new String[] { String.class.getName() }));
   }

   public void testGetCompositeAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      CompositeData data = createCompositeData(SimpleInterface.class.getName(), SimpleInterface.KEYS, SimpleInterface.VALUES);
      Object[] values = { "Simple", data };
      Object[] realValues = { "Simple", composite };
      checkAttributes(server, objectName, CompositeMXBeanSupportMXBean.class, support, CompositeInterface.KEYS, CompositeInterface.TYPES, values, realValues, info);
   }
   
   public void testSetCompositeAttributes() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      String[] keys = CompositeInterface.KEYS;
      CompositeData data = createCompositeData(SimpleInterface.class.getName(), SimpleInterface.KEYS, SimpleInterface.VALUES);
      Object[] values = { "Simple", data };
      data = createCompositeData(SimpleInterface.class.getName(), SimpleInterface.KEYS, SimpleInterface.CHANGED_VALUES);
      Object[] changedValues = { "Changed", data };
      AttributeList list = new AttributeList(keys.length);
      for (int i = 0; i < keys.length; ++i)
      {
         String name = getUpperName(keys[i]);
         server.setAttribute(objectName, new Attribute(name, changedValues[i]));
         Attribute attribute = new Attribute(name, values[i]);
         list.add(attribute);
      }
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
      Object[] realChangedValues = { "Changed", changed };
      checkAttributes(server, objectName, CompositeMXBeanSupportMXBean.class, support, CompositeInterface.KEYS, CompositeInterface.TYPES, changedValues, realChangedValues, info);
      
      setAttributes(server, objectName, list);
      Object[] realValues = { "Simple", composite };
      checkAttributes(server, objectName, CompositeMXBeanSupportMXBean.class, support, keys, CompositeInterface.TYPES, values, realValues, info);
   }

   public void testCompositeInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      SimpleObject composite = new SimpleObject();
      CompositeMXBeanSupport support = new CompositeMXBeanSupport("Simple", composite);
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      composite = new SimpleObject();
      composite.setString("hello");
      CompositeData data = (CompositeData) MXBeanUtils.construct(MXBeanUtils.getOpenType(SimpleInterface.class), composite, getName());
      Object actual = server.invoke(objectName, "echoReverse", new Object[] { data }, new String[] { SimpleInterface.class.getName() });
      composite = new SimpleObject();
      composite.setString("olleh");
      CompositeData expected = (CompositeData) MXBeanUtils.construct(MXBeanUtils.getOpenType(SimpleInterface.class), composite, getName());
      checkValueEquals(expected, actual);
   }
   
   public void testGetCollectionAttributes() throws Exception
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
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);
      String[] collectionArray = collection.toArray(new String[collection.size()]);
      String[] setArray = set.toArray(new String[set.size()]);
      String[] listArray = list.toArray(new String[list.size()]);
      TabularDataSupport mapData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      CompositeType entryType = TABLE_STRING_TO_INTEGER.getRowType();
      mapData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "map", 1 }));

      Object[] values = { array, collectionArray, setArray, listArray, mapData, TestEnum.FIRST.name() };
      Object[] realValues = { array, collection, set, list, map, enumeration };
      checkAttributes(server, objectName, CollectionsMXBeanSupportMXBean.class, support, CollectionsInterface.KEYS, CollectionsInterface.TYPES, values, realValues, info);
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
      OpenMBeanInfo info = (OpenMBeanInfo) server.getMBeanInfo(objectName);

      String[] keys = CollectionsInterface.KEYS;
      String[] collectionArray = collection.toArray(new String[collection.size()]);
      String[] setArray = set.toArray(new String[set.size()]);
      String[] listArray = list.toArray(new String[list.size()]);
      TabularDataSupport mapData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      CompositeType entryType = TABLE_STRING_TO_INTEGER.getRowType();
      mapData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "map", 1 }));
      Object[] values = { array, collectionArray, setArray, listArray, mapData, TestEnum.FIRST.name() };

      String[] changedArray = { "arrayChanged" };
      Collection<String> changedCollection = new ArrayList<String>();
      changedCollection.add("collectionChanged");
      Set<String> changedSet = new LinkedHashSet<String>();
      changedSet.add("setChanged");
      List<String> changedList = new ArrayList<String>();
      changedList.add("listChanged");

      String[] changedCollectionArray = changedCollection.toArray(new String[changedCollection.size()]);
      String[] changedSetArray = changedSet.toArray(new String[changedSet.size()]);
      String[] changedListArray = changedList.toArray(new String[changedList.size()]);
      TabularDataSupport changedMapData = new TabularDataSupport(TABLE_STRING_TO_INTEGER);
      changedMapData.put(new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { "mapChanged", 2 }));
      Object[] changedValues = { changedArray, changedCollectionArray, changedSetArray, changedListArray, changedMapData, TestEnum.SECOND.name() };

      AttributeList attributeList = new AttributeList(keys.length);
      for (int i = 0; i < keys.length; ++i)
      {
         String name = getUpperName(keys[i]);
         server.setAttribute(objectName, new Attribute(name, changedValues[i]));
         Attribute attribute = new Attribute(name, values[i]);
         attributeList.add(attribute);
      }
      Map<String, Integer> changedMap = new LinkedHashMap<String, Integer>();
      changedMap.put("mapChanged", 2);
      Object[] realChangedValues = { changedArray, changedCollection, changedSet, changedList, changedMap, TestEnum.SECOND };
      checkAttributes(server, objectName, CollectionsMXBeanSupportMXBean.class, support, keys, CollectionsInterface.TYPES, changedValues, realChangedValues, info);
      
      setAttributes(server, objectName, attributeList);
      Object[] realValues = { array, collection, set, list, map, enumeration };
      checkAttributes(server, objectName, CollectionsMXBeanSupportMXBean.class, support, keys, CollectionsInterface.TYPES, values, realValues, info);
   }

   public void testCollectionInvoke() throws Exception
   {
      MBeanServer server = createMBeanServer();
      CollectionsMXBeanSupport support = new CollectionsMXBeanSupport();
      ObjectName objectName = CompositeMXBeanSupportMXBean.REGISTERED_OBJECT_NAME;
      server.registerMBean(support, objectName);
      String[] list = { "one", "two", "three" };
      String[] expected = { "three", "two", "one" };
      Object result = server.invoke(objectName, "echoReverse", new Object[] { list }, new String[] { List.class.getName() });
      checkArrayEquals(expected, result);
   }

   private void checkAttributes(MBeanServer server, ObjectName objectName, Class intf, Object mxbean, String[] keys, OpenType[] types, Object[] values, Object[] realValues, OpenMBeanInfo info) throws Exception
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
      
      String[] attributeNames = new String[keys.length];
      
      for (int i = 0; i < keys.length; ++i)
      {
         String name = getUpperName(keys[i]);
         OpenMBeanAttributeInfo attribute = mapping.get(name);
         assertNotNull("Could not find key " + name + " in " + mapping.keySet(), attribute);
         Object value = server.getAttribute(objectName, name);
         checkValueEquals(values[i], value);
         Method method = MXBeanUtils.getCompositeDataMethod(intf, keys[i], types[i] == SimpleType.BOOLEAN);
         value = method.invoke(mxbean, null);
         checkValueEquals(realValues[i], value);
         assertEquals(types[i], attribute.getOpenType());
         attributeNames[i] = name;
      }
      
      AttributeList list = server.getAttributes(objectName, attributeNames);
      for (int i = 0; i < keys.length; ++i)
      {
         String name = attributeNames[i];
         OpenMBeanAttributeInfo attribute = mapping.get(name);
         Attribute attr = (Attribute) list.get(i); 
         checkValueEquals(values[i], attr.getValue());
         assertEquals(types[i], attribute.getOpenType());
      }
   }

   private void setAttributes(MBeanServer server, ObjectName objectName, AttributeList list) throws Exception
   {
      AttributeList result = server.setAttributes(objectName, list);
      for (int i = 0; i < list.size(); ++i)
      {
         Attribute attribute = (Attribute) list.get(i);
         Object expected = attribute.getValue();
         attribute = (Attribute) result.get(i);
         Object actual = attribute.getValue();
         checkValueEquals(expected, actual);
      }
   }
}
