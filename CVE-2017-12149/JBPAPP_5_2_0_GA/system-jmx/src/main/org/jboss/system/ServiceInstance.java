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
package org.jboss.system;

import javax.management.ObjectInstance;

/**
 * ServiceInstance.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceInstance
{
   /** The object instance */
   private ObjectInstance objectInstance;
   
   /** The object */
   private Object resource;

   /**
    * Create a new ServiceInstance.
    */
   public ServiceInstance()
   {
   }

   /**
    * Create a new ServiceInstance.
    * 
    * @param objectInstance the object instance
    * @param resource the object
    */
   public ServiceInstance(ObjectInstance objectInstance, Object resource)
   {
      this.objectInstance = objectInstance;
      this.resource = resource;
   }

   /**
    * Get the resource.
    * 
    * @return the resource.
    */
   public Object getResource()
   {
      return resource;
   }

   /**
    * Get the objectInstance.
    * 
    * @return the objectInstance.
    */
   public ObjectInstance getObjectInstance()
   {
      return objectInstance;
   }
}
