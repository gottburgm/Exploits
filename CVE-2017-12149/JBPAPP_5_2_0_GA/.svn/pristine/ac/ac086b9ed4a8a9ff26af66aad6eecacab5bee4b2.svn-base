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
 * Specifies standard timing measurements.
 * 
 * @author thomas.diesler@jboss.org
 */
public interface TimeStatistic extends Statistic
{
   /**
    * Number of times the operation was invoked since the beginning of this measurement.
    */
   public long getCount();

   /**
    * The maximum amount of time taken to complete one invocation of this operation since the beginning of this measurement.
    */
   public long getMaxTime();

   /**
    * The minimum amount of time taken to complete one invocation of this operation since the beginning of this measurement.
    */
   public long getMinTime();

   /**
    * This is the sum total of time taken to complete every invocation of this operation since the beginning of this measurement.
    * Dividing totalTime by count will give you the average execution time for this operation.
    */
   public long getTotalTime();
}

