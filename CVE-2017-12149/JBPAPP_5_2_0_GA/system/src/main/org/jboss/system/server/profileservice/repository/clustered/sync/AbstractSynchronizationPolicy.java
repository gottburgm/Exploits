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

import java.util.Iterator;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryItemMetadata;
import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryRootMetadata;

/**
 * Abstract base class to support implementations of {@link SynchronizationPolicy}.
 * <p>
 * Implements the various RepositorySynchronizationPolicy 
 * <i>acceptXXX</i> methods by checking if a Boolean property has been set
 * dictating the response; if not delegates the call to one of the abstract
 * protected methods that subclasses implement.
 *  
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 *
 */
public abstract class AbstractSynchronizationPolicy implements SynchronizationPolicy
{
   /**
    * Default value for {@link #getRemovalTrackingTime()}, equal to 30 days.
    */
   public static final long DEFAULT_REMOVAL_TRACKING_TIME = 30l * 24l * 60l * 60l * 1000l;
   
   private long removalTrackingTime = DEFAULT_REMOVAL_TRACKING_TIME;
   private Boolean allowJoinAdditions;
   private Boolean allowJoinReincarnations;
   private Boolean allowJoinUpdates;
   private Boolean allowJoinRemovals;
   private Boolean allowMergeAdditions;
   private Boolean allowMergeReincarnations;
   private Boolean allowMergeUpdates;
   private Boolean allowMergeRemovals;
   private boolean developerMode = false;
   
   // -------------------------------------------------------------  Properties

   /**
    * Gets any fixed response to 
    * {@link #acceptJoinAddition(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptAddition(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowJoinAdditions()
   {
      return allowJoinAdditions;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptJoinAddition(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptAddition(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowJoinAdditions(Boolean allow)
   {
      this.allowJoinAdditions = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptJoinReincarnation(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptReincarnation(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowJoinReincarnations()
   {
      return allowJoinReincarnations;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptJoinReincarnation(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptReincarnation(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowJoinReincarnations(Boolean allow)
   {
      this.allowJoinReincarnations = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptJoinUpdate(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptUpdate(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowJoinUpdates()
   {
      return allowJoinUpdates;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptJoinUpdate(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptUpdate(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowJoinUpdates(Boolean allow)
   {
      this.allowJoinUpdates = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptJoinRemoval(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptRemoval(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowJoinRemovals()
   {
      return allowJoinRemovals;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptJoinRemoval(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptRemoval(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowJoinRemovals(Boolean allow)
   {
      this.allowJoinRemovals = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptMergeAddition(RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptAddition(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowMergeAdditions()
   {
      return allowMergeAdditions;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptMergeAddition(RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptAddition(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowMergeAdditions(Boolean allow)
   {
      this.allowMergeAdditions = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptMergeReincarnation(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptReincarnation(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowMergeReincarnations()
   {
      return allowMergeReincarnations;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptMergeReincarnation(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptReincarnation(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowMergeReincarnations(Boolean allow)
   {
      this.allowMergeReincarnations = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptMergeUpdate(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptUpdate(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowMergeUpdates()
   {
      return allowMergeUpdates;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptMergeUpdate(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptUpdate(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowMergeUpdates(Boolean allow)
   {
      this.allowMergeUpdates = allow;
   }

   /**
    * Gets any fixed response to 
    * {@link #acceptMergeRemoval(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @return a fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptRemoval(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public Boolean getAllowMergeRemovals()
   {
      return allowMergeRemovals;
   }

   /**
    * Sets any fixed response to 
    * {@link #acceptMergeRemoval(RepositoryItemMetadata, RepositoryItemMetadata)}.
    * 
    * @param allow the fixed response, or <code>null</code> if there is no fixed
    *         response and the call should be delegated to 
    *         {@link #acceptRemoval(RepositoryItemMetadata, RepositoryItemMetadata, boolean)}
    */
   public void setAllowMergeRemovals(Boolean allow)
   {
      this.allowMergeRemovals = allow;
   }
   
   
   /**
    * Gets whether this policy is in a very lenient "developer mode" in which
    * case it will return <code>true</code> to all <i>acceptXXX</i> calls.
    * The purpose of this is to eliminate any need for development servers
    * to coordinate system timestamps.
    * 
    * @return <code>true</code> if the policy is in developer mode.
    */
   public boolean isDeveloperMode()
   {
      return developerMode;
   }

   /**
    * Sets whether this policy is in a very lenient "developer mode" in which
    * case it will return <code>true</code> to all <i>acceptXXX</i> calls.
    * The purpose of this is to eliminate any need for development servers
    * to coordinate system timestamps.
    * 
    * @param developerMode <code>true</code> if the policy should be in developer mode.
    */
   public void setDeveloperMode(boolean developerMode)
   {
      this.developerMode = developerMode;
   }

   /**
    * Gets how long in ms this policy should remembered removed items for
    * use in detecting reincarnations. Default is {@link #DEFAULT_REMOVAL_TRACKING_TIME}.
    * 
    * @return the number of ms, or a number less than 1 to indicate removed
    *         items should not be remembered.
    */
   public long getRemovalTrackingTime()
   {
      return removalTrackingTime;
   }

   /**
    * Sets how long in ms this policy should remembered removed items for
    * use in detecting reincarnations. Default is {@link #DEFAULT_REMOVAL_TRACKING_TIME}.
    * 
    * @param removalTrackingTime the number of ms, or a number less than 1 to 
    *                            indicate removed items should not be remembered.
    */
   public void setRemovalTrackingTime(long removalTrackingTime)
   {
      this.removalTrackingTime = removalTrackingTime;
   }
   
   // ----------------------------------------  RepositorySynchronizationPolicy  
   
   public boolean acceptJoinAddition(RepositoryItemMetadata toAdd, RepositoryItemMetadata joinersPrevious)
   {
      if (allowJoinAdditions != null)
      {
         return allowJoinAdditions.booleanValue();
      }
      
      validateParams("toAdd", toAdd, true, null, false);
      
      return acceptAddition(toAdd, joinersPrevious, false);
   }

   public boolean acceptJoinReincarnation(RepositoryItemMetadata reincarnation, RepositoryItemMetadata current)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowJoinReincarnations != null)
      {
         return allowJoinReincarnations.booleanValue();
      }
      
      validateParams("reincarnation", reincarnation, true, current, true);
      
      return acceptReincarnation(reincarnation, current, false);
   }

   public boolean acceptJoinRemoval(RepositoryItemMetadata current, RepositoryItemMetadata joinersItem)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowJoinRemovals != null)
      {
         return allowJoinRemovals.booleanValue();
      }
      
      validateParams("toRemove", joinersItem, false, current, true);
      
      return acceptRemoval(current, joinersItem, false);
   }

   public boolean acceptJoinUpdate(RepositoryItemMetadata update, RepositoryItemMetadata current)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowJoinUpdates != null)
      {
         return allowJoinUpdates.booleanValue();
      }
      
      validateParams("update", update, true, current, true);
      
      return acceptUpdate(update, current, true);
   }

   public boolean acceptMergeAddition(RepositoryItemMetadata toAdd)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowMergeAdditions != null)
      {
         return allowMergeAdditions.booleanValue();
      }
      
      validateParams("toAdd", toAdd, true, null, false);
      
      return acceptAddition(toAdd, null, true);
   }

   public boolean acceptMergeReincarnation(RepositoryItemMetadata reincarnation, RepositoryItemMetadata current)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowMergeReincarnations != null)
      {
         return allowMergeReincarnations.booleanValue();
      }
      
      validateParams("reincarnation", reincarnation, true, current, true);
      
      return acceptReincarnation(reincarnation, current, true);
   }

   public boolean acceptMergeRemoval(RepositoryItemMetadata current, RepositoryItemMetadata mergersView)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowMergeRemovals != null)
      {
         return allowMergeRemovals.booleanValue();
      }
      
      validateParams("toRemove", mergersView, false, current, true);
      
      return acceptRemoval(current, mergersView, true);
   }

   public boolean acceptMergeUpdate(RepositoryItemMetadata update, RepositoryItemMetadata current)
   {
      if (developerMode)
      {
         return true;
      }
      else if (allowMergeUpdates != null)
      {
         return allowMergeUpdates.booleanValue();
      }
      
      validateParams("update", update, true, current, true);
      
      return acceptUpdate(update, current, true);
   }

   public boolean purgeRemovedItems(RepositoryContentMetadata content)
   {
      if (content == null)
      {
         return false;
      }
      
      boolean purged = false;
      
      long oldest = this.removalTrackingTime < 1 ? 0 : System.currentTimeMillis() - this.removalTrackingTime;
      
      for (RepositoryRootMetadata rrmd : content.getRepositories())
      {
         for (Iterator<RepositoryItemMetadata> it = rrmd.getContent().iterator(); it.hasNext(); )
         {
            RepositoryItemMetadata rimd = it.next();
            if (rimd.isRemoved() && rimd.getTimestamp() < oldest)
            {
               it.remove();
               purged = true;
            }
         }
      }
      
      return purged;
   }
   
   // -------------------------------------------------------------  Protected  
   
   protected abstract boolean acceptAddition(RepositoryItemMetadata toAdd, RepositoryItemMetadata joinersPrevious,
         boolean merge);

   protected abstract boolean acceptReincarnation(RepositoryItemMetadata reincarnation, RepositoryItemMetadata current,
         boolean merge);

   protected abstract boolean acceptRemoval(RepositoryItemMetadata current, RepositoryItemMetadata sendersView,
         boolean merge);

   protected abstract boolean acceptUpdate(RepositoryItemMetadata update, RepositoryItemMetadata current,
         boolean merge);
   
   // ----------------------------------------------------------------  Private
   
   /** Utility to throw IAE if required params are null 
    */
   private static void validateParams(String changeName, 
                                      RepositoryItemMetadata change, 
                                      boolean requireChange, 
                                      RepositoryItemMetadata current, 
                                      boolean requireCurrent)
   {      
      if (change == null && requireChange)
      {
         throw new IllegalArgumentException("Null " + changeName);
      }      
      if (requireCurrent && current == null)
      {
         throw new IllegalArgumentException("Null current");
      }      
   }

}
