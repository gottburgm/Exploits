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

import java.util.Map;

import javax.management.ObjectName;

import org.jboss.mx.server.MBeanInvoker;
import org.jboss.mx.server.ServerConstants;

/**
 * info@todo this docs
 *
 * @see org.jboss.mx.server.registry.MBeanRegistry
 * @see org.jboss.mx.server.MBeanServerImpl
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $
 */
public class MBeanEntry
   implements ServerConstants
{
   // Attributes ----------------------------------------------------

   /**
    * The registered object name of the mbean
    */
   private ObjectName objectName = null;

   /**
    * The class name of the mbean
    */
   private String resourceClassName = null;

   /**
    * The object used to invoke the mbean
    */
   private MBeanInvoker invoker  = null;

   /**
    * The mbean registered
    */
   private Object resource  = null;

   /**
    * The context classloader of the mbean
    */
   private ClassLoader cl  = null;

   /**
    * The value map of the mbean
    */
   private Map valueMap  = null;

   // Constructors --------------------------------------------------

   /**
    * Construct a new mbean registration entry.
    *
    * @param objectName the name with which the mbean is registered
    * @param invoker the dynamic mbean used to invoke the mbean
    * @param resource the mbean
    * @param valueMap any other information to include in the registration
    */
   public MBeanEntry(ObjectName objectName, MBeanInvoker invoker, 
                     Object resource, Map valueMap)
   {
      this.objectName = objectName;
      this.invoker = invoker;
      this.resourceClassName = resource.getClass().getName();
      this.resource = resource;
      this.valueMap = valueMap;

      // Adrian: Unpack the classloader because this is used alot
      if (valueMap != null)
         this.cl = (ClassLoader) valueMap.get(CLASSLOADER);
   }

   // Public --------------------------------------------------------

   /**
    * Retrieve the object name with the mbean is registered.
    *
    * @return the object name
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }

   /** A protected method used to set the entry object name when access
    * to the entry is needed before the ultimate name under which the
    * mbean is registered is known.
    * 
    * @param objectName - the object name under which the mbean is registered
    */
   protected void setObjectName(ObjectName objectName)
   {
      this.objectName = objectName;
   }

   /**
    * Retrieve the invoker for the mbean.
    *
    * @return the invoker
    */
   public MBeanInvoker getInvoker()
   {
      return invoker;
   }

   /**
    * Retrieve the class name for the mbean.
    *
    * @return the class name
    */
   public String getResourceClassName()
   {
      return resourceClassName;
   }

   /**
    * Retrieve the class name for the mbean.
    *
    * @param resourceClassName the class name
    */
   public void setResourceClassName(String resourceClassName)
   {
      this.resourceClassName = resourceClassName;
   }

   /**
    * Retrieve the mbean.
    *
    * @return the mbean
    */
   public Object getResourceInstance()
   {
      return resource;
   }

   /**
    * Retrieve the context class loader with which to invoke the mbean.
    *
    * @return the class loader
    */
   public ClassLoader getClassLoader()
   {
      return cl;
   }

   /**
    * Retrieve a value from the map.
    *
    * @return key the key to value
    * @return the value or null if there is no entry
    */
   public Object getValue(String key)
   {
      if (valueMap != null)
         return valueMap.get(key);
      return null;
   }
}
