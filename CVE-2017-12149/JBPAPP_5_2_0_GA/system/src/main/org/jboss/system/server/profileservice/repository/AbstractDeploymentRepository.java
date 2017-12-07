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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.profileservice.spi.DeploymentContentFlags;
import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.virtual.VirtualFile;

/**
 * A abstract VFS based deployment repository.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87267 $
 */
public abstract class AbstractDeploymentRepository extends AbstractVFSProfileSource implements DeploymentRepository
{
   
   /** The ignore flags. */
   protected final static int ignoreFlags = DeploymentContentFlags.LOCKED | DeploymentContentFlags.DISABLED;
   
   /** The associated profile key. */
   private ProfileKey key;
   
   /** The content flags. */
   private Map<String, Integer> contentFlags = new ConcurrentHashMap<String, Integer>();
      
   public AbstractDeploymentRepository(ProfileKey key, URI[] uris)
   {
      super(uris);
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      
      this.key = key;
   }
   
   public ProfileKey getProfileKey()
   {
      return this.key;
   }

   public void create() throws Exception
   {
      //
   }
   
   public void unload()
   {
      super.destroy();
      // Unload
      this.contentFlags.clear();
   }
   
   
   @Override
   protected boolean acceptsDeployment(String name)
   {
      if(hasDeploymentContentFlags(name, ignoreFlags))
      {
         if(log.isTraceEnabled())
            log.trace("Ignoring locked application: " + name);
         
         return false;
      }
      return super.acceptsDeployment(name);
   }
   
   @Override
   public ProfileDeployment removeDeployment(String vfsPath) throws Exception
   {
      if(vfsPath == null)
         throw new IllegalArgumentException("Null vfsPath");
      
      // Get the deployment
      ProfileDeployment deployment = super.removeDeployment(vfsPath);
      // Remove the entries
      this.contentFlags.remove(deployment.getName());
      // Return
      return deployment;
   }

   @Override
   public ProfileDeployment getDeployment(String vfsPath) throws NoSuchDeploymentException
   {
      if(vfsPath == null)
         throw new IllegalArgumentException("Null vfsPath");
      
      ProfileDeployment ctx = super.getDeployment(vfsPath);
      if(ctx == null)
      {
         List<String> names = findDeploymentContent(vfsPath);
         if(names.size() == 1)
         {
            ctx = super.getDeployment(names.get(0));
         }
         else if(names.size() > 1)
         {
            throw new NoSuchDeploymentException("Multiple deployments found for: "+ vfsPath +", available: " + names);            
         }
      }
      if(ctx == null)
      {
         log.debug("Failed to find application for: "+vfsPath+", available: " + getDeploymentNames());
         throw new NoSuchDeploymentException("Failed to find deployment in file: " + vfsPath);
      }
      return ctx;
   }

   public VirtualFile getDeploymentContent(String name) throws IOException
   {
      if(name == null)
         throw new IllegalArgumentException("Null name");
      
      // A deploy content needs to be added over addDeployContent
      VirtualFile vf = getCachedVirtualFile(name);
      if(vf == null)
      {
         List<String> matchingNames = findDeploymentContent(name);
         if(matchingNames.size() == 1)
         {
            vf = getCachedVirtualFile(matchingNames.get(0));
         }
         else if(matchingNames.size() > 1)
         {
            throw new FileNotFoundException("Multiple names found for name: " + name + ", profile: "+ key + ", available: " + matchingNames);
         }
      }
      if(vf == null)
         throw new FileNotFoundException("Failed to find content in profile: "+ key + " filename: " + name);
      
      return vf;
   }
   
   public String[] getRepositoryNames(String... names) throws IOException
   {
      if(names == null)
         throw new IllegalArgumentException("Null names[]");
      
      Collection<String> tmp = new HashSet<String>();
      for(String name : names)
      {
         if(getCachedVirtualFile(name) != null)
         {
            tmp.add(name);
         }
         else
         {
            // Try to find the name
            List<String> deploymentNames = findDeploymentContent(name);
            if(deploymentNames != null)
               tmp.addAll(deploymentNames);  
         }
      }
      return tmp.toArray(new String[tmp.size()]);
   }

   public int lockDeploymentContent(String vfsPath)
   {
      if( log.isTraceEnabled() )
         log.trace("lockDeploymentContent, "+vfsPath);
      int flag = setDeploymentContentFlags(vfsPath, DeploymentContentFlags.LOCKED);
      // FIXME update the lastModified for the ManagementView to check the DeploymentStatus
      updateLastModfied();
      return flag;
   }

   public int unlockDeploymentContent(String vfsPath)
   {
      if( log.isTraceEnabled() )
         log.trace("unlockDeploymentContent, "+vfsPath);
      int flag = clearDeploymentContentFlags(vfsPath, DeploymentContentFlags.LOCKED);
      // FIXME update the lastModified for the ManagementView to check the DeploymentStatus
      updateLastModfied();
      return flag;
   }

   public int getDeploymentContentFlags(String vfsPath)
   {
      Integer flags = contentFlags.get(vfsPath);
      return flags != null ? flags : 0;
   }
   public synchronized int clearDeploymentContentFlags(String vfsPath, int flags)
   {
      Integer dflags = contentFlags.get(vfsPath);
      if(dflags != null)
      {
         dflags &= ~flags;
         contentFlags.put(vfsPath, dflags);
      }
      return dflags != null ? dflags : 0;
   }
   public boolean hasDeploymentContentFlags(String vfsPath, int flag)
   {
      Integer flags = contentFlags.get(vfsPath);
      boolean hasFlag = false;
      if(flags != null )
         hasFlag = (flags & flag) != 0;
      return hasFlag;
   }
   public int setDeploymentContentFlags(String vfsPath, int flags)
   {
      contentFlags.put(vfsPath, flags);
      return flags;
   }

}
