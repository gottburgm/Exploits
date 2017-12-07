/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.servicebindingmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import javax.security.auth.login.FailedLoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.test.JBossTestCase;

/**
 * @author bmaxwell
 *
 */
public class JBPAPP4281UnitTestCase extends JBossTestCase
{
   // https://issues.jboss.org/browse/JBPAPP-4281
 
   public JBPAPP4281UnitTestCase(String name)
   {
      super(name);      
   }

   public void testAJPBinding()
   {
      try
      {
         // read the bindings-jboss-beans.xml and parse out the xslt
         String defaultServerProfileBindingsXml = extractXSLTFromXml( System.getProperty("jboss.dist") + "/server/all/conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml" );
         
         // the server.xml we will transform
         URL serverXMLURL = Thread.currentThread().getContextClassLoader().getResource("servicebindingmanager/jbpapp4281/server.xml");
         
         InputStream serverXML = serverXMLURL.openStream();
         
         InputStream xsltStream = new ByteArrayInputStream(defaultServerProfileBindingsXml.getBytes());
   
         // the xslt stylesheet, xml input source to transform
         OutputStream os = transform(xsltStream, serverXML);
         System.out.println("**************** Transformed ****************************");
         System.out.println(os.toString());
   
         if ( os.toString().contains(("<Connector protocol=\"AJP/1.3\" port=\"8009\" address=\"${jboss.bind.address}\" redirectPort=\"8443\"/>") ))
         {            
            fail("AJP connector is 8443 when it should be 443");
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         fail(e.getMessage());
      }
   }

   public String extractXSLTFromXml(String path)
   {
      String xsltString = null;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setIgnoringElementContentWhitespace(true); 
      dbf.setIgnoringComments(true);
      
      try 
      {
          DocumentBuilder db = dbf.newDocumentBuilder();
          
          Document dom = db.parse(path);       
          
          Element root = dom.getDocumentElement();
          NodeList beans = root.getElementsByTagName("bean");
          
          System.out.println("Found " + beans.getLength() + " beans");
          
          Element xslt = null;
          
          for(int i=0; i<beans.getLength(); i++)
          {
             Element bean = (Element) beans.item(i);
             String name = bean.getAttribute("name");
             if(name != null && name.equals("JBossWebConnectorXSLTConfig"))
             {
                System.out.println("Found JBossWebConnectorXSLTConfig: ");
                xslt = bean;
                break;
             }
          }
          Element constructor = (Element) xslt.getElementsByTagName("constructor").item(0);
          Element parameter = (Element) constructor.getElementsByTagName("parameter").item(0);
          xsltString = parameter.getTextContent();
          System.out.println("XSLT: " + xsltString);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      return xsltString;
   }
   
   public OutputStream transform(InputStream xsltInputStream, InputStream xmlInputStream) throws Exception
   {      
      TransformerFactory factory = TransformerFactory.newInstance();            
      
      // Use the factory to create a template containing the xsl stream 
      Templates template = factory.newTemplates(new StreamSource(xsltInputStream));

      Transformer xformer = template.newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      
      xformer.setParameter("port", 8080);
      
      // Prepare the input and output sources
      Source source = new StreamSource(xmlInputStream);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Result result = new StreamResult(baos);

      // Apply the xsl file to the source file and write the result to the output file
      xformer.transform(source, result);
      
      return baos;
   }
   
   public StreamResult readIgnoreComments(File file) throws Exception
   {      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      // Ignores all the comments described in the XML File
      factory.setIgnoringComments(true);
    
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(file);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
               
      DOMSource source = new DOMSource(doc);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();      
      StreamResult result = new StreamResult(baos);
      
      transformer.transform(source, result);
      
      return result;
   }
   
   private String readIgnoreCommentsToString(File file) throws Exception
   {      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      // Ignores all the comments described in the XML File
      factory.setIgnoringComments(true);
    
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(file);
      
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);      
      return writer.toString();      
   }
   
}
