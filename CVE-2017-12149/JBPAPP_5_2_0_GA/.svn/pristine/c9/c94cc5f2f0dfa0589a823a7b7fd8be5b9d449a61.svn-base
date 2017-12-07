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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of an individual item (i.e. file) in a clustered repository.
 * 
 * @author Brian Stansberry
 */
@XmlType(name="repositoryItemType", propOrder={"timestampAsString", "originatingNode", "removed", "directory", "relativePath"})
public class RepositoryItemMetadata 
   implements Identifiable<List<String>>, Serializable, Comparable<RepositoryItemMetadata>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7712110893517082031L;
   
   private static final DateFormat dateFormat;
   
   static
   {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");      
   }
   
   /** 
    * Marker value to pass to {@link #setTimestampAsString(String)} that will
    * generate a timestamp equal to System.currentTimeMillis(). Useful for
    * testing. 
    */
   public static String NOW = "NOW";
   
   private volatile String relativePath;
   private volatile long timestamp;
   private volatile boolean directory;
   /** The parsed elements of relativePath. Lazy initialized */
   private volatile transient List<String> pathElements;
   
   private volatile String originatingNode;
   private volatile boolean removed;
   private String rootName;
   

   public static List<String> getPathElements(String path)
   {
      String[] elements = path.split("/");
      return Collections.unmodifiableList(Arrays.asList(elements));
   }
   
   /**
    * Constructor for XML parser.
    */
   public RepositoryItemMetadata()
   {
      
   }
   
   public RepositoryItemMetadata(List<String> pathElements, long timestamp, String originatingNode, boolean directory, boolean removed)
   {
      setDirectory(directory);
      setRelativePathElements(pathElements);
      setTimestamp(timestamp);
      setOriginatingNode(originatingNode);
      setRemoved(removed);
   }
   
   /**
    * Copy constructor. Performs a deep copy of the path element list.
    * 
    * @param toCopy the item to copy
    * 
    * @throws NullPointerException if <code>toCopy</code> is <code>null</code>
    */
   public RepositoryItemMetadata(RepositoryItemMetadata toCopy)
   {
      this(toCopy.getRelativePathElements(), toCopy.getTimestamp(), 
            toCopy.getOriginatingNode(), toCopy.isDirectory(), toCopy.isRemoved());
   }
   
   public List<String> getId()
   {
      return getRelativePathElements();
   }
   
   @XmlTransient
   public String getRootName()
   {
      return rootName;
   }

   public void setRootName(String rootName)
   {
      this.rootName = rootName;
   }
   
   @XmlAttribute(name = "relative-path", required=true)
   public String getRelativePath()
   {
      return relativePath;
   }
   
   public void setRelativePath(String path)
   {
      if (path != null && path.length() > 0 && '/' == path.charAt(0))
      {
         path = path.length() == 0 ? "" : path.substring(1);
      }
      this.relativePath = path;
      this.pathElements = null;
   }
   
   @XmlAttribute(name = "directory")
   public boolean isDirectory()
   {
      return directory;
   }

   public void setDirectory(boolean directory)
   {
      this.directory = directory;
      if (relativePath != null)
      {
         if (!directory && relativePath.endsWith("/"))
         {
            relativePath = relativePath.substring(0, relativePath.length() - 1);
         }
         else if (directory && relativePath.endsWith("/") == false)
         {
            relativePath += "/";
         }
      }
   }

   @XmlTransient
   public long getTimestamp()
   {
      return timestamp;
   }
   
   public void setTimestamp(long timestamp)
   {
      this.timestamp = timestamp;
   }
   
   @XmlAttribute(name = "timestamp", required=true)
   public String getTimestampAsString()
   {
      Date d = new Date(timestamp);
      synchronized (dateFormat)
      {
         return dateFormat.format(d);
      }
   }
   
   public void setTimestampAsString(String timestamp)
   {
      if (NOW.equals(timestamp))
      {
         setTimestamp(System.currentTimeMillis());
      }
      else
      {
         try
         {
            synchronized (dateFormat)
            {
               Date d = dateFormat.parse(timestamp);
               setTimestamp(d.getTime());
            }
         }
         catch (ParseException e)
         {
            throw new RuntimeException("Failed to parse " + timestamp, e);
         }
      }
   }
   
   @XmlTransient
   public List<String> getRelativePathElements()
   {
      if (pathElements == null && relativePath != null)
      {
         String[] elements = relativePath.split("/");
         setRelativePathElements(Arrays.asList(elements));
      }
      return pathElements;
   }
   
   public void setRelativePathElements(List<String> pathElements)
   {
      if (pathElements == null)
      {
         this.pathElements = null;
         this.relativePath = null;
      }
      else
      {
         this.pathElements = Collections.unmodifiableList(new ArrayList<String>(pathElements));
         boolean first = true;
         StringBuilder sb = new StringBuilder();
         for (String element : pathElements)
         {
            if (!first)
            {
               sb.append('/');
            }
            else
            {
               first = false;
            }
            sb.append(element);
         }
         if (directory)
         {
            sb.append('/');
         }
         this.relativePath = sb.toString();
      }
   }  
   
   /**
    * The name of the cluster node that propagated this version of
    * the item to the cluster.
    * 
    * @return
    */
   @XmlAttribute(name = "originator", required=true)
   public String getOriginatingNode()
   {
      return originatingNode;
   }

   public void setOriginatingNode(String originatingNode)
   {
      this.originatingNode = originatingNode;
   }

   @XmlAttribute(name = "removed")
   public boolean isRemoved()
   {
      return removed;
   }

   public void setRemoved(boolean removed)
   {
      this.removed = removed;
   }

   /**
    * Gets whether this item is a child of another item.
    * 
    * @param other the other item. Can be <code>null</code> in which case
    *              this method will return <code>false</code>
    *              
    * @return <code>true</code> if other is not <code>null</code>, is a
    *         {@link #isDirectory() directory} and this items path starts
    *         with <code>other</code>'s path.
    */
   public boolean isChildOf(RepositoryItemMetadata other)
   {
      return other != null && other.isDirectory() && getRelativePath().startsWith(other.getRelativePath());
   }
   
   // -------------------------------------------------------------- Comparable

   public int compareTo(RepositoryItemMetadata o)
   {
      int result = 0;
      if (this != o)
      {
         if (this.relativePath != o.relativePath)
         {
            if (this.relativePath != null)
            {
               result = o.relativePath == null ? 1 : this.relativePath.compareTo(o.relativePath);
            }
            else
            {
               result = o.relativePath == null ? 0 : -1;
            }
         }
         
         if (result == 0)
         {
            result = (int) (this.timestamp - o.timestamp);
         }
         
         if (result == 0 && this.directory != o.directory)
         {
            result = this.directory ? 1 : -1;
         }
         
         if (result == 0 && this.removed != o.removed)
         {
            result = this.removed ? 1 : -1;
         }
         
         if (result == 0)
         {
            if (this.originatingNode != null)
            {
               result = o.originatingNode == null ? -1 : this.originatingNode.compareTo(o.originatingNode);
            }
            else
            {
               result = o.originatingNode == null ? 0 : 1;
            }
         }
      }
      return result;
   }
   
   // -------------------------------------------------------------- Overrides 

   @Override
   public boolean equals(Object obj)
   {
      boolean result = (this == obj);
      
      if (!result && obj instanceof RepositoryItemMetadata)
      {
         RepositoryItemMetadata other = (RepositoryItemMetadata) obj;
         result = (this.timestamp == other.timestamp) 
                    && this.removed == other.removed 
                    && this.directory == other.directory
                    && safeEquals(this.getRelativePathElements(), other.getRelativePathElements()) 
                    && safeEquals(this.originatingNode, other.originatingNode);
      }
      return result;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + ((int) (timestamp ^ (timestamp >>>32)));
      result = 31 * result + (removed ? 0 : 1);
      result = 31 * result + (directory ? 0 : 1);
      List<String> elements = getRelativePathElements();
      result = 31 * result + (elements == null ? 0 : elements.hashCode());
      result = 31 * result + (originatingNode == null ? 0 : originatingNode.hashCode());
      return result;
   }

   @Override
   public String toString()
   {
      return new StringBuilder(getClass().getName())
                    .append("[path='")
                    .append(relativePath)
                    .append(",timestamp=")
                    .append(timestamp)
                    .append(",originatingNode=")
                    .append(originatingNode)
                    .append(",removed=")
                    .append(removed)
                    .append(']').toString();
   }
   
   // -------------------------------------------------------------- Private  

   private static boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }
   
   
}
