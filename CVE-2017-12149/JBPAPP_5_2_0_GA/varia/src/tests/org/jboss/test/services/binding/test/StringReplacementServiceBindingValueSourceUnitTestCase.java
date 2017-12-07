/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.services.binding.test;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.net.URL;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl;
import org.w3c.dom.Element;

/**
 * Tests of {@link StringReplacementServiceBindingValueSourceImpl}.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class StringReplacementServiceBindingValueSourceUnitTestCase extends ServiceBindingTestBase
{
   private static final String INPUT = HOST_MARKER + ":" + PORT_MARKER;
   private static final String ALT_HOST_MARKER = "${alt.host}";
   private static final String ALT_PORT_MARKER = "${alt.port}";
   private static final String ALT_INPUT = ALT_HOST_MARKER + ":" + ALT_PORT_MARKER;
   private static final String OUTPUT = HOST + ":" + PORT;
   private static final String ALT_ELEMENT_INPUT = "<element host=\"" + ALT_HOST_MARKER + "\">" + ALT_PORT_MARKER + "</element>";
   
   private StringReplacementServiceBindingValueSourceImpl testee;
   private StringReplacementServiceBindingValueSourceConfig config;
   
   public StringReplacementServiceBindingValueSourceUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      testee = new StringReplacementServiceBindingValueSourceImpl();
      config = new StringReplacementServiceBindingValueSourceConfig(ALT_HOST_MARKER, ALT_PORT_MARKER);
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getStringServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.String)}.
    */
   public void testGetStringServiceBindingValue() throws Exception
   {
      assertEquals(OUTPUT, testee.getStringServiceBindingValue(binding, INPUT));
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getStringServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.String)}.
    */
   public void testGetStringServiceBindingValueNullInput() throws Exception
   {
      assertEquals(HOST, testee.getStringServiceBindingValue(binding, null));
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getStringServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.String)}.
    */
   public void testGetStringServiceBindingValueOverrideMarkers() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertEquals(OUTPUT, testee.getStringServiceBindingValue(binding, ALT_INPUT));
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getElementServiceBindingValue(org.jboss.bindings.ServiceBinding, org.w3c.dom.Element)}.
    */
   public void testGetElementServiceBindingValue() throws Exception
   {
      elementBindingTest(ELEMENT_INPUT);
   }
   
   public void testGetElementServiceBindingValueNullInput() throws Exception
   {
      try
      {
         testee.getResourceServiceBindingValue(binding, null);
         fail("Should fail passing a null input");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testGetElementServiceBindingValueOverrideMarkers() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      elementBindingTest(ALT_ELEMENT_INPUT);
   }
   
   private void elementBindingTest(String inputText) throws Exception
   {
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(inputText);
      Element input = (Element) editor.getValue();
      Element output = testee.getElementServiceBindingValue(binding, input);
      validateOutputElement(output);
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getResourceServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.String)}.
    */
   public void testGetResourceServiceBindingValue() throws Exception
   {
      resourceBindingTest("input.xml");
   }
   
   public void testGetResourceServiceBindingValueNullInput() throws Exception
   {
      try
      {
         testee.getResourceServiceBindingValue(binding, null);
         fail("Should fail passing a null input");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testGetResourceServiceBindingValueOverrideMarkers() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      resourceBindingTest("alt_input.xml");
   }
   
   private void resourceBindingTest(String inputText) throws Exception
   {
      String input = getFullyQualifiedResourceName(inputText);
      String output = testee.getResourceServiceBindingValue(binding, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);      
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getURLServiceBindingValue(org.jboss.bindings.ServiceBinding, java.net.URL)}.
    */
   public void testGetURLServiceBindingValue() throws Exception
   {
      urlBindingTest("input.xml");
   }
   
   public void testGetURLServiceBindingValueNullInput() throws Exception
   {
      try
      {
         testee.getURLServiceBindingValue(binding, null);
         fail("Should fail passing a null input");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testGetURLServiceBindingValueOverrideMarkers() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      urlBindingTest("alt_input.xml");
   }
   
   private void urlBindingTest(String inputText) throws Exception
   {
      String resource = getFullyQualifiedResourceName(inputText);
      URL input = Thread.currentThread().getContextClassLoader().getResource(resource);
      URL output = testee.getURLServiceBindingValue(binding, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);      
   }

   public void testGetServiceBindingValueNullInput() throws Exception
   {
      Object[] params = null;
      assertEquals(HOST, testee.getServiceBindingValue(binding, params));
   }
   
   public void testGetServiceBindingValueBadInput() throws Exception
   {
      try
      {
         testee.getServiceBindingValue(binding, new Object());
         fail("Should fail passing an unknown input");
      }
      catch (IllegalArgumentException good) {}
   }
   
   public void testGetServiceBindingValueStringInput() throws Exception
   {
      assertEquals(OUTPUT, testee.getServiceBindingValue(binding, INPUT));
   }
   
   public void testGetServiceBindingValueElementInput() throws Exception
   {
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(ELEMENT_INPUT);
      Element input = (Element) editor.getValue();
      Element output = (Element) testee.getServiceBindingValue(binding, input);
      validateOutputElement(output);
   }
   
   public void testGetServiceBindingValueURLInput() throws Exception
   {
      String resource = getFullyQualifiedResourceName("input.xml");
      URL input = Thread.currentThread().getContextClassLoader().getResource(resource);
      URL output = (URL) testee.getServiceBindingValue(binding, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);      
   }
   
   public void testGetServiceBindingValueOverrideMarkers() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(config);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertEquals(OUTPUT, testee.getServiceBindingValue(binding, ALT_INPUT));
   }
   
   public void testUnknownConfigObject() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new Object());
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      assertEquals(HOST, testee.getStringServiceBindingValue(binding, null));
   }

}
