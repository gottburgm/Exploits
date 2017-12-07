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

import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.StatelessSessionBeanStats;

/**
 * The JSR77.6.14 StatlessBeanStats implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class StatelessSessionBeanStatsImpl extends EJBStatsImpl
        implements StatelessSessionBeanStats
{
   // Constants -----------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -5805813168279134341L;
   
   // Private Data --------------------------------------------------
   
   private RangeStatisticImpl methodReadyCount;

   // Constructors --------------------------------------------------
      
   public StatelessSessionBeanStatsImpl()
   {
      methodReadyCount = new RangeStatisticImpl("MethodReadyCount", "1",
              "The count of beans in the method-ready state");
      addStatistic("MethodReadyCount", methodReadyCount);
   }

// Begin javax.management.j2ee.statistics.StatelessSessionBeanStats interface methods

   public RangeStatistic getMethodReadyCount()
   {
      return methodReadyCount;
   }

// End javax.management.j2ee.statistics.StatelessSessionBeanStats interface methods
}
