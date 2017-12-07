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
package org.jboss.resource.metadata;

/**
 * Config property meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class ConfigPropertyMetaData extends DescriptionMetaDataContainer
{
   private static final long serialVersionUID = -3247621229521854849L;

   /** The name */
   private String name;
   
   /** The type */
   private String type = "java.lang.String";
   
   /** The value */
   private String value = "";

   /**
    * Get the name
    * 
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name
    * 
    * @param name the name
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get the type
    * 
    * @return the type
    */
   public String getType()
   {
      return type;
   }

   /**
    * Set the type
    * 
    * @param type the type
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * Get the value
    * 
    * @return the value
    */
   public String getValue()
   {
      return value;
   }

   /**
    * Set the value
    * 
    * @param value the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("ConfigPropertyMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[name=").append(name);
      if (type != null)
         buffer.append(" type=").append(type);
      if (value != null)
         buffer.append(" value=").append(value);
      buffer.append(" descriptions=").append(getDescriptions());
      buffer.append(']');
      return buffer.toString();
   }
}
