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

package org.jboss.ha.framework.server.deployers;

import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.util.StringPropertyReplacer;

/**
 * Default impl of {@link HAPartitionDependencyCreator}.  Prepends a configurable
 * prefix to the system-property-replaced partition name.
 * 
 * @author Brian Stansberry
 */
public class DefaultHAPartitionDependencyCreator implements HAPartitionDependencyCreator
{
   private static final Logger log = Logger.getLogger(DefaultHAPartitionDependencyCreator.class);
   
   public static final String DEFAULT_HA_PARTITION_DEPENDENCY_PREFIX = "partition:partitionName=";
   
   /** Shared instance, although this isn't a singleton */
   public static final DefaultHAPartitionDependencyCreator INSTANCE = new DefaultHAPartitionDependencyCreator();
   
   private final String prefix;

   // ----------------------------------------------------------- Constructors
   
   /**
    * Create a new DefaultHAPartitionDependencyCreator using the
    * {@link #DEFAULT_HA_PARTITION_DEPENDENCY_PREFIX default prefix}.
    */
   public DefaultHAPartitionDependencyCreator()
   {
      this(DEFAULT_HA_PARTITION_DEPENDENCY_PREFIX);
   }
   
   /**
    * Create a new DefaultHAPartitionDependencyCreator.
    * 
    * @param prefix the string that should be prepended to 
    * {@link ClusterConfigMetaData#getPartitionName() the bean metadata's partition name}
    * to determine the name of the dependency.
    */
   public DefaultHAPartitionDependencyCreator(String prefix)
   {
      if (prefix == null)
         throw new IllegalArgumentException("prefix cannot be null");
      
      this.prefix = prefix;
   }

   // -------------------------------------------  HAPartitionDependencyCreator

   public String getHAPartitionDependencyName(String partitionName)
   {
      return getHaPartitionDependencyPrefix() + getPropertyReplacedPartitionName(partitionName);
   }

   // -------------------------------------------------------------  Properties
   
   /**
    * Gets the string that should be prepended to 
    * {@link ClusterConfigMetaData#getPartitionName() the bean metadata's partition name}
    * to determine the name of the dependency.
    * <p>
    * <code>ClusterPartition</code> will also use the property to determine
    * the name of a microcontainer alias to itself, which it will register
    * in order to satisfy the dependency.
    * </p>
    * 
    * @return the prefix, or {@link #DEFAULT_HA_PARTITION_DEPENDENCY_PREFIX} if
    *         not configured.
    */
   public String getHaPartitionDependencyPrefix()
   {
      return prefix == null ? DEFAULT_HA_PARTITION_DEPENDENCY_PREFIX : prefix;
   }


   private static String getPropertyReplacedPartitionName(String partitionName)
   {
      String value = partitionName;
      try
      {
         String replacedValue = StringPropertyReplacer.replaceProperties(value);
         if (value != replacedValue)
         {            
            log.debug("Replacing " + ClusterConfigMetaData.class.getSimpleName() + 
                  " partitionName property " + value + " with " + replacedValue);
            value = replacedValue;
         }
      }
      catch (Exception e)
      {
         log.warn("Unable to replace partition name " + value, e);         
      }
      
      return value;
   }
}
