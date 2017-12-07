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

import org.jboss.logging.Logger;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;

/**
 * Abstract superclass of {@link SynchronizationAction} 
 * implementations.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractSynchronizationAction<T extends SynchronizationActionContext>
      implements TwoPhaseCommitAction<T>
{
   public enum State { OPEN, CANCELLED, CLOSED, PREPARED, COMMITTED, ROLLEDBACK, ROLLBACK_ONLY}
   
   private Logger log = Logger.getLogger(getClass());
   
   private final ContentModification modification;
   private final T context;
   private boolean cancelled = false;
   private boolean complete = false;
   private State state = State.OPEN;
   
   /**
    * Create a new AbstractSynchronizationAction.
    *
    * @param context the overall context of the modification
    * @param modification the modification
    */
   protected AbstractSynchronizationAction(T context, 
         ContentModification modification)
   {
      if (context == null)
      {
         throw new IllegalArgumentException("Null context");
      }
      if (modification == null)
      {
         throw new IllegalArgumentException("Null modification");
      }
      this.context = context;
      this.modification = modification;
   }

   public ContentModification getRepositoryContentModification()
   {
      return modification;
   }

   public T getContext()
   {
      return context;
   }

   public void cancel()
   {
      if (state == State.OPEN)
      {
         doCancel();
         this.cancelled = true;
         this.state = State.CANCELLED;
      }
   }

   public void complete()
   {
      if (state == State.OPEN)
      {
         try
         {
            doComplete();
            this.state = State.CLOSED;
         }
         catch (Exception e)
         {
            this.state = State.ROLLBACK_ONLY;
         }
         finally
         {
            this.complete = true;            
         }
      }
   }

   public boolean prepare()
   {
      boolean result = false;
      switch (state)
      {         
         case OPEN:            
            // Not all actions get executed; e.g. reads on nodes that don't
            // get called. So we'll clean up.
            complete();
            if (state != State.CLOSED)
            {
               // break and return false
               break;
            }
            // else fall through
         case CLOSED:
            result = doPrepare();
            if (result)
            {
               state = State.PREPARED;
               result = true;
            }
            else
            {
               state = State.ROLLBACK_ONLY;
            }
            break;
         case PREPARED:
         case COMMITTED:
         case ROLLEDBACK:
            log.warn("Should not call prepare on an item with state " + state);
            // fall through
         case CANCELLED:
         case ROLLBACK_ONLY:
            // fall out and return false
            break;            
      }
      return result;
   }
   
   
   public void commit()
   {
      switch (state)
      {   
         case PREPARED:   
            doCommit();
            state = State.COMMITTED;
            break;
         case OPEN:
         case CANCELLED:
         case CLOSED:
         case ROLLBACK_ONLY:
         case COMMITTED:
         case ROLLEDBACK:
            log.warn("Should not call prepare on an item with state " + state);
            break;            
      }      
   }

   public void rollback()
   {
      switch (state)
      {
         case COMMITTED:
         case ROLLEDBACK:
            log.warn("Should not call prepare on an item with state " + state);
            return; 
         case OPEN:
            doRollbackFromOpen();
            break;
         case CANCELLED:
            doRollbackFromCancelled();
            break;
         case ROLLBACK_ONLY:
            doRollbackFromRollbackOnly();
            break;
         case CLOSED:
            doRollbackFromComplete();
            break;
         case PREPARED:   
            doRollbackFromPrepared();            
            break;           
      }   
      state = State.ROLLEDBACK;
   }

   public boolean isCancelled()
   {
      return this.cancelled;
   }

   public boolean isComplete()
   {
      return this.complete;
   }
   
   public State getState()
   {
      return state;
   }
   
   // --------------------------------------------------------------  Protected
   
   protected abstract void doCancel();
   protected abstract void doComplete() throws Exception;
   protected abstract boolean doPrepare();   
   protected abstract void doCommit();   
   protected abstract void doRollbackFromOpen();   
   protected abstract void doRollbackFromCancelled();   
   protected abstract void doRollbackFromRollbackOnly();   
   protected abstract void doRollbackFromComplete();   
   protected abstract void doRollbackFromPrepared();
   
   protected static RepositoryItemMetadata getMarkedRemovedItem(ContentModification base)
   {
      RepositoryItemMetadata result = base.getItem();
      if (result.isRemoved() == false)
      {
         result = new RepositoryItemMetadata(result);
         result.setRemoved(true);
      }
      
      return result;
   }

}