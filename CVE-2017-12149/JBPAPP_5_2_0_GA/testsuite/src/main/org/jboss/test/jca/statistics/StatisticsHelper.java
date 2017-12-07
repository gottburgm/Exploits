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
package org.jboss.test.jca.statistics;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.statistic.formatter.StatisticsFormatter;

/**
 * A StatisticsHelper.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81036 $
 */
public class StatisticsHelper
{
   
   private static final String DEFAULT_FORMATTER = "org.jboss.resource.statistic.pool.JBossDefaultSubPoolStatisticFormatter";
   private static final String XML_FORMATTER = "org.jboss.resource.statistic.pool.JBossXmlSubPoolStatisticFormatter";
   
   private static final ObjectName POOL_NAME = ObjectNameFactory.create("jboss.jca:service=ManagedConnectionPool,name=StatsDS");
   private static final String ATTRIBUTE_NAME = "StatisticsFormatter";
   private static final String RAW_STATS_METHOD = "listStatistics";
   private static final String FORMATTED_STATS_METHOD = "listFormattedSubPoolStatistics";
   
   //Pool values
   
   public static final int DEFAULT_MIN_SIZE = 0;
   public static final int DEFAULT_MAX_SIZE = 20;
   public static final int DEFAULT_BLOCK_TIMEOUT = 30000;
   public static final int DEFAULT_IDLE_TIMEOUT = 15;
   
   
   
   
   public static Object listRawStatistics(MBeanServerConnection server) throws Exception{
      
      return server.invoke(POOL_NAME, RAW_STATS_METHOD, new Object[0], new String[0]);
            
   }
   
   public static void setStatisticsFormatter(MBeanServerConnection server, String formatter) throws Exception{
      
      server.setAttribute(POOL_NAME, new Attribute(ATTRIBUTE_NAME, formatter));
      
   }

   public static String getStatisticsFormatter(MBeanServerConnection server) throws Exception{
   
      return (String)server.getAttribute(POOL_NAME, ATTRIBUTE_NAME);
      
   }
   
   public static String listFormattedStatistics(MBeanServerConnection server) throws Exception{
      
      return (String)server.invoke(POOL_NAME, FORMATTED_STATS_METHOD, new Object[0], new String[0]);
      
   }
   
   public static StatisticsFormatter getDefaultFormatter() throws Exception{
      
      Class clazz = Class.forName(DEFAULT_FORMATTER);
      return (StatisticsFormatter)clazz.newInstance();
      
   }
}
