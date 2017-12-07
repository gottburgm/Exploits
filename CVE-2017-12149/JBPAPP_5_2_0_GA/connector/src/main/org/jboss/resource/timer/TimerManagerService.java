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
package org.jboss.resource.timer;

import java.util.Date;

import org.jboss.resource.spi.timer.Timer;
import org.jboss.resource.spi.timer.TimerDate;
import org.jboss.resource.spi.timer.TimerExcecutionContext;
import org.jboss.resource.spi.timer.TimerListener;
import org.jboss.resource.spi.timer.TimerManager;
import org.jboss.resource.spi.timer.TimerPeriod;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A TimerManagerService.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class TimerManagerService extends ServiceMBeanSupport implements TimerManager, TimerManagerMBean
{

   public boolean isStopped()
   {
      return false;
   }

   public boolean isSuspending()
   {
      return false;
   }

   public void resume()
   {

   }

   public Timer schedule(TimerListener listener, Date time)
   {
      return null;
   }

   public Timer schedule(TimerListener listener, Date time, long period)
   {
      return null;
   }

   public Timer schedule(TimerListener listener, long delay)
   {
      return null;
   }

   public Timer schedule(TimerListener listener, long delay, long period)
   {
      return null;
   }

   public void suspend()
   {

   }

   public Timer schedule(TimerExcecutionContext context)
   {
      return null;
   }

   public Timer schedule(TimerListener listener, Date time, TimerPeriod period)
   {
      return null;
   }

   public Timer schedule(TimerListener listener, TimerDate time, TimerPeriod period)
   {
      return null;
   }

   public void cancelTimer(Timer timer)
   {
      
   }

   public void cancelTimer(String timerName)
   {
      
   }

}
