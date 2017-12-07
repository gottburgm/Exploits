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

package org.jboss.profileservice.cluster.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.ha.framework.server.lock.LocalAndClusterLockManager;
import org.jboss.ha.framework.server.lock.TimeoutException;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContextAware;
import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.RepositoryClusteringHandler;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManager;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ByteChunk;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.ImmutableSynchronizationPolicy;
import org.jboss.system.server.profileservice.repository.clustered.sync.InconsistentRepositoryStructureException;
import org.jboss.system.server.profileservice.repository.clustered.sync.LocalContentModificationGenerator;
import org.jboss.system.server.profileservice.repository.clustered.sync.RemoteContentModificationGenerator;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationId;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationInitiationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationPolicy;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationReadAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationRemoteAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationWriteAction;
import org.jboss.virtual.VirtualFile;

/**
 * {@link RepositoryClusteringHandler} implementation that uses {@link HAPartition}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DefaultRepositoryClusteringHandler 
   implements RepositoryClusteringHandler, KernelControllerContextAware
{
   private static final Logger log = Logger.getLogger(DefaultRepositoryClusteringHandler.class);
   
   private static final Class<?>[] JOIN_SYNCHRONIZE_TYPES = 
      new Class[]{RepositoryContentMetadata.class, RepositoryContentMetadata.class};
   
   private static final Class<?>[] MERGE_SYNCHRONIZE_TYPES = 
      new Class[]{RepositoryContentMetadata.class};
   
   private static final Class<?>[] INITIATE_SYNCHRONIZATION_TYPES = 
      new Class[]{SynchronizationId.class, List.class};
   
   private static final Class<?>[] TX_TYPES = new Class[]{SynchronizationId.class};
   
   private static final Class<?>[] EXECUTE_MOD_TYPES = 
      new Class[]{SynchronizationId.class, RepositoryItemMetadata.class, boolean.class};
   
   private static final Class<?>[] PULL_BYTES_TYPES = 
      new Class[]{SynchronizationId.class, RepositoryItemMetadata.class};
   
   private static final Class<?>[] PUSH_BYTES_TYPES = 
      new Class[]{SynchronizationId.class, RepositoryItemMetadata.class, ByteChunk.class};
   
   public static final long DEFAULT_TIMEOUT = 60000;
   
   private String serviceHAName;
   private HAPartition partition;
   private String profileDomain;
   private String profileServer;
   private String profileName;
   private boolean immutable;
   private LocalAndClusterLockManager lockSupport;
   private SynchronizationPolicy synchronizationPolicy;
   private LocalContentManager<?> contentManager;
   private RpcTarget rpcTarget = new RpcTarget();
   private boolean inSync = false;
   private long lockTimeout = DEFAULT_TIMEOUT;
   private long methodCallTimeout = DEFAULT_TIMEOUT;
   private volatile ActiveSynchronization activeSynchronization;
   private final DRMListener drmListener = new DRMListener();
   private List<ClusterNode> serviceView;
   private boolean initialized;
   

   public HAPartition getPartition()
   {
      return partition;
   }

   public void setPartition(HAPartition partition)
   {
      checkUnitialized();
      this.partition = partition;
   }

   public String getProfileDomain()
   {
      return profileDomain;
   }

   public void setProfileDomain(String profileDomain)
   {
      checkUnitialized();
      this.profileDomain = profileDomain;
   }

   public String getProfileServer()
   {
      return profileServer;
   }

   public void setProfileServer(String profileServer)
   {
      checkUnitialized();
      this.profileServer = profileServer;
   }

   public String getProfileName()
   {
      return profileName;
   }

   public void setProfileName(String profileName)
   {
      checkUnitialized();
      this.profileName = profileName;
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
      checkUnitialized();
      this.serviceHAName = serviceHAName;
   }

   public SynchronizationPolicy getSynchronizationPolicy()
   {
      return synchronizationPolicy;
   }

   public void setSynchronizationPolicy(SynchronizationPolicy synchronizationPolicy)
   {
      checkUnitialized();
      if (!immutable)
      {
         this.synchronizationPolicy = synchronizationPolicy;
      }
   }   
   
   public boolean isImmutable()
   {
      return immutable;
   }

   public void setImmutable(boolean immutable)
   {
      checkUnitialized();
      if (immutable && !this.immutable)
      {
         setSynchronizationPolicy(new ImmutableSynchronizationPolicy());
      }
      this.immutable = immutable;
   }

   public long getLockTimeout()
   {
      return lockTimeout;
   }

   public void setLockTimeout(long lockTimeout)
   {
      this.lockTimeout = lockTimeout;
   }

   public long getMethodCallTimeout()
   {
      return methodCallTimeout;
   }

   public void setMethodCallTimeout(long methodCallTimeout)
   {
      this.methodCallTimeout = methodCallTimeout;
   }
   
   
   // --------------------------------------------  RepositoryClusteringHandler

   public void initialize(LocalContentManager<?> persister) throws Exception
   {
      if (persister == null)
      {
         throw new IllegalArgumentException("Null persister");
      }
      
      if (this.partition == null)
      {
         throw new IllegalStateException("Null partition; must inject an " +
         		"HAPartition before invoking initialize");
      }
      if (this.serviceHAName == null)
      {
         throw new IllegalStateException("Null serviceHAName; must inject a " +
         		"serviceHAName before invoking initialize");
      }
      if (this.synchronizationPolicy == null)
      {
         throw new IllegalStateException("Null synchronizationPolicy; must " +
         		"inject a RepositorySynchronizationPolicy before invoking initialize");
      }
      
      this.contentManager = persister;
      
      String lockServiceName = this.serviceHAName + "-ClusterLock";
      this.lockSupport = new LocalAndClusterLockManager(lockServiceName, partition);
      
      this.lockSupport.start();
      
      this.partition.registerRPCHandler(getServiceHAName(), rpcTarget);
      
      DistributedReplicantManager drm = this.partition.getDistributedReplicantManager();
      drm.add(getServiceHAName(), this.partition.getClusterNode());
      this.serviceView = drm.lookupReplicantsNodes(getServiceHAName());
      drm.registerListener(getServiceHAName(), drmListener);
      
      this.initialized = true;
   }
   
   public void shutdown() throws Exception
   {
      DistributedReplicantManager drm = this.partition.getDistributedReplicantManager();
      drm.unregisterListener(getServiceHAName(), drmListener);
      drm.remove(getServiceHAName());
      this.partition.unregisterRPCHandler(getServiceHAName(), rpcTarget);
      this.lockSupport.stop();
      
      this.contentManager = null;
      
      this.initialized = false;
   }

   public String getPartitionName()
   {
      return this.partition == null ? null : this.partition.getPartitionName();
   }

   public String getLocalNodeName()
   {
      ClusterNode localNode = this.partition == null ? null : this.partition.getClusterNode();
      return localNode == null ? null : localNode.getName();
   }

   public ProfileKey getProfileKey()
   {
      return new ProfileKey(profileDomain, profileServer, profileName);
   }

   public RepositoryContentMetadata synchronizeContent(boolean pullFromCluster) 
         throws InconsistentRepositoryStructureException, IOException
   {
      List<ContentModification> modifications = null;
      RepositoryContentMetadata localCurrentContent = this.contentManager.getCurrentContentMetadata();
            
      if (pullFromCluster)
      {
         // Normal join case
         modifications = getModificationsFromCluster(localCurrentContent);
      }
      else if (!inSync)
      {
         // Merge case
         modifications = getModificationsFromCluster(null);         
      }
      else
      {
         // Periodic scan case
         modifications = getLocalModifications(localCurrentContent);
      }
      
      if (modifications != null)
      {
         installModifications(modifications, localCurrentContent);
      }
      else
      {
         // No one else out there. Install the localCurrentContent as official.
         this.contentManager.installCurrentContentMetadata();
      }
      
      RepositoryContentMetadata result = this.contentManager.getOfficialContentMetadata();
      inSync = true;
      return result;      
   }

   public RepositoryItemMetadata addDeploymentContent(String vfsPath, InputStream contentIS) throws IOException
   {
      RepositoryItemMetadata item = this.contentManager.getItemForAddition(vfsPath);
      RepositoryContentMetadata updated = this.contentManager.getContentMetadataForAdd(item, contentIS);
      RepositoryContentMetadata official = this.contentManager.getOfficialContentMetadata();
      LocalContentModificationGenerator generator = new LocalContentModificationGenerator();
      List<ContentModification> modifications;
      try
      {
         modifications = generator.getModificationList(official, updated);
      }
      catch (InconsistentRepositoryStructureException e)
      {
         throw new IllegalStateException("Incompatible structure change", e);
      }
      
      installModifications(modifications, updated);
      
      official = this.contentManager.getOfficialContentMetadata();
      RepositoryRootMetadata rrmd = official.getRepositoryRootMetadata(item.getRootName());
      return rrmd.getItemMetadata(item.getRelativePathElements());
   }

   public void removeDeploymentContent(VirtualFile vf) throws Exception
   {
      RepositoryContentMetadata updated = this.contentManager.getContentMetadataForRemove(vf);
      RepositoryContentMetadata official = this.contentManager.getOfficialContentMetadata();
      LocalContentModificationGenerator generator = new LocalContentModificationGenerator();
      List<ContentModification> modifications;
      try
      {
         modifications = generator.getModificationList(official, updated);
      }
      catch (InconsistentRepositoryStructureException e)
      {
         throw new IllegalStateException("Incompatible structure change", e);
      }
      
      installModifications(modifications, updated);
   }

   public boolean lockGlobally()
   {
      try
      {
         lockSupport.lockGlobally(getServiceHAName(), lockTimeout);
         return true;
      }
      catch (TimeoutException e)
      {
         log.info("Unable to acquire global lock: " + e.getLocalizedMessage());
      }
      catch (InterruptedException e)
      {
         log.info("Interrupted while obtaining global lock: " + e.getLocalizedMessage());
         Thread.currentThread().interrupt();         
      }
      return false;
   }

   public boolean lockLocally()
   {
      try
      {
         lockSupport.lockLocally(getServiceHAName(), lockTimeout);
         return true;
      }
      catch (TimeoutException e)
      {
         log.info("Unable to acquire local lock: " + e.getLocalizedMessage());
      }
      catch (InterruptedException e)
      {
         log.info("Interrupted while obtaining global lock: " + e.getLocalizedMessage());
         Thread.currentThread().interrupt(); 
      }
      return false;
   }

   public void unlockGlobally()
   {
      lockSupport.unlockGlobally(getServiceHAName());      
   }

   public void unlockLocally()
   {
      lockSupport.unlockLocally(getServiceHAName());
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
   
   // --------------------------------------------------------------  Protected
   
   /**
    * For benefit of unit tests, exposes the object we register with the HAPartition.
    * Not intended for use outside of unit tests.
    * 
    * TODO see if we can eliminate this by having the unit tests capture
    * the object registered with the HAPartition.
    */
   protected RpcTarget getRpcTarget()
   {
      return rpcTarget;
   }
   
   /**
    * For benefit of unit tests, exposes the object we register with the DRM
    * as a {@link ReplicantListener}. Not intended for use outside of unit tests.
    * 
    * TODO see if we can eliminate this by having the unit tests capture
    * the object registered with the DRM.
    */
   protected DRMListener getDRMListener()
   {
      return drmListener;
   }
   
   // ----------------------------------------------------------------  Private

   private List<ContentModification> getModificationsFromCluster(RepositoryContentMetadata localCurrentContent) 
         throws IOException, InconsistentRepositoryStructureException
   {
      List<ContentModification> modifications = null;
      
      RepositoryContentMetadata localBaseMetadata = this.contentManager.getOfficialContentMetadata();
      if (localBaseMetadata == null)
      {
         localBaseMetadata = this.contentManager.createEmptyContentMetadata();
      }
      
      List<ClusterNode> nodes = this.partition.getDistributedReplicantManager().lookupReplicantsNodes(getServiceHAName());
      for (ClusterNode node : nodes)
      {
         if (node.equals(this.partition.getClusterNode()))
         {
            continue;
         }
         try
         {
            Object rsp = null;
            String methodName = null;
            if (localCurrentContent != null)
            {
               methodName = "joinSynchronizeContent";
               Object[] args = { localBaseMetadata, localCurrentContent };
               rsp = this.partition.callMethodOnNode(getServiceHAName(), methodName, args, JOIN_SYNCHRONIZE_TYPES, methodCallTimeout, node);
            }
            else
            {
               methodName = "mergeSynchronizeContent";
               Object[] args = { localBaseMetadata };
               rsp = this.partition.callMethodOnNode(getServiceHAName(), methodName, args, MERGE_SYNCHRONIZE_TYPES, methodCallTimeout, node);
               
            }
            if (rsp instanceof NotSynchronizedException)
            {
               throw (NotSynchronizedException) rsp;
            }
            else if (rsp instanceof List)
            {
               @SuppressWarnings("unchecked")
               List<ContentModification> mods = (List<ContentModification>) rsp;
               modifications = mods;
               break;
            }
            else
            {
               log.warn("Unknown response to " + methodName + ": " + rsp);
            }
            
         }
         catch (NotSynchronizedException ignored)
         {
            log.debug("Cannot synchronize with " + node + " as it itself is not yet synchronized");
         }
         catch (InconsistentRepositoryStructureException e)
         {
            throw e;
         }
         catch (Throwable t)
         {
            rethrowAsUnchecked(t);
         }
      }
      return modifications;
   }

   private List<ContentModification> getLocalModifications(RepositoryContentMetadata localCurrentContent)
   {
      RepositoryContentMetadata official = this.contentManager.getOfficialContentMetadata();
      LocalContentModificationGenerator generator = new LocalContentModificationGenerator();
      try
      {
         return generator.getModificationList(official, localCurrentContent);
      }
      catch (InconsistentRepositoryStructureException e)
      {
         throw new IllegalStateException("Incompatible structure change", e);
      }
   }

   private void installModifications(List<ContentModification> modifications, RepositoryContentMetadata toInstall)
   {
      SynchronizationId<ClusterNode> id = 
         new SynchronizationId<ClusterNode>(this.partition.getClusterNode());
      
      boolean committed = false;
      try
      {
         // First, inform the cluster of the synchronization, let them
         // establish their local modifications
         
         Object[] args = {id, modifications};
         List<?> rsps = this.partition.callMethodOnCluster(getServiceHAName(), 
               "initiateSynchronization", args, INITIATE_SYNCHRONIZATION_TYPES, true);
         for (Object rsp : rsps)
         {
            if (rsp instanceof NotSynchronizedException)
            {
               continue;
            }
            else if (rsp instanceof Exception)
            {
               throw (Exception) rsp;
            }
         }
         
         // Do it ourself
         handleInitiateSynchronization(id, modifications, toInstall);
         
         // Next, cycle through the actions
         executeSynchronizationActions();
                  
         // Tell the cluster to "prepare" all actions; e.g. move temp files
         // into the real area, do deletes etc
         
         rsps = this.partition.callMethodOnCluster(getServiceHAName(), "prepare", new Object[]{id}, TX_TYPES, true);         
         boolean prepareOK = true;
         for (Object rsp : rsps)
         {
            if (rsp instanceof NotSynchronizedException)
            {
               continue;
            }
            else if (rsp instanceof Exception)
            {
               throw (Exception) rsp;
            }
            else if (Boolean.TRUE.equals(rsp) == false)
            {
               prepareOK = false;
               break;
            }
         }         
         
         if (prepareOK)
         {
            // Do it ourself
            if (handlePrepare(id))
            {
               // Tell cluster to finalize
               this.partition.callMethodOnCluster(getServiceHAName(), "commit", new Object[]{id}, TX_TYPES, true);
               handleCommit(id);
               committed = true;
            }
         }
      }
      catch (Exception e)
      {
         rethrowAsUnchecked(e);
      }
      finally
      {
         if (!committed)
         {
            // Roll back
            try
            {
               this.partition.callMethodOnCluster(getServiceHAName(), "rollback", new Object[]{id}, TX_TYPES, true);
            }
            catch (Exception e)
            {
               log.error("Failed to roll back synchronization " + id + " on remote nodes", e);
            }
            finally
            {
               handleRollback(id);
            }
         }
      }
   }
   
   private void executeSynchronizationActions() throws Exception
   {      
      ActiveSynchronization active = activeSynchronization;
      if (active == null)
      {
         throw new IllegalStateException("No active synchronization");
      }

      SynchronizationId<?> id = active.getId();
      synchronized (active)
      {
         for (SynchronizationAction<?> action : active.getActions())
         {
            if (!active.isAlive())
            {
               throw new RuntimeException("Synchronization " + id + " terminated");
            }
            
            if (action instanceof SynchronizationReadAction)
            {
               executePush(id, (SynchronizationReadAction<?>) action);
            }
            else if (action instanceof SynchronizationWriteAction)
            {
               executePull(id, (SynchronizationWriteAction<?>) action);
            }
            else if (action instanceof SynchronizationRemoteAction)
            {
               executeRemoteAction(id, (SynchronizationRemoteAction<?>) action);
            }
            else
            {
               action.complete();
            }
         }
      }
   }

   private void executePull(SynchronizationId<?> id, SynchronizationWriteAction<?> action) throws Exception
   {
      List<ClusterNode> peers = this.partition.getDistributedReplicantManager().lookupReplicantsNodes(getServiceHAName());
      peers.remove(this.partition.getClusterNode());
      
      boolean ok = false;
      Exception lastCaught = null;
      for (ClusterNode peer : peers)
      {
         try
         {
            if (executePullFromPeer(id, action, peer))
            {
               ok = true;
               break;
            }
         }
         catch (Exception e)
         {
            lastCaught = e;
         }
         // Reset the action for the next go round
         action.cancel();
      }
      
      if (!ok)
      {
         if (lastCaught != null)
         {
            throw lastCaught;
         }
         else
         {
            throw new RuntimeException("No node able to provide item " + 
                  action.getRepositoryContentModification().getItem());
         }
      }
      
      action.complete();
   }
   
   private boolean executePullFromPeer(SynchronizationId<?> id, SynchronizationWriteAction<?> action, ClusterNode node) throws Exception
   {
      int lastRead = 0;
      while (lastRead > -1)
      {
         Object[] args = new Object[]{ id, action.getRepositoryContentModification().getItem()};
         Object rsp = null;
         try
         {
            rsp = this.partition.callMethodOnNode(getServiceHAName(), "pullBytes", args, PULL_BYTES_TYPES, methodCallTimeout, node);
         }
         catch (Throwable t)
         {
            rethrowAsException(t);
         }
         
         if (rsp instanceof ByteChunk)
         {
            ByteChunk chunk = (ByteChunk) rsp;
            lastRead = chunk.getByteCount();
            if (lastRead > -1)
            {
               action.writeBytes(chunk);
            }
         }   
         else if (rsp instanceof NotSynchronizedException)
         {
            return false;
         }
         else if (rsp instanceof Throwable)
         {
            rethrowAsException((Throwable) rsp);
         } 
         else if (rsp == null)
         {
            return false;
         }
         else
         {
            throw new IllegalStateException("Unknown response " + rsp);
         }
      }
      
      return true;
   }

   private void executePush(SynchronizationId<?> id, SynchronizationReadAction<?> action) throws Exception
   {
      int lastRead = 0;
      while (lastRead > -1)
      {
         try
         {
            ByteChunk chunk = action.getNextBytes();
         
            Object[] args = new Object[]{ id, action.getRepositoryContentModification().getItem(), chunk};
            List<?> rsps = this.partition.callMethodOnCluster(getServiceHAName(), "pushBytes", args, PUSH_BYTES_TYPES, true);
            for (Object rsp : rsps)
            {
               if (rsp instanceof NotSynchronizedException)
               {
                  continue;
               }
               else if (rsp instanceof Throwable)
               {
                  rethrowAsException((Throwable) rsp);
               }
            }
            lastRead = chunk.getByteCount();
         }
         catch (Exception e)
         {
            action.cancel();
            throw e;
         }
      }
      action.complete();
   }

   private void executeRemoteAction(SynchronizationId<?> id, 
         SynchronizationRemoteAction<?> action) throws Exception
   {
      Object[] args = new Object[]{ id, action.getRepositoryContentModification().getItem(), action.isInitiation()};
      List<?> rsps = null;
      try
      {
         rsps = this.partition.callMethodOnCluster(getServiceHAName(), "executeModification", args, EXECUTE_MOD_TYPES, true);
      }
      catch (Exception e)
      {
         action.cancel();
         throw e;
      }
      
      action.complete(); // We sent the command, so it is complete even if we fail below
      
      for (Object rsp : rsps)
      {
         if (rsp instanceof NotSynchronizedException)
         {
            continue;
         }
         else if (rsp instanceof Throwable)
         {
            rethrowAsException((Throwable) rsp);
         }
      }
   }
   
   private void checkUnitialized()
   {
      if (initialized)
      {
         throw new IllegalStateException("Cannot reconfigure an initialized " + 
               getClass().getSimpleName());
      }
   }

   private void handleInitiateSynchronization(SynchronizationId<ClusterNode> id, List<ContentModification> modifications, RepositoryContentMetadata toInstall)
   {
      boolean localLed = id.getOriginator().equals(partition.getClusterNode());
      List<? extends SynchronizationAction<?>> actions = 
         contentManager.initiateSynchronization(id, modifications, toInstall, localLed);
      
      activeSynchronization = new ActiveSynchronization(id, actions);
   }

   private boolean handlePrepare(SynchronizationId<ClusterNode> id) throws NotSynchronizedException
   {
      ActiveSynchronization active = activeSynchronization;
      if (active != null)
      {
         synchronized (active)
         {
            active.validate(id);
            
            return active.prepare();
         }
      }
      else
      {
         throw new NotSynchronizedException();
      }
   }

   private void handleCommit(SynchronizationId<ClusterNode> id)
   {
      ActiveSynchronization active = activeSynchronization;
      if (active != null)
      {
         synchronized (active)
         {
            active.validate(id);
            
            try
            {
               active.commit();
            }
            finally
            {
               activeSynchronization = null;
            }
         }
      }
   }

   private void handleRollback(SynchronizationId<ClusterNode> id)
   {
      ActiveSynchronization active = activeSynchronization;
      if (active != null)
      {
         synchronized (active)
         {
            active.validate(id);
            
            try
            {
               active.rollback();
            }
            finally
            {
               activeSynchronization = null;
            }
         }
      }
   }

   private static void rethrowAsException(Throwable t) throws Exception
   {
      if (t == null)
         return;
      if (t instanceof Exception)
      {
         throw (Exception) t;
      }
      else if (t instanceof Error)
      {
         throw (Error) t;
      }
      else
      {
         throw new RuntimeException(t);
      }
   }

   private static void rethrowAsUnchecked(Throwable t)
   {
      if (t == null)
         return;
      if (t instanceof RuntimeException)
      {
         throw (RuntimeException) t;
      }
      else if (t instanceof Error)
      {
         throw (Error) t;
      }
      else
      {
         throw new RuntimeException(t);
      }
   }
   
   // ----------------------------------------------------------------  Classes
   
   public class RpcTarget
   {      
      public List<ContentModification> joinSynchronizeContent(
                              RepositoryContentMetadata remoteBaseContent,
                              RepositoryContentMetadata remoteCurrentContent)
                              throws NotSynchronizedException, InconsistentRepositoryStructureException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         RemoteContentModificationGenerator generator = 
            new RemoteContentModificationGenerator(synchronizationPolicy, remoteBaseContent);
         return generator.getModificationList(contentManager.getOfficialContentMetadata(), remoteCurrentContent);
      }      
      
      public List<ContentModification> mergeSynchronizeContent(
            RepositoryContentMetadata remoteCurrentContent)
            throws NotSynchronizedException, InconsistentRepositoryStructureException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         
         RemoteContentModificationGenerator generator = 
         new RemoteContentModificationGenerator(synchronizationPolicy);
         return generator.getModificationList(contentManager.getOfficialContentMetadata(), remoteCurrentContent);
      }
      
      public void initiateSynchronization(SynchronizationId<ClusterNode> id,
         List<ContentModification> modifications)
            throws NotSynchronizedException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();         
         }
         
         // Create the metadata we'll install when we are done
         RepositoryContentMetadata toInstall = new RepositoryContentMetadata(contentManager.getOfficialContentMetadata());
         
         handleInitiateSynchronization(id, modifications, toInstall);
      }      
      
      public void executeModification(SynchronizationId<ClusterNode> id, 
                                      RepositoryItemMetadata item,
                                      boolean initiation)
            throws NotSynchronizedException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         
         ActiveSynchronization active = activeSynchronization;
         if (active != null)
         {
            synchronized (active)
            {
               active.validate(id);
               
               SynchronizationAction<?> action = 
                  (initiation ? active.getInitiationAction(item) : active.getMiscAction(item));
               if (action != null)
               {
                  action.complete();
               }
               else
               {
                  throw new IllegalStateException("No action for " + item);
               }
            }
         }
         else
         {
            throw new NotSynchronizedException();
         }
      }     
      
      public void pushBytes(SynchronizationId<ClusterNode> id, 
                            RepositoryItemMetadata item,
                            ByteChunk chunk)
            throws NotSynchronizedException, IOException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         
         ActiveSynchronization active = activeSynchronization;
         if (active != null)
         {
            synchronized (active)
            {
               active.validate(id);
               
               SynchronizationWriteAction<?> action = active.getWriteAction(item);
               if (action != null)
               {
                  if (chunk.getByteCount() < 0)
                  {
                     action.complete();
                  }
                  else
                  {
                     action.writeBytes(chunk);
                  }
               }
               else
               {
                  throw new IllegalStateException("No action for " + item);
               }
            }
         }
         else
         {
            throw new NotSynchronizedException();
         }
      }     
      
      public ByteChunk pullBytes(SynchronizationId<ClusterNode> id, 
                            RepositoryItemMetadata item)
            throws NotSynchronizedException, IOException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         
         ActiveSynchronization active = activeSynchronization;
         if (active != null)
         {
            synchronized (active)
            {
               active.validate(id);
               
               SynchronizationReadAction<?> action = active.getReadAction(item);
               if (action != null)
               {
                  return action.getNextBytes();
               }
               else
               {
                  throw new IllegalStateException("No action for " + item);
               }
            }
         }
         else
         {
            throw new NotSynchronizedException();
         }
      }
      
      public boolean prepare(SynchronizationId<ClusterNode> id)
            throws NotSynchronizedException
      {
         if (!inSync)
         {
            throw new NotSynchronizedException();
         }
         
         return handlePrepare(id);
      }      
      
      public void commit(SynchronizationId<ClusterNode> id)
      {
         if (inSync)
         {
            handleCommit(id);
         }
      }      
      
      public void rollback(SynchronizationId<ClusterNode> id)
            throws NotSynchronizedException
      {
         if (inSync)
         {
            handleRollback(id);
         }
      }
   }
   
   public class DRMListener implements ReplicantListener
   {
      @SuppressWarnings("unchecked")
      public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId, boolean merge)
      {
         List<ClusterNode> oldView = serviceView;
         serviceView = newReplicants;
         
         // Abort any ongoing synchronization that if the originator is
         // out of the view
         ActiveSynchronization active = activeSynchronization;
         if (active != null)
         {
            synchronized (active)
            {
               if (serviceView.contains(active.getId().getOriginator()) == false)
               {
                  contentManager.rollbackSynchronization(active.getId());
                  activeSynchronization = null;
               }
            }
         }
         
         if (merge)
         {
            ClusterNode master = (ClusterNode) (newReplicants.size() > 0 ? newReplicants.get(0) : null);
            inSync = inSync && (master != null && oldView.contains(master));
         }
      }      
   }
   
   private static class NotSynchronizedException extends Exception
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -923676063561479453L;      
   }
   
   private class ActiveSynchronization
   {
      private final SynchronizationId<ClusterNode> id;
      private final List<? extends SynchronizationAction<?>> localActions;
      private final Map<RepositoryItemMetadata, SynchronizationAction<?>> miscActionsByItem = 
         new HashMap<RepositoryItemMetadata, SynchronizationAction<?>>();
      private final Map<RepositoryItemMetadata, SynchronizationReadAction<?>> readActionsByItem = 
         new HashMap<RepositoryItemMetadata, SynchronizationReadAction<?>>();
      private final Map<RepositoryItemMetadata, SynchronizationWriteAction<?>> writeActionsByItem = 
         new HashMap<RepositoryItemMetadata, SynchronizationWriteAction<?>>();
      private final Map<RepositoryItemMetadata, SynchronizationInitiationAction<?>> initiationActionsByItem = 
         new HashMap<RepositoryItemMetadata, SynchronizationInitiationAction<?>>();
      private volatile boolean alive = true;
      
      private ActiveSynchronization(SynchronizationId<ClusterNode> id, List<? extends SynchronizationAction<?>> localActions)
      {
         if (id == null)
         {
            throw new IllegalArgumentException("Null id");
         }
         if (localActions == null)
         {
            throw new IllegalArgumentException("Null localActions");
         }
         this.id = id;
         this.localActions = localActions;
         for (SynchronizationAction<?> action : localActions)
         {
            RepositoryItemMetadata item = action.getRepositoryContentModification().getItem();
            if (action instanceof SynchronizationInitiationAction<?>)
            {
               initiationActionsByItem.put(item, (SynchronizationInitiationAction<?>) action);
            }
            else if (action instanceof SynchronizationReadAction<?>)
            {
               readActionsByItem.put(item, (SynchronizationReadAction<?>) action);
            }
            else if (action instanceof SynchronizationWriteAction<?>)
            {
               writeActionsByItem.put(item, (SynchronizationWriteAction<?>) action);
            }
            else
            {
               miscActionsByItem.put(item, action);
            }
         }
      }
      
      public SynchronizationId<ClusterNode> getId()
      {
         return id;
      }
      
      public List<? extends SynchronizationAction<?>> getActions()
      {
         return localActions;
      }
      
      public void validate(SynchronizationId<ClusterNode> id)
      {
         if (this.id.equals(id) == false)
         {
            throw new IllegalStateException("Invalid id " + id + 
                  " another synchronization " + this.getId() + 
                  " is in progress");            
         }
      }
      
      public SynchronizationReadAction<?> getReadAction(RepositoryItemMetadata item)
      {
         return readActionsByItem.get(item);
      }
      
      public SynchronizationWriteAction<?> getWriteAction(RepositoryItemMetadata item)
      {
         return writeActionsByItem.get(item);
      }
      
      public SynchronizationInitiationAction<?> getInitiationAction(RepositoryItemMetadata item)
      {
         return initiationActionsByItem.get(item);
      }
      
      public SynchronizationAction<?> getMiscAction(RepositoryItemMetadata item)
      {
         return miscActionsByItem.get(item);
      }
      
      public boolean isAlive()
      {
         return alive;
      }
      
      public boolean prepare()
      {
         if (alive)
         {
            return contentManager.prepareSynchronization(id);
         }
         return false;
      }
      
      public void rollback()
      {
         if (alive)
         {
            alive = false;
            synchronized (this)
            {
               contentManager.rollbackSynchronization(id);
            }
         }
      }
      
      public void commit()
      {
         if (alive)
         {
            synchronized (this)
            {
               contentManager.commitSynchronization(id);
            }
            alive = false;
         }
      }
   }

}
