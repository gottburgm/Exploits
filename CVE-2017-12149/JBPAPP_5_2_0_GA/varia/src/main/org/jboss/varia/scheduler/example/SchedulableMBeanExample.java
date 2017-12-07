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
package org.jboss.varia.scheduler.example;

import java.security.InvalidParameterException;
import java.util.Date;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.timer.TimerNotification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.varia.scheduler.Schedulable;

/**
 * A sample SchedulableMBean that records when an event is received.
 * 
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author Cameron (camtabor)
 * @version $Revision: 81038 $
 *  
 **/
public class SchedulableMBeanExample
   extends ServiceMBeanSupport
   implements SchedulableMBeanExampleMBean
{

   private Notification notification;
   private Date date;
   private long repetitions;
   private ObjectName name;
   private String test;
   private int hitCount;
   
   // -------------------------------------------------------------------------
   // SchedulableExampleMBean Methods
   // -------------------------------------------------------------------------
   
   /**
	* Called by ScheduleManager.
    * @jmx:managed-operation
    */
   public void hit(Notification notification, Date date, long repetitions, ObjectName name, String test)
   {
      log.info("got hit");
	  this.notification = notification;
	  this.date = date;
	  this.repetitions = repetitions;
	  this.name = name;
	  this.test = test;
      hitCount++;
      log.info(this.toString());
   }

  /**
   * Returns the number of hits.
   * @jmx:managed-attribute
   */   
   public int getHitCount()
   {
     return hitCount;
   }
   
  /**
   * Returns the last hit date.
   * @jmx:managed-attribute
   */   
   public Date getHitDate()
   {
     return date;
   }
   
  /**
   * Returns the last hit notification.
   * @jmx:managed-attribute
   */   
   public Notification getHitNotification()
   {
     return notification;
   }
   
  /**
   * Returns the last hit date.
   * @jmx:managed-attribute
   */   
   public long getRemainingRepetitions()
   {
     return repetitions;
   }
   
  /**
   * Returns the object name.
   * @jmx:managed-attribute
   */   
   public ObjectName getSchedulerName()
   {
     return name;
   }
   
  /**
   * Returns the test string.
   * @jmx:managed-attribute
   */   
   public String getTestString()
   {
     return test;
   }
   
   /**
	* Returns a debug string.
	*/
   public String toString() {
      return super.toString() 
		 + " name=" + getName()
		 + " hitCount=" + hitCount
         + " notification=" + notification
         + " date=" + date
         + " repetitions=" + repetitions
         + " name=" + name
         + " test string=" + test;
   }
}
