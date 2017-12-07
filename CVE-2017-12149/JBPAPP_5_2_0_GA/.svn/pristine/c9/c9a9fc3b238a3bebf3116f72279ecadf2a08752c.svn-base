/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.deployers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.DeploymentException;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.authorization.PolicyRegistration;
import org.jboss.web.WebApplication;

/** A container service used to introduce war dependencies. This service is
 created by the AbstractWebContainer during the create(DeploymentInfo) call
 and registered under the name "jboss.web.deployment:war="+di.shortName
 This name is stored in the di.context under the key AbstractWebContainer.WEB_MODULE

 When the jboss-web.xml dependencies are satisfied, this service is started
 and this triggers the AbstractWebDeployer.start. Likewise, a stop on this
 service triggers the AbstractWebDeployer.stop.
 
 @see AbstractWarDeployer
 
 @author Scott.Stark@jboss.org
 @author adrian@jboss.org
 @version $Revison:$
 */
public class WebModule implements WebModuleMBean
{
   private static Logger log = Logger.getLogger(WebModule.class);

   private DeploymentUnit unit;
   private AbstractWarDeployer container;
   private AbstractWarDeployment deployment;

   private ISecurityManagement securityManagement;

   public WebModule(DeploymentUnit unit, AbstractWarDeployer container, AbstractWarDeployment deployment)
   {
      this.unit = unit;
      this.container = container;
      this.deployment = deployment;
      this.deployment.setDeploymentUnit(unit);
   }

   public void setKernel(Kernel kernel)
   {
      this.deployment.setKernel(kernel);
   }

   /**
    * Set the PolicyRegistration instance
    * @param policyRegistration the policy registration instance
    */
   public void setPolicyRegistration(PolicyRegistration policyRegistration)
   {
      deployment.setPolicyRegistration(policyRegistration);
   }

   /**
    * Set the securityManagement.
    * 
    * @param securityManagement the securityManagement.
    */
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      deployment.setSecurityManagement(securityManagement);
   }

   public void create()
   {

   }

   public void start() throws Exception
   {
      startModule();
   }

   public void stop() throws Exception
   {
      stopModule();
   }

   public void destroy()
   {
      this.unit = null;
      this.container = null;
      this.deployment = null;
   }

   /** Invokes the deployer start
    */
   public synchronized void startModule() throws Exception
   {
      if (this.unit == null || this.container == null || this.deployment == null)
      {
         throw new IllegalStateException("WebModules cannot be restarted, and must be redeployed");
      }
      // Get the war URL
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      WebApplication webApp = deployment.start(unit, metaData);
      String warURL = unit.getName();
      container.addDeployedApp(warURL, webApp);
   }

   /** Invokes the deployer stop
    */
   public synchronized void stopModule() throws DeploymentException
   {
      String warURL = unit.getName();
      try
      {
         WebApplication webApp = container.removeDeployedApp(warURL);
         if (deployment != null && webApp != null)
         {
            deployment.stop(unit, webApp);
         }
         else
         {
            log.debug("Failed to find deployer/deployment for war: " + warURL);
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error during stop", e);
      }
   }

}
