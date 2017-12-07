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

// $Id: EJBTimerServiceLocator.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import javax.ejb.TimerService;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Locates the EJBTimerService, either as MBean or as local instance.
 * 
 * It first checks if the EJBTimerServiceImpl is registered with the MBeanServer,
 * if not it creates a singleton and uses that.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81030 $
 * @since 07-Apr-2004
 */
public class EJBTimerServiceLocator
{
   // logging support
   private static Logger log = Logger.getLogger(EJBTimerServiceLocator.class);

   private static EJBTimerService ejbTimerService;

   /**
    * Locates the EJBTimerService, first as MBean, then as singleton
    */
   public static EJBTimerService getEjbTimerService()
   {
      try
      {
         // First try the MBean server
         MBeanServer server = MBeanServerLocator.locateJBoss();
         if (server != null && server.isRegistered(EJBTimerService.OBJECT_NAME))
            ejbTimerService = new MBeanDelegate(server);
      }
      catch (Exception ignore)
      {
      }

      // This path can be used for standalone test cases
      if (ejbTimerService == null)
      {
         EJBTimerServiceImpl ejbTimerServiceImpl = new EJBTimerServiceImpl();
         ejbTimerService = ejbTimerServiceImpl;         
         try
         {
            ejbTimerServiceImpl.create();
            ejbTimerServiceImpl.start();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Cannot start EJBTimerService", e);
         }
      }
      return ejbTimerService;
   }

   /**
    * Delegates method calls to the EJBTimerService to the MBean server
    */
   public static class MBeanDelegate implements EJBTimerService
   {
      private EJBTimerService mbeanEjbTimerService;

      public MBeanDelegate(MBeanServer server)
      {
         try
         {
            mbeanEjbTimerService = (EJBTimerService)MBeanProxyExt.create(EJBTimerService.class, EJBTimerService.OBJECT_NAME, server);
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Cannot create EJBTimerService proxy: " + e.getMessage());
         }
      }

      public TimerService createTimerService(ObjectName containerId, Object instancePk, Container container)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = mbeanEjbTimerService.createTimerService(containerId, instancePk, container);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService createTimerService(ObjectName containerId, Object instancePk, TimedObjectInvoker invoker)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = mbeanEjbTimerService.createTimerService(containerId, instancePk, invoker);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot createTimerService", e);
            return null;
         }
      }

      public TimerService getTimerService(ObjectName containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            TimerService timerService = mbeanEjbTimerService.getTimerService(containerId, instancePk);
            return timerService;
         }
         catch (Exception e)
         {
            log.error("Cannot getTimerService", e);
            return null;
         }
      }

      public void removeTimerService(ObjectName containerId, Object instancePk)
              throws IllegalStateException
      {
         try
         {
            mbeanEjbTimerService.removeTimerService(containerId, instancePk);
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }
      
      public void removeTimerService(ObjectName containerId, boolean keepState) throws IllegalStateException
      {
         try
         {
            mbeanEjbTimerService.removeTimerService(containerId, keepState);
         }
         catch (Exception e)
         {
            log.error("Cannot removeTimerService", e);
         }
      }
      
      public void restoreTimers(ObjectName containerId, ClassLoader loader) throws IllegalStateException
      {
         try
         {
            mbeanEjbTimerService.restoreTimers(containerId, loader);
         }
         catch (Exception e)
         {
            log.error("Cannot restoreTimer", e);
         }
      }
   }
}
