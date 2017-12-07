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
package org.jboss.test.refs.common;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ServiceLocator
{
   private ServiceLocator()
   {
   }

   public static Object lookup(String name) throws NamingException
   {
      return lookup(name, null);
   }

   public static Object lookupByShortName(String shortName)
         throws NamingException
   {
      return lookup("java:comp/env/" + shortName, null);
   }

   public static Object lookup(Class type) throws NamingException
   {
      return lookup(null, type);
   }

   /**
    * Looks up a resource by its name or fully qualified type name. If name is
    * not null, then use it to look up and type is ignored. If name is null,
    * then try to use the fully qualified class name of type.
    * 
    */
   public static Object lookup(String name, Class type) throws NamingException
   {
      String nameToUse = null;
      if (name == null)
      {
         nameToUse = type.getName();
      }
      else
      {
         nameToUse = name;
      }
      Context context = new InitialContext();
      return context.lookup(nameToUse);
   }
}
