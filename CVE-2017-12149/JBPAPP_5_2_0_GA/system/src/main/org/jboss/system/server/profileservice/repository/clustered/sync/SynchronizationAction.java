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
 * Encapsulates a single action needed to help synchronize the contents
 * of one node's repository with the rest of the cluster.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface SynchronizationAction<T extends SynchronizationActionContext>
{
   /**
    * Gets the contextual information for the set of actions of which 
    * this object is a member.
    * 
    * @return the context. Will not be <code>null</code>
    */
   T getContext();
   
   /**
    * Gets the metadata describing this action.
    * 
    * @return the metadata. Will not be <code>null</code>
    */
   ContentModification getRepositoryContentModification();
   
   /**
    * Cancel the action.
    */
   void cancel();
   
   /**
    * Execute the action and if successful mark it as complete.
    */
   void complete();
   
   /**
    * Gets whether {@link #complete()} has been invoked.
    * 
    * @return <code>true</code> if {@link #complete()} has been invoked
    */
   boolean isComplete();
   
   /**
    * Gets whether {@link #complete()} has been invoked.
    * 
    * @return <code>true</code> if {@link #complete()} has been invoked
    */
   boolean isCancelled();
}
