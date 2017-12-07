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

package org.jboss.system.server.profileservice.repository.clustered.sync;

import java.io.Serializable;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;

/**
 * Describes a modification that a node needs to make to synchronize
 * its repository with the cluster.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision:$
 */
public class ContentModification implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -9060367262266987206L;

   public enum Type
   {
      /** 
       * This node needs to pull the item from the cluster and store it locally.
       */
      PULL_FROM_CLUSTER,
      /** This node it needs to remove this item locally. */
      REMOVE_FROM_CLUSTER,
      /** This node needs to push the item to the cluster. */
      PUSH_TO_CLUSTER,
      /** This node needs to push the item to the cluster. */
      PUSH_INPUT_STREAM_TO_CLUSTER,
      /** This node needs to tell the cluster to remove the item. */
      REMOVE_TO_CLUSTER,
      /** This node needs to make a new directory */
      MKDIR_FROM_CLUSTER,
      /** This node needs to tell the cluster to make a new directory */
      MKDIR_TO_CLUSTER,
      /** 
       * This node needs to tell the cluster to prepare to remove a directory.
       * The actual removal will come later, via a REMOVE_TO_CLUSTER, after
       * all directory children are removed as well. The "prepare" modification
       * allows the remote nodes to set up a {@link RepositorySynchronizationAction}
       * to rollback the directory removal. 
       */
      PREPARE_RMDIR_TO_CLUSTER,
      /** 
       * This node needs to prepare to remove a directory.
       * The actual removal will come later, via a REMOVE_FROM_CLUSTER, after
       * all directory children are removed as well. The "prepare" modification
       * allows the node to set up a {@link RepositorySynchronizationAction}
       * to rollback the directory removal. 
       */
      PREPARE_RMDIR_FROM_CLUSTER,
      /** 
       * This node's directory with the same path as the related
       * RepositoryItemMetadata has a different timestamp. What if anything
       * the node should do about this is unspecified. The 
       * RepositoryItemMetadata included in this RepositoryContentModification
       * will be the cluster version, as this node should know its own version.
       */
      DIR_TIMESTAMP_MISMATCH,
      /** 
       * This node needs to add some metadata for an item that has been removed.
       * Used when this node doesn't have the item either, but is lacking
       * information on when it was removed.   The
       * RepositoryItemMetadata included in this RepositoryContentModification
       * will return <code>true</code> to {@link RepositoryItemMetadata#isRemoved()}.
       */
      REMOVAL_METADATA_FROM_CLUSTER
   }
   
   private final Type type;
   private final String rootName;
   private final RepositoryItemMetadata item;
   
   public ContentModification(Type type, String rootName, RepositoryItemMetadata item)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("Null type");
      }
      if (rootName == null)
      {
         throw new IllegalArgumentException("Null rootName");
      }
      if (item == null)
      {
         throw new IllegalArgumentException("Null item");
      }
      
      this.type = type;
      this.rootName = rootName;
      this.item = item;
   }

   public Type getType()
   {
      return type;
   }

   public String getRootName()
   {
      return rootName;
   }

   public RepositoryItemMetadata getItem()
   {
      return item;
   }
   
}
