/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.profileservice.test;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedCommon;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.ServiceBindingSet;
import org.jboss.services.binding.managed.ServiceBindingMetadataMapper;

/**
 * <p>
 * Profile service ServiceBindingManager tests.
 * </p>
 * 
 * @see org.jboss.test.profileservice.override.test.ServiceBindingManagedObjectsTestCase
 * for tests of updating the metadata
 * 
 * @author Brian Stansberry
 * @version $Revision: 91404 $
 */
public class ServiceBindingManagedObjectsTestCase extends AbstractProfileServiceTest
{
   public static final CompositeMetaType SERVICE_BINDING_METADATA_TYPE;
   public static final CompositeMetaType SERVICE_BINDING_SET_TYPE;
   
   static
   {
      String[] itemNames = {
            "bindingSetName",
            "serviceName",
            "bindingName",
            "fullyQualifiedName",
            "description",
            "hostName",
            "port",
            "fixedHostName",
            "fixedPort"//,
//            "serviceBindingValueSourceClassName",
//            "serviceBindingValueSourceConfig"
      };
      String[] itemDescriptions = {
            "binding set to which this binding applies, or null for all sets",
            "the name of the service to which this binding applies",
            "a qualifier identifying which particular binding within the service this is",
            "the fully qualified binding name",
            "description of the binding",
            "the host name or string notation IP address to use for the binding",
            "the port to use for the binding",
            "whether the host name should remain fixed in all binding sets",
            "whether the port should remain fixed in all binding sets"//,
//            "fully qualified classname of specialized object used to process binding results",
//            ""
      };
      MetaType[] itemTypes = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.INTEGER_PRIMITIVE,
            SimpleMetaType.BOOLEAN_PRIMITIVE,
            SimpleMetaType.BOOLEAN_PRIMITIVE//,
//            SimpleMetaType.STRING,
//            new GenericMetaType(ManagedObject)
      };
      SERVICE_BINDING_METADATA_TYPE = new ImmutableCompositeMetaType(ServiceBindingMetadata.class.getName(), 
            "Service Binding Metadata",
            itemNames, itemDescriptions, itemTypes);
      

      String[] itemNames2 = {
            "name",
            "defaultHostName",
            "portOffset",
            "overrideBindings"
      };
      String[] itemDescriptions2 = {
            "the name of the binding set",
            "the host name that should be used for all bindings whose configuration " +
               "does not specify fixedHostName=\"true\"",
            "value to add to the port configuration for a standard binding to " +
               "derive the port to use in this binding set",
            "binding configurations that apply only to this binding set, either " +
               "non-standard bindings or ones that override standard binding configurations",
      };
      MetaType[] itemTypes2 = {
            SimpleMetaType.STRING,
            SimpleMetaType.STRING,
            SimpleMetaType.INTEGER_PRIMITIVE,
            ServiceBindingMetadataMapper.TYPE
      };
      SERVICE_BINDING_SET_TYPE = new ImmutableCompositeMetaType(ServiceBindingSet.class.getName(), 
            "Service Binding Set", itemNames2, itemDescriptions2, itemTypes2);
   }
   
   /**
    * <p>
    * Creates an instance of {@code SecurityManagedObjectsTestCase} with the specified name.
    * </p>
    * 
    * @param name a {@code String} representing the name of this {@code TestCase}.
    */
   public ServiceBindingManagedObjectsTestCase(String name)
   {
      super(name);
   }

   /**
    * Validates at the {@code ServiceBindingManager} managed component.
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testValidateSBMContent() throws Exception
   {
      ManagedComponent component = getServiceBindingManagerManagedComponent();

      logHierarchy(component);

      // verify that the component has the expected properties.
      Map<String, ManagedProperty> properties = component.getProperties();
      assertNotNull(properties);
      
      getLog().debug("ServiceBindingManager properties: ");
      for (Map.Entry<String, ManagedProperty> entry : properties.entrySet())
      {
         getLog().debug(entry.getKey() + " = " + entry.getValue());
      }
      
      ManagedProperty prop = properties.get("activeBindingSetName");
      assertNotNull("Missing property activeBindingSetName", prop);
      MetaValue val = prop.getValue();
      assertNotNull("property activeBindingSetName has no value", val);
      assertTrue("property activeBindingSetName value is SimpleValue", val instanceof SimpleValue);
      assertEquals("incorrect activeBindingSetName value", "ports-default", ((SimpleValue) val).getValue());
      
      prop = properties.get("standardBindings");
      assertNotNull("Missing property standardBindings", prop);
      val =  prop.getValue();
      assertNotNull("property standardBindings has no value", val);
      assertTrue("property standardBindings value is CollectionValue", val instanceof CollectionValue);
      MetaValue[] elements = ((CollectionValue) val).getElements();
      assertNotNull(elements);
      for (MetaValue mv : elements)
      {
         getLog().info(mv);
         serviceBindingMetadataTest(mv);
      }
      
      prop = properties.get("bindingSets");
      assertNotNull("Missing property bindingSets", prop);
      val =  prop.getValue();
      assertNotNull("property bindingSets has no value", val);
      assertTrue("property bindingSets value is CollectionValue", val instanceof CollectionValue);
      elements = ((CollectionValue) val).getElements();
      assertNotNull("property bindingSets value has elements", elements);
      Map<String, Integer> offsets = new HashMap<String, Integer>();
      for (MetaValue mv : elements)
      {
         getLog().info(mv);
         serviceBindingSetTest(mv, offsets);
      }
      assertEquals(Integer.valueOf(0), offsets.get("ports-default"));
      assertEquals(Integer.valueOf(100), offsets.get("ports-01"));
      assertEquals(Integer.valueOf(200), offsets.get("ports-02"));
      assertEquals(Integer.valueOf(300), offsets.get("ports-03")); 
      
      prop = properties.get("serviceBindings");
      assertNotNull("Missing property serviceBindings", prop);
      log.info("serviceBindings: " + prop);
      val = prop.getValue();
      assertNotNull("property serviceBindings has no value", val);
      assertTrue("property serviceBindings value is CompositeValue", val instanceof CompositeValue);
      CompositeValue compVal = (CompositeValue) val;
      assertEquals("correct number of sets of actual bindings", offsets.size(), compVal.values().size());
      for (String bindingSetName : offsets.keySet())
      {
         MetaValue mv = compVal.get(bindingSetName);
         assertTrue(mv instanceof CollectionValue);
         CollectionValue colVal = (CollectionValue) mv;
         getLog().info(colVal.getElements());
         for (MetaValue element : colVal.getElements())
         {
            serviceBindingTest(element);
         }
      }
      
      boolean hasState = properties.get("state") != null;
      assertEquals("Unexpected number of properties", hasState ? 5 : 4, properties.size());
   }

   private ManagedComponent getServiceBindingManagerManagedComponent() throws Exception
   {
      ManagementView managementView = getManagementView();
      ComponentType type = new ComponentType("MCBean", "ServiceBindingManager");
      ManagedComponent component = managementView.getComponent("ServiceBindingManager", type);
      assertNotNull(component);
      return component;
   }

   private void serviceBindingTest(MetaValue element)
   {
      getLog().info(element);
      
      assertTrue(element instanceof CompositeValue);
      
      CompositeValue compValue = (CompositeValue) element;
      
      MetaValue metaval = compValue.get("serviceName");
      assertNotNull("has serviceName", metaval);
      assertTrue(metaval instanceof SimpleValue);
      assertTrue(((SimpleValue) metaval).getValue() instanceof String);
      
      metaval = compValue.get("bindingName");
      if (metaval != null)
      {
         assertTrue(metaval instanceof SimpleValue);
         Object val = ((SimpleValue) metaval).getValue();
         assertTrue(val instanceof String);
      }
      
      metaval = compValue.get("fullyQualifiedName");
      assertNotNull("has fullyQualifiedName", metaval);
      assertTrue(metaval instanceof SimpleValue);
      assertTrue(((SimpleValue) metaval).getValue() instanceof String);
      
      metaval = compValue.get("description");
      if (metaval != null)
      {
         assertTrue(metaval instanceof SimpleValue);
         Object val = ((SimpleValue) metaval).getValue();
         assertTrue(val instanceof String);
      }
      
      metaval = compValue.get("hostName");
      assertNotNull("has hostName", metaval);
      assertTrue(metaval instanceof SimpleValue);
      assertTrue(((SimpleValue) metaval).getValue() instanceof String);
      
      metaval = compValue.get("bindAddress");
      assertNotNull("has bindAddress", metaval);
      assertTrue(metaval instanceof ArrayValue);
      Object val = ((ArrayValue) metaval).getValue();
      assertTrue(val instanceof byte[]);
      
      metaval = compValue.get("port");
      assertNotNull("has port", metaval);
      assertTrue(metaval instanceof SimpleValue);
      assertEquals("type of port value isn't int", int.class.getName(), metaval.getMetaType().getClassName());
   }
   
   private void serviceBindingSetTest(MetaValue metaValue, Map<String, Integer> offsets) throws Exception
   {
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue bindingSet = (CompositeValue) metaValue;
      
      MetaValue val =  bindingSet.get("name");
      assertNotNull("property name has no value", val);
      assertTrue("property name value is SimpleValue", val instanceof SimpleValue);
      Object simpleVal = ((SimpleValue) val).getValue();
      assertTrue(simpleVal instanceof String);
      String name = (String) simpleVal;
      
      val =  bindingSet.get("defaultHostName");
      assertNotNull(name + " -- property defaultHostName has no value", val);
      assertEquals(name + " -- type of defaultHostName value isn't String", String.class.getName(), val.getMetaType().getClassName());
      assertTrue(name + " -- property defaultHostName value is SimpleValue", val instanceof SimpleValue);
      // Only do further validation of the binding sets that the AS normally ships; 
      // ignore any earlier ones that other tests may have added since we don't
      // know the correct assertions
      if ("ports-default".equals(name) || "ports-01".equals(name) || "ports-02".equals(name) || "ports-03".equals(name))
      { 
         assertEquals(name + " -- correct defaultHostName value", InetAddress.getByName(getServerHost()), InetAddress.getByName((String) ((SimpleValue) val).getValue()));
      }
         
      val =  bindingSet.get("portOffset");
      assertNotNull(name + " -- property portOffset has no value", val);
      assertTrue(name + " -- property portOffset value is SimpleValue", val instanceof SimpleValue);
      simpleVal = ((SimpleValue) val).getValue();
      assertTrue(simpleVal instanceof Integer);
      assertTrue(((Integer) simpleVal).intValue() > -1);
      offsets.put(name, (Integer) simpleVal);
      
      val =  bindingSet.get("overrideBindings");
      assertNotNull(name + " -- property overrideBindings has no value", val);
      assertTrue(name + " -- property overrideBindings value is CollectionValue", val instanceof CollectionValue);
      MetaValue[] elements = ((CollectionValue) val).getElements();
      getLog().info(elements);
      for (MetaValue element : elements)
      {
         serviceBindingMetadataTest(element);
      } 
      
   }
   
   private Checked serviceBindingMetadataTest(MetaValue metaValue)
   {
      assertTrue(metaValue instanceof CompositeValue);
      CompositeValue bindingMetadata = (CompositeValue) metaValue;
      
      Checked result = new Checked();
      
      MetaValue val =  bindingMetadata.get("fullyQualifiedName");
      assertNotNull("property fullyQualifiedName has no value", val);
      assertTrue("property fullyQualifiedName value is SimpleValue", val instanceof SimpleValue);
      assertNotNull("property fullyQualifiedName value is not null", ((SimpleValue) val).getValue());
      
      val =  bindingMetadata.get("serviceName");
      assertNotNull("property serviceName has no value", val);
      assertTrue("property serviceName value is SimpleValue", val instanceof SimpleValue);
      assertNotNull("property serviceName value is not null", ((SimpleValue) val).getValue());
      
      val =  bindingMetadata.get("bindingName");
      if (val != null)
      {
         result.bindingName = true;
         assertNotNull("property bindingName has no value", val);
         assertTrue("property bindingName value is SimpleValue", val instanceof SimpleValue);
         assertEquals("type of bindingName value isn't String", String.class.getName(), val.getMetaType().getClassName());
      }
      
      val =  bindingMetadata.get("bindingSetName");
      if (val != null)
      {
         assertNotNull("property bindingSetName has no value", val);
         assertTrue("property bindingSetName value is SimpleValue", val instanceof SimpleValue);
         assertEquals("type of bindingSetName value isn't String", String.class.getName(), val.getMetaType().getClassName());
      }
      
      val =  bindingMetadata.get("hostName");
      if (val != null)
      {
         result.hostname = true;
         assertTrue("property hostName value is SimpleValue", val instanceof SimpleValue);
         assertEquals("type of hostName value isn't String", String.class.getName(), val.getMetaType().getClassName());
      }
      
      val =  bindingMetadata.get("port");
      assertNotNull("property port has no value", val);
      assertTrue("property port value is SimpleValue", val instanceof SimpleValue);
      assertNotNull("property port value is not null", ((SimpleValue) val).getValue());
      assertEquals("type of port value isn't int", int.class.getName(), val.getMetaType().getClassName());
      assertNotNull("property port value is not null", ((SimpleValue) val).getValue());
      
      val =  bindingMetadata.get("description");
      if (val != null)
      {
         result.hostname = true;
         assertNotNull("property hostName has no value", val);
         assertTrue("property description value is SimpleValue", val instanceof SimpleValue);
         assertEquals("type of description value isn't String", String.class.getName(), val.getMetaType().getClassName());
      }
      
      val =  bindingMetadata.get("fixedPort");
      assertNotNull("property bindingName has no value", val);
      assertTrue("property bindingName value is SimpleValue", val instanceof SimpleValue);
      assertNotNull("property bindingName value is not null", ((SimpleValue) val).getValue());
      assertEquals("type of bindingName value isn't boolean", boolean.class.getName(), val.getMetaType().getClassName());
      assertNotNull("property bindingName value is not null", ((SimpleValue) val).getValue());
      
      val =  bindingMetadata.get("fixedHostName");
      assertNotNull("property bindingName has no value", val);
      assertTrue("property bindingName value is SimpleValue", val instanceof SimpleValue);
      assertNotNull("property bindingName value is not null", ((SimpleValue) val).getValue());
      assertEquals("type of bindingName value isn't boolean", boolean.class.getName(), val.getMetaType().getClassName());
      assertNotNull("property bindingName value is not null", ((SimpleValue) val).getValue());
      
      return result;
   }
   
//   // FIXME: disabled as we don't want to change to name of a server that
//   // the testsuite later restarts. Move this somewhere else where we can do it   
//   public void testUpdateActiveBindingSetName() throws Exception
//   {           
//      ManagedComponent component = getServiceBindingManagerManagedComponent();
//   
//      ManagedProperty property = component.getProperty("activeBindingSetName");
//      assertNotNull(property);
//      
//      property.setValue(SimpleValueSupport.wrap("ports-01"));
//      
//      getManagementView().updateComponent(component);
//      log.debug("updated component " + component);
//      
//      component = getServiceBindingManagerManagedComponent();
//      assertNotNull(component);
//      log.debug("re-acquired component " + component);
//   
//      property = component.getProperty("activeBindingSetName");
//      assertNotNull(property);
//      
//      SimpleValue val = (SimpleValue) property.getValue();
//      assertEquals("ports-01", val.getValue());
//   }


   private IndexedArray checkAddedBindingSet(Map<String, ManagedProperty> properties, int numSets, int portOffset, int overrideBindingPort,
         Map<String, MetaValue> standardBindings)
   {
      IndexedArray result = new IndexedArray();
      
      // First confirm the expected binding set is there
      
      ManagedProperty prop = properties.get("bindingSets");
      assertNotNull("Missing property bindingSets", prop);
      MetaValue val =  prop.getValue();
      assertNotNull("property bindingSets has no value", val);
      assertTrue("property bindingSets value is CollectionValue", val instanceof CollectionValue);
      MetaValue[] elements = ((CollectionValue) val).getElements();
      assertNotNull(elements);
      assertEquals(numSets, elements.length);
      result.array = elements;
      boolean sawAdded = false;
      for (int i = 0; i < elements.length; i++)
      {
         MetaValue mv = elements[i];
         if ("ports-test".equals(getSimpleValue(mv, "name")))
         {
            sawAdded = true;
            assertEquals("added binding set has correct defaultHostName", 
                  "localhost", getSimpleValue(mv, "defaultHostName"));
            assertEquals("added binding set has correct portOffset", 
                  portOffset, getSimpleValue(mv, "portOffset", Integer.class).intValue());
            MetaValue overVal = ((CompositeValue) mv).get("overrideBindings");
            assertTrue(overVal instanceof CollectionValue);
            MetaValue[] overrides = ((CollectionValue) overVal).getElements();
            assertNotNull("added binding set has overrides", overrides);
            assertEquals("added binding set has correct # of overrides", 1, overrides.length);
            mv = overrides[0];
            assertEquals("added binding set has AddedOverrideBinding", 
                  "AddedOverrideBinding", getSimpleValue(mv, "serviceName"));
            assertNull("AddedOverrideBinding has null bindingName", 
                       getSimpleValue(mv, "bindingName"));
            assertEquals("AddedOverrideBinding has correct description", 
                       "description", getSimpleValue(mv, "description"));
            assertNull("AddedOverrideBinding has null hostName", 
                  getSimpleValue(mv, "hostName"));
            assertEquals("AddedOverrideBinding has correct port", 
                  overrideBindingPort, getSimpleValue(mv, "port", Integer.class).intValue());
            assertFalse("AddedOverrideBinding has correct fixedHostName", 
                  getSimpleValue(mv, "fixedHostName", Boolean.class).booleanValue());
            assertFalse("AddedOverrideBinding has correct fixedPort", 
                  getSimpleValue(mv, "fixedPort", Boolean.class).booleanValue());
            result.index = i;
            break;
         }
      }
      assertTrue(sawAdded);
      
      // Next validate the expected actual bindings are there
      prop = properties.get("serviceBindings");
      assertNotNull("Missing property serviceBindings", prop);
      val =  prop.getValue();
      assertNotNull("property serviceBindings has no value", val);
      assertTrue("property serviceBindings value is Composite", val instanceof CompositeValue);
      val = ((CompositeValue) val).get("ports-test");
      assertNotNull(val);
      assertTrue("property serviceBindings value is CollectionValue", val instanceof CollectionValue);
      elements = ((CollectionValue) val).getElements();
      assertNotNull("property serviceBindings value has elements", elements);
      assertEquals("property serviceBindings value has correct # of elements", 
            standardBindings.size() + 1, elements.length);
      for (MetaValue mv : elements)
      {
         String fqn = getSimpleValue(mv, "fullyQualifiedName", String.class);
         if ("AddedOverrideBinding".equals(fqn))
         {
            assertEquals("actual AddedOverrideBinding has correct serviceName", 
                  "AddedOverrideBinding", getSimpleValue(mv, "serviceName"));
            assertNull("actual AddedOverrideBinding has null bindingName", 
                  getSimpleValue(mv, "bindingName"));
            assertEquals("actual AddedOverrideBinding has correct description", 
                  "description", getSimpleValue(mv, "description"));
            assertEquals("actual AddedOverrideBinding has correct port", 
                  overrideBindingPort + portOffset, getSimpleValue(mv, "port", Integer.class).intValue());
            assertEquals("actual AddedOverrideBinding has correct hostName", 
                  "localhost", getSimpleValue(mv, "hostName"));
         }
         else
         {
            MetaValue standard = standardBindings.get(fqn);
            assertNotNull(standard);
            assertEquals("standardBinding " + fqn + " has correct serviceName", 
                  getSimpleValue(standard, "serviceName"), getSimpleValue(mv, "serviceName"));
            assertEquals("standardBinding " + fqn + " has correct bindingName", 
                  getSimpleValue(standard, "bindingName"), getSimpleValue(mv, "bindingName"));
            assertEquals("standardBinding " + fqn + " has correct description", 
                  getSimpleValue(standard, "description"), getSimpleValue(mv, "description"));
            int offset = getSimpleValue(standard, "fixedPort", Boolean.class).booleanValue() ? 0 : portOffset;
            assertEquals("standardBinding " + fqn + " has correct port", 
                  getSimpleValue(standard, "port", Integer.class).intValue() + offset, 
                         getSimpleValue(mv, "port", Integer.class).intValue());
            String host = getSimpleValue(standard, "fixedHostName", Boolean.class).booleanValue() 
                                  ? getSimpleValue(standard, "hostName", String.class) 
                                  : "localhost";
            assertEquals("standardBinding " + fqn + " has correct hostName", 
                  host, getSimpleValue(mv, "hostName"));
         }
      }
      
      return result;
   }

   private void logHierarchy(ManagedComponent mc)
   {
      ManagedCommon child = mc;
      ManagedCommon mcom = mc.getParent();
      while (mcom != null)
      {
         log.debug("parent of " + child.getName() + " is " + mcom.getName());
         child = mcom;
         mcom = mcom.getParent();
      }
   }
   
   private Object getSimpleValue(MetaValue val, String key)
   {
      return getSimpleValue(val, key, Object.class);
   }
   
   private <T> T getSimpleValue(MetaValue val, String key, Class<T> type)
   {
      T result = null;
      assertTrue(val instanceof CompositeValue);
      CompositeValue cval = (CompositeValue) val;
      MetaValue mv = cval.get(key);
      if (mv != null)
      {
         assertTrue(mv instanceof SimpleValue);
         Object obj = ((SimpleValue) mv).getValue();
         result = type.cast(obj);
      }
      return result;
   }
   
   private static MapCompositeValueSupport cloneCompositeValue(CompositeValue toClone)
   {
      if (toClone instanceof MapCompositeValueSupport)
      {
         return (MapCompositeValueSupport) toClone.clone();
      }
      else
      {
         CompositeMetaType type = toClone.getMetaType();
         Map<String, MetaValue> map = new HashMap<String, MetaValue>();
         for (String key : type.keySet())
         {
            map.put(key, toClone.get(key));
         }
         return new MapCompositeValueSupport(map, type);
      }
   }
   
   private class Checked
   {
      private boolean hostname;
      private boolean bindingName;
   }
   
   private class IndexedArray
   {
      int index;
      MetaValue[] array;
   }
}
