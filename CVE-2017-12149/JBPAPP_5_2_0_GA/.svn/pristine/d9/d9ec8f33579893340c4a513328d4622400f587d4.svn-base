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
package org.jboss.mx.remoting.connector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.remote.JMXProviderException;

/**
 * This is a help class for the JMXConnectorFactory and JMXConnectorServerFactory. They both have the same requirements
 * in regards to processing JMXServiceURL, so just putting the code in one place instead of the same code in both of the
 * classes.
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class ConnectorFactoryUtil
{
   private ConnectorFactoryUtil()
   {
      // need to only have public static methods
   }

   public static String normalizeProtocol(String protocol)
   {
      // convert specical characters per spec.
      protocol = protocol.replace('+', '.');
      protocol = protocol.replace('-', '_');
      return protocol;
   }

   public static void validateEnvironmentMap(Map environment)
   {
      // Per spec (javadoc), all the keys in the environment map must be Strings.
      // In order to be totally compliant, will check for this, but really just slows things down.
      Set keys = environment.keySet();
      Iterator itr = keys.iterator();
      while(itr.hasNext())
      {
         Object keyValue = null;
         try
         {
            keyValue = itr.next();
            String keyAsString = (String) keyValue;
         }
         catch(ClassCastException e)
         {
            throw new IllegalArgumentException("All keys within environment map must be of type java.lang.String.  " +
                                               keyValue + " is of type " + keyValue.getClass().getName());
         }
      }
   }

   public static ClassLoader locateClassLoader(Map environment, String classLoaderKey)
   {
      ClassLoader classloader = null;

      Object val = environment.get(classLoaderKey);
      if(val != null)
      {
         if(val instanceof ClassLoader)
         {
            classloader = (ClassLoader) val;
         }
         else
         {
            throw new IllegalArgumentException("Error using value specified for " + classLoaderKey +
                                               " because is not an instance of " + ClassLoader.class.getName());
         }
      }
      else
      {
         // just use current caller's thread's classloader
         classloader = Thread.currentThread().getContextClassLoader();
      }

      environment.put(classLoaderKey, classloader);


      return classloader;
   }

   public static List locateProviderPackage(Map environment, String providerPackageKey)
         throws JMXProviderException
   {

      String providerPackage = null;

      // First look for the provider package within the environment map
      Object val = environment.get(providerPackageKey);

      // once again, to be spec compliant ("If the provider package list is not a String,
      // or if it contains an element that is an empty string, a JMXProviderException is thrown.")
      if(providerPackage != null)
      {
         if(val instanceof String)
         {
            providerPackage = ((String) val).trim();
            // now have to make sure not an empty entry
            if(providerPackage.startsWith("|") || providerPackage.endsWith("|") ||
               providerPackage.indexOf("||") != -1)
            {

               throw new JMXProviderException("Error processing " + providerPackageKey + " from the " +
                                              "environment map.  An empty provider package exists.");
            }
         }
         else
         {
            throw new JMXProviderException("Error processing " + providerPackageKey + " from the " +
                                           "environment map.  Is is not of type String.");
         }
      }
      else
      {
         // Now try system property
         providerPackage = System.getProperty(providerPackageKey);
      }

      List providerPackages = new ArrayList();
      if(providerPackage != null && providerPackage.length() > 0)
      {
         StringTokenizer tokenizer = new StringTokenizer(providerPackage, "|");
         while(tokenizer.hasMoreElements())
         {
            providerPackages.add(tokenizer.nextElement());
         }
      }

      return providerPackages;
   }

}