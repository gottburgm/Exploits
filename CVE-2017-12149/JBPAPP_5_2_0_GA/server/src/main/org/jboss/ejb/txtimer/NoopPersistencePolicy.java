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

// $Id: NoopPersistencePolicy.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * This service implements a PersistencePolicy that does not persist the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81030 $
 * @since 09-Sep-2004
 */
public class NoopPersistencePolicy implements NoopPersistencePolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(NoopPersistencePolicy.class);

   /**
    * Creates the timer in  persistent storage.
    *
    * @param timerId       The timer id
    * @param timedObjectId The timed object id
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    * @param firstEvent    The point in time at which the first txtimer expiration must occur.
    * @param periode       The number of milliseconds that must elapse between txtimer expiration notifications.
    */
   public void insertTimer(String timerId, TimedObjectId timedObjectId, Date firstEvent, long periode, Serializable info)
   {
      log.debug("Noop on insertTimer");
   }

   /**
    * Removes the timer from persistent storage.
    *
    * @param timerId The timer id
    * @param timedObjectId The id of the timed object
    */
   public void deleteTimer(String timerId, TimedObjectId timedObjectId)
   {
      log.debug("Noop on deleteTimer");
   }

   /**
    * Delete all persisted timers
    */
   public void clearTimers()
   {
      log.debug("Noop on clearTimers");
   }
   
   /**
    * Restore the persistet timers
    */
   public void restoreTimers()
   {
      log.debug("Noop on restoreTimers");
   }

   /**
    * List the persisted timer handles
    *
    * @param loader The ClassLoader to use for loading the handles
    * @return a list of TimerHandleImpl objects
    */
   public List listTimerHandles(ObjectName containerId, ClassLoader loader)
   {
      log.debug("Noop on listTimerHandles");
      return new ArrayList();
   }
   
   /**
    * Return a List of TimerHandle objects.
    */
   public List listTimerHandles()
   {
      log.debug("Noop on listTimerHandles");
      return new ArrayList();
   }
   
   /**
    * List the persisted timers for a particular TimerObjectId,
    * or all persisted timers if timedObjectId is null.
    * 
    * @param timedObjectId The id of the timed object, or null
    * @return a list of TimerHandleImpl objects
    */
   public List listTimerHandles(TimedObjectId timedObjectId)
   {
      log.debug("Noop on listTimerHandles");
      return new ArrayList();
   }
   
}
