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
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingManager;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.w3c.dom.Element;

/**
 * Tests of ServiceBindingManager.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ServiceBindingManagerUnitTestCase extends ServiceBindingTestBase
{
   private static final String SERVER = "server";
   private static final String INPUT = "${host}";
   
   private ServiceBindingManager testee;
   private ServiceBindingMetadata noNameMetadata;
   private ServiceBinding noNameBinding;
   private MockServiceBindingStore mockStore;
   private InetAddress address;
   
   /**
    * Create a new ServiceBindingManagerUnitTestCase.
    * 
    * @param arg0
    */
   public ServiceBindingManagerUnitTestCase(String arg0)
   {
      super(arg0);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      mockStore = new MockServiceBindingStore(binding, SERVER);
      testee = new ServiceBindingManager(SERVER, mockStore);
      noNameMetadata = new ServiceBindingMetadata(SVC_NAME, null, null, PORT);
      noNameBinding = new ServiceBinding(noNameMetadata, HOST, 0);
      address = InetAddress.getByName(HOST);
   }
   
   public void testGetServerName()
   {
      assertEquals(SERVER, testee.getServerName());
   }
   
   public void testGetServiceBindings()
   {
      assertEquals(Collections.singleton(binding), testee.getServiceBindings());
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getIntBinding(java.lang.String)}.
    * @throws Exception 
    */
   public void testGetIntBindingString() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      assertEquals(PORT, testee.getIntBinding(SVC_NAME));
   }
   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getIntBinding(java.lang.String, java.lang.String)}.
    */
   public void testGetIntBindingStringString() throws Exception
   {
      assertEquals(PORT, testee.getIntBinding(SVC_NAME, BINDING_NAME));
   }
   
   public void testGetIntBindingViaGeneric() throws Exception
   {
      Integer result = new Integer(5);
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(result);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(5, testee.getIntBinding(SVC_NAME, BINDING_NAME));
      Object[] params = source.getParams();
      assertNull(params);
   }
   
   public void testGetIntBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      assertEquals(binding.getPort() + mockStore.getDefaultPortOffset(SERVER), testee.getIntBinding(SVC_NAME, BINDING_NAME, binding.getHostName(), binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(binding.getPort() + mockStore.getDefaultPortOffset(SERVER), testee.getIntBinding(SVC_NAME, BINDING_NAME, null, binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(binding.getPort(), testee.getIntBinding(SVC_NAME, BINDING_NAME, binding.getHostName(), binding.getPort(), true, true));
      
   }


   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getInetAddressBinding(java.lang.String)}.
    */
   public void testGetInetAddressBindingString() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      assertEquals(address, testee.getInetAddressBinding(SVC_NAME));
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getInetAddressBinding(java.lang.String, java.lang.String)}.
    */
   public void testGetInetAddressBindingStringString() throws Exception
   {
      assertEquals(address, testee.getInetAddressBinding(SVC_NAME, BINDING_NAME));
   }
   
   public void testGetInetAddressBindingViaGeneric() throws Exception
   {
      InetAddress result = InetAddress.getByName("localhost");
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(result);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(result, testee.getInetAddressBinding(SVC_NAME, BINDING_NAME));
      Object[] params = source.getParams();
      assertNull(params);
   }
   
   public void testGetInetAddressBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      assertEquals(binding.getBindAddress(), testee.getInetAddressBinding(SVC_NAME, BINDING_NAME, binding.getHostName(), binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(mockStore.getDefaultHostName(SERVER)), testee.getInetAddressBinding(SVC_NAME, BINDING_NAME, null, binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(mockStore.getDefaultHostName(SERVER)), testee.getInetAddressBinding(SVC_NAME, BINDING_NAME, binding.getHostName(), binding.getPort(), false, false));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(null), testee.getInetAddressBinding(SVC_NAME, BINDING_NAME, null, binding.getPort(), true, true));
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getStringBinding(java.lang.String, java.lang.String)}.
    */
   public void testGetStringBindingStringString() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      assertEquals(HOST, testee.getStringBinding(SVC_NAME, INPUT));
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getStringBinding(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testGetStringBindingStringStringString() throws Exception
   {
      assertEquals(HOST, testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT));
   }
   
   public void testGetStringBindingViaGeneric() throws Exception
   {
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(SVC_NAME);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(SVC_NAME, testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(INPUT, params[0]);
   }
   
   public void testGetStringBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      assertEquals(binding.getHostName(), testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT, binding.getHostName(), binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(mockStore.getDefaultHostName(SERVER)).getHostName(), testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT, null, binding.getPort()));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(mockStore.getDefaultHostName(SERVER)).getHostName(), testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT, binding.getHostName(), binding.getPort(), false, false));
      mockStore.setBinding(null);
      assertEquals(InetAddress.getByName(null).getHostName(), testee.getStringBinding(SVC_NAME, BINDING_NAME, INPUT, null, binding.getPort(), true, true));
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getElementBinding(java.lang.String, org.w3c.dom.Element)}.
    */
   public void testGetElementBindingStringElement() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(ELEMENT_INPUT);
      Element input = (Element) editor.getValue();
      Element output = testee.getElementBinding(SVC_NAME, input);
      validateOutputElement(output, false, false);
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getElementBinding(java.lang.String, java.lang.String, org.w3c.dom.Element)}.
    */
   public void testGetElementBindingStringStringElement() throws Exception
   {
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(ELEMENT_INPUT);
      Element input = (Element) editor.getValue();
      Element output = testee.getElementBinding(SVC_NAME, BINDING_NAME, input);
      validateOutputElement(output);
   }
   
   public void testGetElementBindingViaGeneric() throws Exception
   {
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(ELEMENT_INPUT);
      Element result = (Element) editor.getValue();
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(result);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(result, testee.getElementBinding(SVC_NAME, BINDING_NAME, result));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(result, params[0]);      
   }
   
   public void testGetElementBindingViaXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(getXSLTConfig());
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      
      Element input = getDocumentElementFromClasspath(XSL_INPUT);
      Element output = testee.getElementBinding(SVC_NAME, BINDING_NAME, input);
      validateXSLTOutputElement(output);
   }
   
   public void testGetElementBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      PropertyEditor editor = PropertyEditorManager.findEditor(Element.class);
      editor.setAsText(ELEMENT_INPUT);
      Element input = (Element) editor.getValue();
      Element output = testee.getElementBinding(SVC_NAME, BINDING_NAME, input, binding.getHostName(), binding.getPort());
      validateOutputElement(output, false, true);
      
      mockStore.setBinding(null);
      output =  testee.getElementBinding(SVC_NAME, BINDING_NAME, input, null, binding.getPort());
      validateOutputElement(output, true, true);
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getURLBinding(java.lang.String, java.net.URL)}.
    */
   public void testGetURLBindingStringURL() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      String resource = getFullyQualifiedResourceName("input.xml");
      URL input = Thread.currentThread().getContextClassLoader().getResource(resource);
      URL output = testee.getURLBinding(SVC_NAME, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);      
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getURLBinding(java.lang.String, java.lang.String, java.net.URL)}.
    */
   public void testGetURLBindingStringStringURL() throws Exception
   {
      String resource = getFullyQualifiedResourceName("input.xml");
      URL input = Thread.currentThread().getContextClassLoader().getResource(resource);
      URL output = testee.getURLBinding(SVC_NAME, BINDING_NAME, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);     
   }
   
   public void testGetURLBindingViaGeneric() throws Exception
   {
      URL result = new File(getFullyQualifiedResourceName("input.xml")).toURL();
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(result);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(result, testee.getURLBinding(SVC_NAME, BINDING_NAME, result));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(result, params[0]);
   }
   
   public void testGetURLBindingViaXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(getXSLTConfig());
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      
      URL input = Thread.currentThread().getContextClassLoader().getResource(XSL_INPUT);
      URL output = testee.getURLBinding(SVC_NAME, BINDING_NAME, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element); 
   }
   
   public void testGetURLBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      String resource = getFullyQualifiedResourceName("input.xml");
      URL input = Thread.currentThread().getContextClassLoader().getResource(resource);
      URL output = testee.getURLBinding(SVC_NAME, BINDING_NAME, input, binding.getHostName(), binding.getPort());
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element, false, true);     
      
      mockStore.setBinding(null);
      output = testee.getURLBinding(SVC_NAME, BINDING_NAME, input, null, binding.getPort());
      assertNotNull(output);
      element = getDocumentElement(output);
      validateOutputElement(element, true, true);      
      
      mockStore.setBinding(null);
      output = testee.getURLBinding(SVC_NAME, BINDING_NAME, input, binding.getHostName(), binding.getPort(), false, false);
      assertNotNull(output);
      element = getDocumentElement(output);
      validateOutputElement(element, true, true);  
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getResourceBinding(java.lang.String, java.lang.String)}.
    */
   public void testGetResourceBindingStringString() throws Exception
   {
      String input = getFullyQualifiedResourceName("input.xml");
      String output = testee.getResourceBinding(SVC_NAME, BINDING_NAME, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element);      
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getResourceBinding(java.lang.String, java.lang.String, java.lang.String)}.
    */
   public void testGetResourceBindingStringStringString() throws Exception
   {
      mockStore.setBinding(noNameBinding);
      String input = getFullyQualifiedResourceName("input.xml");
      String output = testee.getResourceBinding(SVC_NAME, input);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element); 
   }
   
   public void testGetResourceBindingViaGeneric() throws Exception
   {
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(SVC_NAME);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(SVC_NAME, testee.getResourceBinding(SVC_NAME, BINDING_NAME, INPUT));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(INPUT, params[0]);
   }
   
   public void testGetResourceBindingViaXSLT() throws Exception
   {
      bindingMetadata.setServiceBindingValueSourceConfig(getXSLTConfig());
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      
      String output = testee.getResourceBinding(SVC_NAME, BINDING_NAME, XSL_INPUT);
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateXSLTOutputElement(element);
   }
   
   public void testGetResourceBindingNoBinding() throws Exception
   {
      mockStore.setBinding(null);
      String input = getFullyQualifiedResourceName("input.xml");
      String output = testee.getResourceBinding(SVC_NAME, BINDING_NAME, input, binding.getHostName(), binding.getPort());
      assertNotNull(output);
      Element element = getDocumentElement(output);
      validateOutputElement(element, false, true);     
      
      mockStore.setBinding(null);
      output = testee.getResourceBinding(SVC_NAME, BINDING_NAME, input, null, binding.getPort());
      assertNotNull(output);
      element = getDocumentElement(output);
      validateOutputElement(element, true, true);       
      
      mockStore.setBinding(null);
      output = testee.getResourceBinding(SVC_NAME, BINDING_NAME, input, binding.getHostName(), binding.getPort(), false, false);
      assertNotNull(output);
      element = getDocumentElement(output);
      validateOutputElement(element, true, true);  
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getGenericBinding(java.lang.String, java.lang.Object[])}.
    */
   public void testGetGenericBindingStringObjectArray() throws Exception
   {
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(SVC_NAME);
      noNameMetadata.setServiceBindingValueSource(source);
      noNameBinding = new ServiceBinding(noNameMetadata, HOST, 0); 
      mockStore.setBinding(noNameBinding);
      assertEquals(SVC_NAME, testee.getGenericBinding(SVC_NAME, null, INPUT));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(INPUT, params[0]);
   }

   /**
    * Test method for {@link org.jboss.services.binding.ServiceBindingManager#getGenericBinding(java.lang.String, java.lang.String, java.lang.Object[])}.
    */
   public void testGetGenericBindingStringStringObjectArray() throws Exception
   {
      MockServiceBindingValueSource source = new MockServiceBindingValueSource(SVC_NAME);
      bindingMetadata.setServiceBindingValueSource(source);
      binding = new ServiceBinding(bindingMetadata, HOST, 0); 
      mockStore.setBinding(binding);
      assertEquals(SVC_NAME, testee.getGenericBinding(SVC_NAME, BINDING_NAME, INPUT));
      Object[] params = source.getParams();
      assertNotNull(params);
      assertEquals(1, params.length);
      assertEquals(INPUT, params[0]);
   }
   
   public void testGetGenericBindingNoValueSource() throws Exception
   {
      try
      {
         Object[] params = null;
         testee.getGenericBinding(SVC_NAME, BINDING_NAME, params);
         fail("should not succeed without value source");         
      }
      catch (IllegalStateException good) {}
   }
   
   protected void validateOutputElement(Element output, boolean expectDefaultHost, boolean expectOffset)
   {
      assertNotNull(output);
      assertEquals(expectDefaultHost ? mockStore.getDefaultHostName(SERVER) : HOST, output.getAttribute("host"));
      int expectedPort = PORT + (expectOffset ? mockStore.getDefaultPortOffset(SERVER) : 0); 
      assertEquals(String.valueOf(expectedPort), output.getFirstChild().getNodeValue());
   }

}
