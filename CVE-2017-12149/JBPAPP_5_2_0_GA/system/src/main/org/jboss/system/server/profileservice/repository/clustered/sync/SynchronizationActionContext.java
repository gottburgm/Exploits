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


/**
 * Contextual information that a {@link SynchronizationAction} can use.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class SynchronizationActionContext
{
   private final SynchronizationId<?> id;
   private final RepositoryContentMetadata inProgressMetadata;
   
   public SynchronizationActionContext(SynchronizationId<?> id, 
         RepositoryContentMetadata inProgressMetadata)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Null id");
      }
      if (inProgressMetadata == null)
      {
         throw new IllegalArgumentException("Null inProgressMetadata");
      }
      this.id = id;
      // Defensive copy
      this.inProgressMetadata = new RepositoryContentMetadata(inProgressMetadata);
   }

   public SynchronizationId<?> getId()
   {
      return id;
   }

   public RepositoryContentMetadata getInProgressMetadata()
   {
      return inProgressMetadata;
   }
   
   
}
