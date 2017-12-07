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
package org.jboss.profileservice.management;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.jboss.managed.api.ManagedProperty;

public class BaseManagedObject
   implements Serializable
{
   private static final long serialVersionUID = 1;
   private String simpleName;
   private Map<String, ManagedProperty> properties;

   public BaseManagedObject(String simpleName, Map<String, ManagedProperty> properties)
   {
      this.simpleName = simpleName;
      this.properties = properties;
   }

   public String getSimpleName()
   {
      return simpleName;
   }
   public String getName()
   {
      return simpleName;
   }

   /**
    * Get the managed property names
    * 
    * @return the property names
    */
   public Set<String> getPropertyNames()
   {
      return properties.keySet();
   }

   /**
    * Get a property
    * 
    * @param name the name
    * @return the property
    */
   public ManagedProperty getProperty(String name)
   {
      ManagedProperty prop = properties.get(name);
      return prop;
   }
   
   /**
    * Get the properties
    * 
    * @return the properties
    */
   public Map<String, ManagedProperty> getProperties()
   {
      return properties;
   }

   /**
    * Append the name and props 
    * @param sb the buffer to append the name and props to
    */
   protected void toString(StringBuilder sb)
   {
      sb.append("simpleName=");
      sb.append(simpleName);
      sb.append(", properties=");
      sb.append(properties);
   }
}
