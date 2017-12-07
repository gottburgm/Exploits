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
package org.jboss.deployment;

import java.io.File;
import java.net.URL;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;

/**
 * A simple subdeployer that deploys a managed object after parsing the 
 * deployment's xml file.
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81033 $
 */
public abstract class SimpleSubDeployerSupport extends SubDeployerSupport
{
   /** A proxy to the ServiceController. */
   private ServiceControllerMBean serviceController;
   
   /** Whether we registered the classloader */
   private boolean registeredClassLoader = false;

   /**
    * Get the package extension for this deployment
    * 
    * @return the package extension
    */
   public abstract String getExtension();

   /**
    * Get the metadata url
    * 
    * @return the meta data url
    */
   public abstract String getMetaDataURL();

   /**
    * Get the object name for this deployment 
    * 
    * @param di the deployment info
    * @return the object name
    */
   public abstract String getObjectName(DeploymentInfo di) throws DeploymentException;

   /**
    * Get the deployment class 
    * 
    * @return the deployment class
    */
   public abstract String getDeploymentClass();

   public boolean accepts(DeploymentInfo di)
   {
      String urlStr = di.url.toString();
      String extension = getExtension();
      return urlStr.endsWith(extension) || urlStr.endsWith(extension + '/');
   }

   public void init(DeploymentInfo di) throws DeploymentException
   {
      URL url = getMetaDataResource(di);
      parseMetaData(di, url);
      resolveWatch(di, url);
      super.init(di);
   }

   public void create(DeploymentInfo di) throws DeploymentException
   {
      determineObjectName(di);
      ObjectName uclName = registerClassLoader(di);
      try
      {
         registerDeployment(di, uclName);
         try
         {
            createService(di);
            try
            {
               super.create(di);
            }
            catch (Throwable t)
            {
               destroyService(di);
            }
         }
         catch (Throwable t)
         {
            unregisterDeployment(di);
            throw t;
         }
      }
      catch (Throwable t)
      {
         unregisterClassLoader(di);
         DeploymentException.rethrowAsDeploymentException("Error creating deployment " + di.url, t);
      }
   }

   public void start(DeploymentInfo di) throws DeploymentException
   {
      startService(di);
      try
      {
         super.start(di);
      }
      catch (Throwable t)
      {
         stopService(di);
         DeploymentException.rethrowAsDeploymentException("Error in start for deployment " + di.url, t);
      }
   }

   public void stop(DeploymentInfo di) throws DeploymentException
   {
      stopService(di);
      super.stop(di);
   }

   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         destroyService(di);
         super.destroy(di);
      }
      finally
      {
         unregisterDeployment(di);
         unregisterClassLoader(di);
      }
   }

   public void postRegister(Boolean done)
   {
      super.postRegister(done);

      if (done.booleanValue())
      {
         serviceController = (ServiceControllerMBean)
             MBeanProxyExt.create(ServiceControllerMBean.class,
                                  ServiceControllerMBean.OBJECT_NAME,
                                  server);
      }
   }
   
   /**
    * Get the url of the meta data resource
    * 
    * @param di the deployment info
    * @return the url of the meta data resource
    * @throws DeploymentException for any error
    */
   protected URL getMetaDataResource(DeploymentInfo di) throws DeploymentException
   {
      URL url = di.localCl.findResource(getMetaDataURL());
      if (url == null)
         throw new DeploymentException("Could not find meta data " + getMetaDataURL() + " for deployment " + di.url);
      return url;
   }

   /**
    * Parse the meta data
    * 
    * @param di the deployment info
    * @param url the location of the meta data
    * @throws DeploymentException for any error
    */
   abstract protected void parseMetaData(DeploymentInfo di, URL url) throws DeploymentException;

   /**
    * Resolve the watch url
    * 
    * @param di the deployment info
    * @param url the location of the meta data
    * @throws DeploymentException for any error
    */
   protected void resolveWatch(DeploymentInfo di, URL url) throws DeploymentException
   {
      // Assume we watch the main deployment
      di.watch = di.url;
      
      // Unless it is an unpacked directory
      if (di.url.getProtocol().equals("file"))
      {
         File file = new File(di.url.getFile());
         if (file.isDirectory())
            di.watch = url;
      }
   }

   /**
    * Determine the object name
    * 
    * @param di the deployment info
    * @throws DeploymentException for any error
    */
   protected void determineObjectName(DeploymentInfo di) throws DeploymentException
   {
      String objectName = getObjectName(di);
      try
      {
         di.deployedObject = new ObjectName(objectName);
      }
      catch (MalformedObjectNameException e)
      {
         throw new DeploymentException("INTERNAL ERROR: Bad object name. " + objectName);
      }
   }

   /**
    * Register the UCL classloader
    * 
    * @param di the deployment info
    * @return the object name of the classloader
    * @throws DeploymentException for any error
    */
   protected ObjectName registerClassLoader(DeploymentInfo di) throws DeploymentException
   {
      ObjectName uclName = null;
      try
      {
         RepositoryClassLoader ucl = di.ucl;
         uclName = ucl.getObjectName();
         if (server.isRegistered(uclName) == false)
         {
            server.registerMBean(di.ucl, uclName);
            registeredClassLoader = true;
         }
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error registering classloader " + uclName, t);
      }
      return uclName;
   }

   /**
    * Unregister the UCL classloader
    * 
    * @param di the deployment info
    */
   protected void unregisterClassLoader(DeploymentInfo di)
   {
      ObjectName uclName = null;
      try
      {
         RepositoryClassLoader ucl = di.ucl;
         if (ucl != null)
         {
            uclName = ucl.getObjectName();
            if (registeredClassLoader && server.isRegistered(uclName))
            {
               server.unregisterMBean(uclName);
               registeredClassLoader = false;
            }
         }
      }
      catch (Throwable t)
      {
         log.warn("Error unregistering classloader " + uclName, t);
      }
   }

   /**
    * Register the deployment
    * 
    * @param di the deployment info
    * @param uclName the object name of the classloader
    * @throws DeploymentException for any error
    */
   protected void registerDeployment(DeploymentInfo di, ObjectName uclName) throws DeploymentException
   {
      try
      {
         String deploymentClass = getDeploymentClass();
         server.createMBean(deploymentClass, di.deployedObject, uclName, new Object[] { di }, new String[] { DeploymentInfo.class.getName() });
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error registering deployment " + di.url, t);
      }
   }

   /**
    * Unregister the deployment
    * 
    * @param di the deployment info
    */
   protected void unregisterDeployment(DeploymentInfo di)
   {
      try
      {
         if (server.isRegistered(di.deployedObject))
            server.unregisterMBean(di.deployedObject);
      }
      catch (Throwable t)
      {
         log.warn("Error unregistering deployment " + di.deployedObject, t);
      }
   }

   /**
    * Do the create lifecyle for the deployment
    * 
    * @param di the deployment info
    * @throws DeploymentException for any error
    */
   protected void createService(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         serviceController.create(di.deployedObject);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error in create for deployment " + di.url, t);
      }
   }

   /**
    * Do the start lifecyle for the deployment
    * 
    * @param di the deployment info
    * @throws DeploymentException for any error
    */
   protected void startService(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         serviceController.start(di.deployedObject);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error in start for deployment " + di.url, t);
      }
   }

   /**
    * Do the stop lifecyle for the deployment
    * 
    * @param di the deployment info
    */
   protected void stopService(DeploymentInfo di)
   {
      try
      {
         if (di.deployedObject != null)
            serviceController.stop(di.deployedObject);
      }
      catch (Throwable t)
      {
         log.warn("Error in stop for deployment " + di.url, t);
      }
   }

   /**
    * Do the destroy lifecyle for the deployment
    * 
    * @param di the deployment info
    */
   protected void destroyService(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         if (di.deployedObject != null)
            serviceController.destroy(di.deployedObject);
      }
      catch (Throwable t)
      {
         log.warn("Error in destroy for deployment " + di.url, t);
      }
      try
      {
         if (di.deployedObject != null)
            serviceController.remove(di.deployedObject);
      }
      catch (Throwable t)
      {
         log.warn("Error in remove for deployment " + di.url, t);
      }
   }
}
