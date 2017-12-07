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

import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * A empty profile, which does not contain any deployments. This profile
 * basically just has a key and dependencies on other profiles.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class NoopProfile implements Profile
{
   
   private final ProfileKey key;
   private final long lastModified;
   private List<ProfileKey> subProfiles;
   
   public NoopProfile(ProfileKey key)
   {
      this(key, null);
   }
   
   public NoopProfile(ProfileKey key, List<ProfileKey> subProfiles)
   {
      this.key = key;
      this.subProfiles = subProfiles;
      this.lastModified = System.currentTimeMillis();
   }

   public ProfileKey getKey()
   {
      return this.key;
   }

   public long getLastModified()
   {
      return this.lastModified;
   }

   public Collection<ProfileKey> getSubProfiles()
   {
      return this.subProfiles;
   }
   
   public void setSubProfiles(List<ProfileKey> subProfiles)
   {
      this.subProfiles = subProfiles;
   }
   
   public Set<String> getDeploymentNames()
   {
      return Collections.emptySet();
   }

   public Collection<ProfileDeployment> getDeployments()
   {
      return Collections.emptySet();
   }
   
   public ProfileDeployment getDeployment(String name) throws NoSuchDeploymentException
   {
      throw new NoSuchDeploymentException("This profiles does not contain any deployments.");
   }

   public boolean hasDeployment(String name)
   {
      return false;
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
