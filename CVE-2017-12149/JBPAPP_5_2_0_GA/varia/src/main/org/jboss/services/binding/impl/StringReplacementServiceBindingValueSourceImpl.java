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
import java.io.IOException;
import java.net.URL;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.services.binding.ElementServiceBindingValueSource;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.StringServiceBindingValueSource;
import org.jboss.services.binding.URLServiceBindingValueSource;
import org.w3c.dom.Element;

/**
 * A {@link ServiceBindingValueSource} implementation that uses
 * string replacement to perform any needed transformations.
 * 
 * @author Brian Stansberry
 * @version $Revision: 113110 $
 */
public class StringReplacementServiceBindingValueSourceImpl 
   implements StringServiceBindingValueSource, ElementServiceBindingValueSource, URLServiceBindingValueSource
{
   
   public String getStringServiceBindingValue(ServiceBinding binding, String input)
   {
      if (input == null)
      {
         return binding.getHostName();
      }
      
      StringReplacementServiceBindingValueSourceConfig config = getConfig(binding);
      return replaceHostAndPort(input, binding.getHostName(), binding.getPort(), config.getHostMarker(), config.getPortMarker());
   }  
      

   public Element getElementServiceBindingValue(ServiceBinding binding, Element input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      PropertyEditor editor = PropertyEditorFinder.getInstance().find(Element.class);
      if (editor == null)
         throw new IllegalStateException("Cannot find PropertyEditor for type Element");
      
      StringReplacementServiceBindingValueSourceConfig config = getConfig(binding);
      
      editor.setValue(input);
      String text = editor.getAsText();
      text = replaceHostAndPort(text, binding.getHostName(), binding.getPort(), config.getHostMarker(), config.getPortMarker());
      editor.setAsText(text);
      return (Element) editor.getValue();
   }

   public String getResourceServiceBindingValue(ServiceBinding binding, String input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      StringReplacementServiceBindingValueSourceConfig config = getConfig(binding);      
      
      try
      {
         String content = Util.getContentAsString(input);
         String transformed = replaceHostAndPort(content, binding.getHostName(), binding.getPort(), config.getHostMarker(), config.getPortMarker());
         return Util.writeToTempFile(transformed).getAbsolutePath();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Caught IOException during transformation", e);
      }
   }


   public URL getURLServiceBindingValue(ServiceBinding binding, URL input)
   {
      if (input == null)
         throw new IllegalArgumentException("input cannot be null");
      
      StringReplacementServiceBindingValueSourceConfig config = getConfig(binding);      
      
      try
      {
         String content = Util.getContentAsString(input);
         String transformed = replaceHostAndPort(content, binding.getHostName(), binding.getPort(), config.getHostMarker(), config.getPortMarker());
         return Util.writeToTempFile(transformed).toURL();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Caught IOException during transformation", e);
      }
   }

   public Object getServiceBindingValue(ServiceBinding binding, Object... params)
   {
      if (params == null)
      {
         return getStringServiceBindingValue(binding, null);
      }
      
      if (params.length != 1)
      {
        throw new IllegalArgumentException(getClass().getSimpleName() + ".getServiceBindingValue() requires a single-value 'params'");
      }
      
      if (params[0] instanceof String)
      {   
         return getStringServiceBindingValue(binding, (String) params[0]);
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
   
   
   
   // ----------------------------------------------------------------  Private


   private StringReplacementServiceBindingValueSourceConfig getConfig(ServiceBinding binding)
   {
      Object configSource = binding.getServiceBindingValueSourceConfig();
      if (configSource instanceof StringReplacementServiceBindingValueSourceConfig)
      {
         return (StringReplacementServiceBindingValueSourceConfig) configSource;
      }
      else
      {
         return new StringReplacementServiceBindingValueSourceConfig();
      }
   }
   
   private String replaceHostAndPort(String text, String host, int port, String hostMarker, String portMarker)
   {
      if( text == null )
         return null;
      
      if( host == null )
         host = "localhost";
      String portStr = String.valueOf(port);

      text = text.replace(hostMarker, host);
      return text.replace(portMarker, portStr);
   }

}
