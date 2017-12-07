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

/**
 * The common interface for sub-deployer components which
 * perform the actual deployment services for application
 * components.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public interface SubDeployer
{
   /** The notification type send when a SubDeployer completes init */
   public static final String INIT_NOTIFICATION = "org.jboss.deployment.SubDeployer.init";
   /** The notification type send when a SubDeployer completes create */
   public static final String CREATE_NOTIFICATION = "org.jboss.deployment.SubDeployer.create";
   /** The notification type send when a SubDeployer completes start */
   public static final String START_NOTIFICATION = "org.jboss.deployment.SubDeployer.start";
   /** The notification type send when a SubDeployer completes stop */
   public static final String STOP_NOTIFICATION = "org.jboss.deployment.SubDeployer.stop";
   /** The notification type send when a SubDeployer completes destroy */
   public static final String DESTROY_NOTIFICATION = "org.jboss.deployment.SubDeployer.destroy";

   /**
    * Get the JMX ObjectName of the service that provides the SubDeployer
    * @return JMX ObjectName of the service
    * 
    * @jmx:managed-attribute
    */
   public ObjectName getServiceName();

   /**
    * Get an array of suffixes of interest to this subdeployer
    * @return array of suffix strings
    * 
    * @jmx:managed-attribute
    */
   public String[] getSuffixes();
   
   /**
    * Get the relative order of the specified suffixes
    * @return the relative order of the specified suffixes
    * 
    * @jmx:managed-attribute
    */
   public int getRelativeOrder();

   /**
    * The <code>accepts</code> method is called by MainDeployer to
    * determine which deployer is suitable for a DeploymentInfo.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @return a <code>boolean</code> value
    *
    * @jmx:managed-operation
    */
   boolean accepts(DeploymentInfo sdi);

   /**
    * The <code>init</code> method lets the deployer set a few properties
    * of the DeploymentInfo, such as the watch url.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    *
    * @jmx:managed-operation
    */
   void init(DeploymentInfo sdi) throws DeploymentException;

   /**
    * Set up the components of the deployment that do not
    * refer to other components
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    *
    * @jmx:managed-operation
    */
   void create(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>start</code> method sets up relationships with other components.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    *
    * @jmx:managed-operation
    */
   void start(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>stop</code> method removes relationships between components.
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    *
    * @jmx:managed-operation
    */
   void stop(DeploymentInfo sdi) throws DeploymentException;

   /**
    * The <code>destroy</code> method removes individual components
    *
    * @param sdi a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    *
    * @jmx:managed-operation
    */
   void destroy(DeploymentInfo sdi) throws DeploymentException;
}
