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
package org.jboss.test.util.test;

import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.w3c.dom.Element;

/**
* Test the DOMWriter
*
* @author Thomas.Diesler@jboss.org
* @author Dimitris.Andreadis@jboss.org
* @since 22-Jun-2005
*/
public class DOMWriterUnitTestCase extends JBossTestCase
{
   public DOMWriterUnitTestCase(String name)
   {
      super(name);
   }
   
   /**
    * Test printing
    */
   public void testPrint() throws Exception
   {
      String envStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "<env:Header/>" +
         "<env:Body>" +
         "<ns1:sendMimeImageGIF xmlns:ns1='http://org.jboss.ws/attachment'>" +
         "<message>Some text message</message>" +
         "</ns1:sendMimeImageGIF>" +
         "</env:Body>" +
         "</env:Envelope>";
      
      String expStr = envStr; 
      
      Element env = DOMUtils.parse(envStr);
      
      String wasStr = DOMWriter.printNode(env, false);
      assertEquals(expStr, wasStr);
   }
   
  /**
   * Test pretty printing
   */
   public void testPrettyPrint() throws Exception
   {
      String envStr = 
        "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
        "<env:Header/>" +
        "<env:Body>" +
        "<ns1:sendMimeImageGIF xmlns:ns1='http://org.jboss.ws/attachment'>" +
        "<message>Some text message</message>" +
        "</ns1:sendMimeImageGIF>" +
        "</env:Body>" +
        "</env:Envelope>";
     
      String expStr = 
        "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>\n" +
        " <env:Header/>\n" + 
        " <env:Body>\n" +
        "  <ns1:sendMimeImageGIF xmlns:ns1='http://org.jboss.ws/attachment'>\n" +
        "   <message>Some text message</message>\n" +
        "  </ns1:sendMimeImageGIF>\n" +
        " </env:Body>\n" +
        "</env:Envelope>";
     
      Element env = DOMUtils.parse(envStr);
     
      String wasStr = DOMWriter.printNode(env, true);
      assertEquals(expStr, wasStr);
   }
  
   /**
    * Test pretty comment printing
    */
   public void testPrettyComment() throws Exception
   {
      String envStr = 
        "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
        "<env:Header/>" +
        "<env:Body>" +
        "<ns1:sendMimeImageGIF xmlns:ns1='http://org.jboss.ws/attachment'>" +
        "<!-- This is some comment -->" +
        "<message>Some text message</message>" +
        "</ns1:sendMimeImageGIF>" +
        "</env:Body>" +
        "</env:Envelope>";
     
      String expStr = 
        "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>\n" +
        " <env:Header/>\n" + 
        " <env:Body>\n" +
        "  <ns1:sendMimeImageGIF xmlns:ns1='http://org.jboss.ws/attachment'>\n" +
        "   <!-- This is some comment -->\n" +
        "   <message>Some text message</message>\n" +
        "  </ns1:sendMimeImageGIF>\n" +
        " </env:Body>\n" +
        "</env:Envelope>";
     
      Element env = DOMUtils.parse(envStr);
     
      String wasStr = DOMWriter.printNode(env, true);
      assertEquals(expStr, wasStr);
   }
}
