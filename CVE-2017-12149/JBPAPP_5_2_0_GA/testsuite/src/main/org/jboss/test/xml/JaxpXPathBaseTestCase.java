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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * A Simple class for jaxp XPath Test Cases
 *
 * @author <a href="mailto:a.walker@base2services.com">Aaron Walker</a>
 * @version $Revision: 81036 $ 
 */
public class JaxpXPathBaseTestCase extends TestCase
{
   protected static final String XML_STRING_SIMPLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<employees>"
         + "  <employee>"
         + "    <name>e1</name>"
         + "  </employee>"
         + "  <employee>"
         + "    <name>e2</name>"
         + "  </employee>" + "</employees>";

   protected static final String XML_STRING_NS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<foo:employees xmlns:foo=\"http://www.jboss.org/foobar\">"
         + "  <foo:employee>"
         + "    <name>e1</name>"
         + "  </foo:employee>"
         + "  <foo:employee>"
         + "    <name>e2</name>"
         + "  </foo:employee>"
         + "</foo:employees>";

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   public void testXPathDefaultFactoryCreate()
   {
      assertNotNull(newXpathFactoryInstance());
   }

   public void testSimpleXpathExpression() throws Exception
   {
      XPathFactory xpathFactory = newXpathFactoryInstance();
      XPath xpath = xpathFactory.newXPath();

      Document doc = parseXML(XML_STRING_SIMPLE);

      String xpe1 = "/employees/employee";
      XPathExpression employeesXPath = xpath.compile(xpe1);

      NodeList nl = (NodeList) employeesXPath.evaluate(doc,
            XPathConstants.NODESET);

      assertNotNull(nl);
      assertEquals(nl.getLength(), 2);
      assertEquals(nl.item(0).getTextContent().trim(), "e1");
      assertEquals(nl.item(1).getTextContent().trim(), "e2");
   }

   public void testNamespaceXpathExpression() throws Exception
   {
      XPathFactory xpathFactory = newXpathFactoryInstance();
      XPath xpath = xpathFactory.newXPath();
      xpath.setNamespaceContext(new JBossFooBarNamespaceContext());

      Document doc = parseXML(XML_STRING_NS);

      String xpe1 = "/employees/employee";
      XPathExpression badXPath = xpath.compile(xpe1);
      NodeList nl = (NodeList) badXPath.evaluate(doc, XPathConstants.NODESET);
      assertNotNull(nl);
      assertEquals(0, nl.getLength());

      String xpe2 = "//foo:employee";
      XPathExpression empXPath = xpath.compile(xpe2);
      NodeList nl2 = (NodeList) empXPath.evaluate(doc, XPathConstants.NODESET);

      assertNotNull(nl2);
      assertEquals(2, nl2.getLength());
      assertEquals("e1", nl2.item(0).getTextContent().trim());
      assertEquals("e2", nl2.item(1).getTextContent().trim());
   }

   protected XPathFactory newXpathFactoryInstance()
   {
      return XPathFactory.newInstance();
   }

   protected Document parseXML(String xml) throws Exception
   {
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      dbfactory.setNamespaceAware(true);
      dbfactory.setXIncludeAware(true);

      DocumentBuilder parser = dbfactory.newDocumentBuilder();

      ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());

      Document doc = parser.parse(is);

      return doc;
   }

   protected class JBossFooBarNamespaceContext implements NamespaceContext
   {
      public String getNamespaceURI(String prefix)
      {
         return "http://www.jboss.org/foobar";
      }

      public String getPrefix(String namespaceURI)
      {
         return "foo";
      }

      public Iterator getPrefixes(String namespaceURI)
      {
         ArrayList list = new ArrayList();
         list.add("foo");
         return list.iterator();
      }
   }
}