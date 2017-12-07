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
package org.jboss.varia.deployment.convertor;

import java.net.URL;
import java.util.Properties;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 * XslTransformer is a utility class for XSL transformations.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 */
public class XslTransformer
{
   // Attributes --------------------------------------------------------
   /**
     The system property that determines which Factory implementation
     to create is named "javax.xml.transform.TransformerFactory".
     If the property is not defined, a platform default is used.
     An implementation of the TransformerFactory class is NOT guaranteed
     to be thread safe.
   */
   private static TransformerFactory transformerFactory =
      TransformerFactory.newInstance();

   // Public static methods ---------------------------------------------
   /**
     Applies transformation.
     Pre-compiled stylesheet should be used in a thread-safe manner grabbing
     a new transformer before completing the transformation.
    */
   public static synchronized void applyTransformation(InputStream srcIs,
                                                       OutputStream destOs,
                                                       InputStream templateIs,
                                                       Properties outputProps)
   throws TransformerException, IOException
   {
      StreamSource source = new StreamSource(srcIs);
      StreamResult result = new StreamResult(destOs);
      StreamSource template = new StreamSource(templateIs);

      /*
        Pre-compile the stylesheet. This Templates object may be used
        concurrently across multiple threads. Creating a Templates object
        allows the TransformerFactory to do detailed performance optimization
        of transformation instructions, without penalizing runtime
        transformation.
      */
      Templates templates = transformerFactory.newTemplates( template );

      Transformer transformer = templates.newTransformer();
      if( outputProps != null )
      {
         transformer.setOutputProperties( outputProps );
      }

      transformer.transform( source, result );
   }


   /**
    * Applies template <code>templateIs</code> to xml source
    * <code>srcIs</code> with output properties <code>outputProps</code>
    * and parameters <code>xslParams</code>.
    * The resulting xml is written to <code>destOs</code>
    */
   public static synchronized void applyTransformation(InputStream srcIs,
                                                       OutputStream destOs,
                                                       InputStream templateIs,
                                                       Properties outputProps,
                                                       Properties xslParams)
   throws TransformerException, IOException
   {
      StreamSource source = new StreamSource( srcIs );
      StreamResult result = new StreamResult( destOs );
      StreamSource template = new StreamSource( templateIs );

      Templates templates = transformerFactory.newTemplates( template );

      // set output properties
      Transformer transformer = templates.newTransformer();
      if(outputProps != null)
      {
         transformer.setOutputProperties(outputProps);
      }

      // set xsl parameters
      if(xslParams != null)
      {
         // note, xslParams.keys() will not work properly,
         // because it will not return the keys for default properties.
         Enumeration keys = xslParams.propertyNames();
         while( keys.hasMoreElements() )
         {
            String key = (String)keys.nextElement();
            transformer.setParameter(key, xslParams.getProperty(key));
         }
      }

      transformer.transform( source, result );
   }
}
