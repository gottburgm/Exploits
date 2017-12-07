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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.DeploymentRepositoryFactory;
import org.jboss.profileservice.spi.NoSuchProfileException;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileRepository;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;

/**
 * The profile repository.
 * 
 * This accepts any implementation of DeploymentRepositoryFactory and delegates
 * the creation of the repository to one of the installed factories, based on
 * the exposed types. 
 * 
 * TODO reuse DeploymentRepositories with the same sources ?
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 98983 $
 */
public class TypedProfileRepository implements ProfileRepository
{
   
   /** The repository factories. */
   private Map<String, DeploymentRepositoryFactory> repositoryFactories = new ConcurrentHashMap<String, DeploymentRepositoryFactory>();

   /** The created repositories. */
   private Map<ProfileKey, DeploymentRepository> repositories = new ConcurrentHashMap<ProfileKey, DeploymentRepository>();
   
   public Collection<ProfileKey> getProfileKeys()
   {
      return this.repositories.keySet();
   }

   public DeploymentRepository getProfileDeploymentRepository(ProfileKey key) throws NoSuchProfileException
   {
      DeploymentRepository repository = this.repositories.get(key);
      if(repository == null)
         throw new NoSuchProfileException("No such repository for profile: "  + key);
      
      return repository;
   }
   
   public DeploymentRepository createProfileDeploymentRepository(ProfileKey key, ProfileMetaData metaData) throws Exception
   {
      if(metaData == null)
         throw new IllegalArgumentException("Null metaData");
      if(metaData.getName() == null)
         throw new IllegalArgumentException("Null metaData name");
      
      DeploymentRepository repository = this.repositories.get(key);
      if(repository == null)
      {
         String type = null;
         if(metaData.getSource() == null)
            throw new IllegalStateException("No profile source.");

         // Extract the profile source type
         type = metaData.getSource().getType();
         if( type == null )
            throw new IllegalArgumentException("Null profile source type.");
   
         ProfileSourceMetaData source = metaData.getSource();
         
         // TODO check if there is a conflict with hotdeployment repositories
         repository = createProfileDeploymentRepository(key, type , source);
         if(repository != null)
         {
            this.repositories.put(key, repository);
         }
      }
      return repository;
   }
   
   protected DeploymentRepository createProfileDeploymentRepository(ProfileKey key, String repositoryType, ProfileSourceMetaData metaData) throws Exception
   {
      DeploymentRepositoryFactory factory = this.repositoryFactories.get(repositoryType);
      if(factory ==  null)
         throw new IllegalStateException("No registered factory for repository type: "+ repositoryType);
      
      // Let the factory create the repository
      return factory.createDeploymentRepository(key, metaData);
   }

   public void removeProfileDeploymentRepository(ProfileKey key) throws Exception, NoSuchProfileException
   {
      DeploymentRepository repository = this.repositories.remove(key);
      if(repository == null)
         throw new NoSuchProfileException("No such repository for profile: "  + key);
      
      // Remove
      repository.remove();
   }
   
   public void addRepositoryFactory(DeploymentRepositoryFactory factory)
   {
      if(factory == null)
         throw new IllegalArgumentException("Null factory.");
      if(factory.getTypes() == null)
         throw new IllegalArgumentException("Empty factory type.");
      
      for(String type : factory.getTypes())
         this.repositoryFactories.put(type, factory);
   }
   
   public void removeRepositoryFactory(DeploymentRepositoryFactory factory)
   {
      if(factory == null)
         throw new IllegalArgumentException("Null factory.");
      if(factory.getTypes() == null)
         throw new IllegalArgumentException("Empty factory type.");
      
      for(String type : factory.getTypes())
         this.repositoryFactories.remove(type);
   }
   
   /**
    * InCallback to register a repository bean.
    * 
    * @param repository the deploymentRepository to register
    */
   public void registerDeploymentRepository(AbstractDeploymentRepository repository)
   {
      if(repository == null)
         throw new IllegalArgumentException("null deployment repository");
      ProfileKey repositoryKey = repository.getProfileKey(); 
      if(repositoryKey == null)
         throw new IllegalArgumentException("null profile key");
      if(this.repositories.containsKey(repositoryKey))
         throw new IllegalStateException("duplicate repository " + repositoryKey);
      
      this.repositories.put(repositoryKey, repository);
   }
   
   /**
    * UnInCallback to unregister a repository bean.
    * 
    * @param repository the deploymentRepository to unregister
    */
   public void unregisterDeploymentRepository(AbstractDeploymentRepository repository)
   {
      if(repository == null)
         throw new IllegalArgumentException("null deployment repository");
      ProfileKey repositoryKey = repository.getProfileKey(); 
      if(repositoryKey == null)
         throw new IllegalArgumentException("null profile key");
      
      this.unregisterDeploymentRepository(repositoryKey);
   }
   
   public void unregisterDeploymentRepository(ProfileKey repositoryKey)
   {
      if(repositoryKey == null)
         throw new IllegalArgumentException("null profile key");
      this.repositories.remove(repositoryKey);
   }
   
}
