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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.common.jboss.WebserviceDescriptionMetaData;
import org.jboss.metadata.common.jboss.WebserviceDescriptionsMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ear.spec.ModuleMetaData;
import org.jboss.metadata.ear.spec.WebModuleMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionsMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData.PublishLocationAdapter;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData.JSEResourceCollection;

/**
 * Build container independent web meta data
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class JSEArchiveMetaDataAdapter
{
   // logging support
   private static Logger log = Logger.getLogger(JSEArchiveMetaDataAdapter.class);
   
   public JSEArchiveMetaData buildMetaData(Deployment dep, DeploymentUnit unit)
   {
      String contextRoot = null;
      
      JBossWebMetaData jbossWebMetaData = unit.getAttachment(JBossWebMetaData.class);
      dep.addAttachment(JBossWebMetaData.class, jbossWebMetaData);

      if (unit.getParent() != null)
      {
         JBossAppMetaData appmd = unit.getParent().getAttachment(JBossAppMetaData.class);
         if (appmd != null)
         {
            ModuleMetaData module = appmd.getModule(dep.getSimpleName());
            if (module != null)
            {
               WebModuleMetaData web = (WebModuleMetaData) module.getValue();
               contextRoot = web.getContextRoot();
            }
         }
      }
      
      if (contextRoot == null)
         contextRoot = jbossWebMetaData.getContextRoot();
      
      JSEArchiveMetaData umd = new JSEArchiveMetaData();
      umd.setContextRoot(contextRoot);
      umd.setServletMappings(getServletMappings(jbossWebMetaData));
      umd.setServletClassNames(getServletClassMap(jbossWebMetaData));
      umd.setSecurityDomain(jbossWebMetaData.getSecurityDomain());
      umd.setPublishLocationAdapter(getPublishLocationAdpater(jbossWebMetaData));
      umd.setSecurityMetaData(getSecurityMetaData(jbossWebMetaData.getSecurityContraints()));

      setConfigNameAndFile(umd, jbossWebMetaData);
      
      return umd;
   }

   private void setConfigNameAndFile(JSEArchiveMetaData umd, JBossWebMetaData jbossWebMetaData)
   {
      String configName = null;
      String configFile = null;
      
      WebserviceDescriptionsMetaData wsDescriptions = jbossWebMetaData.getWebserviceDescriptions();
      if (wsDescriptions != null && wsDescriptions.size() > 1)
         log.warn("Multiple <webservice-description> elements not supported");

      if (wsDescriptions != null && wsDescriptions.size() > 0)
      {
         WebserviceDescriptionMetaData wsd = wsDescriptions.iterator().next();
         configName = wsd.getConfigName();
         configFile = wsd.getConfigFile();
      }

      List<ParamValueMetaData> contextParams = jbossWebMetaData.getContextParams();
      if (contextParams != null)
      {
         for (ParamValueMetaData ctxParam : contextParams)
         {
            if (ctxParam.getParamName().equals("jbossws-config-name"))
               configName = ctxParam.getParamValue();
            if (ctxParam.getParamName().equals("jbossws-config-file"))
               configFile = ctxParam.getParamValue();
         }
      }
      
      umd.setConfigName(configName);
      umd.setConfigFile(configFile);
   }

   private PublishLocationAdapter getPublishLocationAdpater(final JBossWebMetaData wmd)
   {
      return new PublishLocationAdapter()
      {
         public String getWsdlPublishLocationByName(String name)
         {
            WebserviceDescriptionsMetaData wsdmd = wmd.getWebserviceDescriptions();
            WebserviceDescriptionMetaData wsmd = wsdmd.get(name);
            String location = null;
            if (wsmd != null)
               location = wsmd.getWsdlPublishLocation();
            return location;
         }
      };
   }

   protected List<JSESecurityMetaData> getSecurityMetaData(final List<SecurityConstraintMetaData> securityConstraints)
   {
      ArrayList<JSESecurityMetaData> unifiedsecurityMetaData = new ArrayList<JSESecurityMetaData>();
      if (securityConstraints != null)
      {
         for (SecurityConstraintMetaData securityMetaData : securityConstraints)
         {
            JSESecurityMetaData current = new JSESecurityMetaData();
            unifiedsecurityMetaData.add(current);

            current.setTransportGuarantee(securityMetaData.getTransportGuarantee().name());

            WebResourceCollectionsMetaData resources = securityMetaData.getResourceCollections();
            for (WebResourceCollectionMetaData webResource : resources)
            {
               JSEResourceCollection currentResource = current.addWebResource(webResource.getName());
               for (String currentPattern : webResource.getUrlPatterns())
               {
                  currentResource.addPattern(currentPattern);
               }
            }
         }
      }
      return unifiedsecurityMetaData;
   }

   private Map<String, String> getServletMappings(JBossWebMetaData wmd)
   {
      Map<String, String> mappings = new HashMap<String, String>();
      List<ServletMappingMetaData> smappings = wmd.getServletMappings();
      if (smappings != null)
      {
         for(ServletMappingMetaData mapping : smappings)
         {
            // FIXME - Add support for multiple mappings
            mappings.put(mapping.getServletName(), mapping.getUrlPatterns().get(0));
         }
      }
      return mappings;
   }

   private Map<String, String> getServletClassMap(JBossWebMetaData wmd)
   {
      Map<String, String> mappings = new HashMap<String, String>();
      JBossServletsMetaData servlets = wmd.getServlets();
      if (servlets != null)
      {
         for (ServletMetaData servlet : servlets)
         {
            // Skip JSPs
            if (servlet.getServletClass() == null || servlet.getServletClass().length() == 0)
               continue;
   
            mappings.put(servlet.getName(), servlet.getServletClass());
         }
      }
      return mappings;
   }
}
