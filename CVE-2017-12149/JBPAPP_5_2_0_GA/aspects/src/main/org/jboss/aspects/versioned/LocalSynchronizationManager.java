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
package org.jboss.aspects.versioned;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;
import org.jboss.util.id.GUID;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class LocalSynchronizationManager implements SynchronizationManager
{

   protected static Logger log = Logger.getLogger(DistributedSynchronizationManager.class);
   protected TransactionLocal txSynch = new TransactionLocal();

   protected Object tableLock = new Object();
   protected Hashtable objectTable = new Hashtable();
   protected Hashtable stateTable = new Hashtable();
   protected DistributedVersionManager versionManager;

   public LocalSynchronizationManager(DistributedVersionManager versionManager)
   {
      this.versionManager = versionManager;
   }

   public Object getObject(GUID guid)
   {
      synchronized (tableLock)
      {
         WeakReference ref = (WeakReference)objectTable.get(guid);
         if (ref != null)
         {
            return ref.get();
         }
         return null;
      }
   }

   public void putObject(GUID guid, Object obj)
   {
      synchronized (tableLock)
      {
         objectTable.put(guid, new WeakReference(obj));
      }
   }

   public DistributedState getState(GUID guid)
   {
      synchronized (tableLock)
      {
         return (DistributedState)stateTable.get(guid);
      }
   }

   public void putState(GUID guid, Object obj)
   {
      synchronized (tableLock)
      {
         stateTable.put(guid, obj);
      }
   }

   public void registerUpdate(Transaction tx, DistributedState state)
      throws Exception
   {
      if (tx == null) return;
      GUID guid = state.getGUID();
      DistributedStateSynchronization synch = (DistributedStateSynchronization)txSynch.get(tx);
      if (synch == null)
      {
         synch = new DistributedStateSynchronization(tx);
         txSynch.set(tx, synch);
         tx.registerSynchronization(synch);
         synch.updates().put(guid, state);
         return;
      }
      if (synch.updates().containsKey(guid)) return;
      synch.updates().put(guid, state);
   }

   public void createObjects(List newObjects) throws Exception
   {
      log.trace("in create Objects");
      for (int i = 0; i < newObjects.size(); i++)
      {
         DistributedState state = (DistributedState)newObjects.get(i);
         synchronized (tableLock)
         {
            objectTable.put(state.getGUID(), new WeakReference(state.getObject()));
            stateTable.put(state.getGUID(), state);
         }
      }
      sendNewObjects(newObjects);
   }

   public void sendNewObjects(List newObjects) throws Exception
   {
      // NOT YET IMPLEMENTED
   }

   protected void sendClusterUpdatesAndRelease(GUID globalTxId, List clusterUpdates) throws Exception
   {
      // HOOKS FOR DISTRIBUTION
   }
   protected void acquireRemoteLocks(GUID globalTxId, List guids) throws Exception
   {
      // HOOKS FOR DISTRIBUTION
   }

   public void noTxUpdate(DistributedUpdate update) throws Exception
   {
      // HOOKS FOR DISTRIBUTION
   }

   protected void releaseHeldLocks(List locks)
   {
      log.trace("releaseHeldLocks");
      for (int i = 0; i < locks.size(); i++)
      {
         try
         {
            DistributedState state = (DistributedState)locks.get(i);
            state.releaseWriteLock();
         }
         catch (Exception ignored)
         {
            // ignore exception because we want to release everything no matter what
         }
      }
      log.trace("end releaseHeldLocks");
   }

   private final class DistributedStateSynchronization implements Synchronization
   {
      final Transaction tx;
      HashMap managers = new HashMap();
      ArrayList locks; // quick lookup of locks for afterCompletion
      boolean optimisticLockPassed = false;
      ArrayList clusterUpdates;
      GUID globalTxId;
      public DistributedStateSynchronization(final Transaction tx)
      {
         this.tx = tx;
      }

      public HashMap updates() { return managers; }


      public void beforeCompletion()
      {
         ArrayList guidList = new ArrayList(managers.keySet());

         // Sort GUID list so that we have ordered locks and avoid deadlock
         Collections.sort(guidList);
         clusterUpdates = new ArrayList();
         locks = new ArrayList(); // keep track of locks so that we can release later on
         try
         {
            for (int i = 0; i < guidList.size(); i++)
            {
               GUID guid = (GUID)guidList.get(i);
               DistributedState manager = (DistributedState)managers.get(guid);
               log.trace("acquiring writelock in beforecompletion");
               manager.acquireWriteLock();
               // Remember lock so that we can release
               locks.add(manager);
               manager.checkOptimisticLock(tx);
               clusterUpdates.add(manager.createTxUpdate(tx));
            }
            globalTxId = new GUID();
            acquireRemoteLocks(globalTxId, guidList);
         }
         catch (RuntimeException ex)
         {
            // We need to release all locks that have been acquired
            releaseHeldLocks(locks);
            throw ex;
         }
         catch (Exception ex)
         {
            throw new RuntimeException(ex);
         }
         optimisticLockPassed = true;
      }

      public void afterCompletion(int status)
      {
         //possible statuses are committed and rolledback
         if (status != Status.STATUS_ROLLEDBACK)
         {
            Iterator it = managers.values().iterator();
            while (it.hasNext())
            {
               DistributedState manager = (DistributedState)it.next();
               try
               {
                  manager.mergeState(tx);
               }
               catch (Exception ignored)
               {
                  // REVISIT
                  // not sure what to do if there is a failure
                  log.error("afterCompletion failed on mergeState, cache is probably inconsistent and should be flushed", ignored);
               }
            }
            try
            {
               // an update should release the lock too
               sendClusterUpdatesAndRelease(globalTxId, clusterUpdates);
            }
            catch (Exception ignored)
            {
               // REVISIT
               // not sure what to do if there is a failure
               log.error("afterCompletion failed on mergeState, cache is probably inconsistent and should be flushed", ignored);
            }
         }
         if (optimisticLockPassed)
         {
            log.trace("afterCompletion releaseHeldLocks");
            releaseHeldLocks(locks);
         }
      }
   }
}
