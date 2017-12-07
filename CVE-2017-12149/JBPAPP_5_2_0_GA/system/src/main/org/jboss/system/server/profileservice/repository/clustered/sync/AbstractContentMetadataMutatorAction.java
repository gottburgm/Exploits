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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;

/**
 * Base class for {@link SynchronizationAction} implementations
 * that mutate the {@link RepositoryContentMetadata} as part of their
 * function.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractContentMetadataMutatorAction<T extends SynchronizationActionContext> 
   extends AbstractSynchronizationAction<T>
{
   private final RepositoryItemMetadata rollbackMetadata;

   /**
    * Create a new AbstractContentMetadataMutatorAction.
    *
    * @param context the overall context of the modification
    * @param modification the modification
    */
   protected AbstractContentMetadataMutatorAction(T context,
         ContentModification modification)
   {
      super(context, modification);
      
      RepositoryContentMetadata contentMetadata = context.getInProgressMetadata();
      RepositoryRootMetadata rmd = contentMetadata.getRepositoryRootMetadata(modification.getRootName());
      if (rmd == null)
      {
         throw new IllegalStateException("Root " + modification.getRootName() + " unknown to " + contentMetadata);
      }
      this.rollbackMetadata = rmd.getItemMetadata(modification.getItem().getRelativePathElements());
   }
   
   protected void updateContentMetadata()
   {
      ContentModification mod = getRepositoryContentModification();
      RepositoryContentMetadata contentMetadata = getContext().getInProgressMetadata();
      RepositoryRootMetadata rmd = contentMetadata.getRepositoryRootMetadata(mod.getRootName());
      rmd.getContent().add(mod.getItem());
   }
   
   protected void rollbackContentMetadata()
   {
      ContentModification mod = getRepositoryContentModification();
      RepositoryContentMetadata contentMetadata = getContext().getInProgressMetadata();
      RepositoryRootMetadata rmd = contentMetadata.getRepositoryRootMetadata(mod.getRootName());
      if (rollbackMetadata == null)
      {
         rmd.getContent().remove(mod.getItem());
      }
      else
      {
         rmd.getContent().add(rollbackMetadata);
      }
   }

}
