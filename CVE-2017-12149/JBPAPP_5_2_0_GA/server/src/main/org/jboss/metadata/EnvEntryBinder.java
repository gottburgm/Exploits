/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.metadata;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.util.naming.Util;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class EnvEntryBinder
{
   public static void bindEnvEntry(Context ctx, EnvEntryMetaData entry)
      throws ClassNotFoundException, NamingException
   {
      ClassLoader loader = EnvEntryMetaData.class.getClassLoader();
      Class type = loader.loadClass(entry.getType());
      if (type == String.class)
      {
         Util.bind(ctx, entry.getName(), entry.getValue());
      }
      else if (type == Integer.class)
      {
         Util.bind(ctx, entry.getName(), new Integer(entry.getValue()));
      }
      else if (type == Long.class)
      {
         Util.bind(ctx, entry.getName(), new Long(entry.getValue()));
      }
      else if (type == Double.class)
      {
         Util.bind(ctx, entry.getName(), new Double(entry.getValue()));
      }
      else if (type == Float.class)
      {
         Util.bind(ctx, entry.getName(), new Float(entry.getValue()));
      }
      else if (type == Byte.class)
      {
         Util.bind(ctx, entry.getName(), new Byte(entry.getValue()));
      }
      else if (type == Character.class)
      {
         Object value = null;
         String input = entry.getValue();
         if (input == null || input.length() == 0)
         {
            value = new Character((char) 0);
         }
         else
         {
            value = new Character(input.charAt(0));
         }
         Util.bind(ctx, entry.getName(), value);
      }
      else if (type == Short.class)
      {
         Util.bind(ctx, entry.getName(), new Short(entry.getValue()));
      }
      else if (type == Boolean.class)
      {
         Util.bind(ctx, entry.getName(), new Boolean(entry.getValue()));
      }
      else
      {
         // Default to a String type
         Util.bind(ctx, entry.getName(), entry.getValue());
      }
   }

}
