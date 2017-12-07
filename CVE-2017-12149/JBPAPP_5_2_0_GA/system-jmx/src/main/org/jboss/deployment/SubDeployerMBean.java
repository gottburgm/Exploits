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
package org.jboss.deployment;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

/**
 * MBean interface for SubDeployers
 * 
 * @version <tt>$Revision: 81033 $</tt>
 */
public interface SubDeployerMBean extends ServiceMBean
{
   // Attributes ----------------------------------------------------
   
   /**
    * Get the JMX ObjectName of the service that provides the SubDeployer
    * @return JMX ObjectName of the service
    */
   ObjectName getServiceName();

   /**
    * Get an array of suffixes of interest to this subdeployer
    * @return array of suffix strings
    */
   String[] getSuffixes();

   /**
    * Get the relative order of the specified suffixes
    * @return the relative order of the specified suffixes
    */
   int getRelativeOrder();

   // Operations ----------------------------------------------------
   
   /**
    * The <code>accepts</code> method is called by MainDeployer
    * to determine which deployer is suitable for a DeploymentInfo.
    * @param sdi a <code>DeploymentInfo</code> value
    * @return a <code>boolean</code> value
    */
   boolean accepts(DeploymentInfo sdi);

   /**
    * The <code>init</code> method lets the deployer set a few
    * properties of the DeploymentInfo, such as the watch url.
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   void init(DeploymentInfo sdi) throws DeploymentException;

   /**
    * Set up the components of the deployment that do not
    * refer to other components.
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   void create(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>start</code> method sets up relationships
    * with other components.
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   void start(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>stop</code> method removes relationships
    * between components.
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   void stop(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>destroy</code> method removes individual
    * components
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    */
   void destroy(DeploymentInfo sdi) throws DeploymentException;

}
