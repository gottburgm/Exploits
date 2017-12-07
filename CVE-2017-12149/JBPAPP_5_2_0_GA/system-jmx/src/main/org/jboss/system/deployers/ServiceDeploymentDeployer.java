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

import java.util.List;

import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.util.xml.DOMWriter;
import org.w3c.dom.Element;

/**
 * ServiceDeployer.<p>
 * 
 * This deployer is responsible for deploying services of
 * type {@link ServiceDeployment}.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceDeploymentDeployer extends AbstractComponentDeployer<ServiceDeployment, ServiceMetaData>
{
   /**
    * Create a new ServiceDeploymentDeployer.
    */
   public ServiceDeploymentDeployer()
   {
      setDeploymentVisitor(new ServiceDeploymentVisitor());
      setComponentVisitor(new ServiceMetaDataVisitor());
   }

   protected static void addServiceComponent(DeploymentUnit unit, ServiceMetaData service)
   {
      ObjectName objectName = service.getObjectName();
      String name = objectName.getCanonicalName();
      DeploymentUnit component = unit.addComponent(name);
      component.addAttachment(ServiceMetaData.class.getName(), service);
   }

   protected static void removeServiceComponent(DeploymentUnit unit, ServiceMetaData service)
   {
      ObjectName objectName = service.getObjectName();
      String name = objectName.getCanonicalName();
      unit.removeComponent(name);
   }
   
   /**
    * ServiceDeploymentVisitor.
    */
   public class ServiceDeploymentVisitor implements DeploymentVisitor<ServiceDeployment>
   {
      public Class<ServiceDeployment> getVisitorType()
      {
         return ServiceDeployment.class;
      }

      public void deploy(DeploymentUnit unit, ServiceDeployment deployment) throws DeploymentException
      {
         try
         {
            List<ServiceMetaData> services = deployment.getServices();
            if (services == null)
            {
               Element config = deployment.getConfig();
               if (config == null)
               {
                  log.debug("Service deployment has no services: " + deployment.getName());
                  return;
               }
               if (log.isDebugEnabled())
               {
                  String docStr = DOMWriter.printNode(config, true);
                  int index = docStr.toLowerCase().indexOf("password"); 
                  if (index != -1)
                  {
                     docStr = maskPasswords(docStr, index);
                  }
                  log.debug(docStr);
               }
               ServiceMetaDataParser parser = new ServiceMetaDataParser(config);
               services = parser.parse();
               deployment.setServices(services);
            }

            if (services == null || services.isEmpty())
               return;
            
            for (ServiceMetaData service : services)
               addServiceComponent(unit, service);
         }
         catch (Throwable t)
         {
            throw DeploymentException.rethrowAsDeploymentException("Error deploying: " + deployment.getName(), t);
         }
      }

      public void undeploy(DeploymentUnit unit, ServiceDeployment deployment)
      {
         List<ServiceMetaData> services = deployment.getServices();
         if (services == null)
            return;
         
         for (ServiceMetaData service : services)
         {
            ObjectName objectName = service.getObjectName();
            String name = objectName.getCanonicalName();
            unit.removeComponent(name);
         }
      }
   }

   /**
    * ServiceMetaDataVisitor.
    */
   public static class ServiceMetaDataVisitor implements DeploymentVisitor<ServiceMetaData>
   {
      public Class<ServiceMetaData> getVisitorType()
      {
         return ServiceMetaData.class;
      }

      public void deploy(DeploymentUnit unit, ServiceMetaData deployment) throws DeploymentException
      {
         addServiceComponent(unit, deployment);
      }

      public void undeploy(DeploymentUnit unit, ServiceMetaData deployment)
      {
         removeServiceComponent(unit, deployment);
      }
   }
   
   /**
    * Masks passwords so they are not visible in the log.
    * 
    * @param original <code>String</code> plain-text passwords
    * @param index index where the password keyword was found
    * @return modified <code>String</code> with masked passwords
    */
   private String maskPasswords(String original, int index)
   {
      StringBuilder sb = new StringBuilder(original);
      String modified = null;
      int startPasswdStringIndex = sb.indexOf(">", index);
      if (startPasswdStringIndex != -1)
      {
         // checks if the keyword 'password' was not in a comment
         if (sb.charAt(startPasswdStringIndex - 1) != '-')
         {
            int endPasswdStringIndex = sb.indexOf("<", startPasswdStringIndex);
            if (endPasswdStringIndex != -1) // shouldn't happen, but check anyway
            {
               sb.replace(startPasswdStringIndex + 1, endPasswdStringIndex, "****");
            }
         }
         modified = sb.toString();
         // unlikely event of more than one password
         index = modified.toLowerCase().indexOf("password", startPasswdStringIndex);
         if (index != -1)
            return maskPasswords(modified, index);
         return modified;
      }
      return original;
   }
}
