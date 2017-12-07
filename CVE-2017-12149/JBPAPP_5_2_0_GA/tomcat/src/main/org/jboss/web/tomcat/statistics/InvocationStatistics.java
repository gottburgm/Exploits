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
package org.jboss.web.tomcat.statistics;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A web context invocation statistics collection class.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81133 $
 */
public class InvocationStatistics implements Serializable
{
   /** The serial version ID */
   private static final long serialVersionUID = 9153807780893455734L;

   /** A HashMap<String, TimeStatistic> of the method invocations */
   private Map<String, TimeStatistic> ctxStats;
   /** The number of concurrent request across all contexts */
   public volatile int concurrentCalls = 0;
   /** The maximum number of concurrent request across all contexts */
   public volatile int maxConcurrentCalls = 0;
   /** Time of the last resetStats call */
   public long lastResetTime = System.currentTimeMillis();

   public static class TimeStatistic
   {
      public long count;
      public long minTime = Long.MAX_VALUE;
      public long maxTime;
      public long totalTime;

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
      ctxStats = new ConcurrentHashMap<String, TimeStatistic>();
   }

   /** Update the TimeStatistic for the given ctx. This does not synchronize
    * on the TimeStatistic so the results are an approximate values.
    *
    * @param ctx the method to update the statistics for.
    * @param elapsed the elapsed time in milliseconds for the invocation.
    */
   public void updateStats(String ctx, long elapsed)
   {
      TimeStatistic stat = (TimeStatistic) ctxStats.get(ctx);
      if( stat == null )
      {
         stat = new TimeStatistic();
         ctxStats.put(ctx, stat);
      }
      stat.count ++;
      stat.totalTime += elapsed;
      if( stat.minTime > elapsed )
         stat.minTime = elapsed;
      if( stat.maxTime < elapsed )
         stat.maxTime = elapsed;
   }

   public void callIn()
   {
      concurrentCalls ++;
      if (concurrentCalls > maxConcurrentCalls)
         maxConcurrentCalls = concurrentCalls;
   }

   public void callOut()
   {
      concurrentCalls --;
   }

   /** Resets all current TimeStatistics.
    *
    */
   public void resetStats()
   {
      synchronized( ctxStats )
      {
         for(TimeStatistic stat : ctxStats.values())
         {
            stat.reset();
         }
      }
      maxConcurrentCalls = 0;
      lastResetTime = System.currentTimeMillis();
   }

   /** Access the current collection of ctx invocation statistics
    *
    * @return A HashMap<String, TimeStatistic> of the ctx invocations
    */
   public Map<String, TimeStatistic> getStats()
   {
      return ctxStats;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("(concurrentCalls: ");
      tmp.append(concurrentCalls);
      tmp.append(", maxConcurrentCalls: ");
      tmp.append(maxConcurrentCalls);

      for(Map.Entry<String, TimeStatistic> entry : ctxStats.entrySet())
      {
         TimeStatistic stat = (TimeStatistic) entry.getValue();
         tmp.append("[webCtx: ");
         tmp.append(entry.getKey());
         tmp.append(", count=");
         tmp.append(stat.count);
         tmp.append(", minTime=");
         tmp.append(stat.minTime);
         tmp.append(", maxTime=");
         tmp.append(stat.maxTime);
         tmp.append(", totalTime=");
         tmp.append(stat.totalTime);
         tmp.append("];");
      }
      tmp.append(")");
      return tmp.toString();
   }

   /** Generate an XML fragement for the InvocationStatistics. The format is
    * <InvocationStatistics concurrentCalls="c" maxConcurrentCalls="x">
    *    <webCtx name="ctx" count="x" minTime="y" maxTime="z" totalTime="t" />
    *    ...
    * </InvocationStatistics>
    *
    * @return an XML representation of the InvocationStatistics
    */
   public String toXML()
   {
      StringBuffer tmp = new StringBuffer("<InvocationStatistics concurrentCalls='");
      tmp.append(concurrentCalls);
      tmp.append("' maxConcurrentCalls='");
      tmp.append(maxConcurrentCalls);
      tmp.append("' >\n");
      
      for(Map.Entry<String, TimeStatistic> entry : ctxStats.entrySet())
      {
         TimeStatistic stat = (TimeStatistic) entry.getValue();
         tmp.append("<webCtx name='");
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
      tmp.append("</InvocationStatistics>");
      return tmp.toString();
   }
}
