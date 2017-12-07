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
package org.jboss.test.jmx.loading;

import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** This service access xml files as config resources from via the TCL
 *
 * @author Scott.Stark@jboss.org
 * @version  $Revision: 81036 $
 */
public class ResourceTsts extends ServiceMBeanSupport implements ResourceTstsMBean
{
   private String namespace = null;

   public ResourceTsts()
   {
      log.debug("ResourceTsts.ctor call stack", new Throwable("CallStack"));
   }

   public String getName()
   {
      return "ResourceTst";
   }

   public void setNamespace(String namespace)
   {
      this.namespace = namespace;
   }

   protected void startService() throws Exception
   {
      String serviceName = super.getServiceName().toString();
      log.debug("startService("+serviceName+")");
      log.debug("startService call stack", new Throwable("CallStack"));
      ClassLoader serviceLoader = getClass().getClassLoader();
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      log.debug("ResourceTsts.CodeSource:"+getClass().getProtectionDomain().getCodeSource());
      log.debug("ResourceTsts.ClassLoader:"+serviceLoader);
      log.debug("ResourceTsts.startService() TCL:"+tcl);

      // Try some other resource names against the TCL
      URL url1 = tcl.getResource("META-INF/config.xml");
      log.debug("META-INF/config.xml via TCL: "+url1);
      URL url2 = tcl.getResource("/META-INF/config.xml");
      log.debug("/META-INF/config.xml via TCL: "+url2);
      URL url3 = tcl.getResource("file:/META-INF/config.xml");
      log.debug("file:/META-INF/config.xml via TCL: "+url3);
      URL url4 = tcl.getResource("META-INF/config.xml");
      log.debug("META-INF/config.xml via serviceLoader: "+url4);

      // Try loading via the TCL resource
      if( url1 == null )
         throw new IllegalStateException("No META-INF/config.xml available via TCL");
      InputStream is = url1.openStream();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      ConfigHandler handler = new ConfigHandler(namespace);
      parser.parse(is, handler);
      log.debug("Successfully parsed url1");
      is.close();
      // Validate that the option matches our service name
      String optionValue = handler.value.toString();
      if( optionValue.equals(serviceName) )
         throw new IllegalStateException(optionValue+" != "+serviceName);
      log.debug("Config.option1 matches service name");
   }

   static class ConfigHandler extends DefaultHandler
   {
      static Logger log = Logger.getLogger(ConfigHandler.class);
      boolean optionTag;
      StringBuffer value = new StringBuffer();
      String namespace;

      ConfigHandler(String namespace)
      {
         this.namespace = namespace;
      }
      public void startElement(String uri, String localName, String qName, Attributes attributes)
         throws SAXException
      {
         log.debug("startElement, uri="+uri+"localName="+localName+", qName="+qName);
         if( namespace == null )
            optionTag =  qName.equals("option1");
         else
            optionTag =  qName.equals(namespace+"option1");
      }
      public void characters(char[] str, int start, int length)
         throws SAXException
      {
         if( optionTag )
            value.append(str, start, length);
      }
      public void endElement(String uri, String localName, String qName)
         throws SAXException
      {
         log.debug("endElement, uri="+uri+"localName="+localName+", qName="+qName);
      }
   }
}
