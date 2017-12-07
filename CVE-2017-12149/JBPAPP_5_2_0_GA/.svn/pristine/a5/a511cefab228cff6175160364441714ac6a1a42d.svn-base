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
 * {@link SynchronizationAction} that does nothing. Intended
 * for use in cases where a node is already in sync with the cluster with
 * respect to a particular item.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class NoOpSynchronizationAction<T extends SynchronizationActionContext> 
   extends AbstractSynchronizationAction<T>
{
   public NoOpSynchronizationAction(T context, 
         ContentModification modification)
   {
      super(context, modification);
   }
   
   // --------------------------------------------------------------  Protected

   @Override
   protected void doCancel()
   {
      // no-op      
   }

   @Override
   protected void doComplete()
   {
      // no-op
   }

   @Override
   protected boolean doPrepare()
   {
      return true;
   }
   
   @Override 
   protected void doCommit()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromCancelled()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromComplete()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromOpen()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromPrepared()
   {
      // no-op
   }

   @Override
   protected void doRollbackFromRollbackOnly()
   {
      // no-op
   }
   
   
   
}
