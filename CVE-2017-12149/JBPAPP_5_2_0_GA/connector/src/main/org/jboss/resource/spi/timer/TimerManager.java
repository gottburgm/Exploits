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
package org.jboss.resource.spi.timer;

import java.util.Date;

/**
 * The TimerManager interface represents core timer management facilities.
 * Primary responsiblities include scheduling timers, removing timers. Further,
 * the interface allows for certain lifecycle manipulation of the TimerManager.
 * 
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 71554 $
 * 
 * @see Timer
 * @see TimerListener
 * @see CancelTimerListener
 * @see StopTimerListener
 * 
 */
public interface TimerManager
{
   
   /**
    * Request that the TimerManager schedule a timer that will expire at 
    * the specified date.
    * 
    * @param listener the timer listener.
    * @param time the time that the timer will expire.
    * 
    * @see TimerListener
    * @see Timer
    * 
    * @return the newly created and scheduled timer.
    * 
    * 
    */
   public Timer schedule(TimerListener listener, Date time);
   
   
   /**
    * Create and schedule a timer with the listener.
    * 
    * @param listener
    * @param time
    * @param period
    * @return
    */
   public Timer schedule(TimerListener listener, Date time, long period);
   
   /**
    * FIXME Comment this
    * 
    * @param listener
    * @param delay
    * @return
    */
   public Timer schedule(TimerListener listener, long delay);
   
   /**
    * FIXME Comment this
    * 
    * @param listener
    * @param delay
    * @param period
    * @return
    */
   public Timer schedule(TimerListener listener, long delay, long period) ;
   
   /**
    * FIXME Comment this
    * 
    * @param context
    * @return
    */
   public Timer schedule(TimerExcecutionContext context);
   
   /**
    * FIXME Comment this
    * 
    * @param listener
    * @param time
    * @param period
    * @return
    */
   public Timer schedule(TimerListener listener, Date time, TimerPeriod period);
   
   /**
    * FIXME Comment this
    * 
    * @param listener
    * @param time
    * @param period
    * @return
    */
   public Timer schedule(TimerListener listener, TimerDate time, TimerPeriod period);
   
   /**
    * FIXME Comment this
    * 
    * @param timer
    */
   public void cancelTimer(Timer timer);
   
   /**
    * FIXME Comment this
    * 
    * @param timerName
    */
   public void cancelTimer(String timerName);
  
   
   /**
    * FIXME Comment this
    * 
    */
   public void stop();
   
   /**
    * FIXME Comment this
    * 
    */
   public void suspend();
   
   /**
    * FIXME Comment this
    * 
    */
   public void resume();
   
   /**
    * FIXME Comment this
    * 
    * @return
    */
   public boolean isSuspending();
   
   
   /**
    * FIXME Comment this
    * 
    * @return
    */
   public boolean isStopped();
   
   
}
