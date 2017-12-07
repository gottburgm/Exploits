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
package org.jboss.management.j2ee.statistics;

import javax.management.j2ee.statistics.Statistic;
import java.io.Serializable;

/**
 * JBoss Implementation of the base Model for a Statistic Information
 *
 * @author Marc Fleury
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public abstract class StatisticImpl
        implements Statistic, Serializable
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -3427364348020739916L;
   
   // Attributes ----------------------------------------------------
   protected String name;
   protected String units;
   protected String description;
   protected long startTime;
   protected long lastSampleTime;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a named Statistic.
    *
    * @param name        Name of the statistic
    * @param units       Unit description used in this statistic
    * @param description Human description of the statistic
    */
   public StatisticImpl(String name, String units, String description)
   {
      this.name = name;
      this.units = units;
      this.description = description;
      this.startTime = System.currentTimeMillis();
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.Statistics implementation ---------------

   public String getName()
   {
      return name;
   }

   public String getUnit()
   {
      return units;
   }

   public String getDescription()
   {
      return description;
   }

   public long getStartTime()
   {
      return startTime;
   }

   public long getLastSampleTime()
   {
      return lastSampleTime;
   }

   /**
    * Reset the lastSampleTime and startTime to the current time
    */
   public void reset()
   {
      startTime = System.currentTimeMillis();
      lastSampleTime = startTime;
   }

   /**
    * Update the lastSampleTime and startTime on first call
    */
   public void set()
   {
      lastSampleTime = System.currentTimeMillis();
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer(name);
      tmp.append('(');
      tmp.append("description: ");
      tmp.append(description);
      tmp.append(", units: ");
      tmp.append(units);
      tmp.append(", startTime: ");
      tmp.append(startTime);
      tmp.append(", lastSampleTime: ");
      tmp.append(lastSampleTime);
      tmp.append(')');
      return tmp.toString();
   }
}
