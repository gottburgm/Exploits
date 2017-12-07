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
package org.jboss.resource.deployment;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.resource.metadata.AdminObjectMetaData;
import org.jboss.resource.metadata.ConfigPropertyMetaData;

/**
 * An admin object factory
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 113110 $
 */
public class AdminObjectFactory
{
   /** The logger */
   private static final Logger log = Logger.getLogger(AdminObjectFactory.class);

   public static Object createAdminObject(final String jndiName, ObjectName rarName, AdminObjectMetaData aomd,
         Properties properties) throws Exception
   {
      final boolean trace = log.isTraceEnabled();

      // Get the current classloader
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      if (trace)
         log.trace("Creating AdminObject '" + jndiName + "' metadata=" + aomd + " rar=" + rarName + " properties="
               + properties + " classloader=" + cl);

      // The interface class
      String interfaceName = aomd.getAdminObjectInterfaceClass();
      // Load the interface class class
      if (trace)
         log.trace("AdminObject '" + jndiName + "' loading interface=" + interfaceName);
      Class interfaceClass = cl.loadClass(interfaceName);

      // Determine the implementation class
      String implName = aomd.getAdminObjectImplementationClass();
      if (implName == null)
         throw new DeploymentException("No implementation class for admin object '" + interfaceClass + "' ra="
               + rarName);

      // Load the implementation class
      if (trace)
         log.trace("AdminObject '" + jndiName + "' loading implementation=" + implName);
      Class implClass = cl.loadClass(implName);
      if (interfaceClass.isAssignableFrom(implClass) == false)
         throw new DeploymentException(implClass.getName() + " is not a '" + interfaceClass + "' ra=" + rarName);

      Object result = implClass.newInstance();
      if (trace)
         log.trace("AdminObject '" + jndiName + "' created instance=" + result);

      // Create ConfigPropertyHandler for the AdminObject
      ConfigPropertyHandler configPropertyHandler = new ConfigPropertyHandler(result, implClass, "AdminObject: ");
      
      // Apply values from the ra.xml 
      Collection raProperties = aomd.getProperties();
      if (raProperties != null && raProperties.size() != 0)
      {
         
         for (Iterator i = raProperties.iterator(); i.hasNext();)
         {
            ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData) i.next();
            String name = cpmd.getName();
            String value = cpmd.getValue();
            if (value != null && value.length() > 0)
            {
               if (properties.containsKey(name))
               {
                  if (trace)
                     log.trace("AdminObject '" + jndiName + "' property=" + name + " IGNORING value=" + value
                           + " specified in MBean properties.");
               }
               else
               {
                  // Load the property class as defined in the meta data
                  String typeName = cpmd.getType();
                  if (trace)
                     log.trace("AdminObject '" + jndiName + "' property=" + name + " loading class=" + typeName);

                  try
                  {
                     configPropertyHandler.handle(cpmd);
                  }
                  catch (InvocationTargetException e)
                  {
                     DeploymentException.rethrowAsDeploymentException("Error for property '" + name + "' class="
                           + implClass + "' for admin object '" + interfaceClass + "' ra=" + rarName, e
                           .getTargetException());
                  }
                  catch (Throwable t)
                  {
                     DeploymentException.rethrowAsDeploymentException("Error for property '" + name + "' class="
                           + implClass + "' for admin object '" + interfaceClass + "' ra=" + rarName, t);
                  }
               }
            }
         }
      }

      // Apply the properties
      if (properties != null)
      {
         for (Iterator i = properties.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry property = (Map.Entry) i.next();
            String name = (String) property.getKey();
            String value = (String) property.getValue();
            
            if (trace)
               log.trace("AdminObject '" + jndiName + "' property=" + name + " value=" + value);

            // Pick up the property metadata
            ConfigPropertyMetaData cpmd = aomd.getProperty(name);
            if (cpmd == null)
               throw new DeploymentException("No property '" + name + "' for admin object '" + interfaceClass + "' ra="
                     + rarName);
            
            // Make copy of the ConfigPropertyMetaData with new value
            ConfigPropertyMetaData cpmdCopy = new ConfigPropertyMetaData();
            cpmdCopy.setName(name);
            cpmdCopy.setType(cpmd.getType());
            cpmdCopy.setValue(value);
            
            try
            {
               configPropertyHandler.handle(cpmdCopy);
            }
            catch (InvocationTargetException e)
            {
               DeploymentException.rethrowAsDeploymentException("Error for property '" + name + "' class=" + implClass
                     + "' for admin object '" + interfaceClass + "' ra=" + rarName, e.getTargetException());
            }
            catch (Throwable t)
            {
               DeploymentException.rethrowAsDeploymentException("Error for property '" + name + "' class=" + implClass
                     + "' for admin object '" + interfaceClass + "' ra=" + rarName, t);
            }
         }
      }

      return result;
   }
}
