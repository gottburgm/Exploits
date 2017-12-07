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
package org.jboss.test.server.profileservice.persistence.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCollectionValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedGenericValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.NestedTestMetaData;
import org.jboss.test.server.profileservice.persistence.support.PrimitiveMetaData;
import org.jboss.test.server.profileservice.persistence.support.TestMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class TestNestedPeristenceFormatUnitTestCase extends AbstractPersistenceFormatTest
{

   public TestNestedPeristenceFormatUnitTestCase(String name)
   {
      super(name);
   }

   public void test() throws Throwable
   {
      ManagedObject mo = createNestedMO();
      
      // deployment1
      ManagedObject deployment = getDeployment("testDeployment1", mo);
      assertNotNull(deployment);
      deployment.getProperty("");
      
      ManagedProperty p = deployment.getProperty("primitive");
      assertNotNull(p);
      
      CompositeValue composite = (CompositeValue) p.getValue();
      
      Map<String, MetaValue> valueMap = new HashMap<String, MetaValue>();
      valueMap.put("optionalName",
            new SimpleValueSupport(SimpleMetaType.STRING, "changed"));
      valueMap.put("integer", new SimpleValueSupport(
            SimpleMetaType.INTEGER_PRIMITIVE, 111));

      //
      p.setValue(new CompositeValueSupport(composite.getMetaType(), valueMap));

      ManagedProperty property = deployment.getProperty("name");
      assertNotNull(property);
      property.setValue(SimpleValueSupport.wrap("ChangedName"));
      
      // deployment3
      deployment = getDeployment("testDeployment3", mo);
      assertNotNull(deployment);
      
      property = deployment.getProperty("name");
      assertNotNull(property);
      property.setValue(SimpleValueSupport.wrap("ChangedName3"));

      PersistedManagedObject restored = restore(mo);
      assertNotNull(restored);
      PersistedCollectionValue collection = (PersistedCollectionValue) restored.getProperties().get(0).getValue(); 
      assertNotNull(collection);
      PersistedManagedObject o = ((PersistedGenericValue) collection.getValues().get(0)).getManagedObject();
      assertNotNull(o);
      
      enableTrace("org.jboss.system.server.profileservice.persistence");

      // Recreate
      mo = update(createNestedTestMetaData(), restored);
      
      deployment = getDeployment("ChangedName", mo); 
      assertNotNull("changed name deployment null", deployment);
      
      p = deployment.getProperty("primitive");
      assertNotNull(p);
      
      composite = (CompositeValue) p.getValue();
      assertNotNull(composite);
      
      assertEquals("changed", ((SimpleValue)composite.get("optionalName")).getValue());
      
      
      deployment = getDeployment("testDeployment2", mo);
      assertNotNull(deployment);
      
      deployment = getDeployment("ChangedName3", mo);
      assertNotNull(deployment);

      // Null
      deployment = getDeployment("testDeployment1", mo);
      assertNull(deployment);
      // Null
      deployment = getDeployment("testDeployment3", mo);
      assertNull(deployment);

      NestedTestMetaData md = (NestedTestMetaData) mo.getAttachment();
      assertNotNull(md);
      
      assertNotNull(md.getDeployments());
   }
   
   protected ManagedObject getDeployment(String name, ManagedObject mo)
   {
    
      ManagedProperty p = mo.getProperty("deployments");
      CollectionValue collection = (CollectionValue) p.getValue();
      
      assertTrue(p.getMetaType().isCollection());
      assertNotNull("null collection", collection);
      assertEquals(3, collection.getSize());;
      
      for(MetaValue value : collection)
      {         
         ManagedObject deployment = (ManagedObject) ((GenericValue) value).getValue();
         
         ManagedProperty nameProp = deployment.getProperty("name");
         assertNotNull(nameProp);
         
         String deploymentName = (String) ((SimpleValue) deployment.getProperty("name").getValue()).getValue();
         
         if(name.equals(deploymentName))
            return deployment; 
      }
      return null;
   }
   
   protected ManagedObject createNestedMO()
   {
      return createMO(createNestedTestMetaData());
   }
   
   protected ManagedObject createMO(Object o)
   {
      return getMOF().initManagedObject(o, null);
   }
   
   protected NestedTestMetaData createNestedTestMetaData()
   {
      NestedTestMetaData test = new NestedTestMetaData();
      
      List<TestMetaData> deployments = new ArrayList<TestMetaData>();
      
      deployments.add(createTestMetaData("testDeployment1"));
      deployments.add(createTestMetaData("testDeployment2"));
      deployments.add(createTestMetaData("testDeployment3"));
      
      test.setDeployments(deployments);
      return test;
   }
   
   protected TestMetaData createTestMetaData(String name)
   {
      TestMetaData test = new TestMetaData();
      
      //
      test.setName(name);
      
      // primitive
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
      
      // Create 2D array
      char[][] test2D = {{'h', 'e'}, {'l', 'l', 'o'}};
      test.setTest2dChar(test2D);
      
      getLog().debug(test2D.length);
      
      // Create table
      Map<Integer, String> table = new HashMap<Integer, String>();
      table.put(1, "one");
      table.put(2, "two");
      table.put(3, "three");
      
      test.setTable(table);
      
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

