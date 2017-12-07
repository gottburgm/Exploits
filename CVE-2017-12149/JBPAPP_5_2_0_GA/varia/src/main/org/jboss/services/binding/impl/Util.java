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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.bootstrap.spi.ServerConfig;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingValueSource;

/**
 * Utilities used by service binding manager components.
 * 
 * @author Brian Stansberry
 * @version $Revision: 89205 $
 */
public class Util
{  
   
   public static InputStream getInputStream(URL url) throws IOException
   {
      URLConnection conn = url.openConnection();
      conn.connect();
      return conn.getInputStream();      
   }
   
   public static InputStream getInputStream(String resource) throws IOException
   {
      try
      {
         URL url = new URL(resource);
         return getInputStream(url);
      }
      catch (MalformedURLException mue)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         return cl.getResourceAsStream(resource);
      }
   }
   
   public static InputStreamReader getInputStreamReader(URL url) throws IOException
   {
      return new InputStreamReader(getInputStream(url));
   }
   
   public static InputStreamReader getInputStreamReader(String resource) throws IOException
   {
      try
      {
         URL url = new URL(resource);
         return getInputStreamReader(url);
      }
      catch (MalformedURLException mue)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         InputStream is = cl.getResourceAsStream(resource);
         if (is == null)
            throw new IllegalArgumentException("No resource " + resource + " found");
         return new InputStreamReader(is);
      }
   }
   
   public static String getContentAsString(URL url) throws IOException
   {
      InputStreamReader isr = getInputStreamReader(url);
      return getContentAsString(isr);
   }
   
   public static String getContentAsString(String resource) throws IOException
   {
      InputStreamReader isr = getInputStreamReader(resource);
      return getContentAsString(isr);
   }
   
   public static File writeToTempFile(String content) throws IOException
   {
      File targetFile = Util.createTempFile();
      OutputStreamWriter osw = null;
      try
      {
         osw = new OutputStreamWriter(new FileOutputStream(targetFile));
         osw.write(content);
         
         return targetFile;
      }
      finally
      {
         if (osw != null)
            osw.close();
      }
   }
   
   private static String getContentAsString(InputStreamReader isr) throws IOException
   {
      try
      {
         StringWriter writer = new StringWriter();
         char[] buf = new char[1024];
         int read;
         while((read = isr.read(buf, 0, buf.length)) != -1)
         {
            writer.write(buf, 0, read);
         }
         return writer.toString();
      }
      finally
      {
         isr.close();
      }
   }
   
   public static File createTempFile() throws IOException
   {
      String tmpName = SecurityActions.getSystemProperty(ServerConfig.SERVER_TEMP_DIR, "");
      File tempDirectory = new File(tmpName);
      File targetFile = File.createTempFile("service-binding", ".tmp", tempDirectory);
      targetFile.deleteOnExit();
      
      return targetFile;
   }
   
   public static <T> T getBindingValue(ServiceBindingValueSource source, ServiceBinding binding, Class<T> expectedType)
   {
      Object[] params = null;
      Object obj = source.getServiceBindingValue(binding, params);
      if (expectedType.isAssignableFrom(obj.getClass()))
      {
         return expectedType.cast(obj);
      }
      else
      {
         throw new IllegalStateException("Incompatible value source for " + binding + " -- must return " + expectedType.getSimpleName());
      } 
   }
   
   public static <T> T getBindingValueWithInput(ServiceBindingValueSource source, 
         ServiceBinding binding, Object input, Class<T> expectedType)
   {
      Object obj = source.getServiceBindingValue(binding, input);
      if (expectedType.isAssignableFrom(obj.getClass()))
      {
         return expectedType.cast(obj);
      }
      else
      {
         throw new IllegalStateException("Incompatible value source for " + binding + " -- must return " + expectedType.getSimpleName());
      } 
   }
   
   /** Prevent instantiation */
   private Util() {}
}
