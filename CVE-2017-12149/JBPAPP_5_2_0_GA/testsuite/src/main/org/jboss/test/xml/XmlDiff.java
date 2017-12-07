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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.jboss.xb.binding.Constants;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class XmlDiff
{
   public static final ErrorHandler ERROR_HANDLER = new DefErrorHandler();

   public static final byte PRINT_ELEMENT = 0;
   public static final byte PRINT_PARENT = 1;
   public static final byte PRINT_ALL = 2;

   private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
   static
   {
      FACTORY.setNamespaceAware(true);
      FACTORY.setValidating(true);
   }

   private static final String INDENT = "  ";

   public static void main(String[] args) throws Exception
   {
      String xml1 =
         "<ns1:e xmlns:ns1='http://ns' attr1='attr1_val' ns1:attr2='attr2_val'>\n" +
         "  <ns1:child1>\n" +
         "     <ns2:child2 xmlns:ns2='http://ns2' child2_attr='child2_attr_val'>child2_val</ns2:child2>\n" +
         "  </ns1:child1>\n" +
         "  text\n" +
         "</ns1:e>";

      String xml2 =
         "<e xmlns='http://ns' attr1='attr1_val'" +
         " xmlns:ns='http://ns' ns:attr2='attr2_val'>text" +
         " <child1>" +
         "   <child2 xmlns='http://ns2' child2_attr='child2_attr_val'>child2_val</child2>" +
         " </child1>" +
         "</e>";

      System.out.println(new XmlDiff().diff(xml1, xml2));
   }

   public XmlDiff()
   {
      this(PRINT_ALL, true);
   }

   public XmlDiff(byte print, boolean reformat)
   {
      this.print = print;
      this.reformat = reformat;
   }

   private byte print = PRINT_ALL;
   private boolean reformat = true;

   public byte getPrint()
   {
      return print;
   }

   public void setPrint(byte print)
   {
      this.print = print;
   }

   public boolean isReformat()
   {
      return reformat;
   }

   public void setReformat(boolean reformat)
   {
      this.reformat = reformat;
   }

   /**
    * Compares two XML contents and returns a diff if they are different or null if they are equal.
    *
    * @param expected expected XML content
    * @param was      actual XML content
    * @return difference between XML contents or null if the contents are equal
    */
   public String diff(String expected, String was)
   {
      return diff(expected, was, ERROR_HANDLER, null);
   }

   public String diff(String expected, String was, ErrorHandler eh)
   {
      return diff(expected, was, eh, null);
   }

   public String diff(String expected, String was, EntityResolver er)
   {
      return diff(expected, was, ERROR_HANDLER, er);
   }

   public String diff(String expected, String was, ErrorHandler eh, EntityResolver er)
   {
      DocumentBuilder documentBuilder = null;
      try
      {
         documentBuilder = FACTORY.newDocumentBuilder();
      }
      catch(ParserConfigurationException e)
      {
         throw new IllegalStateException("Failed to create a document builder: " + e.getMessage());
      }

      if(eh != null)
      {
         documentBuilder.setErrorHandler(eh);
      }

      if(er != null)
      {
         documentBuilder.setEntityResolver(er);
      }

      Document expDoc = null;
      try
      {
         expDoc = documentBuilder.parse(new InputSource(new StringReader(expected)));
      }
      catch(Exception e)
      {
         throw new IllegalStateException("Failed to parse expected XML\n" + expected + ": " + e.getMessage());
      }

      Document wasDoc = null;
      try
      {
         wasDoc = documentBuilder.parse(new InputSource(new StringReader(was)));
      }
      catch(Exception e)
      {
         throw new IllegalStateException("Failed to parse XML\n" + was + ": " + e.getMessage());
      }

      Element expElement = expDoc.getDocumentElement();
      Element wasElement = wasDoc.getDocumentElement();
      return assertEquals(expElement, wasElement, expElement, wasElement);
   }

   private String assertEquals(Element exp, Element was, Element printAsExp, Element printAsWas)
   {
      QName expName = new QName(exp.getNamespaceURI(), exp.getLocalName());
      QName wasName = new QName(was.getNamespaceURI(), was.getLocalName());

      if(!expName.equals(wasName))
      {
         return fail("Expected name " + expName + " but was " + wasName, exp, was);
      }

      NamedNodeMap expAttrs = exp.getAttributes();
      NamedNodeMap wasAttrs = was.getAttributes();

      if(expAttrs == null && wasAttrs != null && hasNonIgnorableNs(wasAttrs))
      {
         return fail("Element " + expName + " doesn't have attributes", printAsExp, printAsWas);
      }
      else if(wasAttrs == null && expAttrs != null && hasNonIgnorableNs(expAttrs))
      {
         return fail("Element " + expName + " has attributes", printAsExp, printAsWas);
      }
      else if(expAttrs != null && wasAttrs != null)
      {
         String msg = assertAttrs(expAttrs, wasAttrs, printAsExp);
         if(msg != null)
         {
            return fail(msg, printAsExp, printAsWas);
         }
      }

      NodeList expChildren = exp.getChildNodes();
      NodeList wasChildren = was.getChildNodes();

      NodeList expTexts = getTextNodes(expChildren);
      NodeList wasTexts = getTextNodes(wasChildren);
      if(expTexts.getLength() > 0 && wasTexts.getLength() == 0)
      {
         return fail("Element " + expName + " has text content", printAsExp, printAsWas);
      }
      else if(expTexts.getLength() == 0 && wasTexts.getLength() > 0)
      {
         return fail("Element " + expName + " doesn't have text content", printAsExp, printAsWas);
      }
      // todo: should text content be concatenated before comparison?
      else if(expTexts.getLength() != wasTexts.getLength())
      {
         return fail(
            "Element " + expName + " has " + expTexts.getLength() + " text nodes (was " + wasTexts.getLength() + ")",
            printAsExp,
            printAsWas
         );
      }
      else if(expTexts.getLength() > 0 && wasTexts.getLength() > 0)
      {
         for(int i = 0; i < expTexts.getLength(); ++i)
         {
            Text text = (Text)expTexts.item(i);
            if(!containsText(text.getNodeValue(), wasTexts, i))
            {
               return fail("Element " + expName + " has text '" + text.getNodeValue() + "'", printAsExp, printAsWas);
            }
         }
      }

      NodeList expElems = sublist(expChildren, Node.ELEMENT_NODE);
      NodeList wasElems = sublist(wasChildren, Node.ELEMENT_NODE);
      if(expElems.getLength() > 0 && wasElems.getLength() == 0)
      {
         return fail("Element " + expName + " has child elements", printAsExp, printAsWas);
      }
      else if(expElems.getLength() == 0 && wasElems.getLength() > 0)
      {
         return fail("Element " + expName + " doesn't have child elements", printAsExp, printAsWas);
      }
      else if(expElems.getLength() != wasElems.getLength())
      {
         return fail("Element " +
            expName +
            " has " +
            expElems.getLength() +
            " child elements (was " +
            wasElems.getLength() +
            ")",
            printAsExp,
            printAsWas
         );
      }
      else if(expElems.getLength() > 0 && wasElems.getLength() > 0)
      {
         if(print == PRINT_PARENT)
         {
            printAsExp = exp;
            printAsWas = was;
         }

         for(int i = 0; i < expElems.getLength(); ++i)
         {
            Element expChild = (Element)expElems.item(i);
            Element wasChild = getElement(expChild.getNamespaceURI(), expChild.getLocalName(), wasElems, i);
            if(wasChild == null)
            {
               return fail("Element " +
                  expName +
                  " has child element " +
                  new QName(expChild.getNamespaceURI(), expChild.getLocalName()),
                  printAsExp,
                  printAsWas
               );
            }

            if(print == PRINT_ELEMENT)
            {
               printAsExp = expChild;
               printAsWas = wasChild;
            }

            String diff = assertEquals(expChild, wasChild, printAsExp, printAsWas);
            if(diff != null)
            {
               return diff;
            }
         }
      }
      return null;
   }

   private static Element getElement(String ns, String local, NodeList elements, int suggestedIndex)
   {
      if(suggestedIndex >= 0 && suggestedIndex < elements.getLength())
      {
         Element element = (Element)elements.item(suggestedIndex);
         if((ns == null && element.getNamespaceURI() == null ||
            ns != null && ns.equals(element.getNamespaceURI())
            ) &&
            local.equals(element.getLocalName()))
         {
            return element;
         }
      }

      for(int i = 0; i < elements.getLength(); ++i)
      {
         Element element = (Element)elements.item(i);
         if((ns == null && element.getNamespaceURI() == null ||
            ns != null && ns.equals(element.getNamespaceURI())
            ) &&
            local.equals(element.getLocalName()))
         {
            return element;
         }
      }
      return null;
   }

   private static boolean containsText(String text, NodeList textNodes, int suggestedIndex)
   {
      text = text.trim();
      if(suggestedIndex >= 0)
      {
         Text textNode = (Text)textNodes.item(suggestedIndex);
         String wasText = textNode.getNodeValue().trim();
         if(text.equals(wasText))
         {
            return true;
         }
      }

      for(int i = 0; i < textNodes.getLength(); ++i)
      {
         Text textNode = (Text)textNodes.item(i);
         String wasText = textNode.getNodeValue().trim();
         if(text.equals(wasText))
         {
            return true;
         }
      }
      return false;
   }

   private static NodeList getTextNodes(NodeList list)
   {
      MutableNodeList result = new MutableNodeList();
      for(int i = 0; i < list.getLength(); ++i)
      {
         Node node = list.item(i);
         if(node.getNodeType() == Node.TEXT_NODE)
         {
            String text = node.getNodeValue();
            if(text.trim().length() > 0)
            {
               result.add(node);
            }
         }
      }
      return result;
   }

   private static NodeList sublist(NodeList list, short nodeType)
   {
      MutableNodeList result = new MutableNodeList();
      for(int i = 0; i < list.getLength(); ++i)
      {
         Node node = list.item(i);
         if(node.getNodeType() == nodeType)
         {
            result.add(node);
         }
      }
      return result;
   }

   private static String assertAttrs(NamedNodeMap attrsExp,
                                     NamedNodeMap attrsWas,
                                     Element printAsExp)
   {
      String result = assertSubset(attrsExp, attrsWas, printAsExp, true);
      if(result == null)
      {
         result = assertSubset(attrsWas, attrsExp, printAsExp, false);
      }
      return result;
   }

   private static String assertSubset(NamedNodeMap attrsSubset,
                                      NamedNodeMap attrsSet,
                                      Element printAsExp,
                                      boolean checkHave)
   {
      String msg = checkHave ? " has attribute " : " doesn't have attribute ";
      QName expName = new QName(printAsExp.getNamespaceURI(), printAsExp.getLocalName());
      for(int i = 0; i < attrsSubset.getLength(); ++i)
      {
         Attr attr = (Attr)attrsSubset.item(i);
         String attrNs = attr.getNamespaceURI();
         String localName = attr.getLocalName();
         if(xsiNs(attrNs) && "type".equals(localName))
         {
            Attr wasAttr = (Attr)attrsSet.getNamedItemNS(attrNs, localName);
            if(wasAttr == null)
            {
               return "Element " + expName + msg + new QName(attrNs, localName);
            }

            String typeName = attr.getValue();
            int colon = typeName.indexOf(':');
            if(colon != -1)
            {
               typeName = typeName.substring(colon);
            }

            if(!wasAttr.getValue().endsWith(typeName))
            {
               return "Element " + expName +
                  (checkHave ? " has xsi:type " : " doesn't have xsi:type ") +
                  attr.getValue();
            }

            //todo compare namespaces for xsi:types
         }
         else if(nonIgnorableNs(attrNs) || xsiNs(attrNs) && localName.equals("nil"))
         {
            Attr wasAttr = (Attr)attrsSet.getNamedItemNS(attrNs, localName);
            if(wasAttr == null)
            {
               return "Element " + expName + msg + new QName(attrNs, localName);
            }

            if(!attr.getValue().equals(wasAttr.getValue()))
            {
               return "Attribute " +
                  new QName(attrNs, localName) +
                  " in element " +
                  expName +
                  " has value " + attr.getValue();
            }
         }
      }      
      return null;
   }
   
   private static boolean hasNonIgnorableNs(NamedNodeMap nodeMap)
   {
      for(int i = 0; i < nodeMap.getLength(); ++i)
      {
         Node node = nodeMap.item(i);
         if(nonIgnorableNs(node.getNamespaceURI()))
         {
            return true;
         }
      }
      return false;
   }

   private static boolean nonIgnorableNs(String ns)
   {
      return ns == null ||
         !ns.equals(Constants.NS_XML_SCHEMA)
         && !ns.equals(Constants.NS_XML_SCHEMA_INSTANCE)
         && !ns.equals(Constants.NS_XML_XMLNS);
   }

   private static boolean xsiNs(String ns)
   {
      return Constants.NS_XML_SCHEMA_INSTANCE.equals(ns);
   }

   private String fail(String msg, Element exp, Element was)
   {
      return msg + ". Expected\n" + toString(exp) + "\nbut was\n" + toString(was);
   }

   private String toString(Element e)
   {
      return append(e, new StringBuffer(), 0).toString();
   }

   private StringBuffer append(Element e, StringBuffer buf, int depth)
   {
      if(reformat && depth > 0)
      {
         buf.append('\n');
         for(int i = 0; i < depth; ++i)
         {
            buf.append(INDENT);
         }
      }

      buf.append('<');
      if(e.getPrefix() != null && e.getPrefix().length() > 0)
      {
         buf.append(e.getPrefix()).append(':');
      }
      buf.append(e.getLocalName());

      NamedNodeMap attrs = e.getAttributes();
      if(attrs != null && attrs.getLength() > 0)
      {
         for(int i = 0; i < attrs.getLength(); ++i)
         {
            Attr attr = (Attr)attrs.item(i);
            buf.append(' ')
               .append(attr.getName())
               .append('=')
               .append('\'')
               .append(attr.getValue())
               .append('\'');
         }
      }

      buf.append('>');

      NodeList childNodes = e.getChildNodes();
      boolean childElements = false;
      for(int i = 0; i < childNodes.getLength(); ++i)
      {
         Node child = childNodes.item(i);
         switch(child.getNodeType())
         {
            case Node.TEXT_NODE:
               String chars = child.getNodeValue();
               if(chars.trim().length() > 0)
               {
                  buf.append(chars);
               }
               break;
            case Node.ELEMENT_NODE:
               append((Element)child, buf, depth + 1);
               childElements = true;
               break;
            default:
               throw new IllegalStateException("Node type is not supported: " + child.getNodeType());
         }
      }

      if(reformat && childElements)
      {
         buf.append('\n');
         for(int i = 0; i < depth; ++i)
         {
            buf.append(INDENT);
         }
      }

      buf.append("</");
      if(e.getPrefix() != null && e.getPrefix().length() > 0)
      {
         buf.append(e.getPrefix()).append(':');
      }
      buf.append(e.getLocalName())
         .append('>');

      return buf;
   }

   // Inner

   private static final class MutableNodeList
      implements NodeList
   {
      private List list = Collections.EMPTY_LIST;

      public void add(Node node)
      {
         switch(list.size())
         {
            case 0:
               list = Collections.singletonList(node);
               break;
            case 1:
               list = new ArrayList(list);
            default:
               list.add(node);
         }
      }

      public int getLength()
      {
         return list.size();
      }

      public Node item(int index)
      {
         return (Node)list.get(index);
      }
   }

   public static final class DefErrorHandler
      implements ErrorHandler
   {
      public static final byte IGNORE = 0;
      public static final byte LOG = 1;
      public static final byte FAIL = 3;

      private byte warnEvent = IGNORE;
      private byte errorEvent = IGNORE;
      private byte fatalEvent = FAIL;

      public void error(SAXParseException e) throws SAXException
      {
         handleEvent(warnEvent, e);
      }

      public void fatalError(SAXParseException e) throws SAXException
      {
         handleEvent(errorEvent, e);
      }

      public void warning(SAXParseException e) throws SAXException
      {
         handleEvent(fatalEvent, e);
      }

      private void handleEvent(byte event, SAXParseException e)
         throws SAXException
      {
         switch(event)
         {
            case IGNORE:
               break;
            case LOG:
               System.out.println(formatMessage(e));
               break;
            case FAIL:
               String msg = formatMessage(e);
               throw new SAXException(msg);
         }
      }
   }

   private static String formatMessage(SAXParseException exception)
   {
      StringBuffer buffer = new StringBuffer(50);
      buffer.append(exception.getMessage()).append(" @ ");
      String location = exception.getPublicId();
      if(location != null)
      {
         buffer.append(location);
      }
      else
      {
         location = exception.getSystemId();
         if(location != null)
         {
            buffer.append(location);
         }
         else
         {
            buffer.append("*unknown*");
         }
      }
      buffer.append('[');
      buffer.append(exception.getLineNumber()).append(',');
      buffer.append(exception.getColumnNumber()).append(']');
      return buffer.toString();
   }
}
