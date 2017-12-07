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
 * An abstract class for meta data that have config properties
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 75675 $
 */
public class ConfigPropertyMetaDataContainer implements Serializable
{
   static final long serialVersionUID = 2891949219806920844L;
   
   /** The properties by name */
   private Set<ConfigPropertyMetaData> properties = new HashSet<ConfigPropertyMetaData>();
   
   /**
    * Add a property
    * 
    * @param cpmd the property
    */
   public void addProperty(ConfigPropertyMetaData cpmd)
   {
      properties.add(cpmd);
   }
   
   /**
    * Get the properties
    * 
    * @return the properties
    */
   public Collection<ConfigPropertyMetaData> getProperties()
   {
      return properties;
   }
   
   /**
    * Get the property for a name
    * 
    * @param name the name
    * @return the property or null if there is no property with that name
    */
   public ConfigPropertyMetaData getProperty(String name)
   {
      for (ConfigPropertyMetaData cpmd : properties)
      {
         if (cpmd.getName().equals(name))
            return cpmd;
      }
      return null;
   }
}
