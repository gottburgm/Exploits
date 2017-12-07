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
package org.jboss.test.system.metadata.test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.management.ObjectName;

import junit.framework.AssertionFailedError;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyListValueMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceElementValueMetaData;
import org.jboss.system.metadata.ServiceInjectionValueMetaData;
import org.jboss.system.metadata.ServiceJBXBValueMetaData;
import org.jboss.system.metadata.ServiceJavaBeanValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.metadata.ServiceValueFactoryParameterMetaData;
import org.jboss.system.metadata.ServiceValueFactoryValueMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.AbstractTestDelegate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A MetaDataTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class AbstractMetaDataTest extends AbstractSystemTest
{
   protected static ObjectName testBasicMBeanName = ObjectNameFactory.create("jboss.test:type=BasicMBeanName");
   protected static String testBasicMBeanCode = "BasicMBeanCode";
   protected static String testBasicMBeanInterface = "BasicMBeanInterface";
   protected static ObjectName TEST1 = ObjectNameFactory.create("test:test=1");
   protected static ObjectName TEST2 = ObjectNameFactory.create("test:test=2");
   protected static ObjectName[] NO_OBJECT_NAMES = new ObjectName[0];
   
   /**
    * Create a new ContainerTest.
    * 
    * @param name the test name
    */
   public AbstractMetaDataTest(String name)
   {
      super(name);
   }
   
   /**
    * Default setup with security manager enabled
    * 
    * @param clazz the class
    * @return the delegate
    * @throws Exception for any error
    */
   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      MetaDataTestDelegate delegate = new MetaDataTestDelegate(clazz);
      delegate.enableSecurity = true;
      return delegate;
   }
   
   /**
    * Unmarshal some xml
    * 
    * @param name the name
    * @return the list of services
    * @throws Exception for any error
    */
   protected List<ServiceMetaData> unmarshal(String name) throws Exception
   {
      URL url = findXML(name);
      return getMetaDataDelegate().unmarshal(url);
   }
   
   /**
    * Unmarshal a single mbean
    * 
    * @return the single service
    * @throws Exception for any error
    */
   protected ServiceMetaData unmarshalSingleMBean() throws Exception
   {
      String name = getName();
      name = name.substring(4) + ".xml";
      return unmarshalSingleMBean(name);
   }
   
   /**
    * Unmarshal a single mbean
    * 
    * @param name the name
    * @return the single service
    * @throws Exception for any error
    */
   protected ServiceMetaData unmarshalSingleMBean(String name) throws Exception
   {
      URL url = findXML(name);
      List<ServiceMetaData> services = getMetaDataDelegate().unmarshal(url);
      assertFalse(url + " should contain an mbean ", services.isEmpty());
      ServiceMetaData service = services.get(services.size()-1);
      assertNotNull(service);
      return service;
   }
   
   protected void assertFailUnmarshal(Class<? extends Throwable> expected) throws Exception
   {
      String name = getName();
      name = name.substring(4) + ".xml";
      assertFailUnmarshal(name, expected);
   }
   
   protected void assertFailUnmarshal(String name, Class<? extends Throwable> expected) throws Exception
   {
      try
      {
         unmarshal(name);
         fail("Should fail to unmarshal " + name);
      }
      catch (AssertionFailedError e)
      {
         throw e;
      }
      catch (Throwable error)
      {
         AbstractSystemTest.checkThrowableDeep(expected, error);
      }
   }
   
   /**
    * Find the xml
    * 
    * @param name the name
    * @return the url of the xml
    * @throws Exception for any error
    */
   protected URL findXML(String name) throws Exception
   {
      URL url = getResource(name);
      if (url == null)
         throw new IOException(name + " not found");
      return url;
   }

   protected MetaDataTestDelegate getMetaDataDelegate()
   {
      return (MetaDataTestDelegate) getDelegate();
   }
   
   protected void assertDefaultConstructor(ServiceMetaData metaData) throws Exception
   {
      ServiceConstructorMetaData constructor = metaData.getConstructor();
      assertNotNull(constructor);
      String[] signature = constructor.getSignature();
      assertNotNull(signature);
      assertEquals(0, signature.length);
      String[] params = constructor.getParams();
      assertNotNull(params);
      assertEquals(0, params.length);
   }
   
   protected void assertConstructor(String[] expectedSignature, String[] expectedParams, ServiceMetaData metaData) throws Exception
   {
      ServiceConstructorMetaData constructor = metaData.getConstructor();
      assertNotNull(constructor);
      String[] signature = constructor.getSignature();
      assertNotNull(signature);
      assertEquals(expectedSignature, signature);
      String[] params = constructor.getParams();
      assertNotNull(params);
      assertEquals(expectedParams, params);
   }
   
   protected void assertNoAttributes(ServiceMetaData metaData) throws Exception
   {
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      assertNotNull(attributes);
      assertEquals(0, attributes.size());
   }

   protected void assertAttributeName(ServiceAttributeMetaData attribute, String name) throws Exception
   {
      assertNotNull(attribute);
      String result = attribute.getName();
      assertNotNull(result);
      assertEquals("Expected attribute with name " + name + " got " + result, name, result);
   }

   protected void assertAttribute(ServiceMetaData metaData, String name) throws Exception
   {
      assertNotNull(metaData);
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      assertNotNull(attributes);
      assertEquals(1, attributes.size());
      ServiceAttributeMetaData attribute = attributes.get(0);
      assertAttributeName(attribute, name);
   }

   protected void assertAttributes(ServiceMetaData metaData, String[] names) throws Exception
   {
      assertNotNull(names);
      assertNotNull(metaData);
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      assertNotNull(attributes);
      assertEquals(names.length, attributes.size());
      ServiceAttributeMetaData attribute = attributes.get(0);
      for (String name : names)
         assertAttributeName(attribute, name);
   }
   
   protected void assertAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace, String name) throws Exception
   {
      assertNotNull(attribute);
      String result = attribute.getName();
      assertNotNull(result);
      assertEquals(name, result);
      assertEquals(trim, attribute.isTrim());
      assertEquals(replace, attribute.isReplace());
   }

   protected void assertTextAttribute(ServiceAttributeMetaData attribute) throws Exception
   {
      assertTextAttribute(attribute, true, true, "Attribute", "value");
   }

   protected void assertTextAttribute(ServiceAttributeMetaData attribute, String value) throws Exception
   {
      assertTextAttribute(attribute, true, true, "Attribute", value);
   }

   protected void assertTextAttribute(ServiceAttributeMetaData attribute, String name, String value) throws Exception
   {
      assertTextAttribute(attribute, true, true, name, value);
   }

   protected void assertTextAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace) throws Exception
   {
      assertTextAttribute(attribute, trim, replace, "Attribute", "value");
   }

   protected void assertTextAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace, String name, String value) throws Exception
   {
      assertAttribute(attribute, trim, replace, name);
      ServiceValueMetaData actual = attribute.getValue();
      assertNotNull(actual);
      assertTextValue(actual, value);
   }

   protected void assertDependsAttribute(ServiceAttributeMetaData attribute) throws Exception
   {
      assertDependsAttribute(attribute, false, false, "Attribute", TEST1);
   }

   protected void assertDependsAttribute(ServiceAttributeMetaData attribute, String name, ObjectName value) throws Exception
   {
      assertDependsAttribute(attribute, false, false, name, value);
   }

   protected void assertDependsAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace, String name, ObjectName value) throws Exception
   {
      assertAttribute(attribute, trim, replace, name);
      ServiceValueMetaData actual = attribute.getValue();
      assertNotNull(actual);
      assertDependencyValue(actual, value);
   }

   protected void assertDependsListAttribute(ServiceAttributeMetaData attribute) throws Exception
   {
      assertDependsListAttribute(attribute, false, false, "Attribute", TEST1);
   }

   protected void assertDependsListAttribute(ServiceAttributeMetaData attribute, String name, ObjectName value) throws Exception
   {
      assertDependsListAttribute(attribute, false, false, name, value);
   }

   protected void assertDependsListAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace, String name, ObjectName value) throws Exception
   {
      assertDependsListAttribute(attribute, trim, replace, name, new ObjectName[] { value });
   }

   protected void assertDependsListAttribute(ServiceAttributeMetaData attribute, boolean trim, boolean replace, String name, ObjectName[] value) throws Exception
   {
      assertAttribute(attribute, trim, replace, name);
      ServiceValueMetaData actual = attribute.getValue();
      assertNotNull(actual);
      assertDependencyListValue(actual, value);
   }

   protected void assertDependsListAttributeEmpty(ServiceAttributeMetaData attribute) throws Exception
   {
      assertDependsListAttribute(attribute, false, false, "Attribute", NO_OBJECT_NAMES);
   }
   
   protected void assertTextValue(ServiceValueMetaData actual, String expected) throws Exception
   {
      assertNotNull(expected);
      assertNotNull(actual);
      
      ServiceTextValueMetaData value = assertInstanceOf(ServiceTextValueMetaData.class, actual);
      
      assertEquals(expected, value.getText());
   }
   
   protected void assertDependencyValue(ServiceValueMetaData actual, ObjectName expected) throws Exception
   {
      assertDependencyValue(actual, expected, null);
   }
   
   protected void assertDependencyValue(ServiceValueMetaData actual, ObjectName expected, String proxyType) throws Exception
   {
      assertNotNull(expected);
      assertNotNull(actual);
      
      ServiceDependencyValueMetaData value = assertInstanceOf(ServiceDependencyValueMetaData.class, actual);
      
      assertEquals(expected, value.getObjectName());
      assertEquals(proxyType, value.getProxyType());
   }
   
   protected void assertDependencyListValueEmpty(ServiceValueMetaData actual) throws Exception
   {
      assertDependencyListValue(actual, NO_OBJECT_NAMES );
   }
   
   protected void assertDependencyListValue(ServiceValueMetaData actual, ObjectName expected) throws Exception
   {
      assertDependencyListValue(actual, new ObjectName[] { expected } );
   }
   
   protected void assertDependencyListValue(ServiceValueMetaData actual, ObjectName[] expected) throws Exception
   {
      assertNotNull(expected);
      assertNotNull(actual);
      
      ServiceDependencyListValueMetaData value = assertInstanceOf(ServiceDependencyListValueMetaData.class, actual);
      
      List<ObjectName> list = value.getObjectNames();
      assertNotNull(list);
      ObjectName[] test = list.toArray(new ObjectName[list.size()]);
      
      assertTrue("Expected " + Arrays.asList(expected) + " got " + Arrays.asList(test), Arrays.equals(expected, test));
   }
   
   protected void assertElementValue(ServiceValueMetaData actual, String expected) throws Exception
   {
      assertNotNull(expected);
      assertNotNull(actual);
      
      ServiceElementValueMetaData value = assertInstanceOf(ServiceElementValueMetaData.class, actual);
      
      Element element = value.getElement();
      assertNotNull(element);
      
      String name = element.getTagName();
      assertNotNull(name);
      
      assertEquals(expected, name);
   }
   
   protected void assertJavaBeanValue(ServiceValueMetaData actual, String expected) throws Exception
   {
      assertNotNull(actual);

      ServiceJavaBeanValueMetaData value = assertInstanceOf(ServiceJavaBeanValueMetaData.class, actual);
      
      Element element = value.getElement();
      assertChildOfAttribute(element, expected);
   }
   
   protected void assertJBXBValue(ServiceValueMetaData actual, String expected) throws Exception
   {
      assertNotNull(actual);

      ServiceJBXBValueMetaData value = assertInstanceOf(ServiceJBXBValueMetaData.class, actual);
      
      Element element = value.getElement();
      assertChildOfAttribute(element, expected);
   }
   
   protected void assertInjectValue(ServiceValueMetaData actual, Object dependency, String property) throws Exception
   {
      assertInjectValue(actual, dependency, property, ControllerState.INSTALLED);
   }
   
   protected void assertInjectValue(ServiceValueMetaData actual, Object dependency, String property, ControllerState requiredState) throws Exception
   {
      assertNotNull(actual);

      ServiceInjectionValueMetaData value = assertInstanceOf(ServiceInjectionValueMetaData.class, actual);
      
      assertEquals(dependency, value.getDependency());
      assertEquals(property, value.getProperty());
      assertEquals(requiredState, value.getDependentState());
   }
   
   protected void assertValueFactoryValue(ServiceValueMetaData actual, List<ServiceValueFactoryParameterMetaData> parameters, String defaultValue) throws Exception
   {
      assertValueFactoryValue(actual, "method", parameters, defaultValue, "bean", ControllerState.INSTALLED);
   }
   
   protected void assertValueFactoryValue(ServiceValueMetaData actual, List<ServiceValueFactoryParameterMetaData> parameters, String defaultValue, ControllerState state) throws Exception
   {
      assertValueFactoryValue(actual, "method", parameters, defaultValue, "bean", state);
   }
   
   protected void assertValueFactoryValue(ServiceValueMetaData actual, String method, List<ServiceValueFactoryParameterMetaData> parameters, String defaultValue, Object dependency, ControllerState requiredState) throws Exception
   {
      assertNotNull(actual);

      ServiceValueFactoryValueMetaData value = assertInstanceOf(ServiceValueFactoryValueMetaData.class, actual);

      assertEquals(method, value.getMethod());
      assertEquals(dependency, value.getDependency());
      assertEquals(requiredState, value.getDependentState());
      ServiceTextValueMetaData defMetadata = value.getDefaultValue();
      assertEquals(defaultValue, (defMetadata == null ? null : defMetadata.getText()));
      
      assertEquals(parameters, value.getParameterMetaData());      
   }
   
   protected void assertChildOfAttribute(Element element, String expected) throws Exception
   {
      assertNotNull(element);
      
      String name = element.getTagName();
      assertEquals("attribute", name);
      
      NodeList children = element.getChildNodes();
      assertEquals(1, children.getLength());
      Node node = children.item(0);
      element = assertInstanceOf(Element.class, node);
      name = element.getTagName();
      assertEquals(expected, name);
   }
   
   protected void assertNoDependencies(ServiceMetaData metaData) throws Exception
   {
      List<ServiceDependencyMetaData> dependencies = metaData.getDependencies();
      assertNotNull(dependencies);
      assertEquals(0, dependencies.size());
   }
   
   protected void assertDependencies(ServiceMetaData metaData, ObjectName[] expected) throws Exception
   {
      List<ServiceDependencyMetaData> dependencies = metaData.getDependencies();
      assertNotNull(dependencies);
      assertEquals(expected.length, dependencies.size());
      HashSet<ObjectName> expectedSet = new HashSet<ObjectName>();
      for (ObjectName expect : expected)
         expectedSet.add(expect);
      HashSet<ObjectName> actual = new HashSet<ObjectName>();
      for (ServiceDependencyMetaData depends : dependencies)
         actual.add(depends.getIDependOnObjectName());
      assertEquals(expectedSet, actual);
   }
   
   protected void assertNoXMBean(ServiceMetaData metaData) throws Exception
   {
      assertNull(metaData.getXMBeanDD());
      assertEquals(ServiceMetaData.XMBEAN_CODE, metaData.getXMBeanCode());
      assertNull(metaData.getXMBeanDescriptor());
   }
}
