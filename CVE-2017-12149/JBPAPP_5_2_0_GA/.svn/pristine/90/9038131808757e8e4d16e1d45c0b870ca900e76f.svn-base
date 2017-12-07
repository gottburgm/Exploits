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
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.logging.Logger;

/**
 * Entity Bean Timer Test
 **/
public class TimerEntityBean
        implements EntityBean, TimedObject
{
   private static Logger log = Logger.getLogger(TimerEntityBean.class);

   private EntityContext context;

   // count calls to ejbTimeout
   private int callCount;

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
   }

   /**
    * @ejb.interface-method view-type="both"
    **/
   public int getCallCount()
   {
      Object pk = context.getPrimaryKey();
      log.info("getCallCount [pk=" + pk + ",count=" + callCount + ",this=" + this + "]");
      return callCount;
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
      Object pk = context.getPrimaryKey();
      log.info("ejbTimeout [pk=" + pk + ",count=" + callCount + ",this=" + this + "] timer=" + timer);

      ejbTimeoutCaller = context.getCallerPrincipal();

      if (timer.getInfo() != null)
      {
         Properties props = (Properties)timer.getInfo();
         if ("true".equals(props.getProperty("rollback")))
         {
            props.setProperty("rollback", "false");
            throw new IllegalStateException("rollback on ejbTimeout");
         }
      }
   }

   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------

   /**
    * @ejb.create-method view-type="both"
    **/
   public Integer ejbCreate(Integer pk) throws CreateException
   {
      log.info("ejbCreate [pk=" + pk + "]");
      return pk;
   }

   public void ejbPostCreate(Integer pk) throws CreateException
   {
   }

   /**
    * @ejb.finder view-type="both"
    **/
   public Integer ejbFindByPrimaryKey(Integer pk) throws FinderException
   {
      return pk;
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbLoad() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws RemoveException, EJBException, RemoteException
   {
   }

   public void ejbStore() throws EJBException, RemoteException
   {
   }

   public void setEntityContext(EntityContext ctx) throws EJBException, RemoteException
   {
      this.context = ctx;
   }

   public void unsetEntityContext() throws EJBException, RemoteException
   {
   }
}
