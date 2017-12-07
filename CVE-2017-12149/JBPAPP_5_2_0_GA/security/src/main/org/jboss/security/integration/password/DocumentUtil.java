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
package org.jboss.security.integration.password;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node; 
import org.xml.sax.InputSource;

/**
 * Utility dealing with DOM
 * @author Anil.Saldhana@redhat.com
 * @since Jan 14, 2009
 */
public class DocumentUtil
{
   /**
    * Create a new document
    * @return
    * @throws Exception
    */
   public static Document createDocument() throws Exception
   {
      DocumentBuilderFactory factory = getFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.newDocument(); 
   }
   
   /**
    * Parse a document from the string
    * @param docString
    * @return
    * @throws Exception
    */
   public static Document getDocument(String docString) throws Exception
   {
      return getDocument(new StringReader(docString));
   }
   
   /**
    * Parse a document from a reader
    * @param reader
    * @return
    * @throws Exception
    */
   public static Document getDocument(Reader reader) throws Exception
   {
      DocumentBuilderFactory factory = getFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new InputSource(reader));
   }
   
   /**
    * Get Document from a file
    * @param file
    * @return
    * @throws Exception
    */
   public static Document getDocument(File file) throws Exception
   {
      DocumentBuilderFactory factory = getFactory(); 
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      builder.setErrorHandler(new SysOutErrorHandler());
      return builder.parse(file);
   }
  
   /**
    * Get Document from an inputstream
    * @param is
    * @return
    * @throws Exception
    */
   public static Document getDocument(InputStream is) throws Exception
   {
      DocumentBuilderFactory factory = getFactory(); 
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      builder.setErrorHandler(new SysOutErrorHandler());
      return builder.parse(is);
   }
   
   /**
    * Marshall a document into a String
    * @param signedDoc
    * @return
    * @throws Exception
    */
   public static String getDocumentAsString(Document signedDoc) throws Exception
   {
     Source source = new DOMSource(signedDoc);
     StringWriter sw = new StringWriter();
 
     Result streamResult = new StreamResult(sw);
     // Write the DOM document to the stream
     Transformer xformer = TransformerFactory.newInstance().newTransformer();
     xformer.transform(source, streamResult);
     
     return sw.toString();
   }
 
   /**
    * Marshall a DOM Element as string
    * @param element
    * @return
    * @throws Exception
    */
   public static String getDOMElementAsString(Element element) throws Exception
   {
     Source source = new DOMSource(element);
     StringWriter sw = new StringWriter();
 
     Result streamResult = new StreamResult(sw);
     // Write the DOM document to the file
     Transformer xformer = TransformerFactory.newInstance().newTransformer();
     xformer.transform(source, streamResult);
     
     return sw.toString();
   }
   
   /**
    * Stream a DOM Node as an input stream
    * @param node
    * @return
    * @throws Exception
    */
   public static InputStream getNodeAsStream(Node node) throws Exception
   {
      Source source = new DOMSource(node);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      Result streamResult = new StreamResult(baos);
      // Write the DOM document to the stream
      Transformer transformer = TransformerFactory.newInstance().newTransformer(); 
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(source, streamResult);
      
      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      
      return bis;
    } 
  
   private static DocumentBuilderFactory getFactory()
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory;
   }
   
   /** 
    * Error handler for validating parser. 
    */ 
   private static class SysOutErrorHandler implements org.xml.sax.ErrorHandler  {  
     /** 
      * Report the warning to the console. 
      */ 
     public void warning ( org.xml.sax.SAXParseException ex )  
         throws org.xml.sax.SAXException  {  
       System.out.println ( "Warning: " + ex.getMessage (  )  ) ; 
      }  
    
    
     /** 
      * Report the error to the console. 
      */ 
     public void error ( org.xml.sax.SAXParseException ex )  
         throws org.xml.sax.SAXException  {  
       System.out.println ( "Error: " + ex.getMessage (  )  ) ; 
      }  
    
    
     /** 
      * Report the fatal error to the console. 
      */ 
     public void fatalError ( org.xml.sax.SAXParseException ex )  
         throws org.xml.sax.SAXException  {  
       System.out.println ( "Fatal error: " + ex.getMessage (  )  ) ; 
      }  
    }  
}