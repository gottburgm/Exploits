/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.statistic.pool;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.resource.statistic.JBossStatistics;
import org.jboss.resource.statistic.formatter.StatisticsFormatter;


/**
 * A DefaultStatisticsFormatter.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class JBossDefaultSubPoolStatisticFormatter implements StatisticsFormatter
{

   private static final String POOL_SEPERATOR = "------------------------------------------------------";

   public Object formatSubPoolStatistics(Collection subPoolStatistics)
   {
      final StringBuffer statBuff = formatHeader(subPoolStatistics.size());
      
      for(Iterator iter = subPoolStatistics.iterator(); iter.hasNext();){
         
         JBossSubPoolStatistics stat = (JBossSubPoolStatistics)iter.next();
         statBuff.append(stat);
         statBuff.append("\n");
         statBuff.append(POOL_SEPERATOR);
         statBuff.append("\n\n");
         
      }
      
      return statBuff.toString();
   }
   
   
   public Object formatSubPoolStatistics(final ManagedConnectionPoolStatistics stats)
   {
      return formatSubPoolStatistics(stats.getSubPools());
      
   }

   private static StringBuffer formatHeader(int count)
   {

      StringBuffer headerBuff = new StringBuffer();
      headerBuff.append("Sub Pool Statistics: \n");
      headerBuff.append("Sub Pool Count: " + count + "\n");
      headerBuff.append(POOL_SEPERATOR);
      headerBuff.append("\n\n");

      return headerBuff;

   }


   public Object formatStatistics(JBossStatistics stats)
   {
      if(!(stats instanceof ManagedConnectionPoolStatistics)){

         throw new IllegalArgumentException("Error: invalid statistics implementaiton for formatter.");
         
      }
      
      final ManagedConnectionPoolStatistics poolStats = (ManagedConnectionPoolStatistics)stats;      
      return formatSubPoolStatistics(poolStats);
      
   }

}
