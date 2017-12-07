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

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.SubDeployerExtMBean;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBean interface.
 */
public interface BeanShellSubDeployerMBean extends SubDeployerExtMBean
{
   /** The default ObjectName */
   public static final ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system:service=BeanShellSubDeployer");

   /**
    * Create a bsh deployment given the script content and name.
    * This creates a temp file using File.createTempFile(scriptName, ".bsh")
    * and then deploys this script via the main deployer.
    * 
    * @param bshScript the bsh script content
    * @param scriptName the bsh script name to use
    * @return the URL of the temporary file used as the deployment script
    * @throws DeploymentException thrown on failure to create the bsh script or deploy it.
    */
   URL createScriptDeployment(String bshScript, String scriptName) throws DeploymentException;

}
