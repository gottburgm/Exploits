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
package org.jboss.ejb.txtimer;

// $Id: PersistencePolicy.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.management.ObjectName;

/**
 * Timers are persistent objects. In the event of a container crash, any single-event timers that have expired
 * during the intervening time before container restart must cause the ejbTimeout method to be invoked
 * upon restart. Any interval timers that have expired during the intervening time must cause the ejb-
 * Timeout method to be invoked at least once upon restart.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81030 $
 * @since 09-Sep-2004
 */
public interface PersistencePolicy
{
   /**
    * Inserts a timer into persistent storage.
    *
    * @param timerId    The timer id
    * @param targetId   The timed object id
    * @param firstEvent The point in time at which the first txtimer expiration must occur.
    * @param periode    The number of milliseconds that must elapse between txtimer expiration notifications.
    * @param info       A serializable handback object.
    */
   void insertTimer(String timerId, TimedObjectId targetId, Date firstEvent, long periode, Serializable info);

   /**
    * Deletes a timer from persistent storage.
    *
    * @param timerId The timer id
    * @param timedObjectId The id of the timed object
    */
   void deleteTimer(String timerId, TimedObjectId timedObjectId);

   /**
    * List the persisted timer handles for a particular container
    *
    * @param containerId The Container ObjectName
    * @param loader The ClassLoader to use for loading the handles
    * @return a list of TimerHandleImpl objects
    */
   List listTimerHandles(ObjectName containerId, ClassLoader loader);
   
   /**
    * List all the persisted timer handles
    *
    * @return a list of TimerHandleImpl objects
    */
   List listTimerHandles();
   
   /**
    * Clear the persisted timers
    */
   void clearTimers();
   
}
