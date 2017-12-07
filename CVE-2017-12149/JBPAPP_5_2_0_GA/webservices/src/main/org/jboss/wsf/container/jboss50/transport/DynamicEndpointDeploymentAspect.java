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
package org.jboss.wsf.container.jboss50.transport;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.jboss.classloading.spi.dependency.ClassLoading;
import org.jboss.classloading.spi.dependency.Module;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.DeploymentFactory;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.container.jboss50.deployment.tomcat.WebMetaDataModifier;

/**
 * Deploy the generated webapp to JBoss
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class DynamicEndpointDeploymentAspect extends DeploymentAspect
{
   // provide logging
   private static Logger log = Logger.getLogger(DynamicEndpointDeploymentAspect.class);

   private DeploymentFactory factory = new DeploymentFactory();
   private WebMetaDataModifier webMetaDataModifier;
   private DeployerClient mainDeployer;
   
   private Map<String,AbstractDeployment> deployments = new HashMap<String,AbstractDeployment>();

   public void setWebMetaDataModifier(WebMetaDataModifier webMetaDataModifier)
   {
      this.webMetaDataModifier = webMetaDataModifier;
   }

   public void setMainDeployer(DeployerClient mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   public void create(Deployment dep)
   {
      JBossWebMetaData jbwmd = dep.getAttachment(JBossWebMetaData.class);
      if (jbwmd == null)
         throw new WebServiceException("Cannot find web meta data");

      ClassLoader epLoader = dep.getRuntimeClassLoader();
      
      try
      {
         webMetaDataModifier.modifyMetaData(dep);

         String contextRoot = dep.getService().getContextRoot();
         AbstractDeployment deployment = createSimpleDeployment("http://jaxws-endpoint-api" + contextRoot);
         MutableAttachments mutableAttachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
         mutableAttachments.addAttachment(WebMetaDataModifier.PROPERTY_GENERATED_WEBAPP, Boolean.TRUE);
         mutableAttachments.addAttachment("org.jboss.web.explicitDocBase", "/", String.class);
         mutableAttachments.addAttachment(ClassLoaderFactory.class, new ContextClassLoaderFactory(epLoader));
         mutableAttachments.addAttachment(JBossWebMetaData.class, jbwmd);
         mutableAttachments.addAttachment(Module.class, ClassLoading.getModuleForClassLoader(epLoader));
         mainDeployer.deploy(deployment);
         
         deployments.put(contextRoot, deployment);
      }
      catch (Exception ex)
      {
         WSFDeploymentException.rethrow(ex);
      }
   }

   public void destroy(Deployment dep)
   {
      try
      {
         String contextRoot = dep.getService().getContextRoot();
         AbstractDeployment deployment = deployments.remove(contextRoot);
         if (deployment != null)
            mainDeployer.undeploy(deployment);
      }
      catch (Exception ex)
      {
         WSFDeploymentException.rethrow(ex);
      }
   }
   
   private AbstractDeployment createSimpleDeployment(String name)
   {
      AbstractDeployment unit = new AbstractDeployment(name);
      // There is one top level deployment
      factory.addContext(unit, "");
      return unit;
   }

   private static class ContextClassLoaderFactory implements ClassLoaderFactory
   {
      private ClassLoader classloader;
      
      public ContextClassLoaderFactory(ClassLoader classloader)
      {
         this.classloader = classloader;
      }

      public ClassLoader createClassLoader(DeploymentUnit unit) throws Exception
      {
         return classloader;
      }

      public void removeClassLoader(DeploymentUnit unit) throws Exception
      {
         classloader = null;
      }
   }
}
