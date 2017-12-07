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
package org.jboss.system.server.profileservice.repository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.deployers.vfs.spi.structure.modified.StructureModificationChecker;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.DeploymentRepositoryFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.HotDeploymentProfileSourceMetaData;
import org.jboss.system.server.profile.repository.metadata.ImmutableProfileSourceMetaData;
import org.jboss.virtual.VirtualFileFilter;

/**
 * The default repository factory for immutable and mutable repositories.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86176 $
 */
public class DefaultDeploymentRepositoryFactory implements DeploymentRepositoryFactory
{   
   /** The mutable type. */
   public final static String MUTABLE_TYPE = HotDeploymentProfileSourceMetaData.class.getName();
   
   /** The immutable type. */
   public final static String IMMUTABLE_TYPE = ImmutableProfileSourceMetaData.class.getName();
  
   /** The repository types. */
   public final static Collection<String> types; 
   
   /** A list of uploadURIs. */
   private List<URI> uploadURIs;
   
   /** The isFailIfAlreadyExists property. */
   private boolean failIfAlreadyExists = false;

   /** The deployment filter. */
   private VirtualFileFilter deploymentFilter;

   /** The structure modification checker */
   private StructureModificationChecker checker;

   static
   {
      types = Arrays.asList(MUTABLE_TYPE, IMMUTABLE_TYPE);
   }
   
   public String[] getTypes()
   {
      return types.toArray(new String[types.size()]);
   }
   
   public VirtualFileFilter getDeploymentFilter()
   {
      return deploymentFilter;
   }
   
   public void setDeploymentFilter(VirtualFileFilter deploymentFilter)
   {
      this.deploymentFilter = deploymentFilter;
   }
   
   public boolean isFailIfAlreadyExists()
   {
      return failIfAlreadyExists;
   }
   
   public void setFailIfAlreadyExists(boolean failIfAlreadyExists)
   {
      this.failIfAlreadyExists = failIfAlreadyExists;
   }
   
   public List<URI> getUploadURIs()
   {
      return uploadURIs;
   }
   
   public void setUploadURIs(List<URI> uploadURIs)
   {
      this.uploadURIs = uploadURIs;
   }

   public StructureModificationChecker getChecker()
   {
      return checker;
   }

   public void setChecker(StructureModificationChecker checker)
   {
      this.checker = checker;
   }

   public DeploymentRepository createDeploymentRepository(ProfileKey key, ProfileSourceMetaData metaData)
         throws Exception
   {
      if(metaData == null)
         throw new IllegalArgumentException("Null metaData");
      
      // Sanity check
      String repositoryType = metaData.getType();
      if(repositoryType == null)
         throw new IllegalArgumentException("Null repository type.");
      if(types.contains(repositoryType) == false)
         throw new IllegalArgumentException("Cannot handle type: " + repositoryType);
      
      boolean mutable = false;
      if(repositoryType.equals(MUTABLE_TYPE))
      {
         mutable = true;
      }
      
      return createDeploymentRepository(mutable, key, createUris(metaData));
   }
   
   protected DeploymentRepository createDeploymentRepository(boolean mutable, ProfileKey key, URI[] uris) throws Exception
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(uris == null)
         throw new IllegalArgumentException("Null uris");

      BasicDeploymentRepository repository;
      if(mutable)
      {
         repository = new HotDeploymentRepository(key, uris);
         // Set modification checker
         ((HotDeploymentRepository)repository).setChecker(checker);
      }
      else
      {
         repository = new BasicDeploymentRepository(key, uris);
      }
      // Set a optional upload dir
      setUploadURI(repository);
      // Set the deployment filter
      repository.setDeploymentFilter(deploymentFilter);
      // Set if the repository should override existing content
      repository.setFailIfAlreadyExists(isFailIfAlreadyExists());
      
      return repository;
   }
   
   /**
    * Define a upload uri for a deployment repository.
    * 
    * @param repository the deployment repository
    */
   protected void setUploadURI(BasicDeploymentRepository repository)
   {
      if(this.uploadURIs != null && this.uploadURIs.isEmpty() == false)
      {
         URI[] repositoryURIs = repository.getRepositoryURIs();
         if(repositoryURIs != null && repositoryURIs.length > 0)
         {
            for(URI repositoryURI : repositoryURIs)
            {
               if(this.uploadURIs.contains(repositoryURI))
               {
                  repository.setUploadUri(repositoryURI);
               }
            }
         }
      }
   }
   
   protected URI[] createUris(ProfileSourceMetaData metaData) throws URISyntaxException
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
