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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.XSLTServiceBindingValueSourceConfig;
import org.jboss.common.beans.property.ElementEditor;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Base class for service binding manager related test classes.
 * 
 * @author Brian Stansberry
 * @version $Revision: 113110 $
 */
public abstract class ServiceBindingTestBase extends TestCase
{
   protected static final String SVC_NAME = "SVC";
   protected static final String BINDING_NAME = "BIND"; 
   protected static final String HOST = "192.168.0.10";
   protected static final int PORT = 999;
   protected static final String HOST_MARKER = "${host}";
   protected static final String PORT_MARKER = "${port}";   
   protected static final String ELEMENT_INPUT = "<element host=\"" + HOST_MARKER + "\">" + PORT_MARKER + "</element>";

   protected static final String OTHER_PARAM = "other";
   protected static final String OTHER_VALUE = "ABC";
   protected static final String XSL_INPUT = getFullyQualifiedResourceName("xslt_input.xml");
 
   private static final String SERVER_TEMP_DIR_PROPERTY = "jboss.server.temp.dir";
   
   protected ServiceBindingMetadata bindingMetadata;
   protected ServiceBinding binding;
   
   private PropertyEditor existingElementEditor;
   private String serverTempDir;

   public ServiceBindingTestBase()
   {
      super();
   }

   public ServiceBindingTestBase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      existingElementEditor = PropertyEditorManager.findEditor(Element.class);
      if (existingElementEditor == null)
         PropertyEditorManager.registerEditor(Element.class, ElementEditor.class);
      
      serverTempDir = System.getProperty(SERVER_TEMP_DIR_PROPERTY);
      if (serverTempDir == null)
         System.setProperty(SERVER_TEMP_DIR_PROPERTY, System.getProperty("java.io.tmpdir"));
      
      bindingMetadata = new ServiceBindingMetadata(SVC_NAME, BINDING_NAME, HOST, PORT, false, false);
      binding = new ServiceBinding(bindingMetadata, HOST, 0);
   }

   @Override
   protected void tearDown() throws Exception
   {      
      if (existingElementEditor == null)
         PropertyEditorManager.registerEditor(Element.class, null);
      if (serverTempDir == null)
         System.clearProperty(SERVER_TEMP_DIR_PROPERTY);
      
      super.tearDown();
   }
   
   public static String getFullyQualifiedResourceName(String unqualified)
   {
      String base = ServiceBindingTestBase.class.getPackage().getName();
      base = base.replace('.', '/');
      return base + "/" + unqualified;
   }

   protected void validateOutputElement(Element output)
   {
      assertNotNull(output);
      assertEquals(HOST, output.getAttribute("host"));
      assertEquals(String.valueOf(PORT), output.getFirstChild().getNodeValue());
   }

   protected void validateXSLTOutputElement(Element output)
   {
      assertNotNull(output);
      NodeList children = output.getElementsByTagName("attribute");
      assertEquals(3, children.getLength());
      for (int i = 0; i < children.getLength(); i++)
      {
         Element e = (Element) children.item(i);
         String name = e.getAttribute("name");         
         if ("host".equals(name))
            assertEquals(HOST, e.getFirstChild().getNodeValue());
         else if ("port".equals(name))
            assertEquals(String.valueOf(PORT), e.getFirstChild().getNodeValue());
         else if ("other".equals(name))
            assertEquals(OTHER_VALUE, e.getFirstChild().getNodeValue());
         else
            fail("Unknown attribute name " + name);
      }
   }
   
   public static Element getDocumentElement(String filename) throws Exception
   {
      File f = new File(filename);
      Assert.assertTrue(f.exists());
      Assert.assertFalse(f.isDirectory());
      InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
      return getDocumentElement(reader);
   }
   
   public static Element getDocumentElementFromClasspath(String resourcename) throws Exception
   {      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamReader reader = new InputStreamReader(cl.getResourceAsStream(resourcename));
      return getDocumentElement(reader);
   }
   
   public static Element getDocumentElement(URL url) throws Exception
   {
      URLConnection conn = url.openConnection();
      conn.connect();
      InputStreamReader reader = new InputStreamReader(url.openStream());
      return getDocumentElement(reader);
   }
   
   public static Element getDocumentElement(Reader reader) throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder parser = factory.newDocumentBuilder();
      try
      {
         
         InputSource is = new InputSource(reader);
//         is.setSystemId(url.toString());
         parser.setEntityResolver(new JBossEntityResolver());
   
         Document document = parser.parse(is);
         return document.getDocumentElement();
      }
      finally
      {
         reader.close();
      }
      
   }
   
   public static XSLTServiceBindingValueSourceConfig getXSLTConfig() throws Exception
   {
      Element element = getDocumentElementFromClasspath(getFullyQualifiedResourceName("xslt.xml"));
      String xslt = element.getFirstChild().getNodeValue();
      
      Map<String, String> addlParams = new HashMap<String, String>();
      addlParams.put(OTHER_PARAM, OTHER_VALUE);
      
      return new XSLTServiceBindingValueSourceConfig(xslt, addlParams);
   }
   
   

}