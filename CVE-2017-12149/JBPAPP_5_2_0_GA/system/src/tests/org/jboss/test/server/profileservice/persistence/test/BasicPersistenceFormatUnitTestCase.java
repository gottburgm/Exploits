/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.server.profileservice.persistence.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.PrimitiveMetaData;
import org.jboss.test.server.profileservice.persistence.support.TestMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class BasicPersistenceFormatUnitTestCase extends AbstractPersistenceFormatTest
{

   public BasicPersistenceFormatUnitTestCase(String name)
   {
      super(name);
   }
   public void testPrimitive() throws Throwable
   {
      // Create initial model
      ManagedObject mo = createPrimitiveMO();
      assertNotNull(mo);
      
      ManagedProperty p = mo.getProperty("name");
      assertNotNull(p);
      p.setValue(SimpleValueSupport.wrap("newName"));
      
      p = mo.getProperty("integer");
      p.setValue(SimpleValueSupport.wrap(111));
      
      mo.getProperty("optionalName").setValue(SimpleValueSupport.wrap("optional"));
      
      // Assert the restored information
      PersistedManagedObject restored = restore(mo);
      assertNotNull(restored);
      
      mo = update(new PrimitiveMetaData(), restored);
      
      assertEquals("newName", getMVF().unwrap(mo.getProperty("name").getValue()));
      SimpleValue integer = (SimpleValue) mo.getProperty("integer").getValue();
      assertEquals(111, getMVF().unwrap(integer));
      assertTrue("actual " + integer , SimpleMetaType.INTEGER_PRIMITIVE.isValue(integer));
      assertEquals("optional", ((SimpleValue) mo.getProperty("optionalName").getValue()).getValue());
      
   }
   
   public void testMetaDataPrimitive() throws Throwable
   {
      ManagedObject mo = createTestMetaDataMO();
      assertNotNull(mo);
      ManagedProperty p = mo.getProperty("primitive");
      assertNotNull(p);
      
      CompositeValue nested = (CompositeValue) p.getValue();
      MetaValue name = nested.get("name");
      assertNotNull(name);
      MetaValue integer = nested.get("integer");
      assertNotNull(integer);

      // Change values
      Map<String, MetaValue> changedMap = new HashMap<String, MetaValue>();
      changedMap.put("name", new SimpleValueSupport(
            (SimpleMetaType) name.getMetaType(), "newName"));
      changedMap.put("integer", new SimpleValueSupport(
            (SimpleMetaType) integer.getMetaType(), 111));
      
      // Set new values
      p.setValue(
            new CompositeValueSupport(nested.getMetaType(), changedMap)
            );

      // Save and restore
      PersistedManagedObject restored = restore(mo);
      assertNotNull(restored);
      
      // Create empty
      mo = update(new TestMetaData(), restored);
      
      p = mo.getProperty("primitive");
      nested = (CompositeValue) p.getValue();
      name = nested.get("name");
      assertNotNull(name);
      integer = nested.get("integer");
      assertNotNull(integer);
      
      //
      assertEquals(111, ((SimpleValue) integer).getValue());
      assertEquals("newName", ((SimpleValue) name).getValue());
   }
   
   
   public void testMetaDataMap() throws Throwable
   {
      ManagedObject mo = createTestMetaDataMO();
      assertNotNull(mo);
      
      ManagedProperty p = mo.getProperty("testMap");
      assertNotNull(p);
      
      MapCompositeValueSupport composite = (MapCompositeValueSupport) p.getValue();
      assertNotNull(composite);
      
      SimpleValue v = (SimpleValue) composite.get("testString2");
      assertNotNull(v);
      
      composite.put("testString2", SimpleValueSupport.wrap("changedString"));
      
      PersistedManagedObject restored = restore(mo);
      assertNotNull(restored);
      
      mo = update(new TestMetaData(), restored);
      
      p = mo.getProperty("testMap");
      assertNotNull(p);
      
      CompositeValue changedComposite = (CompositeValue) p.getValue();
      assertNotNull(changedComposite);
      
      v = (SimpleValue) changedComposite.get("testString2");
      assertEquals("changedString", v.getValue());
   }
   
   public void testPrimitiveArray() throws Throwable
   {
      ManagedObject mo = createTestMetaDataMO();
      assertNotNull(mo);
      
      ManagedProperty p = mo.getProperty("charArray");
      assertNotNull(p);
      
      ArrayValue a = (ArrayValue) p.getValue();
      assertNotNull(a);
      
      SimpleValueSupport svs = (SimpleValueSupport) a.getValue(1);
      assertNotNull(svs);
      svs.setValue('H');
      
      PersistedManagedObject restored = restore(mo);
      assertNotNull(restored);
      
      //
      mo = update(new TestMetaData(), restored);
      
      p = mo.getProperty("charArray");
      assertNotNull(p);
      
      a = (ArrayValue) p.getValue();
      assertNotNull(a);
      
      assertEquals('H', ((SimpleValue) a.getValue(1)).getValue());
   }
   
   protected ManagedObject createMO(Object o)
   {
      return getMOF().initManagedObject(o, null);
   }
   
   protected ManagedObject createPrimitiveMO()
   {
      PrimitiveMetaData md = createPrimitive();
      assertNotNull(md);
      return createMO(md);
   }
   
   protected ManagedObject createTestMetaDataMO()
   {
      return createMO(createTestMetaData());
   }
   
   protected TestMetaData createTestMetaData()
   {
      TestMetaData test = new TestMetaData();
      test.setPrimitive(createPrimitive());
      
      // Create list
      List<String> list = new ArrayList<String>();
      
      list.add("String1");
      list.add("String2");
      list.add("String3");
      
      test.setList(list);
      
      // Create map
      Map<String, String> map = new HashMap<String, String>();
      
      map.put("testString1", "string1");
      map.put("testString2", "string2");
      map.put("testString3", "string3");
      
      test.setTestMap(map);
      
      // Create primitive array
      
      Character[] array = new Character[3];
      
      array[0] = 'A';
      array[1] = 'B';
      array[2] = 'C';
      
      test.setCharArray(array);
      
      return test;
   }
   
   protected PrimitiveMetaData createPrimitive()
   {
      PrimitiveMetaData primitive = new PrimitiveMetaData();
      primitive.setInteger(12);
      primitive.setName("testName");
      return primitive;
   }

}
