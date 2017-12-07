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
package org.jboss.test.stax;

import org.jboss.test.JBossTestCase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;


/**
 * Test the StAX implementation
 *
 * @author jason.greene@jboss.com
 */
public class StaxTestCase extends JBossTestCase
{
   /**
    * Construct the test case with a given name
    */
   public StaxTestCase(String name)
   {
      super(name);
   }

   private static String getXmlUrl(String name)
   {
      return getTCLResource(name).getFile();
   }

   private static URL getTCLResource(String name)
   {
      URL xmlUrl = Thread.currentThread().getContextClassLoader().getResource(name);
      if(xmlUrl == null)
      {
         throw new IllegalStateException(name + " not found");
      }
      return xmlUrl;
   }

   public void testStreamReader() throws Exception
   {
      FileInputStream stream = new FileInputStream(getXmlUrl("stax/test.xml"));
      XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
      reader.next();
   }

   public void testEventReader() throws Exception
   {
      FileInputStream stream = new FileInputStream(getXmlUrl("stax/test.xml"));
      XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(stream);
      XMLEvent event = reader.nextEvent();
      assertTrue("Expected document start, got:" + event.getClass(), event instanceof StartDocument);
      event = reader.nextEvent();
      assertTrue("Expected characters, got:" + event.getClass(), event instanceof Characters);
   }

   public void testStreamWriter() throws Exception
   {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
      writer.writeStartDocument();
      writer.writeComment("hello");
      writer.writeStartElement("root");
      writer.writeAttribute("some-attribute", "10");
      writer.close();
      assertEquals("<?xml version='1.0' encoding='UTF-8'?><!--hello--><root some-attribute=\"10\"></root>", stream.toString());
   }

   public void testEventWriter() throws Exception
   {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(stream);
      XMLEventFactory factory = XMLEventFactory.newInstance();
      Attribute att = factory.createAttribute("some-attribute", "10");
      ArrayList list = new ArrayList();
      list.add(att);
      writer.add(factory.createStartDocument());
      writer.add(factory.createComment("hello"));
      writer.add(factory.createStartElement(new QName("root"), list.iterator(), null));
      writer.close();
      assertEquals("<?xml version='1.0' encoding='UTF-8'?><!--hello--><root some-attribute=\"10\"></root>", stream.toString());
   }
}
