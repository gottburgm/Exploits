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
package org.jboss.cache.invalidation.bridges;

import org.jboss.cache.invalidation.InvalidationManagerMBean;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface for JGroups cache invalidation bridge.
 * The partition to be used and the invalidation manager can be defined as part
 * of the MBean interface.
 * The bridge automatically discovers the InvalidationGroups that are
 * managed by other nodes of the cluster and only sends invalidation information
 * for these groups over the network. This makes this bridge very easy to setup
 * while still being efficient with network resources and CPU serialization cost.
 *
 * @see JGCacheInvalidationBridge
 * @see org.jboss.cache.invalidation.InvalidationManager
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>24 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface JGCacheInvalidationBridgeMBean extends ServiceMBean
{
   /** 
    * Gets the name of the partition to be used to exchange invalidation messages 
    * and discover which caches (i.e., InvalidationGroup)are available.  This is a 
    * convenience method as the partition name is an attribute of HAPartition.
    * 
    * @return the name of the partition
    */
   String getPartitionName();
   
   /**
    * Get the underlying partition used by this service to exchange
    * invalidation messages and discover which caches (i.e., InvalidationGroup)
    * are available.
    * 
    * @return the partition
    */
   HAPartition getHAPartition();
   
   /**
    * Sets the underlying partition used by this service to exchange
    * invalidation messages and discover which caches (i.e., InvalidationGroup)
    * are available
    * 
    * @param clusterPartition the partition
    */
   void setHAPartition(HAPartition clusterPartition);
   
   /**
    * Get the invalidation bridge name.
    * 
    * @return the invalidation bridge name
    */
   public String getBridgeName();
   
   /**
    * Set the invalidation bridge name
    * 
    * @param name of the bridge
    */
   public void setBridgeName(String name);
   
   /**
    * Get the InvalidationManager
    */
   public InvalidationManagerMBean getInvalidationManager();
   
   /**
    * Set the InvalidationManager
    */
   public void setInvalidationManager(InvalidationManagerMBean manager);
   
}
