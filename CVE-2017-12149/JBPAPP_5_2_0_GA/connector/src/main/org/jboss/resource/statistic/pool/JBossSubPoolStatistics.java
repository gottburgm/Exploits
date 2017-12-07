/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.statistic.pool;

import org.jboss.resource.statistic.JBossStatistics;

/**
 * A SubPoolStatisticGroup.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 92075 $
 */
public class JBossSubPoolStatistics implements JBossStatistics
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -5089686321382050654L;

   /** The availableConnections */
   private long availableConnections;
   
   /** The maxConnectionsInUse */
   private int maxConnectionsInUse;
   
   /** The connectionsInUse */
   private int connectionsInUse;
   
   /** The connectionsDestroyed */
   private int connectionsDestroyed;

   /** The totalBlockTime */
   private long totalBlockTime;
   
   /** The averageBlockTime */
   private long averageBlockTime;
   
   /** The totalTimedOut */
   private int totalTimedOut;
   
   /** The maxWaitTime */
   private long maxWaitTime;

   /**
    * Get the availableConnections.
    * 
    * @return the availableConnections.
    */
   public long getAvailableConnections()
   {
      return availableConnections;
   }


   /**
    * Set the availableConnections.
    * 
    * @param availableConnections The availableConnections to set.
    */
   public void setAvailableConnections(long availableConnections)
   {
      this.availableConnections = availableConnections;
   }


   /**
    * Get the connectionsDestroyed.
    * 
    * @return the connectionsDestroyed.
    */
   public int getConnectionsDestroyed()
   {
      return connectionsDestroyed;
   }


   /**
    * Set the connectionsDestroyed.
    * 
    * @param connectionsDestroyed The connectionsDestroyed to set.
    */
   public void setConnectionsDestroyed(int connectionsDestroyed)
   {
      this.connectionsDestroyed = connectionsDestroyed;
   }


   /**
    * Get the connectionsInUse.
    * 
    * @return the connectionsInUse.
    */
   public int getConnectionsInUse()
   {
      return connectionsInUse;
   }


   /**
    * Set the connectionsInUse.
    * 
    * @param connectionsInUse The connectionsInUse to set.
    */
   public void setConnectionsInUse(int connectionsInUse)
   {
      this.connectionsInUse = connectionsInUse;
   }


   /**
    * Get the maxConnectionsInUse.
    * 
    * @return the maxConnectionsInUse.
    */
   public int getMaxConnectionsInUse()
   {
      return maxConnectionsInUse;
   }


   /**
    * Set the maxConnectionsInUse.
    * 
    * @param maxConnectionsInUse The maxConnectionsInUse to set.
    */
   public void setMaxConnectionsInUse(int maxConnectionsInUse)
   {
      this.maxConnectionsInUse = maxConnectionsInUse;
   }


   public String toString()
   {
      final StringBuffer statBuff = new StringBuffer();      
      statBuff.append("Available Connections Count: " + getAvailableConnections() + "\n");
      statBuff.append("Max Connections In Use Count:" + getMaxConnectionsInUse() + "\n");
      statBuff.append("Connections Destroyed Count:" + getConnectionsDestroyed() + "\n");
      statBuff.append("Connections In Use Count:" + getConnectionsInUse() + "\n");
      statBuff.append("Total Block Time:" + getTotalBlockTime() + "\n");
      statBuff.append("Average Block Time For Sub Pool:" + getAverageBlockTime() + "\n");
      statBuff.append("Maximum Wait Time For Sub Pool:" + getMaxWaitTime() + "\n");
      
      statBuff.append("Total Timed Out:" + getTotalTimedOut() + "\n");

      return statBuff.toString();
   }


   /**
    * Get the totalBlockTime.
    * 
    * @return the totalBlockTime.
    */
   public long getTotalBlockTime()
   {
      return totalBlockTime;
   }


   /**
    * Set the totalBlockTime.
    * 
    * @param totalBlockTime The totalBlockTime to set.
    */
   public void setTotalBlockTime(long totalBlockTime)
   {
      this.totalBlockTime = totalBlockTime;
   }


   /**
    * Get the totalTimedOut.
    * 
    * @return the totalTimedOut.
    */
   public int getTotalTimedOut()
   {
      return totalTimedOut;
   }


   /**
    * Set the totalTimedOut.
    * 
    * @param totalTimedOut The totalTimedOut to set.
    */
   public void setTotalTimedOut(int totalTimedOut)
   {
      this.totalTimedOut = totalTimedOut;
   }


   /**
    * Get the averageBlockTime.
    * 
    * @return the averageBlockTime.
    */
   public long getAverageBlockTime()
   {
      return averageBlockTime;
   }


   /**
    * Set the averageBlockTime.
    * 
    * @param averageBlockTime The averageBlockTime to set.
    */
   public void setAverageBlockTime(long averageBlockTime)
   {
      this.averageBlockTime = averageBlockTime;
   }
   
   /**
    * Get the maxWaitTime.
    * 
    * @return the maxWaitTime.
    */
   public long getMaxWaitTime()
   {
      return maxWaitTime;
   }

   /**
    * Set the maxWaitTime.
    * 
    * @param maxWaitTime The averageBlockTime to set.
    */
   public void setMaxWaitTime(long maxWaitTime)
   {
      this.maxWaitTime = maxWaitTime;
   }
   
}
