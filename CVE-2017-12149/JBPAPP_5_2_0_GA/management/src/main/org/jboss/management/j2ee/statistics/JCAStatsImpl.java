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


import javax.management.j2ee.statistics.JCAConnectionPoolStats;
import javax.management.j2ee.statistics.JCAConnectionStats;
import javax.management.j2ee.statistics.JCAStats;

/**
 * The JSR77.6.18 JCAStats implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public final class JCAStatsImpl extends StatsBase
        implements JCAStats
{
   // Constants -----------------------------------------------------

   /** @since 4.0.2 */
   private static final long serialVersionUID = 2395031578601025482L;
   
   // Attributes ----------------------------------------------------
   private JCAConnectionStats[] connectionStats;
   private JCAConnectionPoolStats[] poolStats;

   // Constructors --------------------------------------------------

   public JCAStatsImpl(JCAConnectionStats[] connectionStats,
                       JCAConnectionPoolStats[] poolStats)
   {
      if (connectionStats == null)
         connectionStats = new JCAConnectionStats[0];
      this.connectionStats = connectionStats;
      if (poolStats == null)
         poolStats = new JCAConnectionPoolStats[0];
      this.poolStats = poolStats;

   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JCAStats implementation -----------------

   public JCAConnectionStats[] getConnections()
   {
      return connectionStats;
   }

   public JCAConnectionPoolStats[] getConnectionPools()
   {
      return poolStats;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("JCAStats");
      tmp.append("[(JCAConnectionStats[]), (");
      for (int p = 0; p < poolStats.length; p++)
      {
         tmp.append(poolStats[p]);
         if (p < poolStats.length - 1)
            tmp.append(',');
      }
      tmp.append(")]");
      return tmp.toString();
   }
}
