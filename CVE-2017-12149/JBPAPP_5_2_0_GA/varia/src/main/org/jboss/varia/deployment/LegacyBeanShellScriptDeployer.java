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
package org.jboss.varia.deployment;

import java.util.Arrays;
import java.util.Collection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mx.util.ObjectNameConverter;
import org.jboss.system.ServiceController;
import org.jboss.system.microcontainer.LifecycleDependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * Old mbean style bean shell script deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class LegacyBeanShellScriptDeployer extends AbstractSimpleRealDeployer<BeanShellScript>
{
   public static final String BASE_SCRIPT_OBJECT_NAME = "jboss.scripts:type=BeanShell";
   /** The service controller */
   private ServiceController serviceController;
   /** The default controller mode */
   private ControllerMode mode = ControllerMode.AUTOMATIC;

   public LegacyBeanShellScriptDeployer(ServiceController serviceController)
   {
      super(BeanShellScript.class);
      if (serviceController == null)
         throw new IllegalArgumentException("Null service controller.");
      this.serviceController = serviceController;
   }

   /**
    * Get the bean shell script name.
    *
    * @param script the script
    * @param deploymentUnit the deployment unit
    * @return get script's object name
    * @throws MalformedObjectNameException for any error
    */
   protected ObjectName getBshScriptName(BeanShellScript script, DeploymentUnit deploymentUnit)
         throws MalformedObjectNameException
   {
      ObjectName bshScriptName = script.getPreferedObjectName();
      if (bshScriptName == null)
      {
         bshScriptName = ObjectNameConverter.convert(BASE_SCRIPT_OBJECT_NAME + ",url=" + deploymentUnit.getSimpleName());
      }
      return bshScriptName;
   }

   public void deploy(DeploymentUnit deploymentUnit, BeanShellScript script) throws DeploymentException
   {
      try
      {
         Controller controller = serviceController.getKernel().getController();
         ObjectName bshScriptName = getBshScriptName(script, deploymentUnit);
         ServiceControllerContext context = new ServiceControllerContext(serviceController, bshScriptName, script);
         context.setMode(mode);
         ObjectName[] depends = script.getDependsServices();
         if (depends != null)
            addDependencies(context, Arrays.asList(depends));

         if (log.isTraceEnabled())
            log.trace("Installing bean shell script: " + bshScriptName);

         controller.install(context);
      }
      catch (Throwable t)
      {
         throw DeploymentException.rethrowAsDeploymentException("Unable to deploy bean shell script.", t);
      }
   }

   public void undeploy(DeploymentUnit deploymentUnit, BeanShellScript script)
   {
      try
      {
         ObjectName bshScriptName = getBshScriptName(script, deploymentUnit);
         Controller controller = serviceController.getKernel().getController();

         if (log.isTraceEnabled())
            log.trace("Uninstalling bean shell script: " + bshScriptName);

         controller.uninstall(bshScriptName.getCanonicalName());
      }
      catch (Throwable t)
      {
         log.error("Exception while undeploying bean shell script: " + t);
      }
   }

   /**
    * Add the passed lifecycle dependencies to the context
    *
    * @param context the context
    * @param depends the dependencies
    */
   private void addDependencies(ServiceControllerContext context, Collection<ObjectName> depends)
   {
      DependencyInfo info = context.getDependencyInfo();
      for (ObjectName other : depends)
      {
         info.addIDependOn(new LifecycleDependencyItem(context.getName(), other.getCanonicalName(), ControllerState.CREATE));
         info.addIDependOn(new LifecycleDependencyItem(context.getName(), other.getCanonicalName(), ControllerState.START));
      }
   }

   /**
    * Set the mode.
    *
    * @param mode the controller mode
    */
   public void setMode(String mode)
   {
      this.mode = ControllerMode.getInstance(mode);
   }
}
