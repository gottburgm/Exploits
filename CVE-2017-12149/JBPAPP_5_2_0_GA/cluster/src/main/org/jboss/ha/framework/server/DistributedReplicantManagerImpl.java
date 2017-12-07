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
package org.jboss.ha.framework.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.logging.Logger;
//import org.jboss.managed.api.ManagedOperation.Impact;
//import org.jboss.managed.api.annotation.ManagementComponent;
//import org.jboss.managed.api.annotation.ManagementObject;
//import org.jboss.managed.api.annotation.ManagementObjectID;
//import org.jboss.managed.api.annotation.ManagementOperation;
//import org.jboss.managed.api.annotation.ManagementParameter;
//import org.jboss.managed.api.annotation.ManagementProperties;
//import org.jboss.managed.api.annotation.ManagementProperty;
//import org.jboss.managed.api.annotation.ViewUse;


/**
 * This class manages replicated objects.
 * 
 * @author  <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author  Scott.stark@jboss.org
 * @author  <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @author  <a href="mailto:pferraro@redhat.com">Paul Ferraro</a>
 * @version $Revision: 111887 $
 */
public class DistributedReplicantManagerImpl
   implements DistributedReplicantManagerImplMBean,
              HAPartition.HAMembershipExtendedListener,
              HAPartition.HAPartitionStateTransfer,
              AsynchEventHandler.AsynchEventProcessor
{
   // Constants -----------------------------------------------------
   
   static final String OBJECT_NAME_BASE = "jboss:service=DistributedReplicantManager";
   
   static final String SERVICE_NAME = "DistributedReplicantManager";
   
   private static final Class<?>[] add_types = new Class<?>[] { String.class, String.class, Serializable.class };
   private static final Class<?>[] remove_types = new Class<?>[] { String.class, String.class };

   // Attributes ----------------------------------------------------
   private static final AtomicInteger threadID = new AtomicInteger();
   
   private final ConcurrentMap<String, Serializable> localReplicants = new ConcurrentHashMap<String, Serializable>();
   private final ConcurrentMap<String, ConcurrentMap<String, Serializable>> replicants = new ConcurrentHashMap<String, ConcurrentMap<String, Serializable>>();
   private final ConcurrentMap<String, List<ReplicantListener>> keyListeners = new ConcurrentHashMap<String, List<ReplicantListener>>();
   private Map<String, Integer> intraviewIdCache = new ConcurrentHashMap<String, Integer>();
   
   private final HAPartition partition;
   /** The handler used to send replicant change notifications asynchronously */
   private final AsynchEventHandler asynchHandler;
   
   private final Logger log;
   
   private String nodeName = null;
   
   // Works like a simple latch
   private volatile CountDownLatch partitionNameKnown = new CountDownLatch(1);

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public DistributedReplicantManagerImpl(HAPartition partition)
   {
      super();
      
      if (partition == null)
      {
         throw new NullPointerException("partition is null");
      }
      
      this.partition = partition;
      this.log = Logger.getLogger(this.getClass().getName() + "." + partition.getPartitionName());
      
      // JBAS-5068 Create the handler early so we don't risk NPEs
      this.asynchHandler = new AsynchEventHandler(this, "AsynchKeyChangeHandler");
   }

   // Public --------------------------------------------------------
   
   public void createService() throws Exception
   {
      if (this.partition == null)
      {
         throw new IllegalStateException("HAPartition property must be set before creating DistributedReplicantManager service");
      }

      this.log.debug("registerRPCHandler");
      this.partition.registerRPCHandler(SERVICE_NAME, this);
      this.log.debug("subscribeToStateTransferEvents");
      this.partition.subscribeToStateTransferEvents(SERVICE_NAME, this);
      this.log.debug("registerMembershipListener");
      this.partition.registerMembershipListener(this);
   }
   
   public void startService() throws Exception
   {
      this.nodeName = this.partition.getNodeName();
      
      this.asynchHandler.start();

      this.partitionNameKnown.countDown(); // partition name is now known!
      
      //log.info("mergemembers");
      //mergeMembers();
   }
   
   public void stopService() throws Exception
   {
      // Stop the asynch handler thread
      try
      {
         this.asynchHandler.stop();
      }
      catch( Exception e)
      {
         this.log.warn("Failed to stop asynchHandler", e);
      }
      
      // Reset the latch
      this.partitionNameKnown = new CountDownLatch(1);
   }

   // NR 200505 : [JBCLUSTER-38] unbind at destroy
   public void destroyService() throws Exception
   {
      // we cleanly shutdown. This should be optimized.
      for (String key: this.localReplicants.keySet())
      {
         this.removeLocal(key); // channel is disconnected, so don't try to notify cluster
      }
      
      if (this.partition != null)
      {
         this.partition.unregisterRPCHandler(SERVICE_NAME, this);
         this.partition.unsubscribeFromStateTransferEvents(SERVICE_NAME, this);
         this.partition.unregisterMembershipListener(this);
      }
   }

   public void registerWithJmx(MBeanServer server) throws Exception
   {
      server.registerMBean(this, this.getObjectName());
   }
   
   public void unregisterWithJmx(MBeanServer server) throws Exception
   {
      server.unregisterMBean(this.getObjectName());
   }
   
   private ObjectName getObjectName() throws Exception
   {
      return new ObjectName("jboss:service=" + SERVICE_NAME + ",partition=" + this.partition.getPartitionName());
   }
   
//   @ManagementProperty(use={ViewUse.STATISTIC}, description="The partition's name")
//   @ManagementObjectID(type="DistributedReplicantManager")
   public String getPartitionName()
   {
      return this.partition.getPartitionName();
   }

//   public void setHAPartition(HAPartition clusterPartition)
//   {
//      this.partition = clusterPartition;
//   }
   
//   @ManagementOperation(name="listDRMContent",
//         description="List all known keys and the nodes that have registered bindings",
//         impact=Impact.ReadOnly)
   public String listContent() throws Exception
   {
      StringBuilder result = new StringBuilder();
      
      result.append("<pre>");

      // we merge all replicants services: local only or not
      //
      for (String category: this.getAllServices())
      {
         result.append("-----------------------------------------------\n");
         result.append("Service : ").append(category).append("\n\n");
         
         Serializable local = this.localReplicants.get(category);
         
         if (local == null)
         {
            result.append("\t- Service is *not* available locally\n");
         }
         else
         {
            result.append("\t- Service *is* also available locally\n");
         }

         Map<String, Serializable> content = this.replicants.get(category);
         
         if (content != null)
         {
            for (String location: content.keySet())
            {
               result.append("\t- ").append(location).append("\n");
            }
         }
         
         result.append ("\n");
         
      }
      
      result.append ("</pre>");
      
      return result.toString();
   }
   
//   @ManagementOperation(name="listDRMContentAsXml",
//         description="List in XML format all known services and the nodes that have registered bindings",
//         impact=Impact.ReadOnly)
   public String listXmlContent() throws Exception
   {
      StringBuilder result = new StringBuilder();
      
      result.append ("<ReplicantManager>\n");

      // we merge all replicants services: local only or not
      //
      for (String category: this.getAllServices())
      {
         result.append("\t<Service>\n");
         result.append("\t\t<ServiceName>").append(category).append("</ServiceName>\n");

         Serializable local = this.localReplicants.get(category);
         
         if (local != null)
         {
            result.append("\t\t<Location>\n");
            result.append("\t\t\t<Name local=\"True\">").append (this.nodeName).append ("</Name>\n");
            result.append("\t\t</Location>\n");
         }

         Map<String, Serializable> content = this.replicants.get(category);
         
         if (content != null)
         {
            for (String location: content.keySet())
            {
               result.append("\t\t<Location>\n");
               result.append("\t\t\t<Name local=\"False\">").append (location).append ("</Name>\n");
               result.append("\t\t</Location>\n");
            }
         }
         
         result.append("\t</Service>\n");
      }

      result.append("</ReplicantManager>\n");
      
      return result.toString();
   }

   // HAPartition.HAPartitionStateTransfer implementation ----------------------------------------------
   
   public Serializable getCurrentState()
   {
      Map<String, ConcurrentMap<String, Serializable>> result = new HashMap<String, ConcurrentMap<String, Serializable>>();
      
      for (String category: this.getAllServices())
      {
         ConcurrentMap<String, Serializable> map = new ConcurrentHashMap<String, Serializable>();
         
         ConcurrentMap<String, Serializable> content = this.replicants.get(category);
         
         if (content != null)
         {
            map.putAll(content);
         }
         
         Serializable local = this.localReplicants.get(category);
         
         if (local != null)
         {
            map.put(this.nodeName, local);
         }
         
         result.put(category, map);
      }
      
      // we add the intraviewid cache to the global result
      //
      return new Object[] { result, this.intraviewIdCache };
   }

   @SuppressWarnings("unchecked")
   public void setCurrentState(Serializable newState)
   {
      Object[] globalState = (Object[]) newState;
      Map<String, ConcurrentMap<String, Serializable>> map = (Map) globalState[0];
      
      this.replicants.putAll(map);
      
      this.intraviewIdCache = (Map) globalState[1];

      if (this.log.isTraceEnabled())
      {
         this.log.trace(this.nodeName + ": received new state, will republish local replicants");
      }
      
      new MembersPublisher().start();
   }
      
//   @ManagementOperation(name="getAllDRMServices",
//         description="Get a collection of the names of all keys for which we have bindings",
//         impact=Impact.ReadOnly)
   public Collection<String> getAllServices()
   {
      Set<String> services = new HashSet<String>();
      services.addAll(this.localReplicants.keySet());
      services.addAll(this.replicants.keySet());
      return services;
   }
   
   // HAPartition.HAMembershipListener implementation ----------------------------------------------

   @SuppressWarnings("unchecked")
   public void membershipChangedDuringMerge(Vector deadMembers, Vector newMembers, Vector allMembers, Vector originatingGroups)
   {
      // Here we only care about deadMembers.  Purge all replicant lists of deadMembers
      // and then notify all listening nodes.
      //
      this.log.info("Merging partitions...");
      this.log.info("Dead members: " + deadMembers.size());
      this.log.info("Originating groups: " + originatingGroups);
      this.purgeDeadMembers(deadMembers, true);
      if (newMembers.size() > 0)
      {
         new MergeMembers().start();
      }
   }
   
   @SuppressWarnings("unchecked")
   public void membershipChanged(Vector deadMembers, Vector newMembers, Vector allMembers)
   {
      // Here we only care about deadMembers.  Purge all replicant lists of deadMembers
      // and then notify all listening nodes.
      //
      this.log.info("I am (" + this.nodeName + ") received membershipChanged event:");
      this.log.info("Dead members: " + deadMembers.size() + " (" + deadMembers + ")");
      this.log.info("New Members : " + newMembers.size()  + " (" + newMembers + ")");
      this.log.info("All Members : " + allMembers.size()  + " (" + allMembers + ")");
      this.purgeDeadMembers(deadMembers, false);
      
      // we don't need to merge members anymore
   }
   
   // AsynchEventHandler.AsynchEventProcessor implementation -----------------
   
   public void processEvent(Object event)
   {
      KeyChangeEvent kce = (KeyChangeEvent) event;
      this.notifyKeyListeners(kce.key, kce.replicants, kce.merge);
   }
   
   static class KeyChangeEvent
   {
      String key;
      List<Serializable> replicants;
      boolean merge;
   }
   
   // DistributedReplicantManager implementation ----------------------------------------------
   
   public void add(String key, Serializable replicant) throws Exception
   {
      if (this.log.isTraceEnabled())
      {
         this.log.trace("add, key=" + key + ", value=" + replicant);
      }
      
      this.partitionNameKnown.await(); // we don't propagate until our name is known
      
      Object[] args = { key, this.nodeName, replicant };
      
      this.partition.callMethodOnCluster(SERVICE_NAME, "_add", args, add_types, true);

      List<Serializable> replicants = null;
      
      synchronized (this.localReplicants)
      {
         this.localReplicants.put(key, replicant);
         
         replicants = this.getReplicants(key);
      }
      
      this.notifyKeyListeners(key, replicants, false);
   }
   
   public void remove(String key) throws Exception
   {
      this.partitionNameKnown.await(); // we don't propagate until our name is known
      
      // optimisation: we don't make a costly network call
      // if there is nothing to remove
      if (this.localReplicants.containsKey(key))
      {
         Object[] args = { key, this.nodeName };
         
         this.partition.callAsynchMethodOnCluster(SERVICE_NAME, "_remove", args, remove_types, true);
         
         this.removeLocal(key);
      }
   }
   
   private void removeLocal(String key)
   {
      List<Serializable> replicants = null;
      
      synchronized (this.localReplicants)
      {
         if (this.localReplicants.remove(key) != null)
         {
            replicants = this.getReplicants(key);
         }
      }
      
      if (replicants != null)
      {
         this.notifyKeyListeners(key, replicants, false);
      }
   }
   
   public Serializable lookupLocalReplicant(String key)
   {
      return this.localReplicants.get(key);
   }
   
   public List<Serializable> lookupReplicants(String key)
   {
      Serializable local = this.localReplicants.get(key);
      
      Map<String, Serializable> replicant = this.replicants.get(key);

      if (replicant == null)
      {
         return (local != null) ? Collections.singletonList(local) : null;
      }

      // JBAS-2677. Put the replicants in view order.
      ClusterNode[] nodes = this.partition.getClusterNodes();

      List<Serializable> result = new ArrayList<Serializable>(nodes.length);
      
      for (ClusterNode node: nodes)
      {
         String name = node.getName();
         
         if (local != null && this.nodeName.equals(name))
         {
            result.add(local);
         }
         else
         {
            Serializable value = replicant.get(name);
            
            if (value != null)
            {
               result.add(value);
            }
         }
      }
      
      return result;
   }
   
   private List<Serializable> getReplicants(String key)
   {
      List<Serializable> result = this.lookupReplicants(key);
      
      if (result == null)
      {
         result = Collections.emptyList();
      }
      
      return result;
   }

//   @ManagementOperation(name="lookupDRMNodeNames",
//         description="Returns the names of the nodes that have registered objects under the given key",
//                        impact=Impact.ReadOnly,
//                        params={@ManagementParameter(name="key",
//                                                     description="The name of the service")})
   @Deprecated
   public List<String> lookupReplicantsNodeNames(String key)
   {
      List<ClusterNode> nodes = this.lookupReplicantsNodes(key);
      
      if (nodes == null) return null;
      
      List<String> nodeNames = new ArrayList<String>(nodes.size());
      
      for (ClusterNode node : nodes)
      {
         nodeNames.add(node.getName());
      }
      
      return nodeNames;
   }

   public List<ClusterNode> lookupReplicantsNodes(String key)
   {
      boolean local = this.localReplicants.containsKey(key);
      Map<String, Serializable> replicant = this.replicants.get(key);
      
      if (replicant == null)
      {
         return local ? Collections.singletonList(this.partition.getClusterNode()) : null;
      }
      
      Set<String> keys = replicant.keySet();
      ClusterNode[] nodes = this.partition.getClusterNodes();
      List<ClusterNode> rtn = new ArrayList<ClusterNode>(nodes.length);

      for (ClusterNode node : nodes)
      {
         String name = node.getName();
         
         if (local && this.nodeName.equals(name))
         {
            rtn.add(this.partition.getClusterNode());
         }
         else if (keys.contains(name))
         {
            rtn.add(node);
         }
      }
      
      return rtn;
   }
   
   public void registerListener(String key, ReplicantListener subscriber)
   {
      List<ReplicantListener> list = new CopyOnWriteArrayList<ReplicantListener>();
      
      List<ReplicantListener> existing = this.keyListeners.putIfAbsent(key, list);
      
      ((existing != null) ? existing : list).add(subscriber);
   }
   
   public void unregisterListener(String key, DistributedReplicantManager.ReplicantListener subscriber)
   {
      List<ReplicantListener> listeners = this.keyListeners.get(key);
      
      if (listeners != null)
      {
         listeners.remove(subscriber);
         
         this.keyListeners.remove(key, Collections.emptyList());
      }
   }
   
//   @ManagementOperation(name="getDRMServiceViewId",
//         description="Returns a hash of the list of nodes that " +
//   		                            "have registered an object for the given key",
//   		                impact=Impact.ReadOnly,
//                        params={@ManagementParameter(name="key",
//                                                     description="The name of the service")})
   public int getReplicantsViewId(String key)
   {
      Integer result = this.intraviewIdCache.get(key);
      
      return (result != null) ? result.intValue() : 0;
   }
   
//   @ManagementOperation(name="isDRMMasterForService",
//         description="Returns whether the DRM considers this node to be the master for the given service",
//         impact=Impact.ReadOnly,
//         params={@ManagementParameter(name="key", description="The name of the service")})
   public boolean isMasterReplica(String key)
   {
      if (this.log.isTraceEnabled())
      {
         this.log.trace("isMasterReplica, key=" + key);
      }
      // if I am not a replicant, I cannot be the master...
      //
      if (!this.localReplicants.containsKey(key))
      {
         if (this.log.isTraceEnabled())
         {
            this.log.trace("no localReplicants, key=" + key + ", isMasterReplica=false");
         }
         return false;
      }

      Map<String, Serializable> repForKey = this.replicants.get(key);
      if (repForKey == null)
      {
         if (this.log.isTraceEnabled())
         {
            this.log.trace("no replicants, key=" + key + ", isMasterReplica=true");
         }
         return true;
      }

      @SuppressWarnings("unchecked")
      Vector<String> allNodes = this.partition.getCurrentView();
      for (String node: allNodes)
      {
         if (this.log.isTraceEnabled())
         {
            this.log.trace("Testing member: " + node);
         }
         
         if (repForKey.containsKey(node))
         {
            if (this.log.isTraceEnabled())
            {
               this.log.trace("Member found in replicaNodes, isMasterReplica=false");
            }
            return false;
         }
         else if (node.equals(this.nodeName))
         {
            if (this.log.isTraceEnabled())
            {
               this.log.trace("Member == nodeName, isMasterReplica=true");
            }
            return true;
         }
      }
      return false;
   }

   // DistributedReplicantManager cluster callbacks ----------------------------------------------
   
   /**
    * Cluster callback called when a new replicant is added on another node
    * @param key Replicant key
    * @param nodeName Node that add the current replicant
    * @param replicant Serialized representation of the replicant
    */
   public void _add(String key, String nodeName, Serializable replicant)
   {
      if (this.log.isTraceEnabled())
      {
         this.log.trace("_add(" + key + ", " + nodeName);
      }
      
      KeyChangeEvent event = new KeyChangeEvent();
      event.key = key;
      
      synchronized (this.replicants)
      {
         this.addReplicant(key, nodeName, replicant);
         
         event.replicants = this.getReplicants(key);
      }
      
      try
      {
         this.asynchHandler.queueEvent(event);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         
         this.log.error("_add failed", e);
      }
   }
   
   /**
    * Cluster callback called when a replicant is removed by another node
    * @param key Name of the replicant key
    * @param nodeName Node that wants to remove its replicant for the give key
    */
   public void _remove(String key, String nodeName)
   {
      KeyChangeEvent event = new KeyChangeEvent();
      event.key = key;
      
      synchronized (this.replicants)
      {
         if (this.removeReplicant(key, nodeName))
         {
            event.replicants = this.getReplicants(key);
         }
      }
      
      if (event.replicants != null)
      {
         try
         {
            this.asynchHandler.queueEvent(event);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
            
            this.log.error("_remove failed", e);
         }
      }
   }
   
   protected boolean removeReplicant(String key, String nodeName)
   {
      Map<String, Serializable> replicant = this.replicants.get(key);
      
      if (replicant != null)
      {
         if (replicant.remove(nodeName) != null)
         {
            // If replicant map is empty, prune it
            this.replicants.remove(key, Collections.emptyMap());
            
            return true;
         }
      }
      
      return false;
   }
   
   /**
    * Cluster callback called when a node wants to know our complete list of local replicants
    * @throws Exception Thrown if a cluster communication exception occurs
    * @return A java array of size 2 containing the name of our node in this cluster and the serialized representation of our state
    */
   public Object[] lookupLocalReplicants() throws Exception
   {
      this.partitionNameKnown.await(); // we don't answer until our name is known
      
      Object[] rtn = { this.nodeName, this.localReplicants };
      
      if (this.log.isTraceEnabled())
      {
         this.log.trace("lookupLocalReplicants called ("+ rtn[0] + "). Return: " + this.localReplicants.size());
      }
      
      return rtn;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected int calculateReplicantsHash(List<ClusterNode> members)
   {
      int result = 0;
      
      for (ClusterNode member: members)
      {
         if (member != null)
         {
            result += member.getName().hashCode(); // no explicit overflow with int addition
         }
      }
      
      return result;
   }
   
   protected int updateReplicantsHashId(String key)
   {
      // we first get a list of all nodes names that replicate this key
      //
      List<ClusterNode> nodes = this.lookupReplicantsNodes(key);
      int result = 0;
      
      if ((nodes == null) || nodes.isEmpty())
      {
         // no nore replicants for this key: we uncache our view id
         //
         this.intraviewIdCache.remove(key);
      }
      else
      {
         result = this.calculateReplicantsHash(nodes);
         this.intraviewIdCache.put(key, new Integer(result));
      }
      
      return result;
      
   }
   
   ///////////////
   // DistributedReplicantManager API
   ///////////////
   
   /**
    * Add a replicant to the replicants map.
    * @param key replicant key name
    * @param nodeName name of the node that adds this replicant
    * @param replicant Serialized representation of the replica
    * @return true, if this replicant was newly added to the map, false otherwise
    */
   protected boolean addReplicant(String key, String nodeName, Serializable replicant)
   {
      ConcurrentMap<String, Serializable> map = new ConcurrentHashMap<String, Serializable>();
      
      ConcurrentMap<String, Serializable> existingMap = this.replicants.putIfAbsent(key, map);
      
      return (((existingMap != null) ? existingMap : map).put(nodeName, replicant) == null);
   }
   
   /**
    * Notifies, through a callback, the listeners for a given replicant that the set of replicants has changed
    * @param key The replicant key name
    * @param newReplicants The new list of replicants
    * @param merge is the notification the result of a cluster merge?
    * 
    */
   protected void notifyKeyListeners(String key, List<Serializable> newReplicants, boolean merge)
   {
      if (this.log.isTraceEnabled())
      {
         this.log.trace("notifyKeyListeners");
      }

      // we first update the intra-view id for this particular key
      //
      int newId = this.updateReplicantsHashId(key);
      
      List<ReplicantListener> listeners = this.keyListeners.get(key);

      if (listeners == null)
      {
         if (this.log.isTraceEnabled())
         {
            this.log.trace("listeners is null");
         }
         return;
      }
      
      if (this.log.isTraceEnabled())
      {
         this.log.trace("notifying " + listeners.size() + " listeners for key change: " + key);
      }
      
      for (ReplicantListener listener: listeners)
      {
         if (listener != null)
         {
            listener.replicantsChanged(key, newReplicants, newId, merge);
         }
      }
   }

   protected void republishLocalReplicants()
   {
      try
      {
         if (this.log.isTraceEnabled())
         {
            this.log.trace("Start Re-Publish local replicants in DRM");
         }

         for (Map.Entry<String, Serializable> entry: this.localReplicants.entrySet())
         {
            Serializable replicant = entry.getValue();
            
            if (replicant != null)
            {
               String key = entry.getKey();
               
               if (this.log.isTraceEnabled())
               {
                  this.log.trace("publishing, key=" + key + ", value=" + replicant);
               }

               Object[] args = { key, this.nodeName, replicant };

               this.partition.callAsynchMethodOnCluster(SERVICE_NAME, "_add", args, add_types, true);
               
               this.notifyKeyListeners(key, this.getReplicants(key), false);
            }
         }
         
         if (this.log.isTraceEnabled())
         {
            this.log.trace("End Re-Publish local replicants");
         }
      }
      catch (Exception e)
      {
         this.log.error("Re-Publish failed", e);
      }
   }

   ////////////////////
   // Group membership API
   ////////////////////

   protected void mergeMembers()
   {
      try
      {
         this.log.debug("Start merging members in DRM service...");
         
         ArrayList<?> rsp = this.partition.callMethodOnCluster(SERVICE_NAME,
                                        "lookupLocalReplicants",
                                        new Object[]{}, new Class[]{}, true);
         if (rsp.isEmpty())
         {
            this.log.debug("No responses from other nodes during the DRM merge process.");
         }
         else
         {
            this.log.debug("The DRM merge process has received " + rsp.size() + " answers");
         }
         
         // Record keys to be notified, and replicant list per key
         Map<String, List<Serializable>> notifications = new HashMap<String, List<Serializable>>();
         
         // Perform add/remove and replicant lookup atomically
         synchronized (this.replicants)
         {
            for (Object o: rsp)
            {
               if (o == null)
               {
                  this.log.warn("As part of the answers received during the DRM merge process, a NULL message was received!");
                  continue;
               }
               else if (o instanceof Throwable)
               {
                  this.log.warn("As part of the answers received during the DRM merge process, a Throwable was received!", (Throwable) o);
                  continue;
               }
               
               Object[] objs = (Object[]) o;
               String node = (String) objs[0];
               @SuppressWarnings("unchecked")
               Map<String, Serializable> replicants = (Map<String, Serializable>) objs[1];
               
               //FIXME: We don't remove keys in the merge process but only add new keys!
               for (Map.Entry<String, Serializable> entry: replicants.entrySet())
               {
                  String key = entry.getKey();
                  
                  if (this.addReplicant(key, node, entry.getValue()))
                  {
                     notifications.put(key, null);
                  }
               }
               
               // The merge process needs to remove some (now) unexisting keys
               for (Map.Entry<String, ConcurrentMap<String, Serializable>> entry: this.replicants.entrySet())
               {
                  String key = entry.getKey();
                  
                  if (entry.getValue().containsKey(node))
                  {
                     if (!replicants.containsKey(key))
                     {
                        if (this.removeReplicant(key, node))
                        {
                           notifications.put(key, null);
                        }
                     }
                  }
               }
            }
            
            // Lookup replicants for each changed key
            for (Map.Entry<String, List<Serializable>> entry: notifications.entrySet())
            {
               entry.setValue(this.getReplicants(entry.getKey()));
            }
         }
         
         // Notify recorded key changes
         for (Map.Entry<String, List<Serializable>> entry: notifications.entrySet())
         {
            this.notifyKeyListeners(entry.getKey(), entry.getValue(), true);
         }

         this.log.debug("..Finished merging members in DRM service");

      }
      catch (Exception ex)
      {
         this.log.error("merge failed", ex);
      }
   }

   /**
    * Get rid of dead members from replicant list.
    * 
    * @param deadMembers the members that are no longer in the view
    * @param merge       whether the membership change occurred during
    *                    a cluster merge
    */
   protected void purgeDeadMembers(Vector<ClusterNode> deadMembers, boolean merge)
   {
      if (deadMembers.isEmpty()) return;

      this.log.debug("purgeDeadMembers, " + deadMembers);

      List<String> deadNodes = new ArrayList<String>(deadMembers.size());
      
      for (ClusterNode member: deadMembers)
      {
         deadNodes.add(member.getName());
      }
      
      for (Map.Entry<String, ConcurrentMap<String, Serializable>> entry: this.replicants.entrySet())
      {
         String key = entry.getKey();
         ConcurrentMap<String, Serializable> replicant = entry.getValue();
         
         List<Serializable> replicants = null;
         
         synchronized (this.replicants)
         {
            if (replicant.keySet().removeAll(deadNodes))
            {
               replicants = this.getReplicants(key);
            }
         }
         
         if (replicants != null)
         {
            this.notifyKeyListeners(key, replicants, merge);
         }
      }
   }

   /**
    */
   protected void cleanupKeyListeners()
   {
      // NOT IMPLEMENTED YET
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

   protected class MergeMembers extends Thread
   {
      public MergeMembers()
      {
         super("DRM Async Merger#" + threadID.getAndIncrement());
      }

      /**
       * Called when the service needs to merge with another partition. This
       * process is performed asynchronously
       */
      public void run()
      {
         DistributedReplicantManagerImpl.this.log.debug("Sleeping for 50ms before mergeMembers");
         try
         {
            // if this thread invokes a cluster method call before
            // membershipChanged event completes, it could timeout/hang
            // we need to discuss this with Bela.
            Thread.sleep(50);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         DistributedReplicantManagerImpl.this.mergeMembers();
      }
   }

   protected class MembersPublisher extends Thread
   {
      public MembersPublisher()
      {
         super("DRM Async Publisher#" + threadID.getAndIncrement());
      }

      /**
       * Called when service needs to re-publish its local replicants to other
       * cluster members after this node has joined the cluster.
       */
      public void run()
      {
         DistributedReplicantManagerImpl.this.log.debug("DRM: Sleeping before re-publishing for 50ms just in case");
         try
         {
            // if this thread invokes a cluster method call before
            // membershipChanged event completes, it could timeout/hang
            // we need to discuss this with Bela.
            Thread.sleep(50);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
         DistributedReplicantManagerImpl.this.republishLocalReplicants();
      }
   }
}
