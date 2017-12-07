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
package org.jboss.kernel.deployment.jboss;

import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployer;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.UnreachableStatementException;

/**
 * A jboss bean deployment
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81038 $
 */
public class JBossBeanDeployment extends ServiceMBeanSupport implements JBossBeanDeploymentMBean
{
   /** The deployment info */
   protected DeploymentInfo di;
   
   /** The deployment */
   protected KernelDeployment deployment;

   /** The kernel deployer */
   protected AbstractKernelDeployer deployer;
   
   /**
    * Create a new deployment
    * 
    * @param di the deployment info
    */
   public JBossBeanDeployment(DeploymentInfo di)
   {
      this.di = di;
      this.deployment = (KernelDeployment) di.metaData;
   }
   
   protected void createService() throws Exception
   {
      Kernel kernel = getKernel();
      deployer = new AbstractKernelDeployer(kernel, ControllerState.CREATE, ControllerMode.MANUAL);
      try
      {
         deployer.deploy(deployment);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Cannot deploy " + deployment, t);
      }
   }
   
   protected void startService() throws Exception
   {
      try
      {
         deployer.change(deployment, ControllerState.INSTALLED);
         deployer.validate(deployment);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Cannot start " + deployment, t);
      }
   }
   
   protected void stopService() throws Exception
   {
      try
      {
         deployer.change(deployment, ControllerState.CREATE);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Cannot stop " + deployment, t);
      }
   }
   
   protected void destroyService() throws Exception
   {
      try
      {
         if (deployer != null)
            deployer.undeploy(deployment);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Cannot stop " + deployment, t);
      }
      deployer = null;
   }
   
   /**
    * Get the kernel for this deployment
    * 
    * @return the kernel
    */
   protected Kernel getKernel() throws DeploymentException
   {
      try
      {
         BasicBootstrap bootstrap = new BasicBootstrap();
         bootstrap.run();
         return bootstrap.getKernel();
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Unable to boot kernel", t);
         throw new UnreachableStatementException();
      }
   }
}
