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
package org.jboss.system;

import java.util.Collection;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.mx.util.ObjectNameFactory;
import org.w3c.dom.Element;

/**
 * ServiceController MBean interface.
 * 
 * @see org.jboss.system.Service
 */
public interface ServiceControllerMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:service=ServiceController");
   
   /**
    * Lists the ServiceContexts of deployed mbeans
    * 
    * @return the list of ServiceContexts for mbeans deployed through ServiceController.
    */
   List<ServiceContext> listDeployed();

   /**
    * The <code>listIncompletelyDeployed</code> method returns the
    * service contexts for the mbeans whose status is not CREATED,
    * RUNNING, STOPPED or DESTROYED. An MBean that has reached one
    * of the above states has its dependencies resolved.
    * 
    * @return a List<ServiceContext>
    */
   List<ServiceContext> listIncompletelyDeployed();

   /**
    * lists ObjectNames of deployed mbeans deployed through
    * serviceController.
    * 
    * @return a list of ObjectNames of deployed mbeans.
    */
   List<ObjectName> listDeployedNames();

   /**
    * Gets the Configuration attribute of the ServiceController object
    * 
    * @param objectNames Description of Parameter
    * @return The Configuration value
    * @throws Exception Description of Exception
    */
   String listConfiguration(ObjectName[] objectNames) throws Exception;

   /**
    * Go through the mbeans of the DeploymentInfo and validate that
    * they are in a state at least equal to that of the argument state
    * 
    * @param di the deployment info
    * @param state the deployment state
    */
   void validateDeploymentState(DeploymentInfo di, DeploymentState state);

   /**
    * Deploy the beans; deploy means "instantiate and configure" so the MBean
    * is created in the MBeanServer. You must call "create" and "start" separately
    * on the MBean to affect the service lifecycle deploy doesn't bother with service
    * lifecycle only MBean instanciation/registration/configuration.
    * 
    * @param config
    * @param loaderName
    * @return Description of the Returned Value
    * @throws Exception for any error
    */
   List<ObjectName> install(Element config, ObjectName loaderName) throws Exception;

   /**
    * Register the mbean against the microkernel with no dependencies.
    * 
    * @see #register(ObjectName, java.util.Collection)
    * @param serviceName the object name
    * @throws Exception for any error
    */
   void register(ObjectName serviceName) throws Exception;

   /**
    * Register the mbean against the microkernel with dependencies.
    * 
    * @param serviceName the object name
    * @param depends the dependencies
    * @throws Exception for any error
    */   
   void register(ObjectName serviceName, Collection<ObjectName> depends) throws Exception;

   /**
    * Create a service
    * 
    * @param serviceName Description of Parameter
    * @throws Exception Description of Exception
    */
   void create(ObjectName serviceName) throws Exception;

   /**
    * Create a service with given dependencies
    * 
    * @param serviceName Description of Parameter
    * @param depends the dependencies
    * @throws Exception Description of Exception
    */
   void create(ObjectName serviceName, Collection<ObjectName> depends) throws Exception;

   /**
    * Starts the indicated service
    * 
    * @param serviceName Description of Parameter
    * @throws Exception Description of Exception
    */
   void start(ObjectName serviceName) throws Exception;

   /**
    * Stops and restarts the indicated service
    * 
    * @param serviceName Description of Parameter
    * @throws Exception Description of Exception
    */
   void restart(ObjectName serviceName) throws Exception;

   /**
    * Stop the indicated service
    * 
    * @param serviceName Description of Parameter
    * @throws Exception Description of Exception
    */
   void stop(ObjectName serviceName) throws Exception;

   /**
    * Destroy the indicated service
    * 
    * @param serviceName Description of Parameter
    * @throws Exception Description of Exception
    */
   void destroy(ObjectName serviceName) throws Exception;

   /**
    * This MBean is going bye bye
    * 
    * @param objectName Description of Parameter
    * @throws Exception Description of Exception
    */
   void remove(ObjectName objectName) throws Exception;

   /**
    * Describe <code>shutdown</code> method here.
    */
   void shutdown();

   /**
    * Lookup the ServiceContext for the given serviceName
    * 
    * @param serviceName the service name
    * @return the service context
    */
   ServiceContext getServiceContext(ObjectName serviceName);
}
