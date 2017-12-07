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

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HAPartition.HAMembershipListener;
import org.jboss.ha.framework.interfaces.HAPartition.HAPartitionStateTransfer;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *  Adds replication
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class DistributedSynchronizationManager extends LocalSynchronizationManager implements HAPartitionStateTransfer, HAMembershipListener
{
   protected static final Class[] STRING_TYPE = new Class[]{String.class};
   protected static final Class[] LIST_TYPE = new Class[]{List.class};
   protected static final Class[] LOCK_TYPES = new Class[]{String.class, GUID.class, List.class};
   
   protected static Logger log = Logger.getLogger(DistributedSynchronizationManager.class);
   protected HAPartition partition;
   protected String domainName;
   protected Hashtable heldLocks = new Hashtable();

   public DistributedSynchronizationManager(String domainName, DistributedVersionManager versionManager, HAPartition partition)
   {
      super(versionManager);
      this.partition = partition;
      this.domainName = domainName + "/SynchManager";
   }

   public void create() throws Exception
   {
      //partition.subscribeToStateTransferEvents(domainName, this);
      partition.registerRPCHandler(domainName, this);
   }

   public void start() throws Exception
   {
      pullState();
   }

   protected void pullState() throws Exception
   {
      Object[] args = {};
      ArrayList rsp = partition.callMethodOnCluster(domainName, "getCurrentState", args, null, true);
      if (rsp.size() > 0)
         setCurrentState((Serializable)rsp.get(0));
   }

   public Serializable getCurrentState()
   {
      if(log.isTraceEnabled() )
         log.trace("getCurrentState called");
      return stateTable;
   }

   public void setCurrentState(Serializable newState)
   {
      if( log.isTraceEnabled() )
         log.trace("setCurrentState called");
      try
      {
         synchronized (tableLock)
         {
            this.stateTable = (Hashtable)newState;
            log.trace("setCurrentState, size: " + stateTable.size());
            Iterator it = stateTable.values().iterator();
            while (it.hasNext())
            {
               DistributedState state = (DistributedState)it.next();
               if (objectTable.containsKey(state.getGUID())) continue;
               state.buildObject(this, versionManager);
            }
         }
      }
      catch (Exception ex)
      {
         log.error("failed to set state sent from cluster", ex);
      }
   }


   public void membershipChanged(Vector deadMembers, Vector newMembers, Vector allMembers)
   {
      for (int i = 0; i < deadMembers.size(); i++)
      {
         Hashtable held = (Hashtable)heldLocks.remove(deadMembers.get(i));
         if (held != null)
         {
            Iterator it = held.values().iterator();
            while (it.hasNext())
            {
               List list = (List)it.next();
               releaseHeldLocks(list);
            }
         }
      }
   }

   public void sendNewObjects(List newObjects) throws Exception
   {
      log.trace("sending new objects");
      try
      {
         Object[] args = {newObjects};
         checkResponses(partition.callMethodOnCluster(domainName, "addNewObjects", args, LIST_TYPE, true));
      }
      catch (Exception ex)
      {
         log.error("serious cache problems, data inconsistency is imminent", ex);
         throw ex;
      }

   }

   protected void sendClusterUpdatesAndRelease(GUID globalTxId, List clusterUpdates) throws Exception
   {
      try
      {
         Object[] args = {partition.getNodeName(), globalTxId, clusterUpdates};
         checkResponses(partition.callMethodOnCluster(domainName, "updateObjects", args, LOCK_TYPES, true));

      }
      catch (Exception ex)
      {
         log.error("serious cache problems, data inconsistency is imminent", ex);
         throw ex;
      }
   }
   protected void acquireRemoteLocks(GUID globalTxId, List guids) throws Exception
   {
      try
      {

         Object[] args = {partition.getNodeName(), globalTxId, guids};
         checkResponses(partition.callMethodOnCluster(domainName, "acquireLocks", args, LOCK_TYPES, true));
      }
      catch (Exception ex)
      {
         try
         {
            Object[] args = {partition.getNodeName()};
            partition.callMethodOnCluster(domainName, "releaseHeldLocks", args, STRING_TYPE, true);
         }
         catch (Exception ignored)
         {
         }
         throw ex;
      }
   }

   public void noTxUpdate(DistributedUpdate update) throws Exception
   {
      throw new RuntimeException("NOT IMPLEMENTED");
   }

   public void addNewObjects(List newObjects) throws Exception
   {
      // updates must be in table first
      synchronized (tableLock)
      {
         for (int i = 0; i < newObjects.size(); i++)
         {
            DistributedState state = (DistributedState)newObjects.get(i);
            // REVISIT synch
            stateTable.put(state.getGUID(), state);
         }
         for (int i = 0; i < newObjects.size(); i++)
         {
            DistributedState state = (DistributedState)newObjects.get(i);
            if (objectTable.containsKey(state.getGUID())) continue;
            state.buildObject(this, versionManager);
         }
      }
   }

   public void updateObjects(String nodeName, GUID globalTxId, ArrayList updates) throws Exception
   {
      log.trace("updateObjects");
      synchronized (tableLock)
      {
         for (int i = 0; i < updates.size(); i++)
         {
            DistributedUpdate update = (DistributedUpdate)updates.get(i);
            // REVISIT: synch
            DistributedState state = (DistributedState)stateTable.get(update.getGUID());
            state.mergeState(update);
            state.releaseWriteLock();
         }
      }
      Hashtable table = (Hashtable)heldLocks.get(nodeName);
      table.remove(globalTxId);
      log.trace("end updateObjects");
   }

   public void releaseHeldLocks(String nodeName, GUID globalTxId)
   {
      Hashtable held = (Hashtable)heldLocks.get(nodeName);
      if (held == null) return;

      List locks = (List)held.remove(globalTxId);
      if (locks != null) releaseHeldLocks(locks);
   }

   public void acquireLocks(String nodeName, GUID globalTxId, List list) throws Exception
   {
      log.trace("acquireLocks");
      ArrayList locks = new ArrayList();
      try
      {
         for (int i = 0; i < list.size(); i++)
         {
            GUID guid = (GUID)list.get(i);
            DistributedState state = getState(guid);
            state.acquireWriteLock();
            locks.add(state);
         }
         Hashtable held = (Hashtable)heldLocks.get(nodeName);
         if (held == null)
         {
            held = new Hashtable();
            heldLocks.put(nodeName, held);
         }
         held.put(globalTxId, locks);
      }
      catch (Exception ex)
      {
         releaseHeldLocks(locks);
         throw ex;
      }
      log.trace("end acquireLocks");
   }

   /**
    * Checks whether any of the responses are exceptions. If yes, re-throws
    * them (as exceptions or runtime exceptions).
    * @param rsps
    * @throws Exception
    */
   protected void checkResponses(List rsps) throws Exception {
      Object rsp;
      if(rsps != null) {
         for(Iterator it=rsps.iterator(); it.hasNext();) {
            rsp=it.next();
            if(rsp != null) {
               if(rsp instanceof RuntimeException)
                  throw (RuntimeException)rsp;
               if(rsp instanceof Exception)
                  throw (Exception)rsp;
            }
         }
      }
   }

}
