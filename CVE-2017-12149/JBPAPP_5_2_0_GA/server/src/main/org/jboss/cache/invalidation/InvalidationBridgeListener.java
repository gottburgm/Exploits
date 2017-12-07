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
package org.jboss.cache.invalidation;

import java.io.Serializable;

/**
 * InvalidationManager (IM) represents locally managed caches and invaliders. To be able
 * to do distributed invalidations, it is necessary to bridge these IM to forward
 * cache invalidation messages.
 * The InvalidationBridgeListener provides the way for any transport mechanism to
 * be used to forward cache invalidation messages accross a network/cluster.
 * @see InvalidationManagerMBean
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81030 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>24 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface InvalidationBridgeListener
{
   /**
    * Called when a set of invalidations, concerning more than one IG, should be forwarded
    * accross the bridge.
    * It is the bridge responsability to determine:
    * - which IG must be bridged (some IG may not exist on other nodes, in this case
    *  the bridge may decide to drop these invalidations messages to reduce the
    *  serialization cost and network usage)
    * - to which other nodes the invalidations must be communicated. This can be done
    *  by any mean (automatic discovery, configuration file, etc.)
    * @param invalidations BatchInvalidation messages containing invalidations
    * @param asynchronous Determine the best-effort indication to be used to communicate invalidations
    */   
   public void batchInvalidate (BatchInvalidation[] invalidations, boolean asynchronous);
   
   /**
    * Called when a single invalidation, concerning a single IG, should be forwarded
    * accross the bridge.
    * It is the bridge responsability to determine:
    * - which IG must be bridged (some IG may not exist on other nodes, in this case
    *  the bridge may decide to drop these invalidations messages to reduce the
    *  serialization cost and network usage)
    * - to which other nodes the invalidations must be communicated. This can be done
    *  by any mean (automatic discovery, configuration file, etc.)
    * @param invalidationGroupName InvalidationGroup name
    * @param key Key to be invalidated
    * @param asynchronous Best effort communication setting
    */   
   public void invalidate (String invalidationGroupName, Serializable key, boolean asynchronous);
   
   /** Called when a set of invalidations, concerning a single IG, should be forwarded
    * accross the bridge.
    * It is the bridge responsability to determine:
    * - which IG must be bridged (some IG may not exist on other nodes, in this case
    *  the bridge may decide to drop these invalidations messages to reduce the
    *  serialization cost and network usage)
    * - to which other nodes the invalidations must be communicated. This can be done
    *  by any mean (automatic discovery, configuration file, etc.)
    * @param invalidationGroupName Name of the InvalidationGroup to which is linked the invalidation message
    * @param keys Keys to be invalidated
    * @param asynchronous Best effort communication setting
    */   
   public void invalidate (String invalidationGroupName, Serializable[] keys, boolean asynchronous);

   /**
    * Issues invalidate all event to other nodes.
    * @param groupName  group's name
    * @param asynchronous  mode
    */
   public void invalidateAll(String groupName, boolean asynchronous);
   
   /**
    * Called when an InvocationGroup is dropped (because no cache and invalider are
    * using it anymore).
    * For bridge implementations that automatically discover which IG should be
    * bridged, this callback can be used to communicate to the other nodes that this
    * node is no more interested in invalidation for this group.
    * @param groupInvalidationName Name of the InvalidationGroup being dropped
    */   
   public void groupIsDropped (String groupInvalidationName);
   
   /**
    * Called when an InvocationGroup is created.
    * For bridge implementations that automatically discover which IG should be
    * bridged, this callback can be used to communicate to the other nodes that this
    * node is now interested in invalidation for this group.
    * @param groupInvalidationName Name of the InvalidationGroup just being created
    */   
   public void newGroupCreated (String groupInvalidationName);
   
}
