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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Message listener meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 75672 $
 */
public class MessageListenerMetaData implements Serializable
{
   private static final long serialVersionUID = -3196418073906964586L;
   
   /** The message listener type */
   private String type;
   
   /** The activation spec type */
   private String asType;
   
   /** The required properties */
   private Set<RequiredConfigPropertyMetaData> requiredProperties = new HashSet<RequiredConfigPropertyMetaData>();

   /**
    * Get the message listener type
    * 
    * @return the message listener type
    */
   public String getType()
   {
      return type;
   }

   /**
    * Set the message listener type
    * 
    * @param type the message listener type
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * Get the activation spec type
    * 
    * @return the activation spec type
    */
   public String getActivationSpecType()
   {
      return asType;
   }

   /**
    * Set the activation spec type
    * 
    * @param type the activation spec type
    */
   public void setActivationSpecType(String type)
   {
      this.asType = type;
   }
   
   /**
    * Add a required config property
    * 
    * @param rcpmd the required config property
    */
   public void addRequiredConfigProperty(RequiredConfigPropertyMetaData rcpmd)
   {
      requiredProperties.add(rcpmd);
   }

   /**
    * Get the required properties
    * 
    * @return the required config properties
    */
   public Collection<RequiredConfigPropertyMetaData> getRequiredConfigProperties()
   {
      return requiredProperties;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("MessageListenerMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[type=").append(type);
      buffer.append(" activationSpecType=").append(asType);
      buffer.append(" requiredProperties=").append(requiredProperties);
      buffer.append(']');
      return buffer.toString();
   }
}
