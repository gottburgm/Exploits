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

import javax.management.j2ee.statistics.CountStatistic;

/**
 * This class is the JBoss specific Counter Statistics class
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas@jboss.com">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class CountStatisticImpl
        extends StatisticImpl
        implements CountStatistic
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = 8087661344599547469L;
   
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   protected long count;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Create a CountStatistic
    *
    * @param name        the name of the state
    * @param units       the units of the stat
    * @param description a description of the stat
    */
   public CountStatisticImpl(String name, String units, String description)
   {
      super(name, units, description);
   }

   // -------------------------------------------------------------------------
   // CountStatistic Implementation
   // -------------------------------------------------------------------------

   /**
    * @return The value of Count
    */
   public long getCount()
   {
      return count;
   }

   /**
    * @return Debug Information about this Instance
    */
   public String toString()
   {
      return "[ " + getCount() + ":" + super.toString() + " ]";
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Adds a hit to this counter
    */
   public void add()
   {
      set(++count);
   }

   /**
    * Removes a hit to this counter
    */
   public void remove()
   {
      if (count > 0)
      {
         set(--count);
      }
   }

   /**
    * Resets the statistics to the initial values
    */
   public void reset()
   {
      count = 0;
      super.reset();
   }

   /**
    * Set the current value of the count
    *
    * @param count the new count
    */
   public void set(long count)
   {
      this.count = count;
      super.set();
   }
}
