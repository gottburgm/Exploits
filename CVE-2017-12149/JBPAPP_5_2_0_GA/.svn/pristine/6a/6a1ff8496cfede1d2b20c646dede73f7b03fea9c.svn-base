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

package org.jboss.ha.timestamp;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContextAware;
import org.jboss.util.threadpool.ThreadPool;

/**
 * Service that tracks {@link TimestampDiscrepancy} information for current
 * and past members of the cluster.
 * <p>
 * Discrepancy information is not persisted, so no knowledge of past members
 * is retained across a cluster restart.
 * </p>
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class TimestampDiscrepancyService 
   implements KernelControllerContextAware
{
   private static final Logger log = Logger.getLogger(TimestampDiscrepancyService.class);
   
   private static final Class<?>[] PUSH_DISCREPANCY_MAP_TYPES = new Class[]{ RemoteDiscrepancies.class };
   private static final Class<?>[] NULL_TYPES = new Class[0];
   private static final Object[] NULL_ARGS = new Object[0];
   
   private String serviceHAName;
   private final RpcHandler rpcTarget = new RpcHandler(); 
   private final DRMListener drmListener = new DRMListener(); 
   private final TreeMap<Server, TimestampDiscrepancy> discrepancies = new TreeMap<Server, TimestampDiscrepancy>();
   private final TreeSet<Server> liveServers = new TreeSet<Server>();
   private final Map<String, ClusterNode> nodesByName = new ConcurrentHashMap<String, ClusterNode>();
   private int maxDeadServers = 100;
   private long minDeadServerTime = 7 * 24 * 60 * 60 * 1000;   // 30 days
   private HAPartition partition;
   private long lastStatusCheck;
   private long minStatusCheckFrequency = 30* 60 * 60 * 1000; // 20 mins
   private volatile boolean statusCheckRequired = true;
   private long lastPurge;
   private long minPurgeFrequency = 60 * 60 * 1000; // one hour
   private final List<TimestampDiscrepancyObserver> observers = new CopyOnWriteArrayList<TimestampDiscrepancyObserver>();
   private boolean coordinator;
   private ThreadPool threadPool;
   private final Map<ClusterNode, Map<Server, TimestampDiscrepancy>> unresolvedRemoteDependencies = new HashMap<ClusterNode, Map<Server, TimestampDiscrepancy>>();
   private boolean deadMembersKnown = false;
   
   // -------------------------------------------------------------  Properties
   
   public HAPartition getPartition()
   {
      return partition;
   }

   public void setPartition(HAPartition partition)
   {
      this.partition = partition;
   }
   
   /**
    * Gets the name under which we register ourself with the HAPartition.
    * 
    * @return the name
    */
   public String getServiceHAName()
   {
      return serviceHAName;
   }

   /**
    * Sets the name under which we register ourself with the HAPartition.
    * 
    * @param serviceHAName the name
    */
   public void setServiceHAName(String serviceHAName)
   {
      this.serviceHAName = serviceHAName;
   }

   /**
    * Gets the maximum number of records for non-active servers we'll
    * retain in our @{link {@link #getTimestampDiscrepancies(boolean) discrepancies map}
    * after which we can attempt to remove non-active servers who we heard
    * from less recently than {@link #getMinDeadServerTime()}.
    * <p>
    * An active server is one that is include in the most recent
    * view propagated by the 
    * {@link HAPartition#getDistributedReplicantManager() HAPartition's DRM}.
    * </p>
    * 
    * @return the max number of servers
    */
   public int getMaxDeadServers()
   {
      return maxDeadServers;
   }

   public void setMaxDeadServers(int maxDeadServers)
   {
      this.maxDeadServers = maxDeadServers;
   }

   /**
    * Gets the minimum period of time since last checking with the server
    * that we'll retain a non-active server in our 
    * @{link {@link #getTimestampDiscrepancies(boolean) discrepancies map}.
    * <p>
    * An active server is one that is included in the most recent
    * view propagated by the 
    * {@link HAPartition#getDistributedReplicantManager() HAPartition's DRM}.
    * </p>
    * 
    * @return the minimum period of time, in ms
    * 
    * @see #getMaxDeadServers()
    */
   public long getMinDeadServerTime()
   {
      return minDeadServerTime;
   }

   public void setMinDeadServerTime(long minDeadServerTime)
   {
      this.minDeadServerTime = minDeadServerTime;
   }

   /**
    * Gets the minimum period between periodic status checks. A status check
    * is a request to the cluster for each server's local timestamp, used
    * to build the {@link TimestampDiscrepancy} for that server.
    * <p>
    * A status check can occur more frequently than this value if the service
    * determines it is necessary, for example following a view change.
    * </p>
    * <p>
    * The default value is 20 minutes.
    * </p>
    * 
    * @return the minimum frequency in ms
    */
   public long getMinStatusCheckFrequency()
   {
      return minStatusCheckFrequency;
   }

   public void setMinStatusCheckFrequency(long minStatusCheckFrequency)
   {
      this.minStatusCheckFrequency = minStatusCheckFrequency;
   }

   /**
    * Gets the minimum period between attempts to purge non-active members
    * from the @{link {@link #getTimestampDiscrepancies(boolean) discrepancies map}.
    * <p>
    * Default is one hour
    * </p>
    * 
    * @return the minimum frequency in ms
    * 
    * @see #getMaxDeadServers() 
    * @see #getMinDeadServerTime()
    */
   public long getMinPurgeFrequency()
   {
      return minPurgeFrequency;
   }

   public void setMinPurgeFrequency(long minPurgeFrequency)
   {
      this.minPurgeFrequency = minPurgeFrequency;
   }

   /**
    * Gets the time of the last request to the cluster for timestamps.
    * 
    * @return the time of the last request, in ms since the epoch
    */
   public long getLastStatusCheck()
   {
      return lastStatusCheck;
   }

   /**
    * Gets whether an event has occurred (e.g. a view change) that requires
    * a status check.
    * 
    * @return <code>true</code> if a check is required
    */
   public boolean isStatusCheckRequired()
   {
      return statusCheckRequired;
   }

   /**
    * Gets the time of the last attempt to purge non-active members from
    * the @{link {@link #getTimestampDiscrepancies(boolean) discrepancies map}.
    * 
    * @return the time of the last purge, in ms since the epoch
    */
   public long getLastPurge()
   {
      return lastPurge;
   }

   /**
    * Injects a thread pool for use in dispatching asynchronous tasks. If
    * no thread pool is injected, threads will be spawned to handle such
    * tasks.
    * <p>
    * Asynchronous tasks are generally associated with view changes.
    * </p>
    * 
    * @param threadPool the thread pool
    */
   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   // -----------------------------------------------------------------  Public

   /**
    * Gets the map of TimestampDiscrepancy data tracked by this service.
    * 
    * @param allowStatusCheck is calling into the cluster to update the 
    *                         discrepancies map before returning allowed?
    * 
    * @return the map. Will not return <code>null</code>
    */
   public Map<ClusterNode, TimestampDiscrepancy> getTimestampDiscrepancies(boolean allowStatusCheck)
   {
      if (allowStatusCheck)
      {
         statusCheck();
      }
      
      purgeDeadEntries();
      
      synchronized (discrepancies)
      {
         HashMap<ClusterNode, TimestampDiscrepancy> result = new HashMap<ClusterNode, TimestampDiscrepancy>();
         for (Map.Entry<Server, TimestampDiscrepancy> entry : discrepancies.entrySet())
         {
            result.put(entry.getKey().getNode(), entry.getValue());
         }
         return result;
      }
   }
   
   /**
    * Gets the TimestampDiscrepancy data associated with a particular node.
    * 
    * @param node the node
    * @param allowStatusCheck is calling into the cluster to update the 
    *                         discrepancies map before returning allowed?
    * 
    * @return the discrepancy data. Will return <code>null</code> if no data
    *         for <code>node</code> is stored.
    */
   public TimestampDiscrepancy getTimestampDiscrepancy(ClusterNode node, boolean allowStatusCheck)
   {
      if (allowStatusCheck)
      {
         statusCheck();
      }
      
      purgeDeadEntries();
      
      synchronized (discrepancies)
      {
         return discrepancies.get(new Server(node));
      }
   }
   
   /**
    * Gets the TimestampDiscrepancy data associated with a particular node.
    * 
    * @param node the name of the node
    * @param allowStatusCheck is calling into the cluster to update the 
    *                         discrepancies map before returning allowed?
    * 
    * @return the discrepancy data. Will return <code>null</code> if no data
    *         for <code>node</code> is stored.
    */
   public TimestampDiscrepancy getTimestampDiscrepancy(String nodeName, boolean allowStatusCheck)
   {
      ClusterNode node = nodesByName.get(nodeName);
      return node == null ? null : getTimestampDiscrepancy(node, allowStatusCheck);
   }
   
   /**
    * Gets whether the particular node is one of the servers this
    * service regards as active (i.e. part of the cluster topology for 
    * the service).
    * 
    * @param node the node
    * @return <code>true</code> if the node is active, false otherwise
    */
   public boolean isServerActive(ClusterNode node)
   {
      synchronized (liveServers)
      {
         return liveServers.contains(new Server(node));
      }
   }
   
   /**
    * Bring the service into active operation.
    * 
    * @throws Exception
    */
   public void start() throws Exception
   {
      partition.registerRPCHandler(getServiceHAName(), rpcTarget);
      
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      drm.add(getServiceHAName(), partition.getClusterNode());
      coordinator = drm.isMasterReplica(getServiceHAName());
      drm.registerListener(getServiceHAName(), drmListener);
      
      statusCheck();
   }
   
   /**
    * Remove the service from active operation.
    * 
    * @throws Exception
    */
   public void stop() throws Exception
   {
      DistributedReplicantManager drm = partition.getDistributedReplicantManager();
      drm.unregisterListener(getServiceHAName(), drmListener);
      drm.remove(getServiceHAName());
      
      coordinator = false;

      partition.unregisterRPCHandler(getServiceHAName(), rpcTarget);
      
      synchronized (liveServers)
      {
         liveServers.clear();
      }
      
      synchronized (unresolvedRemoteDependencies)
      {
         unresolvedRemoteDependencies.clear();
      }
   }
   
   /**
    * Register a {@link TimestampDiscrepancyObserver} with this service.
    * 
    * @param observer the observer
    */
   public void registerObserver(TimestampDiscrepancyObserver observer)
   {
      if (observer != null)
      {
         observers.add(observer);
      }
   }
   
   /**
    * Unregister a {@link TimestampDiscrepancyObserver} with this service.
    * 
    * @param observer the observer
    */
   public void unregisterObserver(TimestampDiscrepancyObserver observer)
   {
      if (observer != null)
      {
         observers.remove(observer);
      }
   }
   
   // -------------------------------------------- KernelControllerContextAware

   /**
    * Registers the context name as the {@link #setServiceHAName(String) serviceHAName}
    * if it is not already set.
    * 
    * {@inheritDoc}
    */
   public void setKernelControllerContext(KernelControllerContext context) throws Exception
   {
      if (context != null && serviceHAName == null)
      {
         setServiceHAName(context.getName().toString());
      }      
   }

   /**
    * This implementation is a no-op.
    * 
    * {@inheritDoc}
    */
   public void unsetKernelControllerContext(KernelControllerContext context) throws Exception
   {
      // no-op
   }
   
//   // --------------------------------------------------- HAMembershipListener
//
//   @SuppressWarnings("unchecked")
//   public synchronized void membershipChangedDuringMerge(Vector deadMembers, Vector newMembers, Vector allMembers,
//         Vector originatingGroups)
//   {
//      boolean wasCoordinator = coordinator;
//      
//      membershipChanged(deadMembers, newMembers, allMembers);
//      
//      if (wasCoordinator && !coordinator)
//      {
//         // There's been a merge and we are no longer coordinator. Asynchronously
//         // tell the rest of the cluster about our knowledge of timestamps
//         Runnable r = getDiscrepancyPushTask();         
//         executeRunnable(r, getServiceHAName() + "-DiscrepancyMapPusher");
//      }      
//      else if (coordinator)
//      {
//         // Other nodes may depend on us having timestamp knowledge, so be
//         // aggressive about getting it -- don't wait for a request. 
//         // We also need to tell whoever merged with us about our 
//         // knowledge of timestamps
//         final Runnable push = getDiscrepancyPushTask();
//         Runnable r = new Runnable()
//         {
//            public void run()
//            {
//               statusCheck();
//               push.run();
//            }
//         };
//         
//         executeRunnable(r, getServiceHAName() + "-AsyncStatusCheck");
//      }
//   }
//
//   @SuppressWarnings("unchecked")
//   public void membershipChanged(Vector deadMembers, Vector newMembers, Vector allMembers)
//   {
//      synchronized (liveServers)
//      {
//         if (deadMembers != null)
//         {
//            for (Object dead : deadMembers)
//            {
//               if (dead instanceof ClusterNode)
//               {
//                  liveServers.remove(new Server((ClusterNode) dead));
//               }
//            }
//         }
//      }   
//      
//      if (newMembers != null && newMembers.size() > 0)
//      {
//         this.statusCheckRequired = true;
//      }
//      
//      coordinator = this.partition.getClusterNode().equals(allMembers.get(0));
//   }
   
   // ----------------------------------------------------------------- Private

   private synchronized void statusCheck()
   {
      if (statusCheckRequired || (System.currentTimeMillis() - lastStatusCheck > minStatusCheckFrequency))
      {
         try
         {
            long requestSent = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            List rsps = partition.callMethodOnCluster(getServiceHAName(), "getLocalTimestamp", NULL_ARGS, NULL_TYPES, true);
            long responseReceived = System.currentTimeMillis();
            long mcastTime = responseReceived - requestSent;
            
            Map<ClusterNode, TimestampDiscrepancy> rspBySender = new HashMap<ClusterNode, TimestampDiscrepancy>();
            if (rsps != null)
            {
               for (Object rsp : rsps)
               {
                  if (rsp instanceof TimestampResponse)
                  {
                     TimestampResponse tr = (TimestampResponse) rsp;
                     rspBySender.put(tr.getResponder(), 
                           new TimestampDiscrepancy(tr.getTimestamp(), requestSent, responseReceived));
                  }
                  else if (rsp != null)
                  {
                     log.warn("Unknown status check response " + rsp);
                  }
               }
            }
            
            if (mcastTime > 250)
            {
               // Multicasting the RPC introduced a high possible error;
               // see if multiple unicast is better
               List<ClusterNode> nodes = partition.getDistributedReplicantManager().lookupReplicantsNodes(getServiceHAName());
               for (ClusterNode node : nodes)
               {
                  if (node.equals(this.partition.getClusterNode()))
                  {
                     continue;
                  }
                  
                  try
                  {
                     long singleRequestSent = System.currentTimeMillis();
                     Object rsp = partition.callMethodOnNode(getServiceHAName(), "getLocalTimestamp", NULL_ARGS, NULL_TYPES, mcastTime, node);
                     long singleResponseReceived = System.currentTimeMillis();
                     long elapsed = singleResponseReceived - singleRequestSent;
                     if (elapsed < mcastTime) // better result than multicast
                     {
                        if (rsp instanceof TimestampResponse)
                        {
                           TimestampResponse tr = (TimestampResponse) rsp;
                           rspBySender.put(tr.getResponder(), 
                                 new TimestampDiscrepancy(tr.getTimestamp(), singleRequestSent, singleResponseReceived));
                        }
                        else if (rsp != null)
                        {
                           log.warn("Unknown status check response " + rsp);
                        }                     
                     }
                  }
                  catch (Throwable e)
                  {
                     if (e instanceof Error)
                     {
                        throw (Error) e;
                     }
                     log.error("Caught exception requesting timestamp from node " + node, e);
                  }
               }
            }
            
            synchronized (discrepancies)
            {
               synchronized (liveServers)
               {
                  for (Map.Entry<ClusterNode, TimestampDiscrepancy> entry : rspBySender.entrySet())
                  {
                     Server s = new Server(entry.getKey());
                     TimestampDiscrepancy latest = entry.getValue();
                     TimestampDiscrepancy existing = discrepancies.get(s);
                     if (existing == null 
                           || latest.getDiscrepancyRange() <= existing.getDiscrepancyRange()
                           || liveServers.contains(s) == false)
                     {
                        updateTimestampDiscrepancy(s, latest, true);
                     }
                     else
                     {
                        // We already have an entry for this live server with a 
                        // narrower range that we'd prefer to keep
                        // If the new entry doesn't fit within the parameters
                        // of the old, we have to replace the old
                        if (existing.getMinDiscrepancy() < latest.getMinDiscrepancy()
                              || existing.getMaxDiscrepancy() > latest.getMaxDiscrepancy())
                        {
                           updateTimestampDiscrepancy(s, latest, true);
                        }
                        else
                        {
                           // Re-store existing, but with the new key
                           updateTimestampDiscrepancy(s, existing, true);
                        }                        
                     }
                  }
               }
            }
            
            statusCheckRequired = false;
            lastStatusCheck = System.currentTimeMillis();
         }
         catch (Exception e)
         {
            log.error("Caught exception in status check", e);
         }
      }
      
      getDeadMembersFromCoordinator();
   }
   
   private void getDeadMembersFromCoordinator()
   {
      if (!deadMembersKnown)
      {
         try
         {
            DistributedReplicantManager drm = partition.getDistributedReplicantManager();
            List<ClusterNode> nodes = drm.lookupReplicantsNodes(getServiceHAName());
            ClusterNode coord = (nodes != null && nodes.size() > 0 ? nodes.get(0) : null);
            if (coord != null && coord.equals(partition.getClusterNode()) == false)
            {
               Object rsp = partition.callMethodOnNode(getServiceHAName(), "getDiscrepancies", NULL_ARGS, NULL_TYPES, 60000, coord);
               if (rsp instanceof RemoteDiscrepancies)
               {
                  handleRemoteDiscrepancies((RemoteDiscrepancies) rsp);
                  deadMembersKnown = true;
               }
               else
               {
                  log.error("No valid response from coordinator: " + rsp);
               }
            }
         }
         catch (Throwable e)
         {
            if (e instanceof Error)
            {
               throw (Error) e;
            }
            log.error("Caught exception pulling dead member records from coordinator", e);
         }
      }
   }

   /**
    * Notification from the DRMListener of a service topology change.
    */
   private void replicantsChanged(List<ClusterNode> newReplicants, boolean merge)
   {
      boolean wasCoordinator = coordinator;
      
      Set<Server> newServers = new HashSet<Server>();
      for (Object replicant : newReplicants)
      {
         newServers.add(new Server((ClusterNode) replicant));
      }
      
      boolean hasAdds = false;
      
      synchronized (liveServers)
      {
         for (Server s : newServers)
         {
            if (liveServers.contains(s) == false)
            {
               liveServers.add(s);
               hasAdds = true;
            }
         }
         
         if (liveServers.size() != newServers.size())
         {
            for (Iterator<Server> it = liveServers.iterator(); it.hasNext(); )
            {
               Server s = it.next();
               if (newServers.contains(s) == false)
               {
                  it.remove();
               }
            }            
         }
      }
      
      if (hasAdds)
      {
         statusCheckRequired = true;
      }
      
      DistributedReplicantManager drm  = partition.getDistributedReplicantManager();
      this.coordinator = drm.isMasterReplica(getServiceHAName());
      
      if (wasCoordinator && !coordinator)
      {
         // There's been a merge and we are no longer coordinator. Asynchronously
         // tell the rest of the cluster about our knowledge of timestamps
         Runnable r = getDiscrepancyPushTask();         
         executeRunnable(r, getServiceHAName() + "-DiscrepancyMapPusher");
      }      
      else if (coordinator)
      {
         // Other nodes may depend on us having timestamp knowledge, so be
         // aggressive about getting it -- don't wait for a request. 
         // We also need to tell whoever merged with us about our 
         // knowledge of timestamps
         final Runnable push = getDiscrepancyPushTask();
         Runnable r = new Runnable()
         {
            public void run()
            {
               statusCheck();
               push.run();
            }
         };
         
         executeRunnable(r, getServiceHAName() + "-AsyncStatusCheck");
      }
   }
   
   private void executeRunnable(final Runnable r, String threadName)
   {
      if (threadPool != null)
      {
         threadPool.run(r);
      }
      else
      {
         final Thread t = new Thread(r, threadName);
         t.setDaemon(true);
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               t.setContextClassLoader(r.getClass().getClassLoader());
               return null;
            }
         });
         t.start();
      }
   }
   
   private synchronized void purgeDeadEntries()
   {
      if ((System.currentTimeMillis() - lastPurge > minPurgeFrequency))
      {
         synchronized (discrepancies)
         {
            synchronized (liveServers)
            {
               lastPurge = System.currentTimeMillis();
               
               Server oldestLive = liveServers.isEmpty()? null : liveServers.first();
               Set<Server> deadServers = oldestLive == null ? discrepancies.keySet() : discrepancies.headMap(oldestLive).keySet();
               
               int excess = deadServers.size() - maxDeadServers;
               if (excess > 0)
               {
                  Set<Server> toClean = new HashSet<Server>();
                  for (Server server : deadServers)
                  {
                     long min = System.currentTimeMillis() - minDeadServerTime;
                     if (excess > 0 && server.getTimestampChecked() < min)
                     {
                        for (TimestampDiscrepancyObserver observer : observers)
                        {
                           if (observer.canRemoveDeadEntry(server.getNode(), server.getTimestampChecked()) == false)
                           {
                              // vetoed
                              continue;
                           }
                        }
                        
                        // If we reached here it wasn't vetoed
                        toClean.add(server);
                        excess--;
                     }
                     else
                     {
                        // We've removed enough or the rest are newer than 
                        // the minimum, so stop checking
                        break;
                     }
                  }
                  
                  for (Server toRemove : toClean)
                  {
                     discrepancies.remove(toRemove);
                  }
               }              
               
            }            
         }
      }
   }
   
   private void updateTimestampDiscrepancy(Server server, TimestampDiscrepancy discrepancy, boolean live)
   {
      discrepancies.put(server, discrepancy);
      nodesByName.put(server.getNode().getName(), server.getNode());
      if (live)
      {
         liveServers.add(server);
      }
      
      synchronized (unresolvedRemoteDependencies)
      {
         Map<Server, TimestampDiscrepancy> unresolved = unresolvedRemoteDependencies.remove(server.getNode());
         if (unresolved != null)
         {
            convertRemoteDiscrepanciesToLocalTime(unresolved, discrepancy);
         }
      }
      
      for (TimestampDiscrepancyObserver observer : observers)
      {
         observer.timestampDiscrepancyChanged(server.getNode(), discrepancy);
      }
   }

   /**
    * Handles a pushRemoteDiscrepancies call from a remote node.
    */
   private void handleRemoteDiscrepancies(RemoteDiscrepancies remote)
   {
      ClusterNode sender = remote.getSender();
      Map<Server, TimestampDiscrepancy> remoteDiscrepancies = remote.getDiscrepancies();
      
      synchronized (discrepancies)
      {
         TimestampDiscrepancy senderDiscrepancy = discrepancies.get(new Server(sender));
         if (senderDiscrepancy == null)
         {
            // We don't know how to convert these to local time. Just cache
            // them until we can.
            synchronized (unresolvedRemoteDependencies)
            {
               unresolvedRemoteDependencies.put(sender, remoteDiscrepancies);
            }
         }
         else
         {
            convertRemoteDiscrepanciesToLocalTime(remoteDiscrepancies, senderDiscrepancy);
         }
      }
   }

   /**
    * Takes a set of discrepancies provided by another node and adds any
    * missing entries to our discrepancy set, *after* adjusting the
    * TimestampDiscrepancy objects to incorporate our discrepancy with
    * the node that provided the set.
    */
   private void convertRemoteDiscrepanciesToLocalTime(Map<Server, TimestampDiscrepancy> remoteDiscrepancies, 
                                                      TimestampDiscrepancy senderDiscrepancy)
   {
      for (Map.Entry<Server, TimestampDiscrepancy> entry : remoteDiscrepancies.entrySet())
      {
         Server key = entry.getKey();
         if (discrepancies.get(key) == null)
         {
            // A node we didn't know about
            discrepancies.put(new Server(key, senderDiscrepancy), 
                              new TimestampDiscrepancy(entry.getValue(), senderDiscrepancy));
            ClusterNode node = key.getNode();
            nodesByName.put(node.getName(), node);
         }
      }
   }

   /**
    * Create a Runnable that will push a copy of our discrepancies map to the 
    * cluster.
    */
   private Runnable getDiscrepancyPushTask()
   {
      Map<Server, TimestampDiscrepancy> map = null;
      synchronized (discrepancies)
      {
         map = new HashMap<Server, TimestampDiscrepancy>(discrepancies);
      }
      
      final RemoteDiscrepancies arg = new RemoteDiscrepancies(partition.getClusterNode(), map);
      final HAPartition haPartition = this.partition;
      Runnable r = new Runnable() 
      {
         public void run()
         {
            try
            {
               haPartition.callMethodOnCluster(getServiceHAName(), 
                                               "pushDiscrepancyMap", 
                                               new Object[] { arg }, 
                                               PUSH_DISCREPANCY_MAP_TYPES, true);
            }
            catch (Exception e)
            {
               log.error("Exception pushing Discrepancy map to cluster", e);
            }
         }
      };
      return r;
   }
   
   /** Object we register with the HAPartition */
   public class RpcHandler
   {
      public RemoteDiscrepancies getDiscrepancies()
      {
         Map<Server, TimestampDiscrepancy> result = null;
         synchronized (discrepancies)
         {
            result = new HashMap<Server, TimestampDiscrepancy>(discrepancies);
         }
         return new RemoteDiscrepancies(partition.getClusterNode(), result);
      }
      
      public TimestampResponse getLocalTimestamp()
      {
         return new TimestampResponse(partition.getClusterNode());
      }
      
      public void pushDiscrepancyMap(RemoteDiscrepancies remote)
      {
         handleRemoteDiscrepancies(remote);
      }
   }
   
   /** Object we register with the DRM */
   @SuppressWarnings("unchecked")
   private class DRMListener implements ReplicantListener
   {
      public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId, boolean merge)
      {
         TimestampDiscrepancyService.this.replicantsChanged(newReplicants, merge);
      }      
   }
   
   
   public static class Server implements Serializable, Comparable<Server>
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 4477441836405966100L;
      
      private final ClusterNode node;
      private final long timestampChecked;
      
      private Server(ClusterNode node)
      {
         if (node == null)
         {
            throw new IllegalArgumentException("Null node");
         }
         this.node = node;
         this.timestampChecked = System.currentTimeMillis();
      }
      
      private Server(Server base, TimestampDiscrepancy offset)
      {
         this.node = base.node;
         this.timestampChecked = offset.getMaxLocalTimestamp(base.timestampChecked);
      }

      public ClusterNode getNode()
      {
         return node;
      }

      public long getTimestampChecked()
      {
         return timestampChecked;
      }

      public int compareTo(Server o)
      {
         if (this.node.equals(o.node))
            return 0;
         return (int) (this.timestampChecked - o.timestampChecked);
      }

      @Override
      public boolean equals(Object obj)
      { 
         if (this == obj)
            return true;
         
         if (obj instanceof Server)
         {
            return this.node.equals(((Server) obj).node);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return node.hashCode();
      }

      @Override
      public String toString()
      {
         StringBuilder sb = new StringBuilder(getClass().getName());
         sb.append("{node=");
         sb.append(node);
         sb.append('}');
         return sb.toString();
      }
      
   }
   
   public static class TimestampResponse implements Serializable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -9171752596968923020L;
      
      private final ClusterNode responder;
      private final long timestamp = System.currentTimeMillis();
      
      private TimestampResponse(ClusterNode responder)
      {
         if (responder == null)
         {
            throw new IllegalArgumentException("Null responder");
         }
         this.responder = responder;
      }

      public ClusterNode getResponder()
      {
         return responder;
      }

      public long getTimestamp()
      {
         return timestamp;
      }
   }
   
   public static class RemoteDiscrepancies implements Serializable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -7394430305832099065L;
      
      private final ClusterNode sender;
      private final Map<Server, TimestampDiscrepancy> discrepancies;
      
      private RemoteDiscrepancies(ClusterNode sender, Map<Server, TimestampDiscrepancy> discrepancies)
      {
         if (sender == null)
         {
            throw new IllegalArgumentException("Null sender");
         }
         if (discrepancies == null)
         {
            throw new IllegalArgumentException("Null discrepancies");
         }
         
         this.sender = sender;
         this.discrepancies = discrepancies;
      }

      public ClusterNode getSender()
      {
         return sender;
      }

      public Map<Server, TimestampDiscrepancy> getDiscrepancies()
      {
         return discrepancies;
      }
      
   }
}
