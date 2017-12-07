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

package org.jboss.system.server.profileservice.repository.clustered;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.DeploymentRepositoryFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManagerFactory;
import org.jboss.system.server.profileservice.repository.clustered.metadata.ClusteredProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.clustered.metadata.HotDeploymentClusteredProfileSourceMetaData;
import org.jboss.system.server.profileservice.repository.clustered.metadata.ImmutableClusteredProfileSourceMetaData;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Factory for clustered deployment repositories.
 * 
 * @author Brian Stansberry
 */
public class ClusteredDeploymentRepositoryFactory implements DeploymentRepositoryFactory
{   
   /** The mutable type. */
   public final static String MUTABLE_TYPE = HotDeploymentClusteredProfileSourceMetaData.class.getName();
   
   /** The immutable type. */
   public final static String IMMUTABLE_TYPE = ImmutableClusteredProfileSourceMetaData.class.getName();
  
   /** The repository types. */
   private static final List<String> types = Arrays.asList(new String[]{ MUTABLE_TYPE, IMMUTABLE_TYPE });
   
   private String defaultPartitionName;
   
   /** The deployment filter. */
   private VirtualFileFilter deploymentFilter;

   /** The structure modification checker */
   private StructureModificationChecker checker;
   
   /** Factories for LocalContentManagers */
   private final Set<LocalContentManagerFactory<?>> localContentManagerFactories = new HashSet<LocalContentManagerFactory<?>>();
   
   private final Map<ProfileKey, RepositoryClusteringHandler> clusteringHandlers = new HashMap<ProfileKey, RepositoryClusteringHandler>();
   
   // -------------------------------------------------------------- Properties

   public String getDefaultPartitionName()
   {
      if (defaultPartitionName == null)
      {
         defaultPartitionName = new PrivilegedAction<String>()
         {
            public String run()
            {
               return System.getProperty("jboss.partition.name", "DefaultPartition");
            }
            
         }.run();
      }
      return defaultPartitionName;
   }

   public void setDefaultPartitionName(String defaultPartitionName)
   {
      this.defaultPartitionName = defaultPartitionName;
   }
   
   public VirtualFileFilter getDeploymentFilter()
   {
      return deploymentFilter;
   }
   
   public void setDeploymentFilter(VirtualFileFilter deploymentFilter)
   {
      this.deploymentFilter = deploymentFilter;
   }

   public StructureModificationChecker getChecker()
   {
      return checker;
   }

   public void setChecker(StructureModificationChecker checker)
   {
      this.checker = checker;
   }
   
   
   // -------------------------------------------- Install/Uninstall Callbacks

   
   public void addRepositoryClusteringHandler(RepositoryClusteringHandler handler)
   {
      if (handler != null)
      {
         ProfileKey key = handler.getProfileKey();
         synchronized (clusteringHandlers)
         {
            clusteringHandlers.put(key, handler);
         }
      }      
   }
   
   public void removeRepositoryClusteringHandler(RepositoryClusteringHandler handler)
   {
      if (handler != null)
      {
         ProfileKey key = handler.getProfileKey();
         synchronized (clusteringHandlers)
         {
            clusteringHandlers.remove(key);
         }
      }  
   }
   
   public void addLocalContentManagerFactory(LocalContentManagerFactory<?> factory)
   {
      synchronized (localContentManagerFactories)
      {
         localContentManagerFactories.add(factory);
      }
   }
   
   public void removeLocalContentManagerFactory(LocalContentManagerFactory<?> factory)
   {
      synchronized (localContentManagerFactories)
      {
         localContentManagerFactories.remove(factory);
      }
   }
   
   // --------------------------------------------- DeploymentRepositoryFactory
   
   
   public DeploymentRepository createDeploymentRepository(ProfileKey key, ProfileSourceMetaData metaData)
         throws Exception
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(metaData == null)
         throw new IllegalArgumentException("Null metaData");
      
      if ((metaData instanceof ClusteredProfileSourceMetaData) == false)
      {
         throw new IllegalArgumentException("Incompatible metadata type " + 
               metaData.getClass().getName() + " -- a " + 
               ClusteredProfileSourceMetaData.class.getSimpleName() + " is required");
      }
      
      // Sanity check
      String repositoryType = metaData.getType();
      if(repositoryType == null)
         throw new IllegalArgumentException("Null repository type.");
      if(types.contains(repositoryType) == false)
         throw new IllegalArgumentException("Cannot handle type: " + repositoryType);
      
      ClusteredProfileSourceMetaData clusteredMD = (ClusteredProfileSourceMetaData) metaData;
      if (clusteredMD.getPartitionName() == null)
      {
         clusteredMD.setPartitionName(getDefaultPartitionName());
      }
      
      URI[] uris = createUris(metaData);
      Map<ProfileKey, RepositoryClusteringHandler> handlers = Collections.unmodifiableMap(this.clusteringHandlers);
      Set<LocalContentManagerFactory<?>> persisters = Collections.unmodifiableSet(this.localContentManagerFactories);
      
      boolean immutable = clusteredMD instanceof ImmutableClusteredProfileSourceMetaData; 
      if (immutable)
      {
         ImmutableClusteredDeploymentRepository repository = new ImmutableClusteredDeploymentRepository(key, uris, handlers, persisters);
         // Manually inject beans :)
         repository.setDeploymentFilter(deploymentFilter);
         repository.setChecker(checker);
         
         return repository;
      }
      else
      {
         ClusteredDeploymentRepository repository = new ClusteredDeploymentRepository(key, uris, handlers, persisters);
         // Manually inject beans :)
         repository.setDeploymentFilter(deploymentFilter);
         repository.setChecker(checker);
         
         return repository;
      }
   }

   public String[] getTypes()
   {
      return types.toArray(new String[types.size()]); 
   }
   
   // ----------------------------------------------------------------- Private
   
   private static URI[] createUris(ProfileSourceMetaData metaData) throws URISyntaxException
   {
      List<URI> uris = new ArrayList<URI>();
      for(String source : metaData.getSources())
      {
         URI uri = new URI(source);
         uris.add(uri);
      }
      return uris.toArray(new URI[uris.size()]);
   }

}
