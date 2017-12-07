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
package org.jboss.services.loggingmonitor;

import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates the specification of a log file's rollover period.
 * 
 * @author <a href="mailto:jimmy.wilson@acxiom.com">James Wilson</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
class RolloverPeriod
{
   private static final Map periodFormatsMap;
   static
   {
      periodFormatsMap = new HashMap();
      periodFormatsMap.put("MONTH",     "\'.\'yyyy-MM");
      periodFormatsMap.put("MONTHLY",   "\'.\'yyyy-MM");
      periodFormatsMap.put("WEEK",      "\'.\'yyyy-ww");
      periodFormatsMap.put("WEEKLY",    "\'.\'yyyy-ww");
      periodFormatsMap.put("DAY",       "\'.\'yyyy-MM-dd");
      periodFormatsMap.put("DAILY",     "\'.\'yyyy-MM-dd");
      periodFormatsMap.put("HALFDAY",   "\'.\'yyyy-MM-dd-a");
      periodFormatsMap.put("HALFDAILY", "\'.\'yyyy-MM-dd-a");
      periodFormatsMap.put("HOUR",      "\'.\'yyyy-MM-dd-HH");
      periodFormatsMap.put("HOURLY",    "\'.\'yyyy-MM-dd-HH");
      periodFormatsMap.put("MINUTE",    "\'.\'yyyy-MM-dd-HH-mm");
   }

   private String rolloverPeriod;
   private String rolloverFormat;

   /**
    * Constructor.
    *
    * @param rolloverPeriod a rollover period specification.
    */
   public RolloverPeriod(String rolloverPeriod)
   {
      this.rolloverFormat = (String)periodFormatsMap.get(rolloverPeriod.toUpperCase());

      if (this.rolloverFormat == null)
      {
         throw new IllegalArgumentException("Unknown rollover period: " + rolloverPeriod);
      }
      this.rolloverPeriod = rolloverPeriod;
   }

   /**
    * Returns the rollover format specification associated with the rollover
    * period specification used to construct this class.
    */
   public String getRolloverFormat()
   {
      return rolloverFormat;
   }

   /**
    * Returns the rollover period specification used to construct this class.
    */
   public String toString()
   {
      return rolloverPeriod;
   }
}
