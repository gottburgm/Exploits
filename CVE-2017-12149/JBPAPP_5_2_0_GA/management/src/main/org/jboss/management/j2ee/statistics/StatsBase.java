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
import javax.management.j2ee.statistics.Stats;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The base JSR77.6.10 Stats interface base implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class StatsBase
        implements Stats, Serializable
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = 384207297746356032L;
   
   // Private Data --------------------------------------------------
   
   /**
    * A Map<String,Statistic> of the statistics held by a given
    * Stats implementation
    */
   private Map statistics;

   // Constructors --------------------------------------------------

   public StatsBase()
   {
      statistics = new HashMap();
   }

   public StatsBase(Map statistics)
   {
      this.statistics = statistics;
   }


// Begin Stats interface methods

   /**
    * Access all the Statistics names
    *
    * @return An array of the names of the statistics held in the Stats object
    */
   public String[] getStatisticNames()
   {
      String[] names = new String[statistics.size()];
      statistics.keySet().toArray(names);
      return names;
   }

   /**
    * Access all the Statistics
    *
    * @return An array of the Statistic held in the Stats object
    */
   public Statistic[] getStatistics()
   {
      Statistic[] stats = new Statistic[statistics.size()];
      statistics.values().toArray(stats);
      return stats;
   }

   /**
    * Access a Statistic by its name.
    *
    * @param name
    * @return
    */
   public Statistic getStatistic(String name)
   {
      Statistic stat = (Statistic) statistics.get(name);
      return stat;
   }
// End Stats interface methods

   /**
    * Reset all StatisticImpl objects
    */
   public void reset()
   {
      Iterator iter = statistics.values().iterator();
      while (iter.hasNext())
      {
         Object next = iter.next();
         if (next instanceof StatisticImpl)
         {
            StatisticImpl s = (StatisticImpl) next;
            s.reset();
         }
      }
   }

   public String toString()
   {
      return this.getClass().getName() + " [ " + statistics + " ]";
   }

   /**
    * Add or replace Statistic in the Stats collection.
    *
    * @param name      Name of the Statistic instance
    * @param statistic Statistic to be added
    */
   public void addStatistic(String name, Statistic statistic)
   {
      statistics.put(name, statistic);
   }

}
