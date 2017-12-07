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
package org.jboss.test.system.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * SimpleSARDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SimpleSARDeployer
{
   private static Logger log = Logger.getLogger(SimpleSARDeployer.class);
   
   public static ObjectName classLoaderObjectName = ObjectNameFactory.create("test:classloader=test");
   
   private ServiceControllerMBean serviceController;

   private List<ObjectName> deployed = new CopyOnWriteArrayList<ObjectName>();

   private List<ObjectName> tempDeployed = new CopyOnWriteArrayList<ObjectName>();
   
   private DocumentBuilder parser;
   
   public SimpleSARDeployer(MBeanServer server, ServiceControllerMBean serviceController) throws Exception
   {
      this.serviceController = serviceController;
      
      TestClassLoader classLoader = new TestClassLoader();
      server.registerMBean(classLoader, classLoaderObjectName);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
   }

   public List<ObjectName> deploy(URL url, boolean temporary) throws Exception
   {
      long start = System.currentTimeMillis();
      
      List<ObjectName> result = doInstall(url, temporary);

      try
      {
         create(result);
         try
         {
            start(result);
         }
         catch (Throwable t)
         {
            stop(result);
            throw t;
         }
      }
      catch (Throwable t)
      {
         destroy(result);
         remove(result);
         DeploymentException.rethrowAsDeploymentException("Error", t);
      }
      
      log.debug("Deployed " + url + " took " + (System.currentTimeMillis() - start) + "ms");
      return result;
   }
   
   public void uninstall()
   {
      undeploy(deployed);
   }
   
   public void uninstallTemporary()
   {
      undeploy(tempDeployed);
   }
   
   public void undeploy(List<ObjectName> objectNames)
   {
      stop(objectNames);
      destroy(objectNames);
      remove(objectNames);
   }
   
   public void create(List<ObjectName> services) throws Exception
   {
      for (ObjectName name: services)
         serviceController.create(name);
   }
   
   public void start(List<ObjectName> services) throws Exception
   {
      for (ObjectName name: services)
         serviceController.start(name);
   }
   
   public void stop(List<ObjectName> services)
   {
      for (ListIterator<ObjectName> i = services.listIterator(services.size()); i.hasPrevious();)
      {
         ObjectName name = i.previous();
         try
         {
            serviceController.stop(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public void destroy(List<ObjectName> services)
   {
      for (ListIterator<ObjectName> i = services.listIterator(services.size()); i.hasPrevious();)
      {
         ObjectName name = i.previous();
         try
         {
            serviceController.destroy(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public void remove(List<ObjectName> services)
   {
      for (ObjectName name: services)
      {
         deployed.remove(name);
         tempDeployed.remove(name);
         try
         {
            serviceController.remove(name);
         }
         catch (Throwable ignored)
         {
            log.debug("Ignored", ignored);
         }
      }
   }
   
   public List<ObjectName> install(URL url) throws Exception
   {
      long start = System.currentTimeMillis();
      
      List<ObjectName> result = doInstall(url, true);
      
      log.debug("Deployed " + url + " took " + (System.currentTimeMillis() - start) + "ms");
      return result;
   }
   
   public void uninstall(List<ObjectName> services)
   {
      remove(services);
   }
   
   protected List<ObjectName> doInstall(URL url, boolean temporary) throws Exception
   {
      List<ObjectName> result = null;
      
      Element element = null;

      try
      {
         InputStream stream = url.openStream();
         try
         {
            InputSource is = new InputSource(stream);
            is.setSystemId(url.toString());
            parser.setEntityResolver(new JBossEntityResolver());

            Document document = parser.parse(is);
            element = document.getDocumentElement();
         }
         finally
         {
            stream.close();
         }

         result = serviceController.install(element, classLoaderObjectName);

         deployed.addAll(result);
         if (temporary)
            tempDeployed.addAll(result);
      }
      catch (Exception e)
      {
         log.debug("Error deploying: " + url + ": " + e);
         throw e;
      }
      
      return result;
   }
}
