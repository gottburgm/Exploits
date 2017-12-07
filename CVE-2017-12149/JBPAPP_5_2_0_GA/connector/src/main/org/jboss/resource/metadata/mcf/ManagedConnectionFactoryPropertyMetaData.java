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
package org.jboss.resource.metadata.mcf;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.jboss.managed.api.annotation.ManagementObject;

/**
 * A ManagedConnectionFactoryProperty.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@ManagementObject
public class ManagedConnectionFactoryPropertyMetaData implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4978655092571661074L;
   
   /** The name */
   @XmlAttribute(name="name")
   private String name;
   
   /** The type */
   @XmlAttribute(name="type")   
   private String type = "java.lang.String";
   
   @XmlValue
   private String value;
   
   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }
   /**
    * Set the name.
    * 
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   /**
    * Get the type.
    * 
    * @return the type.
    */
   public String getType()
   {
      return type;
   }
   /**
    * Set the type.
    * 
    * @param type The type to set.
    */
   public void setType(String type)
   {
      this.type = type;
   }
   /**
    * Get the value.
    * 
    * @return the value.
    */
   public String getValue()
   {
      return value;
   }
   /**
    * Set the value.
    * 
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if(this == obj)
         return true;
      
      if(!(obj instanceof ManagedConnectionFactoryPropertyMetaData))
      {
         return false;         
      }
      
      ManagedConnectionFactoryPropertyMetaData other = (ManagedConnectionFactoryPropertyMetaData)obj;
      
      if(getName() == null)
         return false;
      
      else
      {
         return getName().equals(other.getName());
      }
   }
   
   @Override
   public int hashCode()
   {
      if (getName() == null)
      {
         return 42;
      }

      return getName().hashCode();
   }
}
