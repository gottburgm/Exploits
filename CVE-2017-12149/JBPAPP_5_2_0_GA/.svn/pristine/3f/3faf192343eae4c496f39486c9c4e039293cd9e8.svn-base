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
package org.jboss.test.txtimer.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.logging.Logger;

/**
 * Session Bean Timer Test
 **/
public class TimerSessionBean
        implements SessionBean, TimedObject
{
   private static Logger log = Logger.getLogger(TimerSessionBean.class);

   private SessionContext context;

   // count calls to ejbTimeout
   private int callCount;

   // count calls to ejbTimeout
   private static int globalCallCount;

   // seen from ejbTimeout
   private Principal ejbTimeoutCaller;


   /**
    * @ejb.interface-method view-type="both"
    **/
   public void createTimer(long duration, long periode, Serializable info)
   {
      TimerService timerService = context.getTimerService();
      if (periode > 0)
         timerService.createTimer(duration, periode, info);
      else
         timerService.createTimer(duration, info);
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public void cancelFirstTimer()
   {
      TimerService timerService = context.getTimerService();
      if (timerService.getTimers().isEmpty())
         throw new EJBException("There are no timers");

      Timer timer = (Timer)timerService.getTimers().iterator().next();
      timer.cancel();
   }

   /**
    * This is not allowed on the remote interface.
    * @ejb.interface-method view-type="both"
    **/
   public Object createTimerReturnHandle(long duration)
   {
      TimerService timerService = context.getTimerService();
      Timer timer = timerService.createTimer(duration, null);
      return timer.getHandle();
   }

   /**
    * This is not allowed on the remote interface.
    * @ejb.interface-method view-type="both"
    **/
   public String passTimerHandle(Object handle)
   {
      return handle.toString();
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public void resetCallCount()
   {
      callCount = 0;
      globalCallCount = 0;
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public int getCallCount()
   {
      log.info("getCallCount [count=" + callCount + "]");
      return callCount;
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public int getGlobalCallCount()
   {
      log.info("getGlobalCallCount [count=" + globalCallCount + "]");
      return globalCallCount;
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public List getTimers()
   {
      TimerService timerService = context.getTimerService();

      ArrayList handles = new ArrayList();
      Iterator it = timerService.getTimers().iterator();
      while (it.hasNext())
      {
         Timer timer = (Timer) it.next();
         handles.add(timer.getHandle().toString());
      }
      return handles;
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public Principal getEjbTimeoutCaller()
   {
      return ejbTimeoutCaller;
   }

   public void ejbTimeout(Timer timer)
   {
      callCount++;
      globalCallCount++;

      log.info("ejbTimeout [count=" + callCount + "] timer=" + timer);

      ejbTimeoutCaller = context.getCallerPrincipal();
      log.info("ejbTimeout [callerPrincipal=" + ejbTimeoutCaller + "]");

      Serializable info = timer.getInfo();
      log.info("ejbTimeout [info=" + info + "]");
      
      if (info != null)
      {
         if (info instanceof Properties)
         {
            Properties props = (Properties)timer.getInfo();
            if ("true".equals(props.getProperty("cancel")))
               timer.cancel();
         }
      }
   }

   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      this.context = ctx;
   }

   /**
    * @ejb.create-method view-type="both"
    **/
   public void ejbCreate() throws CreateException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
}
