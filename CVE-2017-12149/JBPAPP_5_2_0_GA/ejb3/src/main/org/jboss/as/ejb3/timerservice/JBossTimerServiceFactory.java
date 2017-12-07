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

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory_2;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

import javax.ejb.TimerService;
import javax.management.ObjectName;

/**
 * Factory to create timer services which use the JBoss EJB Timer Service.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 112630 $
 */
public class JBossTimerServiceFactory implements TimerServiceFactory_2
{
   private static Logger log = Logger.getLogger(JBossTimerServiceFactory.class);
   
   public TimerService createTimerService(TimedObjectInvoker invoker)
   {
      TimerService timerService = null;
      try
      {
         EJBTimerService service = getEJBTimerService();
         ObjectName objectName = new ObjectName(invoker.getTimedObjectId());
         org.jboss.ejb.txtimer.TimedObjectInvoker bridge = new TimedObjectInvokerBridge(invoker);
         TimerService delegate = service.createTimerService(objectName, null, bridge);
         timerService = new TimerServiceFacade(objectName, delegate);
      }
      catch (Exception e)
      {
         //throw new EJBException("Could not create timer service", e);
         if (log.isTraceEnabled())
         {
            log.trace("Unable to initialize timer service", e);
         }
         else
         {
            log.trace("Unable to initialize timer service");
         }
      }
      return timerService;
   }

   protected EJBTimerService getEJBTimerService()
   {
      return (EJBTimerService) MBeanProxyExt.create(EJBTimerService.class, EJBTimerService.OBJECT_NAME, MBeanServerLocator.locateJBoss());
   }
   
   public void removeTimerService(TimerService timerService)
   {
      removeTimerService(((TimerServiceFacade) timerService).getContainerId());
   }

   protected void removeTimerService(ObjectName containerId)
   {
      try
      {
         EJBTimerService service = getEJBTimerService();
         service.removeTimerService(containerId, true);
      }
      catch (Exception e)
      {
         log.warn("Unable to remove timer service", e);
      }
   }

   public void restoreTimerService(TimerService aTimerService)
   {
      log.warn("JBPAPP-3308: deprecated restoreTimerService(TimerService) is called");
      restoreTimerService(aTimerService, null);
   }

   public void restoreTimerService(TimerService aTimerService, ClassLoader loader)
   {
      if (aTimerService == null)
      {
         log.warn("TIMER SERVICE IS NOT INSTALLED");
         return;
      }
      TimerServiceFacade timerService = (TimerServiceFacade) aTimerService;
      
      // FIXME: A hack to circumvent the check in TimerServiceFacade
      // In AS itself (/EJB2) the container has an unsecured timer service association
      // see org.jboss.ejb.Container.getTimerService(Object pKey)
      AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsAssociation.IN_BUSINESS_METHOD);
      try
      {
         getEJBTimerService().restoreTimers(timerService.getContainerId(), loader);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }

   public void suspendTimerService(TimerService timerService)
   {
      removeTimerService(timerService);
   }
}
