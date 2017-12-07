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
package org.jboss.test.xslt.support;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * A test mbean service.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class XalanCheck extends ServiceMBeanSupport
   implements XalanCheckMBean
{
   // Constructors --------------------------------------------------
  
   /**
    * CTOR
   **/
   public XalanCheck()
   {
      // empty
   }

   // Attributes -----------------------------------------------------
   
   public String getXalanVersion()
   {
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class c = loader.loadClass("org.apache.xalan.Version");
         Object v = c.newInstance();
         Class[] sig = {};
         Method getVersion = c.getDeclaredMethod("getVersion", sig);
         Object[] args = {};
         String version = (String) getVersion.invoke(v, args);
         return version;
      }
      catch(Throwable e)
      {
         throw new UndeclaredThrowableException(e);
      }      
   }
   
   // Operations -----------------------------------------------------
  
   public Hashtable fetchXalanEnvironmentHash()
   {
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Class c = loader.loadClass("org.apache.xalan.xslt.EnvironmentCheck");
         Object envc = c.newInstance();
         Class[] sig = {};
         Method getEnvironmentHash = c.getDeclaredMethod("getEnvironmentHash", sig);
         Object[] args = {};
         Hashtable htab = (Hashtable) getEnvironmentHash.invoke(envc, args);
         return htab;
      }
      catch(Throwable e)
      {
         throw new UndeclaredThrowableException(e);
      }
   }
   
   /**
    * Throws an exception when run using xalan 2.5.2
    * Borrowed from here: http://issues.apache.org/bugzilla/show_bug.cgi?id=15140
    */
   public void testXalan25Bug15140() throws Exception
   {
       String testString = "<doc xmlns:a=\"http://www.test.com\"/>";
       
       SAXParserFactory parserFactory
         = SAXParserFactory.newInstance();
       SAXParser parser = parserFactory.newSAXParser();
       XMLReader reader = parser.getXMLReader();
       reader.setFeature("http://xml.org/sax/features/namespaces", true);
       reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true); 

       DOMResult domResult = new DOMResult();
       SAXTransformerFactory transformerFactory
         = (SAXTransformerFactory) TransformerFactory.newInstance();
       TransformerHandler handler = transformerFactory.newTransformerHandler();
       handler.setResult(domResult);

       reader.setContentHandler(handler);

       InputSource input = new InputSource(new StringReader(testString));
       reader.parse(input);
   }
}
