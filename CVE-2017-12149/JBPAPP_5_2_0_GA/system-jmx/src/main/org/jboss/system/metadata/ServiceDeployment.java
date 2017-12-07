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
package org.jboss.system.metadata;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.managed.api.annotation.ManagementDeployment;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.w3c.dom.Element;

/**
 * ServiceDeployment.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@ManagementObject
@ManagementDeployment(types={"sar"})
@XmlRootElement
public class ServiceDeployment
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The deployment name */
   private String name;

   /** The services */
   private List<ServiceMetaData> services;
   
   /** The config */
   private Element config;
   
   /** The loader repository config */
   private LoaderRepositoryConfig loaderRepositoryConfig;

   /** The classpaths */
   private List<ServiceDeploymentClassPath> classPaths;
   
   /**
    * Get the name.
    * 
    * @return the name.
    */
   @ManagementProperty(description="The -service.xml url string")
   @ManagementObjectID(type="ServiceDeployment")
   @XmlTransient
   public String getName()
   {
      return name;
   }

   /**
    * Set the name.
    * 
    * @param name the name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get the services.
    * 
    * @return the services.
    */
   @ManagementProperty(description="The mbeans", managed=true)
   @XmlElement(name="mbean")
   @XmlJavaTypeAdapter(ServiceMetaDataAdapter.class)
   public List<ServiceMetaData> getServices()
   {
      return services;
   }

   /**
    * Set the services.
    * 
    * @param services the services.
    */
   public void setServices(List<ServiceMetaData> services)
   {
      this.services = services;
   }

   /**
    * Get the config.
    * 
    * @return the config.
    */
   @ManagementProperty(ignored=true)
   @XmlTransient
   public Element getConfig()
   {
      return config;
   }

   /**
    * Set the config.
    * 
    * @param config the config.
    */
   public void setConfig(Element config)
   {
      this.config = config;
   }

   /**
    * Get the loaderRepositoryConfig.
    * 
    * @return the loaderRepositoryConfig.
    */
   @ManagementProperty(ignored=true)
   @XmlElement(name = "loader-repository")
   @XmlJavaTypeAdapter(ServiceLoaderRepositoryAdapter.class)
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

   /**
    * Get the classPaths.
    * 
    * @return the classPaths.
    */
   @ManagementProperty(name="classpath", description="The deployment classpath", managed=true)
   @XmlElement(name = "classpath")
   @XmlJavaTypeAdapter(ServiceDeploymentClasspathAdapter.class)
   public List<ServiceDeploymentClassPath> getClassPaths()
   {
      return classPaths;
   }

   /**
    * Set the classPaths.
    * 
    * @param classPaths the classPaths.
    */
   public void setClassPaths(List<ServiceDeploymentClassPath> classPaths)
   {
      this.classPaths = classPaths;
   }
}
