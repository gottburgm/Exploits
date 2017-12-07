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

import java.net.URI;

import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.system.server.profileservice.attachments.AttachmentMetaData;
import org.jboss.system.server.profileservice.attachments.AttachmentStore;
import org.jboss.system.server.profileservice.attachments.RepositoryAttachmentMetaData;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89127 $
 */
public class MockAttachmentStore implements AttachmentStore
{

   private final static VFSDeploymentFactory factory = VFSDeploymentFactory.getInstance();
   
   public URI getAttachmentStoreRoot()
   {
      return null;
   }

   public Deployment createDeployment(ProfileDeployment deployment) throws Exception
   {
      return loadDeploymentData(deployment);
   }
   
   public Deployment loadDeploymentData(ProfileDeployment deployment) throws Exception
   {
      if(deployment == null)
         throw new IllegalArgumentException("Null deployment");
      
      if(deployment.getRoot() == null)
         return new AbstractDeployment(deployment.getName());
      else
         return factory.createVFSDeployment(deployment.getRoot());
   }
   
   public void removeComponent(ProfileDeployment deployment, ManagedComponent comp) throws Exception
   {
      // FIXME removeComponent
      
   }

   public void updateDeployment(ProfileDeployment deployment, ManagedComponent comp) throws Exception
   {
      // nothing
   }

   public PersistenceRoot loadAttachment(VirtualFile deploymentCtx, AttachmentMetaData attachment) throws Exception
   {
      // FIXME loadAttachment
      return null;
   }

   public RepositoryAttachmentMetaData loadMetaData(VirtualFile deploymentCtx) throws Exception
   {
      // FIXME loadMetaData
      return null;
   }

   public void removeComponent(String ctx, ManagedComponent comp) throws Exception
   {
      // FIXME removeComponent
      
   }

   public void updateDeployment(String ctx, ManagedComponent comp) throws Exception
   {
      // FIXME updateDeployment
      
   }

}

