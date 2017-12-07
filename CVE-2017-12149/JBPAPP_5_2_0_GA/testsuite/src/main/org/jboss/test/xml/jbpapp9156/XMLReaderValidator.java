/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.xml.jbpapp9156;

import java.io.IOException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.jboss.logging.Logger;

public class XMLReaderValidator
{
   private Logger log = null;
   
   public XMLReaderValidator(Logger log)
   {
      this.log = log;
   }
   
   public boolean parse(String xmlFileLocation)
   {
      String parserClass = "org.apache.xerces.parsers.SAXParser";
      String validationFeature = "http://xml.org/sax/features/validation";
      String schemaFeature = "http://apache.org/xml/features/validation/schema";
      try
      {
         String x = xmlFileLocation;
         XMLReader r = XMLReaderFactory.createXMLReader(parserClass);
         r.setFeature(validationFeature, true);
         r.setFeature(schemaFeature, true);
         r.setErrorHandler(new MyErrorHandler(log));
         r.parse(x);
         return true;
      }
      catch (SAXException e)
      {
         log.debug(e);
         return false;
      }
      catch (IOException e)
      {
         log.debug(e);
         return false;
      }
      catch (Exception e)
      {
         log.debug(e);
         return false;
      }
   }

   private static class MyErrorHandler extends DefaultHandler
   {
      private Logger log = null;
      
      public MyErrorHandler(Logger log)
      {
         this.log = log;
      }
      
      public void warning(SAXParseException e) throws SAXException
      {
         log.warn(e);
         log.warn(printInfo(e));
      }

      public void error(SAXParseException e) throws SAXException
      {
         log.error("Error: " + printInfo(e));
      }

      public void fatalError(SAXParseException e) throws SAXException
      {
         log.error("Fattal error: " + printInfo(e));
      }

      private String printInfo(SAXParseException e)
      {
         StringBuilder sb = new StringBuilder();
         sb.append(" Public ID: " + e.getPublicId());
         sb.append(" System ID: " + e.getSystemId());
         sb.append(" Line number: " + e.getLineNumber());
         sb.append(" Column number: " + e.getColumnNumber());
         sb.append(" Message: " + e.getMessage());
         return sb.toString();
      }
   }
}
