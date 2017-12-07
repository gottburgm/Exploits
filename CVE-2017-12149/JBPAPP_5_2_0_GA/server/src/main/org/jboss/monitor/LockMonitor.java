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
package org.jboss.monitor;

import java.io.Serializable;

/**
 * Simple thread-safe POJO encapsulating locking stats.
 * 
 * Turned this class to Serializable to be able to
 * return copies of instances of this class over RMI.
 * 
 * In this case it becomes detached from the EntityLockMonitor
 * factory.
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81030 $
 */
public class LockMonitor implements Serializable
{
   // Private -------------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -6710878502772579272L;
   
   /* Lock Stats */
   private long totalTime;
   private long numContentions;
   private long timeouts;
   private long maxContenders;
   private long currentContenders;
   
   /** Back reference to the non-Serializable LockMonitor factory */
   private transient EntityLockMonitor parent;

   // Constructors --------------------------------------------------
   
   /**
    * @param parent factory
    */
   public LockMonitor(EntityLockMonitor parent)
   {
      this.parent = parent;
   }
   
   // Accessors -----------------------------------------------------
   
   public synchronized long getTotalTime()
   {
      return totalTime;
   }
   
   public synchronized long getNumContentions()
   {
      return numContentions;
   }
   
   public synchronized long getTimeouts()
   {
      return timeouts;
   }
   
   public synchronized long getMaxContenders()
   {
      return maxContenders;
   }

   public synchronized long getCurrentContenders()
   {
      return currentContenders;
   }
   
   // Modifiers -----------------------------------------------------
   
   /**
    * Adjust the counters to indicate a contetion conditions.
    * 
    * If the parent EntityLockMonitor has been initialized
    * the total stats will be updated, as well.
    */
   public void contending()
   {
   	  synchronized(this)
   	  {
   	     ++numContentions;
         ++currentContenders;
         
         if (currentContenders > maxContenders)
         {
            maxContenders = currentContenders;
         }
	  }
      
      // Remark Ulf Schroeter: DO NOT include following call into the
      // synchronization block because it will cause deadlocks between
      // LockMonitor methods and EntityLockMonitor.clearMonitor() call!
      if (parent != null)
      {
         parent.incrementContenders();
      }
   }

   /**
    * Adjust the counters to indicate that contention is over
    * 
    * If the parent EntityLockMonitor has been initialized
    * the total stats will be updated, too.
    */
   public void finishedContending(long time)
   {
      synchronized(this)
      {	
         totalTime += time;
         --currentContenders;
	  }

      // Remark Ulf Schroeter: DO NOT include following call into the
      // synchronization block because it will cause deadlocks between
      // LockMonitor methods and EntityLockMonitor.clearMonitor() call! 
      if (parent != null)
      {
         parent.decrementContenders(time);
      }
   }
   
   /**
    * Increase the timeouts on this lock
    */
   public void increaseTimeouts()
   {
      synchronized(this)
      {
         ++timeouts;
      }
   }
   
   /**
    * Reset the counters.
    * 
    * CurrentContenders stays unchanged and
    * MaxCondenders is set to CurrentContenders
    */
   public void reset()
   {
      synchronized(this)
      {
         timeouts = 0;
         totalTime = 0;
         numContentions = 0;
         // maxContenders always >= currentContenders
         maxContenders = currentContenders;
      }
   }
   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(128);
      
      sbuf.append(super.toString())
          .append("[ ")
          .append("totalTime=").append(getTotalTime())
          .append(", numContentions=").append(getNumContentions())
          .append(", timeouts=").append(getTimeouts())
          .append(", maxContenders=").append(getMaxContenders())
          .append(", currentContenders=").append(getCurrentContenders())
          .append(" ]");
                
      return sbuf.toString();
   }
}