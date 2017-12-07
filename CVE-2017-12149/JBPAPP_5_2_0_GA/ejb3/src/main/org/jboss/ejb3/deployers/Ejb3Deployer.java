/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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

import java.util.Set;

import javax.management.MBeanServer;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.security.JaccPolicyUtil;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.ejb.deployers.MergedJBossMetaDataDeployer;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.resolvers.MessageDestinationReferenceResolver;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * Deployes EJB 3 components based on meta data coming from JBossEjbParsingDeployer.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 95912 $
 */
public class Ejb3Deployer extends AbstractSimpleVFSRealDeployer<JBossMetaData>
{
   private Set<String> allowedSuffixes;
   
   /** EJBTHREE-1040: mandate a deployment descriptor to actually deploy */
   private boolean deploymentDescriptorRequired;
   
   private Set<String> ignoredJarsSet;
   
   private Kernel kernel;
   
   private MBeanServer mbeanServer;
   
   private CacheFactoryRegistry cacheFactoryRegistry;
   
   private PoolFactoryRegistry poolFactoryRegistry;
   
   private PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry;
   /** A flag indicating if wars should be scanned for ejbs */
   private boolean scanWars = false;
   
   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;

   private MessageDestinationReferenceResolver messageDestinationReferenceResolver;
   
   public Ejb3Deployer()
   {
	   // let the super deployer do the necessary stuff
	   // to add JBossMetaData as a requirement
	   super(JBossMetaData.class);
	   // we also want post processed metadata for processing.
	   // Using addInput ensures ordering http://www.jboss.org/index.html?module=bb&op=viewtopic&t=156725
      addInput(AttachmentNames.PROCESSED_METADATA);
      // TODO: should we really output this
      setOutput(Ejb3Deployment.class);
      // JBossASKernel install output
      setOutput(KernelDeployment.class);
   }

   
   @Override
   public void deploy(VFSDeploymentUnit unit, JBossMetaData metaData) throws DeploymentException
   {
      try
      {
         // Pickup any deployment which doesn't have metaData or metaData with ejbVersion unknown or 3
         if(metaData != null && (metaData.isEJB2x() || metaData.isEJB1x()))
         {
            log.debug("Ignoring legacy EJB deployment " + unit);
            return;
         }
         // If this is a war, check scanWars
         if(unit.getAttachment(JBossWebMetaData.class) != null && scanWars == false)
         {
            log.trace("Skipping web deployment: "+unit.getSimpleName());
            return;            
         }

         VirtualFile jar = unit.getRoot();
         if (jar.isLeaf() || ignoredJarsSet.contains(jar.getName()))
         {
            log.trace(this.getClass().getName() + " ignoring: " + jar.getName());
            return;
         }
         if(!hasAllowedSuffix(jar.getName()))
         {
            log.trace(this.getClass().getName() + " suffix not allowed: " + jar.getName());
            return;
         }
         
         // If DDs are required and none are present, skip deployment
         // EJBTHREE-1040
         if (this.isDeploymentDescriptorRequired() && (metaData == null))
         {
            log.trace(this.getClass().getSimpleName() + " skipping deployment \"" + unit.getSimpleName()
                  + "\", jar: \"" + jar.getName()
                  + "\" - either EJB3 Deployment Descriptor or \"jboss.xml\" is required and neither were found.");
            return;
         }
            
         log.debug("********* " + this.getClass().getSimpleName() + " Begin Unit: " + unit.getSimpleName() + " jar: "
               + jar.getName());
         JBoss5DeploymentScope scope = null;
         VFSDeploymentUnit parent = unit.getTopLevel();
         boolean initScopeDeployment = false;
         if (parent != null)
         {
            // Check for an existing scope
            scope = (JBoss5DeploymentScope) parent.getAttachment(DeploymentScope.class);
            if (scope == null)
            {
               // Check for a scoped deployment or an ear top-level unit
               boolean isEar = unit != unit.getTopLevel() || parent.isAttachmentPresent(JBossAppMetaData.class);
               if(parent.isAttachmentPresent(DeploymentEndpointResolver.class) == true)
                  scope = new JBoss5DeploymentScope(parent, isEar);
               else
               {
                  // EJBTHREE-1291
                  scope = new JBoss5DeploymentScope(parent, isEar, unit.getSimpleName());
                  initScopeDeployment = true;
               }
               parent.addAttachment(DeploymentScope.class, scope);
            }
         }

         JBoss5DeploymentUnit du = new JBoss5DeploymentUnit(unit);
         Ejb3JBoss5Deployment deployment = new Ejb3JBoss5Deployment(du, kernel, mbeanServer, unit, scope, metaData);
         if(initScopeDeployment)
         {
            scope.setDeployment(deployment);
         }
         deployment.setCacheFactoryRegistry(this.getCacheFactoryRegistry());
         // TODO: if the deployment becomes a proper MC bean, it'll get injected by MC.
         deployment.setMessageDestinationReferenceResolver(messageDestinationReferenceResolver);
         deployment.setPersistenceManagerFactoryRegistry(this.getPersistenceManagerFactoryRegistry());
         // TODO: if the deployment becomes a proper MC bean, it'll get injected by MC.
         deployment.setPersistenceUnitDependencyResolver(persistenceUnitDependencyResolver);
         deployment.setPoolFactoryRegistry(this.getPoolFactoryRegistry());
         if (scope != null)
            scope.register(deployment);
         // create() creates initial EJB containers and initializes metadata.
         deployment.create();
         if (deployment.getEjbContainers().size() == 0)
         {
            log.trace("Found no containers in scanned jar, consider adding it to the ignore list: " + jar.getName() + " url: " + jar.toURL() + " unit: " + unit.getSimpleName());
            deployment.destroy();
            return;
         }
         deployment.start();
         unit.addAttachment(Ejb3Deployment.class, deployment);
         // TODO: temporarily disable the security deployment
         unit.addAttachment(JaccPolicyUtil.IGNORE_ME_NAME, true, Boolean.class);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Error deploying " + unit.getSimpleName() + ": " + t.getMessage(), t);
      }
   }

   public Set<String> getAllowedSuffixes()
   {
      return allowedSuffixes;
   }
   
   public CacheFactoryRegistry getCacheFactoryRegistry()
   {
      return cacheFactoryRegistry;
   }
   
   public void setCacheFactoryRegistry(CacheFactoryRegistry cacheFactoryRegistry)
   {
      this.cacheFactoryRegistry = cacheFactoryRegistry;
   }

   public PoolFactoryRegistry getPoolFactoryRegistry()
   {
      return poolFactoryRegistry;
   }

   public void setPoolFactoryRegistry(PoolFactoryRegistry poolFactoryRegistry)
   {
      this.poolFactoryRegistry = poolFactoryRegistry;
   }

   public PersistenceManagerFactoryRegistry getPersistenceManagerFactoryRegistry()
   {
      return persistenceManagerFactoryRegistry;
   }

   @Inject
   public void setMessageDestinationReferenceResolver(MessageDestinationReferenceResolver resolver)
   {
      this.messageDestinationReferenceResolver = resolver;   
   }
   
   public void setPersistenceManagerFactoryRegistry(PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry)
   {
      this.persistenceManagerFactoryRegistry = persistenceManagerFactoryRegistry;
   }

   @Inject
   public void setPersistenceUnitDeploymentResolver(PersistenceUnitDependencyResolver resolver)
   {
      this.persistenceUnitDependencyResolver = resolver;
   }
   
   public boolean isScanWars()
   {
      return scanWars;
   }
   public void setScanWars(boolean scanWars)
   {
      this.scanWars = scanWars;
   }

   private boolean hasAllowedSuffix(String name)
   {
      if(allowedSuffixes == null)
         return true;
      
      for (String suffix : allowedSuffixes)
      {
         if (name.endsWith(suffix))
         {
            return true;
         }
      }
      return false;
   }
   
   public boolean isDeploymentDescriptorRequired()
   {
      return deploymentDescriptorRequired;
   }
   
   public void setAllowedSuffixes(Set<String> s)
   {
      this.allowedSuffixes = s;
   }
   
   public void setDeploymentDescriptorRequired(boolean b)
   {
      this.deploymentDescriptorRequired = b;
   }
   
   public void setIgnoredJarsSet(Set<String> s)
   {
      this.ignoredJarsSet = s;
   }
   
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   public void setMbeanServer(MBeanServer server)
   {
      this.mbeanServer = server;
   }
   
   @Override
   public void undeploy(VFSDeploymentUnit unit, JBossMetaData metadata)
   {
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      if(deployment == null)
         return;

      try
      {
         deployment.stop();
      }
      catch(Exception e)
      {
         log.warn("Failed to stop deployment " + deployment, e);
      }
      try
      {
         deployment.destroy();
      }
      catch(Exception e)
      {
         log.warn("Failed to destroy deployment " + deployment, e);
      }
   }

   /**
    * LifeCycle Start
    * 
    * Responsible for Binding an MC-based EJB3 Registrar Object Store
    * 
    * @author ALR
    * @throws Throwable
    */
   @Start
   public void start() throws Throwable
   {
      // Bind an EJB3 Registrar Implementation if not already bound
      if (!Ejb3RegistrarLocator.isRegistrarBound())
      {
         // Obtain the Kernel
         Kernel sanders = this.kernel;
         assert sanders != null : Kernel.class.getSimpleName() + " must be provided in order to bind "
               + Ejb3Registrar.class.getSimpleName();

         // Create an EJB3 Registrar
         Ejb3Registrar registrar = new Ejb3McRegistrar(sanders);

         // Bind Registrar to the Locator
         Ejb3RegistrarLocator.bindRegistrar(registrar);
         
         // Log
         log.debug("Bound " + Ejb3Registrar.class.getSimpleName() + " to static "
               + Ejb3RegistrarLocator.class.getSimpleName());
      }
   }
   
   /**
    * LifeCycle Stop
    * 
    * Responsible for Unbinding the MC-based EJB3 Registrar Object Store
    * 
    * @throws Throwable
    */
   @Stop
   public void stop() throws Throwable
   {
      // If bound
      if (Ejb3RegistrarLocator.isRegistrarBound())
      {
         Ejb3RegistrarLocator.unbindRegistrar();
      }
   }
}
