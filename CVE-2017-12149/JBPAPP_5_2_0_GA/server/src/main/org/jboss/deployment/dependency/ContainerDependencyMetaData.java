/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployment.dependency;

import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DefaultJndiBindingPolicy;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.EjbDeploymentSummary;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * Metadata for an endpoint container and its dependencies on other endpoints.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ContainerDependencyMetaData extends JBossObject
{
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger
         .getLogger(ContainerDependencyMetaData.class);

   /** The unique name of the deployment root */
   private String deploymentRootName;
   /** The deployment relative path name */
   private String deploymentPath;
   /** The id of the component in the deployment */
   private String componentName;

   private Set<String> jndiNames = new HashSet<String>();

   /** The jndi name of the container */
   private String containerName;

   /** The injection targets this bean container has */
   private Set<ResourceInjectionTargetMetaData> targets = new HashSet<ResourceInjectionTargetMetaData>();
   /** The jndi lookups this bean container has */
   private Set<JndiDependencyMetaData> jndiDepends = new HashSet<JndiDependencyMetaData>();
   /** The jndi names for other container this container depends on */
   private Set<String> jndiAliasDepends = new HashSet<String>();
   /** The jndi binding policy associated with the bean deployment */
   private DefaultJndiBindingPolicy jndiPolicy;
   /** Is this an ejb3 endpoint */
   private boolean isEjb3X;
   private EjbDeploymentSummary unitSummary;

   /**
    * 
    * @param containerName - the jndi name of the container
    * @param componentName - the unique vfs/component-name id
    * @param deploymentPath - the vfs relative path of the component deployment
    */
   public ContainerDependencyMetaData(String containerName, String componentName, String deploymentPath)
   {
      this.containerName = containerName;
      this.componentName = componentName;
      this.deploymentPath = deploymentPath;
      this.addJndiName(containerName);
   }

   public String getComponentName()
   {
      return componentName;
   }
   public String getComponentID()
   {
      return deploymentPath + "#" + componentName;
   }
   public String getDeploymentPath()
   {
      return deploymentPath;
   }

   public String getDeploymentRootName()
   {
      return deploymentRootName;
   }
   public void setDeploymentRootName(String deploymentRootName)
   {
      this.deploymentRootName = deploymentRootName;
   }

   public String getContainerName()
   {
      return containerName;
   }

   public JBossEnterpriseBeanMetaData getBeanMetaData()
   {
      return this.unitSummary.getBeanMD();
   }

   public EjbDeploymentSummary getUnitSummary()
   {
      return unitSummary;
   }
   public void setUnitSummary(EjbDeploymentSummary unitSummary)
   {
      this.unitSummary = unitSummary;
   }

   public boolean isEjb3X()
   {
      return isEjb3X;
   }
   public void setEjb3X(boolean isEjb3X)
   {
      this.isEjb3X = isEjb3X;
   }

   public void addDependency(ContainerDependencyMetaData endpointCDMD)
   {
      jndiAliasDepends.add(endpointCDMD.getContainerName());
   }

   public void addInjectionTargets(Set<ResourceInjectionTargetMetaData> injectionTargets)
   {
      log.info("addInjectionTargets, " + injectionTargets);
      this.targets.addAll(injectionTargets);
   }

   public Set<ResourceInjectionTargetMetaData> getInjectionTargets()
   {
      return this.targets;
   }

   public void addJndiDependency(JndiDependencyMetaData jdmd)
   {
      log.info("addJndiDependency, " + jdmd);
      jndiDepends.add(jdmd);
   }

   public Set<JndiDependencyMetaData> getJndiDepends()
   {
      return this.jndiDepends;
   }
   public void addJndiName(String jndiName)
   {
      jndiNames.add(jndiName);      
   }
   public Set<String> getJndiNames()
   {
      return jndiNames;
   }

   public String toShortString()
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      toShortString(buffer);
      return buffer.toString();
   }

   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(containerName);
   }
   @Override
   protected int getHashCode()
   {
      // TODO Auto-generated method stub
      return super.getHashCode();
   }

   @Override
   protected String toStringImplementation()
   {
      StringBuilder tmp = new StringBuilder("ContainerDependencyMetaData(");
      tmp.append("deploymentPath=");
      tmp.append(deploymentPath);
      tmp.append(",componentName=");
      tmp.append(this.componentName);
      tmp.append(",containerName=");
      tmp.append(this.containerName);
      tmp.append(",jndiNames=");
      tmp.append(this.jndiNames);
      tmp.append(",jndiAliasDepends=");
      tmp.append(this.jndiAliasDepends);
      tmp.append(",targets=");
      tmp.append(this.targets);
      tmp.append(",jndiDepends=");
      tmp.append(this.jndiDepends);
      tmp.append(")");
      return tmp.toString();
   }

   @Override
   public boolean equals(Object obj)
   {
      // TODO Auto-generated method stub
      return super.equals(obj);
   }

}
