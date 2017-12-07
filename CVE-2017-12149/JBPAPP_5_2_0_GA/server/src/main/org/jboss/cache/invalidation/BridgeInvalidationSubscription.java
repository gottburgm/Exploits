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
 * Every bridge subscribing to a InvalidationManager has access to this interface that
 * it can used to invalidate messages on the local IM.
 * @see InvalidationManagerMBean
 * @see InvalidationBridgeListener
 * @version $Revision: 81030 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>21 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface BridgeInvalidationSubscription
{
   /** 
    * Used to invalidate a single key in a given InvalidationGroup
    * @param invalidationGroupName Name of the InvalidationGroup for which this invalidation is targeted
    * @param key Key to be invalidated
    */   
   public void invalidate (String invalidationGroupName, Serializable key);
   
   /**
    * Invalidate a set of keys in a give InvalidationGRoup
    * @param invalidationGroupName Name of the InvalidationGroup to which is targeted this invalidation
    * @param keys Keys to be invalidated
    */   
   public void invalidate (String invalidationGroupName, Serializable[] keys);

   /**
    * Invalidate all the entries in the specified group
    * @param groupName
    */
   public void invalidateAll(String groupName);
   
   /**
    * Invalidates a set of keys in a set of InvalidationGroup. It is the responsability
    * of the InvalidationManager to determine which IG are actually present i.e. the
    * bridge may transmit BatchInvalidation for IG that are not present locally. The
    * IM will simply ignore them.
    * @param invalidations Invalidations to be performed on the local IM instance
    */   
   public void batchInvalidate (BatchInvalidation[] invalidations);
   
   /**
    * Unregister the current bridge form the InvalidationManager
    */   
   public void unregister ();   
   
}
