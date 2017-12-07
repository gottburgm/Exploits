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
package javax.management.j2ee.statistics;

/**
 * The Stats model and its submodels specify performance data attributes for each of the specific managed object types.
 * 
 * @author thomas.diesler@jboss.org
 */
public interface Stats
{
   /**
    * Get a Statistic by name.
    */
   public Statistic getStatistic(String statisticName);

   /**
    * Returns an array of Strings which are the names of the attributes from the specific Stats submodel that this object supports.
    * Attributes named in the list must correspond to attributes that will return a Statistic object of the appropriate type
    * which contains valid performance data. The return value of attributes in the Stats submodel that are not included in
    * the statisticNames list must be null. For each name in the statisticNames list there must be one Statistic with the
    * same name in the statistics list.
    */
   public String[] getStatisticNames();

   /**
    * Returns an array containing all of the Statistic objects supported by this Stats object.
    */
   public Statistic[] getStatistics();
}

