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

package org.jboss.test.services.binding.test;

import java.net.URL;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceImpl;
import org.w3c.dom.Element;

/**
 * Tests of {@link XSLTServiceBindingValueSourceImpl}.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class XSLTServiceBindingValueSourceUnitTestCase extends ServiceBindingTestBase
{  
   private XSLTServiceBindingValueSourceImpl testee;
   private XSLTServiceBindingValueSourceConfig xsltConfig;
   
   /**
    * Create a new XSLTServiceBindingValueSourceUnitTestCase.
    * 
    * @param name
    */
   public XSLTServiceBindingValueSourceUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      testee = new XSLTServiceBindingValueSourceImpl();      
      xsltConfig = getXSLTConfig();
      bindingMetadata.setServiceBindingValueSourceConfig(xsltConfig);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
   }

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getElementServiceBindingValue(org.jboss.bindings.ServiceBinding, org.w3c.dom.Element)}.
    */
   public void testGetElementServiceBindingValue() throws Exception
   {
      Element input = getDocumentElementFromClasspath(XSL_INPUT);
      Element output = testee.getElementServiceBindingValue(binding, input);
      validateXSLTOutputElement(output);
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

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getResourceServiceBindingValue(org.jboss.bindings.ServiceBinding, java.lang.String)}.
    */
   public void testGetResourceServiceBindingValue() throws Exception
   {
      String output = testee.getResourceServiceBindingValue(binding, XSL_INPUT);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element);
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

   /**
    * Test method for {@link org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl#getURLServiceBindingValue(org.jboss.bindings.ServiceBinding, java.net.URL)}.
    */
   public void testGetURLServiceBindingValue() throws Exception
   {
      URL input = Thread.currentThread().getContextClassLoader().getResource(XSL_INPUT);
      URL output = testee.getURLServiceBindingValue(binding, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element); 
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

   public void testGetServiceBindingValueNullInput() throws Exception
   {
      Object[] params = null;
      try
      {
         testee.getServiceBindingValue(binding, params);
         fail("Should fail passing a null input");
      }
      catch (IllegalArgumentException good) {}
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
      String output = (String) testee.getServiceBindingValue(binding, XSL_INPUT);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element);
   }
   
   public void testGetServiceBindingValueElementInput() throws Exception
   {
      Element input = getDocumentElementFromClasspath(XSL_INPUT);
      Element output = (Element) testee.getServiceBindingValue(binding, input);
      validateXSLTOutputElement(output);
   }
   
   public void testGetServiceBindingValueURLInput() throws Exception
   {
      URL input = Thread.currentThread().getContextClassLoader().getResource(XSL_INPUT);
      URL output = (URL) testee.getServiceBindingValue(binding, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element);      
   }
   
   public void testNullConfigObject() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(null);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      String input = getFullyQualifiedResourceName("xlst_input.xml");
      try
      {
         testee.getResourceServiceBindingValue(binding, input);
         fail("Should fail using an unknown xsltConfig");
      }
      catch (IllegalStateException good) {}
   }
   
   public void testUnknownConfigObject() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(new Object());
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
      String input = getFullyQualifiedResourceName("xlst_input.xml");
      try
      {
         testee.getResourceServiceBindingValue(binding, input);
         fail("Should fail using an unknown xsltConfig");
      }
      catch (IllegalStateException good) {}
   }

}
