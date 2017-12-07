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

import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * TimerSLSB
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote
@RemoteBinding (jndiBinding = TimerSLSB.JNDI_NAME)
public class TimerSLSB implements SimpleTimer
{

   public static final String JNDI_NAME = "JBPAPP-4681-TimerSLSB";
   
   @Resource
   private TimerService timerService;
   
   private TimeoutTracker timeoutTracker;
   
   private static Logger logger = Logger.getLogger(TimerSLSB.class);
   
   @PostConstruct
   public void onConstruct()
   {
      this.timeoutTracker = TimeoutTracker.getInstance();
   }
   
   @Override
   public void createTimer(Date initialExpiry, long interval, String testName)
   {
      this.timerService.createTimer(initialExpiry, interval, testName);
      
   }

   @Override
   public void createTimer(Date initialExpiry, String testName)
   {
      this.timerService.createTimer(initialExpiry, testName);
      
   }
   
   @Timeout
   public void onTimeout(Timer timer)
   {
      logger.info("Timeout invoked at : " + new Date() + " for timer " + timer);
      this.timeoutTracker.trackTimeout(timer.getInfo().toString());
   }
   
   @Override
   public void cancelAllTimers()
   {
      Collection<Timer> timers = this.timerService.getTimers();
      for (Timer timer : timers)
      {
         timer.cancel();
      }
      
   }
   
   @Override
   public int getTimeoutCount(String name)
   {
      return this.timeoutTracker.getTimeoutCount(name);
   }

}
