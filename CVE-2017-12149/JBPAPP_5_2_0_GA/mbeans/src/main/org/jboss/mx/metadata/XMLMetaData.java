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
package org.jboss.mx.metadata;

// $Id: XMLMetaData.java 81026 2008-11-14 12:51:05Z dimitris@jboss.org $

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.mx.modelmbean.XMBeanConstants;
import org.jboss.mx.service.ServiceConstants;
import org.jboss.mx.util.JBossNotCompliantMBeanException;
import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.SAXException;

/**
 * Aggregate builder for XML schemas. This builder implementation is used
 * as an aggregate for all XML based builder implementations. The correct
 * XML parser is picked based on the schema declaration of the XML file.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @author Matt Munz
 */
public class XMLMetaData
   extends AbstractBuilder
   implements ServiceConstants, XMBeanConstants
{

   // Attributes ----------------------------------------------------
   private static final int NO_VERSION = -1;
   private static final int JBOSS_XMBEAN_1_0 = 0;
   private static final int JBOSS_XMBEAN_1_1 = 1;
   private static final int JBOSS_XMBEAN_1_2 = 2;

   /**
    * The URL for the XML file.
    */
   private URL url                  = null;

   private Element element;

   private String versionString;

   /**
    * The class name of the resource class this Model MBean represents.
    */
   private String resourceClassName = null;

   /**
    * The class name of the Model MBean implementation class.
    */
   private String mmbClassName      = null;


   // Constructors --------------------------------------------------

   /**
    * Constructs an aggregate XML builder implementation.
    *
    * @param   mmbClassName         the class name of the Model MBean
    *                               implementation
    * @param   resourceClassName    the class name of the resource object the
    *                               Model MBean represents
    * @param   url                  the URL for the XML definition of the
    *                               management interface
    */
   public XMLMetaData(String mmbClassName, String resourceClassName, URL url)
   {
      super();

      this.url               = url;
      this.mmbClassName      = mmbClassName;
      this.resourceClassName = resourceClassName;
   }

   /**
    * Constructs an aggregate XML builder implementation.
    *
    * @param   mmbClassName         the class name of the Model MBean
    *                               implementation
    * @param   resourceClassName    the class name of the resource object the
    *                               Model MBean represents
    * @param   url                  the URL for the XML definition of the
    *                               management interface
    *
    * @throws  MalformedURLException if the URL string could not be resolved
    */
   public XMLMetaData(String mmbClassName, String resourceClassName, String url)
      throws MalformedURLException
   {
      this(mmbClassName, resourceClassName, new URL(url));
   }

   /**
    * Constructs an aggregate XML builder implementation.
    *
    * @param   mmbClassName         the class name of the Model MBean
    *                               implementation
    * @param   resourceClassName    the class name of the resource object the
    *                               Model MBean represents
    * @param   url                  the URL for the XML definition of the
    *                               management interface
    * @param   properties           Map of configuration properties for this
    *                               builder. These properties will be passed
    *                               to the appropriate XML schema specific builder
    *                               when it is created.
    */
   public XMLMetaData(String mmbClassName, String resourceClassName, URL url, Map properties)
   {
      this(mmbClassName, resourceClassName, url);
      setProperties(properties);
   }

   /**
    * Constructs an aggregate XML builder implementation.
    *
    * @param   mmbClassName         the class name of the Model MBean
    *                               implementation
    * @param   resourceClassName    the class name of the resource object the
    *                               Model MBean represents
    * @param   url                  the URL for the XML definition of the
    *                               management interface
    * @param   properties           Map of configuration properties for this
    *                               builder. These properties will be passed
    *                               to the appropriate XML schema specific builder
    *                               when it is created.
    *
    * @throws  MalformedURLException if the URL string could not be resolved
    */
   public XMLMetaData(String mmbClassName, String resourceClassName,
                      String url, Map properties) throws MalformedURLException
   {
      this(mmbClassName, resourceClassName, new URL(url), properties);
   }

   /**
    * Creates a new <code>XMLMetaData</code> instance using an explicit DOM element
    * as the configuration source, and requiring an explicit version indicator.
    * The version should be the PublicID for the dtd or (worse) the dtd url.
    *
    * @param mmbClassName a <code>String</code> value
    * @param resourceClassName a <code>String</code> value
    * @param element an <code>org.w3c.dom.Element</code> value
    * @param version a <code>String</code> value
    */
   public XMLMetaData(String mmbClassName, String resourceClassName, Element element, String version)
   {
      super();
      this.mmbClassName = mmbClassName;
      this.resourceClassName = resourceClassName;
      this.element = element;
      versionString = version;
   }


   // MetaDataBuilder implementation --------------------------------
   /**
    * Constructs the Model MBean metadata. This implementation reads the
    * document type definition from the beginning of the XML file and picks
    * a corresponding XML builder based on the schema name. In case no
    * document type is defined the latest schema builder for this JBossMX
    * release is used. <p>
    *
    * The SAX parser implementation is selected by default based on JAXP
    * configuration. If you want to use JAXP to select the parser, you can
    * set the system property <tt>"javax.xml.parsers.SAXParserFactory"</tt>.
    * For example, to use Xerces you might define:   <br><pre>
    *
    *    java -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl ...
    *
    * </pre>
    *
    * In case you can't or don't want to use JAXP to configure the SAX parser
    * implementation you can override the SAX parser implementation by setting
    * an MBean descriptor field {@link XMBeanConstants#SAX_PARSER} to the
    * parser class string value.
    *
    * @return initialized MBean info
    * @throws NotCompliantMBeanException if there were errors building the
    *         MBean info from the given XML file.
    */
   public MBeanInfo build() throws NotCompliantMBeanException
   {
      try
      {
         int version = NO_VERSION;

         if (versionString == null)
         {
            // by default, let JAXP pick the SAX parser
            SAXReader reader = new SAXReader();

            // check if user wants to override the SAX parser property
            if (properties.get(SAX_PARSER) != null)
            {
               try
               {
                  reader.setXMLReaderClassName(getStringProperty(SAX_PARSER));
               }

               catch (SAXException e)
               {
                  //Should log and ignore, I guess
               } // end of try-catch
            }
            // by default we validate
            reader.setValidation(true);

            // the user can override the validation by setting the VALIDATE property
            try
            {
               boolean validate = getBooleanProperty(XML_VALIDATION);
               reader.setValidation(validate);
            }
            catch (IllegalPropertyException e)
            {
               // FIXME: log the exception (warning)

               // fall through, use the default value
            }

            //supply it with our dtd locally.
            reader.setEntityResolver(new JBossEntityResolver());

            // get the element and start parsing...
            Document doc = reader.read(url);
            element = doc.getRootElement();
            DocumentType type = doc.getDocType();

            version = validateVersionString(type.getPublicID());
            if (version == NO_VERSION)
            {
               version = validateVersionString(type.getSystemID());
            } // end of if ()

         }
         else
         {
            version = validateVersionString(versionString);
         } // end of else

         if (element == null)
         {
            throw new IllegalStateException("No element supplied with explict version!");
         }
         // These are the known schemas for us. Pick the correct one based on
         // schema or default to the latest.docURL.endsWith(JBOSSMX_XMBEAN_DTD_1_0)
         if (version == JBOSS_XMBEAN_1_0 ||
             version == JBOSS_XMBEAN_1_1 ||
             version == JBOSS_XMBEAN_1_2)
         {
            // jboss_xmbean_1_0.dtd is the only implemented useful xmbean
            return new JBossXMBean10(mmbClassName, resourceClassName, element, properties).build();
         }
         else
         {
            throw new NotCompliantMBeanException("Unknown xmbean type " + versionString);
         } // end of else

      }
      catch (DocumentException e)
      {
         throw new JBossNotCompliantMBeanException("Error parsing the XML file, from XMLMetaData: ", e);
      }
   }

   private int validateVersionString(String versionString)
   {
      if (PUBLIC_JBOSSMX_XMBEAN_DTD_1_0.equals(versionString))
      {
         return JBOSS_XMBEAN_1_0;
      } // end of if ()
      if (versionString != null && versionString.endsWith(JBOSSMX_XMBEAN_DTD_1_0))
      {
         return JBOSS_XMBEAN_1_0;
      } // end of if ()
      if (PUBLIC_JBOSSMX_XMBEAN_DTD_1_1.equals(versionString))
      {
         return JBOSS_XMBEAN_1_1;
      } // end of if ()
      if (versionString != null && versionString.endsWith(JBOSSMX_XMBEAN_DTD_1_1))
      {
         return JBOSS_XMBEAN_1_1;
      } // end of if ()
      if (PUBLIC_JBOSSMX_XMBEAN_DTD_1_2.equals(versionString))
      {
         return JBOSS_XMBEAN_1_2;
      } // end of if ()
      if (versionString != null && versionString.endsWith(JBOSSMX_XMBEAN_DTD_1_2))
      {
         return JBOSS_XMBEAN_1_2;
      } // end of if ()

      //There is nothing defined for jboss xmbean 1.2, so we can't recognize it.
      return NO_VERSION;
   }

}
