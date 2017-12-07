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

package org.jboss.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged actions for the package
 * 
 * @author Scott.Stark@jboss.org
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 *
 */
class SysPropertyActions
{
   interface SysProps
   {
      SysProps NON_PRIVILEGED = new SysProps()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            return System.getProperty(name, defaultValue);
         }
         
         public String setProperty(final String name, final String value)
         {
            return System.setProperty(name, value);
         }
      };
      SysProps PRIVILEGED = new SysProps()
      {
         public String getProperty(final String name, final String defaultValue)
         {
            PrivilegedAction<String> action = new PrivilegedAction<String>()
            {
               public String run()
               {
                  return System.getProperty(name, defaultValue);
               }
            };
            return (String) AccessController.doPrivileged(action);
         }
         
         public String setProperty(final String name, final String value)
         {
            PrivilegedAction<String> action = new PrivilegedAction<String>()
            {
               public String run()
               {
                  return System.setProperty(name, value);
               }
            };
            return (String) AccessController.doPrivileged(action);
         }
      };
      String getProperty(String name, String defaultValue);
      String setProperty(final String name, final String value);
   }

   public static String getProperty(String name, String defaultValue)
   {
      String prop;
      if( System.getSecurityManager() == null )
         prop = SysProps.NON_PRIVILEGED.getProperty(name, defaultValue);
      else
         prop = SysProps.PRIVILEGED.getProperty(name, defaultValue);
      return prop;
   }

   public static String setProperty(String name, String value)
   {
      String prop;
      if( System.getSecurityManager() == null )
         prop = SysProps.NON_PRIVILEGED.setProperty(name, value);
      else
         prop = SysProps.PRIVILEGED.setProperty(name, value);
      return prop;
   }
}
