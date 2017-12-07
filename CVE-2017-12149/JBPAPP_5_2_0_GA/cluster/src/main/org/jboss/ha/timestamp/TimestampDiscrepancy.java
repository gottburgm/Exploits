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

import java.io.Serializable;

/**
 * Provides information on possible system timestamp discrepancies between
 * a remote node and the local node.
 * <p>
 * <strong>Usage:</strong> The local node should record the current system
 * time and then request the current system time from the remote node. The
 * local node should then record the current system time when the response
 * is received from the remote node.  The three values are then passed to
 * this class' constructor.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class TimestampDiscrepancy implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6193847623651196577L;
   
   /** Fake discrepancy that indicates no system clock difference */
   public static final TimestampDiscrepancy NO_DISCREPANCY;
   static
   {
      long now = System.currentTimeMillis();
      NO_DISCREPANCY = new TimestampDiscrepancy(now, now, now);
   }
   
   private final long fastRequestLimit;
   private final long fastResponseLimit;
   private final long minDiscrepancy;
   private final long maxDiscrepancy;
   private final long remoteTimestamp;
   private final long requestRoundtripTime;
   
   /**
    * Create a new TimestampDiscrepancy using the value returned by a remote
    * request plus the local timestamps for when the request started and 
    * completed.
    * 
    * @param remoteTimestamp the timestamp returned by the remote node
    * @param requestSent local timestamp immediately before the timestamp request was made 
    * @param responseReceived local timestamp immediately after receipt of response
    *                         to the timestamp request
    */
   public TimestampDiscrepancy(long remoteTimestamp, long requestSent, long responseReceived)
   {
      if (responseReceived < requestSent)
      {
         throw new IllegalArgumentException("Apparent time travel: " + 
               responseReceived + " is less than " + requestSent);
      }
      
      // Limit 1: assume the remote node responded immediately when the
      // request was sent, i.e. 0 time to transmit request
      fastRequestLimit = remoteTimestamp - requestSent;
      
      // Limit 2: assume the remote node responded immediately before the
      // response was received, i.e. 0 time to transmit response
      fastResponseLimit = responseReceived - remoteTimestamp;
      
      this.minDiscrepancy = Math.min(fastRequestLimit, fastResponseLimit);
      this.maxDiscrepancy = Math.max(fastRequestLimit, fastResponseLimit);
      
      this.remoteTimestamp = remoteTimestamp;
      this.requestRoundtripTime = responseReceived - requestSent;
   }
   
   /**
    * Generates a synthetic TimestampDiscrepancy based on a value provided
    * by another node adjusted for the discrepancy between this node and
    * the
   
   private TimestampDiscrepancy(long now)
   {
      this(now, now, now);
   } node that provided the base value. Used to create an estimated
    * discrepancy between this node and a node that can no longer be contacted
    * directly (e.g. because it has shut down).  Necessarily less accurate
    * than a TimestampDiscrepancy constructed via the normal method.
    * 
    * @param base
    * @param intermediary
    */
   public TimestampDiscrepancy(TimestampDiscrepancy base, TimestampDiscrepancy intermediary)
   {
      if (base == null)
      {
         throw new IllegalArgumentException("Null base");
      }
      if (intermediary == null)
      {
         throw new IllegalArgumentException("Null intermediary");
      }
      
      fastRequestLimit = base.fastRequestLimit + intermediary.fastRequestLimit;      
      fastResponseLimit = base.fastResponseLimit + intermediary.fastResponseLimit;
      
      this.minDiscrepancy = Math.min(fastRequestLimit, fastResponseLimit);
      this.maxDiscrepancy = Math.max(fastRequestLimit, fastResponseLimit);
      
      this.remoteTimestamp = base.remoteTimestamp;
      this.requestRoundtripTime = base.requestRoundtripTime + intermediary.requestRoundtripTime;
   }
   
   /**
    * Gets the timestamp that the remote node returned. 
    * 
    * @return the remote timestamp.
    */
   public long getRemoteTimestamp()
   {
      return remoteTimestamp;
   }

   /**
    * Minimum offset that would be applied to a local timestamp to obtain
    * the timestamp on the remote system of a simultaneously occurring event.
    * 
    * @return the minimum discrepancy
    */
   public long getMinDiscrepancy()
   {
      return minDiscrepancy;
   }

   /**
    * Maximum offset that would be applied to a remote timestamp to obtain
    * the timestamp on the local system of a simultaneously occurring event.
    * 
    * @return the maximum discrepancy
    */
   public long getMaxDiscrepancy()
   {
      return maxDiscrepancy;
   }
   
   /**
    * Gets the higher of the absolute value of {@link #getMinDiscrepancy()}
    * or the absolute value of {@link #getMaxDiscrepancy()}.
    */
   public long getAbsoluteMaxDiscrepancy()
   {
      return Math.max(Math.abs(minDiscrepancy), Math.abs(maxDiscrepancy));
   }
   
   /**
    * Gets the difference between {@link #getMinDiscrepancy()} and 
    * {@link #getMaxDiscrepancy()}
    * 
    * @return
    */
   public long getDiscrepancyRange()
   {
      return maxDiscrepancy - minDiscrepancy;
   }
   
   /**
    * Gets a rough estimate of the timestamp discrepancy between the systems.
    * 
    * @return the average between {@link #getMinDiscrepancy()} and 
    * {@link #getMaxDiscrepancy()}
    */
   public long getEstimatedDiscrepancy()
   {
      return (maxDiscrepancy + minDiscrepancy) / 2;
   }
   
   /**
    * Gets the number of ms it took for the remote request that returned
    * {@link #getRemoteTimestamp() the remote timestamp}. The longer the
    * request took to execute, the less accurate the timestamp discrepancy.
    * 
    * @return number of ms it took to execute the timestamp request
    */
   public long getRequestRoundtripTime()
   {
      return requestRoundtripTime;
   }
   
   /**
    * Gets the minimum value for a local timestamp that would correspond
    * to the remote timestamp.
    * 
    * @param remoteTimestamp the remote timestamp
    * @return the equivalent local timestamp
    */
   public long getMinLocalTimestamp(long remoteTimestamp)
   {
      return remoteTimestamp - minDiscrepancy;
   }
   
   /**
    * Gets the maximum value for a local timestamp that would correspond
    * to the remote timestamp.
    * 
    * @param remoteTimestamp the remote timestamp
    * @return the equivalent local timestamp
    */
   public long getMaxLocalTimestamp(long remoteTimestamp)
   {
      return remoteTimestamp + maxDiscrepancy;
   }
   
   /**
    * Gets the minimum value for a remote timestamp that would correspond
    * to the local timestamp.
    * 
    * @param localTimestamp the local timestamp
    * @return the equivalent remote timestamp
    */
   public long getMinRemoteTimestamp(long localTimestamp)
   {
      return localTimestamp + minDiscrepancy;
   }
   
   /**
    * Gets the maximum value for a remote timestamp that would correspond
    * to the local timestamp.
    * 
    * @param localTimestamp the local timestamp
    * @return the equivalent remote timestamp
    */
   public long getMaxRemoteTimestamp(long localTimestamp)
   {
      return localTimestamp - maxDiscrepancy;
   }
}
