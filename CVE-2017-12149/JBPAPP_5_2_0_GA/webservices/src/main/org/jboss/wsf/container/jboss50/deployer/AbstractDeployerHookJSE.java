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

import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.container.jboss50.deployment.tomcat.WebMetaDataModifier;

/**
 * An abstract deployer for JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public abstract class AbstractDeployerHookJSE extends ArchiveDeployerHook
{
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      boolean isGenerated = Boolean.TRUE.equals(unit.getAttachment(WebMetaDataModifier.PROPERTY_GENERATED_WEBAPP));
      return webMetaData != null && isGenerated == false;
   }

   protected String getTargetBean(ServletMetaData servlet)
   {
      String endpointClass = servlet.getServletClass();
      List<ParamValueMetaData> initParams = servlet.getInitParam();
      if (initParams != null)
      {
         for (ParamValueMetaData param : initParams)
         {
            if (Endpoint.SEPID_DOMAIN_ENDPOINT.equals(param.getParamName()))
            {
               endpointClass = param.getParamValue();
               break;
            }
         }
      }
      return endpointClass;
   }
}
