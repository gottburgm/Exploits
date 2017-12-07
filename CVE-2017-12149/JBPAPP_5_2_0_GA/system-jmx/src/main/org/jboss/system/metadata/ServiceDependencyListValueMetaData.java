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
package org.jboss.system.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.system.microcontainer.LifecycleDependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceDependencyListValueMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceDependencyListValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
   private static final long serialVersionUID = 1;

   /** The dependencies */
   private List<String> dependencies;

   /** The object names */
   private List<ObjectName> objectNames;
   
   /**
    * Create a new ServiceDependencyListValueMetaData.
    */
   public ServiceDependencyListValueMetaData()
   {
   }
   
   /**
    * Create a new ServiceDependencyListValueMetaData.
    * 
    * @param dependencies the dependencies
    */
   public ServiceDependencyListValueMetaData(List<String> dependencies)
   {
      setDependencies(dependencies);
   }

   /**
    * Get the dependencies.
    * 
    * @return the dependencies.
    */
   public List<String> getDependencies()
   {
      if (dependencies == null)
      {
         List<String> result = new ArrayList<String>(objectNames.size());
         for (ObjectName objectName : objectNames)
            result.add(objectName.getCanonicalName());
         return result;
      }
      return dependencies;
   }

   /**
    * Set the dependencies.
    * 
    * @param dependencies the dependencies.
    */
   public void setDependencies(List<String> dependencies)
   {
      if (dependencies == null)
         throw new IllegalArgumentException("Null dependencies");
      for (String dependency: dependencies)
      {
         if (dependency == null)
            throw new IllegalArgumentException("Null dependency in dependencies");
      }
      
      this.dependencies = dependencies;
      this.objectNames = null;
   }

   /**
    * Get the objectNames.
    * 
    * @return the objectNames.
    * @throws MalformedObjectNameException if the list of string was set with an invalid object name
    */
   public List<ObjectName> getObjectNames() throws MalformedObjectNameException
   {
      if (objectNames == null)
      {
         List<ObjectName> names = new ArrayList<ObjectName>(dependencies.size());
         for (String dependency : dependencies)
         {
            if (dependency.trim().length() == 0)
               throw new MalformedObjectNameException("Missing object name in depends-list");
            ObjectName objectName = new ObjectName(dependency);
            if (objectName.isPattern())
               throw new MalformedObjectNameException("ObjectName patterns are not allowed in depends-list: " + dependency);
            names.add(objectName);
         }
         objectNames = names;
         dependencies = null;
      }
      return objectNames;
   }

   /**
    * Set the objectNames.
    * 
    * @param objectNames the objectNames.
    */
   public void setObjectNames(List<ObjectName> objectNames)
   {
      if (objectNames == null)
         throw new IllegalArgumentException("Null objectNames");
      for (ObjectName objectName: objectNames)
      {
         if (objectName == null)
            throw new IllegalArgumentException("Null object name in objectNames");
      }

      this.objectNames = objectNames;
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      return getObjectNames();
   }

   public void visit(ServiceMetaDataVisitor visitor)
   {
      ServiceControllerContext context = visitor.getControllerContext();
      Object name = context.getName();
      List<String> list = dependencies;
      try
      {
         List<ObjectName> names = getObjectNames();
         list = new ArrayList<String>(names.size());
         for (ObjectName objectName : names)
            list.add(objectName.getCanonicalName());
      }
      catch (MalformedObjectNameException ignored)
      {
      }
      for (String other : list)
      {
         // TODO visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.CONFIGURED));
         visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.CREATE));
         visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.START));
      }
      visitor.visit(this);
   }
}
