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

package org.jboss.ha.timestamp;

import org.jboss.ha.framework.interfaces.ClusterNode;

/**
 * An object that observes changes in a {@link TimestampDiscrepancyService}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface TimestampDiscrepancyObserver
{
   /**
    * Notification from {@link TimestampDiscrepancyService} when it has
    * changed the {@link TimestampDiscrepancy} associated with a particular
    * node.
    * 
    * @param node the node
    * @param discrepancy the new discrepancy
    */
   void timestampDiscrepancyChanged(ClusterNode node, TimestampDiscrepancy discrepancy);
   
   /**
    * Callback allowing the observer to veto the removal by the
    * {@link TimestampDiscrepancyService} of discrepancy data for a
    * node that is no longer active in the cluster. Allows the observer to
    * request that data for historically relevant nodes be retained.
    * 
    * @param dead the node
    * @param lastChecked the time (in ms since the epoch) the caller was
    *                    last able to obtain timestamp information from the
    *                    caller
    *                    
    * @return <code>true</code> if the data can be removed, <code>false</code>
    *         if it must be retained.
    */
   boolean canRemoveDeadEntry(ClusterNode dead, long lastChecked);
}
