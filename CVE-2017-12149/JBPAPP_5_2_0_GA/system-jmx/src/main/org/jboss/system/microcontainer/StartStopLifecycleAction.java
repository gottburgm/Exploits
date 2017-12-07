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
package org.jboss.system.microcontainer;

import javax.management.ObjectName;

import org.jboss.system.Service;
import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceController;
import org.jboss.system.ServiceMBean;

/**
 * StartStopLifecycleAction.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class StartStopLifecycleAction extends ServiceControllerContextAction
{
   public void installAction(ServiceControllerContext context) throws Throwable
   {
      Service service = context.getServiceProxy();
      service.start();

      context.getServiceContext().state = ServiceContext.RUNNING;

      ObjectName objectName = context.getObjectName();
      ServiceController serviceController = context.getServiceController();
      serviceController.sendControllerNotification(ServiceMBean.START_EVENT, objectName);            
   }

   public void uninstallAction(ServiceControllerContext context)
   {
      try
      {
         Service service = context.getServiceProxy();
         service.stop();

         context.getServiceContext().state = ServiceContext.STOPPED;

         ObjectName objectName = context.getObjectName();
         ServiceController serviceController = context.getServiceController();
         serviceController.sendControllerNotification(ServiceMBean.STOP_EVENT, objectName);            
      }
      catch (Throwable t)
      {
         log.debug("Error during stop for " + context.getObjectName(), t);
      }
   }
}
