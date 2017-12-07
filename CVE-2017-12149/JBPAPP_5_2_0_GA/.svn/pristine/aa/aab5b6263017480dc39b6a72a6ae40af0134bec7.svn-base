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
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Service;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;

/**
 * A deployer JAXRPC JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class JAXRPCDeployerHookPreJSE extends AbstractDeployerHookJSE
{

   /** Get the deployemnt type this deployer can handle
    */
   public DeploymentType getDeploymentType()
   {
      return DeploymentType.JAXRPC_JSE;
   }

   /**
    * Create an endpoint for every servlet-link in webservices.xml
    */
   @Override
   public Deployment createDeployment(DeploymentUnit unit)
   {
      ArchiveDeployment dep = newDeployment(unit);
      dep.setRootFile(new VirtualFileAdaptor(((VFSDeploymentUnit)unit).getRoot()));
      dep.setRuntimeClassLoader(null); 
      dep.setType(getDeploymentType());

      Service service = dep.getService();

      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      if (webMetaData == null)
         throw new IllegalStateException("Deployment unit does not contain web meta data");

      WebservicesMetaData wsMetaData = getWebservicesMetaData(unit);
      if (wsMetaData == null)
         throw new IllegalStateException("Deployment unit does not contain webservices meta data");

      // Copy the attachments
      dep.addAttachment(WebservicesMetaData.class, wsMetaData);
      dep.addAttachment(JBossWebMetaData.class, webMetaData);

      for (WebserviceDescriptionMetaData wsd : wsMetaData.getWebserviceDescriptions())
      {
         for (PortComponentMetaData pcmd : wsd.getPortComponents())
         {
            String servletLink = pcmd.getServletLink();
            if (servletLink == null)
               throw new IllegalStateException("servlet-link cannot be null");

            ServletMetaData servlet = getServletForName(webMetaData, servletLink);
            String targetBean = getTargetBean(servlet);

            // Create the endpoint
            Endpoint ep = newEndpoint(targetBean);
            ep.setShortName(servletLink);
            service.addEndpoint(ep);
         }
      }

      return dep;
   }

   private ServletMetaData getServletForName(JBossWebMetaData wmd, String servletLink)
   {
      for (JBossServletMetaData servlet : wmd.getServlets())
      {
         if (servletLink.equals(servlet.getName()))
         {
            return servlet;
         }
      }
      throw new IllegalStateException("Cannot find servlet for link: " + servletLink);
   }

   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      if (super.isWebServiceDeployment(unit) == false)
         return false;

      WebservicesMetaData wsMetaData = getWebservicesMetaData(unit);
      return wsMetaData != null;
   }
}
