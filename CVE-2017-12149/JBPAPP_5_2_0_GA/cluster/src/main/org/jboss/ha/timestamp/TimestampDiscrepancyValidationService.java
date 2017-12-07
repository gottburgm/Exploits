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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.logging.Logger;

/**
 * Logs a WARN or ERROR when it determines that this node's system clock is out
 * of sync with another node by more than a 
 * {@link #setMaxDiscrepancy(long) configurable amount}. Whether
 * a WARN or ERROR is logged is {@link #setUseErrorLogging(boolean) configurable}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class TimestampDiscrepancyValidationService implements TimestampDiscrepancyObserver
{
   private static final Object NULL = new Object();
   
   private static final Logger log = Logger.getLogger(TimestampDiscrepancyValidationService.class);
   
   /** Default value for property {@link #getMaxDiscrepancy() maxDiscrepancy} */
   public static final long DEFAULT_MAX_DISCREPANCY = 60000;
   
   private TimestampDiscrepancyService service;
   private long maxDiscrepancy = DEFAULT_MAX_DISCREPANCY;
   private final Map<ClusterNode, Object> loggedNodes = new ConcurrentHashMap<ClusterNode, Object>();
   private boolean useErrorLogging;
   
   // -------------------------------------------------------------- Properties
   
   public TimestampDiscrepancyService getTimestampDiscrepancyService()
   {
      return service;
   }
   
   public void setTimestampDiscrepancyService(TimestampDiscrepancyService service)
   {
      this.service = service;
   }
   
   /**
    * Gets the maximum allowed potential discrepancy, in ms, between another 
    * server's clock and this server's clock. Default is 
    * {@link #DEFAULT_MAX_DISCREPANCY}. 
    * <p>
    * The potential discrepancy is the larger of the absolute value of
    * {@link TimestampDiscrepancy#getMinDiscrepancy()} and the absolute value of
    * {@link TimestampDiscrepancy#getMaxDiscrepancy()}.
    * 
    * @return
    */
   public long getMaxDiscrepancy()
   {
      return maxDiscrepancy;
   }
   
   public void setMaxDiscrepancy(long maxDiscrepancy)
   {
      this.maxDiscrepancy = maxDiscrepancy;
   }
   
   /**
    * Gets whether this object will log at ERROR level instead of WARN.
    * 
    * @return <code>true</code> if ERROR logging will be used.
    */
   public boolean getUseErrorLogging()
   {
      return useErrorLogging;
   }
   
   /**
    * Sets whether this object will log at ERROR level instead of WARN.
    * 
    * @param useErrorLogging <code>true</code> if ERROR logging should be used.
    */
   public void setUseErrorLogging(boolean useErrorLogging)
   {
      this.useErrorLogging = useErrorLogging;
   }
   
   // ------------------------------------------------------------------ Public
   
   public void start()
   {
      if (service == null)
      {
         throw new IllegalStateException("A TimestampDiscrepancyService must be injected");
      }
      
      Map<ClusterNode, TimestampDiscrepancy> map = service.getTimestampDiscrepancies(true);
      for (Map.Entry<ClusterNode, TimestampDiscrepancy> entry : map.entrySet())
      {
         timestampDiscrepancyChanged(entry.getKey(), entry.getValue());
      }
   }
   
   public void stop()
   {
      loggedNodes.clear();
   }
   
   // -------------------------------------------- TimestampDiscrepancyObserver
   

   public boolean canRemoveDeadEntry(ClusterNode dead, long lastChecked)
   {
      return true;
   }
   
   public void timestampDiscrepancyChanged(ClusterNode node, TimestampDiscrepancy discrepancy)
   {
      if (service.isServerActive(node))
      {
         long discrep = Math.max(Math.abs(discrepancy.getMinDiscrepancy()), 
                                 Math.abs(discrepancy.getMaxDiscrepancy()));
         if (discrep > maxDiscrepancy)
         {
            if (loggedNodes.put(node, NULL) == null)
            {
               String msg = "Possible excessive system clock discrepancy between " +
               "this node and " + node + " -- potential discrepancy of" +
               discrep + " ms exceeds the configured limit of " + 
               maxDiscrepancy + " ms";
               
               if (useErrorLogging)
               {
                  log.error(msg);
               }
               else
               {
                  log.warn(msg);
               }
            }
         }
      }      
   }
}
