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
package org.jboss.management.j2ee.factory;

import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb.EjbModule;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.EJB;
import org.jboss.management.j2ee.EJBModule;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * A factory for mapping EJBDeployer deployments to EJBModule
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 62057 $
 */
public class EJBModuleFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(EJBModuleFactory.class);
   private static Map moduleServiceToMgmtMap = new ConcurrentReaderHashMap();
   private Map deploymentToModuleNameMap = new ConcurrentReaderHashMap();
   private Map containerToModuleNameMap = new ConcurrentReaderHashMap();

   static ObjectName getEJBModuleName(ObjectName ejbModuleService)
   {
      ObjectName jsr77Name = (ObjectName) moduleServiceToMgmtMap.get(ejbModuleService);
      return jsr77Name;
   }

   /**
    * Create JSR-77 EJBModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public ObjectName create(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return null;

      DeploymentInfo di = (DeploymentInfo) data;
      String ejbJarName = di.shortName;
      ObjectName ejbModuleService = di.deployedObject;
      ObjectName jsr77Name = EJBModule.create(server,
              FactoryUtils.findEarParent(di),
              ejbJarName,
              di.localUrl,
              ejbModuleService);
      moduleServiceToMgmtMap.put(ejbModuleService, jsr77Name);
      deploymentToModuleNameMap.put(di, jsr77Name);
      log.debug("Created module: " + jsr77Name);
      Iterator ejbs = di.mbeans.iterator();
      while (ejbs.hasNext())
      {
         ObjectName containerName = (ObjectName) ejbs.next();
         createEJB(server, containerName);
      }
      return jsr77Name;
   }

   /**
    * Destroy JSR-77 EJBModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public void destroy(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return;

      DeploymentInfo di = (DeploymentInfo) data;
      ObjectName jsr77Name = (ObjectName) deploymentToModuleNameMap.remove(di);

      log.debug("Destroy module: " + jsr77Name);
      Iterator ejbs = di.mbeans.iterator();
      while (ejbs.hasNext())
      {
         ObjectName containerName = (ObjectName) ejbs.next();
         destroyEJB(server, containerName);
      }
      
      if (jsr77Name != null)
      {
         EJBModule.destroy(server, jsr77Name);
      }

      ObjectName ejbModuleService = di.deployedObject;
      if (ejbModuleService != null)
         containerToModuleNameMap.remove(ejbModuleService);
   }

   /**
    Create an EJB mbean for the container

    @param server - the deployment server
    @param containerName - the internal ejb container jmx name
    @return JSR77 ObjectName of the EJB mbean subtype
    */
   public ObjectName createEJB(MBeanServer server, ObjectName containerName)
   {
      ObjectName jsr77Name = null;
      try
      {
         BeanMetaData metaData = (BeanMetaData) server.getAttribute(containerName, "BeanMetaData");
         EjbModule ejbModule = (EjbModule) server.getAttribute(containerName, "EjbModule");
         ObjectName ejbModName = EJBModuleFactory.getEJBModuleName(ejbModule.getServiceName());
         String ejbName = metaData.getEjbName();
         String jndiName = metaData.getJndiName();
         String localJndiName = metaData.getLocalJndiName();
         int type = EJB.STATELESS_SESSION_BEAN;
         if (metaData.isSession())
         {
            SessionMetaData smetaData = (SessionMetaData) metaData;
            if (smetaData.isStateful())
               type = EJB.STATEFUL_SESSION_BEAN;
         }
         else if (metaData.isMessageDriven())
            type = EJB.MESSAGE_DRIVEN_BEAN;
         else
            type = EJB.ENTITY_BEAN;

         
         jsr77Name = EJB.create(server, ejbModName, containerName, type,
            ejbName, jndiName, localJndiName);
         containerToModuleNameMap.put(containerName, jsr77Name);
         log.debug("Create container: " + containerName + ", module: " + jsr77Name);
      }
      catch (Exception e)
      {
         log.debug("", e);
      }

      return jsr77Name;
   }

   /**
    * Destory JSR-77 J2EEApplication
    *
    * @param server the MBeanServer context
    * @param containerName   arbitrary data associated with the creation context
    */
   public void destroyEJB(MBeanServer server, ObjectName containerName)
   {
      ObjectName jsr77Name = (ObjectName) containerToModuleNameMap.get(containerName);

      log.debug("Destroy container: " + containerName + ", module: " + jsr77Name);
      if (jsr77Name != null)
      {
         EJB.destroy(server, jsr77Name);
      }
   }

}
