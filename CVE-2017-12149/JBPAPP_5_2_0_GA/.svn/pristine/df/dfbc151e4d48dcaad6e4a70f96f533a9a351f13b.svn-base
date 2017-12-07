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
package org.jboss.test.ejb3.jbpapp4681;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * TimeoutTracker
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TimeoutTracker
{

   private static TimeoutTracker instance;

   private static Logger logger = Logger.getLogger(TimeoutTracker.class);
   
   private Map<String, Integer> timeoutCount;
   
   public synchronized static TimeoutTracker getInstance()
   {
      if (instance == null)
      {
         instance = new TimeoutTracker();
      }
      return instance;
   }

   private TimeoutTracker()
   {
      this.timeoutCount = new HashMap<String, Integer>();
   }
   
   public void trackTimeout(String name)
   {
      logger.info("Tracking timeout for: " + name);
      Integer count = this.timeoutCount.get(name);
      if (count == null)
      {
         count = new Integer(0);
      }
      count++;
      logger.info("Number of timeouts for: " + name + " = " + count);
      this.timeoutCount.put(name, count);
   }
   
   public int getTimeoutCount(String name)
   {
      Integer count = this.timeoutCount.get(name);
      if (count == null)
      {
         return 0;
      }
      return count;
   }
}
