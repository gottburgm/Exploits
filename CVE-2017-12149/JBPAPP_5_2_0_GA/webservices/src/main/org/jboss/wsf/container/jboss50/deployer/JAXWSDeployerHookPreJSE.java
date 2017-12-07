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
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Service;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * A deployer JAXWS JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class JAXWSDeployerHookPreJSE extends AbstractDeployerHookJSE
{

   /** Get the deployment type this deployer can handle
    */
   public DeploymentType getDeploymentType()
   {
      return DeploymentType.JAXWS_JSE;
   }

   @Override
   public Deployment createDeployment(DeploymentUnit unit)
   {
      ArchiveDeployment dep = newDeployment(unit);
      dep.setRootFile(new VirtualFileAdaptor(((VFSDeploymentUnit)unit).getRoot()));     
      dep.setType(getDeploymentType());

      Service service = dep.getService();

      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      if (webMetaData == null)
         throw new IllegalStateException("Deployment unit does not contain web meta data");

      // Copy the attachments
      dep.addAttachment(JBossWebMetaData.class, webMetaData);

      List<ServletMetaData> servlets = getRelevantServlets(webMetaData, unit.getClassLoader());
      for (ServletMetaData servlet : servlets)
      {
         String servletName = servlet.getName();
         String targetBean = getTargetBean(servlet);

         // Create the endpoint
         Endpoint ep = newEndpoint(targetBean);
         ep.setShortName(servletName);
         service.addEndpoint(ep);
      }

      return dep;
   }

   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      if (super.isWebServiceDeployment(unit) == false)
         return false;

      boolean isWebServiceDeployment = false;
      try
      {
         JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
         List<ServletMetaData> servlets = getRelevantServlets(webMetaData, unit.getClassLoader());
         isWebServiceDeployment = servlets.size() > 0;
      }
      catch (Exception ex)
      {
         log.error("Cannot process web deployment", ex);
      }

      return isWebServiceDeployment;
   }

   private List<ServletMetaData> getRelevantServlets(JBossWebMetaData webMetaData, ClassLoader loader)
   {
      List<ServletMetaData> servlets = new ArrayList<ServletMetaData>();
      for (ServletMetaData servlet : webMetaData.getServlets())
      {
         String servletClassName = getTargetBean(servlet);

         // Skip JSPs
         if (servletClassName == null || servletClassName.length() == 0)
            continue;

         try
         {
            Class<?> servletClass = loader.loadClass(servletClassName.trim());
            boolean isWebService = servletClass.isAnnotationPresent(WebService.class);
            boolean isWebServiceProvider = servletClass.isAnnotationPresent(WebServiceProvider.class);
            if (isWebService || isWebServiceProvider)
               servlets.add(servlet);
         }
         catch (ClassNotFoundException ex)
         {
            log.warn("Cannot load servlet class: " + servletClassName);
            continue;
         }
      }
      return servlets;
   }
}
