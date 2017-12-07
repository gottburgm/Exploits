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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Concise description of the contents under a single root URI in a
 * ClusteredDeploymentRepository. A repository may consist of multiple root
 * URIs.
 * 
 * @author Brian Stansberry
 */
@XmlType(name="repositoryRootType", propOrder={"content", "name"})
public class RepositoryRootMetadata  
   extends AbstractSortedMetadataContainer<List<String>, RepositoryItemMetadata>
   implements Identifiable<String>, Serializable, Comparable<RepositoryRootMetadata>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -4102001386900647551L;

   private String name;
   
   /**
    * Constructor for XML parser.
    */
   public RepositoryRootMetadata()
   {      
   }
   
   /**
    * Create a new RepositoryRootMetadata with the given name.
    * 
    * @param name the name. Cannot be <code>null</code>
    * 
    * @throws IllegalArgumentException if name is <code>null</code>
    */
   public RepositoryRootMetadata(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("Null name");
      }
      setName(name);
   }
   
   /**
    * Copy constructor.
    * 
    * @param toCopy the item to copy
    * 
    * @throws NullPointerException if <code>toCopy</code> is <code>null</code>
    */
   public RepositoryRootMetadata(RepositoryRootMetadata toCopy)
   {
      this(toCopy.getName());
      Collection<RepositoryItemMetadata> content = toCopy.getContent();
      Collection<RepositoryItemMetadata> internal = getExposedCollection();
      for(RepositoryItemMetadata item : content)
      {
         internal.add(new RepositoryItemMetadata(item));
      }
   }
   
   public String getId()
   {
      return name;
   }
   
   @XmlAttribute(name = "name")
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   @XmlElement(name = "content")
   public Collection<RepositoryItemMetadata> getContent()
   {
      return getExposedCollection();
   }

   public void setContent(Collection<RepositoryItemMetadata> content)
   {
      Collection<RepositoryItemMetadata> internal = getExposedCollection();
      internal.clear();
      if (content != null)
      {
         for (RepositoryItemMetadata md : content)
         {
            internal.add(md);
         }
      }
   }
   
   public RepositoryItemMetadata getItemMetadata(List<String> path)
   {
      return getContainedMetadata(path);
   }
   
   /**
    * Adds the given item to this root's content.
    * 
    * @param md the item
    *         
    * @deprecated use {@link #getContent()} and {@link Collection#add(Object)}
    */
   @Deprecated
   public void addItemMetadata(RepositoryItemMetadata md)
   {
      getExposedCollection().add(md);
   }

   /**
    * Removes the item with the given path from this root's content.
    * 
    * @param path the path
    * @return <code>true</code> if the item was removed, <code>false</code> if
    *         no such item existed
    *         
    * @deprecated use {@link #getContent()} and {@link Collection#remove(Object)}
    */
   @Deprecated
   public boolean removeItemMetadata(List<String> path)
   {   
      RepositoryItemMetadata md = getItemMetadata(path);
      if (md != null)
      {
         return getExposedCollection().remove(md);
      }
      return false;
   }
   
   // -------------------------------------------------------------- Comparable
   
   public int compareTo(RepositoryRootMetadata other)
   {
      int result = 0;
      // Null name comes later
      if (this.name == null)
      {
         if (other.name != null)
         {
            result = 1;
         }
      }
      else if (other.name == null)
      {
         result = -1;
      }
      else
      {
         result = this.name.compareTo(other.name);
      }
      
      if (result == 0)
      {
         List<RepositoryItemMetadata> us = new ArrayList<RepositoryItemMetadata>(this.getExposedCollection());
         List<RepositoryItemMetadata> them = new ArrayList<RepositoryItemMetadata>(other.getExposedCollection());
         
         result = them.size() - us.size();
         if (result == 0)
         {
            for (int i = 0; i < us.size(); i++)
            {
               result = us.get(i).compareTo(them.get(i));
               if (result != 0)
               {
                  break;
               }
            }
         }
      }
      return result;
      
   }
   
   // -------------------------------------------------------------- Overrides 

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      
      if (obj instanceof RepositoryRootMetadata)
      {
         RepositoryRootMetadata other = (RepositoryRootMetadata) obj;
         return (this.getExposedCollection().equals(other.getExposedCollection()) 
                  && (this.name != null && this.name.equals(other.name)));
      }
      
      return false;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + (name == null ? 0 : name.hashCode());
      result = 31 * result + getExposedCollection().hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      return new StringBuilder(getClass().getName())
      .append("[name='")
      .append(name)
      .append(']').toString();
   }

}
