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
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.container.jboss50.deployment.tomcat.WebMetaDataModifier;

import java.net.URL;

/**
 * Build container independent deployment info.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class ContainerMetaDataAdapter
{
   // logging support
   private static Logger log = Logger.getLogger(ContainerMetaDataAdapter.class);

   private EJBArchiveMetaDataAdapterEJB3 ejbMetaDataAdapterEJB3 = new EJBArchiveMetaDataAdapterEJB3();
   private EJBArchiveMetaDataAdapterEJB21 ejbMetaDataAdapterEJB21 = new EJBArchiveMetaDataAdapterEJB21();
   private JSEArchiveMetaDataAdapter webMetaDataAdapter = new JSEArchiveMetaDataAdapter();

   public void setEjbMetaDataAdapterEJB21(EJBArchiveMetaDataAdapterEJB21 adapter)
   {
      this.ejbMetaDataAdapterEJB21 = adapter;
   }

   public void setEjbMetaDataAdapterEJB3(EJBArchiveMetaDataAdapterEJB3 adapter)
   {
      this.ejbMetaDataAdapterEJB3 = adapter;
   }

   public void setWebMetaDataAdapter(JSEArchiveMetaDataAdapter adapter)
   {
      this.webMetaDataAdapter = adapter;
   }

   public void buildContainerMetaData(Deployment dep, DeploymentUnit unit)
   {
      dep.addAttachment(DeploymentUnit.class, unit);
      
      try
      {
         // JSE endpoints
         if (unit.getAttachment(JBossWebMetaData.class) != null)
         {
            JSEArchiveMetaData webMetaData = webMetaDataAdapter.buildMetaData(dep, unit);
            if (webMetaData != null)
               dep.addAttachment(JSEArchiveMetaData.class, webMetaData);

            if (dep instanceof ArchiveDeployment)
            {
               URL webURL = ((ArchiveDeployment)dep).getRootFile().toURL();
               dep.setProperty(WebMetaDataModifier.PROPERTY_WEBAPP_URL, webURL);
            }
         }
         
         // EJB3 endpoints
         else if (unit.getAttachment(Ejb3Deployment.class) != null)
         {
            EJBArchiveMetaData ejbMetaData = ejbMetaDataAdapterEJB3.buildMetaData(dep, unit);
            if (ejbMetaData != null)
               dep.addAttachment(EJBArchiveMetaData.class, ejbMetaData);
         }
         
         // EJB21 endpoints
         else if (unit.getAttachment(JBossMetaData.class) != null)
         {
            EJBArchiveMetaData ejbMetaData = ejbMetaDataAdapterEJB21.buildMetaData(dep, unit);
            if (ejbMetaData != null)
               dep.addAttachment(EJBArchiveMetaData.class, ejbMetaData);
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException(ex);
      }
   }
}
