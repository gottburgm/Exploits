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
package org.jboss.test.system.metadata.test;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * MetaDataTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MetaDataTestDelegate extends AbstractTestDelegate
{
   /** The parser */
   private DocumentBuilder parser;
   
   public MetaDataTestDelegate(Class<?> clazz)
   {
      super(clazz);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
   }

   /**
    * Unmarshal an object
    * 
    * @param url the url
    * @return the list of services
    * @throws Exception for any error
    */
   public List<ServiceMetaData> unmarshal(URL url) throws Exception
   {
      long start = System.currentTimeMillis();

      Element element = null;

      try
      {
         InputStream stream = url.openStream();
         try
         {
            InputSource is = new InputSource(stream);
            is.setSystemId(url.toString());
            parser.setEntityResolver(new JBossEntityResolver());
            log.debug("Initialized parsing in " + (System.currentTimeMillis() - start) + "ms");

            Document document = parser.parse(is);
            element = document.getDocumentElement();
         }
         finally
         {
            stream.close();
         }

         ServiceMetaDataParser parser = new ServiceMetaDataParser(element);
         List<ServiceMetaData> result = parser.parse();
         log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");
         return result;
      }
      catch (Exception e)
      {
         log.debug("Error during parsing: " + url + ": " + e);
         throw e;
      }
   }
}
