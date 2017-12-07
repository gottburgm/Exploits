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
package org.jboss.system.server.profileservice.persistence.deployer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.attachments.AttachmentMetaData;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;
import org.jboss.system.server.profileservice.attachments.RepositoryAttachmentMetaData;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.virtual.VirtualFile;

/**
 * The ProfileService Persistence Deployer. This deployer applies the
 * persisted changes to an attachment.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89188 $
 */
public class ProfileServicePersistenceDeployer extends AbstractRealDeployer
{

   /** The managed prefix. */
   public static final String PERSISTED_ATTACHMENT_PREFIX = "PERISTED";
   
   /** The attachment store. */
   private AttachmentStore store;
   
   /** The persistence factory. */
   private PersistenceFactory persistenceFactory;
   
   /** The Logger. */
   private static final Logger log = Logger.getLogger(ProfileServicePersistenceDeployer.class);
   
   public ProfileServicePersistenceDeployer()
   {
      super();
      setAllInputs(true);
      setStage(DeploymentStages.PRE_REAL);
   }
   
   public PersistenceFactory getPersistenceFactory()
   {
      return persistenceFactory;
   }
   
   public void setPersistenceFactory(PersistenceFactory persistenceFactory)
   {
      this.persistenceFactory = persistenceFactory;
   }
   
   public AttachmentStore getAttachmentStore()
   {
      return store;
   }
   
   public void setAttachmentStore(AttachmentStore store)
   {
      this.store = store;
   }
   
   @Override
   protected void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      if(unit == null || unit instanceof VFSDeploymentUnit == false)
         return;
      try
      {
         applyPersistentChanges((VFSDeploymentUnit) unit);
      }
      catch(Throwable e)
      {
         log.warn("Failed to update the persisted attachment information", e);
      }
   }
   
   protected void applyPersistentChanges(VFSDeploymentUnit unit) throws Throwable
   {
      VirtualFile vf = unit.getRoot();
      RepositoryAttachmentMetaData metaData = store.loadMetaData(vf);
      if(metaData == null)
         return;      
      
      // Check if the deployment was modified
      if(PersistenceModificationChecker.hasBeenModified(unit, metaData.getLastModified()))
      {
         log.debug("Deployment was modified, not applying persisted information : " + unit);
         return;
      }
      //
      if(metaData.getAttachments() != null && metaData.getAttachments().isEmpty() == false)
      {
         for(AttachmentMetaData attachment: metaData.getAttachments())
         {
            Object instance = unit.getAttachment(attachment.getName());
            if(instance != null)
            {
               PersistenceRoot root = this.store.loadAttachment(vf, attachment);
               if(root == null)
               {
                  log.warn("Null persisted information for deployment: " + vf);
               }
               // update ...
               getPersistenceFactory().restorePersistenceRoot(root, instance, unit.getClassLoader());
            }
            else
            {
               log.warn("Could not apply changes, failed to find attachment: " + attachment.getName());
            }
         }         
      }      
   }
   

}
