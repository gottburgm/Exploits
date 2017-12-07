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
package org.jboss.as.ejb3.timerservice;

import static org.jboss.ejb.AllowedOperationsFlags.IN_BUSINESS_METHOD;
import static org.jboss.ejb.AllowedOperationsFlags.IN_EJB_TIMEOUT;
import static org.jboss.ejb.AllowedOperationsFlags.IN_SERVICE_ENDPOINT_METHOD;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.txtimer.PersistentIdTimerService;
import org.jboss.ejb.txtimer.TimerRestoringTimerService;
import org.jboss.logging.Logger;

/**
 * Holds the association with the container, without exposing it.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href=mailto:miclark@redhat.com">Mike M. Clark</a>
 * 
 * @version $Revision: 107116 $
 */
public class TimerServiceFacade implements TimerRestoringTimerService
{
   private static Logger log = Logger.getLogger(TimerServiceFacade.class);
   
   private ObjectName objectName;
   private TimerService delegate;
   
   protected TimerServiceFacade(ObjectName objectName, TimerService delegate)
   {
      this.objectName = objectName;
      this.delegate = delegate;
   }

   private void assertAllowedIn(String timerMethod)
   {
      // TODO: This isn't handled by the AS timer service itself
      AllowedOperationsAssociation.assertAllowedIn(timerMethod, IN_BUSINESS_METHOD | IN_EJB_TIMEOUT | IN_SERVICE_ENDPOINT_METHOD);
   }
   
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      assertAllowedIn("TimerService.createTimer");
      return delegate.createTimer(initialExpiration, intervalDuration, info);
   }

   public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      assertAllowedIn("TimerService.createTimer");
      return delegate.createTimer(expiration, info);
   }

   public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      assertAllowedIn("TimerService.createTimer");
      return delegate.createTimer(initialDuration, intervalDuration, info);
   }
   
   // JBPAPP-3926
   @Override
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info, String timerId) throws IllegalArgumentException, IllegalStateException, EJBException
   {
	  if (delegate instanceof PersistentIdTimerService)
	  {
		  PersistentIdTimerService persistentTimerService = (PersistentIdTimerService) delegate;
		  return persistentTimerService.createTimer(initialExpiration, intervalDuration, info, timerId);
	  }
	  else
	  {
		  log.warn("Unable to preserve timerId. Will generate new timerId");
		  return delegate.createTimer(initialExpiration, intervalDuration, info);
	  }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Timer restoreTimer(Date initialExpiration, long intervalDuration, Date nextExpiry, Serializable info,
         String timerId) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (delegate instanceof TimerRestoringTimerService)
      {
         TimerRestoringTimerService persistentTimerService = (TimerRestoringTimerService) delegate;
         // restore the timer
         return persistentTimerService.restoreTimer(initialExpiration, intervalDuration, nextExpiry, info, timerId);
      }
      else
      {
         log.warn("Unable to restore timer, since the delegate timerservice " + delegate.getClass() + " isn't of type "
               + TimerRestoringTimerService.class + " - will create the timer afresh");
          // we can't "restore" the timer state, so let's just recreate the timer afresh, using the initial expiry date and the repeat interval
          return delegate.createTimer(initialExpiration, intervalDuration, info);
      }
   }

   public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      assertAllowedIn("TimerService.createTimer");
      return delegate.createTimer(duration, info);
   }

//   protected EJBContainer getContainer()
//   {
//      return (EJBContainer) container;
//   }
   
   protected ObjectName getContainerId()
   {
      return objectName;
   }
   
   public Collection<?> getTimers() throws IllegalStateException, EJBException
   {
      assertAllowedIn("TimerService.getTimers");
      return delegate.getTimers();
   }
}
