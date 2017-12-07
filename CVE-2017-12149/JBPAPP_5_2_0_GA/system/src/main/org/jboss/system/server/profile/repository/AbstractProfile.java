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
package org.jboss.system.server.profile.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.profileservice.spi.DeploymentRepository;
import org.jboss.profileservice.spi.ModificationInfo;
import org.jboss.profileservice.spi.MutableProfile;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * A basic profile implementation, which delegates all the deployment
 * actions the a DeploymentRepository.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public class AbstractProfile implements MutableProfile
{

   /** The profile key */
   final private ProfileKey key;
   
   /** The repository containing the deployment contents */
   final private DeploymentRepository repository;
   
   /** The profile dependencies */
   private List<ProfileKey> subProfiles;
   
   /** Is hot deployment checking enabled */
   private volatile boolean hotdeployEnabled = false;
   
   public AbstractProfile(DeploymentRepository repository, ProfileKey key)
   {
      this(repository, key, null);
   }
   
   public AbstractProfile(DeploymentRepository repository, ProfileKey key, List<ProfileKey> subProfiles)
   {
      if(key == null)
         throw new IllegalArgumentException("Null profile key.");
      if(repository == null)
         throw new IllegalArgumentException("Null deployment repository.");
            
      this.key = key;
      this.repository = repository;
      this.subProfiles = subProfiles;
   }

   public void addDeployment(ProfileDeployment d) throws Exception
   {
      if(d == null)
         throw new IllegalArgumentException("Null deployment");
      this.repository.addDeployment(d.getName(), d);
   }

   public void enableModifiedDeploymentChecks(boolean flag)
   {
      this.hotdeployEnabled = flag;
   }
   
   public ProfileDeployment getDeployment(String name) throws NoSuchDeploymentException
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      return this.repository.getDeployment(name);
   }

   public Set<String> getDeploymentNames()
   {
      return this.repository.getDeploymentNames();
   }

   public Collection<ProfileDeployment> getDeployments()
   {
      return this.repository.getDeployments();
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

   public long getLastModified()
   {
      return this.repository.getLastModified();
   }

   public Collection<ModificationInfo> getModifiedDeployments() throws Exception
   {
      if(this.hotdeployEnabled == false)
         return Collections.emptySet();
      return this.repository.getModifiedDeployments();
   }

   public boolean hasDeployment(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");

      try
      {
         return this.repository.getDeployment(name) != null;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   public ProfileDeployment removeDeployment(String name) throws Exception
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      return this.repository.removeDeployment(name);
   }
   
   public void create() throws Exception
   {
      if(this.repository == null)
         throw new IllegalStateException("Null deployment repository.");
      // Load
      this.repository.load();
   }
   
   public void destroy()
   {
      this.repository.unload();
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

   public boolean isMutable()
   {
      return true;
   }
}

