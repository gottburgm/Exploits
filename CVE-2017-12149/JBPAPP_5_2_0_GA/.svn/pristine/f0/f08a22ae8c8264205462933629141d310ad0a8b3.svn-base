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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.DeployerClient;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.logging.Logger;

/**
 * Old client style bean shell invocation client.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@ManagementObject
public class LegacyBeanShellScriptClient implements BeanShellScriptClient
{
   protected Logger log = Logger.getLogger(getClass());
   private DeployerClient deployer;

   public LegacyBeanShellScriptClient(DeployerClient deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Null client deployer.");
      this.deployer = deployer;
   }

   protected BeanShellScript createBeanShellScript(String bshScript, String scriptName)
         throws org.jboss.deployment.DeploymentException
   {
      InputStream stream = new ByteArrayInputStream(bshScript.getBytes());
      try
      {
         return new BeanShellScript(this + ": " + scriptName, stream);
      }
      finally
      {
         try
         {
            stream.close();
         }
         catch (IOException ignored)
         {
         }
      }
   }

   @ManagementOperation
   public String createScriptDeployment(String bshScript, String scriptName) throws DeploymentException
   {
      if (bshScript == null)
         throw new IllegalArgumentException("Null bean shell script.");

      if (scriptName == null)
         throw new IllegalArgumentException("Null script name.");

      BeanShellScript script = createBeanShellScript(bshScript, scriptName);
      Deployment deployment = new AbstractDeployment(scriptName);
      MutableAttachments mutableAttachments = ((MutableAttachments)deployment.getPredeterminedManagedObjects());
      mutableAttachments.addAttachment(BeanShellScript.class, script);

      deployer.addDeployment(deployment);
      deployer.process();

      return deployment.getName();
   }

   @ManagementOperation
   public void removeScriptDeployment(String scriptName) throws DeploymentException
   {
      deployer.removeDeployment(scriptName);
   }
}
