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
package org.jboss.system.deployers;

import javax.management.ObjectName;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceController;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * ServiceDeployer.<p>
 * 
 * This deployer is responsible for deploying services of
 * type {@link ServiceDeployment}.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 85945 $
 */
public class ServiceDeployer extends AbstractSimpleRealDeployer<ServiceMetaData>
{
   /** The service controller */
   private final ServiceController controller;
   public static final ObjectName DEFAULT_CLASSLOADER_OBJECT_NAME = ObjectNameFactory.create("jboss:service=defaultClassLoader");

   private ObjectName defaultClassLoader = DEFAULT_CLASSLOADER_OBJECT_NAME;

   /**
    * Create a new ServiceDeployer.
    * 
    * @param controller the service controller
    * @throws IllegalArgumentException for a null controller
    */
   public ServiceDeployer(ServiceController controller)
   {
      super(ServiceMetaData.class);
      if (controller == null)
         throw new IllegalArgumentException("Null controller");
      this.controller = controller;
      setComponentsOnly(true);
      setUseUnitName(true);
   }


   public ObjectName getDefaultClassLoader()
   {
      return defaultClassLoader;
   }

   public void setDefaultClassLoader(ObjectName defaultClassLoader)
   {
      this.defaultClassLoader = defaultClassLoader;
   }

   public void deploy(DeploymentUnit unit, ServiceMetaData deployment) throws DeploymentException
   {
      ObjectName name = deployment.getObjectName();
      try
      {
         ObjectName loaderName = deployment.getClassLoaderName();
         if (loaderName == null)
            loaderName = findLoaderName(unit.getClassLoader());

         controller.install(deployment, loaderName);
         ServiceContext context = controller.getServiceContext(name);
         if (context == null)
            throw new IllegalStateException("No context for " + name);
         try
         {
            create(context);
            try
            {
               start(context);
               Throwable t = context.getProblem();
               if (t != null)
                  throw t;
            }
            catch (Throwable t)
            {
               destroy(name);
               throw t;
            }
         }
         catch (Throwable t)
         {
            remove(name);
            throw t;
         }
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + name, t);
      }
   }

   /**
    * Find first RealClassLoader instance
    * and return its ObjectName.
    * If none is found return defaultClassloader.
    *
    * @param cl the classloader
    * @return classloader's ObjectName
    */
   protected ObjectName findLoaderName(ClassLoader cl)
   {
      if (cl == null)
         return defaultClassLoader;

      if (cl instanceof RealClassLoader)
      {
         RealClassLoader rcl = RealClassLoader.class.cast(cl);
         return rcl.getObjectName();
      }

      return findLoaderName(cl.getParent());
   }

   public void undeploy(DeploymentUnit unit, ServiceMetaData deployment)
   {
      ObjectName name = deployment.getObjectName();
      ServiceContext context = controller.getServiceContext(name);
      if (context != null)
      {
         stop(name);
         destroy(name);
         remove(name);
      }
   }
   
   protected void create(ServiceContext context) throws Throwable
   {
      controller.create(context.objectName);
   }
   
   protected void start(ServiceContext context) throws Throwable
   {
      controller.start(context.objectName);
   }
   
   protected void stop(ObjectName name)
   {
      try
      {
         controller.stop(name);
      }
      catch (Throwable t)
      {
         log.warn("Error during stop for " + name, t);
      }
   }
   
   protected void destroy(ObjectName name)
   {
      try
      {
         controller.destroy(name);
      }
      catch (Throwable t)
      {
         log.warn("Error during destroy for " + name, t);
      }
   }
   
   protected void remove(ObjectName name)
   {
      try
      {
         controller.remove(name);
      }
      catch (Throwable t)
      {
         log.warn("Error during destroy for " + name, t);
      }
   }
}
