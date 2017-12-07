/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/** A method invocation statistics collection class.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 111905 $
 */
public class InvocationStatistics
   implements Serializable
{
   /** @since 4.2.0 */
   private static final long serialVersionUID = -8031193044335393420L;

   /** A HashMap<Method, TimeStatistic> of the method invocations */
   private Map<Method, TimeStatistic> methodStats;

   public long concurrentCalls = 0;
   public long maxConcurrentCalls = 0;
   public long lastResetTime = System.currentTimeMillis();

   public class TimeStatistic
      implements Serializable
   {
      /** @since 4.2.0 */
      private static final long serialVersionUID = -8689933338506854386L;

      public volatile long count;
      public volatile long minTime = Long.MAX_VALUE;
      public volatile long maxTime;
      public volatile long totalTime;

      public void reset()
      {
         count = 0;
         minTime = Long.MAX_VALUE;
         maxTime = 0;
         totalTime = 0;
      }
   }

   public InvocationStatistics()
   {
      methodStats = new ConcurrentReaderHashMap();
   }

   /** Update the TimeStatistic for the given method. This synchronizes on
    * m to ensure that the TimeStatistic for m is updated atomically.
    *
    * @param m the method to update the statistics for.
    * @param elapsed the elapsed time in milliseconds for the invocation.
    */
   public void updateStats(Method m, long elapsed)
   {
      TimeStatistic stat = methodStats.get(m);
      if (stat == null)
      {
         stat = new TimeStatistic();
         methodStats.put(m, stat);
      }
      stat.count++;
      stat.totalTime += elapsed;
      if (stat.minTime > elapsed)
         stat.minTime = elapsed;
      if (stat.maxTime < elapsed)
         stat.maxTime = elapsed;
   }

   public synchronized void callIn()
   {
      concurrentCalls++;
      if (concurrentCalls > maxConcurrentCalls)
         maxConcurrentCalls = concurrentCalls;
   }

   public synchronized void callOut()
   {
      concurrentCalls--;
   }

   /** 
    * Resets all methodStats by clearing the methodStats map. This will leave    
    * methodStats in the same state as it was before any methods statistics 
    * were captured.
    */
   public void resetStats()
   {
      synchronized (methodStats)
      {
         methodStats.clear();
      }
      maxConcurrentCalls = 0;
      lastResetTime = System.currentTimeMillis();
   }

   /** Access the current collection of method invocation statistics
    *
    * @return A HashMap<Method, TimeStatistic> of the method invocations
    */
   public Map getStats()
   {
      return methodStats;
   }

   /** Generate an XML fragement for the InvocationStatistics. The format is
    * <InvocationStatistics concurrentCalls="c">
    *    <method name="aMethod" count="x" minTime="y" maxTime="z" totalTime="t" />
    *    ...
    * </InvocationStatistics>
    *
    * @return an XML representation of the InvocationStatistics
    */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("<InvocationStatistics concurrentCalls='");
      tmp.append(concurrentCalls);
      tmp.append("' >\n");

      Iterator<Map.Entry<Method, TimeStatistic>> iter = methodStats.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry<Method, TimeStatistic> entry = iter.next();
         TimeStatistic stat = (TimeStatistic) entry.getValue();
         if (stat != null)
         {
            tmp.append("<method name='");
            tmp.append(entry.getKey());
            tmp.append("' count='");
            tmp.append(stat.count);
            tmp.append("' minTime='");
            tmp.append(stat.minTime);
            tmp.append("' maxTime='");
            tmp.append(stat.maxTime);
            tmp.append("' totalTime='");
            tmp.append(stat.totalTime);
            tmp.append("' />\n");
         }
      }
      tmp.append("</InvocationStatistics>");
      return tmp.toString();
   }

   /**
    * Converts the method invocation stats into a detyped nested map structure.
    * The format is:
    *
    * {methodName => {statisticTypeName => longValue}}
    *
    * In addition some other global statistics are added under the fake
    * method name #Global
    *
    * @return A map indexed by method name with map values indexed by statistic type
    */
   public Map<String, Map<String, Long>> toDetypedMap()
   {

      Map<String, Map<String, Long>> detyped = new HashMap<String, Map<String, Long>>();
      for (Map.Entry<Method, TimeStatistic> entry : methodStats.entrySet())
      {
         TimeStatistic stats = entry.getValue();
         Map<String, Long> detypedStats = new HashMap<String, Long>(methodStats.size());
         detypedStats.put("count", stats.count);
         detypedStats.put("minTime", stats.minTime);
         detypedStats.put("maxTime", stats.maxTime);
         detypedStats.put("totalTime", stats.totalTime);
         detyped.put(entry.getKey().getName(), detypedStats);
      }

      Map<String, Long> global = new HashMap<String, Long>();
      global.put("concurrentCalls", concurrentCalls);
      global.put("maxConcurrentCalls", maxConcurrentCalls);
      global.put("lastResetTime", lastResetTime);
      detyped.put("#Global", global);

      return detyped;
   }
}
