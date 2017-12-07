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
package org.jboss.test.util;

import java.security.Provider;

import org.jboss.logging.Logger;

/**
 *  Util class that deals with Security Providers as part
 *  of the JVM
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Aug 8, 2006 
 *  @version $Revision: 85945 $
 */
public class SecurityProviderUtil
{
   private static Logger log = Logger.getLogger(SecurityProviderUtil.class);
   
   //Pass the system properties as part of the test suite setup - based on your VM
   private static String jsseProviderClassName = System.getProperty("jsse.provider.class",
                                              "com.sun.net.ssl.internal.ssl.Provider");
   
   private static String sslProtocolClass = System.getProperty("www.protocol.class",
                                              "com.sun.net.ssl.internal.www.protocol");
   
   /**
    * Get a JSSE Security Provider
    * 
    * @return the provider
    */
   public static Provider getJSSEProvider() 
   {
      Provider obj = null;
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      Class clazz;
      try
      {
         clazz = tcl.loadClass(jsseProviderClassName);
         obj = (Provider)clazz.newInstance();
      }
      catch (Throwable t)
      {
         log.error("getJSSEProvider error:", t);
      }
      return obj;
   }
   
   /**
    * Get the https protocl handler
    * 
    * @return the protocol handler name
    */
   public static String getProtocolHandlerName()
   {
      return sslProtocolClass;
   }
}
