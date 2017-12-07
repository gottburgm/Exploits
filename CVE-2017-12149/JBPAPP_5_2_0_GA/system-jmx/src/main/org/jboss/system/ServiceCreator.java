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

import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.server.registry.MBeanEntry;
import org.jboss.mx.service.ServiceConstants;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.w3c.dom.Element;

/**
 * A helper class for the controller.
 *
 * @see Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81485 $
 */
public class ServiceCreator
{
   /** Instance logger. */
   private static final Logger log = Logger.getLogger(ServiceCreator.class);
   
   /** The MBean Registry Object Name */
   private static ObjectName MBEAN_REGISTRY = ObjectNameFactory.create(ServerConstants.MBEAN_REGISTRY);
   
   /** The server */
   private MBeanServer server;

   /**
    * Rethrow an error as an exception
    * 
    * @param context the context
    * @param t the original throwable
    * @return never
    * @throws Exception always
    */
   public static Exception rethrow(String context, Throwable t) throws Exception
   {
      if (t instanceof Error)
         throw (Error) t;
      else if (t instanceof Exception)
         throw (Exception) t;
      throw new RuntimeException(context, t);
   }

   /**
    * Install an MBean
    * 
    * @todo expand the meta data to include a pre-instantiated object
    * @param server the mbean server
    * @param objectName the object name
    * @param metaData the service metadata
    * @param mbean any mbean instance
    * @return the installed instance
    * @throws Exception for any error
    */
   public static ServiceInstance install(MBeanServer server, ObjectName objectName, ServiceMetaData metaData, Object mbean) throws Exception
   {
      if (server == null)
         throw new IllegalArgumentException("Null MBeanServer");
      if (objectName == null)
         throw new IllegalArgumentException("Null ObjectName");
      if (metaData == null && mbean == null)
         throw new IllegalArgumentException("Either metadata or an mbean object must be supplied");
      
      // Check for duplicate
      if (server.isRegistered(objectName))
         throw new RuntimeException("Trying to install an already registered mbean: " + objectName);

      try
      {
         ServiceInstance result = null; 
         
         // No meta data just register directly
         if (metaData == null)
         {
            ObjectInstance instance = server.registerMBean(mbean, objectName);
            result = new ServiceInstance(instance, mbean);
         }
         else
         {
            String code = metaData.getCode();
            if (code == null || code.trim().length() == 0)
               throw new ConfigurationException("Missing or empty code for mbean " + objectName);
            
            String xmbeanDD = metaData.getXMBeanDD();
            
            // Not an XMBean
            if (xmbeanDD == null)
            {
               String interfaceName = metaData.getInterfaceName();
               if (interfaceName != null)
                  result = installStandardMBean(server, objectName, metaData);
               else
                  result = installPlainMBean(server, objectName, metaData);
            }
            // Embedded XMBean Descriptor
            else if (xmbeanDD.length() == 0)
               result = installEmbeddedXMBean(server, objectName, metaData);
            // Reference to external XMBean descriptor
            else
               result = installExternalXMBean(server, objectName, metaData);
         }

         log.debug("Created mbean: " + objectName);
         return result;
      }
      catch (Throwable e)
      {
         Throwable newE = JMXExceptionDecoder.decode(e);

         // didn't work, unregister in case the jmx agent is screwed.
         try
         {
            server.unregisterMBean(objectName);
         }
         catch (Throwable ignore)
         {
         }

         throw rethrow("Unable to createMBean for " + objectName, newE);
      }
   }

   /**
    * Install a StandardMBean
    * 
    * @param server the mbean server
    * @param objectName the object name
    * @param metadata the service metadata
    * @return the installed instance
    * @throws Exception for any error
    */
   private static ServiceInstance installStandardMBean(MBeanServer server, ObjectName objectName, ServiceMetaData metaData) throws Exception
   {
      ObjectName loaderName = metaData.getClassLoaderName();
      ClassLoader loader = server.getClassLoader(loaderName);
      String code = metaData.getCode();
      ServiceConstructorMetaData constructor = metaData.getConstructor();
      String interfaceName = metaData.getInterfaceName();

      Class<?> intf = loader.loadClass(interfaceName);
      log.debug("About to create bean resource: " + objectName + " with code: " + code + " and interface " + interfaceName);
      Object resource = server.instantiate(code,
                                           loaderName,
                                           constructor.getParameters(loader),
                                           constructor.getSignature());

      log.debug("About to register StandardMBean : " + objectName);
      ObjectInstance instance = server.createMBean(StandardMBean.class.getName(),
                                                   objectName,
                                                   loaderName,
                                                   new Object[] { resource, intf },
                                                   new String[] { Object.class.getName() , Class.class.getName() });
      return new ServiceInstance(instance, resource);
   }

   /**
    * Install a plain MBean
    * 
    * @param server the mbean server
    * @param objectName the object name
    * @param metadata the service metadata
    * @return the installed instance
    * @throws Exception for any error
    */
   private static ServiceInstance installPlainMBean(MBeanServer server, ObjectName objectName, ServiceMetaData metaData) throws Exception
   {
      ObjectName loaderName = metaData.getClassLoaderName();
      ClassLoader loader = server.getClassLoader(loaderName);
      String code = metaData.getCode();
      ServiceConstructorMetaData constructor = metaData.getConstructor();
      
      // This is a standard or dynamic mbean
      log.debug("About to create bean: " + objectName + " with code: " + code);
      ObjectInstance instance = server.createMBean(code,
                                                   objectName,
                                                   loaderName,
                                                   constructor.getParameters(loader),
                                                   constructor.getSignature());

      MBeanEntry entry = (MBeanEntry) server.invoke(MBEAN_REGISTRY, "get", new Object[] { objectName }, new String[] { ObjectName.class.getName() });
      Object resource = entry.getResourceInstance();
      return new ServiceInstance(instance, resource);
   }

   /**
    * Install an embedded XMBean
    * 
    * @param server the mbean server
    * @param objectName the object name
    * @param metadata the service metadata
    * @return the installed instance
    * @throws Exception for any error
    */
   private static ServiceInstance installEmbeddedXMBean(MBeanServer server, ObjectName objectName, ServiceMetaData metaData) throws Exception
   {
      ObjectName loaderName = metaData.getClassLoaderName();
      ClassLoader loader = server.getClassLoader(loaderName);
      String code = metaData.getCode();
      ServiceConstructorMetaData constructor = metaData.getConstructor();

      // This is an xmbean with an embedded mbean descriptor
      log.debug("About to create xmbean object: " + objectName + " with code: " + code + " with embedded descriptor");
      //xmbean: construct object first.
      Object resource = server.instantiate(code, 
                                           loaderName,
                                           constructor.getParameters(loader), 
                                           constructor.getSignature());

      String xmbeanCode = metaData.getXMBeanCode();
      Element mbeanDescriptor = metaData.getXMBeanDescriptor();
      Object[] args = { resource, mbeanDescriptor, ServiceConstants.PUBLIC_JBOSSMX_XMBEAN_DTD_1_0 };
      String[] sig = { Object.class.getName(), Element.class.getName(), String.class.getName() };
      ObjectInstance instance = server.createMBean(xmbeanCode,
                                                   objectName,
                                                   loaderName,
                                                   args,
                                                   sig);
      return new ServiceInstance(instance, resource);
   }

   /**
    * Install an external XMBean
    * 
    * @param server the mbean server
    * @param objectName the object name
    * @param metadata the service metadata
    * @return the installed instance
    * @throws Exception for any error
    */
   private static ServiceInstance installExternalXMBean(MBeanServer server, ObjectName objectName, ServiceMetaData metaData) throws Exception
   {
      ObjectName loaderName = metaData.getClassLoaderName();
      ClassLoader loader = server.getClassLoader(loaderName);
      String code = metaData.getCode();
      ServiceConstructorMetaData constructor = metaData.getConstructor();
      String xmbeanDD = metaData.getXMBeanDD();
      
      // This is an xmbean with an external descriptor
      log.debug("About to create xmbean object: " + objectName  + " with code: " + code + " with descriptor: " + xmbeanDD);
      //xmbean: construct object first.
      Object resource = server.instantiate(code, 
                                           loaderName, 
                                           constructor.getParameters(loader), 
                                           constructor.getSignature());
      // Try to find the dd first as a resource then as a URL
      URL xmbeanddUrl = null;
      try
      {
         xmbeanddUrl = resource.getClass().getClassLoader().getResource(xmbeanDD);
      }
      catch (Exception e)
      {
      }

      if (xmbeanddUrl == null)
         xmbeanddUrl = new URL(xmbeanDD);

      String xmbeanCode = metaData.getXMBeanCode();

      //now create the mbean
      Object[] args = { resource, xmbeanddUrl };
      String[] sig = { Object.class.getName(), URL.class.getName() };
      ObjectInstance instance = server.createMBean(xmbeanCode,
                                                   objectName,
                                                   loaderName,
                                                   args,
                                                   sig);
      return new ServiceInstance(instance, resource);
   }

   /**
    * Uninstall an MBean
    * 
    * @param server the mbean server
    * @param objectName the object name
    */
   public static void uninstall(MBeanServer server, ObjectName objectName)
   {
      if (server == null)
         throw new IllegalArgumentException("Null MBeanServer");
      if (objectName == null)
         throw new IllegalArgumentException("Null ObjectName");
      try
      {
         log.debug("Removing mbean from server: " + objectName);
         server.unregisterMBean(objectName);
      }
      catch (Throwable t)
      {
         log.debug("Error unregistering mbean " + objectName, t);
      }
   }
   
   /**
    * Create a new ServiceCreator
    * 
    * @deprecated This is no longer used and will be going away
    * @param server the mbean server
    */
   public ServiceCreator(final MBeanServer server)
   {
      if (server == null)
         throw new IllegalArgumentException("Null MBeanServer");
      this.server = server;
   }
   
   /**
    * Clean shutdown
    */
   public void shutdown()
   {
      this.server = null;
   } 
   
   /**
    * Parses the given configuration document and creates MBean
    * instances in the current MBean server.
    *
    * @deprecated This is no longer used and will be going away
    * @param mbeanName the object name
    * @param loaderName the classloader
    * @param mbeanElement the config
    * @return the created object instance
    * @throws Exception for any error
    */
   public ObjectInstance install(ObjectName mbeanName, ObjectName loaderName, Element mbeanElement) throws Exception
   {
      if (mbeanName == null)
         throw new IllegalArgumentException("Null mbeanName");
      if (mbeanElement == null)
         throw new IllegalArgumentException("Null mbean element");
      
      ServiceMetaDataParser parser = new ServiceMetaDataParser(mbeanElement);
      List<ServiceMetaData> metaDatas = parser.parse();
      if (metaDatas.isEmpty())
         throw new RuntimeException("No mbeans found in passed configuration for " + mbeanName);
      ServiceMetaData metaData = metaDatas.get(0);
      metaData.setClassLoaderName(loaderName);
      ServiceInstance instance = install(server, mbeanName, metaData, null);
      return instance.getObjectInstance();
   }
   
   /**
    * Remove the installed object
    * 
    * @param name the object name
    * @throws Exception for any error
    */
   public void remove(ObjectName name) throws Exception
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      uninstall(server, name);
   }
}
