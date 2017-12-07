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
package org.jboss.resource.metadata.repository;

import java.io.Serializable;

/**
 * A JCAConnectorMetaDataKey.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class JCAConnectorMetaDataKey implements Serializable
{
   static final long serialVersionUID = 6536528769896871603L;
   private String name;
      
   public JCAConnectorMetaDataKey(String name)
   {
      this.name = name;      
   }
   
   public String getName()
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if(obj == this)
         return true;
      
      if(!(obj instanceof JCAConnectorMetaDataKey))
      {
         return false;
      }
      
      JCAConnectorMetaDataKey other = (JCAConnectorMetaDataKey)obj;
      return other.getName().equals(getName());
      
   }

   @Override
   public int hashCode()
   {
      return getName().hashCode();
      
   }
   
   @Override
   public String toString()
   {
      return "[JCAConnectorMetaDataKey: " + getName() + "]";
   }
}
