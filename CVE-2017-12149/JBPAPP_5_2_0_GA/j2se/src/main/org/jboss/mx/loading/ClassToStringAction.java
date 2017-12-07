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
package org.jboss.mx.loading;

import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.CodeSource;

/** An encapsulation creating a to string rep for a class using a
 * PrivilegedAction for getting the ProtectionDomain.
 * 
 * @version $Revision: 81019 $
 * @author Scott.Stark@jboss.org
 */
class ClassToStringAction implements PrivilegedAction
{
   private StringBuffer buffer;
   private Class clazz;
   ClassToStringAction(Class clazz, StringBuffer buffer)
   {
      this.clazz = clazz;
      this.buffer = buffer;
   }
   public Object run()
   {
      if( clazz != null )
      {
         buffer.append(clazz.getName());
         buffer.append("@"+Integer.toHexString(clazz.hashCode()));
         CodeSource cs = clazz.getProtectionDomain().getCodeSource();
         buffer.append("<CodeSource: "+cs+">");
      }
      else
      {
         buffer.append("null");
      }
      return null;
   }

   static void toString(Class clazz, StringBuffer buffer)
   {
      PrivilegedAction action = new ClassToStringAction(clazz, buffer);
      AccessController.doPrivileged(action);
   }

   static class SysPropertyAction implements PrivilegedAction
   {
      private String key;
      private String def;
      SysPropertyAction(String key, String def)
      {
         this.key = key;
         this.def = def;
      }
      public Object run()
      {
         return System.getProperty(key, def);
      }
   }
   static String getProperty(String key, String def)
   {
      PrivilegedAction action = new SysPropertyAction(key, def);
      String value = (String) AccessController.doPrivileged(action);
      return value;
   }
}
