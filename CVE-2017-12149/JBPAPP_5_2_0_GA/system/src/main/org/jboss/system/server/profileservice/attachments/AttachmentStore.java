/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.attachments;

import java.net.URI;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.virtual.VirtualFile;

/**
 * The AttachmentStore.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89127 $
 */
public interface AttachmentStore
{

   /**
    * Get the uri where the attachments are stored.
    * 
    * @return the attachment store root
    */
   URI getAttachmentStoreRoot();

   /**
    * Load the repository attachment meta data
    * 
    * @param deploymentCtx the deployment root
    * @return the persisted meta data or null if it does not exist
    * @throws Exception
    */
   RepositoryAttachmentMetaData loadMetaData(VirtualFile deploymentCtx) throws Exception;

   /**
    * Load a attachment.
    * 
    * @param deploymentCtx
    * @param attachment
    * @return
    * @throws Exception
    */
   PersistenceRoot loadAttachment(VirtualFile deploymentCtx, AttachmentMetaData attachment) throws Exception;

   /**
    * Persist the managed component.
    * 
    * @param ctx the deployment context name
    * @param comp the managed componenbt
    * @throws Exception
    */
   void updateDeployment(String ctx, ManagedComponent comp) throws Exception;

   /**
    * Remove a component from the attachment.
    * 
    * @param ctx the deployment context name
    * @param comp the managed componenbt
    * @throws Exception
    */
   void removeComponent(String ctx, ManagedComponent comp) throws Exception;
   
   /**
    * Create a MC deployment.
    * 
    * @param deployment
    * @return
    * @throws Exception
    */
   Deployment createDeployment(ProfileDeployment deployment) throws Exception;
   
}
