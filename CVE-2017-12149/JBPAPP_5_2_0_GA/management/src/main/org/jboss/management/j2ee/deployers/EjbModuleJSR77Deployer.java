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
package org.jboss.management.j2ee.deployers;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb.EjbModule;
import org.jboss.management.j2ee.EJB;
import org.jboss.management.j2ee.EJBModule;
import org.jboss.management.j2ee.factory.FactoryUtils;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * Ejb module jsr77 view.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class EjbModuleJSR77Deployer extends AbstractVFSJSR77Deployer<JBossMetaData>
{
   public EjbModuleJSR77Deployer()
   {
      super(JBossMetaData.class);
   }

   protected void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossMetaData metaData) throws Throwable
   {
      String ejbJarName = unit.getSimpleName();
      ObjectName ejbModuleService = extractRootObjectName(server, unit, metaData); // TODO
      ObjectName jsr77Name = EJBModule.create(server, FactoryUtils.findEarParent(unit), ejbJarName, unit.getRoot().toURL(), ejbModuleService);
      putObjectName(unit, ejbModuleService.getCanonicalName(), jsr77Name);
      putObjectName(unit, EJBModule.class.getName(), jsr77Name);
      log.debug("Created module: " + jsr77Name);
      Iterable<ObjectName> ejbs = extractComponentObjectNames(server, unit, metaData); // TODO
      for (ObjectName containerName : ejbs)
      {
         createEJB(server, unit, containerName);
      }
   }

   protected void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossMetaData metaData)
   {
      ObjectName jsr77Name = removeObjectName(unit, EJBModule.class.getName());
      log.debug("Destroy module: " + jsr77Name);
      Iterable<ObjectName> ejbs = extractComponentObjectNames(server, unit, metaData); // TODO
      for (ObjectName containerName : ejbs)
      {
         destroyEJB(server, unit, containerName);
      }
      if (jsr77Name != null)
      {
         EJBModule.destroy(server, jsr77Name);
      }
      ObjectName ejbModuleService = extractRootObjectName(server, unit, metaData); // TODO
      if (ejbModuleService != null)
         removeObjectName(unit, ejbModuleService.getCanonicalName());
   }

   /**
    * Create an EJB mbean for the container
    *
    * @param server - the deployment server
    * @param unit the deployment unit
    * @param containerName - the internal ejb container jmx name
    * @return JSR77 ObjectName of the EJB mbean subtype
    */
   @SuppressWarnings("deprecation")
   public ObjectName createEJB(MBeanServer server, VFSDeploymentUnit unit, ObjectName containerName)
   {
      ObjectName jsr77Name = null;
      try
      {
         BeanMetaData metaData = (BeanMetaData) server.getAttribute(containerName, "BeanMetaData");
         EjbModule ejbModule = (EjbModule) server.getAttribute(containerName, "EjbModule");
         ObjectName ejbModName = getObjectName(unit, ejbModule.getServiceName().getCanonicalName());
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

         jsr77Name = EJB.create(server, ejbModName, containerName, type, ejbName, jndiName, localJndiName);
         putObjectName(unit, containerName.getCanonicalName(), jsr77Name);
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
    * @param unit the deployment unit
    * @param containerName   arbitrary data associated with the creation context
    */
   public void destroyEJB(MBeanServer server, VFSDeploymentUnit unit, ObjectName containerName)
   {
      ObjectName jsr77Name = removeObjectName(unit, containerName.getCanonicalName());
      log.debug("Destroy container: " + containerName + ", module: " + jsr77Name);
      if (jsr77Name != null)
      {
         EJB.destroy(server, jsr77Name);
      }
   }
}