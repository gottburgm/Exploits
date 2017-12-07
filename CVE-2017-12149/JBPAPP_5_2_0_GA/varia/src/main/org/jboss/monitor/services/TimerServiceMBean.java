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
package org.jboss.monitor.services;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

/**
 * MBean interface for a simple service used to configure the
 * periodic emition of notifications by a standard JMX Timer. 
 * 
 * @see javax.management.timer.Timer
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface TimerServiceMBean extends ServiceMBean
{
   // Attributes ----------------------------------------------------
   
   /**
    * The type of the emitted notifications
    */
   void setNotificationType(String type);
   String getNotificationType();
   
   /**
    * The message carried by the emitted notifications
    */
   void setNotificationMessage(String message);
   String getNotificationMessage();
   
   /**
    * Timer period in milliseconds; accepts suffix modifiers
    * e.g. "1000", "500msec", "10sec", "5min", "1h"
    */
   void setTimerPeriod(String timerPeriod);   
   String getTimerPeriod();
   
   /**
    * Number of occurences
    */
   void setRepeatitions(long repeatitions);
   long getRepeatitions();
   
   /**
    * The execution mode, true==fixed-rate, false==fixed-delay (default)
    */
   void setFixedRate(boolean fixedRate);
   boolean getFixedRate();
   
   /**
    * The utilized TimerMBean
    */
   void setTimerMBean(ObjectName timerMBean);
   ObjectName getTimerMBean();

}
