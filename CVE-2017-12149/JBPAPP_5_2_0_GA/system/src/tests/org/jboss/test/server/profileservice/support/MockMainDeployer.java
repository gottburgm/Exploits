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
package org.jboss.test.server.profileservice.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.deployer.DeploymentStage;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.util.graph.Graph;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85526 $
 */
public class MockMainDeployer implements MainDeployer, MainDeployerStructure
{

   private Map<String, Deployment> deployments = new HashMap<String, Deployment>();
   
   private static final Logger log = Logger.getLogger(MockMainDeployer.class);
   
   public void prepareShutdown()
   {
      // FIXME prepareShutdown
      
   }

   public void shutdown()
   {
      // FIXME shutdown
      
   }

   public void addDeployment(Deployment deployment) throws DeploymentException
   {
      log.debug("add deployment: " + deployment);
      deployments.put(deployment.getName(), deployment);
   }

   public void change(String deploymentName, DeploymentStage stage) throws DeploymentException
   {
      // FIXME change
      
   }

   public void checkComplete() throws DeploymentException
   {
      // FIXME checkComplete
      
   }

   public void checkComplete(Deployment... deployment) throws DeploymentException
   {
      // FIXME checkComplete
      
   }

   public void checkComplete(String... names) throws DeploymentException
   {
      // FIXME checkComplete
      
   }

   public void checkStructureComplete(Deployment... deployments) throws DeploymentException
   {
      // FIXME checkStructureComplete
      
   }

   public void checkStructureComplete(String... names) throws DeploymentException
   {
      // FIXME checkStructureComplete
      
   }

   public void deploy(Deployment... deployments) throws DeploymentException
   {
      // FIXME deploy
      
   }

   public Graph<Map<String, ManagedObject>> getDeepManagedObjects(String name) throws DeploymentException
   {
      // FIXME getDeepManagedObjects
      return null;
   }

   public Deployment getDeployment(String name)
   {
      return this.deployments.get(name);
   }

   public DeploymentStage getDeploymentStage(String deploymentName) throws DeploymentException
   {
      // FIXME getDeploymentStage
      return null;
   }

   public DeploymentState getDeploymentState(String name)
   {
      // FIXME getDeploymentState
      return null;
   }

   public ManagedDeployment getManagedDeployment(String name) throws DeploymentException
   {
      // FIXME getManagedDeployment
      return null;
   }

   public Map<String, ManagedObject> getManagedObjects(String name) throws DeploymentException
   {
      // FIXME getManagedObjects
      return null;
   }

   public Collection<Deployment> getTopLevel()
   {
      // FIXME getTopLevel
      return null;
   }

   public void process()
   {
      log.debug("process");      
   }

   public boolean removeDeployment(Deployment deployment) throws DeploymentException
   {
      log.debug("remove deployment: " + deployment);
      return this.deployments.remove(deployment.getName()) != null;
   }

   public boolean removeDeployment(String name) throws DeploymentException
   {
      // FIXME removeDeployment
      return false;
   }

   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      // FIXME undeploy
      
   }

   public void undeploy(String... names) throws DeploymentException
   {
      // FIXME undeploy
      
   }

   public DeploymentContext getDeploymentContext(String name)
   {
      // FIXME getDeploymentContext
      return null;
   }

   public DeploymentContext getDeploymentContext(String name, boolean errorNotFound) throws DeploymentException
   {
      // FIXME getDeploymentContext
      return null;
   }

   public DeploymentUnit getDeploymentUnit(String name)
   {
      // FIXME getDeploymentUnit
      return null;
   }

   public DeploymentUnit getDeploymentUnit(String name, boolean errorNotFound) throws DeploymentException
   {
      // FIXME getDeploymentUnit
      return null;
   }

}

