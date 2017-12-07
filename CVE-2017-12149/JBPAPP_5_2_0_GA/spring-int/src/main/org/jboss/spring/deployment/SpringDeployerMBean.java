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
package org.jboss.spring.deployment;

/**
 * MBean interface.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
public interface SpringDeployerMBean extends org.jboss.deployment.SubDeployerMBean
{
   /**
    * Returns true if this deployer can deploy the given DeploymentInfo.
    *
    * @return True if this deployer can deploy the given DeploymentInfo.
    */
   boolean accepts(org.jboss.deployment.DeploymentInfo di);

   /**
    * Describe <code>init</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws org.jboss.deployment.DeploymentException
    *          if an error occurs
    */
   void init(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

   /**
    * Describe <code>create</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws org.jboss.deployment.DeploymentException
    *          if an error occurs
    */
   void create(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

   /**
    * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws org.jboss.deployment.DeploymentException
    *          if an error occurs
    */
   void start(org.jboss.deployment.DeploymentInfo di) throws org.jboss.deployment.DeploymentException;

   /**
    * Undeploys the package at the url string specified. This will: Undeploy packages depending on this one.
    * Stop, destroy, and unregister all the specified mbeans Unload this package and packages this package
    * deployed via the classpath tag. Keep track of packages depending on this one that we undeployed so
    * that they can be redeployed should this one be redeployed.
    *
    * @param di the <code>DeploymentInfo</code> value to stop.
    */
   void stop(org.jboss.deployment.DeploymentInfo di);

   /**
    * Describe <code>destroy</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    */
   void destroy(org.jboss.deployment.DeploymentInfo di);
}
