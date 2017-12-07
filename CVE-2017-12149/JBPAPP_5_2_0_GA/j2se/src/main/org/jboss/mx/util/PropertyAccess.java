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
package org.jboss.mx.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

/** System property access utilties that encapsulate the
 * AccessController.doPrivileged calls required when running with a
 * security manager. Use to access system properties when the callers
 * permissions should not dictate whether or not access is allowed.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public class PropertyAccess
{
   static class PropertyReadAction implements PrivilegedAction
   {
      private String name;
      private String defaultValue;
      PropertyReadAction(String name, String defaultValue)
      {
         this.name = name;
         this.defaultValue = defaultValue;
      }
      public Object run()
      {
         return System.getProperty(name, defaultValue);
      }
   }
   static class PropertyWriteAction implements PrivilegedAction
   {
      private String name;
      private String value;
      PropertyWriteAction(String name, String value)
      {
         this.name = name;
         this.value = value;
      }
      public Object run()
      {
         return System.setProperty(name, value);
      }
   }

   public static String getProperty(String name)
   {
      return getProperty(name, null);
   }

   public static String getProperty(String name, String defaultValue)
   {
      PrivilegedAction action = new PropertyReadAction(name, defaultValue);
      String property = (String) AccessController.doPrivileged(action);
      return property;
   }

   public static String setProperty(String name, String value)
   {
      PrivilegedAction action = new PropertyWriteAction(name, value);
      String property = (String) AccessController.doPrivileged(action);
      return property;
   }
   
}
