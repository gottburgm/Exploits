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
package org.jboss.system.server.profile.repository;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.AbstractVFSProfileSource;
import org.jboss.virtual.VirtualFile;

/**
 * A immutable vfs based profile.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public class AbstractImmutableProfile extends AbstractVFSProfileSource implements Profile
{

   /** The profile key. */
   private ProfileKey key;
   
   /** The profile dependencies */
   private List<ProfileKey> subProfiles;
   
   public AbstractImmutableProfile(ProfileKey key, URI[] uris)
   {
      this(key, uris, null);
   }
   
   public AbstractImmutableProfile(ProfileKey key, URI[] uris, List<ProfileKey> subprofiles)
   {
      super(uris);
      //
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      
      this.key = key;
      this.subProfiles = subprofiles;
   }
   
   public void create() throws Exception
   {
      for(URI uri : getRepositoryURIs())
      {
         VirtualFile root = getCachedVirtualFile(uri);
         loadApplications(root);
      }
      updateLastModfied();
   }
   
   public ProfileKey getKey()
   {
      return this.key;
   }

   public Collection<ProfileKey> getSubProfiles()
   {
      return this.subProfiles;
   }
   
   public void setSubProfiles(List<ProfileKey> subProfiles)
   {
      this.subProfiles = subProfiles;
   }
   
   @Override
   public ProfileDeployment getDeployment(String vfsPath) throws NoSuchDeploymentException
   {
      if(vfsPath == null)
         throw new IllegalArgumentException("Null name");
      
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

   public boolean hasDeployment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");

      try
      {
         return super.getDeployment(name) != null;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   public boolean isMutable()
   {
      return false;
   }
   
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(getClass().getSimpleName());
      builder.append('@').append(Integer.toHexString(System.identityHashCode(this)));
      builder.append("{key = ").append(getKey());
      toString(builder);
      builder.append("}");
      return builder.toString();
   }
   
   /**
    * Additional information for toString().
    * 
    * @param builder the builder.
    */
   protected void toString(StringBuilder builder)
   {
      //
   }

}
