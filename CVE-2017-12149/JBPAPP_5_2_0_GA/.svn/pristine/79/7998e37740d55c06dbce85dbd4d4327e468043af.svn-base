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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.common.jboss.WebserviceDescriptionMetaData;
import org.jboss.metadata.common.jboss.WebserviceDescriptionsMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.WebservicesMetaData;
import org.jboss.metadata.javaee.spec.PortComponent;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBSecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.MDBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.SLSBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData.PublishLocationAdapter;

/**
 * Build container independent application meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class EJBArchiveMetaDataAdapterEJB21
{
   // logging support
   private static Logger log = Logger.getLogger(EJBArchiveMetaDataAdapterEJB21.class);

   public EJBArchiveMetaData buildMetaData(Deployment dep, DeploymentUnit unit)
   {
      JBossMetaData jbossMetaData = unit.getAttachment(JBossMetaData.class);
      dep.addAttachment(JBossMetaData.class, jbossMetaData);
      
      EJBArchiveMetaData ejbMetaData = new EJBArchiveMetaData();
      buildEnterpriseBeansMetaData(ejbMetaData, jbossMetaData);
      buildWebservicesMetaData(ejbMetaData, jbossMetaData);
      ejbMetaData.setSecurityDomain(jbossMetaData.getSecurityDomain());
      
      return ejbMetaData;
   }

   private void buildEnterpriseBeansMetaData(EJBArchiveMetaData ejbMetaData, JBossMetaData jbossMetaData)
   {
      List<EJBMetaData> targetBeans = new ArrayList<EJBMetaData>();
      JBossEnterpriseBeansMetaData sourceBeans = jbossMetaData.getEnterpriseBeans();
      Iterator<JBossEnterpriseBeanMetaData> it = sourceBeans.iterator();
      while (it.hasNext())
      {
         JBossEnterpriseBeanMetaData bmd = it.next();
         buildBeanMetaData(targetBeans, bmd);
      }
      ejbMetaData.setEnterpriseBeans(targetBeans);
   }

   private void buildWebservicesMetaData(EJBArchiveMetaData ejbMetaData, JBossMetaData jbossMetaData)
   {
      WebservicesMetaData webservices = jbossMetaData.getWebservices();
      if (webservices != null)
      {
         String contextRoot = webservices.getContextRoot();
         ejbMetaData.setWebServiceContextRoot(contextRoot);
         
         ejbMetaData.setPublishLocationAdapter(getPublishLocationAdpater(webservices));

         WebserviceDescriptionsMetaData wsDescriptions = webservices.getWebserviceDescriptions();
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

   private PublishLocationAdapter getPublishLocationAdpater(final WebservicesMetaData wsMetaData)
   {
      return new PublishLocationAdapter()
      {
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

   private EJBMetaData buildBeanMetaData(List<EJBMetaData> ejbBeans, JBossEnterpriseBeanMetaData jbossBeansMetaData)
   {
      EJBMetaData targetBean = null;
      if (jbossBeansMetaData.isSession())
      {
         targetBean = new SLSBMetaData();
         JBossSessionBeanMetaData jbossSessionBean = (JBossSessionBeanMetaData)jbossBeansMetaData;
         
         targetBean.setEjbName(jbossSessionBean.getEjbName());
         targetBean.setEjbClass(jbossSessionBean.getEjbClass());
         targetBean.setServiceEndpointInterface(jbossSessionBean.getServiceEndpoint());
         targetBean.setHome(jbossSessionBean.getHome());
         targetBean.setLocalHome(jbossSessionBean.getLocalHome());
         targetBean.setJndiName(jbossSessionBean.determineJndiName());
         targetBean.setLocalJndiName(jbossBeansMetaData.determineLocalJndiName());
         
         PortComponent pcmd = jbossSessionBean.getPortComponent();
         if (pcmd != null)
         {
            targetBean.setPortComponentName(pcmd.getPortComponentName());
            targetBean.setPortComponentURI(pcmd.getPortComponentURI());
            EJBSecurityMetaData smd = new EJBSecurityMetaData();
            smd.setAuthMethod(pcmd.getAuthMethod());
            smd.setTransportGuarantee(pcmd.getTransportGuarantee());
            smd.setSecureWSDLAccess(pcmd.getSecureWSDLAccess());
            targetBean.setSecurityMetaData(smd);
         }
      }
      else if (jbossBeansMetaData.isMessageDriven())
      {
         targetBean = new MDBMetaData();
         JBossMessageDrivenBeanMetaData jbossMessageBean = (JBossMessageDrivenBeanMetaData)jbossBeansMetaData;
         
         targetBean.setEjbName(jbossMessageBean.getEjbName());
         targetBean.setEjbClass(jbossMessageBean.getEjbClass());
         //targetBean.setServiceEndpointInterface(???);
         //targetBean.setJndiName(???);
         targetBean.setLocalJndiName(jbossBeansMetaData.getLocalJndiName());
         ((MDBMetaData)targetBean).setDestinationJndiName(jbossMessageBean.getDestinationJndiName());
      }

      if (targetBean != null)
         ejbBeans.add(targetBean);
      
      return targetBean;
   }
}
