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
package org.jboss.test.server.profileservice.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jboss.profileservice.spi.ProfileDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profile.repository.AbstractImmutableProfile;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class FilteredProfile extends AbstractImmutableProfile
{

   /** The profile deployments. */
   private List<String> deploymentNames;
   
   public FilteredProfile(ProfileKey key, URI[] uris, List<String> deploymentNames)
   {
      this(key, uris, null, deploymentNames);
   }
   
   public FilteredProfile(ProfileKey key, URI[] uris, List<ProfileKey> subprofiles, List<String> deploymentNames)
   {
      super(key, uris, subprofiles);
      if(deploymentNames == null)
         throw new IllegalArgumentException("Null profile deployments.");
      
      this.deploymentNames = deploymentNames;
   }
   
   @Override
   public void create() throws Exception
   {
      // Load deployment names
      for(String deploymentName : deploymentNames)
      {
         // Get the deployment content
         VirtualFile vf = resolveDeploymentName(deploymentName);
         // Load the deployment
         ProfileDeployment deployment = createDeployment(vf);
         // Add the deployment
         addDeployment(deployment.getName(), deployment);
      }
      updateLastModfied();
   }

   protected VirtualFile resolveDeploymentName(String deploymentName) throws IOException
   {
      List<VirtualFile> list = new ArrayList<VirtualFile>();
      for(URI uri : getRepositoryURIs())
      {
         VirtualFile repo = getCachedVirtualFile(uri);
         VirtualFile vf = repo.getChild(deploymentName);
         if(vf != null)
            list.add(vf);
      }
      if(list.size() == 0)
      {
         throw new FileNotFoundException("Unable to find name: " + deploymentName);
      }
      else if (list.size() > 1)
      {
         throw new FileNotFoundException("Multiple matching names: " + deploymentName + " available " + list);
      }
      return list.get(0);
   }
   
}

