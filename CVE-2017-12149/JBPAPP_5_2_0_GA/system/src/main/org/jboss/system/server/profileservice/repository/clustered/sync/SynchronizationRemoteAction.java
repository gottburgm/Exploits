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

/**
 * Marker interface for a {@link SynchronizationAction} that a
 * node should handle by telling the other nodes in the cluster to execute the
 * {@link SynchronizationAction#getRepositoryContentModification() modification}.
 * <p>
 * An example of this would be handling of a 
 * {@link ContentModification.Type#REMOVE_TO_CLUSTER} wherein the
 * node driving the synchronization would tell the other nodes to
 * execute the remove by sending an RPC with the modification.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface SynchronizationRemoteAction<T extends SynchronizationActionContext>  
   extends SynchronizationAction<T>
{
   boolean isInitiation();
}
