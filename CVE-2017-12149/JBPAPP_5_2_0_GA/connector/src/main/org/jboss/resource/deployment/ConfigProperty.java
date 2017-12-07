/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

/**
 * Holder for resolved configuration properties.  

 * @author <a href="baileyje@gmail.com">John Bailey</a>
 * @version $Revision: $
 */
public class ConfigProperty
{
   /**
    * Name of config property
    */
   private final String name;

   /**
    * Class type of the config property
    */
   private final Class type;

   /**
    * Value of the config property
    */
   private final Object value;

   /**
    * Constructor
    * 
    * @param name The name of the property
    * @param type The type of the property
    * @param value The value of the property
    */
   public ConfigProperty(String name, Class type, Object value)
   {
      super();
      this.name = name;
      this.type = type;
      this.value = value;
   }

   /**
    * Get the name of the property
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the type of the property
    * @return The value
    */
   public Class getType()
   {
      return type;
   }

   /**
    * Get the value of the property
    * @return The value
    */
   public Object getValue()
   {
      return value;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("ConfigProperty").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[name=").append(name);
      if (type != null)
         buffer.append(" type=").append(type);
      if (value != null)
         buffer.append(" value=").append(value);
      buffer.append(']');
      return buffer.toString();
   }
}
