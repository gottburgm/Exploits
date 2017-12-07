/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.persistence.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A component mapper registry.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ComponentMapperRegistry
{
   
   /** The instance. */
   private static final ComponentMapperRegistry INSTANCE = new ComponentMapperRegistry();
   
   /** The component mapper map. */
   private final Map<String, ComponentMapper> map = new ConcurrentHashMap<String, ComponentMapper>();

   protected ComponentMapperRegistry()
   {
      //
   }
   
   public static ComponentMapperRegistry getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Get a component mapper.
    * 
    * @param name the mapper type
    * @return the mapper or null if not registered
    */
   public ComponentMapper getMapper(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("null name");
      
      return this.map.get(name);
   }
   
   /**
    * Add a component mapper.
    * 
    * @param mapper the component mapper
    */
   public void addMapper(ComponentMapper mapper)
   {
      if(mapper == null)
         throw new IllegalArgumentException("null mapper");
      
      this.map.put(mapper.getType(), mapper);
   }
   
   public void addMapper(String type, ComponentMapper mapper)
   {
      if(type == null)
         throw new IllegalArgumentException("null mapper type");

      this.map.put(type, mapper);      
   }

   /**
    * Remove a component mapper.
    * 
    * @param mapper the component mapper
    * @return the previous mapper or null
    */
   public ComponentMapper removeComponentMapper(ComponentMapper mapper)
   {
      if(mapper == null)
         throw new IllegalArgumentException("null mapper");
      return removeComponentMapper(mapper.getType());
   }
   
   public ComponentMapper removeComponentMapper(String type)
   {
      if(type == null)
         throw new IllegalArgumentException("null mapper type");
      
      return this.map.remove(type);
   }
}

