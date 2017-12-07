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
package org.jboss.services.deployment.metadata;

import java.io.Serializable;

/**
 * Simple POJO class to model XML data
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81038 $
 */
public class PropertyInfo
   implements Serializable
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = -1246926015774516936L;
      
   private String  name;
   private String  type;
   private boolean optional;
   private String  description;
   private Object  defaultValue;
   
   public PropertyInfo()
   {
      // empty
   }
   
   public PropertyInfo(PropertyInfo that)
   {
      this.name = that.name;
      this.type = that.type;
      this.optional = that.optional;
      this.description = that.description;
      this.defaultValue = that.defaultValue; // shouldn't we copy this?
   }
   
   public PropertyInfo(String name, String type, boolean optional, String description, Object defaultValue)
   {
      this.name = name;
      this.type = type;
      this.optional = optional;
      this.description = description;
      this.defaultValue = defaultValue;
   }
   
   public Object getDefaultValue()
   {
      return defaultValue;
   }
   
   public void setDefaultValue(Object defaultValue)
   {
      this.defaultValue = defaultValue;
   }
   
   public String getDescription()
   {
      return description;
   }
   
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public boolean isOptional()
   {
      return optional;
   }
   
   public void setOptional(boolean optional)
   {
      this.optional = optional;
   }
   
   public String getType()
   {
      return type;
   }
   
   public void setType(String type)
   {
      this.type = type;
   }
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append('[')
      .append("name=").append(name)
      .append(", type=").append(type)
      .append(", optional=").append(optional)
      .append(", description=").append(description)
      .append(", defaultValue=").append(defaultValue)      
      .append(']');
      
      return sbuf.toString();      
   }     
}
