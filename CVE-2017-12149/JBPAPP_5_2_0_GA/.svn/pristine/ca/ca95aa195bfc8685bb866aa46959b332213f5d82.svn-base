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
package org.jboss.wsf.container.jboss50.deployment.metadata;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.common.jboss.WebserviceDescriptionMetaData;
import org.jboss.metadata.common.jboss.WebserviceDescriptionsMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.WebservicesMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeployment;
import org.jboss.wsf.spi.metadata.j2ee.*;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData.PublishLocationAdapter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Build container independent application meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Apr-2007
 */
public class EJBArchiveMetaDataAdapterEJB3
{
   // logging support
   private static Logger log = Logger.getLogger(EJBArchiveMetaDataAdapterEJB3.class);

   public EJBArchiveMetaData buildMetaData(Deployment dep, DeploymentUnit unit)
   {     
      EJBArchiveMetaData umd = new EJBArchiveMetaData();

      WebServiceDeployment webServiceDeployment = dep.getAttachment(WebServiceDeployment.class);
      buildEnterpriseBeansMetaData(umd, webServiceDeployment);

      JBossMetaData jbMetaData = unit.getAttachment(JBossMetaData.class);
      if (jbMetaData != null)
         buildWebservicesMetaData(umd, jbMetaData);

      return umd;
   }

   private void buildWebservicesMetaData(EJBArchiveMetaData ejbMetaData, JBossMetaData jbMetaData)
   {
      WebservicesMetaData wsMetaData = jbMetaData.getWebservices();
      if (wsMetaData != null)
      {
         String contextRoot = wsMetaData.getContextRoot();
         ejbMetaData.setWebServiceContextRoot(contextRoot);

         ejbMetaData.setPublishLocationAdapter(getPublishLocationAdpater(wsMetaData));

         WebserviceDescriptionsMetaData wsDescriptions = wsMetaData.getWebserviceDescriptions();
         if (wsDescriptions != null)
         {
            if (wsDescriptions.size() > 1)
               log.warn("Multiple <webservice-description> elements not supported");

            if (wsDescriptions.size() > 0)
            {
               WebserviceDescriptionMetaData wsd = wsDescriptions.iterator().next();
               ejbMetaData.setConfigName(wsd.getConfigName());
               ejbMetaData.setConfigFile(wsd.getConfigFile());
            }
         }
      }
   }

   private void buildEnterpriseBeansMetaData(EJBArchiveMetaData jarMetaData, WebServiceDeployment ejb3Deployment)
   {
      List<EJBMetaData> ejbMetaDataList = new ArrayList<EJBMetaData>();
      Iterator<WebServiceDeclaration> it = ejb3Deployment.getServiceEndpoints().iterator();
      while (it.hasNext())
      {
         WebServiceDeclaration container = it.next();

         PortComponentSpec pcMetaData = container.getAnnotation(PortComponentSpec.class);
         MessageDriven mdbMetaData = container.getAnnotation(MessageDriven.class);

         EJBMetaData ejbMetaData = null;

         if(mdbMetaData!=null)
         {
            ejbMetaData = new MDBMetaData();

            ActivationConfigProperty[] props = mdbMetaData.activationConfig();
            if (props != null)
            {
               String destination = getActivationProperty("destination", props);
               if (destination != null)
               {                  
                  ((MDBMetaData)ejbMetaData).setDestinationJndiName(destination);
               }
            }
         }
         else
         {
            ejbMetaData = new SLSBMetaData();
         }

         if (ejbMetaData != null)
         {
            ejbMetaData.setEjbName(container.getComponentName());
            ejbMetaData.setEjbClass(container.getComponentClassName());

            if (pcMetaData != null)
            {
               ejbMetaData.setPortComponentName(pcMetaData.portComponentName());
               ejbMetaData.setPortComponentURI(pcMetaData.portComponentURI());
               EJBSecurityMetaData smd = new EJBSecurityMetaData();
               smd.setAuthMethod(pcMetaData.authMethod());
               smd.setTransportGuarantee(pcMetaData.transportGuarantee());
               smd.setSecureWSDLAccess(pcMetaData.secureWSDLAccess());
               ejbMetaData.setSecurityMetaData(smd);
            }
            
            ejbMetaDataList.add(ejbMetaData);
         }
      }
      
      jarMetaData.setEnterpriseBeans(ejbMetaDataList);
   }

   private String getActivationProperty(String name, ActivationConfigProperty[] props)
   {
      String result = null;
      for(ActivationConfigProperty p : props)
      {
         if(p.propertyName().equals(name))
         {
            result = p.propertyValue();
            break;
         }
      }

      return result;
   }

   private PublishLocationAdapter getPublishLocationAdpater(final WebservicesMetaData wsMetaData)
   {
      return new PublishLocationAdapter() {
         public String getWsdlPublishLocationByName(String name)
         {
            String wsdlPublishLocation = null;
            WebserviceDescriptionsMetaData wsDescriptions = wsMetaData.getWebserviceDescriptions();
            if (wsDescriptions != null && wsDescriptions.get(name) != null)
            {
               WebserviceDescriptionMetaData wsdMetaData = wsDescriptions.get(name);
               wsdlPublishLocation = wsdMetaData.getWsdlPublishLocation();
            }
            return wsdlPublishLocation;
         }
      };
   }

}
