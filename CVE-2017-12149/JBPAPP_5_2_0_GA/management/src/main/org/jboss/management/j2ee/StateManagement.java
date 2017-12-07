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
package org.jboss.management.j2ee;

import org.jboss.system.ServiceMBean;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.security.InvalidParameterException;

/**
 * Root class of the JBoss JSR-77 implementation of StateManagement
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class StateManagement implements NotificationListener
{
   // Constants -----------------------------------------------------
   // These are not defined by JSR-77 as valid states
   public static final int CREATED = 5;
   public static final int DESTROYED = 6;
   public static final int REGISTERED = 7;
   public static final int UNREGISTERED = 8;

   /**
    * The int state to state name mappings
    */
   public static final String[] stateTypes = new String[]
   {
      NotificationConstants.STATE_STARTING,
      NotificationConstants.STATE_RUNNING,
      NotificationConstants.STATE_STOPPING,
      NotificationConstants.STATE_STOPPED,
      NotificationConstants.STATE_FAILED,
      NotificationConstants.OBJECT_CREATED,
      NotificationConstants.OBJECT_DELETED,
      NotificationConstants.OBJECT_REGISTERED,
      NotificationConstants.OBJECT_DELETED
   };

   // Attributes ----------------------------------------------------

   private long startTime = -1;
   private int state = StateManageable.UNREGISTERED;
   private J2EEManagedObject managedObject;

   // Static --------------------------------------------------------

   /**
    * Converts a state from JBoss ServiceMBean to the JSR-77 state
    *
    * @param theState the JBoss ServiceMBean state.
    * @return Converted state or -1 if unknown.
    */
   public static int convertJBossState(int theState)
   {
      int jsr77State = -1;
      switch (theState)
      {
         case ServiceMBean.STARTING:
            jsr77State = StateManageable.STARTING;
            break;
         case ServiceMBean.STARTED:
            jsr77State = StateManageable.RUNNING;
            break;
         case ServiceMBean.STOPPING:
            jsr77State = StateManageable.STOPPING;
            break;
         case ServiceMBean.STOPPED:
            jsr77State = StateManageable.STOPPED;
            break;
         case ServiceMBean.FAILED:
            jsr77State = StateManageable.FAILED;
            break;
         case ServiceMBean.CREATED:
            jsr77State = CREATED;
            break;
         case ServiceMBean.DESTROYED:
            jsr77State = DESTROYED;
            break;
         case ServiceMBean.REGISTERED:
            jsr77State = REGISTERED;
            break;
         case ServiceMBean.UNREGISTERED:
            jsr77State = UNREGISTERED;
            break;
         default:
            jsr77State = -1;
            break;
      }
      return jsr77State;
   }

   /**
    * Converts a JSR-77 state to the JBoss ServiceMBean state
    *
    * @param theState the JSR-77 state.
    * @return Converted state or -1 if unknown.
    */
   public static int convertJSR77State(int theState)
   {
      int jbossState = -1;
      switch (theState)
      {
         case StateManageable.STARTING:
            jbossState = ServiceMBean.STARTING;
            break;
         case StateManageable.RUNNING:
            jbossState = ServiceMBean.STARTED;
            break;
         case StateManageable.STOPPING:
            jbossState = ServiceMBean.STOPPING;
            break;
         case StateManageable.STOPPED:
            jbossState = ServiceMBean.STOPPED;
            break;
         case StateManageable.FAILED:
            jbossState = ServiceMBean.FAILED;
            break;
         case CREATED:
            jbossState = ServiceMBean.CREATED;
            break;
         case DESTROYED:
            jbossState = ServiceMBean.DESTROYED;
            break;
         case REGISTERED:
            jbossState = ServiceMBean.REGISTERED;
            break;
         case UNREGISTERED:
            jbossState = ServiceMBean.UNREGISTERED;
            break;
      }
      return jbossState;
   }

   // Constructors --------------------------------------------------
   /**
    * @param managedObject
    * @throws InvalidParameterException If the given Name is null
    */
   public StateManagement(J2EEManagedObject managedObject)
   {
      if (managedObject == null)
      {
         throw new InvalidParameterException("managedObject must not be null");
      }
      this.managedObject = managedObject;
      this.startTime = System.currentTimeMillis();
   }

   // Public --------------------------------------------------------

   public long getStartTime()
   {
      return startTime;
   }

   public void setStartTime(long pTime)
   {
      startTime = pTime;
   }

   public int getState()
   {
      return state;
   }

   public String getStateString()
   {
      String stateName = stateTypes[state];
      return stateName;
   }

   /**
    * Sets a new state and if it changed the appropriate state change event
    * is sent.
    *
    * @param newState Integer indicating the new state according to
    *                 {@link org.jboss.management.j2ee.StateManageable StateManageable}
    *                 constants
    */
   public void setState(int newState)
   {
      // Only send a notification if the state really changes
      if (0 <= newState && newState < stateTypes.length)
      {
         if (newState != state)
         {
            state = newState;
            // Now send the event to the JSR-77 listeners
            String type = stateTypes[state];
            managedObject.sendNotification(type, "State changed");
         }
      }
   }

   // NotificationListener overrides ---------------------------------

   /**
    * A notification from the underlying JBoss service.
    *
    * @param msg      The notification msg, AttributeChangeNotification is what we
    *                 care about
    * @param handback not used
    */
   public void handleNotification(Notification msg, Object handback)
   {
      if (msg instanceof AttributeChangeNotification)
      {
         AttributeChangeNotification change = (AttributeChangeNotification) msg;
         String attrName = change.getAttributeName();
         Object newValue = change.getNewValue();
         if ("State".equals(attrName) && newValue != null && newValue instanceof Integer)
         {
            int newState = ((Integer) newValue).intValue();
            long eventTime = -1;
            if (newState == ServiceMBean.STARTED)
            {
               eventTime = change.getTimeStamp();
            }
            if (newState == ServiceMBean.STARTED)
               setStartTime(eventTime);
            int jsr77State = convertJBossState(newState);
            setState(jsr77State);
         }
      }
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "StateManagement [ " +
              "State: " + state +
              ", Start Time: " + startTime +
              " ]";
   }
}
