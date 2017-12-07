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

import java.util.Collections;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * Abstract jsr77 deployer.
 *
 * @param <T> exact input type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractJSR77Deployer<T> extends AbstractSimpleRealDeployer<T>
{
   private boolean requiresVFSDeployment;

   private MBeanServer server;

   protected AbstractJSR77Deployer(Class<T> input)
   {
      super(input);
      setOutput(ObjectName.class);
   }

   /**
    * Inject the mbean server.
    *
    * @param server the mbean server
    */
   @Inject(bean = "JMXKernel", property = "mbeanServer")
   public void setServer(MBeanServer server)
   {
      this.server = server;
   }

   /**
    * Set the requires vfs flag.
    *
    * @param requiresVFSDeployment the requires vfs flag
    */
   public void setRequiresVFSDeployment(boolean requiresVFSDeployment)
   {
      this.requiresVFSDeployment = requiresVFSDeployment;
   }

   /**
    * Deploy jsr77 view.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    * @throws Throwable for any error
    */
   protected abstract void deployJsr77(MBeanServer server, DeploymentUnit unit, T metaData) throws Throwable;

   /**
    * Undeploy jsr77 view.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    */
   protected abstract void undeployJsr77(MBeanServer server, DeploymentUnit unit, T metaData);

   /**
    * Put top level object name.
    *
    * @param unit the deployment unit
    * @param attachmentName the attachment name
    * @param name the object name
    */
   protected static void putObjectName(DeploymentUnit unit, String attachmentName, ObjectName name)
   {
      // something in org.jboss.management probably returned null, see log's debug for more info
      if (name != null)
         unit.addAttachment(attachmentName + "." + ObjectName.class.getSimpleName(), name, ObjectName.class);
   }

   /**
    * Get object name from attachment.
    *
    * @param unit the deployment unit
    * @param attachmentName the attachment name
    * @return object name from attachment
    */
   protected static ObjectName getObjectName(DeploymentUnit unit, String attachmentName)
   {
      return unit.getAttachment(attachmentName + "." + ObjectName.class.getSimpleName(), ObjectName.class);
   }

   /**
    * Remove object name from attachment.
    *
    * @param unit the deployment unit
    * @param attachmentName the attachment name
    * @return object name from attachment
    */
   protected static ObjectName removeObjectName(DeploymentUnit unit, String attachmentName)
   {
      return unit.removeAttachment(attachmentName + "." + ObjectName.class.getSimpleName(), ObjectName.class);
   }

   /**
    * Extract root object name from parameters.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    * @return root's object name
    * @throws IllegalArgumentException for any error
    */
   protected ObjectName extractRootObjectName(MBeanServer server, DeploymentUnit unit, T metaData)
   {
      try
      {
         // TODO - fake object name, this method should be made abstract!
         return new ObjectName("jboss.service:type=" + metaData.getClass().getSimpleName() + ",unit=" + unit.getSimpleName());
      }
      catch (Throwable t)
      {
         throw new IllegalArgumentException("Cannot extract root ObjectName.", t);
      }
   }

   /**
    * Extract object names for components from parameters.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    * @return root's object name
    * @throws IllegalArgumentException for any error
    */
   protected Iterable<ObjectName> extractComponentObjectNames(MBeanServer server, DeploymentUnit unit, T metaData)
   {
      // TODO - empty object names, this method should be made abstract!
      return Collections.emptySet();
   }

   public void deploy(DeploymentUnit unit, T metaData) throws DeploymentException
   {
      if (server != null && unit instanceof VFSDeploymentUnit == requiresVFSDeployment)
      {
         try
         {
            deployJsr77(server, unit, metaData);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Cannot deploy jsr77.", t);
         }
      }
      else if (log.isTraceEnabled())
      {
         log.trace("Missing mbean server or deployment unit type mismatch.");
      }
   }

   public void undeploy(DeploymentUnit unit, T metaData)
   {
      if (server != null && unit instanceof VFSDeploymentUnit == requiresVFSDeployment)
         undeployJsr77(server, unit, metaData);
      else if (log.isTraceEnabled())
         log.trace("Missing mbean server or deployment unit type mismatch.");
   }
}
