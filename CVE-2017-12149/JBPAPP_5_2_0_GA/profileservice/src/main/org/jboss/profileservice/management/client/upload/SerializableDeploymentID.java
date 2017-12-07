/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management.client.upload;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.deployers.spi.management.deploy.DeploymentID;
import org.jboss.profileservice.spi.DeploymentOption;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * A serializable DeploymentID implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 91130 $
 */
public class SerializableDeploymentID implements DeploymentID, Serializable
{
   private static final long serialVersionUID = 1;

   /** An InputStream to use to copy the contents */
   private transient InputStream contentIS;
   private Set<DeploymentOption> options;
   private String[] deploymentNames;
   private String[] repositoryNames;
   private ProfileKey profileKey;
   private String description;
   private URL contentURL;
   private boolean copyContent;

   public SerializableDeploymentID(DeploymentID deployment)
   {
      this(deployment.getNames(), deployment.getProfile(), deployment.getDescription(), deployment.getDeploymentOptions());         
   }
   
   public SerializableDeploymentID(String name, ProfileKey profileKey, String description)
   {
      this(new String[]{name}, profileKey, description);
   }
   
   public SerializableDeploymentID(String[] names, ProfileKey profileKey, String description)
   {
      this(names, profileKey, description, new DeploymentOption[0]);
   }
   
   public SerializableDeploymentID(String[] names, ProfileKey profileKey, String description, DeploymentOption... options)
   {
      this.deploymentNames = names;
      this.profileKey = profileKey;
      this.description = description;
      this.copyContent = true; // by default we copy content
      this.options = new HashSet<DeploymentOption>();
      if(options != null && options.length > 0)
      {
         for(DeploymentOption option : options)
            addDeploymentOption(option);
      }  
   }

   public String[] getNames()
   {
      return deploymentNames;
   }

   public String[] getRepositoryNames()
   {
      if(repositoryNames == null)
         repositoryNames = deploymentNames;
      return repositoryNames;
   }
   public void setRepositoryNames(String[] names)
   {
      this.repositoryNames = names;
   }

   /**
    * The target profile for the deployment.
    * For further use.
    */
   public ProfileKey getProfile()
   {
      return this.profileKey;
   }
   
   public String getDescription()
   {
      return description;
   }

   public URL getContentURL()
   {
      return contentURL;
   }
   public void setContentURL(URL contentURL)
   {
      this.contentURL = contentURL;
   }

   public boolean isCopyContent()
   {
      return copyContent;
   }
   public void setCopyContent(boolean copyContent)
   {
      this.copyContent = copyContent;
   }

   /**
    * An optional deployment archive content stream for the top-level
    * deployment.
    * 
    * @return the archive input stream if it exists
    */
   public InputStream getContentIS()
   {
      return contentIS;
   }
   /**
    * 
    * @param contentIS
    */
   public void setContentIS(InputStream contentIS)
   {
      this.contentIS = contentIS;
   }

   public void addDeploymentOption(DeploymentOption option)
   {
      if(option == null)
         throw new IllegalArgumentException("null option");
      this.options.add(option);
   }

   public DeploymentOption[] getDeploymentOptions()
   {
      return this.options.toArray(new DeploymentOption[this.options.size()]);
   }

   public boolean hasDeploymentOption(DeploymentOption option)
   {
      if(option == null)
         throw new IllegalArgumentException("null option");
      return this.options.contains(option);
   }

   public boolean removeDeploymentOption(DeploymentOption option)
   {
      return this.options.remove(option);
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("names=").append(Arrays.toString(getNames()));
      // Only add the repositoryNames if it differs from deploymentNames
      if(getRepositoryNames() != getNames())
         buffer.append(", repositoryNames=").append(Arrays.toString(getRepositoryNames()));
      // Only log copyContent when the contentURL is set
      if(getContentURL() != null)
         buffer.append(", copyContent=").append(copyContent);
      if(description != null)
         buffer.append(", description=").append(description);
      if(options != null && options.isEmpty() == false)
         buffer.append(", options=").append(options);
      return buffer.toString();
   }

}
