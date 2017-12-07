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

import javax.management.j2ee.statistics.RangeStatistic;

/**
 * This class is the JBoss specific Range Statistics class allowing
 * just to increase and resetStats the instance.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>
 * @version $Revision: 81025 $
 */
public class RangeStatisticImpl
        extends StatisticImpl
        implements RangeStatistic
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -7893492477598566197L;
      
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   protected long current;
   protected long highWaterMark;
   protected long lowWaterMark;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) constructor
    */
   public RangeStatisticImpl(String pName, String pUnit, String pDescription)
   {
      super(pName, pUnit, pDescription);
   }

   // -------------------------------------------------------------------------
   // RangeStatistic Implementation
   // -------------------------------------------------------------------------

   /**
    * @return The value of Current
    */
   public long getCurrent()
   {
      return current;
   }

   /**
    * @return The value of HighWaterMark
    */
   public long getHighWaterMark()
   {
      return highWaterMark;
   }

   /**
    * @return The value of LowWaterMark
    */
   public long getLowWaterMark()
   {
      return lowWaterMark;
   }

   /**
    * @return Debug Information about this Instance
    */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer();
      tmp.append('[');
      tmp.append("low: ");
      tmp.append(lowWaterMark);
      tmp.append(", high: ");
      tmp.append(highWaterMark);
      tmp.append(", current: ");
      tmp.append(current);
      tmp.append(']');
      tmp.append(super.toString());
      return tmp.toString();
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Adds a hit to this counter
    */
   public void add()
   {
      set(++current);
   }

   /**
    * Removes a hit to this counter
    */
   public void remove()
   {
      if (current > 0)
      {
         set(--current);
      }
   }

   /**
    * Resets the statistics to the initial values
    */
   public void reset()
   {
      current = 0;
      highWaterMark = 0;
      lowWaterMark = 0;
      super.reset();
   }

   public void set(long current)
   {
      this.current = current;
      if (current < lowWaterMark)
      {
         lowWaterMark = current;
      }
      if (current > highWaterMark)
      {
         highWaterMark = current;
      }
      super.set();
   }
}
