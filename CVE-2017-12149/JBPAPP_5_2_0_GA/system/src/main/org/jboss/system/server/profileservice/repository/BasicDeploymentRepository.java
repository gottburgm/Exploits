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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SyncFailedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipInputStream;

import org.jboss.profileservice.spi.DeploymentContentFlags;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * A basic deployment repository.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 106340 $
 */
public class BasicDeploymentRepository extends AbstractDeploymentRepository
{

   /** Should an attempt to overwrite existing content fail in {@link #addDeploymentContent(String, ZipInputStream)}*/
   private boolean failIfAlreadyExists = false;
   
   /** A optional upload uri. */
   private URI uploadUri;
   
   /** A lock for the hot deployment/{@link #getModifiedDeployments()} */
   private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
   
   public BasicDeploymentRepository(ProfileKey key, URI[] uris)
   {
      super(key, uris);
   }
   
   public boolean isFailIfAlreadyExists()
   {
      return failIfAlreadyExists;
   }
   
   public void setFailIfAlreadyExists(boolean failIfAlreadyExists)
   {
      this.failIfAlreadyExists = failIfAlreadyExists;
   }
   
   public URI getUploadUri()
   {
      if(uploadUri != null)
         return uploadUri;

      if(getRepositoryURIs() != null && getRepositoryURIs().length > 0)
         return getRepositoryURIs()[0];
      
      throw new IllegalArgumentException("No upload uri available.");
   }
   
   public void setUploadUri(URI uploadUri)
   {
      if(uploadUri == null)
      {
         this.uploadUri = null;
         return;
      }
      // Validate
      if(getRepositoryURIs() != null && getRepositoryURIs().length > 0)
      {
         if(Arrays.asList(getRepositoryURIs()).contains(uploadUri))
         {
            this.uploadUri = uploadUri;
            return;
         }
      }
      throw new IllegalArgumentException("Upload uri is not managed by this repository: "+ uploadUri);
   }

   public void load() throws Exception
   {
      for(URI uri : getRepositoryURIs())
      {
         VirtualFile root = getCachedVirtualFile(uri);
         loadApplications(root);
      }
      updateLastModfied();
   }
   
   @Override
   public void addDeployment(String vfsPath, ProfileDeployment d) throws Exception
   {
      // Suspend hot deployment checking
      if( log.isTraceEnabled() )
         log.trace("Aquiring content write lock");
      lockWrite();
      try
      {
         internalAddDeployment(vfsPath, d);
      }
      finally
      {
         // Allow hot deployment checking
         unlockWrite();
         if(log.isTraceEnabled())
            log.trace("Released content write lock");
      }
   }
   
   public Collection<ModificationInfo> getModifiedDeployments() throws Exception
   {
      return Collections.emptySet();
   }

   public String addDeploymentContent(String vfsPath, InputStream contentIS) throws IOException
   {
      return addDeploymentContent(vfsPath, contentIS, new DeploymentOption[0]);
   }
   
   public String addDeploymentContent(String vfsPath, InputStream contentIS, DeploymentOption... options)
      throws IOException
   {     
      boolean trace = log.isTraceEnabled();
      // Suspend hot deployment checking
      if( trace )
         log.trace("Aquiring content write lock");
      lockWrite();
      String repositoryName = null;
      try
      {
         // Write the content out
         File contentRoot = new File(getUploadUri()); 
         if(contentRoot == null)
            throw new FileNotFoundException("Failed to obtain content dir for phase: "+vfsPath);
         if(contentRoot.isDirectory() == false)
            throw new FileNotFoundException("The content root is not a directory." + contentRoot.getAbsolutePath());
         // The content file
         File contentFile = new File(contentRoot, vfsPath);
         
         // Check if it already exists
         boolean exists = contentFile.exists();
         // Get the content options
         List<DeploymentOption> deploymentOptions = Arrays.asList(options); 
         boolean failIfAlreadyExsists = isFailIfAlreadyExists()
                  || deploymentOptions.contains(DeploymentOption.FailIfExists);
         if(exists && failIfAlreadyExsists)
            throw new SyncFailedException("Deployment content already exists: "+ contentFile.getAbsolutePath());
         
         // Check if we need unpack this deployment
         if(deploymentOptions.contains(DeploymentOption.Explode))
         {
            // Unjar
            DeploymentUtils.unjar(contentIS, contentFile);
         }
         else
         {
            // Copy stream
            FileOutputStream fos = new FileOutputStream(contentFile);
            try
            {
               byte[] tmp = new byte[4096];
               int read;
               while((read = contentIS.read(tmp)) > 0)
               {
                  if (trace)
                     log.trace("write, " + read);
                  fos.write(tmp, 0, read);
               }
               fos.flush();
            }
            finally
            {
               try
               {
                  fos.close();
               }
               catch (IOException ignored)
               {
               }
            }
            contentFile.setLastModified(System.currentTimeMillis());
         }

         // Get the vfs uri and add the VFS uri to the cached VFS uris
         VirtualFile contentVF = VFS.getRoot(contentFile.toURI());
         try
         {
            // Add the new virtual file to the cache
            repositoryName = addVirtualFileCache(contentVF);
            
            // Cleanup 
            if(exists)
               cleanUpRoot(contentVF);
         }
         catch(URISyntaxException e)
         {
            throw new RuntimeException(e); // This should not happen anyway
         }

         // Lock the content
         setDeploymentContentFlags(repositoryName, DeploymentContentFlags.LOCKED);
      }
      finally
      {
         // Allow hot deployment checking
         unlockWrite();
         if(trace)
            log.trace("Released content write lock");
      }
      return repositoryName;
   }
   
   public ProfileDeployment removeDeployment(String vfsPath) throws Exception
   {
      return removeDeployment(vfsPath, true);
   }

   /**
    * Internal add the deployment, without locking the repository.
    * 
    * @param vfsPath - the name of the deployment
    * @param deployment - the deployment
    * @throws Exception
    */
   protected void internalAddDeployment(String vfsPath, ProfileDeployment deployment) throws Exception
   {
      super.addDeployment(vfsPath, deployment);
   }
   
   /**
    * Remove deployment.
    *
    * @param vfsPath the vfs path
    * @param deleteFile do we delete the file
    * @return found profile deployment
    * @throws Exception for any error
    */
   protected ProfileDeployment removeDeployment(String vfsPath, boolean deleteFile) throws Exception
   {
      // Suspend hot deployment checking
      if( log.isTraceEnabled() )
         log.trace("Aquiring content write lock");
      lockWrite();
      try
      {
         // Remove the deployment from the filesystem
         ProfileDeployment deployment = getDeployment(vfsPath);
         VirtualFile root = deployment.getRoot();
         
         if(deleteFile && root != null)
         {
            // Delete the file, fail if it can't be deleted and still exists
            if(root.delete() == false && root.exists())
               throw new IOException("Failed to delete: " + root);
            
            cleanUpRoot(root);  
         }
         
         // Cleanup
         return super.removeDeployment(deployment.getName());
      }
      finally
      {
         unlockWrite();
         if (log.isTraceEnabled())
            log.trace("Released content write lock");
      }
   }
   
   /**
    * A way for the hot-deployment repository to cleanup
    * the root (modification checker).
    * 
    * @param vf the deployment root
    */
   protected void cleanUpRoot(VirtualFile vf)
   {
      //
   }
   
   public void remove() throws Exception
   {
      // FIXME remove
   }
   
   /**
    * Lock for read
    */
   protected void lockRead()
   {
      lock.readLock().lock();
   }

   /**
    * Unlock for read
    */
   protected void unlockRead()
   {
      lock.readLock().unlock();
   }

   /**
    * Lock for write
    */
   protected void lockWrite()
   {
      lock.writeLock().lock();
   }

   /**
    * Unlock for write
    */
   protected void unlockWrite()
   {
      lock.writeLock().unlock();
   }

}

