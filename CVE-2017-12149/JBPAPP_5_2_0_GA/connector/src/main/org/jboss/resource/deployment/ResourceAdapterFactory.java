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
import java.util.Iterator;

import javax.resource.spi.ResourceAdapter;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;

/**
 * A resource adapter factory
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 113110 $
 */
public class ResourceAdapterFactory
{
   /** The dummy resource adapter for old deployment */
   public static final String DUMMY_RA_CLASS = DummyResourceAdapter.class.getName();
   
   /**
    * Create a new ResourceAdapter
    * 
    * @param ramd the ResourceAdapterMetaData
    * @return the resource adapter
    * @throws Exception
    */
   public static ResourceAdapter createResourceAdapter(RARDeploymentMetaData ramd) throws Exception
   {
      ResourceAdapter adapter = createResourceAdapter(ramd.getConnectorMetaData());
      
      ConfigPropertyHandler configPropertyHandler = new ConfigPropertyHandler(adapter, adapter.getClass(), "ResourceAdapter: ");
      for(Iterator iter = ramd.getRaXmlMetaData().getProperties().iterator(); iter.hasNext();)
      {
         ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData)iter.next();
         applyProperty(configPropertyHandler, cpmd, adapter.getClass());
      }
            
      return adapter;
   }
   
   /**
    * Create a new resource adapter
    * 
    * @param cmd the connector meta data
    * @throws Exception for any error
    */
   public static ResourceAdapter createResourceAdapter(ConnectorMetaData cmd) throws Exception
   {
      // Determine the resource adapter class
      String className = cmd.getRAClass();
      if (className == null)
         className = DUMMY_RA_CLASS;
      
      // Load the class
      Class raClass = Thread.currentThread().getContextClassLoader().loadClass(className);
      if (ResourceAdapter.class.isAssignableFrom(raClass) == false)
         throw new DeploymentException(raClass.getName() + " is not a resource adapter class");
      ResourceAdapter result = (ResourceAdapter) raClass.newInstance();
      
      // Apply the properties
      ConfigPropertyHandler configPropertyHandler = new ConfigPropertyHandler(result, raClass, "ResourceAdapter: ");
      for (Iterator i = cmd.getProperties().iterator(); i.hasNext();)
      {
         ConfigPropertyMetaData cpmd = (ConfigPropertyMetaData) i.next();
         applyProperty(configPropertyHandler, cpmd, raClass);         
      }
      
      return result;
   }

   private static void applyProperty(ConfigPropertyHandler configPropertyHandler,
                                     ConfigPropertyMetaData cpmd,
                                     Class clz) throws Exception
   {
      try
      {
         configPropertyHandler.handle(cpmd);
      }
      catch (InvocationTargetException e)
      {
         DeploymentException.rethrowAsDeploymentException("Error for resource adapter class " + clz.getName() + " setting property " + cpmd, e.getTargetException());
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error for resource adapter class " + clz.getName() + " accessing property setter " + cpmd, t);
      }
   }
}
