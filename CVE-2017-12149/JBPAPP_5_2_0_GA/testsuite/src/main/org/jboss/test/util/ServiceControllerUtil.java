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
package org.jboss.test.util;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceControllerMBean;

/**
 *  Utility class that can deal with the ServiceController to
 *  start/stop/create/destroy a service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @version $Revision: 85945 $
 *  @since  Aug 23, 2006
 */
public class ServiceControllerUtil
{
   private ServiceControllerMBean scmb = null;
   
   public ServiceControllerUtil(MBeanServerConnection server)
   {   
      ObjectName so = ServiceControllerMBean.OBJECT_NAME;
      scmb = (ServiceControllerMBean)
      MBeanServerInvocationHandler.newProxyInstance(server, so,
               ServiceControllerMBean.class, false);
   }

   public String getStateString(ObjectName serviceName)
   {
      ServiceContext ctx = scmb.getServiceContext(serviceName);
      return ctx.getStateString();
   }
   public void createAService(ObjectName serviceName) throws Exception
   { 
      scmb.create(serviceName); 
   }
   
   public void startAService(ObjectName serviceName) throws Exception
   { 
      scmb.start(serviceName); 
   }
   
   public void stopAService(ObjectName serviceName) throws Exception
   {  
      scmb.stop(serviceName); 
   }
   
   public void destroyAService(ObjectName serviceName) throws Exception
   { 
      scmb.destroy(serviceName);
   } 
   
   public void removeAService(ObjectName serviceName) throws Exception
   { 
      scmb.remove(serviceName);
   } 

   public boolean isStarted(ObjectName serviceName)
   {
      ServiceContext sc = scmb.getServiceContext(serviceName);
      return sc.state == ServiceContext.RUNNING;
   }
   
   public boolean isCreated(ObjectName serviceName)
   {
      ServiceContext sc = scmb.getServiceContext(serviceName);
      return sc.state == ServiceContext.CREATED;
   }
   
   public boolean isStopped(ObjectName serviceName)
   {
      ServiceContext sc = scmb.getServiceContext(serviceName);
      return sc.state == ServiceContext.STOPPED;
   }
   
   public boolean isDestroyed(ObjectName serviceName)
   {
      ServiceContext sc = scmb.getServiceContext(serviceName);
      return sc.state == ServiceContext.DESTROYED;
   }
   
   public boolean isFailed(ObjectName serviceName)
   {
      ServiceContext sc = scmb.getServiceContext(serviceName);
      return sc.state == ServiceContext.FAILED;
   }
}
