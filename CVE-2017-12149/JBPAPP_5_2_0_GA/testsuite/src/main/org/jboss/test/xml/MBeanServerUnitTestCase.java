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
package org.jboss.test.xml;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.net.URL;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.security.auth.login.AppConfigurationEntry;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.jboss.test.xml.mbeanserver.Services;
import org.jboss.test.xml.mbeanserver.MBeanData;
import org.jboss.test.xml.mbeanserver.MBeanAttribute;
import org.jboss.test.xml.mbeanserver.PolicyConfig;
import org.jboss.test.xml.mbeanserver.AuthenticationInfo;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.XsdBinder;
import org.jboss.naming.JNDIBindings;
import org.jboss.naming.JNDIBinding;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Test unmarshalling xml documents conforming to mbean-service_1_0.xsd into
 * the org.jboss.test.xml.mbeanserver.Services and related objects.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113110 $
 */
public class MBeanServerUnitTestCase
   extends TestCase
{
   public void testMbeanService() throws Exception
   {
      InputStream is = getResource("xml/mbeanserver/mbean-service_1_0.xsd");
      SchemaBinding schemaBinding = XsdBinder.bind(is, null);
      schemaBinding.setIgnoreUnresolvedFieldOrClass(true);
      schemaBinding.setSchemaResolver(new SchemaBindingResolver()
      {
         public String getBaseURI()
         {
            throw new UnsupportedOperationException("getBaseURI is not implemented.");
         }

         public void setBaseURI(String baseURI)
         {
            throw new UnsupportedOperationException("setBaseURI is not implemented.");
         }

         public SchemaBinding resolve(String nsUri, String baseURI, String schemaLocation)
         {
            try
            {
               if("urn:jboss:login-config2".equals(nsUri))
               {
                  InputStream is = getResource("xml/mbeanserver/login-config2.xsd");
                  SchemaBinding schemaBinding = XsdBinder.bind(is, null, baseURI);
                  schemaBinding.setSchemaResolver(this);
                  return schemaBinding;
               }
               else if("urn:jboss:user-roles".equals(nsUri))
               {
                  InputStream is = getResource("xml/mbeanserver/user-roles.xsd");
                  return XsdBinder.bind(is, null, baseURI);
               }
               else
               {
                  throw new JBossXBRuntimeException("Unrecognized namespace: " + nsUri);
               }
            }
            catch(IOException e)
            {
               throw new JBossXBRuntimeException("IO error", e);
            }
         }

         public LSInput resolveAsLSInput(String nsUri, String baseUri, String schemaLocation)
         {
            throw new UnsupportedOperationException("resolveResource is not implemented.");
         }
      });

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      InputStream is2 = getResource("xml/mbeanserver/testObjFactory.xml");
      Services services = (Services)unmarshaller.unmarshal(is2, schemaBinding);
      List mbeans = services.getMBeans();
      assertEquals("There is 1 mbean", 1, mbeans.size());

      MBeanData mbean = (MBeanData) mbeans.get(0);
      assertEquals("Wrong class name","org.jboss.security.auth.login.DynamicLoginConfig", mbean.getCode());
      assertEquals("Name","jboss.security.tests:service=DynamicLoginConfig", mbean.getName());
      Map attributes = mbean.getAttributeMap();
      assertEquals("Wrong number of attributes", 2, attributes.size());
      MBeanAttribute attr = (MBeanAttribute) attributes.get("PolicyConfig");
      Object value = attr.getValue();
      assertTrue("Value isA PolicyConfig",
          value instanceof PolicyConfig );
      PolicyConfig pc = (PolicyConfig) value;
      assertEquals("Wrong number of AuthenticationInfo", 1, pc.size());
      AuthenticationInfo auth = pc.get("conf1");
      assertNotNull("The AuthenticationInfo name ic config1", auth);
      AppConfigurationEntry[] ace = auth.getAppConfigurationEntry();
      assertNotNull("AppConfiguration entry must not be null",ace);
      assertEquals("The AppConfigurationEntry must have one entry", 1 ,ace.length);
      assertEquals("LoginModuleName", "org.jboss.security.auth.spi.IdentityLoginModule", ace[0].getLoginModuleName());

      attr = (MBeanAttribute) attributes.get("UserHome");
      assertEquals("UserHome", attr.getName());
      assertNotNull("Text != null", attr.getText() );
   }

   /**
    * A test of unmarshalling an element from a document without any knowledge
    * of the associated schema.
    * 
    * @throws Exception
    */ 
   public void testJndiBindings() throws Exception
   {
      InputStream is = getResource("xml/mbeanserver/testBinding.xml");
      // Get the Bindings attribute element
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(is);
      NodeList attributes = doc.getElementsByTagName("attribute");
      Element element = (Element) attributes.item(0);
      NodeList children = element.getChildNodes();
      Element content = null;
      for(int n = 0; n < children.getLength(); n ++)
      {
         Node node = children.item(n);
         if( node.getNodeType() == Node.ELEMENT_NODE )
         {
            content = (Element) node;
            break;
         }
      }

      // Get a parsable representation of this elements content
      DOMSource source = new DOMSource(content);
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(baos);
      transformer.transform(source, result);
      baos.close();

      ByteArrayInputStream is2 = new ByteArrayInputStream(baos.toByteArray());

      /* Parse the element content using the Unmarshaller starting with an
      empty schema since we don't know anything about it. This is not quite
      true as we set the schema baseURI to the resources/xml/naming/ directory
      so that the jndi-binding-service_1_0.xsd can be found, but this baseURI
      can be easily specified to the SARDeployer, or the schema can be made
      available to the entity resolver via some other configuration.
      */
      final URL url = Thread.currentThread().getContextClassLoader().getResource("xml/naming/");

      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.setEntityResolver(new JBossEntityResolver(){
         public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
         {
            if(systemId.endsWith("custom-object-binding.xsd") ||
               systemId.endsWith("jndi-binding-service_1_0.xsd"))
            {
               String fileName = systemId.substring(systemId.lastIndexOf('/') + 1);
               URL url = Thread.currentThread().getContextClassLoader().
                  getResource("xml/naming/" + fileName);
               return new InputSource(url.toExternalForm());
            }
            else
            {
               return super.resolveEntity(publicId, systemId);
            }
         }
      });

      JNDIBindings bindings = (JNDIBindings) unmarshaller.unmarshal(is2,
         new SchemaBindingResolver(){
            public String getBaseURI()
            {
               throw new UnsupportedOperationException("getBaseURI is not implemented.");
            }

            public void setBaseURI(String baseURI)
            {
               throw new UnsupportedOperationException("setBaseURI is not implemented.");
            }

            public SchemaBinding resolve(String nsUri, String baseURI, String schemaLocation)
            {
               return XsdBinder.bind(url.toExternalForm() + schemaLocation, this);
            }

            public LSInput resolveAsLSInput(String nsUri, String baseUri, String schemaLocation)
            {
               throw new UnsupportedOperationException("resolveAsLSInput is not implemented.");
            }
         });

      is2.close();

      // Validate the bindings
      JNDIBinding[] values = bindings.getBindings();
      assertEquals("Wrong bindings count", 5, values.length);

      JNDIBinding key1 = values[0];
      assertEquals("values[0]", "ctx1/key1",key1.getName());
      assertEquals("values[0] text", "value1",key1.getText());

      JNDIBinding userHome = values[1];
      assertEquals("values[1]", "ctx1/user.home",userHome.getName());
      String p = System.getProperty("user.home");
      assertEquals("values[1] property( ${user.home} )", p,userHome.getText());

      // Test binding from a text to URL based on the type attribute PropertyEditor
      JNDIBinding jbossHome = values[2];
      assertEquals("values[2]", "ctx1/key2", jbossHome.getName());
      assertEquals("values[2] text", "http://www.jboss.org", jbossHome.getText());
      assertEquals("values[2] type","java.net.URL", jbossHome.getType());
      Object value2 = jbossHome.getValue();
      assertEquals("values[2] as URL(http://www.jboss.org)", new URL("http://www.jboss.org"), value2);

      // Test a binding from an xml fragment from a foreign namespace.
      JNDIBinding properties = values[3];
      Object value = properties.getValue();
      assertEquals("values[3]", "ctx2/key1", properties.getName());
      assertEquals("values[3] is java.util.Properties", Properties.class,value.getClass());
      Properties props = (Properties) value;
      assertEquals("Properties(key1)", "value1", props.getProperty("key1"));
      assertEquals("Properties(key2)", "value2", props.getProperty("key2"));

      // Test binding from a text to InetAddress based on the editor attribute PropertyEditor
      JNDIBinding host = values[4];
      assertEquals("values[4] ", "hosts/localhost", host.getName());
      assertTrue(host.isTrim());
      assertEquals("values[4] text","127.0.0.1",
         host.getText());
      assertEquals("values[4] editor","org.jboss.common.beans.property.InetAddressEditor", host.getEditor());
      Object value4 = host.getValue();
      InetAddress hostValue = (InetAddress) value4;
      InetAddress localhost = InetAddress.getByName("127.0.0.1");
      assertEquals("values[4] value InetAddress(127.0.0.1)",localhost.getHostAddress(), hostValue.getHostAddress());

   }

   // Private

   private InputStream getResource(String path)
      throws IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if(url == null)
      {
         fail("URL not found: " + path);
      }
      return url.openStream();
   }
}
