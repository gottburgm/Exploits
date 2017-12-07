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

package org.jboss.system.server.profileservice.repository.clustered.metadata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Concise description of the contents of a ClusteredDeploymentRepository.
 * 
 * @author Brian Stansberry
 */
@XmlRootElement(name="repository-content", namespace= "")
@JBossXmlSchema(ignoreUnresolvedFieldOrClass=false,
      namespace="",
      elementFormDefault=XmlNsForm.UNSET,
      normalizeSpace=true)
@XmlType(name="repositoryContentType", propOrder={"repositories", "name", "server", "domain"})
@XmlAccessorType(XmlAccessType.NONE)
public class RepositoryContentMetadata 
   extends AbstractSortedMetadataContainer<String, RepositoryRootMetadata>
   implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -557008659849613674L;
   
   private String domain;
   private String server;
   private String name;
   
   /**
    * Default constructor.
    */
   public RepositoryContentMetadata()
   {
      
   }
   
   public RepositoryContentMetadata(ProfileKey key)
   {
      this();
      
      if (key == null)
      {
         throw new IllegalArgumentException("Null key");
      }
      setDomain(key.getDomain());
      setServer(key.getServer());
      setName(key.getName());
   }
   
   /**
    * Copy constructor.
    * 
    * @param toCopy the item to copy
    * 
    * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
    */
   public RepositoryContentMetadata(RepositoryContentMetadata toCopy)
   {
      this();
      
      if (toCopy == null)
      {
         throw new IllegalArgumentException("Null toCopy");
      }
      
      setDomain(toCopy.getDomain());
      setServer(toCopy.getServer());
      setName(toCopy.getName());
      
      Collection<RepositoryRootMetadata> exposed = getExposedCollection();
      for (RepositoryRootMetadata rmd : toCopy.getRepositories())
      {
         exposed.add(new RepositoryRootMetadata(rmd));
      }
   }
   
   @XmlElement(name = "repository-root", type = RepositoryRootMetadata.class, required=true)   
   public Collection<RepositoryRootMetadata> getRepositories()
   {
      return getExposedCollection();
   }

   public void setRepositories(Collection<RepositoryRootMetadata> repositories)
   {
      Collection<RepositoryRootMetadata> internal = getExposedCollection();
      internal.clear();
      
      if (repositories != null)
      {
         internal.addAll(repositories);
      }
   }

   public RepositoryRootMetadata getRepositoryRootMetadata(String repositoryRoot)
   {
      return getContainedMetadata(repositoryRoot);
   }
   
   public Set<String> getRootNames()
   {
      return getContainedMetadataIds();
   }

   @XmlAttribute(name = "name", required=true)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {      
      if (name == null)
      {
         throw new IllegalArgumentException("Null name");
      }
      this.name = name;
   }
   
   @XmlAttribute(name = "server", required=true)
   public String getServer()
   {
      return server;
   }

   public void setServer(String server)
   {      
      if (server == null)
      {
         throw new IllegalArgumentException("Null server");
      }
      this.server = server;
   }

   @XmlAttribute(name = "domain", required=true)
   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {      
      if (domain == null)
      {
         throw new IllegalArgumentException("Null domain");
      }
      this.domain = domain;
   }

   // --------------------------------------------------------------  Overrides

   @Override
   public boolean equals(Object obj)
   {  
      if (this == obj)
      {
         return true;
      }
      
      if (obj instanceof RepositoryContentMetadata)
      {
         RepositoryContentMetadata other = (RepositoryContentMetadata) obj;
         return (getExposedCollection().equals(other.getExposedCollection())
                 && safeEquals(this.name, other.name)
                 && safeEquals(this.server, other.server)
                 && safeEquals(this.domain, other.domain));
      }
      
      return false;
   }

   @Override
   public int hashCode()
   {

      int result = 17;
      result = 31 * result + (name == null ? 0 : name.hashCode());
      result = 31 * result + (server == null ? 0 : server.hashCode());
      result = 31 * result + (domain == null ? 0 : domain.hashCode());
      result = 31 * result + getExposedCollection().hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(getClass().getName())
            .append("[domain=").append(domain)
            .append(",server=").append(server)
            .append(",name=").append(name)
            .append(",roots={");
      boolean first = true;
      for (String root : getRootNames())
      {
         if (!first)
         {
            sb.append(',');
         }
         else
         {
            first = false;
         }
         sb.append(root);
      }
      sb.append("}]");
      return sb.toString();
   }
   
   private static boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }
   
}
