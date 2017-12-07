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
package org.jboss.wsf.container.jboss50.deployer;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeployment;
import org.jboss.wsf.container.jboss50.invocation.InvocationHandlerEJB3;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;
import java.util.Iterator;

/**
 * A deployer JAXWS EJB3 Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class JAXWSDeployerHookEJB3 extends AbstractDeployerHookEJB
{
   /** Get the deployemnt type this deployer can handle
    */
   public DeploymentType getDeploymentType()
   {
      return DeploymentType.JAXWS_EJB3;
   }

   @Override
   public Deployment createDeployment(DeploymentUnit unit)
   {
      ArchiveDeployment dep = newDeployment(unit);
      dep.setRootFile(new VirtualFileAdaptor(((VFSDeploymentUnit)unit).getRoot()));
      dep.setRuntimeClassLoader(unit.getClassLoader());
      dep.setType(getDeploymentType());

      Service service = dep.getService();

      WebServiceDeployment webServiceDeployment = unit.getAttachment(WebServiceDeployment.class);
      if (webServiceDeployment == null)
         throw new IllegalStateException("Deployment unit does not contain webServiceDeployment");

      // Copy the attachments
      dep.addAttachment(WebServiceDeployment.class, webServiceDeployment);

      Iterator<WebServiceDeclaration> it = webServiceDeployment.getServiceEndpoints().iterator();
      while (it.hasNext())
      {
         WebServiceDeclaration container = it.next();
         if (isWebServiceBean(container))
         {
            String ejbName = container.getComponentName();
            String epBean = container.getComponentClassName();

            // Create the endpoint
            Endpoint ep = newEndpoint(epBean);
            ep.setShortName(ejbName);

            String containName = container.getContainerName();
            if(null==containName)
               throw new IllegalArgumentException("Target container name not set");
            ep.setProperty(InvocationHandlerEJB3.CONTAINER_NAME, containName);

            service.addEndpoint(ep);
         }
      }

      return dep;
   }

   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      WebServiceDeployment webServiceDeployment = unit.getAttachment(WebServiceDeployment.class);
      if (null == webServiceDeployment )
         return false;

      boolean isWebServiceDeployment = false;

      Iterator<WebServiceDeclaration> it = webServiceDeployment.getServiceEndpoints().iterator();
      while (it.hasNext())
      {
         WebServiceDeclaration container = it.next();
         if (isWebServiceBean(container))
         {
            isWebServiceDeployment = true;
            break;
         }
      }

      return isWebServiceDeployment;
   }

   private boolean isWebServiceBean(WebServiceDeclaration container)
   {
      boolean isWebServiceBean = false;
      boolean isWebService = container.getAnnotation(WebService.class) != null;
      boolean isWebServiceProvider = container.getAnnotation(WebServiceProvider.class) != null;
      isWebServiceBean = isWebService || isWebServiceProvider;

      return isWebServiceBean;
   }
}
