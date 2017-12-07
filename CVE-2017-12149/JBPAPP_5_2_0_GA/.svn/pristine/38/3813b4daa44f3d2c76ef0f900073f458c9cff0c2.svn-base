/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.web.deployers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.JSFDeployment;
import org.jboss.logging.Logger;
import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Use the {@link #parse(org.jboss.deployers.structure.spi.DeploymentUnit, java.net.URL, org.jboss.deployment.JSFDeployment)}
 * method to parse a JSF faces configuration file
 * <p/>
 * Note that this class and the parse method just look for the presence of JSF managed beans and on finding such managed beans,
 * update the {@link JSFDeployment}, passed to the parse method, with the managed bean class name.
 * This class should <b>not</b> be used as a full fledged faces config file parser. The real and complete parsing of the
 * faces configuration files is left to the JSF implementation provider.
 *
 * @author Jaikiran Pai
 * @see https://issues.jboss.org/browse/JBAS-8318
 */
public class FacesConfigParsingUtil
{
   /**
    * Logger
    */
   private static final Logger logger = Logger.getLogger(FacesConfigParsingUtil.class);

   /**
    * Entity resolver for JSF configuration files
    */
   private static final JBossEntityResolver jBossJSFEntityResolver = new JBossEntityResolver();

   static
   {
      // JSF 1.0
      jBossJSFEntityResolver.registerEntity("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN", "web-facesconfig_1_0.dtd");
      jBossJSFEntityResolver.registerEntity("http://java.sun.com/dtd/web-facesconfig_1_0.dtd", "web-facesconfig_1_0.dtd");
      // JSF 1.1
      jBossJSFEntityResolver.registerEntity("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN", "web-facesconfig_1_1.dtd");
      jBossJSFEntityResolver.registerEntity("http://java.sun.com/dtd/web-facesconfig_1_1.dtd", "web-facesconfig_1_1.dtd");
      // JSF 1.2
      jBossJSFEntityResolver.registerEntity("http://java.sun.com/xml/ns/j2ee/web-facesconfig_1_2.xsd", "web-facesconfig_1_2.xsd");

   }

   /**
    * Parses the faces configuration file represented by the <code>facesConfigXmlURL</code> and checks for the presence of
    * any JSF managed beans. On finding any managed beans, this method updates the passed <code>jsfDeployment</code> with the
    * managed bean class name.
    *
    * @param unit              The deployment unit
    * @param facesConfigXmlURL The faces config file URL
    * @param jsfDeployment     The JSFDeployment which will be updated with any managed bean class names, that might be found in the
    *                          faces config file.
    * @throws Exception
    */
   public static void parse(final DeploymentUnit unit, final URL facesConfigXmlURL, final JSFDeployment jsfDeployment) throws Exception
   {
      logger.debug("Checking for the presence of JSF managed-bean(s) in JSF config file: " + facesConfigXmlURL + " in deployment unit: " + unit);
      // get the parser factory
      SAXParserFactory parserFactory = getParserFactory();
      // create a parser
      SAXParser saxParser = parserFactory.newSAXParser();
      InputStream inputStream = null;
      try
      {
         // get the input stream and the input source for the faces config file
         inputStream = getInputStream(facesConfigXmlURL);
         InputSource inputSource = new InputSource(getInputStream(facesConfigXmlURL));
         inputSource.setSystemId(facesConfigXmlURL.toExternalForm());

         // parse it!
         saxParser.parse(inputSource, new DefaultHandler()
         {
            /**
             * Flag to keep track of managed-bean-class element being processed
             */
            private boolean managedBeanClassElementProcessingInProgress;

            /**
             * Uses the {@link JBossEntityResolver} to resolve the entity. If it cannot be resolved by the {@link JBossEntityResolver}
             * then this method lets the {@link DefaultHandler} to resolve it.
             *
             * @param publicId
             * @param systemId
             * @return
             * @throws IOException
             * @throws SAXException
             */
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
            {
               // try resolving it with the JBossEntityResolver
               InputSource source = jBossJSFEntityResolver.resolveEntity(publicId, systemId);
               if (source != null)
               {
                  return source;
               }
               // we couldn't resolve, so let the default handler try to resolve it
               return super.resolveEntity(publicId, systemId);
            }


            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
            {
               // we are only interested in managed-bean-class element.
               if (localName.equals("managed-bean-class"))
               {
                  this.managedBeanClassElementProcessingInProgress = true;
               }
               // let the super do its job
               super.startElement(uri, localName, qName, attributes);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException
            {
               // reset the flag when the managed-bean-class element ends
               if (localName.equals("managed-bean-class"))
               {
                  this.managedBeanClassElementProcessingInProgress = false;
               }
               // let super do its job
               super.endElement(uri, localName, qName);
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException
            {
               // if we are currently processing the managed-bean-class element, then fetch the managed bean
               // class name text
               if (this.managedBeanClassElementProcessingInProgress)
               {
                  // get the managed bean class name
                  String managedBeanClassName = new String(ch, start, length);
                  if (!managedBeanClassName.trim().isEmpty())
                  {
                     logger.debug("Found JSF managed bean class: " + managedBeanClassName + " in unit " + unit);
                     // add it to the jsf deployment
                     jsfDeployment.addManagedBean(managedBeanClassName);
                  }
               }
               // let super do its job now
               super.characters(ch, start, length);
            }
         });
      }
      finally
      {
         if (inputStream != null)
         {
            inputStream.close();
         }
      }
      return;

   }

   /**
    * Returns a instance of {@link SAXParserFactory}
    *
    * @return
    */
   private static SAXParserFactory getParserFactory()
   {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      return factory;
   }

   /**
    * Returns the {@link InputStream} for the passed <code>url</code>
    *
    * @param url
    * @return
    * @throws IOException
    */
   private static InputStream getInputStream(URL url) throws IOException
   {
      URLConnection conn = url.openConnection();
      conn.setUseCaches(false);
      return new BufferedInputStream(conn.getInputStream());

   }

}
