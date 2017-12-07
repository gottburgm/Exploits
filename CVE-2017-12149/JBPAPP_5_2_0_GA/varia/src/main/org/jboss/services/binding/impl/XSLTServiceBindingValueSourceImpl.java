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

package org.jboss.services.binding.impl;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.services.binding.ElementServiceBindingValueSource;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingValueSource;
import org.jboss.services.binding.URLServiceBindingValueSource;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Element;

/**
 * A {@link ServiceBindingValueSource} implementation that uses
 * XSLT to perform any needed transformations.
 * 
 * 
 * @author Brian Stansberry
 * @version $Revision: 113110 $
 */
public class XSLTServiceBindingValueSourceImpl implements URLServiceBindingValueSource, ElementServiceBindingValueSource
{
   private static final Logger log = Logger.getLogger(XSLTServiceBindingValueSourceImpl.class);
   
   public String getResourceServiceBindingValue(ServiceBinding binding, final String input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      XSLTServiceBindingValueSourceConfig config = getConfig(binding);
      
      Reader reader = null;
      try
      {
         reader = AccessController.doPrivileged(new PrivilegedExceptionAction<Reader>()
         {
            public Reader run() throws IOException
            { 
               return Util.getInputStreamReader(input);
            }
         });
      }
      catch (Exception e)
      {
         throw new RuntimeException("Caught IOException during transformation", e);
      }
      
      return doFileTransform(input, reader, binding, config).getAbsolutePath();
   }

   public URL getURLServiceBindingValue(ServiceBinding binding, URL input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      XSLTServiceBindingValueSourceConfig config = getConfig(binding);
      
      Reader reader = null;
      try
      {
         reader = Util.getInputStreamReader(input);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Caught IOException during transformation", e);
      }
      
      try
      {
         return doFileTransform(input, reader, binding, config).toURL();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Unexpected exception creating URL from File", e);
      }
   }

   public Element getElementServiceBindingValue(ServiceBinding binding, Element input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      PropertyEditor editor = PropertyEditorFinder.getInstance().find(Element.class);
      if (editor == null)
         throw new IllegalStateException("Cannot find PropertyEditor for type Element");
      
      editor.setValue(input);      
      Reader reader = new StringReader(editor.getAsText());
      Writer writer = new StringWriter();
      
      doXslTransform(binding, getConfig(binding), reader, writer);
      
      editor.setAsText(writer.toString());
      return (Element) editor.getValue();
   }

   public Object getServiceBindingValue(ServiceBinding binding, Object... params)
   {
      if (params == null || params.length != 1)
      {
        throw new IllegalArgumentException(getClass().getSimpleName() + ".getServiceBindingValue() requires a single-value 'params'");
      }
      
      if (params[0] instanceof String)
      {   
         return getResourceServiceBindingValue(binding, (String) params[0]);
      }
      else if (params[0] instanceof Element)
      {   
         return getElementServiceBindingValue(binding, (Element) params[0]);
      }
      else if (params[0] instanceof URL)
      {   
         return getURLServiceBindingValue(binding, (URL) params[0]);
      }
      
      throw new IllegalArgumentException(getClass().getSimpleName() + ".getServiceBindingValue() requires a single-value 'params' of type String, Element or URL");
   }
   
   private File doFileTransform(Object input, Reader reader, ServiceBinding binding, 
         XSLTServiceBindingValueSourceConfig config)
   {
      Writer writer = null;
      File targetFile = null;
      try
      {
         targetFile = AccessController.doPrivileged(new PrivilegedExceptionAction<File>()
         {
            public File run() throws Exception
            {
               return Util.createTempFile();
            }
         });
         writer = new OutputStreamWriter(new FileOutputStream(targetFile));
         
         doXslTransform(binding, config, reader, writer);

         return targetFile;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Caught IOException during transformation", e);
      }
      finally
      {
         if (reader != null)
         {
            try
            {
               reader.close();
            }
            catch (IOException e)
            {
               log.warn("Failed closing Reader for " + input, e);
            }
         }
         if (writer != null)
         {
            try
            {
               writer.close();
            }
            catch (IOException e)
            {
               log.warn("Failed closing Writer to " + targetFile, e);
            }
         }
      }
   }
   
   private XSLTServiceBindingValueSourceConfig getConfig(ServiceBinding binding)
   {
      Object config = binding.getServiceBindingValueSourceConfig();
      if (config instanceof XSLTServiceBindingValueSourceConfig)
      {
         return (XSLTServiceBindingValueSourceConfig) config;
      }
      else if (config == null)
      {
         throw new IllegalStateException("No config object bound to " + binding);
      }
      throw new IllegalStateException("Incompatible config object of type " + 
                                       config.getClass() + " bound to " + binding +
                                       " -- must use " + XSLTServiceBindingValueSourceConfig.class.getName());
   }

   private void doXslTransform(ServiceBinding binding, XSLTServiceBindingValueSourceConfig config, 
         Reader reader, Writer writer)
   {
      Source xmlSource = new StreamSource(reader);
      Result xmlResult = new StreamResult(writer);         
      Source xslSource = new StreamSource(new StringReader(config.getXslt()));
      
      TransformerFactory factory = TransformerFactory.newInstance();
      try
      {
         Transformer transformer = factory.newTransformer(xslSource);
   
         transformer.setParameter("port", new Integer(binding.getPort()));
         String host = binding.getHostName();
         if (host != null)
         {
            transformer.setParameter("host", host);
         }
   
         // Check for any arbitrary attributes
         Map<String, String> attributes = config.getAdditionalAttributes();
         for(Map.Entry<String, String> entry : attributes.entrySet())
         {
            String attrValue = StringPropertyReplacer.replaceProperties(entry.getValue());
            transformer.setParameter(entry.getKey(), attrValue);
         }
   
         transformer.transform(xmlSource, xmlResult);
      }
      catch (TransformerException e)
      {
         throw new RuntimeException("Caught TransformerException during transformation", e);
      }
   }

}
