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

package org.jboss.profileservice.cluster.repository;

import org.jboss.ha.timestamp.TimestampDiscrepancy;
import org.jboss.ha.timestamp.TimestampDiscrepancyService;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.sync.AbstractSynchronizationPolicy;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationPolicy;

/**
 * Default implementation of {@link SynchronizationPolicy}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 *
 */
public class DefaultSynchronizationPolicy extends AbstractSynchronizationPolicy
{
   private TimestampDiscrepancyService timestampService; 
   
   public TimestampDiscrepancyService getTimestampService()
   {
      return timestampService;
   }

   public void setTimestampService(TimestampDiscrepancyService timestampService)
   {
      this.timestampService = timestampService;
   }
   
   
   // ----------------------------------------------------  Protected Overrides 

   /**
    * Always accepts merge additions; accepts join additions if the 
    * time-discrepancy adjusted timestamp of <code>toAdd</code> is later than
    * the current time minus {@link #getRemovalTrackingTime() the maximum
    * period we track item removals}. The idea of the latter being that if
    * a previous version of <code>toAdd</code> was removed more recently than 
    * that then we should have a record of its removal.
    */
   protected boolean acceptAddition(RepositoryItemMetadata toAdd, RepositoryItemMetadata joinersPrevious,
         boolean merge)
   {
      if (merge)
      {
         return true;
      }
      else
      {
         TimestampDiscrepancy addDiscrepancy = getTimestampDiscrepancy(toAdd.getOriginatingNode(), false);
         long adjustedTimestamp = addDiscrepancy.getMinLocalTimestamp(toAdd.getTimestamp());
         return adjustedTimestamp > System.currentTimeMillis() - getRemovalTrackingTime();
      }
   }

   /**
    * Accepts the reincarnation if the time-discrepancy adjusted timestamp of
    * <code>reincarnation</code> is at least as recent as the time-discrepancy 
    * adjusted timestamp of the <code>current</code> item.
    */
   protected boolean acceptReincarnation(RepositoryItemMetadata reincarnation, RepositoryItemMetadata current,
         boolean merge)
   {
      TimestampDiscrepancy addDiscrepancy = getTimestampDiscrepancy(reincarnation.getOriginatingNode(), false); 
      TimestampDiscrepancy deadDiscrepancy = getTimestampDiscrepancy(current.getOriginatingNode(), false); 
      return isChangeMoreRecent(reincarnation, current, addDiscrepancy, deadDiscrepancy, false);
   }

   /**
    * Rejects removal if <code>sendersView</code> is <code>null</code>, else
    * accepts the removal if the time-discrepancy adjusted timestamp of
    * <code>sendersView</code> is at least as recent as the time-discrepancy 
    * adjusted timestamp of the <code>current</code> item.
    */
   protected boolean acceptRemoval(RepositoryItemMetadata current, RepositoryItemMetadata sendersView,
         boolean merge)
   {
      if (sendersView == null)
      {
         return false;
      }

      TimestampDiscrepancy senderTimestampDiscrepancy = getTimestampDiscrepancy(sendersView.getOriginatingNode(), false); 
      TimestampDiscrepancy currentTimestampDiscrepancy = getTimestampDiscrepancy(current.getOriginatingNode(), false);
      
      return isChangeMoreRecent(sendersView, current, senderTimestampDiscrepancy, currentTimestampDiscrepancy, true);
   }

   /**
    * Accepts the removal if the time-discrepancy adjusted timestamp of
    * <code>update</code> is at least as recent as the time-discrepancy 
    * adjusted timestamp of the <code>current</code> item.
    */
   protected boolean acceptUpdate(RepositoryItemMetadata update, RepositoryItemMetadata current,
         boolean merge)
   {
      TimestampDiscrepancy updateDiscrepancy = getTimestampDiscrepancy(update.getOriginatingNode(), false); 
      TimestampDiscrepancy currentTimestampDiscrepancy = getTimestampDiscrepancy(current.getOriginatingNode(), false);
      
      return isChangeMoreRecent(update, current, updateDiscrepancy, currentTimestampDiscrepancy, false);
   }
   
   // ----------------------------------------------------------------  Private  

   private static boolean isChangeMoreRecent(RepositoryItemMetadata toChange, RepositoryItemMetadata current,
         TimestampDiscrepancy senderTimestampDiscrepancy, TimestampDiscrepancy currentTimestampDiscrepancy, boolean equalAllowed)
   {
      if (senderTimestampDiscrepancy == null)
      {
         // Just have to hope for the best
         senderTimestampDiscrepancy = TimestampDiscrepancy.NO_DISCREPANCY;
      }
      if (currentTimestampDiscrepancy == null)
      {
         // Just have to hope for the best
         currentTimestampDiscrepancy = TimestampDiscrepancy.NO_DISCREPANCY;
      }
      
      long senderTime, currentTime;
      if (toChange.getOriginatingNode().equals(current.getOriginatingNode()))
      {
         senderTime = toChange.getTimestamp();
         currentTime = current.getTimestamp();
      }
      else
      {
         senderTime = senderTimestampDiscrepancy.getMinLocalTimestamp(toChange.getTimestamp());
         currentTime = currentTimestampDiscrepancy.getMaxLocalTimestamp(current.getTimestamp());         
      }
      return equalAllowed ? senderTime > currentTime :senderTime > currentTime;
   }
   
   private TimestampDiscrepancy getTimestampDiscrepancy(String originatingNode, boolean allowStatusCheck)
   {
      TimestampDiscrepancy td = timestampService.getTimestampDiscrepancy(originatingNode, allowStatusCheck);
      // If we don't have a record for the originator, use NO_DISCREPANCY and hope for the best
      return td == null ? TimestampDiscrepancy.NO_DISCREPANCY : td;
   }

}
