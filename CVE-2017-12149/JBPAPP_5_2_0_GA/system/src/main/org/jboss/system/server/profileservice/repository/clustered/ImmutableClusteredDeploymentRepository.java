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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.local.LocalContentManagerFactory;

/**
 * A {@link ClusteredDeploymentRepository} that will only accept content changes
 * <i>pulled</i> from the cluster at {@link DeploymentRepository#load() load} time;
 * pushing updates to the cluster at load is not support, nor are changes
 * after load supported.  Such a repository can be used to provision a node
 * from the cluster at startup, while leaving content immutable thereafter.
 * 
 * @author Brian Stansberry
 */
public class ImmutableClusteredDeploymentRepository extends ClusteredDeploymentRepository
{
   
   /**
    * Create a new ImmutableClusteredDeploymentRepository.
    * 
    * @param key
    * @param uris
    */
   public ImmutableClusteredDeploymentRepository(ProfileKey key, URI[] uris, 
         Map<ProfileKey, RepositoryClusteringHandler> clusteringHandlers, 
         Set<LocalContentManagerFactory<?>> persisterFactories)
        throws IOException
   {
      super(key, uris, clusteringHandlers, persisterFactories);
   }
   
   public Collection<ModificationInfo> getModifiedDeployments() throws Exception
   {
      return Collections.emptySet();
   }

   public String addDeploymentContent(String vfsPath, InputStream contentIS, DeploymentOption... options) throws IOException
   {
      throw new IllegalStateException("Cannot add content to an immutable repository.");
   }

   public void remove() throws Exception
   {
      throw new IllegalStateException("Cannot remove immutable repository.");
   }

   @Override
   public synchronized boolean registerClusteringHandler(RepositoryClusteringHandler handler)
   {
      boolean update = super.registerClusteringHandler(handler);
      if (update)
      {
         RepositoryClusteringHandler ours = getClusteringHandler();
         ours.setImmutable(true);
      }
      return update;
   }

}
