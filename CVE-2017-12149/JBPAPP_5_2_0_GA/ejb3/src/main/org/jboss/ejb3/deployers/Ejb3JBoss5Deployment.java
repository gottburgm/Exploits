/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
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
package org.jboss.ejb3.deployers;

import org.jboss.beans.metadata.plugins.AbstractSupplyMetaData;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.MappedReferenceMetaDataResolverDeployer;
import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3HandlerFactory;
import org.jboss.ejb3.MCDependencyPolicy;
import org.jboss.ejb3.javaee.JavaEEApplication;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.kernel.JNDIKernelRegistryPlugin;
import org.jboss.ejb3.vfs.spi.VirtualFile;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.EjbDeploymentSummary;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.security.jacc.PolicyConfiguration;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * JBoss 5.0 Microkernel specific implementation
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 112275 $
 */
public class Ejb3JBoss5Deployment extends Ejb3Deployment
{
   private static Logger log = Logger.getLogger(Ejb3JBoss5Deployment.class);
   private VFSDeploymentUnit jbossUnit;
   private Map<String, ContainerDependencyMetaData> endpoints;

   public Ejb3JBoss5Deployment(DeploymentUnit ejb3Unit, Kernel kernel, MBeanServer mbeanServer, VFSDeploymentUnit jbossUnit, JBoss5DeploymentScope deploymentScope, JBossMetaData metaData)
   {
      // Either call the old constructor and do process persistence units
      //super(ejb3Unit, deploymentScope, metaData, persistenceUnitsMetaData);
      // or call the new constructor and don't process persistence units
      super(jbossUnit, ejb3Unit, deploymentScope, metaData);
      
      this.jbossUnit = jbossUnit;
      kernelAbstraction = new JBossASKernel(kernel, mbeanServer, ejb3Unit);

      // todo maybe mbeanServer should be injected?
      this.mbeanServer = mbeanServer;
      org.jboss.deployers.structure.spi.DeploymentUnit topUnit = jbossUnit.getTopLevel();
      endpoints = (Map<String, ContainerDependencyMetaData>) topUnit.getAttachment(MappedReferenceMetaDataResolverDeployer.ENDPOINT_MAP_KEY);
   }

   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
      return null;
   }

   @Override
   protected void deployUrl(Ejb3HandlerFactory factory) throws Exception
   {
      // make sure we are not deploying ejbs from client jar
      if (hasClientDescriptors())
         return;

      InitialContext ctx = initialContext;
      // need to look into every entry in the archive to see if anybody has tags
      // defined.
      List<VirtualFile> classes = unit.getResources(new org.jboss.ejb3.ClassFileFilter());
      for (VirtualFile classFile : classes)
      {
         InputStream stream = classFile.openStream();
         deployElement(stream, factory, ctx);
      }
   }

   private boolean hasClientDescriptors()
   {
      return jbossUnit.getMetaDataFile("application-client.xml") != null || jbossUnit.getMetaDataFile("jboss-client.xml") != null;
   }

   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit ejb3Unit)
   {
      //Ignore
   }

   public DependencyPolicy createDependencyPolicy(JavaEEComponent component)
   {
      return new JBoss5DependencyPolicy(component);
   }

   @Override
   public JavaEEApplication getApplication()
   {
      // getApplication must return null if there is no ear
      JavaEEApplication app = super.getApplication();
      if(((JBoss5DeploymentScope) app).isEar())
         return app;
      return null;
   }
   
   @Override
   protected void registerEJBContainer(Container container) throws Exception
   {
      // Add the jndi supplies
      MCDependencyPolicy dependsPolicy = (MCDependencyPolicy) container.getDependencyPolicy();
      EJBContainer ejbContainer = (EJBContainer) container;
      JBossEnterpriseBeanMetaData beanMD = ejbContainer.getXml();

      ContainerDependencyMetaData cdmd = null;
      if(endpoints != null)
      {
         String ejbKey = "ejb/" + jbossUnit.getRelativePath() + "#" + container.getEjbName();
         cdmd = endpoints.get(ejbKey);
      }
      else
      {
         log.warn(jbossUnit+" has no ContainerDependencyMetaData attachment");
      }

      if(cdmd != null)
      {
         for(String jndiName : cdmd.getJndiNames())
         {
         String supplyName = JNDIKernelRegistryPlugin.JNDI_DEPENDENCY_PREFIX + jndiName;
         AbstractSupplyMetaData supply = new AbstractSupplyMetaData(supplyName);
         dependsPolicy.getSupplies().add(supply);
         }
      }

      // EJBTHREE-1335: container name in meta data
      generateContainerName(container, beanMD);
      
      super.registerEJBContainer(container);
   }

   private void generateContainerName(Container container, JBossEnterpriseBeanMetaData beanMD)
   {
      ObjectName on = container.getObjectName();
      assert on!=null : "ObjectName was null";

      // Heiko: This should actually generate the name and assign it to ejb3 meta data
      // Currently we stick to copying the values around until an EJB3 team member figures out a proper way      
      beanMD.setGeneratedContainerName(on.getCanonicalName());
   }

   private static EjbDeploymentSummary getUnitSummary(DeploymentUnit unit, JBossEnterpriseBeanMetaData beanMD)
   {
      ClassLoader loader = unit.getClassLoader();
      EjbDeploymentSummary summary = new EjbDeploymentSummary();
      summary.setBeanMD(beanMD);
      summary.setBeanClassName(beanMD.getEjbClass());
      summary.setDeploymentName(unit.getShortName());
      String baseName = unit.getRootFile().getName();
      summary.setDeploymentScopeBaseName(baseName);
      summary.setEjbName(beanMD.getEjbName());
      summary.setLoader(loader);
      summary.setLocal(beanMD.isMessageDriven());
      if(beanMD instanceof JBossSessionBeanMetaData)
      {
         JBossSessionBeanMetaData sbeanMD = (JBossSessionBeanMetaData) beanMD;
         summary.setStateful(sbeanMD.isStateful());
      }
      summary.setService(beanMD.isService());
      return summary;
   }   
}
