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

import javax.management.j2ee.statistics.BoundedRangeStatistic;

/**
 * This class is the JBoss specific BoundedRangeStatistic implementation
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>
 * @version $Revision: 81025 $
 */
public class BoundedRangeStatisticImpl
        extends StatisticImpl
        implements BoundedRangeStatistic
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = 3607798304045224550L;
   
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   protected BoundaryStatisticImpl boundaryStat;
   protected RangeStatisticImpl rangeStat;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Create a named BoundedRangeStatistic with the given upper and lower bounds.
    *
    * @param name        the name of the statistic
    * @param units       the units of the statistic
    * @param description the description of the statistic
    * @param lowerBound  the lower bound the statistic will attain
    * @param upperBound  the upper bound the statistic will attain
    */
   public BoundedRangeStatisticImpl(String name, String units, String description,
                                    long lowerBound, long upperBound)
   {
      super(name, units, description);
      boundaryStat = new BoundaryStatisticImpl(name, units, description,
              lowerBound, upperBound);
      rangeStat = new RangeStatisticImpl(name, units, description);
   }

   // -------------------------------------------------------------------------
   // CountStatistic Implementation
   // -------------------------------------------------------------------------

   /**
    * @return The value of Current
    */
   public long getCurrent()
   {
      return rangeStat.getCurrent();
   }

   /**
    * @return The value of HighWaterMark
    */
   public long getHighWaterMark()
   {
      return rangeStat.getHighWaterMark();
   }

   /**
    * @return The value of LowWaterMark
    */
   public long getLowWaterMark()
   {
      return rangeStat.getLowWaterMark();
   }

   /**
    * @return The value of Lower Bound
    */
   public long getLowerBound()
   {
      return boundaryStat.getLowerBound();
   }

   /**
    * @return The value of Upper Bound
    */
   public long getUpperBound()
   {
      return boundaryStat.getUpperBound();
   }

   /**
    * @return Debug Information about this Instance
    */
   public String toString()
   {
      return "BoundedRangeStatistics[ " + rangeStat.toString() + ", " + boundaryStat.toString() + " ]";
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Adds a hit to this counter
    */
   public void add()
   {
      rangeStat.add();
   }

   /**
    * Removes a hit to this counter
    */
   public void remove()
   {
      rangeStat.remove();
   }

   /**
    * Resets the statistics to the initial values
    */
   public void reset()
   {
      rangeStat.reset();
   }

   /**
    * Set the current value of the RangeStatistic
    *
    * @param current the new current value
    */
   public void set(long current)
   {
      rangeStat.set(current);
   }
}
