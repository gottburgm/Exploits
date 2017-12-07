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
package org.jboss.mx.server.registry;

import java.util.Enumeration;
import java.util.Vector;

import javax.management.ObjectName;

/**
 * In-Memory database of MBeanInfo objects.  
 * This is primarily used to store and load MBean info objects (and therefore, MBeans)
 * through the persistence manager attached to this object.
 * The MBean Registry delegates to this class the work of MBean Info persistence.
 * This class further delegates that task to it's persistence manager.  This allows
 * MBeanInfo persistence to be managed as part of the invocation stack via the 
 * Persistence Interceptor.
 * @author Matt Munz
 */
public class MbeanInfoDb extends Object
{
   protected Vector fMbInfosToStore;

   public MbeanInfoDb() 
   {
       super();
   } 
   
   public void add(ObjectName nameOfMbean)
   {
      mbInfosToStore().add(nameOfMbean);
   }

   public void add(Vector namesOfMbeans)
   {
      mbInfosToStore().addAll(namesOfMbeans);
   }

   /**
    * ObjectName objects bound to MBean Info objects that are waiting to be stored in the 
    * persistence store.
    */
   protected Vector mbInfosToStore()
   {
      if(fMbInfosToStore == null)
      {
         fMbInfosToStore = new Vector(10);
      }
      return fMbInfosToStore;
   }
   
   public Enumeration mbiPersistenceQueue()
   {
      return mbInfosToStore().elements();
   }
   
   public void removeFromMbiQueue(ObjectName name)
   {
       mbInfosToStore().remove(name);
   }   
}