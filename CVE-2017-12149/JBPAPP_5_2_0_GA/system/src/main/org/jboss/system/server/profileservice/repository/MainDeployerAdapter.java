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
package org.jboss.system.server.profileservice.repository;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;

/**
 * A basic adapter for the MainDeployer.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89127 $
 */
public class MainDeployerAdapter
{
   
   /** The main deployer. */
   private MainDeployer mainDeployer;
   
   /** The attachment store. */
   private AttachmentStore store;
   
   public AttachmentStore getAttachmentStore()
   {
      return store;
   }
   
   public void setAttachmentStore(AttachmentStore store)
   {
      this.store = store;
   }
   
   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }
   
   public void setMainDeployer(MainDeployer mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }
   
   public void create() throws Exception
   {
      // Sanity check
      if(mainDeployer == null)
         throw new IllegalStateException("Null mainDeployer");
      if(store == null)
         throw new IllegalStateException("Null attachment store.");
   }
   
   /**
    * Add a deployment.
    * 
    * @param deployment the profile deployment.
    * @throws Exception
    */
   public void addDeployment(ProfileDeployment deployment) throws Exception
   {
      if(deployment == null)
         throw new IllegalArgumentException("Null deployment.");
   
      Deployment d = loadDeploymentData(deployment);
      mainDeployer.addDeployment(d);
   }
   
   /**
    * Remove a deployment.
    * 
    * @param name the deployment name.
    * @throws DeploymentException
    */
   public void removeDeployment(String name) throws DeploymentException
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      
      this.mainDeployer.removeDeployment(name);
   }
   
   public void removeDeployment(ProfileDeployment deployment) throws Exception
   {
      if(deployment == null)
         throw new IllegalArgumentException("Null deployment");
      
      removeDeployment(deployment.getName());  
   }
   
   /**
    * Process ...
    *
    */
   public void process()
   {
      this.mainDeployer.process();
   }
   
   /**
    * Check complete
    * 
    * @param names the deployment names
    * @throws DeploymentExcetion
    */
   public void checkComplete(String... names) throws DeploymentException
   {
      this.mainDeployer.checkComplete(names);
   }
   
   /**
    * CheckComplete
    * 
    * @throws DeploymentException
    */
   public void checkComplete() throws DeploymentException
   {
      this.mainDeployer.checkComplete();
   }
 
   /**
    * Create a MC deployment and load the persisted attachment data.
    * 
    * @param deployment the profile deployment.
    * @return the MC deployment.
    * @throws Exception
    */
   protected Deployment loadDeploymentData(ProfileDeployment deployment) throws Exception
   {
      return store.createDeployment(deployment);
   }
   
}

