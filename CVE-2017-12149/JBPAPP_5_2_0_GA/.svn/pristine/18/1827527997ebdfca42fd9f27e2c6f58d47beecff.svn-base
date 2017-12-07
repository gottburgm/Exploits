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

package org.jboss.system.server.profileservice.repository.clustered.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.ContentModification;
import org.jboss.system.server.profileservice.repository.clustered.sync.NoOpSynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.RemovalMetadataInsertionAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationAction;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationActionContext;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationId;
import org.jboss.system.server.profileservice.repository.clustered.sync.TwoPhaseCommitAction;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Abstract base class for a {@link LocalContentManager} implementation.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractLocalContentManager<T extends SynchronizationActionContext> implements LocalContentManager<T>
{
   private final Logger log = Logger.getLogger(getClass());
   
   private RepositoryContentMetadata officialContentMetadata;
   private RepositoryContentMetadata currentContentMetadata;
   private final Map<String, URI> namedURIMap;
   private final Map<String, VirtualFile> vfCache = new ConcurrentHashMap<String, VirtualFile>();
   private final ProfileKey profileKey;
   private final String storeName;
   private final String localNodeName;
   private final ContentMetadataPersister contentMetadataPersister;
   private List<TwoPhaseCommitAction<T>> currentSynchronizationActions;
   private T currentSynchronizationActionContext;
   private Map<RepositoryItemMetadata, InputStream> pendingStreams = new ConcurrentHashMap<RepositoryItemMetadata, InputStream>();

   
   protected static String createStoreName(ProfileKey key)
   {
      // Normal case
      if (ProfileKey.DEFAULT.equals(key.getDomain()) 
            && ProfileKey.DEFAULT.equals(key.getServer()))
      {
         return key.getName();
      }
      
      StringBuilder sb = new StringBuilder();
      if (ProfileKey.DEFAULT.equals(key.getDomain()) == false)
      {
         sb.append(key.getDomain());
         sb.append('-');
      }
      if (ProfileKey.DEFAULT.equals(key.getServer()) == false)
      {
         sb.append(key.getServer());
         sb.append('-');
      }
      sb.append(key.getName());
      return sb.toString();
   }
   
   /**
    * Create a new AbstractLocalContentManager.
    * 
    * @param namedURIMap Map of URIs managed by this object, keyed by a
    *                    String identifier
    * @param profileKey  key of the profile the content of which this
    *                    object is managing
    * @param localNodeName name of the local node in the cluster
    * @param contentMetadataPersister object to use for storing/retrieving content metadata
    */
   protected AbstractLocalContentManager(Map<String, URI> namedURIMap,
         ProfileKey profileKey, String localNodeName, 
         ContentMetadataPersister contentMetadataPersister)
   {
      if (namedURIMap == null)
      {
         throw new IllegalArgumentException("Null namedURIMap");
      }
      if (profileKey == null)
      {
         throw new IllegalArgumentException("Null profileKey");
      }
      if (localNodeName == null)
      {
         throw new IllegalArgumentException("Null localNodeName");
      }
      if (contentMetadataPersister == null)
      {
         throw new IllegalArgumentException("Null contentMetadataPersister");
      }
      
      this.namedURIMap = namedURIMap;
      this.profileKey = profileKey;
      this.storeName = createStoreName(profileKey);
      this.localNodeName = localNodeName;
      this.contentMetadataPersister = contentMetadataPersister;
      this.officialContentMetadata = this.contentMetadataPersister.load(this.storeName);
   }
   
   // -------------------------------------------------------------  Properties

   public String getLocalNodeName()
   {
      return localNodeName;
   }
   
   public String getStoreName()
   {
      return storeName;
   }
   
   // ----------------------------------------------------  LocalContentManager

   public RepositoryContentMetadata getOfficialContentMetadata()
   {
      return officialContentMetadata;
   }

   public RepositoryContentMetadata createEmptyContentMetadata()
   {
      RepositoryContentMetadata md = new RepositoryContentMetadata(profileKey);
      List<RepositoryRootMetadata> roots = new ArrayList<RepositoryRootMetadata>();
      for (Map.Entry<String, URI> entry : namedURIMap.entrySet())
      {
         RepositoryRootMetadata rmd = new RepositoryRootMetadata();
         rmd.setName(entry.getKey());
         roots.add(rmd);
      }
      md.setRepositories(roots);
      return md;      
   }

   public RepositoryContentMetadata getCurrentContentMetadata() throws IOException
   {
      synchronized (this)
      {
         RepositoryContentMetadata md = new RepositoryContentMetadata(profileKey);
         List<RepositoryRootMetadata> roots = new ArrayList<RepositoryRootMetadata>();
         RepositoryContentMetadata official = getOfficialContentMetadata();
         for (Map.Entry<String, URI> entry : namedURIMap.entrySet())
         {
            RepositoryRootMetadata rmd = new RepositoryRootMetadata();
            rmd.setName(entry.getKey());
            RepositoryRootMetadata existingRmd = official == null ? null : official.getRepositoryRootMetadata(entry.getKey());
            
            VirtualFile root = getCachedVirtualFile(entry.getValue());         
            if (isDirectory(root))
            {
               for(VirtualFile child: root.getChildren())
               {
                  createItemMetadataFromScan(rmd, existingRmd, child, root);
               }
            }
            else
            {
               // The root is itself an item. Treat it as a "child" of
               // itself with no relative path
               createItemMetadataFromScan(rmd, existingRmd, root, root);
            }
            
            roots.add(rmd);
         }
         md.setRepositories(roots);
         
         // Retain any existing "removed item" metadata -- but only if
         // it wasn't re-added!!
         RepositoryContentMetadata existing = getOfficialContentMetadata();
         if (existing != null)
         {
            for (RepositoryRootMetadata existingRoot : existing.getRepositories())
            {
               RepositoryRootMetadata rmd = md.getRepositoryRootMetadata(existingRoot.getName());
               if (rmd != null)
               {
                  Collection<RepositoryItemMetadata> rimds = rmd.getContent();
                  for (RepositoryItemMetadata existingItem : existingRoot.getContent())
                  {
                     if (existingItem.isRemoved() // but check for re-add 
                           && rmd.getItemMetadata(existingItem.getRelativePathElements()) == null)
                     {
                        rimds.add(new RepositoryItemMetadata(existingItem));
                     }
                  }
               }
            }
         }
         
         this.currentContentMetadata = md;
         return this.currentContentMetadata;
      }
   }

   public List<? extends SynchronizationAction<T>> initiateSynchronization(SynchronizationId<?> id,
         List<ContentModification> modifications, RepositoryContentMetadata toInstall, boolean localLed)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Null id");
      }
      if (modifications == null)
      {
         throw new IllegalArgumentException("Null modifications");
      }
      if (toInstall == null)
      {
         throw new IllegalArgumentException("Null toInstall");
      }
      
      synchronized (this)
      {
         if (currentSynchronizationActionContext != null)
         {
            throw new IllegalStateException("Synchronization " + currentSynchronizationActionContext.getId() + 
                  " is already in progress");
         }
         
         this.currentSynchronizationActionContext = createSynchronizationActionContext(id, toInstall);
      }

      List<TwoPhaseCommitAction<T>> actions = new ArrayList<TwoPhaseCommitAction<T>>();
      for (ContentModification mod : modifications)
      {
         TwoPhaseCommitAction<T> action = null;
         switch (mod.getType())
         {
            case PULL_FROM_CLUSTER:
               action = createPullFromClusterAction(mod, localLed);
               break;
            case PUSH_TO_CLUSTER:
               InputStream stream = pendingStreams.remove(mod.getItem());
               if (stream == null)
               {
                  action = createPushToClusterAction(mod, localLed);
               }
               else
               {
                  action = createPushStreamToClusterAction(mod, stream);
               }
               break;
            case REMOVE_FROM_CLUSTER:
               action = createRemoveFromClusterAction(mod, localLed);
               break;
            case REMOVE_TO_CLUSTER:
               action = createRemoveToClusterAction(mod, localLed);
               break;
            case PREPARE_RMDIR_FROM_CLUSTER:
               action = createPrepareRmdirFromClusterAction(mod, localLed);
               break;
            case PREPARE_RMDIR_TO_CLUSTER:
               action = createPrepareRmdirToClusterAction(mod, localLed);
               break;
            case DIR_TIMESTAMP_MISMATCH:
               action = createDirectoryTimestampMismatchAction(mod, localLed);
               break;
            case MKDIR_FROM_CLUSTER:
               action = createMkdirFromClusterAction(mod, localLed);
               break;
            case MKDIR_TO_CLUSTER:
               action = createMkdirToClusterAction(mod, localLed);
               break;
            case REMOVAL_METADATA_FROM_CLUSTER:
               action = createRemovalMetadataAction(mod, localLed);
               break;
            default:
               throw new IllegalStateException("Unknown " + ContentModification.Type.class.getSimpleName() + " " + mod.getType());
         }
         actions.add(action);         
      }
      
      this.currentSynchronizationActions = actions;
      return Collections.unmodifiableList(actions);
   }

   public boolean prepareSynchronization(SynchronizationId<?> id)
   {
      validateSynchronization(id);
      for (TwoPhaseCommitAction<T> action : this.currentSynchronizationActions)
      {
         if (action.prepare() == false)
         {            
            if (log.isTraceEnabled())
            {
               ContentModification mod = action.getRepositoryContentModification();
               log.trace("prepare failed for " + mod.getType() + " " + mod.getItem().getRelativePath());
            }
            return false;
         }
      }
      
      if (log.isTraceEnabled())
      {
         log.trace("prepared synchronization " + id);
      }
      return true;
   }
   
   public void commitSynchronization(SynchronizationId<?> id)
   {
      validateSynchronization(id);
      for (TwoPhaseCommitAction<T> action : this.currentSynchronizationActions)
      {
         action.commit();
      }
      updateContentMetadata(this.currentSynchronizationActionContext.getInProgressMetadata());
      synchronized (this)
      {
         this.currentSynchronizationActions = null;
         this.currentSynchronizationActionContext = null;
      }
      
      if (log.isTraceEnabled())
      {
         log.trace("committed synchronization " + id);
      }
   }

   public void rollbackSynchronization(SynchronizationId<?> id)
   {
      validateSynchronization(id);
      
      for (TwoPhaseCommitAction<T> action : this.currentSynchronizationActions)
      {
         action.rollback();
      }
      synchronized (this)
      {
         this.currentSynchronizationActionContext = null;
         this.currentSynchronizationActions = null;
      }
      
      if (log.isTraceEnabled())
      {
         log.trace("rolled back synchronization " + id);
      }
   }
   
   public void installCurrentContentMetadata()
   {
      synchronized (this)
      {
         if (this.currentContentMetadata == null)
         {
            throw new IllegalStateException("No currentContentMetadata");
         }
         if (this.currentSynchronizationActionContext != null)
         {
            throw new IllegalStateException("Cannot install currentContentMetadata; " +
            		"cluster synchronization " + this.currentSynchronizationActionContext.getId() + 
            		" is in progress");
         }
         
         updateContentMetadata(this.currentContentMetadata);
      }      
   }

   public RepositoryItemMetadata getItemForAddition(String vfsPath) throws IOException
   {
      RepositoryItemMetadata item = new RepositoryItemMetadata();
      item.setRelativePath(vfsPath);
      item.setOriginatingNode(this.localNodeName);
      item.setTimestampAsString("NOW");
      List<String> pathElements = item.getRelativePathElements();
      String rootName = null;
      for (RepositoryRootMetadata rmd : getOfficialContentMetadata().getRepositories())
      {
         if (rmd.getItemMetadata(pathElements) != null)
         {
            // Exact match to existing item -- done
            rootName = rmd.getName();
            break;
         }
         else if (rootName == null)
         {
            // Use the first root that can accept children
            URI rootURI = namedURIMap.get(rmd.getName());
            VirtualFile vf = getCachedVirtualFile(rootURI);
            if (isDirectory(vf))
            {
               rootName = rmd.getName();
               break;               
            }
         }
      }
      
      if (rootName == null)
      {
         throw new IllegalStateException("No roots can accept children");
      }
      
      item.setRootName(rootName);
      return item;
   }

   public RepositoryContentMetadata getContentMetadataForAdd(RepositoryItemMetadata toAdd, InputStream contentIS) throws IOException
   {
      RepositoryContentMetadata result = new RepositoryContentMetadata(getOfficialContentMetadata());
      RepositoryRootMetadata rmd = result.getRepositoryRootMetadata(toAdd.getRootName());
      if (rmd == null)
      {
         throw new IllegalArgumentException("Unknown root name " + toAdd.getRootName());
      }
      RepositoryItemMetadata remove = rmd.getItemMetadata(toAdd.getRelativePathElements());
      if (remove != null)
      {
         Collection<RepositoryItemMetadata> content = rmd.getContent();
         if (remove.isDirectory())
         {
            for (Iterator<RepositoryItemMetadata> it = content.iterator(); it.hasNext(); )
            {
               if (it.next().isChildOf(remove))
               {
                  it.remove();
               }
            }
         }
         content.remove(remove);
      }
      rmd.getContent().add(toAdd);
      pendingStreams.put(toAdd, contentIS);      
      return result;
   }

   public VirtualFile getVirtualFileForItem(RepositoryItemMetadata item) throws IOException
   {
      URI uri = namedURIMap.get(item.getRootName());
      VirtualFile vf = getCachedVirtualFile(uri);
      VirtualFile parent = null;
      List<String> path = item.getRelativePathElements();
      for (String element : path)
      {
         parent = vf;
         vf = parent.getChild(element);
         if (vf == null)
         {
            throw new IllegalStateException("No child " + element + " under " + parent);
         }
      }
      return vf;
   }
   
   public RepositoryContentMetadata getContentMetadataForRemove(VirtualFile vf) throws IOException
   {
      List<String> path = null;
      RepositoryRootMetadata root = null;
      RepositoryContentMetadata cmd = new RepositoryContentMetadata(getOfficialContentMetadata());
      for (RepositoryRootMetadata rmd : cmd.getRepositories())
      {
         URI uri = namedURIMap.get(rmd.getName());
         VirtualFile vfRoot = getCachedVirtualFile(uri);
         try
         {
            path = getRelativePath(vf, vfRoot);
            root = rmd;
            break;
         }
         catch (IllegalStateException ise)
         {
            // vf wasn't a child; ignore and move on to next root
         }
      }
      
      if (root == null)
      {
         throw new IllegalArgumentException(vf + " is not a child of any known roots");
      }
      
      RepositoryItemMetadata remove = root.getItemMetadata(path);      
      if (remove != null)
      {
         Collection<RepositoryItemMetadata> items = root.getContent();
         if (isDirectory(vf))
         {
            for (Iterator<RepositoryItemMetadata> it = items.iterator(); it.hasNext(); )
            {
               if (it.next().isChildOf(remove))
               {
                  it.remove();
               }
            }
            
         }
         items.remove(remove);
      }
      return cmd;
   }  
   
   
   // --------------------------------------------------------------  Protected


   /** 
    * Create a {@link SynchronizationActionContext} for the given cluster-wide
    * content synchronization.
    * 
    * @param id the id of the synchronization
    * @param toUpdate metadata object that should be updated as synchronization
    *                 actions are performed.
    */
   protected abstract T createSynchronizationActionContext(SynchronizationId<?> id, RepositoryContentMetadata toUpdate);
   
   /**
    * Create an action to handle the local end of a node pulling content from
    * the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createPullFromClusterAction(ContentModification mod, boolean localLed);
   
   /**
    * Create an action to handle the local end of a node pushing content to
    * the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createPushToClusterAction(ContentModification mod, boolean localLed);

   /**
    * Create an action to handle the local end of a node pushing content that is
    * read from an external-to-the-repository stream to the cluster. Used to
    * handle installation of content to the repository via 
    * {@link DeploymentRepository#addDeployment(String, org.jboss.profileservice.spi.ProfileDeployment)}. 
    * <p> 
    * This is only invoked on the node that is driving the synchronization process.
    * </p>
    * 
    * @param mod object describing the content modification this action is
    *            part of
    *            
    * @param stream the stream from which content will be read.
    *                 
    * @return an action that will handle both the local end of pushing the stream content to
    *         other nodes in the cluster <b>and</b> storing the stream content
    *         in this node's repository. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createPushStreamToClusterAction(ContentModification mod, InputStream stream);
   
   /**
    * Create an action to handle the local end of a node removing content that
    * the rest of the cluster regards as invalid.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createRemoveFromClusterAction(ContentModification mod, boolean localLed);
   
   /**
    * Create an action to handle the local end of a node removing content from
    * the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createRemoveToClusterAction(ContentModification mod, boolean localLed);

   /**
    * Create an action to handle the local end of a node removing a directory
    * from the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createPrepareRmdirToClusterAction(ContentModification mod, boolean localLed);


   protected abstract TwoPhaseCommitAction<T> createPrepareRmdirFromClusterAction(ContentModification mod, boolean localLed);
   
   /**
    * Create an action to handle the local end of a node adding a directory
    * to the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createMkdirToClusterAction(ContentModification mod,
         boolean localLed);

   /**
    * Create an action to handle the local end of a node adding a directory
    * due to its presence on the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createMkdirFromClusterAction(
         ContentModification mod, boolean localLed);

   /**
    * Create an action to handle the local end of a node updating a directory
    * timestamp to match the cluster.
    * 
    * @param mod object describing the content modification this action is
    *            part of
    * @param localLed <code>true</code> if this node is driving the synchronization
    *                 process the action is part of; <code>false</code> if 
    *                 another node is
    *                 
    * @return the action. Will not return <code>null</code>.
    */
   protected abstract TwoPhaseCommitAction<T> createDirectoryTimestampMismatchAction(
         ContentModification mod, boolean localLed);

   /**
    * Gets the current {@link SynchronizationActionContext}.
    * 
    * @return the current context, or <code>null</code> if there isn't one
    */
   protected T getSynchronizationActionContext()
   {
      return currentSynchronizationActionContext;
   }
   
   /**
    * Gets a {@link VirtualFile} corresponding to the given URI.
    * 
    * @param uri the uri. Cannot be <code>null</code>.
    * @return the virtual file
    * 
    * @throws IOException
    * @throws NullPointerException if <code>uri</code> is <code>null</code>. 
    */
   protected VirtualFile getCachedVirtualFile(URI uri) throws IOException 
   {
      VirtualFile vf = this.vfCache.get(uri.toString());
      if(vf == null)
      {
         vf = VFS.getRoot(uri);
         this.vfCache.put(uri.toString(), vf);
      }
      return vf;
   }
   
   /**
    * Gets the URI of the repository root with which the given modification
    * is associated.
    * 
    * @param mod the modification. Cannot be <code>null</code>
    * @return the URI. May be <code>null</code> if the modification is for
    *         an unknown root
    *         
    * @throws NullPointerException if <code>uri</code> is <code>null</code>.
    * 
    *  @see ContentModification#getRootName()
    */
   protected URI getRootURIForModification(ContentModification mod)
   {
      return namedURIMap.get(mod.getRootName());
   }
   
   // --------------------------------------------------------------  Private
   
   private TwoPhaseCommitAction<T> createRemovalMetadataAction(ContentModification mod,
                                                            boolean localLed)
   {
      if (localLed)
      {
         return new RemovalMetadataInsertionAction<T>(getSynchronizationActionContext(), mod);
      }
      else
      {
         return new NoOpSynchronizationAction<T>(getSynchronizationActionContext(), mod);
      }
   }
   
   private void updateContentMetadata(RepositoryContentMetadata newOfficial)
   {
      if (newOfficial.equals(this.officialContentMetadata) == false)
      {
         try
         {
            this.contentMetadataPersister.store(this.storeName, newOfficial);
         }
         catch (Exception e)
         {
            log.error("Caught exception persisting " + RepositoryContentMetadata.class.getSimpleName(), e);
         }
         this.officialContentMetadata = newOfficial;
         
         if (log.isTraceEnabled())
         {
            log.trace("updateContentMetadata(): updated official metadata");
         }
      }
      else if (log.isTraceEnabled())
      {
         log.trace("updateContentMetadata(): content is unchanged");
      }
      this.currentContentMetadata = null;
   }
   
   private void createItemMetadataFromScan(RepositoryRootMetadata rmd, 
                                           RepositoryRootMetadata existingRMD, 
                                           VirtualFile file, VirtualFile root) 
        throws IOException
   {      
      boolean directory = isDirectory(file);
      long timestamp = file.getLastModified();
      
      List<String> pathElements = getRelativePath(file, root);
      RepositoryItemMetadata existing = existingRMD == null ? null : existingRMD.getItemMetadata(pathElements);
      
      // If there's an existing item, assume for now it's unchanged and keep existing originator
      String originator = existing == null ? this.localNodeName : existing.getOriginatingNode();      
      RepositoryItemMetadata md = new RepositoryItemMetadata(pathElements, timestamp, originator, directory, false);
      if (md.equals(existing) == false)
      {
         // above if test failing means this is a new item or
         // timestamp, removed or directory status has changed
         // In any case, this node is now the originator
         md.setOriginatingNode(this.localNodeName);
      }
      
      rmd.getContent().add(md);
      
      if (directory)
      {
         for(VirtualFile child: file.getChildren())
         {
            createItemMetadataFromScan(rmd, existingRMD, child, root);
         }
      }
   }

   private void validateSynchronization(SynchronizationId<?> id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Null id");
      }
      
      if (this.currentSynchronizationActionContext == null)
      {
         throw new IllegalStateException("No active synchronization");
      }
      
      SynchronizationId<?> ours = this.currentSynchronizationActionContext.getId();
      if (id.equals(ours) == false)
      {
         throw new IllegalStateException(id + " does not match the current synchronization " + ours);
      }
   }
   
   private static boolean isDirectory(VirtualFile file) throws IOException
   {
      return (!file.isLeaf() && !file.isArchive());
   }
   
   private static List<String> getRelativePath(VirtualFile file, VirtualFile root)
      throws IOException
   {
      List<String> reversed = new ArrayList<String>();
      VirtualFile now = file;
      while(now != null && now.equals(root) == false)
      {
         reversed.add(now.getName());
         now = now.getParent();
      }
      
      if (now == null)
      {
         throw new IllegalArgumentException(file + " is not a child of " + root);
      }
      
      List<String> forward = new ArrayList<String>(reversed.size());
      for (int i = reversed.size() - 1; i > -1; i--)
      {
         forward.add(reversed.get(i));
      }
      
      return forward;
   }

}
