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
 * The Statistic model and its sub-models specify the data models which are requried to be used to provide the performance
 * data described by the specific attributes in the Stats models.
 * 
 * @author thomas.diesler@jboss.org
 */
public interface Statistic
{
   /**
    * The name of this Statistic.
    */
   public String getName();

   /**
    * The unit of measurement for this Statistic. Valid values for TimeStatistic measurements are "HOUR", "MINUTE", "SECOND", "MILLISECOND", "MICROSECOND" and "NANOSECOND".
    */
   public String getUnit();

   /**
    * A human-readable description of the Statistic.
    */
   public String getDescription();

   /**
    * The time of the first measurement represented as a long, whose value is the number of milliseconds since January 1, 1970, 00:00:00.
    */
   public long getStartTime();

   /**
    * The time of the last measurement represented as a long, whose value is the number of milliseconds since January 1, 1970, 00:00:00.
    */
   public long getLastSampleTime();
}

