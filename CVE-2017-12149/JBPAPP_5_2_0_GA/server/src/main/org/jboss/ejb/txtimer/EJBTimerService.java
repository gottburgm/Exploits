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

// $Id: EJBTimerService.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * A service that implements this interface provides a Tx aware EJBTimerService.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81030 $
 * @since 07-Apr-2004
 */
public interface EJBTimerService
{
   /**
    * The default object name
    */
   static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBTimerService");

   /**
    * Create a TimerService for a given containerId/pKey (TimedObjectId) that lives in a JBoss Container.
    * The TimedObjectInvoker is constructed from the invokerClassName.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    * @param container   The Container that is associated with the TimerService
    * @return the TimerService
    */
   TimerService createTimerService(ObjectName containerId, Object pKey, Container container) throws IllegalStateException;

   /**
    * Create a TimerService for a given containerId/pKey (TimedObjectId) that is invoked through the given invoker.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    * @param invoker     The TimedObjectInvoker
    * @return the TimerService
    */
   TimerService createTimerService(ObjectName containerId, Object pKey, TimedObjectInvoker invoker) throws IllegalStateException;

   /**
    * Get the TimerService for a given containerId/pKey (TimedObjectId).
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    * @return The TimerService, or null if it does not exist
    */
   TimerService getTimerService(ObjectName containerId, Object pKey) throws IllegalStateException;

   /**
    * Remove the TimerService for a given containerId/pKey (TimedObjectId),
    * along with any persisted timer information.
    * 
    * This should be used for removing the TimerService and Timers
    * associated with a particular entity bean, when it gets removed.
    * 
    * @param containerId The string identifier for a class of TimedObjects
    * @param pKey        The primary key for an instance of a TimedObject, may be null
    */
   void removeTimerService(ObjectName containerId, Object pKey) throws IllegalStateException;
   
   /**
    * Remove the TimerService for a given containerId.
    * 
    * This should be used to remove the timer service and timers for
    * any type of container (session, entity, message) at the time of
    * undeployment.
    *
    * @param containerId The string identifier for a class of TimedObjects
    * @param keepState   Flag indicating whether timer persistent state should be kept or removed 
    */
   void removeTimerService(ObjectName containerId, boolean keepState) throws IllegalStateException;
   
   /**
    * Restore the persisted timers for a given ejb container
    * 
    * @param containerId The ejb container id
    * @param loader      The classloader to use for loading the timers
    */
   void restoreTimers(ObjectName containerId, ClassLoader loader) throws IllegalStateException;
   
}
