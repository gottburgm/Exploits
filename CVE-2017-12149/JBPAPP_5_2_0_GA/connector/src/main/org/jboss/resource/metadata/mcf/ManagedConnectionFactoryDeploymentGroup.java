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
package org.jboss.resource.metadata.mcf;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * A ManagedConnectionFactoryDeploymentGroup.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@XmlAccessorType(XmlAccessType.NONE)
@ManagementObject
@XmlRootElement
public class ManagedConnectionFactoryDeploymentGroup implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7650097438654698297L;

   /** The fileUrl */
   private URL fileUrl;
      
   /** The deployments   */

   @XmlElements({@XmlElement(name="local-tx-datasource", type=LocalDataSourceDeploymentMetaData.class),
                 @XmlElement(name="no-tx-datasource", type=NoTxDataSourceDeploymentMetaData.class),
                 @XmlElement(name="xa-datasource", type=XADataSourceDeploymentMetaData.class),
                 @XmlElement(name="no-tx-connection-factory", type=NoTxConnectionFactoryDeploymentMetaData.class),
                 @XmlElement(name="tx-connection-factory",type=TxConnectionFactoryDeploymentMetaData.class)})
   private List<ManagedConnectionFactoryDeploymentMetaData> deployments = new ArrayList<ManagedConnectionFactoryDeploymentMetaData>();
   
   /** The services */
   @XmlElement(name="mbean")
   @XmlJavaTypeAdapter(ServiceMetaDataAdapter.class)
   private List<ServiceMetaData> services = new ArrayList<ServiceMetaData>();
   
   /** The loader repository config */
   @XmlElement(name="loader-repository")
   @XmlJavaTypeAdapter(LoaderRepositoryAdapter.class)
   private LoaderRepositoryConfig loaderRepositoryConfig;
   
   public void addManagedConnectionFactoryDeployment(ManagedConnectionFactoryDeploymentMetaData deployment)
   {
      this.deployments.add(deployment);
   }
   
   @ManagementProperty(description="The connection factories", managed=true)
   public List<ManagedConnectionFactoryDeploymentMetaData> getDeployments()
   {
      return Collections.unmodifiableList(deployments);
   }

   /**
    * Get the fileUrl.
    * 
    * @return the fileUrl.
    */
   @ManagementProperty(description="The ds.xml url", ignored=true)
   public URL getUrl()
   {
      return fileUrl;
   }

   /**
    * Set the fileUrl.
    * 
    * @param fileUrl The fileUrl to set.
    */
   public void setUrl(URL fileUrl)
   {
      this.fileUrl = fileUrl;
   }

   /**
    * Set the deployments.
    * 
    * @param deployments The deployments to set.
    */
   public void setDeployments(List<ManagedConnectionFactoryDeploymentMetaData> deployments)
   {
      this.deployments = deployments;
   }

   /**
    * Get the services.
    * 
    * @return the services.
    */
   @ManagementProperty(description="The extra mbeans from the ds.xml", managed=true)
   public List<ServiceMetaData> getServices()
   {
      return services;
   }

   /**
    * Set the services.
    * 
    * @param services The services to set.
    */
   public void setServices(List<ServiceMetaData> services)
   {
      this.services = services;
   }

   /**
    * Get the loaderRepositoryConfig.
    * 
    * @return the loaderRepositoryConfig.
    */
   public LoaderRepositoryConfig getLoaderRepositoryConfig()
   {
      return loaderRepositoryConfig;
   }

   /**
    * Set the loaderRepositoryConfig.
    * 
    * @param loaderRepositoryConfig the loaderRepositoryConfig.
    */
   public void setLoaderRepositoryConfig(LoaderRepositoryConfig loaderRepositoryConfig)
   {
      this.loaderRepositoryConfig = loaderRepositoryConfig;
   }

}
